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
import java.util.Set;

/**
 * event at:
 * 
 * "load" put: (factory/event create: #( "value_name" "value_index" ));
 * 
 * --> value_name: ??? --> value_index: id do link
 * 
 * @author Ingrid Nunes
 */
public class LinkMonitor extends RouterComponent {

	private final Map<Link, Boolean> overUsageLinks;

	public LinkMonitor(Router router) {
		super(router);
		this.overUsageLinks = new HashMap<>();
	}

	public Set<Link> getLinks() {
		return overUsageLinks.keySet();
	}

	public boolean isOverUsage(Link link) {
		Boolean overUsage = this.overUsageLinks.get(link);
		if (overUsage == null)
			overUsage = false;
		return overUsage;
	}

	public void setOverUsage(Link link, boolean overUsage) {
		this.overUsageLinks.put(link, overUsage);
		notifyObservers(link);
	}

}
