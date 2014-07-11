package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.jeannet.expressions.*;
import tvla.logic.*;
import tvla.util.*;
import java.util.*;

public class SeqExpressionAST extends ExpressionAST {
    List exprs;

    /** Constructor */
    public SeqExpressionAST(List exprs) {
	this.exprs = exprs;
    }

    /** Substitutes predicate names */
    public void substitute(String from, String to){
	substituteList(exprs,from,to);
    }
    public void substitute(List froms, List tos){
	substituteList(exprs,froms,tos);
    }
    /** Copy */
    public AST copy(){
	return new SeqExpressionAST(copyList(exprs));
    }

    /** Evaluate Foreach intructions */
    public void evaluate(){
	for (Iterator i=exprs.iterator(); i.hasNext(); ){
	    ExpressionAST e = (ExpressionAST)i.next();
	    e.evaluate();
	}
    }

    /** Generate an Expression */
    public Expression getExpression(){
	List list = new ArrayList(exprs.size());
	for (Iterator i=exprs.iterator(); i.hasNext(); ){
	    ExpressionAST e = (ExpressionAST)i.next();
	    list.add(e.getExpression());
	}
	return new SeqExpression(list);
    }
}
