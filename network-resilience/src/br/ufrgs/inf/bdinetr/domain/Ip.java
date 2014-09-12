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

import jade.content.Concept;
import jade.content.ContentElement;

/**
 * @author Ingrid Nunes
 */
public class Ip implements ContentElement, Concept {

	private static final long serialVersionUID = -6397439243662425210L;

	private final String address;

	public Ip(String address) {
		this.address = address;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Ip) {
			Ip ip = (Ip) obj;
			return this.address.equals(ip.address);
		}
		return false;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public int hashCode() {
		return address == null ? 0 : address.hashCode();
	}

	@Override
	public String toString() {
		return address;
	}

}
