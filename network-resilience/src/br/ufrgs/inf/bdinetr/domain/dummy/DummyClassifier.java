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

import br.ufrgs.inf.bdinetr.domain.Classifier;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Router;

/**
 * @author Ingrid Nunes
 */
public class DummyClassifier extends AbstractRouterComponent implements Classifier {
	
	public static final long DELAY = 30;

	public DummyClassifier(Router router) {
		super(router);
	}

	@Override
	public Set<Flow> classifyFlows(Ip ip) {
		Set<Flow> flows = new HashSet<>();
		if (ip.getAddress().equals("victim1")) {
			flows.add(new Flow(new Ip("DDoS1"), 80, new Ip("victim1"), 80,
					"http"));
			flows.add(new Flow(new Ip("DDoS2"), 80, new Ip("victim1"), 80,
					"http"));
		} else if (ip.getAddress().equals("victim2")) {
			flows.add(new Flow(new Ip("DDoS3"), 80, new Ip("victim2"), 80,
					"http"));
		}
		delay(DELAY);
		return flows;
	}

}
