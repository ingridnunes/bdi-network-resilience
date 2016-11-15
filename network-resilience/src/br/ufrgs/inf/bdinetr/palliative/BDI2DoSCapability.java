package br.ufrgs.inf.bdinetr.palliative;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.belief.Belief;
import bdi4jade.belief.BeliefSet;
import bdi4jade.belief.PredicateBelief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientBeliefSet;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.event.GoalEvent;
import bdi4jade.event.GoalListener;
import bdi4jade.extension.palliative.PlanRequiredResource;
import bdi4jade.extension.palliative.ResourcePreferences;
import bdi4jade.extension.palliative.goal.ConstrainedGoal;
import bdi4jade.extension.palliative.goal.ObjectiveFunction;
import bdi4jade.extension.palliative.graph.AlternativeCauseSet;
import bdi4jade.extension.palliative.graph.CauseEffectKnowledgeModel;
import bdi4jade.extension.palliative.graph.CauseEffectRelationship;
import bdi4jade.extension.palliative.logics.Fact;
import bdi4jade.extension.palliative.logics.UnaryPredicate;
import bdi4jade.extension.palliative.reasoning.PalliativeOptionGenerationFunction;
import bdi4jade.extension.palliative.reasoning.PalliativePlanSelectionStrategy;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.BeliefValueGoal;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalStatus;
import bdi4jade.goal.GoalTemplate;
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.goal.PredicateGoal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import bdi4jade.reasoning.BeliefRevisionStrategy;
import br.ufrgs.inf.bdinetr.agent.FlowExporterCapability;
import br.ufrgs.inf.bdinetr.agent.RateLimiterCapability;
import br.ufrgs.inf.bdinetr.agent.RouterAgentCapability;
import br.ufrgs.inf.bdinetr.agent.belief.AnomalousUsageBelief;
import br.ufrgs.inf.bdinetr.agent.belief.BenignBelief;
import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Classifier;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.FlowExporter;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.predicate.Anomalous;
import br.ufrgs.inf.bdinetr.domain.predicate.AnomalousUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.predicate.Benign;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowExport;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.IpRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.LinkRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.OverUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.OverUsageCause;
import br.ufrgs.inf.bdinetr.domain.predicate.Restricted;
import br.ufrgs.inf.bdinetr.domain.predicate.Threat;

public class BDI2DoSCapability extends RouterAgentCapability implements BeliefRevisionStrategy, GoalListener, Observer {

	public class OverUsageGoal extends ConstrainedGoal {

		public OverUsageGoal(Link link, Boolean value) {
			super(new PredicateGoal<OverUsage>(new OverUsage(link), false));
			this.addObjectiveFunction(Resources.TIME, ObjectiveFunction.MINIMIZE);
			this.addObjectiveFunction(Resources.NETWORK_AVAILABILITY, ObjectiveFunction.MAXIMIZE);
		}

	}

