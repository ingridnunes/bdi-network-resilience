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
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import bdi4jade.reasoning.AbstractReasoningStrategy;
import bdi4jade.reasoning.OptionGenerationFunction;
import br.ufrgs.inf.bdinetr.BDINetRAgent.RootCapability;
import br.ufrgs.inf.bdinetr.domain.Device;
import br.ufrgs.inf.bdinetr.domain.IpAddress;
import br.ufrgs.inf.bdinetr.domain.IpPreposition.Anomalous;
import br.ufrgs.inf.bdinetr.domain.IpPreposition.Benign;
import br.ufrgs.inf.bdinetr.domain.IpPreposition.RateLimited;
import br.ufrgs.inf.bdinetr.domain.IpPreposition.Restricted;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.FullyOperational;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.OverUsage;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.RegularUsage;

/**
 * @author Ingrid Nunes
 */
public class RateLimiterCapability extends BDINetRAppCapability {

	public class LimitIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		@bdi4jade.annotation.Belief(name = RootCapability.DEVICE_BELIEF)
		private Belief<String, Device> device;
		private IpAddress ip;

		@Override
		public void execute() {
			device.getValue().limitIp(ip, IP_LIMIT_RATE);
			belief(new RateLimited(ip), true);
			belief(new Restricted(ip), true);

			boolean exists = false;
			Set<Belief<?, ?>> anomalousBeliefs = getBeliefBase()
					.getBeliefsByType(Anomalous.class);
			for (Belief<?, ?> belief : anomalousBeliefs) {
				PropositionalBelief<Anomalous> anomalous = (PropositionalBelief<Anomalous>) belief;
				if (anomalous.getValue()) {
					PropositionalBelief<Restricted> restricted = (PropositionalBelief<Restricted>) getBeliefBase()
							.getBelief(
									new Restricted(anomalous.getName().getIp()));
					if (restricted == null || !restricted.getValue()) {
						exists = true;
					}

				}
			}
			if (!exists) {
				// FIXME
				belief(new RegularUsage(new Link("")), true);
			}

			log.info(getGoal());
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(Restricted restricted) {
			this.ip = restricted.getIp();
		}
	}

	public class LimitLinkRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			link.setLimitedBandwidth(LINK_LIMIT_RATE * link.getBandwidth());
			belief(new FullyOperational(link), false);
			belief(new AttackPrevented(link), true);
			log.info(getGoal());
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(AttackPrevented attackPrevented) {
			this.link = attackPrevented.getLink();
		}
	}
	
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
						goal(new FullyOperational(fullyOperational.getName()
								.getLink()), true);
					}

				}
			}
		}
	}

	public class RestoreIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		@bdi4jade.annotation.Belief(name = RootCapability.DEVICE_BELIEF)
		private Belief<String, Device> device;
		private IpAddress ip;

		@Override
		public void execute() {
			device.getValue().unlimitIp(ip);
			belief(new RateLimited(ip), false);
			belief(new Restricted(ip), false);
			belief(new Anomalous(ip), null);
			log.info(getGoal());
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(Restricted restricted) {
			this.ip = restricted.getIp();
		}
	}

	public class RestoreLinkRate extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			link.setLimitedBandwidth(null);
			belief(new FullyOperational(link), true);
			belief(new AttackPrevented(link), null);
			log.info(getGoal());
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(FullyOperational fullyOperational) {
			this.link = fullyOperational.getLink();
		}
	}

	public static final double IP_LIMIT_RATE = 0.5;
	public static final double LINK_LIMIT_RATE = 0.5;

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan limitIpRate = new DefaultPlan(
			GoalTemplateFactory.hasValueForBeliefOfType(Restricted.class,
					Boolean.TRUE), LimitIPRatePlan.class) {
		public boolean isContextApplicable(Goal goal) {
			BeliefGoal<Restricted> bg = (BeliefGoal<Restricted>) goal;
			PropositionalBelief<Anomalous> anomalous = (PropositionalBelief<Anomalous>) getBeliefBase()
					.getBelief(new Anomalous(bg.getBeliefName().getIp()));
			return (anomalous != null && anomalous.getValue());
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

	@bdi4jade.annotation.Plan
	private Plan restoreIpRate = new DefaultPlan(
			GoalTemplateFactory.hasValueForBeliefOfType(Restricted.class,
					Boolean.FALSE), RestoreIPRatePlan.class) {
		public boolean isContextApplicable(Goal goal) {
			BeliefGoal<Restricted> bg = (BeliefGoal<Restricted>) goal;
			PropositionalBelief<Benign> benign = (PropositionalBelief<Benign>) getBeliefBase()
					.getBelief(new Benign(bg.getBeliefName().getIp()));
			PropositionalBelief<RateLimited> rateLimited = (PropositionalBelief<RateLimited>) getBeliefBase()
					.getBelief(new RateLimited(bg.getBeliefName().getIp()));
			return (benign != null && benign.getValue())
					&& (rateLimited != null && rateLimited.getValue());
		};
	};

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

	public RateLimiterCapability() {
		ReasoningStrategy strategy = new ReasoningStrategy();
		setOptionGenerationFunction(strategy);
	}

}
