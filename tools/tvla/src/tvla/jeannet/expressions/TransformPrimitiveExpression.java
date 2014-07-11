package tvla.jeannet.expressions;

import java.util.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.formulae.*;
import tvla.core.*;
import tvla.core.common.*;
import tvla.core.assignments.*;
import tvla.logic.*;
import tvla.predicates.*;
import tvla.exceptions.*;
import tvla.io.*;
import tvla.analysis.AnalysisStatus;
import tvla.analysis.Engine;

/** An class for the Precondition primitive
 * @author Bertrand Jeannet
 */

public class TransformPrimitiveExpression implements Expression {
    /** Primary fields */
    private Symbol var;
    private Formula preconditionFormula = null;
    private Formula haltCondition = null;
    private List messages = new ArrayList();  // List of type ReportMessage
    private NewUpdateFormula newFormula = null;
    private CloneUpdateFormula cloneFormula = null;
    private Map updateFormulae = new HashMap(); /* map from Predicate to PredicateUpdateFormula. */
    private RetainUpdateFormula retainFormula = null;

    /** Auxiliary fields (created from primary for efficiency reasons) */
    private boolean initialized = false;
    private List preconditionConjunction = null;
    private List preconditionTC = null;

    /* ===================================================================== */
    /** Printing */
    /* ===================================================================== */
    public String toString(){
	StringBuffer buffer = new StringBuffer();

	Set updates = updateFormulae.entrySet();
	for (Iterator i=updates.iterator(); i.hasNext(); ){
	    Map.Entry entry = (Map.Entry)i.next();
	    PredicateUpdateFormula formula = (PredicateUpdateFormula)entry.getValue();
	    buffer.append("\t");
	    buffer.append(formula);
	    buffer.append("\n");
	}
	return "transform(" +
	    var + ",\n" +
	    (preconditionFormula!=null ? ("\tprecond:" + preconditionFormula + ",\n") : "") +
	    (haltCondition!=null ? ("\thalt:" + haltCondition + ",\n") : "") +
	    (newFormula!=null ? ("\tnew:" + newFormula + ", ") : "") +
	    (cloneFormula!=null ? ("\tclone:" + cloneFormula + ",\n") : "") +
	    "\tupdate: {\n" + buffer + "},\n" +
	    (retainFormula!=null ? ("\tretain:" + retainFormula) : "") +
	    ")";
    }



    /* ===================================================================== */
    /** Expression */
    /* ===================================================================== */
    public void var(Symbol var){
	this.var = var;
    }
    public Symbol getVar(){
	return this.var;
    }

    /* ===================================================================== */
    /** Preconditions */
    /* ===================================================================== */
    public void precondition(Formula formula) {
	this.preconditionFormula = formula;
    }
    public Formula getPrecondition() {
	return preconditionFormula;
    }
    private Collection checkPrecondition(TVS structure) {
	Set satisfy = new HashSet();
	if (preconditionFormula == null) {
	    satisfy.add(Assign.EMPTY);
	}
	else {
	    if (preconditionConjunction == null) {
		preconditionConjunction = new ArrayList();
		Formula.getAnds(preconditionFormula, preconditionConjunction);
		preconditionTC = new ArrayList();
		Formula.getAllTC(preconditionFormula, preconditionTC);
	    }
	    for (Iterator it = preconditionTC.iterator(); it.hasNext(); ) {
		tvla.formulae.TransitiveFormula TC = (TransitiveFormula) it.next();
		TC.explicitRecalc();
	    }
	    int numberOfSteps = preconditionConjunction.size();
	    Assign[] stepAssign = new Assign[numberOfSteps + 1];
	    Iterator[] stepIt = new Iterator[numberOfSteps + 1];
	    stepIt[0] = new SingleIterator(Assign.EMPTY);
	    int currentStep = 0;
	    while (currentStep >= 0) {
		if (stepIt[currentStep].hasNext()) {
		    Assign currentAssign = stepAssign[currentStep] = (Assign) stepIt[currentStep].next();
		    if (currentStep == numberOfSteps) {
			Assign satisfyAssign = new Assign(currentAssign);
			satisfyAssign.project(preconditionFormula.freeVars());
			satisfy.add(satisfyAssign);
		    }
		    else {
			Formula formula = (Formula) preconditionConjunction.get(currentStep);
			currentStep++;
			stepIt[currentStep] = structure.evalFormula(formula, currentAssign);
		    }
		}
		else {
		    currentStep--;
		}
	    }
	    for (Iterator it = preconditionTC.iterator(); it.hasNext(); ) {
		tvla.formulae.TransitiveFormula TC = (TransitiveFormula) it.next();
		TC.setCalculatedTC(null);
	    }
	}
	return satisfy;
    }
    /* ===================================================================== */
    /** Halt */
    /* ===================================================================== */

