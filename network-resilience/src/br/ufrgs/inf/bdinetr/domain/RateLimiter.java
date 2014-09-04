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

import java.util.HashMap;
import java.util.Map;

/**
 * ratelimiter
 * 
 * addOperation: "name:index:rate:" remoteName: "limitlink";
 * 
 * addOperation: "name:ip:rate:" remoteName: "limitip";
 * 
 * addOperation: "name:source:destination:protocol:rate:" remoteName:
 * "limitflow".
 * 
 * @author Ingrid Nunes
 */
public class RateLimiter extends PReSETRole {

	public class LimitLinkEvent {
		private Link link;

		public LimitLinkEvent(Link link) {
			this.link = link;
		}

		public Link getLink() {
			return link;
		}
	}

	private final Map<Flow, Double> rateLimitedflows;
	private final Map<Ip, Double> rateLimitedIps;
	private final Map<Link, Double> rateLimitedLinks;

	public RateLimiter(PReSETRouter router) {
		super(router);
		this.rateLimitedLinks = new HashMap<>();
		this.rateLimitedIps = new HashMap<>();
		this.rateLimitedflows = new HashMap<>();
	}

	public void limitFlow(Flow flow, double rate) {
		this.rateLimitedflows.put(flow, rate);
	}

	public void limitIp(Ip ip, double rate) {
		this.rateLimitedIps.put(ip, rate);
	}

	public void limitLink(Link link, double rate) {
		this.rateLimitedLinks.put(link, rate);
		notifyObservers(new LimitLinkEvent(link));
	}

	public void unlimitFlow(Flow flow) {
		this.rateLimitedflows.remove(flow);
	}

	public void unlimitIp(Ip ip) {
		this.rateLimitedIps.remove(ip);
	}

	public void unlimitLink(Link link) {
		this.rateLimitedLinks.remove(link);
	}

}
