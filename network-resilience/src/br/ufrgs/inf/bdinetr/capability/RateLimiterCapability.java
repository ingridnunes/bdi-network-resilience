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
package br.ufrgs.inf.bdinetr.capability;

import java.util.Set;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Belief;
import bdi4jade.belief.PropositionalBelief;
import bdi4jade.belief.TransientPropositionalBelief;
import bdi4jade.core.Capability;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.goal.PropositionalBeliefValueGoal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import bdi4jade.reasoning.AbstractReasoningStrategy;
import bdi4jade.reasoning.OptionGenerationFunction;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.FullyOperational;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.OverUsage;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.RegularUsage;

/**
 * @author Ingrid Nunes
 */
public class RateLimiterCapability extends Capability {

	private class ReasoningStrategy extends AbstractReasoningStrategy implements
			OptionGenerationFunction {
		@Override
		public void generateGoals(GoalUpdateSet goalUpdateSet) {
			Set<Belief<?, ?>> fullyOperationalBeliefs = getBeliefBase()
					.getBeliefsByType(FullyOperational.class);
			for (Belief<?, ?> belief : fullyOperationalBeliefs) {
				PropositionalBelief<FullyOperational> fullyOperational = (PropositionalBelief<FullyOperational>) belief;
				if (!fullyOperational.getValue()) {
					PropositionalBelief<RegularUsage> regularUsage = (PropositionalBelief<RegularUsage>) getBeliefBase()
							.getBelief(
									new RegularUsage(fullyOperational.getName()
											.getLink()));
					if (regularUsage != null && regularUsage.getValue()) {
						getMyAgent()
								.addGoal(
										RateLimiterCapability.this,
										new PropositionalBeliefValueGoal<FullyOperational>(
												new FullyOperational(
														fullyOperational
																.getName()
																.getLink()),
												Boolean.TRUE));
						log.debug("goal(fullyOperational("
								+ fullyOperational.getName().getLink() + "))");
					}

				}
			}
		}
	}

	public static final double LINK_LIMIT_RATE = 0.5;

	private static final long serialVersionUID = -1705728861020677126L;

	public static class LimitLinkRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			link.setLimitedBandwidth(LINK_LIMIT_RATE * link.getBandwidth());
			getBeliefBase().addOrUpdateBelief(
					new TransientPropositionalBelief<FullyOperational>(
							new FullyOperational(link), Boolean.FALSE));
			getCapability()
					.getWholeCapability()
					.getBeliefBase()
					.addOrUpdateBelief(
							new TransientPropositionalBelief<AttackPrevented>(
									new AttackPrevented(link), Boolean.TRUE));
			log.info(getGoal());
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(AttackPrevented attackPrevented) {
			this.link = attackPrevented.getLink();
		}
	}

	public static class RestoreLinkRate extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			link.setLimitedBandwidth(null);
			getBeliefBase().addOrUpdateBelief(
					new TransientPropositionalBelief<FullyOperational>(
							new FullyOperational(link), Boolean.TRUE));
			getCapability()
					.getWholeCapability()
					.getBeliefBase()
					.addOrUpdateBelief(
							new TransientPropositionalBelief<AttackPrevented>(
									new AttackPrevented(link), null));
			log.info(getGoal());
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(FullyOperational fullyOperational) {
			this.link = fullyOperational.getLink();
		}
	}

	@bdi4jade.annotation.Plan
	private Plan restoreLinkRate = new DefaultPlan(
			GoalTemplateFactory.hasValueForBeliefOfType(FullyOperational.class,
					Boolean.TRUE), RestoreLinkRate.class) {
		public boolean isContextApplicable(Goal goal) {
			BeliefGoal<FullyOperational> bg = (BeliefGoal<FullyOperational>) goal;
			PropositionalBelief<RegularUsage> regularUsage = (PropositionalBelief<RegularUsage>) getBeliefBase()
					.getBelief(new RegularUsage(bg.getBeliefName().getLink()));
			return (regularUsage != null && regularUsage.getValue());
		};
	};

	@bdi4jade.annotation.Plan
	private Plan limitLinkRate = new DefaultPlan(
			GoalTemplateFactory.hasValueForBeliefOfType(AttackPrevented.class,
					Boolean.TRUE), LimitLinkRatePlan.class) {
		public boolean isContextApplicable(Goal goal) {
			BeliefGoal<AttackPrevented> bg = (BeliefGoal<AttackPrevented>) goal;
			PropositionalBelief<OverUsage> overUsage = (PropositionalBelief<OverUsage>) getBeliefBase()
					.getBelief(new OverUsage(bg.getBeliefName().getLink()));
			return (overUsage != null && overUsage.getValue());
		};
	};

	public RateLimiterCapability() {
		ReasoningStrategy strategy = new ReasoningStrategy();
		setOptionGenerationFunction(strategy);
	}

}
