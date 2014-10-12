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

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import br.ufrgs.inf.bdinetr.domain.LimitLinkEvent;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.omnet.event.OverUsageEvent;

/**
 * @author Alberto Egon and Ingrid Nunes
 */
public class OMNeTLinkMonitor extends OMNeTRouterComponent implements
		LinkMonitor, Observer {

	private final Map<Link, Boolean> overUsageLinks;

	public OMNeTLinkMonitor(Router router) {
		super(router);
		this.overUsageLinks = new HashMap<>();
		EventBroker.getInstance().addObserver(this);
	}

	@Override
	public Set<Link> getLinks() {
		return overUsageLinks.keySet();
	}

	@Override
	public boolean isOverUsage(Link link) {
		Boolean overUsage = this.overUsageLinks.get(link);
		if (overUsage == null)
			overUsage = false;
		return overUsage;
	}

	@Override
	public void removeLink(Link link) {
		this.overUsageLinks.remove(link);
	}

	@Override
	public void setOverUsage(Link link, boolean overUsage) {
		this.overUsageLinks.put(link, overUsage);
		setChanged();
		notifyObservers(link);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof OverUsageEvent) {
			setOverUsage(((OverUsageEvent) arg).getLink(), true);
		} else if (arg instanceof LimitLinkEvent) {
			LimitLinkEvent event = (LimitLinkEvent) arg;
			if (this.isOverUsage(event.getLink())) {
				this.setOverUsage(event.getLink(), false);
			}
		}
	}

}
