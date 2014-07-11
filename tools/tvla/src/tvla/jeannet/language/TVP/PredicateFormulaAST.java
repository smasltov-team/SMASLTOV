package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;
import tvla.predicates.*;

/** @author Tal Lev-Ami
 */
public class PredicateFormulaAST extends FormulaAST {
	PredicateAST predicate;
	Var[] parameters;

	public PredicateFormulaAST(PredicateAST predicate) {
		// A nullary predicate
		type = "NullaryPredicateFormula";
		this.predicate = predicate;
		this.predicate.checkArity(0);
	}

	public PredicateFormulaAST(PredicateAST predicate, Var var) {
		// An unary predicate
		type = "UnaryPredicateFormula";
		this.predicate = predicate;
		this.predicate.checkArity(1);
		this.parameters = new Var[1];
		this.parameters[0] = var;
	}

	public PredicateFormulaAST(PredicateAST predicate,
							   Var first, Var second) {
		// A binary predicate
		type = "BinaryPredicateFormula";
		this.predicate = predicate;
		this.predicate.arity = 2;
		this.predicate.checkArity(2);
		this.parameters = new Var[2];
		this.parameters[0] = first;
		this.parameters[1] = second;
	}

	public PredicateFormulaAST(PredicateAST predicate, List args) {
		this.predicate = predicate;
		this.predicate.arity = args.size();
		if (args.size() == 0)
			this.type = "NullaryPredicateFormula";
		else if (args.size() == 1)
			this.type = "UnaryPredicateFormula";
		else if (args.size() == 2)
			this.type = "BinaryPredicateFormula";
		else {
			this.type = "PredicateFormula";
			this.predicate.checkArity(args.size());
		}
		this.parameters = new Var[args.size()];

		ListIterator li = args.listIterator();
		int i = 0;
		while (li.hasNext()) {
			parameters[i] = (Var)li.next();
			i++;
		}
	}

	public PredicateFormulaAST(PredicateAST predicate, Var[] vars) {
		this.predicate = predicate;
		this.predicate.arity = vars.length;
		if (vars.length == 0)
			this.type = "NullaryPredicateFormula";
		else if (vars.length == 1)
			this.type = "UnaryPredicateFormula";
		else if (vars.length == 2)
			this.type = "BinaryPredicateFormula";
		else {
			this.type = "PredicateFormula";
			this.predicate.checkArity(vars.length);
		}
		this.parameters = (Var[])vars.clone();
	}



	public AST copy() {
		return new PredicateFormulaAST((PredicateAST) predicate.copy(), parameters);
	}

	public void substitute(String from, String to) {
		predicate.substitute(from, to);
	}
	public void substitute(List froms, List tos){
		predicate.substitute(froms, tos);
	}

	public Formula getFormula() {
		return new PredicateFormula(predicate.getPredicate(),parameters);
	}
}
