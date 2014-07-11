package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.formulae.*;
import tvla.jeannet.expressions.*;

/** An class for the Precondition primitive
 * @author Bertrand Jeannet
 */

public class TransformPrimitiveExpressionAST extends ExpressionAST {
    /** Primary fields */
    private Symbol var;
    private FormulaAST preconditionFormula = null;
    private FormulaAST haltCondition = null;
    private List messages = null; // List of type ReportMessageAST
    private FormulaAST newFormula = null;
    private FormulaAST cloneFormula = null;
    private List updates;
    private FormulaAST retainFormula = null;

    /** Constructor */
    public TransformPrimitiveExpressionAST(Symbol var,
					FormulaAST preconditionFormula,
					FormulaAST haltCondition,
					List messages,
					FormulaAST newFormula,
					FormulaAST cloneFormula,
					List updates,
					FormulaAST retainFormula)
    {
	this.var = var;
	this.preconditionFormula = preconditionFormula;
	this.haltCondition = haltCondition;
	this.messages = messages;
	this.newFormula = newFormula;
	this.cloneFormula = cloneFormula;
	this.updates = updates;
	this.retainFormula = retainFormula;
    }

    /** Substitutes predicate names */
    public void substitute(String from, String to){
	if (preconditionFormula != null)
	    preconditionFormula.substitute(from, to);
	if (haltCondition != null)
	    haltCondition.substitute(from, to);
	if (messages != null)
	    substituteList(messages, from, to);
	if (newFormula != null)
	    newFormula.substitute(from, to);
	if (cloneFormula != null)
	    cloneFormula.substitute(from, to);
	if (updates!=null)
	    substituteList(updates, from, to);
	if (retainFormula != null)
	    retainFormula.substitute(from, to);
    }
    public void substitute(List froms, List tos){
	if (preconditionFormula != null)
	    preconditionFormula.substitute(froms, tos);
	if (haltCondition != null)
	    haltCondition.substitute(froms, tos);
	if (messages != null)
	    substituteList(messages, froms, tos);
	if (newFormula != null)
	    newFormula.substitute(froms, tos);
	if (cloneFormula != null)
	    cloneFormula.substitute(froms, tos);
	if (updates!=null)
	    substituteList(updates, froms, tos);
	if (retainFormula != null)
	    retainFormula.substitute(froms, tos);
    }

    /** Copy */
    public AST copy(){
	return new TransformPrimitiveExpressionAST(var,
						preconditionFormula == null ? null : (FormulaAST)preconditionFormula.copy(),
						haltCondition == null ? null : (FormulaAST)haltCondition.copy(),
						messages == null ? null : copyList(messages),
						newFormula == null ? null : (FormulaAST)newFormula.copy(),
						cloneFormula == null ? null : (FormulaAST)cloneFormula.copy(),
						updates == null ? null : copyList(updates),
						retainFormula == null ? null : (FormulaAST)retainFormula.copy());
    }

    /** Evaluate Foreach intructions */
    public void evaluate(){
	if (this.updates != null){
	    List newUpdates = new ArrayList();
	    for (Iterator i = this.updates.iterator(); i.hasNext(); ) {
		AST anUpdate = (AST) i.next();
		if (anUpdate instanceof ForeachAST) {
		    newUpdates.addAll(((ForeachAST) anUpdate).evaluate());
		} else {
		    newUpdates.add(anUpdate);
		}
	    }
	    this.updates = newUpdates;
	}
    }

    /** Generate an Expression */
    public Expression getExpression(){
	TransformPrimitiveExpression result = new TransformPrimitiveExpression();

	result.var(var);
	if (preconditionFormula!=null) result.precondition(preconditionFormula.getFormula());
	for (Iterator i=messages.iterator(); i.hasNext(); ){
	    ReportMessageAST m = (ReportMessageAST)i.next();
	    result.addReportMessage(m.getReportMessage());
	}
	if (haltCondition!=null) result.haltCondition(haltCondition.getFormula());
	if (newFormula!=null) result.newFormula(newFormula.getFormula());
	if (cloneFormula!=null) result.cloneFormula(cloneFormula.getFormula());

	if (updates != null){
	    for (Iterator i=updates.iterator(); i.hasNext(); ){
		UpdateAST update = (UpdateAST)i.next();
		result.setPredicateUpdateFormula(update.getPredicateUpdateFormula());
	    }
	}
	if (retainFormula!=null) result.retainFormula(retainFormula.getFormula());
	return result;
    }
}
