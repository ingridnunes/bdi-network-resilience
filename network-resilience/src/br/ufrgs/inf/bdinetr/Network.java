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

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.Observer;
import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;
import br.ufrgs.inf.bdinetr.domain.PReSETRouter;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.RateLimiter.LimitLinkEvent;

/**
 * @author Ingrid Nunes
 */
public class Network implements Observer {

	public static final Link AFFECTED_LINK;
	public static final Network NETWORK;

	static {
		NETWORK = new Network();
		NETWORK.addRouter(new PReSETRouter(new Ip("Router"),
				RoleType.RATE_LIMITER.getId() | RoleType.CLASSIFIER.getId()
						| RoleType.ANOMALY_DETECTION.getId()
						| RoleType.LINK_MONITOR.getId()));

		AFFECTED_LINK = new Link("AFFECTED_LINK");
		NETWORK.addLink(AFFECTED_LINK);
		NETWORK.addLink(new Link("LINK_01"));
		NETWORK.addLink(new Link("LINK_02"));
	}

	private final Set<Link> links;
	private final Log log;
	private final Set<PReSETRouter> router;
	private Timer timer;

	public Network() {
		this.log = LogFactory.getLog(this.getClass());
		this.router = new HashSet<>();
		this.links = new HashSet<>();
		this.timer = new Timer();

		for (PReSETRouter router : NETWORK.getRouters()) {
			if (router.hasRole(RoleType.RATE_LIMITER)) {
				((RateLimiter) router.getRole(RoleType.RATE_LIMITER))
						.attachObserver(this);
			}
		}
	}

	public void addLink(Link link) {
		this.links.add(link);
	}

	public void addRouter(PReSETRouter router) {
		this.router.add(router);
	}

	public Set<Link> getLinks() {
		return links;
	}

	public Set<PReSETRouter> getRouters() {
		return router;
	}

	/**
	 * Creates and shows a GUI whose content pane is an
	 * {@link BDI4JADEExamplesPanel}.
	 */
	public void run() {
		log.info("Updating link usage");
		for (PReSETRouter router : NETWORK.getRouters()) {
			if (router.hasRole(RoleType.LINK_MONITOR)) {
				LinkMonitor lm = (LinkMonitor) router
						.getRole(RoleType.LINK_MONITOR);
				lm.setOverUsage(AFFECTED_LINK, true);
			}
		}
	}

	@Override
	public void update(Object o, Object arg) {
		if (arg instanceof LimitLinkEvent) {
			LimitLinkEvent event = (LimitLinkEvent) arg;
			for (PReSETRouter router : NETWORK.getRouters()) {
				if (router.hasRole(RoleType.LINK_MONITOR)) {
					LinkMonitor lm = (LinkMonitor) router
							.getRole(RoleType.LINK_MONITOR);
					if (lm.isOverUsage(event.getLink())) {
						lm.setOverUsage(event.getLink(), false);
					}
				}
			}
		}
	}

}
