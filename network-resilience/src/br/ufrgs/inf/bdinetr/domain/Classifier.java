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
package br.ufrgs.inf.bdinetr.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * flowexporter addOperation: "victim:flow:idle:" remoteName: "setthreshold".
 * 
 * event at:
 * 
 * "classification" put: (factory/event create: #( "value_name" "value_source"
 * "value_destination" "value_protocol" ));
 * 
 * @author Ingrid Nunes
 */
public class Classifier extends PReSETRole {

	public Classifier(PReSETRouter router) {
		super(router);
	}

	public Set<Flow> classifyFlows(Ip ip) {
		Set<Flow> flows = new HashSet<>();
		if (ip.getAddress().equals("victim1")) {
			flows.add(new Flow(new Ip("DDoS1"), 80, new Ip("victim1"), 80,
					"http"));
			flows.add(new Flow(new Ip("DDoS2"), 80, new Ip("victim1"), 80,
					"http"));
		} else if (ip.getAddress().equals("victim2")) {
			flows.add(new Flow(new Ip("DDoS3"), 80, new Ip("victim2"), 80,
					"http"));
		}
		return flows;
	}

	public void turnFlowExporterOn() {

	}

}
