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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bdi4jade.belief.Belief;
import bdi4jade.belief.Predicate;
import bdi4jade.belief.TransientPredicate;
import bdi4jade.core.Capability;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.event.GoalListener;
import bdi4jade.goal.BeliefNotPresentGoal;
import bdi4jade.goal.BeliefPresentGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.PredicateGoal;
import br.ufrgs.inf.bdinetr.domain.Role;

/**
 * @author Ingrid Nunes
 */
public abstract class RouterAgentCapability extends Capability {

	public static final String ROLE_BELIEF = "role";
	private static final long serialVersionUID = -3491170777812144486L;

	protected final Log log = LogFactory.getLog(getClass());

	protected void addBelief(Belief<?, ?> belief) {
		getBeliefBase().addOrUpdateBelief(belief);
		log.debug("belief added or updated: " + belief);
	}

	protected Predicate<?> belief(Object proposition, Boolean value) {
		if (value == null) {
			getBeliefBase().removeBelief(proposition);
			log.debug("belief(~" + proposition + "))");
			return null;
		} else {
			Predicate<?> predicate = new TransientPredicate(proposition, value);
			getBeliefBase().addOrUpdateBelief(predicate);
			log.debug("belief(" + (value ? "" : "not ") + proposition + ")");
			return predicate;
		}
	}

	public abstract Role getRole();

	protected void goal(GoalUpdateSet goalUpdateSet, Object proposition) {
		goal(goalUpdateSet, proposition, (GoalListener) null);
	}

	protected void goal(GoalUpdateSet goalUpdateSet, Object proposition,
			Boolean value) {
		goal(goalUpdateSet, proposition, value, null);
	}

	protected void goal(GoalUpdateSet goalUpdateSet, Object proposition,
			Boolean value, GoalListener listener) {
		Goal goal;
		if (value == null) {
			goal = new BeliefNotPresentGoal(proposition);
		} else {
			goal = new PredicateGoal(proposition, value);
		}
		if (!getMyAgent().hasGoal(goal)) {
			if (value == null) {
				log.debug("goal(~" + proposition + "))");
			} else {
				log.debug("goal(" + (value ? "" : "not ") + proposition + "))");
			}
			goalUpdateSet.generateGoal(goal, this, listener);
		}
	}

	protected void goal(GoalUpdateSet goalUpdateSet, Object proposition,
			GoalListener listener) {
		Goal goal = new BeliefPresentGoal(proposition);
		if (!getMyAgent().hasGoal(goal)) {
			log.debug("goal(?" + proposition + "))");
			goalUpdateSet.generateGoal(goal, this, listener);
		}
	}

	protected void removeBelief(Belief<?, ?> belief) {
		getBeliefBase().removeBelief(belief.getName());
		log.debug("belief removed: " + belief);
	}

}
