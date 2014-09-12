package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;

public class OverUsageCause extends BinaryPredicate<Ip, Link> {

	private static final long serialVersionUID = -5495943806870470494L;

	public OverUsageCause() {

	}

	public OverUsageCause(Ip ip, Link link) {
		super(ip, link);
	}

}