	public class LimitLinkRate extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(new LinkRateLimited(link), true));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal()).getOutputBelief());
						belief(new AttackPrevented(link), true);
					} else {
						setEndState(EndState.FAILED);
					}
				}
			}
		}

		@Override
		protected void init() {
			this.subgoalDispatched = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(UnaryPredicate<Link> attackPrevented) {
			this.link = attackPrevented.getVariable();
		}
	}

	public class RestoreLinkRate extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(new LinkRateLimited(link), false));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal()).getOutputBelief());
						belief(new AttackPrevented(link), false);
						belief(new AnomalousUsage(link), null);
					} else {
						setEndState(EndState.FAILED);
					}
				}
			}
		}

		@Override
		protected void init() {
			this.subgoalDispatched = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(AttackPrevented attackPrevented) {
			this.link = attackPrevented.getVariable();
		}
	}

	public class AnalyseLinkStatistics extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Link link;

		@Override
		public void execute() {
			Set<Ip> outliers = getAnomalyDetection().detectIntrusion(link);
			BeliefSet<OverUsageCause, Ip> overUsageCause = new TransientBeliefSet<OverUsageCause, Ip>(
					new OverUsageCause(link));
			if (outliers != null && !outliers.isEmpty()) {
				for (Ip outlier : outliers) {
					belief(new Anomalous(outlier), true);
					belief(new Benign(outlier), null);
					overUsageCause.addValue(outlier);
				}
			}
			addBelief(overUsageCause);
			addBelief(new AnomalousUsageBelief(new AnomalousUsage(link)));
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(AnomalousUsage anomalousUsage) {
			this.link = anomalousUsage.getVariable();
		}
	}

	public class LimitIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(new IpRateLimited(ip), true));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal()).getOutputBelief());
						belief(new Restricted(ip), true);

						Set<Belief<?, ?>> overUsageCauseBeliefs = getBeliefBase()
								.getBeliefsByType(OverUsageCause.class);
						Iterator<Belief<?, ?>> it = overUsageCauseBeliefs.iterator();
						while (it.hasNext()) {
							BeliefSet<OverUsageCause, Ip> overUsageCause = (BeliefSet<OverUsageCause, Ip>) it.next();
							if (overUsageCause.hasValue(ip)) {
								// ip in OverUsageCause(l) --> remove
								overUsageCause.removeValue(ip);
							}
						}
						belief(new Anomalous(ip), false);
					} else {
						setEndState(EndState.FAILED);
					}
				}
			}
		}

		@Override
		protected void init() {
			this.subgoalDispatched = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(UnaryPredicate<Ip> restricted) {
			this.ip = restricted.getVariable();
		}
	}

	public class RestoreIPRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(new IpRateLimited(ip), false));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal()).getOutputBelief());
						belief(new Restricted(ip), false);
						belief(new Anomalous(ip), null);
					} else {
						setEndState(EndState.FAILED);
					}
				}
			}
		}

		@Override
		protected void init() {
			this.subgoalDispatched = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(Restricted restricted) {
			this.ip = restricted.getVariable();
		}
	}

	public class AnalyseIPFlows extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new FlowExport(ip));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						Set<Flow> malicious = getClassifier().classifyFlows(ip);
						for (Flow flow : malicious) {
							belief(new Threat(flow), true);
						}
						addBelief(new BenignBelief(new Benign(ip)));
					} else {
						setEndState(EndState.FAILED);
					}
				} else {
					block();
				}
			}
		}

		@Override
		public void init() {
			this.subgoalDispatched = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(Benign benign) {
			this.ip = benign.getVariable();
		}
	}

	public class LimitFlowRatePlan extends BeliefGoalPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Flow flow;
		private boolean subgoalDispatched;

		@Override
		public void execute() {
			if (!subgoalDispatched) {
				dispatchSubgoalAndListen(new BeliefValueGoal<>(new FlowRateLimited(flow), true));
				this.subgoalDispatched = true;
			} else {
				GoalEvent event = getGoalEvent();
				if (event != null) {
					if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
						addBelief(((BeliefGoal<?>) event.getGoal()).getOutputBelief());
						belief(new Threat(flow), false);
					} else {
						setEndState(EndState.FAILED);
					}
				}
			}
		}

		@Override
		protected void init() {
			this.subgoalDispatched = false;
		}

		@Parameter(direction = Direction.IN)
		public void setBeliefName(Threat threat) {
			this.flow = threat.getVariable();
		}
	}

	public static final String ROUTER_BELIEF = "router";
	private static final long serialVersionUID = 4633237702870865396L;

	@bdi4jade.annotation.Belief
	private final Belief<String, Router> router;

	@bdi4jade.annotation.Belief
	protected CauseEffectKnowledgeModel causeEffectKnowledgeModel = new CauseEffectKnowledgeModel();

	@bdi4jade.annotation.Belief
	protected ResourcePreferences preferences = new ResourcePreferences();

	@bdi4jade.annotation.Plan
	private Plan limitLinkRate;
	@bdi4jade.annotation.Plan
	private Plan restoreLinkRate;

	@bdi4jade.annotation.Plan
	private Plan analyseLinkStatistics;
	@bdi4jade.annotation.Plan
	private Plan limitIpRate;
	@bdi4jade.annotation.Plan
	private Plan restoreIpRate;

	@bdi4jade.annotation.Plan
	private Plan analyseIpFlows;
	@bdi4jade.annotation.Plan
	private Plan limitFlowRate;

	public BDI2DoSCapability(Router router) {
		this.router = new TransientBelief<>(ROUTER_BELIEF, router);
		getLinkMonitor().addObserver(this);

		setBeliefRevisionStrategy(this);
		setOptionGenerationFunction(new MyGoalGenerationFunction());
		setPlanSelectionStrategy(new PalliativePlanSelectionStrategy(this));

		addPartCapability(new RateLimiterCapability(getRateLimiter()));
		addPartCapability(new FlowExporterCapability(getFlowExporter()));

		// Cause-Effect Knowledge Model
		CauseEffectRelationship cer = new CauseEffectRelationship(
				new Fact(new OverUsage(new Link("AFFECTED_LINK")), true));
		cer.addOptionalCause(new Fact(new AnomalousUsage(new Link("AFFECTED_LINK")), true));
		AlternativeCauseSet alternatives = new AlternativeCauseSet(1, 2);
		alternatives.addAlternativeCause(new Fact(new Anomalous(new Ip("victim1")), true));
		alternatives.addAlternativeCause(new Fact(new Anomalous(new Ip("victim2")), true));
		cer.addAlternativeCauseSet(alternatives);
		causeEffectKnowledgeModel.addCauseEffectRelationship(cer);

		cer = new CauseEffectRelationship(new Fact(new Anomalous(new Ip("victim1")), true));
		cer.addOptionalCause(new Fact(new Benign(new Ip("victim1")), false));
		alternatives = new AlternativeCauseSet(1, 2);
		alternatives.addAlternativeCause(
				new Fact(new Threat(new Flow(new Ip("DDoS1"), 80, new Ip("victim1"), 80, "http")), true));
		alternatives.addAlternativeCause(
				new Fact(new Threat(new Flow(new Ip("DDoS2"), 80, new Ip("victim1"), 80, "http")), true));
		cer.addAlternativeCauseSet(alternatives);
		causeEffectKnowledgeModel.addCauseEffectRelationship(cer);

		cer = new CauseEffectRelationship(new Fact(new Anomalous(new Ip("victim2")), true));
		cer.addOptionalCause(new Fact(new Benign(new Ip("victim2")), false));
		alternatives = new AlternativeCauseSet(1, 1);
		alternatives.addAlternativeCause(
				new Fact(new Threat(new Flow(new Ip("DDoS3"), 80, new Ip("victim2"), 80, "http")), true));
		cer.addAlternativeCauseSet(alternatives);
		causeEffectKnowledgeModel.addCauseEffectRelationship(cer);

		// Resource Preferences
		preferences.getValue().put(Resources.TIME, 0.75);
		preferences.getValue().put(Resources.NETWORK_AVAILABILITY, 0.25);

		// Plans

		this.limitLinkRate = new DefaultPlan(
				new GoalTemplate[] { GoalTemplateFactory.hasBeliefOfTypeWithValue(AttackPrevented.class, Boolean.TRUE),
						GoalTemplateFactory.hasBeliefOfTypeWithValue(OverUsage.class, Boolean.FALSE) },
				LimitLinkRate.class);
		PlanRequiredResource prr = new PlanRequiredResource();
		prr.setRequiredResource(Resources.TIME, 5.0);
		prr.setRequiredResource(Resources.NETWORK_AVAILABILITY, 0.6);
		this.limitLinkRate.putMetadata(PlanRequiredResource.METADATA_NAME, prr);
		this.restoreLinkRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(AttackPrevented.class, Boolean.FALSE),
				RestoreLinkRate.class) {
			@Override
			public boolean isContextApplicable(Goal goal) {
				BeliefGoal<AttackPrevented> bg = (BeliefGoal<AttackPrevented>) goal;
				PredicateBelief<LinkRateLimited> linkRateLimited = (PredicateBelief<LinkRateLimited>) getBeliefBase()
						.getBelief(new LinkRateLimited(bg.getBeliefName().getVariable()));
				return (linkRateLimited != null && linkRateLimited.getValue());
			}
		};

		this.analyseLinkStatistics = new DefaultPlan(GoalTemplateFactory.hasBeliefOfType(AnomalousUsage.class),
				AnalyseLinkStatistics.class);
		this.limitIpRate = new DefaultPlan(
				new GoalTemplate[] { GoalTemplateFactory.hasBeliefOfTypeWithValue(Restricted.class, Boolean.TRUE),
						GoalTemplateFactory.hasBeliefOfTypeWithValue(Anomalous.class, Boolean.FALSE) },
				LimitIPRatePlan.class);
		this.restoreIpRate = new DefaultPlan(
				GoalTemplateFactory.hasBeliefOfTypeWithValue(Restricted.class, Boolean.FALSE),
				RestoreIPRatePlan.class) {
			@Override
			public boolean isContextApplicable(Goal goal) {
				BeliefGoal<Restricted> bg = (BeliefGoal<Restricted>) goal;
				PredicateBelief<IpRateLimited> ipRateLimited = (PredicateBelief<IpRateLimited>) getBeliefBase()
						.getBelief(new IpRateLimited(bg.getBeliefName().getVariable()));
				return (ipRateLimited != null && ipRateLimited.getValue());
			}
		};

		this.analyseIpFlows = new DefaultPlan(GoalTemplateFactory.hasBeliefOfType(Benign.class)

				, AnalyseIPFlows.class);
		this.limitFlowRate = new DefaultPlan(
				new GoalTemplate[] { GoalTemplateFactory.hasBeliefOfTypeWithValue(Threat.class, Boolean.FALSE) },
				LimitFlowRatePlan.class) {
			public boolean isContextApplicable(Goal goal) {
				BeliefGoal<Threat> bg = (BeliefGoal<Threat>) goal;
				PredicateBelief<Threat> threat = (PredicateBelief<Threat>) getBeliefBase()
						.getBelief(bg.getBeliefName());
				return (threat != null && threat.getValue());
			};
		};

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

	@Override
	public Role getRole() {
		// FIXME
		return Role.LINK_MONITOR;
	}

	@Override
	protected Set<Class<?>> getLowPriorityGoal() {
		Set<Class<?>> lowPriorityGoals = new HashSet<>();
		lowPriorityGoals.add(AnomalousUsage.class);
		lowPriorityGoals.add(Benign.class);
		return lowPriorityGoals;
	}

	class MyGoalGenerationFunction extends PalliativeOptionGenerationFunction {

		private Boolean added = false;

		public MyGoalGenerationFunction() {
			super(BDI2DoSCapability.this);
		}

		@Override
		public void generateGoals(GoalUpdateSet goalUpdateSet) {
			super.generateGoals(goalUpdateSet);

			// Link Monitor
			Set<Belief<?, ?>> overUsageBeliefs = getBeliefBase().getBeliefsByType(OverUsage.class);
			for (Belief<?, ?> belief : overUsageBeliefs) {
				PredicateBelief<OverUsage> overUsage = (PredicateBelief<OverUsage>) belief;
				Link link = overUsage.getName().getVariable();
				if (overUsage.getValue()) {
					PredicateBelief<AnomalousUsage> anomalousUsage = (PredicateBelief<AnomalousUsage>) getBeliefBase()
							.getBelief(new AnomalousUsage(link));
					PredicateBelief<AttackPrevented> attackPrevented = (PredicateBelief<AttackPrevented>) getBeliefBase()
							.getBelief(new AttackPrevented(link));
					if (anomalousUsage == null || anomalousUsage.getValue()) {
						// OverUsage(l) AND !(not AnomalousUsage(l)) -->
						// ~OverUsage(l)
						if (!added) {
							goalUpdateSet.generateGoal(new OverUsageGoal(link, Boolean.FALSE), BDI2DoSCapability.this,
									null);
							log.info("OverUsageGoal added.");
							added = true;
						}
					}
				}
			}

			Set<Belief<?, ?>> attackPreventedBeliefs = getBeliefBase().getBeliefsByType(AttackPrevented.class);
			for (Belief<?, ?> belief : attackPreventedBeliefs) {
				PredicateBelief<AttackPrevented> attackPrevented = (PredicateBelief<AttackPrevented>) belief;
				if (attackPrevented.getValue()) {
					PredicateBelief<AnomalousUsage> anomalousUsage = (PredicateBelief<AnomalousUsage>) getBeliefBase()
							.getBelief(new AnomalousUsage(attackPrevented.getName().getVariable()));
					// AttackPrevented(l) AND not AnomalousUsage(l) --> not
					// AttackPrevented(l)
					if (anomalousUsage != null && !anomalousUsage.getValue()) {
						goal(goalUpdateSet, attackPrevented.getName(), Boolean.FALSE);
					}
				}
			}

			Set<Belief<?, ?>> restrictedBeliefs = getBeliefBase().getBeliefsByType(Restricted.class);
			for (Belief<?, ?> belief : restrictedBeliefs) {
				PredicateBelief<Restricted> restricted = (PredicateBelief<Restricted>) belief;
				if (restricted.getValue()) {
					PredicateBelief<Benign> benign = (PredicateBelief<Benign>) getBeliefBase()
							.getBelief(new Benign(restricted.getName().getVariable()));
					if (benign != null && benign.getValue()) {
						// Restricted(l) AND Benign(l) --> not
						// Restricted(l)
						goal(goalUpdateSet, restricted.getName(), false);
					}
				}
			}

		}
	}

	@Override
	public void goalPerformed(GoalEvent event) {
		if (GoalStatus.ACHIEVED.equals(event.getStatus())) {
			addBelief(((BeliefGoal<?>) event.getGoal()).getOutputBelief());
		}
	}

	@Override
	public void reviewBeliefs() {
		for (Link link : getLinkMonitor().getLinks()) {
			OverUsage overUsage = new OverUsage(link);
			if (getLinkMonitor().isOverUsage(link)) {
				PredicateBelief<OverUsage> overUsageBelief = (PredicateBelief<OverUsage>) getBeliefBase()
						.getBelief(overUsage);
				if (overUsageBelief == null || !overUsageBelief.getValue()) {
					belief(overUsage, true);
					belief(new AnomalousUsage(link), null);
				}
			} else {
				belief(overUsage, false);
				getLinkMonitor().removeLink(link);
			}
		}

		Set<Belief<?, ?>> overUsageCauseBeliefs = getBeliefBase().getBeliefsByType(OverUsageCause.class);
		Iterator<Belief<?, ?>> it = overUsageCauseBeliefs.iterator();
		while (it.hasNext()) {
			BeliefSet<OverUsageCause, Ip> overUsageCause = (BeliefSet<OverUsageCause, Ip>) it.next();
			if (overUsageCause.getValue().isEmpty()) {
				removeBelief(overUsageCause);
			}
			PredicateBelief<AnomalousUsage> anomalousUsage = (PredicateBelief<AnomalousUsage>) getBeliefBase()
					.getBelief(new AnomalousUsage(overUsageCause.getName().getVariable()));
			if (anomalousUsage != null && (anomalousUsage.getValue() == null || !anomalousUsage.getValue())) {
				removeBelief(anomalousUsage);
			}
		}

		Set<Belief<?, ?>> benignBeliefs = getBeliefBase().getBeliefsByType(Benign.class);
		Iterator<Belief<?, ?>> it2 = benignBeliefs.iterator();
		while (it2.hasNext()) {
			PredicateBelief<Benign> benignBelief = (PredicateBelief<Benign>) it2.next();
			if (benignBelief.getValue() == null || benignBelief.getValue()) {
				removeBelief(benignBelief);
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		getMyAgent().restart();
	}

}