    public void haltCondition(Formula formula) {
	this.haltCondition = formula.copy();
    }
    public Formula getHaltCondition() {
	return this.haltCondition;
    }
    public boolean checkHaltCondition(HighLevelTVS structure, Assign assign) {
	if (haltCondition == null)
	    return false;
	else
	    return haltCondition.eval(structure, assign).equals(Kleene.trueKleene);
    }

    /* ===================================================================== */
    /** Messages */
    /* ===================================================================== */

    /** Adds to the primitive a new ReportMessage */
    public void addReportMessage(ReportMessage m){
	this.messages.add(m);
    }
    /** Returns the List of ReportMessage of the primitive */
    public List getReportMessages(){
	return messages;
    }
    public Collection checkMessages(HighLevelTVS structure, Assign assign) {
	Collection answer = new ArrayList();
	for (Iterator it = messages.iterator(); it.hasNext(); ) {
	    ReportMessage report = (ReportMessage) it.next();
	    Kleene value = report.getFormula().eval(structure, assign);
	    if (value != Kleene.falseKleene)
		answer.add(report.getMessage());
	}
	return answer;
    }

    /* ===================================================================== */
    /** New, Clone and Retain formulae */
    /* ===================================================================== */
    public void newFormula(Formula formula) {
	this.newFormula = new NewUpdateFormula(formula);
    }
    public NewUpdateFormula getNewFormula() {
	return this.newFormula;
    }
    public void cloneFormula(Formula formula) {
	this.cloneFormula = new CloneUpdateFormula(formula);
    }
    public CloneUpdateFormula getCloneFormula() {
	return this.cloneFormula;
    }
    public void retainFormula(Formula formula) {
	this.retainFormula = new RetainUpdateFormula(formula);
    }
    public RetainUpdateFormula getRetainFormula() {
	return this.retainFormula;
    }

    /* ===================================================================== */
    /** Update */
    /* ===================================================================== */

    /** Retrieves the update formula associated with a predicate.
     */
    public PredicateUpdateFormula getUpdateFormula(Predicate predicate) {
	return (PredicateUpdateFormula) updateFormulae.get(predicate);
    }

    /** Retrieves all the update formulas, as a Map from Predicate to PredicateUpdateFormula.
     */
    public Map getUpdateFormulae(){
	return updateFormulae;
    }

    /** Stores a new update formula associated with a predicate.
     */
    public void setPredicateUpdateFormula(PredicateUpdateFormula formula) {
	updateFormulae.put(formula.getPredicate(), formula);
    }

    /* ===================================================================== */
    /** Initialize the auxiliary fields from the primary fields, and
	perform some checks. */
    /* ===================================================================== */

    /* QUESTIONS TO ALEXEY FOR FINITE DIFFERENCING */

    public void init() {
	if (initialized)
	    return;
	initialized = true;
	// Verify all formula have the right variables.
	Collection precondFree = (preconditionFormula == null) ? Collections.EMPTY_SET : preconditionFormula.freeVars();

	// Check the new formula.
	if (newFormula != null) {
	    Set newVars = new HashSet(newFormula.freeVars());
	    newVars.removeAll(precondFree);

	    if (newVars.size() == 0) {
		newFormula.newVar = null;
	    }
	    else if (newVars.size() == 1) {
		newFormula.newVar = (Var) newVars.iterator().next();
	    }
	    else {
		throw new SemanticErrorException("New formula (" + newFormula +
						 ") must be nullary or unary.");
	    }
	}

	// Check the clone formula.
	if (cloneFormula != null) {
	    Set cloneVars = new HashSet(cloneFormula.freeVars());
	    cloneVars.removeAll(precondFree);
	    if (cloneVars.size() == 1) {
		cloneFormula.var = (Var) cloneVars.iterator().next();
	    }
	    else {
		throw new SemanticErrorException("Clone formula (" + cloneFormula +
						 ") must be unary.");
	    }
	}


	// Check the update formulae
	for (Iterator updateIt = updateFormulae.entrySet().iterator(); updateIt.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) updateIt.next();
	    Predicate predicate = (Predicate) entry.getKey();
	    PredicateUpdateFormula update = (PredicateUpdateFormula) entry.getValue();

	    Set freeVars = new HashSet(update.freeVars());
	    freeVars.removeAll(precondFree);
	    switch (predicate.arity()) {
	    case 0:	if (!freeVars.isEmpty()) {
		throw new SemanticErrorException("Nullary update formula " +
						 "for " + predicate +
						 " should be closed but has " +
						 freeVars + " as free variables.");
	    }
		break;
	    case 1:
		Var v = update.getVariable(0);
		freeVars.remove(v);

		if (!freeVars.isEmpty()) {
		    throw new SemanticErrorException("Unary update formula " +
						     "for " + predicate +
						     " has the following superfluous " +
						     "free variables " + freeVars);
		}
		break;
	    case 2:
		Var leftVar  = update.getVariable(0);
		Var rightVar = update.getVariable(1);

		freeVars.remove(leftVar);
		freeVars.remove(rightVar);

		if (!freeVars.isEmpty()) {
		    throw new SemanticErrorException("Binary update formula " +
						     "for " + predicate +
						     " has the following superfluous " +
						     "free variables " + freeVars);
		}
		break;
	    default:
		int varNum = update.predicateArity();
		for (int i=0;i<varNum;i++) {
		    Var currVar = update.getVariable(i);
		    freeVars.remove(currVar);
		}
		if (!freeVars.isEmpty()) {
		    throw new SemanticErrorException("Update formula " +
						     "for " + predicate +
						     " has the following superfluous " +
						     "free variables " + freeVars);
		}
		break;
	    }
	}

