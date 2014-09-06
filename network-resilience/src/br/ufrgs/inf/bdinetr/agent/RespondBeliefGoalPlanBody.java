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
import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Belief;
import bdi4jade.event.GoalEvent;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.GoalStatus;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;

/**
 * @author Ingrid Nunes
 */
public class RespondBeliefGoalPlanBody extends AbstractPlanBody {

	private enum State {
		SendingResponse, ReceivingReply, AchievingBeliefGoal, Ended;
	}

	private static final long serialVersionUID = -4231465068344668721L;

	private ACLMessage beliefGoalMsg;
	private MessageTemplate mt;
	private State state;
	private ACLMessage incomingMsg;
	private ACLMessage outcomingMsg;
	private BeliefGoal<?> beliefGoal;

	@Override
	public void action() {
		try {
			switch (state) {
			case SendingResponse:
				outcomingMsg = beliefGoalMsg.createReply();
				Object content = beliefGoalMsg.getContentObject();
				if (content instanceof BeliefGoal) {
					beliefGoal = (BeliefGoal<?>) content;
					Boolean canAchieve = getCapability().canAchieve(beliefGoal);
					outcomingMsg.setContentObject(canAchieve);
					log.info("Agent " + myAgent + " can achieve " + content
							+ ": " + canAchieve);
				} else {
					outcomingMsg.setContentObject(Boolean.FALSE);
				}
				outcomingMsg.setReplyWith("cfp" + System.currentTimeMillis());
				this.myAgent.send(outcomingMsg);
				this.mt = MessageTemplate.and(MessageTemplate
						.MatchConversationId(outcomingMsg.getConversationId()),
						MessageTemplate.MatchInReplyTo(outcomingMsg
								.getReplyWith()));
				this.state = State.ReceivingReply;
				break;
			case ReceivingReply:
				incomingMsg = myAgent.receive(mt);
				if (incomingMsg != null) {
					if (ACLMessage.ACCEPT_PROPOSAL == incomingMsg
							.getPerformative()) {
						dispatchSubgoalAndListen(beliefGoal);
						this.state = State.AchievingBeliefGoal;
					} else {
						setEndState(EndState.SUCCESSFULL);
						log.info("Proposal rejected.");
						this.state = State.Ended;
						return;
					}
				} else {
					block();
				}
				break;
			case AchievingBeliefGoal:
				GoalEvent event = getGoalEvent();
				if (event != null) {
					outcomingMsg = incomingMsg.createReply();
					outcomingMsg.setPerformative(ACLMessage.INFORM);
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						Belief<?, ?> belief = (getBeliefBase()
								.getBelief(beliefGoal.getBeliefName()));
						outcomingMsg.setContentObject((Belief<?, ?>) belief
								.clone());
					} else {
						outcomingMsg.setContentObject(null);
					}
					this.myAgent.send(outcomingMsg);
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
		this.state = State.SendingResponse;
	}

	@Parameter(direction = Direction.IN)
	public void setMessage(ACLMessage beliefGoalMsg) {
		this.beliefGoalMsg = beliefGoalMsg;
	}

}
