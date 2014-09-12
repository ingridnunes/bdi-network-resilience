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
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.core.BDIAgent;
import bdi4jade.core.Capability;
import bdi4jade.core.SingleCapabilityAgent;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.Goal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.reasoning.AgentPlanSelectionStrategy;
import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Classifier;
import br.ufrgs.inf.bdinetr.domain.FlowExporter;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;
import br.ufrgs.inf.bdinetr.domain.PReSETRouter;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;

/**
 * @author Ingrid Nunes
 */
public class RouterAgent extends SingleCapabilityAgent implements
		AgentPlanSelectionStrategy {

	public static class RootCapability extends Capability {

		public static class ExportFlows implements Goal {

			private static final long serialVersionUID = -7114413010093171144L;

			private Ip ip;

			public ExportFlows(Ip ip) {
				this.ip = ip;
			}

			@Parameter(direction = Direction.IN)
			public Ip getIp() {
				return ip;
			}

		}

		public static final String ROUTER_BELIEF = "router";

		private static final long serialVersionUID = -2156730094556459899L;

		@bdi4jade.annotation.Plan
		private final Plan requestBeliefGoalPlan;
		@bdi4jade.annotation.Plan
		private final Plan respondBeliefGoalPlan;
		@bdi4jade.annotation.Belief
		private final Belief<String, PReSETRouter> router;

		public RootCapability(PReSETRouter router) {
			this.router = new TransientBelief<>(ROUTER_BELIEF, router);
			this.requestBeliefGoalPlan = new DefaultPlan(BeliefGoal.class,
					RequestBeliefGoalPlanBody.class);
			this.respondBeliefGoalPlan = new DefaultPlan(new MessageTemplate(
					new MatchExpression() {
						private static final long serialVersionUID = -3581014512390059387L;

						@Override
						public boolean match(ACLMessage msg) {
							try {
								return (msg.getContentObject() != null && msg
										.getContentObject() instanceof BeliefGoal<?>);
							} catch (Exception exc) {
								log.error(exc);
								return false;
							}
						}
					}), RespondBeliefGoalPlanBody.class);
		}

	}

	private static final long serialVersionUID = 6534875498063013722L;

	public RouterAgent(PReSETRouter router) {
		super(new RootCapability(router));
		if (router.hasRole(RoleType.LINK_MONITOR)) {
			this.getCapability().addPartCapability(
					new LinkMonitorCapability((LinkMonitor) router
							.getRole(RoleType.LINK_MONITOR)));
		}
		if (router.hasRole(RoleType.ANOMALY_DETECTION)) {
			this.getCapability().addPartCapability(
					new AnomalyDetectionCapability((AnomalyDetection) router
							.getRole(RoleType.ANOMALY_DETECTION)));
		}
		if (router.hasRole(RoleType.RATE_LIMITER)) {
			this.getCapability().addPartCapability(
					new RateLimiterCapability((RateLimiter) router
							.getRole(RoleType.RATE_LIMITER)));
		}
		if (router.hasRole(RoleType.CLASSIFIER)) {
			this.getCapability().addPartCapability(
					new ClassifierCapability((Classifier) router
							.getRole(RoleType.CLASSIFIER)));
		}
		if (router.hasRole(RoleType.FLOW_EXPORTER)) {
			this.getCapability().addPartCapability(
					new FlowExporterCapability((FlowExporter) router
							.getRole(RoleType.FLOW_EXPORTER)));
		}
		setPlanSelectionStrategy(this);
	}

	@Override
	protected void init() {
		for (Capability capability : getAllCapabilities()) {
			if (capability instanceof RouterAgentCapability) {
				try {
					TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
					AID roleTopic = topicHelper
							.createTopic(((RouterAgentCapability) capability)
									.getRole().name());
					topicHelper.register(roleTopic);
				} catch (Exception exc) {
					log.error(exc);
					exc.printStackTrace();
				}
			}
		}
	}

	@Override
	public Plan selectPlan(Goal goal, Map<Capability, Set<Plan>> capabilityPlans) {
		Set<Plan> preselectedPlans = new HashSet<>();
		for (Capability capability : capabilityPlans.keySet()) {
			if (!getCapability().equals(capability)) {
				Plan preselectedPlan = capability.getPlanSelectionStrategy()
						.selectPlan(goal, capabilityPlans.get(capability));
				if (preselectedPlan != null) {
					preselectedPlans.add(preselectedPlan);
				}
			}
		}

		if (preselectedPlans.isEmpty()) {
			Set<Plan> candidatePlans = capabilityPlans.get(getCapability());
			return (candidatePlans == null) ? null : getCapability()
					.getPlanSelectionStrategy()
					.selectPlan(goal, candidatePlans);
		} else {
			return preselectedPlans.iterator().next();
		}
	}

	@Override
	public void setAgent(BDIAgent agent) {
		if (!this.equals(agent)) {
			throw new IllegalArgumentException(
					"This reasoning strategy is already associated with another agent.");
		}
	}

}
