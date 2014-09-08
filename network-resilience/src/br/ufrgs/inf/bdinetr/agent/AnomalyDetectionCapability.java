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
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import bdi4jade.reasoning.OptionGenerationFunction;
import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.Anomalous;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.Benign;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.OverUsageCause;
import br.ufrgs.inf.bdinetr.domain.logic.IpPreposition.Restricted;
import br.ufrgs.inf.bdinetr.domain.logic.LinkProposition.RegularUsage;

/**
 * @author Ingrid Nunes
 */
public class AnomalyDetectionCapability extends RouterAgentCapability implements
		OptionGenerationFunction {

	public class AnalyseLinkStatistics extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			Set<Ip> outliers = role.detectIntrusion(link);
			for (Ip outlier : outliers) {
				belief(new Anomalous(outlier), true);
				belief(new Benign(outlier), null);
				belief(new OverUsageCause(outlier, link), true);
			}

			// Exists ip.(OverUsageCause(ip, link) AND not(Restricted(ip)) --> not RegularUsage(link)
			// nExists ip.(OverUsageCause(ip, link) AND not(Restricted(ip)) --> RegularUsage(link)
			boolean exists = false;
			Set<Belief<?, ?>> overUsageCauseBeliefs = getBeliefBase()
					.getBeliefsByType(OverUsageCause.class);
			for (Belief<?, ?> belief : overUsageCauseBeliefs) {
				PropositionalBelief<OverUsageCause> overUsageCause = (PropositionalBelief<OverUsageCause>) belief;
				assert overUsageCause.getValue();

				if (link.equals(overUsageCause.getName().getLink())) {
					PropositionalBelief<Restricted> restricted = (PropositionalBelief<Restricted>) getBeliefBase()
							.getBelief(
									new Restricted(overUsageCause.getName()
											.getIp()));
					if (restricted == null || !restricted.getValue()) {
						exists = true;
						break;
					}
				}
			}
			belief(new RegularUsage(link), !exists);
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(RegularUsage regularUsage) {
			this.link = regularUsage.getLink();
		}
	}

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan analyseLinkStatistics;
	@bdi4jade.annotation.TransientBelief
	private final AnomalyDetection role;

	public AnomalyDetectionCapability(AnomalyDetection anomalyDetection) {
		this.role = anomalyDetection;

		setOptionGenerationFunction(this);

		analyseLinkStatistics = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfType(RegularUsage.class),
				AnalyseLinkStatistics.class);
	}

	@Override
	public void generateGoals(GoalUpdateSet goalUpdateSet) {
		// Anomalous(ip) AND not Restricted(ip) --> goal(Restricted(ip)) AND goal(belief(Anomalous(ip))
		Set<Belief<?, ?>> anomalousIpBeliefs = getBeliefBase()
				.getBeliefsByType(Anomalous.class);
		for (Belief<?, ?> belief : anomalousIpBeliefs) {
			PropositionalBelief<Anomalous> anomalous = (PropositionalBelief<Anomalous>) belief;
			if (anomalous.getValue()) {
				PropositionalBelief<Restricted> restricted = (PropositionalBelief<Restricted>) getBeliefBase()
						.getBelief(new Restricted(anomalous.getName().getIp()));
				if (restricted == null || !restricted.getValue()) {
					goalUpdateSet.generateGoal(createGoal(new Restricted(
							anomalous.getName().getIp()), true));
					goalUpdateSet.generateGoal(createGoal(new Benign(anomalous
							.getName().getIp())));
				}
			}
		}
	}

	@Override
	public RoleType getRole() {
		return RoleType.ANOMALY_DETECTION;
	}

	@Override
	public void setCapability(Capability capability) {
		if (!this.equals(capability)) {
			throw new IllegalArgumentException(
					"This reasoning strategy is already associated with another capability.");
		}
	}

}
