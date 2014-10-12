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
package br.ufrgs.inf.bdinetr.domain.omnet;

import br.ufrgs.inf.bdinetr.domain.FlowExporter;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Router;

/**
 * @author Alberto Egon and Ingrid Nunes
 */
public class OMNeTFlowExporter extends OMNeTRouterComponent implements
		FlowExporter {

	public OMNeTFlowExporter(Router router) {
		super(router);
	}

	@Override
	public void turnFlowExporterOn(Ip ip) {
		Object[] params = new Object[3];
		params[0] = "Inet.sas1.core0.flowExporter";
		params[1] = new Integer(60);
		params[2] = new Integer(10);
		invoke("setthreshold", params);
	}

}
