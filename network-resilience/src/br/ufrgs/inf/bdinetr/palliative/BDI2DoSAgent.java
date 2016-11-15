package br.ufrgs.inf.bdinetr.palliative;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bdi4jade.core.SingleCapabilityAgent;
import br.ufrgs.inf.bdinetr.agent.RouterAgent;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.ontology.BDINetROntology;
import jade.content.lang.sl.SLCodec;

public class BDI2DoSAgent extends SingleCapabilityAgent {

	private static final Log log = LogFactory.getLog(RouterAgent.class);
	private static final long serialVersionUID = 6534875498063013722L;

	public BDI2DoSAgent(Router router) {
		super(new BDI2DoSCapability(router));
	}

	@Override
	protected void init() {
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(BDINetROntology.getInstance());
	}

}