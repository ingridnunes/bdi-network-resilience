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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ingrid Nunes
 */
public class Device {

	private final Set<Link> connectedLinks;
	private final IpAddress ip;

	public Device(final IpAddress id) {
		this.ip = id;
		this.connectedLinks = new HashSet<>();
	}

	public void connectLink(Link link) {
		connectedLinks.add(link);
	}

	public void disconnectLink(Link link) {
		connectedLinks.remove(link);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Device) {
			Device d = (Device) obj;
			return this.ip.equals(d.ip);
		}
		return false;
	}

	public Set<Link> getConnectedLinks() {
		return connectedLinks;
	}

	public IpAddress getIp() {
		return ip;
	}

	@Override
	public int hashCode() {
		return ip == null ? 0 : ip.hashCode();
	}

	@Override
	public String toString() {
		return ip.toString();
	}

}
