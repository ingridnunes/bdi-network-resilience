package br.ufrgs.inf.bdinetr.domain.ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;
import bdi4jade.belief.AbstractBelief;
import bdi4jade.belief.AbstractBeliefSet;
import bdi4jade.belief.Belief;
import bdi4jade.belief.BeliefSet;
import bdi4jade.belief.PersistentBelief;
import bdi4jade.belief.PropositionalBelief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientBeliefSet;
import bdi4jade.belief.TransientPropositionalBelief;
import bdi4jade.goal.BeliefGoal;
import bdi4jade.goal.BeliefPresentGoal;
import bdi4jade.goal.BeliefSetHasValueGoal;
import bdi4jade.goal.BeliefValueGoal;
import bdi4jade.goal.PropositionalBeliefValueGoal;

public class BDI4JADEOntology extends Ontology implements BDI4JADEVocabulary {

	/**
	 * <p>
	 * UID generated.
	 * </p>
	 */
	private static final long serialVersionUID = 6113260660006024268L;

	public static final String ONTOLOGY_NAME = "BDI4JADEOntology";
	private static Ontology theInstance = new BDI4JADEOntology();

	/**
	 * This method grants access to the unique instance of the ontology.
	 * 
	 * @return An <code>Ontology</code> object, containing the concepts of the
	 *         ontology.
	 */
	public static Ontology getInstance() {
		return theInstance;
	}

	public BDI4JADEOntology() {
		super(ONTOLOGY_NAME, new Ontology[] { BasicOntology.getInstance() },
				new Introspector());

		try {
			add(new ConceptSchema(OBJECT_CONCEPT), Object.class);
			add(new PredicateSchema(OBJECT_PREDICATE), Object.class);

			// add(new EnumerationSchema(DATEFORMAT), DateFormat.class);
			// cs.add(CALENDARUSERSERVICE_EVENTS, (PredicateSchema)
			// getSchema(CALENDAREVENT), 0, ObjectSchema.UNLIMITED,
			// BasicOntology.SET);
			
			add(new PredicateSchema(BELIEF), Belief.class);
			add(new PredicateSchema(BELIEF_SET), BeliefSet.class);
			add(new PredicateSchema(PROPOSITIONAL_BELIEF), PropositionalBelief.class);

			add(new PredicateSchema(ABSTRACT_BELIEF), AbstractBelief.class);
			add(new PredicateSchema(ABSTRACT_BELIEF_SET), AbstractBeliefSet.class);
			add(new PredicateSchema(TRANSIENT_BELIEF), TransientBelief.class);
			add(new PredicateSchema(TRANSIENT_BELIEF_SET), TransientBeliefSet.class);
			add(new PredicateSchema(PERSISTENT_BELIEF), PersistentBelief.class);
			add(new PredicateSchema(TRANSIENT_PROPOSITIONAL_BELIEF), TransientPropositionalBelief.class);

			PredicateSchema cs = (PredicateSchema) getSchema(BELIEF);
			cs.add(BELIEF_NAME, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.add(BELIEF_VALUE, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.addSuperSchema((PredicateSchema) getSchema(OBJECT_PREDICATE));

			cs = (PredicateSchema) getSchema(BELIEF_SET);
			cs.addSuperSchema((PredicateSchema) getSchema(BELIEF));
			
			cs = (PredicateSchema) getSchema(PROPOSITIONAL_BELIEF);
			cs.add(BELIEF_NAME, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.add(BELIEF_VALUE, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
			cs.addSuperSchema((PredicateSchema) getSchema(OBJECT_PREDICATE));
			
			cs = (PredicateSchema) getSchema(ABSTRACT_BELIEF);
			cs.addSuperSchema((PredicateSchema) getSchema(BELIEF));
			cs = (PredicateSchema) getSchema(PERSISTENT_BELIEF);
			cs.addSuperSchema((PredicateSchema) getSchema(ABSTRACT_BELIEF));
			cs = (PredicateSchema) getSchema(TRANSIENT_BELIEF);
			cs.addSuperSchema((PredicateSchema) getSchema(ABSTRACT_BELIEF));
			
			cs = (PredicateSchema) getSchema(ABSTRACT_BELIEF_SET);
			cs.addSuperSchema((PredicateSchema) getSchema(ABSTRACT_BELIEF));
			cs.addSuperSchema((PredicateSchema) getSchema(BELIEF_SET));
			cs = (PredicateSchema) getSchema(TRANSIENT_BELIEF_SET);
			cs.addSuperSchema((PredicateSchema) getSchema(ABSTRACT_BELIEF_SET));
			
			cs = (PredicateSchema) getSchema(TRANSIENT_PROPOSITIONAL_BELIEF);
			cs.addSuperSchema((PredicateSchema) getSchema(PROPOSITIONAL_BELIEF));
			cs.addSuperSchema((PredicateSchema) getSchema(TRANSIENT_BELIEF));

			add(new PredicateSchema(BELIEF_GOAL), BeliefGoal.class);
			add(new PredicateSchema(BELIEF_PRESENT_GOAL), BeliefPresentGoal.class);
			add(new PredicateSchema(BELIEF_SET_HAS_VALUE_GOAL), BeliefSetHasValueGoal.class);
			add(new PredicateSchema(BELIEF_VAUE_GOAL), BeliefValueGoal.class);
			add(new PredicateSchema(PROPOSITIONAL_BELIEF_VAUE_GOAL), PropositionalBeliefValueGoal.class);

			cs = (PredicateSchema) getSchema(BELIEF_GOAL);
			cs.add(BELIEF_GOAL_BELIEF_NAME, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.addSuperSchema((PredicateSchema) getSchema(OBJECT_PREDICATE));

			cs = (PredicateSchema) getSchema(BELIEF_PRESENT_GOAL);
			cs.addSuperSchema((PredicateSchema) getSchema(BELIEF_GOAL));

			cs = (PredicateSchema) getSchema(BELIEF_SET_HAS_VALUE_GOAL);
			cs.add(BELIEF_SET_HAS_VALUE_GOAL_VALUE, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.addSuperSchema((PredicateSchema) getSchema(BELIEF_GOAL));

			cs = (PredicateSchema) getSchema(BELIEF_VAUE_GOAL);
			cs.add(BELIEF_VAUE_GOAL_VALUE, (ConceptSchema) getSchema(OBJECT_CONCEPT));
			cs.addSuperSchema((PredicateSchema) getSchema(BELIEF_GOAL));

			cs = (PredicateSchema) getSchema(PROPOSITIONAL_BELIEF_VAUE_GOAL);
			cs.add(PROPOSITIONAL_BELIEF_VAUE_GOAL_VALUE, (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));
			cs.addSuperSchema((PredicateSchema) getSchema(BELIEF_GOAL));
		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}

}
