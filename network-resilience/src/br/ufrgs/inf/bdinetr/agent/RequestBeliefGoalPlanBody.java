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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;

/**
 * @author Ingrid Nunes
 */
public class RequestBeliefGoalPlanBody extends BeliefGoalPlanBody {

	private enum State {
		Resquesting, ReceivingResponses, Selecting, AchievingGoal, Ended;
	}

	private static final long serialVersionUID = -1833810388789537049L;

	private MessageTemplate mt;
	private State state;

	@Override
	protected void execute() {
		switch (state) {
		case Resquesting:

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent("");
			msg.addReceiver(new AID("", false));
			msg.setConversationId("cin" + System.currentTimeMillis());
			msg.setReplyWith("inform" + System.currentTimeMillis());
			myAgent.send(msg);
			this.mt = MessageTemplate.and(MessageTemplate
					.MatchConversationId(msg.getConversationId()),
					MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
			break;
		case ReceivingResponses:
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				log.info("Pong received from " + reply.getSender().getName()
						+ "!");
				log.info("Content: " + reply.getContent());
				
			} else {
				block();
			}
			break;
		case Selecting:

			break;
		case AchievingGoal:

			break;
		case Ended:

			break;
		}
	}

	@Override
	public void onStart() {
		this.state = State.Resquesting;
	}

}
