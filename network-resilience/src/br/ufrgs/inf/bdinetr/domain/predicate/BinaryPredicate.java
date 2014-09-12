package br.ufrgs.inf.bdinetr.domain.predicate;

import jade.content.Concept;
import jade.content.ContentElement;

import java.io.Serializable;

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
