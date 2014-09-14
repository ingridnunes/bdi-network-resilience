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

import java.util.Date;

/**
 * @author Ingrid Nunes
 */
public interface BDINetRVocabulary {

	/** Concepts **/

	public static final String OBJECT_CONCEPT = "ObjectConcept";

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
	
	public static final String FLOW_EXPORT = "FlowExport";
	public static final String FLOW_EXPORT_IP = "ip";

	public static final String UNARY_PREDICATE = "UnaryPredicate";
	public static final String UNARY_PREDICATE_CONCEPT = "concept";

	public static final String BINARY_PREDICATE = "BinaryPredicate";
	public static final String BINARY_PREDICATE_FIRST = "first";
	public static final String BINARY_PREDICATE_SECOND = "second";

	public static final String ANOMALOUS = "Anomalous";
	public static final String ANOMALOUS_USAGE = "AnomalousUsage";
	public static final String ATTACK_PREVENTED = "AttackPrevented";
	public static final String BENIGN = "Benign";
	public static final String FLOW_RATE_LIMITED = "FlowRateLimited";
	public static final String IP_RATE_LIMITED = "IpRateLimited";
	public static final String LINK_RATE_LIMITED = "LinkRateLimited";
	public static final String OVER_USAGE = "OverUsage";
	public static final String RESTRICTED = "Restricted";
	public static final String THREAT = "Threat";

	/** Predicates **/

	public static final String OBJECT_PREDICATE = "ObjectPredicate";
	
	public static final String GOAL_PROPOSAL = "GoalProposal";
	public static final String GOAL_PROPOSAL_COST = "cost";

	public static final String GOAL_REQUEST = "GoalRequest";
	public static final String GOAL_REQUEST_BELIEF_GOAL = "beliefGoal";
	public static final String GOAL_REQUEST_PREDICATE = "predicate";
	public static final String GOAL_REQUEST_SUBSCRIBE = "subscribe";
	public static final String GOAL_REQUEST_VALUE = "value";
	
	public static final String GOAL_RESPONSE = "GoalResponse";
	public static final String GOAL_RESPONSE_PREDICATE = "predicate";
	public static final String GOAL_RESPONSE_TIMESTAMP = "timestamp";
	public static final String GOAL_RESPONSE_VALUE = "value";

}
