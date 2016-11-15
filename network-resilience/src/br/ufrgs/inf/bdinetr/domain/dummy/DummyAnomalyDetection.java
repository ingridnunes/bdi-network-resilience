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

import java.util.HashSet;
import java.util.Set;

import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.Router;

/**
 * @author Ingrid Nunes
 */
public class DummyAnomalyDetection extends AbstractRouterComponent implements AnomalyDetection {
	
	public static final long DELAY = 10;

	private boolean first;
	private boolean mode;

	public DummyAnomalyDetection(Router router) {
		super(router);
		this.first = true;
		this.mode = true;
	}

	@Override
	public Set<Ip> detectIntrusion(Link link) {
		Set<Ip> intrusions = new HashSet<>();
		if (link.getId().equals("AFFECTED_LINK")) {
			if (mode) {
				intrusions.add(new Ip("victim1"));
				intrusions.add(new Ip("victim2"));
			} else {
				if (first) {
					intrusions.add(new Ip("victim1"));
					first = false;
				} else {
					intrusions.add(new Ip("victim2"));
				}
			}
		}
		delay(DELAY);
		return intrusions;
	}

}
