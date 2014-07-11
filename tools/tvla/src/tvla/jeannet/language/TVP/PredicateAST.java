package tvla.jeannet.language.TVP;

import tvla.core.*;
import tvla.exceptions.*;
import tvla.formulae.*;
import tvla.logic.*;
import tvla.predicates.*;
import java.util.*;

/**
 * @todo:
 *	-	handle k-ary predicates
 *  -	support functional constraints (syntax defined in manual)
 */

/** An abstract snytax node for predicates.
 * @author Tal Lev-Ami
 */
public class PredicateAST extends AST {
	protected String name;
	public int arity = -1;
	protected List params;
	protected Set attr;
	protected PredicatePropertiesAST properties;

	/** @author Tal Lev-Ami
	 * @since 6.5.2001 Nullary predicates are now abstraction by default (Roman).
	 */
	public PredicateAST(String name, List params, PredicatePropertiesAST props, Set attr, int arity) {
		this.name = name;
		this.params = params;
		this.attr = attr;
		this.arity = arity;
		if (props != null)
			this.properties = props;
		else
			this.properties = new PredicatePropertiesAST();

		if (arity > 1) // the default is abstraction = true
			properties.setAbstraction(false);

		properties.validate(generateName(), arity);
	}

	public AST copy() {
		return new PredicateAST(this);
	}

	public String generateName() {
		return generateName(name, params);
	}

	public void substitute(String from, String to) {
	    if (name.equals(from))
		name = to;
	    for (int i = 0; i < params.size(); i++) {
		String param = (String) params.get(i);
		if (param.equals(from)) {
		    params.set(i, to);
		}
	    }
	}
	public void substitute(List froms, List tos){
	    for (int k=0; k<froms.size(); k++){
		if (name.equals((String)froms.get(k))){
		    name = (String)tos.get(k);
		    break;
		}
	    }
	    for (int i = 0; i < params.size(); i++) {
		String param = (String) params.get(i);
		for (int k=0; k<froms.size(); k++){
		    if (param.equals((String)froms.get(k))){
			params.set(i,(String)tos.get(k));
			break;
		    }
		}
	    }
	}

	public Predicate getPredicate() {
		String name = generateName();
		Predicate predicate = Vocabulary.getPredicateByName(name);
		if (predicate == null) {
			throw new SemanticErrorException("Predicate " + name + " was used but not declared.");
		}
		return predicate;
	}

	public void checkArity(int arity) {
		if (this.arity == -1)
			this.arity = arity;
		else if (this.arity != arity)
			throw new SemanticErrorException("Error. Using predicate " + name + " which is " +
				this.arity + "-ary as in " + arity + "-ary context.");
	}

	public static String generateName(String name, List params) {
		StringBuffer result = new StringBuffer();
		String sep = "";
		for (Iterator paramIt = params.iterator(); paramIt.hasNext(); ) {
			String param = (String) paramIt.next();
			result.append(sep + param);
			sep = ",";
		}
		return name + (params.isEmpty() ? "" : "[" + result.toString() + "]");
	}

	public static PredicateAST getPredicateAST(String name, List params) {
		return new PredicateAST(name, params);
	}

	protected PredicateAST(String name, List params) {
		this.name = name;
		this.params = params;
		this.properties = new PredicatePropertiesAST();
	}

	protected PredicateAST(PredicateAST other) {
		this.name = other.name;
		this.arity = other.arity;
		this.params = new ArrayList(other.params);
		this.properties = new PredicatePropertiesAST(other.properties);
		this.attr = other.attr;
	}

	protected void setAttr(Predicate predicate) {
		if (attr != null) {
			boolean showTrue = false;
			boolean showUnknown = false;
			boolean showFalse = false;
			if (attr.contains(Kleene.trueKleene))
				showTrue = true;
			if (attr.contains(Kleene.unknownKleene))
				showUnknown = true;
			if (attr.contains(Kleene.falseKleene))
				showFalse = true;
			predicate.setShowAttr(showTrue, showUnknown, showFalse);
		}
		predicate.setShowAttr(properties.pointer());
	}

	protected void generatePredicate(Predicate predicate) {
		setAttr(predicate);
		if (properties.unique())
			predicate.unique(true);
		if (properties.function())
			predicate.function(true);
		if (properties.invfunction())
			predicate.invfunction(true);
		if (properties.acyclic())
			predicate.acyclic(true);
		if (Constraints.automaticConstraints)
			properties.generateConstraints(predicate);
	}
}
