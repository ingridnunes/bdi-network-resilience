package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Ip;

public class Anomalous extends UnaryPredicate<Ip> {

	private static final long serialVersionUID = -5495943806870470494L;

	public Anomalous(Ip ip) {
		super(ip);
	}

}