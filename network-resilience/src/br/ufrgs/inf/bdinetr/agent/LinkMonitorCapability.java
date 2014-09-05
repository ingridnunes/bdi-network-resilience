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
import java.util.Set;

import bdi4jade.belief.Belief;
import bdi4jade.belief.PropositionalBelief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.core.Capability;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.reasoning.BeliefRevisionStrategy;
import bdi4jade.reasoning.OptionGenerationFunction;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.Observer;
import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;
import br.ufrgs.inf.bdinetr.domain.logic.LinkProposition.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.logic.LinkProposition.OverUsage;
import br.ufrgs.inf.bdinetr.domain.logic.LinkProposition.RegularUsage;

/**
 * @author Ingrid Nunes
 */
public class LinkMonitorCapability extends RouterAgentCapability implements
		BeliefRevisionStrategy, OptionGenerationFunction, Observer {

	public static final double OVER_USAGE_THRESHOLD = 0.8;

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.TransientBeliefSet
	private final Set<Link> linkEvents;
	@bdi4jade.annotation.Belief
	private Belief<String, Double> overUsageThreshold;
	@bdi4jade.annotation.TransientBelief
	private LinkMonitor role;

	public LinkMonitorCapability(LinkMonitor linkMonitor) {
		this.role = linkMonitor;
		role.attachObserver(this);

		setBeliefRevisionStrategy(this);
		setOptionGenerationFunction(this);

		this.linkEvents = new HashSet<>();
		this.overUsageThreshold = new TransientBelief<>("threshold",
				OVER_USAGE_THRESHOLD);
	}

	@Override
	public void generateGoals(GoalUpdateSet goalUpdateSet) {
		Set<Belief<?, ?>> overUsageBeliefs = getBeliefBase().getBeliefsByType(
				OverUsage.class);
		for (Belief<?, ?> belief : overUsageBeliefs) {
			PropositionalBelief<OverUsage> overUsage = (PropositionalBelief<OverUsage>) belief;
			if (overUsage.getValue()) {
				PropositionalBelief<AttackPrevented> attackPrevented = (PropositionalBelief<AttackPrevented>) getBeliefBase()
						.getBelief(
								new AttackPrevented(overUsage.getName()
										.getLink()));
				if (attackPrevented == null || !attackPrevented.getValue()) {
					goalUpdateSet.generateGoal(createGoal(new AttackPrevented(
							overUsage.getName().getLink()), Boolean.TRUE));
					goalUpdateSet.generateGoal(createGoal(new RegularUsage(
							overUsage.getName().getLink())));
				}
			}
		}
	}

	@Override
	public RoleType getRole() {
		return RoleType.LINK_MONITOR;
	}

	@Override
	public void reviewBeliefs() {
		synchronized (linkEvents) {
			for (Link link : linkEvents) {
				OverUsage overUsage = new OverUsage(link);
				boolean isOverUsage = role.isOverUsage(link);

				if (isOverUsage) {
					PropositionalBelief<OverUsage> overUsageBelief = (PropositionalBelief<OverUsage>) getBeliefBase()
							.getBelief(overUsage);
					if (overUsageBelief == null || !overUsageBelief.getValue()) {
						belief(overUsage, true);
						belief(new RegularUsage(link), null);
					}
				} else {
					belief(overUsage, null);
				}
			}
			linkEvents.clear();
		}
	}

	@Override
	public void setCapability(Capability capability) {
		if (!this.equals(capability)) {
			throw new IllegalArgumentException(
					"This reasoning strategy is already associated with another capability.");
		}
	}

	@Override
	public void update(Object o, Object arg) {
		synchronized (linkEvents) {
			this.linkEvents.add((Link) arg);
		}
		getMyAgent().restart();
	}

}
