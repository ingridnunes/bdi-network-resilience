//----------------------------------------------------------------------------
// Copyright (C) 2011  Ingrid Nunes
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://inf.ufrgs.br/prosoft/bdi4jade/
//
//----------------------------------------------------------------------------
package br.ufrgs.inf.bdinetr.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames.ContentLanguage;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bdi4jade.belief.Predicate;
import bdi4jade.belief.TransientPredicate;
import bdi4jade.core.BDIAgent;
import bdi4jade.core.Capability;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.BeliefPresentGoal;
import bdi4jade.goal.BeliefValueGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalTemplate;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.message.GoalProposal;
import br.ufrgs.inf.bdinetr.domain.message.GoalRequest;
import br.ufrgs.inf.bdinetr.domain.message.GoalResponse;
import br.ufrgs.inf.bdinetr.domain.ontology.BDINetROntology;

/**
 * @author Ingrid Nunes
 */
public class GoalRequestPlan extends DefaultPlan {

	public class GoalRequestPlanBody extends AbstractPlanBody {
		private static final long serialVersionUID = -1833810388789537049L;

		private int answers;
		private MessageTemplate mt;
		private Set<ACLMessage> positiveAnswers;
		private RequestDescription requestDescription;
		private int requests;
		private long requestTime;
		private State state;

		private void achieveGoal() throws Exception {
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				if (ACLMessage.INFORM == reply.getPerformative()) {
					log.debug("Goal " + getGoal() + " achieved.");
					if (getGoal() instanceof BeliefGoal) {
						GoalResponse<?> response = (GoalResponse<?>) myAgent
								.getContentManager().extractContent(reply);

						Predicate<?> predicate = new TransientPredicate(
								response.getPredicate(), response.getValue());
						((BeliefGoal) getGoal()).setOutputBelief(predicate);

						if (requestDescription.subscribe) {
							myAgent.addBehaviour(new ReceiveUpdatesBehavior(
									myAgent, mt, requestDescription.capability,
									predicate));
						}
					}
					setEndState(EndState.SUCCESSFUL);
				} else {
					setEndState(EndState.FAILED);
				}
				this.state = State.Ended;
			} else {
				block();
			}
		}

		@Override
		public void action() {
			try {
				switch (state) {
				case Resquesting:
					request();
					break;
				case ReceivingResponses:
					receiveResponse();
					break;
				case Selecting:
					selectProposal();
					break;
				case AchievingGoal:
					achieveGoal();
					break;
				case Ended:
					break;
				}
			} catch (Exception exc) {
				log.error(exc);
				exc.printStackTrace();
				setEndState(EndState.FAILED);
			}
		}

		private DFAgentDescription[] getReceivers(Role role) {
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(role.name());
			sd.addLanguages(ContentLanguage.FIPA_SL);
			sd.addOntologies(BDINetROntology.ONTOLOGY_NAME);
			dfd.addServices(sd);

			try {
				return DFService.search(myAgent, dfd);
			} catch (FIPAException fe) {
				log.error(myAgent.getLocalName()
						+ " search with DF unsucceeded. Reason: "
						+ fe.getMessage());
				log.error(fe);
				fe.printStackTrace();
				return new DFAgentDescription[0];
			}
		}

		@Override
		public void onStart() {
			this.state = State.Resquesting;
			this.requestDescription = getRequestDescription(getGoal());
		}

