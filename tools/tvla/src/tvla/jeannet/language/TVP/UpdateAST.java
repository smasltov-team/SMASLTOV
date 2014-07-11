package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.formulae.*;
import tvla.predicates.*;
import java.util.*;

public class UpdateAST extends AST {
    protected PredicateAST predicate;
    protected FormulaAST updateFormula;
    protected List args;
    protected boolean auto;

    public UpdateAST(PredicateAST predicate, FormulaAST updateFormula, List args) {
	this(predicate, updateFormula, args, false);
    }

    public UpdateAST(PredicateAST predicate, FormulaAST updateFormula, List args, boolean auto) {
	this.predicate = predicate;
	this.updateFormula = updateFormula;
	this.args = args;
	this.auto = auto;
	predicate.checkArity(args.size());
    }

    public AST copy() {
	return new UpdateAST((PredicateAST) predicate.copy(),
			     (FormulaAST) updateFormula.copy(), new ArrayList(args), auto);
    }

    public void substitute(String from, String to) {
	predicate.substitute(from, to);
	updateFormula.substitute(from, to);
    }
    public void substitute(List froms, List tos) {
	predicate.substitute(froms, tos);
	updateFormula.substitute(froms, tos);
    }

    public PredicateUpdateFormula getPredicateUpdateFormula(){
	if (predicate.getPredicate().arity() != args.size())
	    throw new SemanticErrorException("Attempt to create a predicate update formula"
					     + " for predicate " + predicate.getPredicate() + " with arity " +
					     predicate.getPredicate().arity() + " and " + args.size() + " argument(s)!");
	if (args.size() == 0) {
	    return new PredicateUpdateFormula(updateFormula.getFormula(), predicate.getPredicate(),
					      auto);
	}
	else if (args.size() == 1) {
	    return new PredicateUpdateFormula(updateFormula.getFormula(),predicate.getPredicate(),
					     (Var) args.get(0), auto);
	}
	else if (args.size() == 2) {
	    return new PredicateUpdateFormula(updateFormula.getFormula(),predicate.getPredicate(),
					      (Var) args.get(0),
					      (Var) args.get(1), auto);
	}
	else {
	    return new PredicateUpdateFormula(updateFormula.getFormula(),predicate.getPredicate(),
					      args, auto);
	}
    }
}
