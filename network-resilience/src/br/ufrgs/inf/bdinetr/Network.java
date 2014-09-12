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
package br.ufrgs.inf.bdinetr;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.Observer;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.RateLimiter.LimitLinkEvent;
import br.ufrgs.inf.bdinetr.domain.Role;

/**
 * @author Ingrid Nunes
 */
public class Network implements Observer {

	private final Set<Link> affectedLinks;
	private final Set<Link> links;
	private final Log log;
	private final Set<Router> routers;

	public Network(Set<Router> routers, Set<Link> links,
			Set<Link> affectedLinks) {
		this.log = LogFactory.getLog(this.getClass());
		this.routers = routers;
		this.links = links;
		this.affectedLinks = affectedLinks;

		for (Router router : routers) {
			if (router.hasRole(Role.RATE_LIMITER)) {
				((RateLimiter) router.getRole(Role.RATE_LIMITER))
						.attachObserver(this);
			}
		}
	}

	public void addLink(Link link) {
		this.links.add(link);
	}

	public void addRouter(Router router) {
		this.routers.add(router);
	}

	public Set<Link> getLinks() {
		return links;
	}

	public Set<Router> getRouters() {
		return routers;
	}

	/**
	 * Creates and shows a GUI whose content pane is an
	 * {@link BDI4JADEExamplesPanel}.
	 */
	public void run() {
		log.info("Updating link usage");
		for (Link link : affectedLinks) {
			for (Router router : routers) {
				if (router.hasRole(Role.LINK_MONITOR)) {
					LinkMonitor lm = (LinkMonitor) router
							.getRole(Role.LINK_MONITOR);
					lm.setOverUsage(link, true);
				}
			}
		}
	}

	@Override
	public void update(Object o, Object arg) {
		if (arg instanceof LimitLinkEvent) {
			LimitLinkEvent event = (LimitLinkEvent) arg;
			for (Router router : routers) {
				if (router.hasRole(Role.LINK_MONITOR)) {
					LinkMonitor lm = (LinkMonitor) router
							.getRole(Role.LINK_MONITOR);
					if (lm.isOverUsage(event.getLink())) {
						lm.setOverUsage(event.getLink(), false);
					}
				}
			}
		}
	}

}
