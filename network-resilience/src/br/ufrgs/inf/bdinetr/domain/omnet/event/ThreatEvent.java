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
package br.ufrgs.inf.bdinetr.domain.omnet.event;

import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;

/**
 * event at:
 * 
 * "classification" put: (factory/event create: #( "value_name" "value_source"
 * "value_destination" "value_protocol" ));
 * 
 * @author Alberto Egon and Ingrid Nunes
 */
public class ThreatEvent implements OMNeTEvent {

	public static final String NAME = "classification";

	private Flow flow;

	public ThreatEvent(String s) {
		String[] parts = s.split(";");
		this.flow = new Flow(new Ip(parts[2]), 0, new Ip(parts[3]), 0, parts[4]);
	}

	public Flow getFlow() {
		return flow;
	}

}
