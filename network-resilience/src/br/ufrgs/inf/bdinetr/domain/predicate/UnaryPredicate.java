package br.ufrgs.inf.bdinetr.domain.predicate;

import jade.content.Concept;
import jade.content.ContentElement;

public abstract class UnaryPredicate<T> implements ContentElement, Concept {

	private static final long serialVersionUID = -1506723105103606268L;

	protected T concept;

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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		sb.append("(").append(concept).append(")");
		return sb.toString();
	}

}
