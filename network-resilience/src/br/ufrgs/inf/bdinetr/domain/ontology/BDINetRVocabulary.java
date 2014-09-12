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
package br.ufrgs.inf.bdinetr.domain.ontology;

/**
 * @author Ingrid Nunes
 */
public interface BDINetRVocabulary extends BDI4JADEVocabulary {

	/** Concepts **/

	public static final String FLOW = "Flow";
	public static final String FLOW_DST_IP = "dstIp";
	public static final String FLOW_DST_PORT = "dstPort";
	public static final String FLOW_PROTOCOL = "protocol";
	public static final String FLOW_IP = "srcIp";
	public static final String FLOW_PORT = "srcPort";

	public static final String IP = "Ip";
	public static final String IP_ADDRESS = "address";

	public static final String LINK = "Link";
	public static final String LINK_ID = "id";

	/** Predicates **/

	public static final String UNARY_PREDICATE = "UnaryPredicate";
	public static final String UNARY_PREDICATE_CONCEPT = "concept";

	public static final String BINARY_PREDICATE = "BinaryPredicate";
	public static final String BINARY_PREDICATE_FIRST = "first";
	public static final String BINARY_PREDICATE_SECOND = "second";

	public static final String ANOMALOUS = "Anomalous";
	public static final String ATTACK_PREVENTED = "AttackPrevented";
	public static final String BENIGN = "Benign";
	public static final String FLOW_RATE_LIMITED = "FlowRateLimited";
	public static final String FULLY_OPERATIONAL = "FullyOperational";
	public static final String OVER_USAGE = "OverUsage";
	public static final String OVER_USAGE_CAUSE = "OverUsageCause";
	public static final String RATE_LIMITED = "RateLimited";
	public static final String REGULAR_USAGE = "RegularUsage";
	public static final String RESTRICTED = "Restricted";
	public static final String THREAT = "Threat";
	public static final String THREAT_RESPONDED = "ThreatResponded";

}
