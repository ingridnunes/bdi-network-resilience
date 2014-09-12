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

import java.util.Set;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Belief;
import bdi4jade.belief.PropositionalBelief;
import bdi4jade.core.Capability;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.event.GoalEvent;
import bdi4jade.goal.BeliefPresentGoal;
import bdi4jade.goal.GoalStatus;
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import bdi4jade.reasoning.OptionGenerationFunction;
import br.ufrgs.inf.bdinetr.agent.RouterAgent.RootCapability.ExportFlows;
import br.ufrgs.inf.bdinetr.domain.Classifier;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;
import br.ufrgs.inf.bdinetr.domain.logic.FlowPreposition.Threat;
import br.ufrgs.inf.bdinetr.domain.logic.FlowPreposition.ThreatResponded;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.Anomalous;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.Benign;

/**
 * @author Ingrid Nunes
 */
public class ClassifierCapability extends RouterAgentCapability implements
		OptionGenerationFunction {

	public class AnalyseIPFlows extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;
		private boolean flowsExported;

		@Override
		public void execute() {
			if (!flowsExported) {
				dispatchSubgoalAndListen(new ExportFlows(ip));
				this.flowsExported = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						Set<Flow> malicious = role.classifyFlows(ip);

						for (Flow flow : malicious) {
							belief(new Threat(flow), true);
						}

						// Exists flow.(threat(flow) AND ip = dst(flow)) --> not
						// Benign(ip)
						// nExists flow.(threat(flow) AND ip = dst(flow)) -->
						// Benign(ip)
						boolean exists = false;
						Set<Belief<?, ?>> threatBeliefs = getBeliefBase()
								.getBeliefsByType(Threat.class);
						for (Belief<?, ?> belief : threatBeliefs) {
							PropositionalBelief<Threat> threat = (PropositionalBelief<Threat>) belief;
							assert threat.getValue();

							if (ip.equals(threat.getName().getFlow().getDstIp())) {
								exists = true;
								break;
							}
						}
						belief(new Benign(ip), !exists);
					} else {
						setEndState(EndState.FAILED);
					}
				} else {
					block();
				}
			}
		}

		@Override
		public void onStart() {
			super.onStart();
			this.flowsExported = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(Benign benign) {
			this.ip = benign.getIp();
		}
	}

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan analyseIpFlows;
	@bdi4jade.annotation.TransientBelief
	private final Classifier role;

	public ClassifierCapability(Classifier classifier) {
		this.role = classifier;

		setOptionGenerationFunction(this);

		analyseIpFlows = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfType(Benign.class),
				AnalyseIPFlows.class) {
			public boolean isContextApplicable(bdi4jade.goal.Goal goal) {
				BeliefPresentGoal<Benign> bg = (BeliefPresentGoal<Benign>) goal;
				PropositionalBelief<Anomalous> anomalous = (PropositionalBelief<Anomalous>) getBeliefBase()
						.getBelief(new Anomalous(bg.getBeliefName().getIp()));
				return (anomalous != null && anomalous.getValue());
			};
		};
	}

	@Override
	public void generateGoals(GoalUpdateSet goalUpdateSet) {
		// Threat(flow) --> goal(ThreatResponded(flow))
		Set<Belief<?, ?>> threatBeliefs = getBeliefBase().getBeliefsByType(
				Threat.class);
		for (Belief<?, ?> belief : threatBeliefs) {
			PropositionalBelief<Threat> threat = (PropositionalBelief<Threat>) belief;
			if (threat.getValue()) {
				goalUpdateSet.generateGoal(createGoal(new ThreatResponded(
						threat.getName().getFlow()), true));
			}
		}
	}

	@Override
	public RoleType getRole() {
		return RoleType.CLASSIFIER;
	}

	@Override
	public void setCapability(Capability capability) {
		if (!this.equals(capability)) {
			throw new IllegalArgumentException(
					"This reasoning strategy is already associated with another capability.");
		}
	}

}
