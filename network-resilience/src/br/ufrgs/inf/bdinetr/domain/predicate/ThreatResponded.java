package br.ufrgs.inf.bdinetr.domain.predicate;

import br.ufrgs.inf.bdinetr.domain.Flow;

public class ThreatResponded extends UnaryPredicate<Flow> {

	private static final long serialVersionUID = -5495943806870470494L;

	public ThreatResponded(Flow flow) {
		super(flow);
	}

}