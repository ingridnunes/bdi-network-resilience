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

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.message.GoalProposal;
import br.ufrgs.inf.bdinetr.domain.message.GoalRequest;
import br.ufrgs.inf.bdinetr.domain.message.GoalResponse;
import br.ufrgs.inf.bdinetr.domain.predicate.Anomalous;
import br.ufrgs.inf.bdinetr.domain.predicate.AnomalousUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.predicate.Benign;
import br.ufrgs.inf.bdinetr.domain.predicate.BinaryPredicate;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowExport;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.IpRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.LinkRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.OverUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.Restricted;
import br.ufrgs.inf.bdinetr.domain.predicate.Threat;
import br.ufrgs.inf.bdinetr.domain.predicate.UnaryPredicate;

/**
 * @author Ingrid Nunes
 */
public class BDINetROntology extends Ontology implements BDINetRVocabulary {

	/**
	 * <p>
	 * UID generated.
	 * </p>
	 */
	private static final long serialVersionUID = 6113260660006024268L;

	public static final String ONTOLOGY_NAME = "BDINetROntology";
	private static Ontology theInstance = new BDINetROntology();

	/**
	 * This method grants access to the unique instance of the ontology.
	 * 
	 * @return An <code>Ontology</code> object, containing the concepts of the
	 *         ontology.
	 */
	public static Ontology getInstance() {
		return theInstance;
	}

	public BDINetROntology() {
		super(ONTOLOGY_NAME, new Ontology[] { BasicOntology.getInstance() }, new Introspector());

		try {
			add(new ConceptSchema(OBJECT_CONCEPT), Object.class);
			add(new PredicateSchema(OBJECT_PREDICATE), Object.class);
			
			add(new ConceptSchema(FLOW), Flow.class);
			add(new ConceptSchema(IP), Ip.class);
			add(new ConceptSchema(LINK), Link.class);

			ConceptSchema cs = (ConceptSchema) getSchema(FLOW);
			cs.add(FLOW_DST_IP, (ConceptSchema) getSchema(IP));
			cs.add(FLOW_DST_PORT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(FLOW_PROTOCOL, (PrimitiveSchema) getSchema(BasicOntology.STRING));
			cs.add(FLOW_IP, (ConceptSchema) getSchema(IP));
			cs.add(FLOW_PORT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.addSuperSchema((ConceptSchema) getSchema(OBJECT_CONCEPT));

			cs = (ConceptSchema) getSchema(IP);
			cs.add(IP_ADDRESS, (PrimitiveSchema) getSchema(BasicOntology.STRING));
			cs.addSuperSchema((ConceptSchema) getSchema(OBJECT_CONCEPT));

			cs = (ConceptSchema) getSchema(LINK);
			cs.add(LINK_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
			cs.addSuperSchema((ConceptSchema) getSchema(OBJECT_CONCEPT));

			add(new ConceptSchema(FLOW_EXPORT), FlowExport.class);

			cs = (ConceptSchema) getSchema(FLOW_EXPORT);
			cs.add(FLOW_EXPORT_IP, (ConceptSchema) getSchema(IP));
			cs.addSuperSchema((ConceptSchema) getSchema(OBJECT_CONCEPT));
			
			add(new ConceptSchema(UNARY_PREDICATE), UnaryPredicate.class);
			add(new ConceptSchema(BINARY_PREDICATE), BinaryPredicate.class);
			
			add(new ConceptSchema(ANOMALOUS), Anomalous.class);
			add(new ConceptSchema(ANOMALOUS_USAGE), AnomalousUsage.class);
			add(new ConceptSchema(ATTACK_PREVENTED), AttackPrevented.class);
			add(new ConceptSchema(BENIGN), Benign.class);
			add(new ConceptSchema(FLOW_RATE_LIMITED), FlowRateLimited.class);
			add(new ConceptSchema(IP_RATE_LIMITED), IpRateLimited.class);
			add(new ConceptSchema(LINK_RATE_LIMITED), LinkRateLimited.class);
			add(new ConceptSchema(OVER_USAGE), OverUsage.class);
			add(new ConceptSchema(RESTRICTED), Restricted.class);
			add(new ConceptSchema(THREAT), Threat.class);

			cs = (ConceptSchema) getSchema(UNARY_PREDICATE);
			cs.add(UNARY_PREDICATE_CONCEPT, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.addSuperSchema((ConceptSchema)getSchema(OBJECT_CONCEPT));
			
			((ConceptSchema) getSchema(ANOMALOUS)).addSuperSchema(cs);
			((ConceptSchema) getSchema(ANOMALOUS_USAGE)).addSuperSchema(cs);
			((ConceptSchema) getSchema(ATTACK_PREVENTED)).addSuperSchema(cs);
			((ConceptSchema) getSchema(BENIGN)).addSuperSchema(cs);
			((ConceptSchema) getSchema(FLOW_RATE_LIMITED)).addSuperSchema(cs);
			((ConceptSchema) getSchema(IP_RATE_LIMITED)).addSuperSchema(cs);
			((ConceptSchema) getSchema(LINK_RATE_LIMITED)).addSuperSchema(cs);
			((ConceptSchema) getSchema(OVER_USAGE)).addSuperSchema(cs);
			((ConceptSchema) getSchema(RESTRICTED)).addSuperSchema(cs);
			((ConceptSchema) getSchema(THREAT)).addSuperSchema(cs);
			
			cs = (ConceptSchema) getSchema(BINARY_PREDICATE);
			cs.add(BINARY_PREDICATE_FIRST, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.add(BINARY_PREDICATE_SECOND, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.addSuperSchema((ConceptSchema)getSchema(OBJECT_CONCEPT));
			
			add(new PredicateSchema(GOAL_PROPOSAL), GoalProposal.class);
			add(new PredicateSchema(GOAL_REQUEST), GoalRequest.class);
			add(new PredicateSchema(GOAL_RESPONSE), GoalResponse.class);
			
			PredicateSchema ps = (PredicateSchema) getSchema(GOAL_PROPOSAL);
			ps.add(GOAL_PROPOSAL_COST, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
			ps.addSuperSchema((PredicateSchema)getSchema(OBJECT_PREDICATE));
			
			ps = (PredicateSchema) getSchema(GOAL_REQUEST);
			ps.add(GOAL_REQUEST_BELIEF_GOAL, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
			ps.add(GOAL_REQUEST_PREDICATE, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			ps.add(GOAL_REQUEST_SUBSCRIBE, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
			ps.add(GOAL_REQUEST_VALUE, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
			ps.addSuperSchema((PredicateSchema)getSchema(OBJECT_PREDICATE));

			ps = (PredicateSchema) getSchema(GOAL_RESPONSE);
			ps.add(GOAL_RESPONSE_PREDICATE, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			ps.add(GOAL_RESPONSE_TIMESTAMP, (PrimitiveSchema) getSchema(BasicOntology.DATE));
			ps.add(GOAL_RESPONSE_VALUE, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
			ps.addSuperSchema((PredicateSchema)getSchema(OBJECT_PREDICATE));
		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}

}
