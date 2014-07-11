package tvla.jeannet.language.TVP;

import tvla.core.*;
import tvla.exceptions.*;
import tvla.formulae.*;
import tvla.logic.*;
import tvla.predicates.*;
import java.util.*;
import tvla.core.Constraints.*;

/** An AST node for predicate properties.
 * @author Eran Yahav
 * @since tvla-2-alpha (May 14 2002) Initial creation.
 */
public final class PredicatePropertiesAST extends AST {	
    protected boolean unique = false;
    protected boolean function = false;
    protected boolean invfunction = false;
    protected boolean abstraction = true;
    protected boolean symmetric = false;
    protected boolean transitive = false;
    protected boolean antisymmetric = false;
    protected boolean reflexive = false;
    protected boolean antireflexive = false;
    protected boolean pointer = false;
    protected boolean karyfunction = false;

    /** @author Alexey Loginov.
     */
    protected boolean acyclic = false;
	
    protected List predicateConstraints;
	
	
    public PredicatePropertiesAST() {	
    }
	
    public PredicatePropertiesAST(PredicatePropertiesAST other) {
	this.abstraction = other.abstraction;
	this.symmetric = other.symmetric;
	this.antisymmetric = other.antisymmetric;
	this.reflexive = other.reflexive;
	this.antireflexive = other.antireflexive;
	this.transitive = other.transitive;
	this.unique = other.unique;
	this.function = other.function;
	this.invfunction = other.invfunction;
	this.pointer = other.pointer;
	this.acyclic = other.acyclic;
    }
	
    /** Returns a copy of this PredicatePropertiesAST()
     */
    public AST copy() {
	return new PredicatePropertiesAST(this);
    }
	
    /** Substitue doesn't need to do anything
     */
    public void substitute(String from, String to) {}
    public void substitute(List froms, List tos) {}
	
    /** Adds a property.
     */
    public void addProperty(String prop) {
	prop = prop.trim();
	if (prop.equals("unique"))
	    this.unique = true;
	else if (prop.equals("function"))
	    this.function = true;
	else if (prop.equals("invfunction"))
	    this.invfunction = true;
	else if (prop.equals("abs")) 
	    abstraction = true;
	else if (prop.equals("nonabs"))
	    abstraction = false;
	else if (prop.equals("reflexive"))
	    reflexive = true;
	else if (prop.equals("antireflexive"))
	    antireflexive = true;
	else if (prop.equals("symmetric"))
	    symmetric = true;
	else if (prop.equals("antisymmetric"))
	    antisymmetric = true;
	else if (prop.equals("transitive"))
	    transitive = true;
	else if (prop.equals("pointer") || prop.equals("box"))
	    pointer = true;
	else if (prop.equals("acyclic"))
	    acyclic = true;
	else {
	    throw new SemanticErrorException("Unknown property " + prop);
	}
    }	
	
	
    /** Adds a functional dependency.
     */
    public void addFunctionalDependency(List lhs, String rhs) {
	throw new UnsupportedOperationException("Implementation incomplete " + 
						"for functional dependencies of predicates with arbitrary arity!");
    }
	
    /** Validates the properties for a given predicate arity
     * name is given for error reporting purposes.
     */
    public void validate(String predName, int arity) {		
	if (arity !=1) {
	    if (this.unique) 
		throw new SemanticErrorException("Only unary predicates can be unique in " 
						 + predName);
	    if (this.pointer)
		throw new SemanticErrorException("Only unary predicates can be in a pointer in " 
						 + predName);
	}
	if (arity !=2) {
	    if (this.function) 
		throw new SemanticErrorException("Only binary predicates can be injective in "
						 + predName);
	    if (this.invfunction)
		throw new SemanticErrorException("Only binary predicates can be invfunction in " + 
						 predName);
	    if (this.reflexive) 
		throw new SemanticErrorException("Only binary predicates can be reflexive in " +
						 predName);
	    if (this.antireflexive) 
		throw new SemanticErrorException("Only binary predicates can be antireflexive in " +
						 predName);
	    if (this.symmetric) 
		throw new SemanticErrorException("Only binary predicates can be symmetric in " +
						 predName);
	    if (this.antisymmetric) 
		throw new SemanticErrorException("Only binary predicates can be antisymmetric in " +
						 predName);
	    if (this.transitive) 
		throw new SemanticErrorException("Only binary predicates can be transitive in " +
						 predName);
	    if (this.acyclic)
		throw new SemanticErrorException("Only binary predicates can be acyclic in " + 
						 predName);
	}
    }
	
