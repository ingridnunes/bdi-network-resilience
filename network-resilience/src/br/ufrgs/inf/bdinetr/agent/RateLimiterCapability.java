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

import java.util.HashSet;
import java.util.Iterator;
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
import br.ufrgs.inf.bdinetr.agent.RouterAgent.RootCapability;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.PReSETRouter;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.logic.FlowPreposition.FlowRateLimited;
import br.ufrgs.inf.bdinetr.domain.logic.FlowPreposition.Threat;
import br.ufrgs.inf.bdinetr.domain.logic.FlowPreposition.ThreatResponded;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.Anomalous;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.Benign;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.OverUsageCause;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.RateLimited;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.Restricted;
import br.ufrgs.inf.bdinetr.domain.logic.LinkProposition.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.logic.LinkProposition.FullyOperational;
import br.ufrgs.inf.bdinetr.domain.logic.LinkProposition.OverUsage;
import br.ufrgs.inf.bdinetr.domain.logic.LinkProposition.RegularUsage;

/**
 * @author Ingrid Nunes
 */
public class RateLimiterCapability extends RouterAgentCapability {

	public class LimitFlowRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Flow flow;

		@Override
		public void execute() {
			role.limitFlow(flow, FLOW_LIMIT_RATE);
			belief(new FlowRateLimited(flow), true);
			belief(new ThreatResponded(flow), true);
			belief(new Threat(flow), null);

			boolean exists = false;
			Set<Belief<?, ?>> threatBeliefs = getBeliefBase().getBeliefsByType(
					Threat.class);
			for (Belief<?, ?> belief : threatBeliefs) {
				PropositionalBelief<Threat> threat = (PropositionalBelief<Threat>) belief;
				assert threat.getValue();
				if (flow.getDstIp().equals(
						threat.getName().getFlow().getDstIp())) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				belief(new Benign(flow.getDstIp()), true);
			}

			log.info(getGoal());
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(ThreatResponded threatResponded) {
			this.flow = threatResponded.getFlow();
		}
	}

	public class LimitIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		@bdi4jade.annotation.Belief(name = RootCapability.ROUTER_BELIEF)
		private Belief<String, PReSETRouter> device;
		private Ip ip;

		@Override
		public void execute() {
			role.limitIp(ip, IP_LIMIT_RATE);
			belief(new RateLimited(ip), true);
			belief(new Restricted(ip), true);

			Set<Belief<?, ?>> overUsageCauseBeliefs = getBeliefBase()
					.getBeliefsByType(OverUsageCause.class);
			Set<OverUsageCause> causedByIp = new HashSet<>();
			Iterator<Belief<?, ?>> it = overUsageCauseBeliefs.iterator();
			while (it.hasNext()) {
				PropositionalBelief<OverUsageCause> overUsageCause = (PropositionalBelief<OverUsageCause>) it
						.next();
				if (ip.equals(overUsageCause.getName().getIp())) {
					assert overUsageCause.getValue();
					causedByIp.add(overUsageCause.getName());
				}
				it.remove();
				belief(overUsageCause.getName(), null);
			}

			for (OverUsageCause overUsageCause : causedByIp) {
				boolean exists = false;
				for (Belief<?, ?> belief : overUsageCauseBeliefs) {
					PropositionalBelief<OverUsageCause> otherOverUsageCause = (PropositionalBelief<OverUsageCause>) belief;
					if (overUsageCause.getLink().equals(
							otherOverUsageCause.getName().getLink())) {
						assert !overUsageCause.getIp().equals(
								otherOverUsageCause.getName().getIp());
						exists = true;
						break;
					}
				}
				if (!exists) {
					belief(new RegularUsage(overUsageCause.getLink()), true);
				}
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
			role.limitLink(link, LINK_LIMIT_RATE);
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

		@bdi4jade.annotation.Belief(name = RootCapability.ROUTER_BELIEF)
		private Belief<String, PReSETRouter> device;
		private Ip ip;

		@Override
		public void execute() {
			role.unlimitIp(ip);
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
			role.unlimitLink(link);
			belief(new FullyOperational(link), true);
			belief(new AttackPrevented(link), null);
			log.info(getGoal());
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(FullyOperational fullyOperational) {
			this.link = fullyOperational.getLink();
		}
	}

	public static final double FLOW_LIMIT_RATE = 0.5;
	public static final double IP_LIMIT_RATE = 0.5;
	public static final double LINK_LIMIT_RATE = 0.5;

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan limitFlowRate = new DefaultPlan(
			GoalTemplateFactory.hasValueForBeliefOfType(ThreatResponded.class,
					Boolean.TRUE), LimitFlowRatePlan.class) {
		public boolean isContextApplicable(Goal goal) {
			BeliefGoal<ThreatResponded> bg = (BeliefGoal<ThreatResponded>) goal;
			PropositionalBelief<Threat> threat = (PropositionalBelief<Threat>) getBeliefBase()
					.getBelief(new Threat(bg.getBeliefName().getFlow()));
			return (threat != null && threat.getValue());
		};
	};

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

	@bdi4jade.annotation.TransientBelief
	private final RateLimiter role;

	public RateLimiterCapability(RateLimiter rateLimiter) {
		this.role = rateLimiter;

		ReasoningStrategy strategy = new ReasoningStrategy();
		setOptionGenerationFunction(strategy);
	}

}
