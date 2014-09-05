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

import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashSet;
import java.util.Set;

import bdi4jade.belief.Belief;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;

/**
 * @author Ingrid Nunes
 */
public class RequestBeliefGoalPlanBody extends BeliefGoalPlanBody {

	public static final int MSG_TIME_OUT = 10000;
	public static final int ANSWER_TIME_OUT = 30000;

	private enum State {
		Resquesting, ReceivingResponses, Selecting, AchievingGoal, Ended;
	}

	private static final long serialVersionUID = -1833810388789537049L;

	private MessageTemplate mt;
	private State state;
	private long requestTime;
	private Set<ACLMessage> positiveAnswers;

	@Override
	protected void execute() {
		try {
			switch (state) {
			case Resquesting:
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);
				msg.setContentObject(getGoal());

				// FIXME send request to specific role
				TopicManagementHelper topicHelper = (TopicManagementHelper) myAgent
						.getHelper(TopicManagementHelper.SERVICE_NAME);
				for (RoleType role : RoleType.values()) {
					msg.addReceiver(topicHelper.createTopic(role.name()));
				}

				this.requestTime = System.currentTimeMillis();
				msg.setConversationId("cin" + requestTime);
				msg.setReplyWith("cfp" + requestTime);
				myAgent.send(msg);
				this.mt = MessageTemplate.and(MessageTemplate
						.MatchConversationId(msg.getConversationId()),
						MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
				this.positiveAnswers = new HashSet<>();
				break;
			case ReceivingResponses:
				ACLMessage reply = myAgent.blockingReceive(mt, MSG_TIME_OUT);
				if (reply != null) {
					Object content = reply.getContentObject();
					if (content instanceof Boolean) {
						log.info("Agent " + reply.getSender() + "'s reply: "
								+ content);
						if ((Boolean) content) {
							positiveAnswers.add(reply);
						}
					}
				}
				if ((System.currentTimeMillis() - requestTime) > ANSWER_TIME_OUT) {
					this.state = State.Selecting;
				}
				break;
			case Selecting:
				if (positiveAnswers.isEmpty()) {
					log.info("No positive answers.");
					setEndState(EndState.FAILED);
					this.state = State.Ended;
					return;
				} else {
					ACLMessage chosenMsg = positiveAnswers.iterator().next(); // TODO
																				// Better
																				// selection
																				// process.
					for (ACLMessage answer : positiveAnswers) {
						reply = answer.createReply();
						reply.setPerformative((answer == chosenMsg) ? ACLMessage.ACCEPT_PROPOSAL
								: ACLMessage.REJECT_PROPOSAL);
						this.myAgent.send(reply);
					}
					this.state = State.AchievingGoal;
				}
				break;
			case AchievingGoal:
				reply = myAgent.blockingReceive(mt);
				if (reply != null) {
					Object content = reply.getContentObject();
					if (content instanceof Belief) {
						getCapability().getWholeCapability().getBeliefBase()
								.addBelief((Belief<?, ?>) content);
						assert ((BeliefGoal<?>) getGoal())
								.isAchieved(getBeliefBase());
						log.info("Goal " + getGoal() + " achieved.");
					} else {
						setEndState(EndState.FAILED);
					}
					this.state = State.Ended;
				}
				break;
			case Ended:
				break;
			}
		} catch (Exception exc) {
			log.error(exc);
			setEndState(EndState.FAILED);
		}
	}

	@Override
	public void onStart() {
		this.state = State.Resquesting;
	}

}
