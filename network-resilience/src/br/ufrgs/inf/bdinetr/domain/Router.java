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

/**
 * @author Ingrid Nunes
 */
public class Router {

	private final Map<Role, RouterComponent> components;
	private final Ip ip;
	private final String type;

	public Router(final Ip ip, int roles, AbstractRouterComponentFactory factory) {
		this(ip, null, roles, factory);
	}

	public Router(final Ip ip, final String type, int roles,
			AbstractRouterComponentFactory factory) {
		this.ip = ip;
		this.type = type;
		this.components = new HashMap<>();
		for (Role role : Role.values()) {
			if (role.isPresent(roles)) {
				this.components.put(role,
						factory.createRouterComponent(role, this));
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Router) {
			Router d = (Router) obj;
			return (this.ip + this.type).equals((d.ip + type));
		}
		return false;
	}

	public Ip getIp() {
		return ip;
	}

	public RouterComponent getRole(Role role) {
		return components.get(role);
	}

	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return (this.ip + this.type).hashCode();
	}

	public boolean hasRole(Role role) {
		return components.containsKey(role);
	}

	@Override
	public String toString() {
		return ip.toString();
	}

}
