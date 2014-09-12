package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Flow;

public class Threat extends UnaryPredicate<Flow> {

	private static final long serialVersionUID = -5495943806870470494L;

	public Threat() {

	}

	public Threat(Flow flow) {
		super(flow);
	}

}