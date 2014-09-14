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
package br.ufrgs.inf.bdinetr.agent.belief;

import java.util.Set;

import bdi4jade.belief.Belief;
import bdi4jade.belief.DerivedPredicate;
import bdi4jade.belief.Predicate;
import br.ufrgs.inf.bdinetr.domain.predicate.Benign;
import br.ufrgs.inf.bdinetr.domain.predicate.Threat;

public class BenignBelief extends DerivedPredicate<Benign> {

	private static final long serialVersionUID = 6923761036847007160L;

	public BenignBelief(Benign benign) {
		super(benign);
	}

	@Override
	protected Boolean evaluate() {
		boolean exists = false;
		Set<Belief<?, ?>> threatBeliefs = getMainBeliefBase().getBeliefsByType(
				Threat.class);
		for (Belief<?, ?> belief : threatBeliefs) {
			Predicate<Threat> threat = (Predicate<Threat>) belief;
			assert threat.getValue();
			if (getName().getConcept().equals(
					threat.getName().getConcept().getDstIp())) {
				exists = true;
				break;
			}
		}
		return !exists;
	}

}