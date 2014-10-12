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

import br.ufrgs.inf.bdinetr.domain.AbstractRouterComponentFactory;
import br.ufrgs.inf.bdinetr.domain.AnomalyDetection;
import br.ufrgs.inf.bdinetr.domain.Classifier;
import br.ufrgs.inf.bdinetr.domain.FlowExporter;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.RateLimiter;
import br.ufrgs.inf.bdinetr.domain.Router;

/**
 * @author Ingrid Nunes
 */
public class OMNeTRouterComponentFactory extends AbstractRouterComponentFactory {

	@Override
	public AnomalyDetection createAnomalyDetection(Router router) {
		return new OMNeTAnomalyDetection(router);
	}

	@Override
	public Classifier createClassifier(Router router) {
		return new OMNeTClassifier(router);
	}

	@Override
	public FlowExporter createFlowExporter(Router router) {
		return new OMNeTFlowExporter(router);
	}

	@Override
	public LinkMonitor createLinkMonitor(Router router) {
		return new OMNeTLinkMonitor(router);
	}

	@Override
	public RateLimiter createRateLimiter(Router router) {
		return new OMNeTRateLimiter(router);
	}

}