		private void receiveResponse() {
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				log.debug("Goal: " + getGoal());
				this.answers++;
				if (ACLMessage.PROPOSE == reply.getPerformative()) {
					log.debug("Agent " + reply.getSender().getLocalName()
							+ " sent a proposal.");
					positiveAnswers.add(reply);
				} else {
					log.debug("Agent " + reply.getSender().getLocalName()
							+ " refused the request.");
				}
			} else {
				block(MSG_TIME_OUT);
			}
			long timeElapsed = System.currentTimeMillis() - requestTime;
			if (answers >= requests || timeElapsed >= ANSWER_TIME_OUT) {
				log.debug("Received answers: " + answers + " (of " + requests
						+ ")");
				this.state = State.Selecting;
			} else {
				log.debug("Waiting for more answers...");
			}
		}

		private void request() throws Exception {
			log.debug("Goal: " + getGoal());
			this.requestTime = System.currentTimeMillis();

			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.setLanguage(ContentLanguage.FIPA_SL);
			msg.setOntology(BDINetROntology.ONTOLOGY_NAME);
			msg.setConversationId("cin" + requestTime);
			msg.setReplyWith("cfp" + requestTime);

			Set<AID> receivers = new HashSet<>();
			for (DFAgentDescription agentDesc : getReceivers(requestDescription.role)) {
				if (receivers.add(agentDesc.getName())) {
					msg.addReceiver(agentDesc.getName());
				}
			}
			log.debug("Number of requests: " + receivers.size());

			GoalRequest request = new GoalRequest();
			if (getGoal() instanceof BeliefPresentGoal) {
				BeliefPresentGoal<?> bg = (BeliefPresentGoal<?>) getGoal();
				request.setPredicate(bg.getBeliefName());
			} else if (getGoal() instanceof BeliefValueGoal) {
				BeliefValueGoal<?, Boolean> bg = (BeliefValueGoal<?, Boolean>) getGoal();
				request.setPredicate(bg.getBeliefName());
				request.setValue(bg.getValue());
			} else {
				request.setPredicate(getGoal());
				request.setBeliefGoal(false);
			}
			request.setSubscribe(requestDescription.subscribe);
			myAgent.getContentManager().fillContent(msg, request);

			myAgent.send(msg);

			this.mt = MessageTemplate.and(MessageTemplate
					.MatchConversationId(msg.getConversationId()),
					MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
			this.requests = receivers.size();
			this.answers = 0;
			this.positiveAnswers = new HashSet<>();
			this.state = State.ReceivingResponses;
		}

		private void selectProposal() throws Exception {
			log.debug("Goal: " + getGoal());
			if (positiveAnswers.isEmpty()) {
				log.debug("No positive answers");
				setEndState(EndState.FAILED);
				this.state = State.Ended;
				return;
			} else {
				ACLMessage chosenMsg = null;
				Double lowestCost = null;
				for (ACLMessage answer : positiveAnswers) {
					GoalProposal proposal = (GoalProposal) myAgent
							.getContentManager().extractContent(answer);
					if (lowestCost == null || lowestCost > proposal.getCost()) {
						chosenMsg = answer;
						lowestCost = proposal.getCost();
					}
				}

				for (ACLMessage answer : positiveAnswers) {
					ACLMessage reply = answer.createReply();
					if (answer == chosenMsg) {
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						reply.setReplyWith("atp" + System.currentTimeMillis());
						this.mt = MessageTemplate.and(
								MessageTemplate.MatchConversationId(reply
										.getConversationId()), MessageTemplate
										.MatchInReplyTo(reply.getReplyWith()));
						log.debug("Accepted proposal of agent: "
								+ answer.getSender().getLocalName());
					} else {
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						log.debug("Rejected proposal of agent: "
								+ answer.getSender().getLocalName());
					}
					this.myAgent.send(reply);
				}
				this.state = State.AchievingGoal;
			}
		}

	}

	public class ReceiveUpdatesBehavior extends MsgReceiver {

		private static final long serialVersionUID = 1328980796345824527L;

		private Capability capability;
		private Predicate<?> predicate;
		private boolean done;

		public ReceiveUpdatesBehavior(Agent agent, MessageTemplate mt,
				Capability capability, Predicate<?> predicate) {
			super(agent, mt, INFINITE, new DataStore(),
					ReceiveUpdatesBehavior.class.getSimpleName());
			this.capability = capability;
			this.predicate = predicate;
			this.done = false;
		}

		@Override
		protected void handleMessage(ACLMessage msg) {
			try {
				if (ACLMessage.INFORM == msg.getPerformative()) {
					GoalResponse<?> response = (GoalResponse<?>) myAgent
							.getContentManager().extractContent(msg);
					assert capability.getBeliefBase().getBelief(
							response.getPredicate()) == predicate;
					if (response.getValue() != null) {
						predicate.setValue(response.getValue());
						log.debug("Predicate updated: " + predicate);
						((BDIAgent) myAgent).restart();
					} else {
						// capability.getBeliefBase().removeBelief(predicate.getName());
						log.debug("Predicate removed: " + predicate);
						this.done = true;
					}
				}
			} catch (Exception exc) {
				log.error(exc);
				exc.printStackTrace();
				myAgent.removeBehaviour(this);
			}
		}

		@Override
		public boolean done() {
			return done;
		}

	}

	class RequestDescription {
		RouterAgentCapability capability;
		Role role;
		boolean subscribe;

		public RequestDescription(RouterAgentCapability capability, Role role,
				boolean subscribe) {
			this.capability = capability;
			this.role = role;
			this.subscribe = subscribe;
		}
	}

	private enum State {
		AchievingGoal, Ended, ReceivingResponses, Resquesting, Selecting;
	}

	public static final int ANSWER_TIME_OUT = 5000;
	private static final Log log = LogFactory.getLog(GoalRequestPlan.class);
	public static final int MSG_TIME_OUT = 1000;
	private Map<GoalTemplate, RequestDescription> requestDescriptions;

	public GoalRequestPlan() {
		super(GoalRequestPlanBody.class);
		this.requestDescriptions = new HashMap<>();
	}

	public void addGoalTemplate(GoalTemplate goalTemplate,
			RouterAgentCapability capability, Role role, boolean subscribe) {
		super.addGoalTemplate(goalTemplate);
		this.requestDescriptions.put(goalTemplate, new RequestDescription(
				capability, role, subscribe));
	}

	private RequestDescription getRequestDescription(Goal goal) {
		for (GoalTemplate goalTemplate : requestDescriptions.keySet()) {
			if (goalTemplate.match(goal)) {
				return requestDescriptions.get(goalTemplate);
			}
		}
		return null;
	}

}
