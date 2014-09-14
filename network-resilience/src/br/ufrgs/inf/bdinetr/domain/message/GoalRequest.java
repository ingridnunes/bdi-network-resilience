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
package br.ufrgs.inf.bdinetr.domain.message;

import jade.content.Concept;
import jade.content.ContentElement;

import java.io.Serializable;

/**
 * @author Ingrid Nunes
 */
public class GoalRequest<K> implements Serializable, Concept,
		ContentElement {

	private static final long serialVersionUID = -1823087321617388067L;

	private Boolean beliefGoal;
	private K predicate;
	private Boolean subscribe;
	private Boolean value;

	public GoalRequest() {
		this.beliefGoal = true;
	}
	
	public GoalRequest(K predicate) {
		this.predicate = predicate;
		this.value = null;
		this.subscribe = false;
		this.beliefGoal = false;
	}
	
	public GoalRequest(K predicate, Boolean value) {
		this(predicate, value, false);
	}

	public GoalRequest(K predicate, Boolean value, Boolean subscribe) {
		this.predicate = predicate;
		this.value = value;
		this.subscribe = subscribe;
		this.beliefGoal = true;
	}

	public Boolean getBeliefGoal() {
		return beliefGoal;
	}

	public K getPredicate() {
		return predicate;
	}

	public Boolean getSubscribe() {
		return subscribe;
	}

	public Boolean getValue() {
		return value;
	}

	public void setBeliefGoal(Boolean beliefGoal) {
		this.beliefGoal = beliefGoal;
	}

	public void setPredicate(K predicate) {
		this.predicate = predicate;
	}

	public void setSubscribe(Boolean subscribe) {
		this.subscribe = subscribe;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

}
