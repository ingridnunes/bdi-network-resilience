package br.ufrgs.inf.bdinetr.domain.ontology;

public interface BDI4JADEVocabulary {

	/** Concepts **/

	public static final String OBJECT_CONCEPT = "ObjectConcept";
	public static final String OBJECT_PREDICATE = "ObjectPredicate";

	public static final String BELIEF = "Belief";
	public static final String BELIEF_NAME = "name";
	public static final String BELIEF_VALUE = "value";
	public static final String BELIEF_SET = "BeliefSet";
	public static final String PROPOSITIONAL_BELIEF = "PropositionalBelief";

	public static final String ABSTRACT_BELIEF = "AbstractBelief";
	public static final String ABSTRACT_BELIEF_SET = "AbstractBeliefSet";
	public static final String TRANSIENT_BELIEF = "TransientBelief";
	public static final String TRANSIENT_BELIEF_SET = "TransientBeliefSet";
	public static final String PERSISTENT_BELIEF = "PersistentBelief";
	public static final String TRANSIENT_PROPOSITIONAL_BELIEF = "TransientPropositionalBelief";
	
	public static final String BELIEF_GOAL = "BeliefGoal";
	public static final String BELIEF_PRESENT_GOAL = "BeliefPresentGoal";
	public static final String BELIEF_SET_HAS_VALUE_GOAL = "BeliefSetHasValueGoal";
	public static final String BELIEF_VAUE_GOAL = "BeliefValueGoal";
	public static final String PROPOSITIONAL_BELIEF_VAUE_GOAL = "PropositionalBeliefValueGoal";
	
	public static final String BELIEF_GOAL_BELIEF_NAME = "beliefName";
	public static final String BELIEF_SET_HAS_VALUE_GOAL_VALUE = "value";
	public static final String BELIEF_VAUE_GOAL_VALUE = "value";
	public static final String PROPOSITIONAL_BELIEF_VAUE_GOAL_VALUE = "value";

}
