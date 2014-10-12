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

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Belief;
import bdi4jade.belief.Predicate;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.event.GoalEvent;
import bdi4jade.event.GoalListener;
import bdi4jade.goal.BeliefGoal;
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
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.predicate.AnomalousUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.predicate.LinkRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.OverUsage;

/**
 * @author Ingrid Nunes
 */
public class LinkMonitorCapability extends RouterAgentCapability implements
		BeliefRevisionStrategy, OptionGenerationFunction, GoalListener,
		Observer {

	public class LimitLinkRate extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(
						new LinkRateLimited(link), true));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal())
								.getOutputBelief());
						belief(new AttackPrevented(link), true);
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
		public void setBeliefName(AttackPrevented attackPrevented) {
			this.link = attackPrevented.getConcept();
		}
	}

	public class RestoreLinkRate extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(
						new LinkRateLimited(link), false));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal())
								.getOutputBelief());
						belief(new AttackPrevented(link), false);
						belief(new AnomalousUsage(link), null);
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
		public void setBeliefName(AttackPrevented attackPrevented) {
			this.link = attackPrevented.getConcept();
		}
	}

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan limitLinkRate;
	@bdi4jade.annotation.Plan
	private Plan restoreLinkRate;
	@bdi4jade.annotation.TransientBelief
	private LinkMonitor role;

	public LinkMonitorCapability(LinkMonitor linkMonitor,
			GoalRequestPlan beliefGoalRequestPlan) {
		this.role = linkMonitor;
		role.addObserver(this);

		setBeliefRevisionStrategy(this);
		setOptionGenerationFunction(this);

		beliefGoalRequestPlan.addGoalTemplate(GoalTemplateFactory
				.hasBeliefOfTypeWithValue(LinkRateLimited.class, true), this,
				Role.RATE_LIMITER, false);
		beliefGoalRequestPlan.addGoalTemplate(GoalTemplateFactory
				.hasBeliefOfTypeWithValue(LinkRateLimited.class, false), this,
				Role.RATE_LIMITER, false);
		beliefGoalRequestPlan.addGoalTemplate(
				GoalTemplateFactory.hasBeliefOfType(AnomalousUsage.class),
				this, Role.ANOMALY_DETECTION, true);

		this.limitLinkRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(
						AttackPrevented.class, Boolean.TRUE),
				LimitLinkRate.class);
		this.restoreLinkRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(
						AttackPrevented.class, Boolean.FALSE),
				RestoreLinkRate.class) {
			@Override
			public boolean isContextApplicable(Goal goal) {
				BeliefGoal<AttackPrevented> bg = (BeliefGoal<AttackPrevented>) goal;
				Predicate<LinkRateLimited> linkRateLimited = (Predicate<LinkRateLimited>) getBeliefBase()
						.getBelief(
								new LinkRateLimited(bg.getBeliefName()
										.getConcept()));
				return (linkRateLimited != null && linkRateLimited.getValue());
			}
		};
	}

	@Override
	public void generateGoals(GoalUpdateSet goalUpdateSet) {
		Set<Belief<?, ?>> overUsageBeliefs = getBeliefBase().getBeliefsByType(
				OverUsage.class);
		for (Belief<?, ?> belief : overUsageBeliefs) {
			Predicate<OverUsage> overUsage = (Predicate<OverUsage>) belief;
			Link link = overUsage.getName().getConcept();
			if (overUsage.getValue()) {
				Predicate<AnomalousUsage> anomalousUsage = (Predicate<AnomalousUsage>) getBeliefBase()
						.getBelief(new AnomalousUsage(link));
				if (anomalousUsage == null) {
					// OverUsage(l) AND ~AnomalousUsage(l) -->
					// ?AnomalousUsage(l)
					goal(goalUpdateSet, new AnomalousUsage(link), this);
				}

				Predicate<AttackPrevented> attackPrevented = (Predicate<AttackPrevented>) getBeliefBase()
						.getBelief(new AttackPrevented(link));
				if ((anomalousUsage == null || anomalousUsage.getValue())
						&& (attackPrevented == null || !attackPrevented
								.getValue())) {
					// OverUsage(l) AND !(not AnomalousUsage(l)) AND
					// !(AttackPrevented(l)) --> AttackPrevented(l)
					goal(goalUpdateSet, new AttackPrevented(link), Boolean.TRUE);
				}
			}
		}

		Set<Belief<?, ?>> attackPreventedBeliefs = getBeliefBase()
				.getBeliefsByType(AttackPrevented.class);
		for (Belief<?, ?> belief : attackPreventedBeliefs) {
			Predicate<AttackPrevented> attackPrevented = (Predicate<AttackPrevented>) belief;
			if (attackPrevented.getValue()) {
				Predicate<AnomalousUsage> anomalousUsage = (Predicate<AnomalousUsage>) getBeliefBase()
						.getBelief(
								new AnomalousUsage(attackPrevented.getName()
										.getConcept()));
				// AttackPrevented(l) AND not AnomalousUsage(l) --> not
				// AttackPrevented(l)
				if (anomalousUsage != null && !anomalousUsage.getValue()) {
					goal(goalUpdateSet, attackPrevented.getName(),
							Boolean.FALSE);
				}
			}
		}
	}

	@Override
	protected Class<?> getLowPriorityGoal() {
		return AnomalousUsage.class;
	}

	@Override
	public Role getRole() {
		return Role.LINK_MONITOR;
	}

	@Override
	public void goalPerformed(GoalEvent event) {
		if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
			addBelief(((BeliefGoal<?>) event.getGoal()).getOutputBelief());
		}
	}

	@Override
	public void reviewBeliefs() {
		for (Link link : role.getLinks()) {
			OverUsage overUsage = new OverUsage(link);
			if (role.isOverUsage(link)) {
				Predicate<OverUsage> overUsageBelief = (Predicate<OverUsage>) getBeliefBase()
						.getBelief(overUsage);
				if (overUsageBelief == null || !overUsageBelief.getValue()) {
					belief(overUsage, true);
					belief(new AnomalousUsage(link), null);
				}
			} else {
				belief(overUsage, null);
				role.removeLink(link);
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		getMyAgent().restart();
	}

}
