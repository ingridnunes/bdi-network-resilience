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

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Predicate;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.IpRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.LinkRateLimited;

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
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(FlowRateLimited flowRateLimited) {
			this.flow = flowRateLimited.getConcept();
		}
	}

	public class LimitIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;

		@Override
		public void execute() {
			role.limitIp(ip, IP_LIMIT_RATE);
			belief(new IpRateLimited(ip), true);
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(IpRateLimited ipRateLimited) {
			this.ip = ipRateLimited.getConcept();
		}
	}

	public class LimitLinkRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			role.limitLink(link, LINK_LIMIT_RATE);
			belief(new LinkRateLimited(link), true);
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(LinkRateLimited linkRateLimited) {
			this.link = linkRateLimited.getConcept();
		}
	}

	public class RestoreIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;

		@Override
		public void execute() {
			role.unlimitIp(ip);
			belief(new IpRateLimited(ip), false);
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(IpRateLimited ipRateLimited) {
			this.ip = ipRateLimited.getConcept();
		}
	}

	public class RestoreLinkRate extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			role.unlimitLink(link);
			belief(new LinkRateLimited(link), false);
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(LinkRateLimited linkRateLimited) {
			this.link = linkRateLimited.getConcept();
		}
	}

	public static final double FLOW_LIMIT_RATE = 90;
	public static final double IP_LIMIT_RATE = 50;
	public static final double LINK_LIMIT_RATE = 90;
	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan limitFlowRate;
	@bdi4jade.annotation.Plan
	private Plan limitIpRate;
	@bdi4jade.annotation.Plan
	private Plan limitLinkRate;
	@bdi4jade.annotation.Plan
	private Plan restoreIpRate;
	@bdi4jade.annotation.Plan
	private Plan restoreLinkRate;
	@bdi4jade.annotation.TransientBelief
	private final RateLimiter role;

	public RateLimiterCapability(RateLimiter rateLimiter) {
		this.role = rateLimiter;

		this.limitLinkRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(
						LinkRateLimited.class, Boolean.TRUE),
				LimitLinkRatePlan.class);
		this.restoreLinkRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(
						LinkRateLimited.class, Boolean.FALSE),
				RestoreLinkRate.class) {
			public boolean isContextApplicable(Goal goal) {
				BeliefGoal<LinkRateLimited> bg = (BeliefGoal<LinkRateLimited>) goal;
				Predicate<LinkRateLimited> rateLimited = (Predicate<LinkRateLimited>) getBeliefBase()
						.getBelief(bg.getBeliefName());
				return (rateLimited != null && rateLimited.getValue());
			};
		};
		this.limitIpRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(
						IpRateLimited.class, Boolean.TRUE),
				LimitIPRatePlan.class);
		this.restoreIpRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(
						IpRateLimited.class, Boolean.FALSE),
				RestoreIPRatePlan.class) {
			public boolean isContextApplicable(Goal goal) {
				BeliefGoal<IpRateLimited> bg = (BeliefGoal<IpRateLimited>) goal;
				Predicate<IpRateLimited> rateLimited = (Predicate<IpRateLimited>) getBeliefBase()
						.getBelief(bg.getBeliefName());
				return rateLimited != null && rateLimited.getValue();
			};
		};
		this.limitFlowRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(
						FlowRateLimited.class, Boolean.TRUE),
				LimitFlowRatePlan.class);
	}

	@Override
	public Role getRole() {
		return Role.RATE_LIMITER;
	}

}