	// Check the retain formula
	if (retainFormula != null) {
	    Set retainVars = new HashSet(retainFormula.freeVars());
	    retainVars.removeAll(precondFree);

	    if (retainVars.size() != 1) {
		throw new SemanticErrorException("Retain formula (" +
						 retainFormula + ") must be unary.");
	    }
	    retainFormula.retainVar = (Var) retainVars.iterator().next();
	}
    }

    /* ===================================================================== */
    /* Evaluation */
    /* ===================================================================== */

    /** Evaluates the expression on a single structure,
	returns a collection of structures.
    */
    private Collection evaluateTVS(HighLevelTVS structure, RuntimeExpr runtime) {
	Collection result = new ArrayList();

	// Precondition evaluation
	if (runtime.status.debugstep) {
	    if (preconditionFormula==null){
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nTransform");
	    } else {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nTransform: executing precond:" + preconditionFormula);
	    }
	}
	//runtime.status.startTimer(AnalysisStatus.PRECONDITION_TIME);
	Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.PRECONDITION_TIME);
    Collection assigns = checkPrecondition(structure);
	Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.PRECONDITION_TIME);
    //runtime.status.stopTimer(AnalysisStatus.PRECONDITION_TIME);

	// Iter on assignements of the free variables of the precondition formula
	for (Iterator assignIt = assigns.iterator(); assignIt.hasNext(); ) {
	    Assign assign = (Assign)assignIt.next();

	    if (checkHaltCondition(structure,assign)){
		throw new AnalysisHaltException(structure,assign,haltCondition);
	    }
	    if (runtime.status.debugstep) {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nTransform: executing new:,clone:,update:,retain:, with assign=" + (assign.isEmpty() ? "empty" : "" + assign));
	    }
	    Collection newMessages = checkMessages(structure, assign);
	    if (!newMessages.isEmpty() && runtime.status.debugstep) {
		// A completer
	    }


	    // Update formulae evaluation
	    //runtime.status.startTimer(AnalysisStatus.UPDATE_TIME);
        Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.UPDATE_TIME);

	    HighLevelTVS newStructure = (HighLevelTVS) structure.copy();

	    if (newFormula != null)
		newStructure.applyNewUpdateFormula(newFormula, assign);

	    if (cloneFormula != null)
		newStructure.applyCloneUpdateFormula(cloneFormula, assign);

	    newStructure.updatePredicates(updateFormulae.values(), assign);

	    if (retainFormula != null)
		newStructure.applyRetainUpdateFormula(retainFormula, assign, structure);

	    if (newFormula != null || cloneFormula != null) {
		newStructure.clearPredicate(Vocabulary.isNew);
		newStructure.clearPredicate(Vocabulary.instance);
	    }

        Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.UPDATE_TIME);
	    //runtime.status.stopTimer(AnalysisStatus.UPDATE_TIME);

	    if (runtime.status.debugstep)
		IOFacade.instance().printStructure(newStructure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nAfter Transform " + (assign.isEmpty() ? "" : " " + assign));

	    result.add(newStructure);
	}
	ModifiedPredicates.clear();
	return result;
    }

    /** Does the expression has a value ? */
    public boolean hasValue(){ return true; }

    /** Evaluates the expression using the given environment.
     */
    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime){
	runtime.primitiveNumber++;

	init();

	/* First, evaluate the first argument. */
	ValueExpression value = bindings.lookup(var);
	Collection collection = value.getCollectionTVS();

	/* Then compute the image of the argument by update. */
	Collection result = new ArrayList();
	for (Iterator structIt = collection.iterator(); structIt.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)structIt.next();
	    Collection answer = evaluateTVS(structure, runtime);
	    result.addAll(answer);
	}
	return new ValueExpression(result);
    }
}