    /** Generates constraints for the given predicate according to the
     * current properties.
     */
    public void generateConstraints(Predicate predicate) {
	Constraints consInstance = Constraints.getInstance();
	Constraint cons;
	if (unique) {
	    Var v1 = new Var("v1");
	    Var v2 = new Var("v2");
	    cons = new Constraint(new AndFormula(new PredicateFormula(predicate, v1),
						 new PredicateFormula(predicate, v2)),
				  new EqualityFormula(v1, v2));
	    consInstance.add(cons);
	    cons = new Constraint(new ExistQuantFormula(v1, 
							new AndFormula(new PredicateFormula(predicate, v1), 
								       new NotFormula(new EqualityFormula(v1, v2)))), 
				  new NotFormula(new PredicateFormula(predicate, v2)));
	    consInstance.add(cons);
	}

	if (function) {
	    Var v1 = new Var("v1");
	    Var v2 = new Var("v2");
	    Var v = new Var("v");
	    cons = new Constraint(new ExistQuantFormula(v, 
							new AndFormula(new PredicateFormula(predicate, v, v1),
								       new PredicateFormula(predicate, v, v2))),
				  new EqualityFormula(v1, v2));
	    consInstance.add(cons);
	    cons = new Constraint(new ExistQuantFormula(v1, 
							new AndFormula(new PredicateFormula(predicate, v, v1), 
								       new NotFormula(new EqualityFormula(v1, v2)))), 
				  new NotFormula(new PredicateFormula(predicate, v, v2)));
	    consInstance.add(cons);
	}

	if (invfunction) {
	    Var v1 = new Var("v1");
	    Var v2 = new Var("v2");
	    Var v = new Var("v");
	    cons = new Constraint(new ExistQuantFormula(v, 
							new AndFormula(new PredicateFormula(predicate, v1, v),
								       new PredicateFormula(predicate, v2, v))),
				  new EqualityFormula(v1, v2));
	    consInstance.add(cons);
	    cons = new Constraint(new ExistQuantFormula(v1, 
							new AndFormula(new PredicateFormula(predicate, v1, v), 
								       new NotFormula(new EqualityFormula(v1, v2)))), 
				  new NotFormula(new PredicateFormula(predicate, v2, v)));
	    consInstance.add(cons);
	}
	if (symmetric) {
	    Var v1 = new Var("v1");
	    Var v2 = new Var("v2");
	    cons = new Constraint(new PredicateFormula(predicate, v1, v2),
				  new PredicateFormula(predicate, v2, v1));
	    consInstance.add(cons);
	}
	if (antisymmetric) {
	    Var v1 = new Var("v1");
	    Var v2 = new Var("v2");
	    cons = new Constraint(new AndFormula(new PredicateFormula(predicate, v1, v2),
						 new PredicateFormula(predicate, v2, v1)),
				  new EqualityFormula(v1, v2));
	    consInstance.add(cons);
	    cons = new Constraint(new AndFormula(new PredicateFormula(predicate, v1, v2),
						 new NotFormula(new EqualityFormula(v1, v2))),
				  new NotFormula(new PredicateFormula(predicate, v2, v1)));	    
	    consInstance.add(cons);
	}
	if (reflexive) {
	    Var v1 = new Var("v1");
	    Var v2 = new Var("v2");
	    cons = new Constraint(new EqualityFormula(v1, v2),
				  new PredicateFormula(predicate, v1, v2));
	    consInstance.add(cons);
	}
	if (antireflexive) {
	    Var v1 = new Var("v1");
	    Var v2 = new Var("v2");
	    cons = new Constraint(new EqualityFormula(v1, v2),
				  new NotFormula(new PredicateFormula(predicate, v1, v2)));
	    consInstance.add(cons);
	}
	if (transitive) {
	    Var v1 = new Var("v1");
	    Var v2 = new Var("v2");
	    Var v3 = new Var("v3");
	    cons = new Constraint(new ExistQuantFormula(v2, 
							new AndFormula(new PredicateFormula(predicate, v1, v2),
								       new PredicateFormula(predicate, v2, v3))),
				  new PredicateFormula(predicate, v1, v3));
	    consInstance.add(cons);
	}
	if (karyfunction) {
	    // k-ary function, taking (k-1)-tuple into a single value
	    int arity = predicate.arity();
		    
	    Var[] vars1 = new Var[arity];
	    Var[] vars2 = new Var[arity];
		    
	    int domSize = arity-1;
	    for (int i=0;i<domSize;i++) {
		Var currVar = new Var("v" + i);
		vars1[i] = currVar;
		vars2[i] = currVar;
	    }
		    
	    Var u1 = new Var("u1");
	    Var u2 = new Var("u2");
			
	    vars1[arity] = u1;
	    vars2[arity] = u2;
			
	    AndFormula andFormula = new AndFormula(new PredicateFormula(predicate, vars1),
						   new PredicateFormula(predicate, vars2));
			
	    Formula currFormula = andFormula;
	    for (int j = domSize; j >= 0; j--) {
		currFormula = new ExistQuantFormula(vars1[j], currFormula);
	    }
	    cons = new Constraint(currFormula,new EqualityFormula(u1, u2));
	    consInstance.add(cons);
	    cons = new Constraint(new ExistQuantFormula(u1, 
							new AndFormula(new PredicateFormula(predicate, vars1), 
								       new NotFormula(new EqualityFormula(u1, u2)))), 
				  new NotFormula(new PredicateFormula(predicate,vars2)));
	    consInstance.add(cons);
	}

	if (acyclic) {
	    Var v = new Var("v");
	    cons = new Constraint(new PredicateFormula(predicate, v, v),
				  new ValueFormula(Kleene.falseKleene));
	    consInstance.add(cons);
	    // At this point we don't know if there's an instrumentation predicate, rtc[p], which
	    // is the RTC closure of p.  However, we'll know it when Coerce converts these
	    // constraints to its structure.  At that point we will add the two constraints below
	    // if we do have rtc[p] and only the first one with p* in place of rtc[p], if we don't:
	    // rtc[p](v1, v2) & rtc[p](v2, v1) ==> v1 == v2  (1)
	    // rtc[p](v1, v2) & v1 != v2 ==> !rtc[p](v2, v1) (2)
	    // In the future we may instead want to automatically generate rtc[p] given p.
	}
    }
	
    public boolean unique() {
	return unique;
    }
	
    public boolean abstraction() {
	return abstraction;
    }
	
    public boolean function() {
	return function;
    }
	
    public boolean invfunction() {
	return invfunction;
    }

    public boolean pointer() {
	return pointer;
    }
	
    /** @author Alexey Loginov.
     */
    public boolean acyclic() {
	return acyclic;
    }
	
    public void setAbstraction(boolean val) {
	abstraction = val;
    }
	
    public String toString() {
	StringBuffer result = new StringBuffer("(");
	if (unique)
	    result.append("unique ");
	if (function)
	    result.append("function ");
	if (invfunction)
	    result.append("invfunction ");
	if (abstraction)
	    result.append("abstraction ");
	if (symmetric)
	    result.append("symmetric ");
	if (transitive)
	    result.append("transitive ");
	if (antisymmetric)
	    result.append("antisymmetric ");
	if (reflexive)
	    result.append("reflexive ");
	if (antireflexive)
	    result.append("antireflexive ");
	if (pointer)
	    result.append("pointer ");
	if (karyfunction)
	    result.append("karyfunction ");
	if (acyclic)
	    result.append("acyclic ");
	result.append(")");
	return result.toString();
    }
}
