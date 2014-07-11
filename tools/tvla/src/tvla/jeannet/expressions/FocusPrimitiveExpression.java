package tvla.jeannet.expressions;

import tvla.core.*;
import tvla.formulae.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.io.*;
import java.util.*;

import tvla.analysis.AnalysisStatus;
import tvla.analysis.Engine;

/** An class for the Focus primitive
 * @author Bertrand Jeannet
 */

public class FocusPrimitiveExpression implements Expression {
    private Symbol var;
    private List formulae;

    public FocusPrimitiveExpression(Symbol var, List formulae){
	this.var = var;
	this.formulae = formulae;
    }

    public boolean hasValue(){ return false; }

    /** Focus the given structure on the set of formulae. Returns a
     * collection of HighLevelTVS. */
    private Collection focus(HighLevelTVS structure, List formulae, RuntimeExpr runtime){
	Collection result = new ArrayList();
	result.add(structure);

	if (runtime.status.debugstep) {
	    IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nStarting Focus");
	}
	// Iterates on formulae
	for (Iterator formulaIt = formulae.iterator(); formulaIt.hasNext(); ){
	    Formula formula = (Formula)formulaIt.next();

	    Collection newResult = new ArrayList(result.size());
	    // Iterates on partially focused structures  stored in result
	    for (Iterator structIt = result.iterator(); structIt.hasNext(); ){
		HighLevelTVS resultStructure = (HighLevelTVS)structIt.next();

		//runtime.status.startTimer(AnalysisStatus.FOCUS_TIME);
		Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.FOCUS_TIME);
        Collection focusResult = resultStructure.focus(formula);
		Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.FOCUS_TIME);
        //runtime.status.stopTimer(AnalysisStatus.FOCUS_TIME);

		for (Iterator resultIt = focusResult.iterator(); resultIt.hasNext(); ) {
		    HighLevelTVS focusedStructure = (HighLevelTVS) resultIt.next();

		    if (runtime.status.debugstep) {
			IOFacade.instance().printStructure(resultStructure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nExecuting Focus(" + formula +")");
			IOFacade.instance().printStructure(focusedStructure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nAfter Focus");
		    }
		    // Coerce now if interleave-focus-coerce is enabled
		    if (runtime.interleaveFocusCoerce){
			/*
			{
			    Logger.println("*** Constraints:");
			    Constraints cons = (Constraints)focusedStructure.stackConstraints.peek();
			    Collection coll = cons.allConstraints();
			    int j=0;
			    for (Iterator i=coll.iterator(); i.hasNext();){
				Logger.println(j + ": " + ((Constraint)i.next()).toString());
				j++;
			    }
			}
			*/
			//runtime.status.startTimer(AnalysisStatus.COERCE_TIME);
			Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.COERCE_TIME);
            boolean valid = focusedStructure.coerce();
			Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.COERCE_TIME);
            //runtime.status.stopTimer(AnalysisStatus.COERCE_TIME);
			if (runtime.status.debugstep & valid)
			    IOFacade.instance().printStructure(focusedStructure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nAfter Coerce");
			if (!valid)
			    resultIt.remove();
		    }
		}
		newResult.addAll(focusResult);
		structIt.remove();
	    }
	    result = newResult;
	}
	// Coerce now if interleave-focus-coerce is disabled but coerce-after-focus is enabled
	if (!runtime.interleaveFocusCoerce && runtime.doCoerceAfterFocus){
	    for (Iterator resultIt = result.iterator(); resultIt.hasNext(); ){
		HighLevelTVS focusedStructure = (HighLevelTVS) resultIt.next();

		if (runtime.status.debugstep)
		    IOFacade.instance().printStructure(focusedStructure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nBefore Coerce ");

		//runtime.status.startTimer(AnalysisStatus.COERCE_TIME);
		Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.COERCE_TIME);
        boolean valid = focusedStructure.coerce();
		Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.COERCE_TIME);
        //runtime.status.stopTimer(AnalysisStatus.COERCE_TIME);
		if (runtime.status.debugstep)
		    IOFacade.instance().printStructure(focusedStructure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nAfter Coerce ");
		if (!valid)
		    resultIt.remove();
	    }
	}
	return result;
    }



    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime){
	runtime.primitiveNumber++;

	ValueExpression value = bindings.lookup(var);
	if (formulae.size() > 0) {
	    Collection collection = value.getCollectionTVS();
	    Collection result = new ArrayList();

	    // Iterates on initial structures
	    for (Iterator i = collection.iterator(); i.hasNext(); ){
		HighLevelTVS structure = (HighLevelTVS)i.next();
		Collection resultStructures = focus(structure,formulae,runtime);
		result.addAll(resultStructures);
	    }
	    bindings.update(var, new ValueExpression(result));
	}
	return null;
    }
}
