package tvla.jeannet.language.TVP;

import java.util.*;

import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.core.TVS;
import tvla.jeannet.expressions.*;
import tvla.jeannet.expressions.PrefabExpression;
import tvla.formulae.*;
import tvla.logic.Kleene;

/** A class for a pre-fabricated set of structures for use in the separation logic semi-decision procedure.
 *  This is the abstract syntax tree class corresponding to the PrefabExpression.
 * @author Jason Breck
 */
public class PrefabExpressionAST extends ExpressionAST {
    private Symbol topVar;
    private List args = new ArrayList();
    private String prefabType;
    private String overUnder;
    
    private Kleene truth;
    private TVS top;    

    /** Constructor */
    public PrefabExpressionAST(Symbol topVar, String prefabType, String overUnder, Kleene truth, List args){
	this.topVar = topVar;
	this.prefabType = prefabType;
	this.overUnder = overUnder;
	this.truth = truth;
	this.args = args;
    }
    /** Substitutes predicate names */
    public void substitute(String from, String to){
	substituteList(args,from,to);
    }
    public void substitute(List froms, List tos){
	substituteList(args,froms,tos);
    }
    /** Copy */
    public AST copy(){
	return new PrefabExpressionAST(topVar, prefabType, overUnder, truth, copyList(args));
    }
    /** Evaluate Foreach intructions */
    public void evaluate(){
	// FIXME: do I need to do anything here?
    }

    /** Generate an Expression */
    public Expression getExpression(){
	List newArgs = new ArrayList(args.size());
	for (Iterator i=args.iterator(); i.hasNext(); ){
	    String str = ((PredicateAST)i.next()).name;
	    newArgs.add(str);
	}
	return new PrefabExpression(topVar, prefabType, overUnder, truth, newArgs);
    }

}
