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

import bdi4jade.belief.Belief;
import bdi4jade.belief.PropositionalBelief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.reasoning.AbstractReasoningStrategy;
import bdi4jade.reasoning.BeliefRevisionStrategy;
import bdi4jade.reasoning.OptionGenerationFunction;
import br.ufrgs.inf.bdinetr.BDINetRAgent.RootCapability;
import br.ufrgs.inf.bdinetr.domain.Device;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.OverUsage;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.RegularUsage;
import br.ufrgs.inf.bdinetr.domain.LinkProposition.Usage;

/**
 * @author Ingrid Nunes
 */
public class LinkMonitorCapability extends BDINetRAppCapability {

	private class ReasoningStrategy extends AbstractReasoningStrategy implements
			BeliefRevisionStrategy, OptionGenerationFunction {
		@Override
		public void generateGoals(GoalUpdateSet goalUpdateSet) {
			Set<Belief<?, ?>> overUsageBeliefs = getBeliefBase()
					.getBeliefsByType(OverUsage.class);
			for (Belief<?, ?> belief : overUsageBeliefs) {
				PropositionalBelief<OverUsage> overUsage = (PropositionalBelief<OverUsage>) belief;
				if (overUsage.getValue()) {
					PropositionalBelief<AttackPrevented> attackPrevented = (PropositionalBelief<AttackPrevented>) getBeliefBase()
							.getBelief(
									new AttackPrevented(overUsage.getName()
											.getLink()));
					if (attackPrevented == null || !attackPrevented.getValue()) {
						goal(new AttackPrevented(overUsage.getName().getLink()),
								Boolean.TRUE);
						goal(new RegularUsage(overUsage.getName().getLink()));
					}
				}
			}
		}

		@Override
		public void reviewBeliefs() {
			Set<Belief<?, ?>> linkUsageBeliefs = getBeliefBase()
					.getBeliefsByType(Usage.class);
			for (Belief<?, ?> belief : linkUsageBeliefs) {
				Belief<Usage, Double> linkUsage = (Belief<Usage, Double>) belief;
				OverUsage overUsage = new OverUsage(linkUsage.getName()
						.getLink());
				double percentageUsed = linkUsage.getName().getLink()
						.getUsedBandwidthPercentage();
				linkUsage.setValue(percentageUsed);
				if (percentageUsed > overUsageThreshold.getValue()) {
					PropositionalBelief<OverUsage> overUsageBelief = (PropositionalBelief<OverUsage>) getBeliefBase()
							.getBelief(overUsage);
					if (overUsageBelief == null || !overUsageBelief.getValue()) {
						belief(overUsage, true);
						belief(new RegularUsage(linkUsage.getName().getLink()),
								null);
					}
				} else {
					belief(overUsage, false);
				}
			}
		}
	}

	public static final double OVER_USAGE_THRESHOLD = 0.8;

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Belief
	private Belief<String, Double> overUsageThreshold = new TransientBelief<>(
			"threshold", OVER_USAGE_THRESHOLD);

	public LinkMonitorCapability() {
		ReasoningStrategy strategy = new ReasoningStrategy();
		setBeliefRevisionStrategy(strategy);
		setOptionGenerationFunction(strategy);
	}

	@Override
	protected void setup() {
		Belief<String, Device> device = (Belief<String, Device>) getBeliefBase()
				.getBelief(RootCapability.DEVICE_BELIEF);
		for (Link link : device.getValue().getConnectedLinks()) {
			getBeliefBase().addBelief(
					new TransientBelief<Usage, Double>(new Usage(link), link
							.getUsedBandwidthPercentage()));
		}
	}

}
