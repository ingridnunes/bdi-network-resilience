package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Link;

public class FullyOperational extends UnaryPredicate<Link> {

	private static final long serialVersionUID = -5495943806870470494L;

	public FullyOperational(Link link) {
		super(link);
	}

}
