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

import bdi4jade.belief.TransientPropositionalBelief;
import bdi4jade.core.Capability;
import bdi4jade.goal.BeliefPresentGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.PropositionalBeliefValueGoal;
import br.ufrgs.inf.bdinetr.domain.Role;

/**
 * @author Ingrid Nunes
 */
public abstract class RouterAgentCapability extends Capability {

	public static final String ROLE_BELIEF = "role";

	private static final long serialVersionUID = -3491170777812144486L;

	protected void belief(Object proposition, Boolean value) {
		if (value == null) {
			getWholeCapability().getBeliefBase().removeBelief(proposition);
			log.debug("belief(~" + proposition + "))");
		} else {
			getWholeCapability().getBeliefBase().addOrUpdateBelief(
					new TransientPropositionalBelief(proposition, value));
			log.debug("belief(" + (value ? "" : "not ") + proposition + ")");
		}
	}

	protected Goal createGoal(Object proposition) {
		log.debug("goal(?" + proposition + "))");
		return new BeliefPresentGoal(proposition);
	}

	protected Goal createGoal(Object proposition, Boolean value) {
		log.debug("goal(" + (value ? "" : "not ") + proposition + "))");
		return new PropositionalBeliefValueGoal(proposition, value);
	}

	public abstract Role getRole();

	protected void goal(Object proposition) {
		getMyAgent().addGoal(this, createGoal(proposition));
	}

	protected void goal(Object proposition, Boolean value) {
		getMyAgent().addGoal(this, createGoal(proposition, value));
	}

}
