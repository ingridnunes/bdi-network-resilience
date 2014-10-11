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

/**
 * @author Ingrid Nunes
 */
public abstract class AbstractRouterComponentFactory {

	public abstract AnomalyDetection createAnomalyDetection(Router router);

	public abstract Classifier createClassifier(Router router);

	public abstract FlowExporter createFlowExporter(Router router);

	public abstract LinkMonitor createLinkMonitor(Router router);

	public abstract RateLimiter createRateLimiter(Router router);

	public RouterComponent createRouterComponent(Role role, Router router) {
		if (Role.ANOMALY_DETECTION.equals(role)) {
			return createAnomalyDetection(router);
		}
		if (Role.CLASSIFIER.equals(role)) {
			return createClassifier(router);
		}
		if (Role.FLOW_EXPORTER.equals(role)) {
			return createFlowExporter(router);
		}
		if (Role.LINK_MONITOR.equals(role)) {
			return createLinkMonitor(router);
		}
		if (Role.RATE_LIMITER.equals(role)) {
			return createRateLimiter(router);
		}
		return null;
	}

}
