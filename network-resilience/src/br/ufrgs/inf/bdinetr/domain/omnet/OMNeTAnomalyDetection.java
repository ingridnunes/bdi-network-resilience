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
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.omnet.event.AnomalousEvent;

/**
 * @author Alberto Egon and Ingrid Nunes
 */
public class OMNeTAnomalyDetection extends OMNeTRouterComponent implements
		AnomalyDetection, Observer {

	private final Set<AnomalousEvent> events;

	public OMNeTAnomalyDetection(Router router) {
		super(router);
		this.events = new HashSet<>();
		EventBroker.getInstance().addObserver(this);
	}

	@Override
	public Set<Ip> detectIntrusion(Link link) {
		synchronized (events) {
			while (events.isEmpty()) {
				try {
					events.wait();
				} catch (InterruptedException e) {
					log.warn(e);
				}
			}

			Set<Ip> outliers = new HashSet<>();
			Iterator<AnomalousEvent> it = events.iterator();
			while (it.hasNext()) {
				AnomalousEvent event = it.next();
				outliers.add(event.getIp());
				it.remove();
			}
			return outliers;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof AnomalousEvent) {
			synchronized (events) {
				events.add((AnomalousEvent) arg);
				events.notifyAll();
			}
		}
	}

}
