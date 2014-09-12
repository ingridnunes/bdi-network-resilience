package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Link;

public class RegularUsage extends UnaryPredicate<Link> {

	private static final long serialVersionUID = -5495943806870470494L;

	public RegularUsage(Link link) {
		super(link);
	}

}
