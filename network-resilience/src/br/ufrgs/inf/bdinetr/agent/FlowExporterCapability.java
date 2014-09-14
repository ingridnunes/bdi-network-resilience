//----------------------------------------------------------------------------
// Copyright (C) 2011  Ingrid Nunes
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://inf.ufrgs.br/prosoft/bdi4jade/
//
//----------------------------------------------------------------------------
package br.ufrgs.inf.bdinetr.agent;

import bdi4jade.annotation.Parameter;
import bdi4jade.annotation.Parameter.Direction;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.plan.Plan.EndState;
import bdi4jade.plan.planbody.AbstractPlanBody;
import br.ufrgs.inf.bdinetr.domain.FlowExporter;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowExport;

/**
 * @author Ingrid Nunes
 */
public class FlowExporterCapability extends RouterAgentCapability {

	public class ExportFlowsPlanBody extends AbstractPlanBody {
		private static final long serialVersionUID = -3493377510830902961L;

		private Ip ip;

		@Override
		public void action() {
			role.turnFlowExporterOn(ip);
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

		this.exportFlows = new DefaultPlan(FlowExport.class,
				ExportFlowsPlanBody.class);
	}

	@Override
	public Role getRole() {
		return Role.FLOW_EXPORTER;
	}

}
