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

import java.util.HashSet;
import java.util.Set;

import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.Observer;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.omnet.event.AnomalousEvent;

/**
 * @author Alberto Egon and Ingrid Nunes
 */
public class OMNeTAnomalyDetection extends OMNeTRouterComponent implements
		AnomalyDetection, Observer {

	public OMNeTAnomalyDetection(Router router) {
		super(router);
		EventBroker.getInstance().attachObserver(this);
	}

	@Override
	public Set<Ip> detectIntrusion(Link link) {
		Set<Ip> intrusions = new HashSet<>();
		if (link.getId().equals("AFFECTED_LINK")) {
			intrusions.add(new Ip("victim1"));
			intrusions.add(new Ip("victim2"));
		}
		return intrusions;
	}

	@Override
	public void update(Object o, Object arg) {
		if (arg instanceof AnomalousEvent) {
			// TODO Auto-generated method stub
		}
	}

}
