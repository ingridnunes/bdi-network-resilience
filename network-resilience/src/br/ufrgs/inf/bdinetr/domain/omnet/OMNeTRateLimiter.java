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

import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.LimitLinkEvent;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.Router;

/**
 * @author Alberto Egon and Ingrid Nunes
 */
public class OMNeTRateLimiter extends OMNeTRouterComponent implements
		RateLimiter {

	public OMNeTRateLimiter(Router router) {
		super(router);
	}

	@Override
	public void limitFlow(Flow flow, double rate) {
		Object[] params = new Object[5];
		params[0] = "Inet.sas1.core0.rateLimiter";
		params[1] = flow.getSrcIp().getAddress();
		params[2] = flow.getDstIp().getAddress();
		params[3] = flow.getProtocol();
		params[4] = rate;
		invoke("limitflow", params);
	}

	@Override
	public void limitIp(Ip ip, double rate) {
		Object[] params = new Object[3];
		params[0] = "Inet.sas1.core0.rateLimiter";
		params[1] = ip.getAddress();
		params[2] = rate;
		invoke("limitip", params);
	}

	@Override
	public void limitLink(Link link, double rate) {
		Object[] params = new Object[3];
		params[0] = "Inet.sas1.core0.rateLimiter";
		params[1] = link.getId();
		params[2] = rate;
		invoke("limitlink", params);

		setChanged();
		notifyObservers(new LimitLinkEvent(link));
	}

	@Override
	public void unlimitFlow(Flow flow) {
		// TODO unsupported by OMNeT
	}

	@Override
	public void unlimitIp(Ip ip) {
		// TODO unsupported by OMNeT
	}

	@Override
	public void unlimitLink(Link link) {
		// TODO unsupported by OMNeT
	}

}
