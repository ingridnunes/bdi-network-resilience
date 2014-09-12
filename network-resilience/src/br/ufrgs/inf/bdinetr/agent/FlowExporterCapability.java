package br.ufrgs.inf.bdinetr.agent;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.goal.GoalTemplateFactory;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import br.ufrgs.inf.bdinetr.agent.RouterAgent.RootCapability.ExportFlows;
import br.ufrgs.inf.bdinetr.domain.FlowExporter;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Role;

public class FlowExporterCapability extends RouterAgentCapability {

	public class ExportFlowsPlanBody extends AbstractPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;

		@Override
		public void action() {
			role.turnFlowExporterOn();
			setEndState(EndState.SUCCESSFUL);
		}

		@Parameter(direction = Direction.IN)
		public void setIp(Ip ip) {
			this.ip = ip;
		}
	}

	private static final long serialVersionUID = -1705728861020677126L;

	@bdi4jade.annotation.Plan
	private Plan exportFlows;
	@bdi4jade.annotation.TransientBelief
	private final FlowExporter role;

	public FlowExporterCapability(FlowExporter flowExporter) {
		this.role = flowExporter;

		exportFlows = new DefaultPlan(
				GoalTemplateFactory.goalOfType(ExportFlows.class),
				ExportFlowsPlanBody.class);
	}

	@Override
	public Role getRole() {
		return Role.FLOW_EXPORTER;
	}
}
