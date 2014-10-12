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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufrgs.inf.bdinetr.agent.RouterAgent;
import br.ufrgs.inf.bdinetr.domain.LimitLinkEvent;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.Router;

/**
 * @author Ingrid Nunes
 */
public class Network implements Observer {

	private final Log log;
	private final Map<Router, RouterAgent> routerAgents;

	public Network() {
		this.log = LogFactory.getLog(this.getClass());
		this.routerAgents = new HashMap<>();
	}

	public void addRouter(Router router) {
		this.routerAgents.put(router, new RouterAgent(router));
		if (router.hasRole(Role.RATE_LIMITER)) {
			((RateLimiter) router.getRole(Role.RATE_LIMITER)).addObserver(this);
		}
	}

	public RouterAgent getAgent(Router router) {
		return routerAgents.get(router);
	}

	public Collection<RouterAgent> getRouterAgents() {
		return routerAgents.values();
	}

	public Set<Router> getRouters() {
		return routerAgents.keySet();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof LimitLinkEvent) {
			LimitLinkEvent event = (LimitLinkEvent) arg;
			for (Router router : getRouters()) {
				if (router.hasRole(Role.LINK_MONITOR)) {
					LinkMonitor lm = (LinkMonitor) router
							.getRole(Role.LINK_MONITOR);
					if (lm.isOverUsage(event.getLink())) {
						log.info("Updating link monitors...");
						lm.setOverUsage(event.getLink(), false);
					}
				}
			}
		}
	}

}
