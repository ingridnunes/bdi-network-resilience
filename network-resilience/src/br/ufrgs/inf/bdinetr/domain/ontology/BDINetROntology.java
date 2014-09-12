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
import jade.content.schema.PrimitiveSchema;
import br.ufrgs.inf.bdinetr.domain.Flow;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.predicate.Anomalous;
import br.ufrgs.inf.bdinetr.domain.predicate.AttackPrevented;
import br.ufrgs.inf.bdinetr.domain.predicate.Benign;
import br.ufrgs.inf.bdinetr.domain.predicate.BinaryPredicate;
import br.ufrgs.inf.bdinetr.domain.predicate.FlowRateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.FullyOperational;
import br.ufrgs.inf.bdinetr.domain.predicate.OverUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.OverUsageCause;
import br.ufrgs.inf.bdinetr.domain.predicate.RateLimited;
import br.ufrgs.inf.bdinetr.domain.predicate.RegularUsage;
import br.ufrgs.inf.bdinetr.domain.predicate.Restricted;
import br.ufrgs.inf.bdinetr.domain.predicate.Threat;
import br.ufrgs.inf.bdinetr.domain.predicate.ThreatResponded;
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
		super(ONTOLOGY_NAME, new Ontology[] { BDI4JADEOntology
				.getInstance() }, new Introspector());

		try {
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

			add(new ConceptSchema(UNARY_PREDICATE), UnaryPredicate.class);
			add(new ConceptSchema(BINARY_PREDICATE), BinaryPredicate.class);
			
			add(new ConceptSchema(ANOMALOUS), Anomalous.class);
			add(new ConceptSchema(ATTACK_PREVENTED), AttackPrevented.class);
			add(new ConceptSchema(BENIGN), Benign.class);
			add(new ConceptSchema(FLOW_RATE_LIMITED), FlowRateLimited.class);
			add(new ConceptSchema(FULLY_OPERATIONAL), FullyOperational.class);
			add(new ConceptSchema(OVER_USAGE), OverUsage.class);
			add(new ConceptSchema(OVER_USAGE_CAUSE), OverUsageCause.class);
			add(new ConceptSchema(RATE_LIMITED), RateLimited.class);
			add(new ConceptSchema(REGULAR_USAGE), RegularUsage.class);
			add(new ConceptSchema(RESTRICTED), Restricted.class);
			add(new ConceptSchema(THREAT), Threat.class);
			add(new ConceptSchema(THREAT_RESPONDED), ThreatResponded.class);

			ConceptSchema ps = (ConceptSchema) getSchema(UNARY_PREDICATE);
			ps.add(UNARY_PREDICATE_CONCEPT, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			ps.addSuperSchema((ConceptSchema)getSchema(OBJECT_CONCEPT));
			
			((ConceptSchema) getSchema(ANOMALOUS)).addSuperSchema(ps);
			((ConceptSchema) getSchema(ATTACK_PREVENTED)).addSuperSchema(ps);
			((ConceptSchema) getSchema(BENIGN)).addSuperSchema(ps);
			((ConceptSchema) getSchema(FLOW_RATE_LIMITED)).addSuperSchema(ps);
			((ConceptSchema) getSchema(FULLY_OPERATIONAL)).addSuperSchema(ps);
			((ConceptSchema) getSchema(OVER_USAGE)).addSuperSchema(ps);
			((ConceptSchema) getSchema(RATE_LIMITED)).addSuperSchema(ps);
			((ConceptSchema) getSchema(REGULAR_USAGE)).addSuperSchema(ps);
			((ConceptSchema) getSchema(RESTRICTED)).addSuperSchema(ps);
			((ConceptSchema) getSchema(THREAT)).addSuperSchema(ps);
			((ConceptSchema) getSchema(THREAT_RESPONDED)).addSuperSchema(ps);
			
			ps = (ConceptSchema) getSchema(BINARY_PREDICATE);
			ps.add(BINARY_PREDICATE_FIRST, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			ps.add(BINARY_PREDICATE_SECOND, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			ps.addSuperSchema((ConceptSchema)getSchema(OBJECT_CONCEPT));
			
			((ConceptSchema) getSchema(OVER_USAGE_CAUSE)).addSuperSchema(ps);
		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}

}
