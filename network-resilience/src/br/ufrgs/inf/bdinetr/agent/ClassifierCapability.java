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

import java.util.Iterator;
import java.util.Set;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Belief;
import bdi4jade.belief.Predicate;
import bdi4jade.core.Capability;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.event.GoalEvent;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.BeliefNotPresentGoal;
import bdi4jade.goal.BeliefValueGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalStatus;
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import bdi4jade.reasoning.BeliefRevisionStrategy;
import bdi4jade.reasoning.OptionGenerationFunction;
import br.ufrgs.inf.bdinetr.agent.belief.BenignBelief;
import br.ufrgs.inf.bdinetr.domain.Classifier;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.predicate.Benign;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowExport;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.Threat;

/**
 * @author Ingrid Nunes
 */
public class ClassifierCapability extends RouterAgentCapability implements
		BeliefRevisionStrategy, OptionGenerationFunction {

	public class AnalyseIPFlows extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new FlowExport(ip));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						Set<Flow> malicious = role.classifyFlows(ip);
						for (Flow flow : malicious) {
							belief(new Threat(flow), true);
						}
						addBelief(new BenignBelief(new Benign(ip)));
					} else {
						setEndState(EndState.FAILED);
					}
				} else {
					block();
				}
			}
		}

		@Override
		public void init() {
			this.subgoalDispatched = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(Benign benign) {
			this.ip = benign.getConcept();
		}
	}

	public class LimitFlowRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Flow flow;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(
						new FlowRateLimited(flow), true));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal())
								.getOutputBelief());
						belief(new Threat(flow), null);
					} else {
						setEndState(EndState.FAILED);
					}
				}
			}
		}

		@Override
		protected void init() {
			this.subgoalDispatched = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(Threat threat) {
			this.flow = threat.getConcept();
		}
	}

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan analyseIpFlows;
	@bdi4jade.annotation.Plan
	private Plan limitFlowRate;
	@bdi4jade.annotation.TransientBelief
	private final Classifier role;

	public ClassifierCapability(Classifier classifier,
			GoalRequestPlan beliefGoalRequestPlan) {
		this.role = classifier;

		setBeliefRevisionStrategy(this);
		setOptionGenerationFunction(this);

		beliefGoalRequestPlan.addGoalTemplate(GoalTemplateFactory
				.hasBeliefOfTypeWithValue(FlowRateLimited.class, true), this,
				Role.RATE_LIMITER, false);
		beliefGoalRequestPlan.addGoalTemplate(
				GoalTemplateFactory.goalOfType(FlowExport.class), this,
				Role.FLOW_EXPORTER, false);

		analyseIpFlows = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfType(Benign.class),
				AnalyseIPFlows.class);
		limitFlowRate = new DefaultPlan(
				GoalTemplateFactory.hasNoBeliefOfType(Threat.class),
				LimitFlowRatePlan.class) {
			public boolean isContextApplicable(Goal goal) {
				BeliefNotPresentGoal<Threat> bg = (BeliefNotPresentGoal<Threat>) goal;
				Predicate<Threat> threat = (Predicate<Threat>) getBeliefBase()
						.getBelief(bg.getBeliefName());
				return (threat != null && threat.getValue());
			};
		};
	}

	@Override
	public void generateGoals(GoalUpdateSet goalUpdateSet) {
		// Threat(flow) --> ~Threat(flow)
		Set<Belief<?, ?>> threatBeliefs = getBeliefBase().getBeliefsByType(
				Threat.class);
		for (Belief<?, ?> belief : threatBeliefs) {
			Predicate<Threat> threat = (Predicate<Threat>) belief;
			if (threat.getValue()) {
				goal(goalUpdateSet, threat.getName(), (Boolean) null);
			}
		}
	}

	@Override
	public Role getRole() {
		return Role.CLASSIFIER;
	}

	@Override
	public void reviewBeliefs() {
		Set<Belief<?, ?>> benignBeliefs = getBeliefBase().getBeliefsByType(
				Benign.class);
		Iterator<Belief<?, ?>> it = benignBeliefs.iterator();
		while (it.hasNext()) {
			Predicate<Benign> benignBelief = (Predicate<Benign>) it.next();
			if (benignBelief.getValue() == null || benignBelief.getValue()) {
				removeBelief(benignBelief);
			}
		}
	}

	@Override
	public void setCapability(Capability capability) {
		if (!this.equals(capability)) {
			throw new IllegalArgumentException(
					"This reasoning strategy is already associated with another capability.");
		}
	}

}
