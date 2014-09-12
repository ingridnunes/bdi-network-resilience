package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Link;

public class OverUsage extends UnaryPredicate<Link> {

	private static final long serialVersionUID = -5495943806870470494L;

	public OverUsage(Link link) {
		super(link);
	}

}
