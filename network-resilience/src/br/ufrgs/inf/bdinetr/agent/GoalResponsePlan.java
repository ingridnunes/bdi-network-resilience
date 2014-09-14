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

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;

import java.util.Date;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Predicate;
import bdi4jade.core.Capability;
import bdi4jade.event.BeliefEvent;
import bdi4jade.event.BeliefEvent.Action;
import bdi4jade.event.BeliefListener;
import bdi4jade.event.GoalEvent;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.BeliefPresentGoal;
import bdi4jade.goal.BeliefValueGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalStatus;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import br.ufrgs.inf.bdinetr.domain.message.GoalProposal;
import br.ufrgs.inf.bdinetr.domain.message.GoalRequest;
import br.ufrgs.inf.bdinetr.domain.message.GoalResponse;

/**
 * @author Ingrid Nunes
 */
public class GoalResponsePlan extends DefaultPlan {

	public class GoalResponsePlanBody extends AbstractPlanBody implements
			BeliefListener {

		private static final long serialVersionUID = -4231465068344668721L;

		private ACLMessage acceptProposalMsg;
		private ACLMessage beliefGoalMsg;
		private Goal goal;
		private Boolean lastValue;
		private MessageTemplate mt;
		private Capability partCapability;
		private Predicate<?> predicate;
		private GoalRequest<?> request;
		private State state;
		private long responseTime;

		private void achieveBeliefGoal() throws Exception {
			GoalEvent event = getGoalEvent();
			if (event == null)
				return;

			log.debug(goal + " finished: " + event.getStatus());
			ACLMessage reply = acceptProposalMsg.createReply();
			if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
				reply.setPerformative(ACLMessage.INFORM);
				if (goal instanceof BeliefGoal) {
					predicate = (Predicate<?>) partCapability.getBeliefBase()
							.getBelief(((BeliefGoal<?>) goal).getBeliefName());
					GoalResponse response = new GoalResponse();
					response.setPredicate(predicate.getName());
					response.setValue(predicate.getValue());
					response.setTimestamp(new Date());
					myAgent.getContentManager().fillContent(reply, response);
				}

				if (request.getSubscribe()) {
					partCapability.getBeliefBase().addBeliefListener(this);
					this.lastValue = predicate.getValue();
					this.state = State.SendingUpdates;
				} else {
					setEndState(EndState.SUCCESSFUL);
					this.state = State.Ended;
				}
			} else {
				reply.setPerformative(ACLMessage.FAILURE);
				setEndState(EndState.SUCCESSFUL);
				this.state = State.Ended;
			}
			this.myAgent.send(reply);
		}

