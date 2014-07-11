package tvla.jeannet.language.TVP;

import tvla.core.*;
import tvla.exceptions.*;
import tvla.formulae.*;
import tvla.logic.*;
import tvla.predicates.*;
import java.util.*;
import tvla.core.Constraints.*;

/** An abstract snytax node for instrumentation predicates. 
 * @author Tal Lev-Ami
 */
public class InstrumPredicateAST extends PredicateAST {    
	List args;
	FormulaAST formula;

	public InstrumPredicateAST(String name, List params, List args, 
							   FormulaAST formula, PredicatePropertiesAST type, Set attr) {
		super(name, params, type, attr, args.size());
		this.args = args;
		this.formula = formula;
	}

	private InstrumPredicateAST(InstrumPredicateAST other) {
		super(other);
		this.args = other.args;
		this.formula = (FormulaAST) other.formula.copy();
	}

	public void substitute(String from, String to) {
		formula.substitute(from, to);
		super.substitute(from, to);
	}
	
	public AST copy() {
		return new InstrumPredicateAST(this);
	}

	public void generate() {
	        Constraint cons;
		Constraints consInstance = Constraints.getInstance();
		// Add to the structure.
		Formula formula = this.formula.getFormula();
		Predicate predicate = Vocabulary.createInstrumentationPredicate(generateName(), 
																		arity, 
																		properties.abstraction(),
																		formula,
																		args);
		generatePredicate(predicate);

		PredicateFormula predicateFormula = new PredicateFormula(predicate,args);
		
		// Check that the free variables of the formula match the arguments to the predicate.
		Set tempFreeVars = new HashSet(args);
		tempFreeVars.removeAll(formula.freeVars());
		if (!tempFreeVars.isEmpty())
			throw new SemanticErrorException("Formula's (" + formula + 
				") free variables (" +
				formula.freeVars() + ") must match " + 
				" the predicates arguments (" + 
				args + ") in instrumentation " + 
				generateName());
		

		if (!Constraints.automaticConstraints)
			return;
		// Add the definition constraints
		cons = new Constraint(formula, predicateFormula.copy());
		consInstance.add(cons);
		cons = new Constraint(new NotFormula(formula), 
				      new NotFormula(predicateFormula.copy()));
		consInstance.add(cons);

		// If the definition is expanded to extended horn, create the closure.

		// Get the prenex DNF normal form.
		Formula prenexDNF = Formula.toPrenexDNF(formula);
		boolean negated = false;

		// Remove all existantial quantifiers.
		while (true) {
			if (prenexDNF instanceof ExistQuantFormula) {
				ExistQuantFormula eformula = (ExistQuantFormula) prenexDNF;
				prenexDNF = eformula.subFormula();
			}
			else {
				break;
			}
		}

		if ((prenexDNF instanceof AllQuantFormula) || (prenexDNF instanceof OrFormula)) {
			// Try the negated formula.
			negated = true;
			prenexDNF = Formula.toPrenexDNF(new NotFormula(formula));
			// Remove all existantial quantifiers.
			while (true) {
				if (prenexDNF instanceof ExistQuantFormula) {
					ExistQuantFormula eformula = (ExistQuantFormula) prenexDNF;
					prenexDNF = eformula.subFormula();
				}
				else {
					break;
				}
			}
		}

		if (prenexDNF instanceof AndFormula) {
			// OK. This is a good candidate for closure.
			List terms = new ArrayList();
			Formula.getAnds(prenexDNF, terms);
			for (Iterator termIt = terms.iterator(); termIt.hasNext(); ) {
				Formula term = (Formula) termIt.next();
				Formula origTerm = term;
				boolean negatedTerm = false;
				if (term instanceof NotFormula) {
					NotFormula nterm = (NotFormula) term;
					term = nterm.subFormula();
					negatedTerm = true;
				}

				if ((term instanceof PredicateFormula) || (negatedTerm && (term instanceof EqualityFormula))) {
					// The body formula is the terms without this term and 
					// with the negated instrumentation. All the variables not in the head are
					// existantialy quantified.
					Formula body = negated ? predicateFormula.copy() : new NotFormula(predicateFormula.copy());
					for (Iterator otherTermIt = terms.iterator(); otherTermIt.hasNext(); ) {
						Formula otherTerm = (Formula) otherTermIt.next();
						if (otherTerm == origTerm)
							continue;
						body = new AndFormula(body, otherTerm.copy());
					}
					Formula head = negatedTerm ? term.copy() : new NotFormula(term.copy());
					Set freeVars = new HashSet(body.freeVars());
					freeVars.removeAll(head.freeVars());
					for (Iterator varIt = freeVars.iterator(); varIt.hasNext(); ) {
						Var var = (Var) varIt.next();
						body = new ExistQuantFormula(var, body);
					}

					cons = new Constraint(body, head);
					consInstance.add(cons);
				}
			}
		}
	}
}
