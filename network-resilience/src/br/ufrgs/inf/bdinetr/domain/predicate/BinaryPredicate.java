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
public abstract class BinaryPredicate<T, U> implements Serializable, Concept,
		ContentElement {

	private static final long serialVersionUID = -1506723105103606268L;

	protected T first;
	protected U second;

	public BinaryPredicate() {

	}

	public BinaryPredicate(T first, U second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && this.getClass().equals(obj.getClass())) {
			BinaryPredicate<?, ?> p = (BinaryPredicate<?, ?>) obj;
			return this.first.equals(p.first) && this.second.equals(p.second);
		}
		return false;
	}

	public T getFirst() {
		return first;
	}

	public U getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.getClass() == null) ? 0 : this.getClass().hashCode());
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	public void setFirst(T first) {
		this.first = first;
	}

	public void setSecond(U second) {
		this.second = second;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		sb.append("(").append(first).append(",").append(second).append(")");
		return sb.toString();
	}

}
