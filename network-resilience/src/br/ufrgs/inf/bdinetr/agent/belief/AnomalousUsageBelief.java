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

import bdi4jade.belief.BeliefSet;
import bdi4jade.belief.DerivedPredicate;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.predicate.AnomalousUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.OverUsageCause;

public class AnomalousUsageBelief extends DerivedPredicate<AnomalousUsage> {

	private static final long serialVersionUID = 6923761036847007160L;

	public AnomalousUsageBelief(AnomalousUsage anomalousUsage) {
		super(anomalousUsage);
	}

	@Override
	protected Boolean evaluate() {
		BeliefSet<OverUsageCause, Ip> overUsageCauseBeliefs = (BeliefSet<OverUsageCause, Ip>) getMainBeliefBase()
				.getBelief(new OverUsageCause(getName().getConcept()));
		return overUsageCauseBeliefs == null ? false : !overUsageCauseBeliefs
				.getValue().isEmpty();
	}

}
