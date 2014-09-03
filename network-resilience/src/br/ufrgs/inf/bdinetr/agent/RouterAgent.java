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

import bdi4jade.belief.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.core.Capability;
import bdi4jade.core.SingleCapabilityAgent;
import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;
import br.ufrgs.inf.bdinetr.domain.PReSETRouter;

/**
 * @author Ingrid Nunes
 */
public class RouterAgent extends SingleCapabilityAgent {

	public static class RootCapability extends Capability {

		public static final String ROUTER_BELIEF = "router";

		private static final long serialVersionUID = -2156730094556459899L;

		@bdi4jade.annotation.Belief
		private Belief<String, PReSETRouter> router = new TransientBelief<>(
				ROUTER_BELIEF);

		public RootCapability(PReSETRouter router) {
			this.router.setValue(router);
		}

	}

	private static final long serialVersionUID = 6534875498063013722L;

	public RouterAgent(PReSETRouter router) {
		super(new RootCapability(router));
		if (router.hasRole(RoleType.LINK_MONITOR)) {
			this.getCapability().addPartCapability(new LinkMonitorCapability());
		} else if (router.hasRole(RoleType.ANOMALY_DETECTION)) {
			this.getCapability().addPartCapability(
					new AnomalyDetectionCapability());
		} else if (router.hasRole(RoleType.RATE_LIMITER)) {
			this.getCapability().addPartCapability(new RateLimiterCapability());
		} else if (router.hasRole(RoleType.CLASSIFIER)) {
			this.getCapability().addPartCapability(new ClassifierCapability());
		}
	}

}
