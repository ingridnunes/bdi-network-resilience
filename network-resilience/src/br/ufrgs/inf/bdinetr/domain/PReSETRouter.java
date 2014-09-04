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
package br.ufrgs.inf.bdinetr.domain;

import java.util.HashMap;
import java.util.Map;

import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;

/**
 * @author Ingrid Nunes
 */
public class PReSETRouter {

	private final Ip ip;
	private final Map<RoleType, PReSETRole> roles;

	public PReSETRouter(final Ip id, int roles) {
		this.ip = id;
		this.roles = new HashMap<>();
		if (RoleType.LINK_MONITOR.isPresent(roles)) {
			this.roles.put(RoleType.LINK_MONITOR, new LinkMonitor(this));
		}
		if (RoleType.ANOMALY_DETECTION.isPresent(roles)) {
			this.roles.put(RoleType.ANOMALY_DETECTION, new AnomalyDetection(
					this));
		}
		if (RoleType.RATE_LIMITER.isPresent(roles)) {
			this.roles.put(RoleType.RATE_LIMITER, new RateLimiter(this));
		}
		if (RoleType.CLASSIFIER.isPresent(roles)) {
			this.roles.put(RoleType.CLASSIFIER, new Classifier(this));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PReSETRouter) {
			PReSETRouter d = (PReSETRouter) obj;
			return this.ip.equals(d.ip);
		}
		return false;
	}

	public Ip getIp() {
		return ip;
	}

	public PReSETRole getRole(RoleType role) {
		return roles.get(role);
	}

	@Override
	public int hashCode() {
		return ip == null ? 0 : ip.hashCode();
	}

	public boolean hasRole(RoleType role) {
		return roles.containsKey(role);
	}

	@Override
	public String toString() {
		return ip.toString();
	}

}
