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
import bdi4jade.belief.BeliefSet;
import bdi4jade.belief.Predicate;
import bdi4jade.belief.TransientBeliefSet;
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
import bdi4jade.reasoning.DeliberationFunction;
import bdi4jade.reasoning.OptionGenerationFunction;
import br.ufrgs.inf.bdinetr.agent.belief.AnomalousUsageBelief;
import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.predicate.Anomalous;
import br.ufrgs.inf.bdinetr.domain.predicate.AnomalousUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.Benign;
import br.ufrgs.inf.bdinetr.domain.predicate.IpRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.OverUsageCause;
import br.ufrgs.inf.bdinetr.domain.predicate.Restricted;

/**
 * @author Ingrid Nunes
 */
public class AnomalyDetectionCapability extends RouterAgentCapability implements
		BeliefRevisionStrategy, OptionGenerationFunction, GoalListener {

	public class AnalyseLinkStatistics extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			Set<Ip> outliers = role.detectIntrusion(link);
			BeliefSet<OverUsageCause, Ip> overUsageCause = new TransientBeliefSet<OverUsageCause, Ip>(
					new OverUsageCause(link));
			if (outliers != null && !outliers.isEmpty()) {
				for (Ip outlier : outliers) {
					belief(new Anomalous(outlier), true);
					belief(new Benign(outlier), null);
					overUsageCause.addValue(outlier);
				}
			}
			addBelief(overUsageCause);
			addBelief(new AnomalousUsageBelief(new AnomalousUsage(link)));
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(AnomalousUsage anomalousUsage) {
			this.link = anomalousUsage.getConcept();
		}
	}

	public class LimitIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(
						new IpRateLimited(ip), true));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal())
								.getOutputBelief());
						belief(new Restricted(ip), true);

						Set<Belief<?, ?>> overUsageCauseBeliefs = getBeliefBase()
								.getBeliefsByType(OverUsageCause.class);
						Iterator<Belief<?, ?>> it = overUsageCauseBeliefs
								.iterator();
						while (it.hasNext()) {
							BeliefSet<OverUsageCause, Ip> overUsageCause = (BeliefSet<OverUsageCause, Ip>) it
									.next();
							if (overUsageCause.hasValue(ip)) {
								// ip in OverUsageCause(l) --> remove
								overUsageCause.removeValue(ip);
							}
						}
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
		public void setBeliefName(Restricted restricted) {
			this.ip = restricted.getConcept();
		}
	}

	public class RestoreIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(
						new IpRateLimited(ip), false));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal())
								.getOutputBelief());
						belief(new Restricted(ip), false);
						belief(new Anomalous(ip), null);
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
		public void setBeliefName(Restricted restricted) {
			this.ip = restricted.getConcept();
		}
	}

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan analyseLinkStatistics;
	@bdi4jade.annotation.Plan
	private Plan limitIpRate;
	@bdi4jade.annotation.Plan
	private Plan restoreIpRate;
	@bdi4jade.annotation.TransientBelief
	private final AnomalyDetection role;

	public AnomalyDetectionCapability(AnomalyDetection anomalyDetection,
			GoalRequestPlan beliefGoalRequestPlan) {
		this.role = anomalyDetection;

		setBeliefRevisionStrategy(this);
		setOptionGenerationFunction(this);

		beliefGoalRequestPlan.addGoalTemplate(GoalTemplateFactory
				.hasBeliefOfTypeWithValue(IpRateLimited.class, true), this,
				Role.RATE_LIMITER, false);
		beliefGoalRequestPlan.addGoalTemplate(GoalTemplateFactory
				.hasBeliefOfTypeWithValue(IpRateLimited.class, false), this,
				Role.RATE_LIMITER, false);
		beliefGoalRequestPlan.addGoalTemplate(
				GoalTemplateFactory.hasBeliefOfType(Benign.class), this,
				Role.CLASSIFIER, true);

		this.analyseLinkStatistics = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfType(AnomalousUsage.class),
				AnalyseLinkStatistics.class);
		this.limitIpRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(Restricted.class,
						Boolean.TRUE), LimitIPRatePlan.class);
		this.restoreIpRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(Restricted.class,
						Boolean.FALSE), RestoreIPRatePlan.class) {
			@Override
			public boolean isContextApplicable(Goal goal) {
				BeliefGoal<Restricted> bg = (BeliefGoal<Restricted>) goal;
				Predicate<IpRateLimited> ipRateLimited = (Predicate<IpRateLimited>) getBeliefBase()
						.getBelief(
								new IpRateLimited(bg.getBeliefName()
										.getConcept()));
				return (ipRateLimited != null && ipRateLimited.getValue());
			}
		};
	}

	@Override
	public void generateGoals(GoalUpdateSet goalUpdateSet) {
		// Anomalous(ip) AND not Restricted(ip) --> goal(Restricted(ip)) AND
		// goal(?belief(Anomalous(ip))
		Set<Belief<?, ?>> anomalousIpBeliefs = getBeliefBase()
				.getBeliefsByType(Anomalous.class);
		for (Belief<?, ?> belief : anomalousIpBeliefs) {
			Predicate<Anomalous> anomalous = (Predicate<Anomalous>) belief;
			Ip ip = anomalous.getName().getConcept();
			if (anomalous.getValue()) {
				Predicate<Benign> benign = (Predicate<Benign>) getBeliefBase()
						.getBelief(new Benign(ip));
				if (benign == null) {
					// Anomalous(ip) AND ~Benign(ip) -->
					// ?Benign(ip)
					goal(goalUpdateSet, new Benign(ip), this);
				}

				Predicate<Restricted> restricted = (Predicate<Restricted>) getBeliefBase()
						.getBelief(new Restricted(ip));
				if ((benign == null || !benign.getValue())
						&& (restricted == null || !restricted.getValue())) {
					// Anomalous(ip) AND !(not Benign(l)) AND
					// !(Restricted(l)) --> Restricted(ip)
					goal(goalUpdateSet, new Restricted(ip), true);
				}
			}
		}

		Set<Belief<?, ?>> restrictedBeliefs = getBeliefBase().getBeliefsByType(
				Restricted.class);
		for (Belief<?, ?> belief : restrictedBeliefs) {
			Predicate<Restricted> restricted = (Predicate<Restricted>) belief;
			if (restricted.getValue()) {
				Predicate<Benign> benign = (Predicate<Benign>) getBeliefBase()
						.getBelief(
								new Benign(restricted.getName().getConcept()));
				if (benign != null && benign.getValue()) {
					// Restricted(l) AND Benign(l) --> not
					// Restricted(l)
					goal(goalUpdateSet, restricted.getName(), false);
				}
			}
		}
	}

	@Override
	protected Class<?> getLowPriorityGoal() {
		return Benign.class;
	}

	@Override
	public Role getRole() {
		return Role.ANOMALY_DETECTION;
	}

	@Override
	public void goalPerformed(GoalEvent event) {
		if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
			addBelief(((BeliefGoal<?>) event.getGoal()).getOutputBelief());
		}
	}

	@Override
	public void reviewBeliefs() {
		Set<Belief<?, ?>> overUsageCauseBeliefs = getBeliefBase()
				.getBeliefsByType(OverUsageCause.class);
		Iterator<Belief<?, ?>> it = overUsageCauseBeliefs.iterator();
		while (it.hasNext()) {
			BeliefSet<OverUsageCause, Ip> overUsageCause = (BeliefSet<OverUsageCause, Ip>) it
					.next();
			if (overUsageCause.getValue().isEmpty()) {
				removeBelief(overUsageCause);
			}
			Predicate<AnomalousUsage> anomalousUsage = (Predicate<AnomalousUsage>) getBeliefBase()
					.getBelief(
							new AnomalousUsage(overUsageCause.getName()
									.getConcept()));
			if (anomalousUsage != null
					&& (anomalousUsage.getValue() == null || !anomalousUsage
							.getValue())) {
				removeBelief(anomalousUsage);
			}
		}
	}

}
