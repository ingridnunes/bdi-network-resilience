package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Ip;

public class Benign extends UnaryPredicate<Ip> {

	private static final long serialVersionUID = -5495943806870470494L;

	public Benign(Ip ip) {
		super(ip);
	}

}