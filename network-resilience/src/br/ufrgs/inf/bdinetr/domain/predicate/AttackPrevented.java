package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Link;

public class AttackPrevented extends UnaryPredicate<Link> {

	private static final long serialVersionUID = -5495943806870470494L;

	public AttackPrevented() {

	}

	public AttackPrevented(Link link) {
		super(link);
	}

}
