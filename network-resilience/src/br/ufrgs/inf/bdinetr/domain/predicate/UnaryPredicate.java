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
package br.ufrgs.inf.bdinetr.domain.predicate;

import jade.content.Concept;
import jade.content.ContentElement;

import java.io.Serializable;

/**
 * @author Ingrid Nunes
 */
public abstract class UnaryPredicate<T> implements Serializable, Concept,
		ContentElement {

	private static final long serialVersionUID = -1506723105103606268L;

	protected T concept;

	public UnaryPredicate() {

	}

	public UnaryPredicate(T concept) {
		this.concept = concept;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && this.getClass().equals(obj.getClass())) {
			UnaryPredicate<?> p = (UnaryPredicate<?>) obj;
			return this.concept.equals(p.concept);
		}
		return false;
	}

	public T getConcept() {
		return concept;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.getClass() == null) ? 0 : this.getClass().hashCode());
		result = prime * result + ((concept == null) ? 0 : concept.hashCode());
		return result;
	}

	public void setConcept(T concept) {
		this.concept = concept;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		sb.append("(").append(concept).append(")");
		return sb.toString();
	}

}
