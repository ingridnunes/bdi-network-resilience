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
package br.ufrgs.inf.bdinetr.domain.dummy;

import java.util.HashMap;
import java.util.Map;

import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.LimitLinkEvent;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.Observable;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.Router;

/**
 * @author Ingrid Nunes
 */
public class DummyRateLimiter extends AbstractRouterComponent implements
		RateLimiter, Observable {

	private final Map<Flow, Integer> rateLimitedflows;
	private final Map<Ip, Integer> rateLimitedIps;
	private final Map<Link, Integer> rateLimitedLinks;

	public DummyRateLimiter(Router router) {
		super(router);
		this.rateLimitedLinks = new HashMap<>();
		this.rateLimitedIps = new HashMap<>();
		this.rateLimitedflows = new HashMap<>();
	}

	@Override
	public void limitFlow(Flow flow, int rate) {
		this.rateLimitedflows.put(flow, rate);
	}

	@Override
	public void limitIp(Ip ip, int rate) {
		this.rateLimitedIps.put(ip, rate);
	}

	@Override
	public void limitLink(Link link, int rate) {
		this.rateLimitedLinks.put(link, rate);
		setChanged();
		notifyObservers(new LimitLinkEvent(link));
	}

	@Override
	public void unlimitFlow(Flow flow) {
		this.rateLimitedflows.remove(flow);
	}

	@Override
	public void unlimitIp(Ip ip) {
		this.rateLimitedIps.remove(ip);
	}

	@Override
	public void unlimitLink(Link link) {
		this.rateLimitedLinks.remove(link);
	}

}
