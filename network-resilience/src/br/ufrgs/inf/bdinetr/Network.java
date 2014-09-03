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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bdi4jade.examples.BDI4JADEExamplesPanel;
import br.ufrgs.inf.bdinetr.domain.IpAddress;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.PReSETRole.RoleType;
import br.ufrgs.inf.bdinetr.domain.PReSETRouter;

/**
 * @author Ingrid Nunes
 */
public class Network {

	class LinkUsageUpdater extends TimerTask {
		private static final double OVER_USAGE_PROBABILITY = 0.3;

		@Override
		public void run() {
			Map<Link, Boolean> overUsage = new HashMap<>();
			Random random = new Random(System.currentTimeMillis());
			for (Link link : NETWORK.getLinks()) {
				double d = random.nextDouble();
				overUsage.put(link, d < OVER_USAGE_PROBABILITY);
			}
			log.info("Updating link usage");
			for (PReSETRouter router : NETWORK.getRouters()) {
				if (router.hasRole(RoleType.LINK_MONITOR)) {
					LinkMonitor lm = (LinkMonitor) router
							.getRole(RoleType.LINK_MONITOR);
					for (Link link : overUsage.keySet()) {
						lm.setOverUsage(link, overUsage.get(link));
					}
				}
			}
		}
	}

	public static final Network NETWORK;

	static {
		NETWORK = new Network();
		PReSETRouter firewall = new PReSETRouter(new IpAddress("Firewall"),
				RoleType.RATE_LIMITER.getId());
		NETWORK.addRouter(firewall);
		PReSETRouter linkMonitor = new PReSETRouter(new IpAddress(
				"Rate Limiter"), RoleType.LINK_MONITOR.getId());
		NETWORK.addRouter(linkMonitor);

		NETWORK.addLink(new Link("LINK_01"));
		NETWORK.addLink(new Link("LINK_02"));
		NETWORK.addLink(new Link("LINK_03"));
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
		int interval = 10 * 1000;
		this.timer.schedule(new LinkUsageUpdater(), interval, interval);
	}

}
