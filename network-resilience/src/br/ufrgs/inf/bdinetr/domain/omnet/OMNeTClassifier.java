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

import br.ufrgs.inf.bdinetr.domain.Classifier;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.omnet.event.ThreatEvent;

/**
 * @author Alberto Egon and Ingrid Nunes
 */
public class OMNeTClassifier extends OMNeTRouterComponent implements
		Classifier, Observer {

	private final Set<ThreatEvent> events;

	public OMNeTClassifier(Router router) {
		super(router);
		this.events = new HashSet<>();
		EventBroker.getInstance().addObserver(this);
	}

	@Override
	public Set<Flow> classifyFlows(Ip ip) {
		synchronized (events) {
			while (events.isEmpty()) {
				try {
					events.wait();
				} catch (InterruptedException e) {
					log.warn(e);
				}
			}

			Set<Flow> flows = new HashSet<>();
			Iterator<ThreatEvent> it = events.iterator();
			while (it.hasNext()) {
				ThreatEvent event = it.next();
				flows.add(event.getFlow());
				it.remove();
			}
			return flows;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof ThreatEvent) {
			synchronized (events) {
				events.add((ThreatEvent) arg);
				events.notifyAll();
			}
		}
	}

}
