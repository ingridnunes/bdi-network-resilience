package br.ufrgs.inf.bdinetr.palliative;

import bdi4jade.belief.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.extension.palliative.PalliativeCapability;
import br.ufrgs.inf.bdinetr.agent.FlowExporterCapability;
import br.ufrgs.inf.bdinetr.agent.RateLimiterCapability;
import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Classifier;
import br.ufrgs.inf.bdinetr.domain.FlowExporter;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.Router;

public class BDI2DoSCapability extends PalliativeCapability {

	public static final String ROUTER_BELIEF = "router";
	private static final long serialVersionUID = 4633237702870865396L;

	@bdi4jade.annotation.Belief
	private final Belief<String, Router> router;

	public BDI2DoSCapability(Router router) {
		this.router = new TransientBelief<>(ROUTER_BELIEF, router);

		addPartCapability(new RateLimiterCapability(getRateLimiter()));
		addPartCapability(new FlowExporterCapability(getFlowExporter()));
	}

	private LinkMonitor getLinkMonitor() {
		return (LinkMonitor) router.getValue().getRole(Role.LINK_MONITOR);
	}

	private RateLimiter getRateLimiter() {
		return (RateLimiter) router.getValue().getRole(Role.RATE_LIMITER);
	}

	private FlowExporter getFlowExporter() {
		return (FlowExporter) router.getValue().getRole(Role.FLOW_EXPORTER);
	}

	private Classifier getClassifier() {
		return (Classifier) router.getValue().getRole(Role.CLASSIFIER);
	}

	private AnomalyDetection getAnomalyDetection() {
		return (AnomalyDetection) router.getValue().getRole(Role.ANOMALY_DETECTION);
	}

}
