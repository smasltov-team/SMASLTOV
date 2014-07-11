package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.logic.*;
import tvla.formulae.*;
import tvla.predicates.*;

/** An abstract syntax node for core predicates.
 * @author Tal Lev-Ami.
 */
public class CorePredicateAST extends PredicateAST {
	public CorePredicateAST(String name, List params, List args, int arity, PredicatePropertiesAST type, Set attr) {
		super(name, params, type, attr, arity);
	}

	private CorePredicateAST(CorePredicateAST other) {
		super(other);
	}

	public AST copy() {
		return new CorePredicateAST(this);
	}

	public void generate() {			
		Predicate predicate = Vocabulary.createPredicate(generateName(), arity, properties.abstraction());
		generatePredicate(predicate);
	}
}
