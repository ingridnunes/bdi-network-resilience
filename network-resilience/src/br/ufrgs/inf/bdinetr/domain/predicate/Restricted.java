package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Ip;

public class Restricted extends UnaryPredicate<Ip> {

	private static final long serialVersionUID = -5495943806870470494L;

	public Restricted() {

	}

	public Restricted(Ip ip) {
		super(ip);
	}

}