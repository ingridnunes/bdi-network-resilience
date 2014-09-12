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
public class Link implements ContentElement, Concept {

	private static final long serialVersionUID = 8547078610455643585L;

	private final String id;

	public Link(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Link) {
			Link l = (Link) obj;
			return this.id.equals(l.id);
		}
		return false;
	}

	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}

}