		@Override
		public void action() {
			try {
				switch (state) {
				case SendingResponse:
					sendResponse();
					break;
				case ReceivingReply:
					receiveReply();
					break;
				case AchievingBeliefGoal:
					achieveBeliefGoal();
					break;
				case SendingUpdates:
					block();
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

		@Override
		public void eventOccurred(BeliefEvent beliefEvent) {
			if (predicate == beliefEvent.getBelief()) {
				if (beliefEvent.getAction().equals(Action.BELIEF_REMOVED)) {
					try {
						ACLMessage reply = acceptProposalMsg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						GoalResponse response = new GoalResponse();
						response.setPredicate(predicate.getName());
						response.setValue(null);
						response.setTimestamp(new Date());
						myAgent.getContentManager()
								.fillContent(reply, response);
						this.myAgent.send(reply);
						log.debug("Sending update (belief removed): " + goal);
					} catch (Exception exc) {
						log.error(exc);
						exc.printStackTrace();
					}

					partCapability.getBeliefBase().removeBeliefListener(this);
					this.restart();
					this.state = State.Ended;
					setEndState(EndState.SUCCESSFUL);
				}
			} else {
				Boolean currentValue = predicate.getValue();
				if (!currentValue.equals(lastValue)) {
					try {
						ACLMessage reply = acceptProposalMsg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						GoalResponse response = new GoalResponse();
						response.setPredicate(predicate.getName());
						response.setValue(currentValue);
						response.setTimestamp(new Date());
						myAgent.getContentManager()
								.fillContent(reply, response);
						this.myAgent.send(reply);
						this.lastValue = currentValue;
						log.debug("Goal: " + goal);
					} catch (Exception exc) {
						log.error(exc);
						exc.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onStart() {
			this.state = State.SendingResponse;
		}

		private void receiveReply() {
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				log.debug("Goal: " + goal);
				if (ACLMessage.ACCEPT_PROPOSAL == reply.getPerformative()) {
					dispatchSubgoalAndListen(goal);
					this.acceptProposalMsg = reply;
					log.debug("Proposal accepted");
					this.state = State.AchievingBeliefGoal;
				} else {
					setEndState(EndState.SUCCESSFUL);
					log.debug("Proposal rejected");
					this.state = State.Ended;
					return;
				}
			} else {
				long timeElapsed = System.currentTimeMillis() - responseTime;
				if (timeElapsed >= ANSWER_TIME_OUT) {
					setEndState(EndState.SUCCESSFUL);
					log.debug("No answer received... ending plan.");
				} else {
					block(ANSWER_TIME_OUT);
				}
			}
		}

		private void sendResponse() throws Exception {
			log.debug(beliefGoalMsg.getContent());
			this.responseTime = System.currentTimeMillis();

			this.request = (GoalRequest<?>) myAgent.getContentManager()
					.extractContent(beliefGoalMsg);
			if (request.getBeliefGoal()) {
				if (request.getValue() == null) {
					this.goal = new BeliefPresentGoal(request.getPredicate());
				} else {
					this.goal = new BeliefValueGoal(request.getPredicate(),
							request.getValue());
				}
			} else {
				this.goal = (Goal) request.getPredicate();
			}

			Boolean canAchieve = false;
			for (Capability part : getCapability().getPartCapabilities()) {
				if (part.canAchieve(goal)) {
					partCapability = part;
					canAchieve = true;
					break;
				}
			}

			ACLMessage reply = beliefGoalMsg.createReply();
			reply.setPerformative(canAchieve ? ACLMessage.PROPOSE
					: ACLMessage.REFUSE);
			reply.setReplyWith("per" + responseTime);

			// TODO set proposal cost
			Random r = new Random(System.currentTimeMillis());
			GoalProposal proposal = new GoalProposal(r.nextDouble());
			myAgent.getContentManager().fillContent(reply, proposal);

			myAgent.send(reply);

			log.debug("Agent " + myAgent.getLocalName() + " can achieve "
					+ goal + ": " + canAchieve);

			this.mt = MessageTemplate.and(MessageTemplate
					.MatchConversationId(reply.getConversationId()),
					MessageTemplate.MatchInReplyTo(reply.getReplyWith()));
			this.state = State.ReceivingReply;
		}

		@Parameter(direction = Direction.IN)
		public void setMessage(ACLMessage beliefGoalMsg) {
			this.beliefGoalMsg = beliefGoalMsg;
		}

	}

	private enum State {
		AchievingBeliefGoal, Ended, ReceivingReply, SendingResponse, SendingUpdates;
	}

	private static final Log log = LogFactory.getLog(GoalResponsePlan.class);
	public static final int ANSWER_TIME_OUT = 60000;

	public GoalResponsePlan() {
		super(new MessageTemplate(new MatchExpression() {
			private static final long serialVersionUID = -3581014512390059387L;

			@Override
			public boolean match(ACLMessage msg) {
				try {
					return (ACLMessage.CFP == msg.getPerformative() && msg
							.getContent().indexOf(
									GoalRequest.class.getSimpleName()) > 0);
				} catch (Exception exc) {
					log.error(exc);
					return false;
				}
			}
		}), GoalResponsePlanBody.class);
	}

}
