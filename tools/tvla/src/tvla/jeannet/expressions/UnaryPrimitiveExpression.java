package tvla.jeannet.expressions;

import java.util.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.core.*;
import tvla.core.generic.*;
import tvla.io.*;
import tvla.analysis.AnalysisStatus;
import tvla.analysis.Engine;


/** A class for all primitive having as a single parameter a variable on
 * which they act
 * @author Bertrand Jeannet
 */

public class UnaryPrimitiveExpression implements Expression {
    static public final int COPY = 0;
    static public final int BLUR = 1;
    static public final int JOIN = 2;
    static public final int EMBEDBLUR = 3;
    static public final int COERCE = 4;
    // static public final int POP = 5;
    // static public final int TOCONSTRAINTS = 6;

    private int type;
    private Symbol var;

    public UnaryPrimitiveExpression(int type, Symbol var){
	this.type = type;
	this.var = var;
    }

    /** Does the expression has a value ? */
    public boolean hasValue(){
	switch (this.type){
	case COPY:
	    return true;
	default:
	    return false;
	}
    }

    /** Converts to a string. */
    public String toString(){
	StringBuffer result = new StringBuffer();
	switch (this.type){
	case COPY:
	    result.append("copy"); break;
	case BLUR:
	    result.append("blur"); break;
	case JOIN:
	    result.append("join"); break;
	case EMBEDBLUR:
	    result.append("embedblur"); break;
	case COERCE:
	    result.append("coerce"); break;
	    /*
	case POP:
	    result.append("pop"); break;
	case TOCONSTRAINTS:
	    result.append("toconstraints"); break;
	    */
	}
	result.append("(" + var.toString() + ")");
	return result.toString();
    }

    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime){
	runtime.primitiveNumber++;

	ValueExpression result = null;
	switch (this.type){
	case COPY:
	    result = copyPrimitive(bindings, runtime);
	    break;
	case BLUR:
	    blur(bindings, runtime);
	    result = null;
	    break;
	case JOIN:
	    join(bindings, runtime);
	    result = null;
	    break;
	case EMBEDBLUR:
	    embedBlur(bindings, runtime);
	    result = null;
	    break;
	case COERCE:
	    coerce(bindings, runtime);
	    result = null;
	    break;
	    /*
	case POP:
	    pop(bindings, runtime);
	    result = null;
	    break;
	case TOCONSTRAINTS:
	    toConstraints(bindings, runtime);
	    result = null;
	    */
	}
	return result;
    }

    private final ValueExpression copyPrimitive(BindingsExpr bindings, RuntimeExpr runtime) {
	ValueExpression value = bindings.lookup(var);
	Collection collection = value.getCollectionTVS();
	Collection result = new ArrayList(collection.size());
	for (Iterator i = collection.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    HighLevelTVS newstructure = (HighLevelTVS)structure.copy();
	    result.add(newstructure);
	}
	return new ValueExpression(result);
    }

    private final void blur(BindingsExpr bindings, RuntimeExpr runtime){
	ValueExpression value = bindings.lookup(var);
	Collection collection = value.getCollectionTVS();
	for (Iterator i = collection.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    if (runtime.status.debugstep) {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nExecuting Blur");
	    }
	    //runtime.status.startTimer(AnalysisStatus.BLUR_TIME);
	    Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.BLUR_TIME);
        structure.blur();
	    //runtime.status.stopTimer(AnalysisStatus.BLUR_TIME);
        Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.BLUR_TIME);
	    if (runtime.status.debugstep) {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nAfter Blur");
	    }
	}
    }

    private final void join(BindingsExpr bindings, RuntimeExpr runtime)
    {
	ValueExpression value = bindings.lookup(var);
	Collection collection = value.getCollectionTVS();
	TVSSet newSet = TVSFactory.getInstance().makeEmptySet();
	for (Iterator i = collection.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    // Add to the collection
	    //runtime.status.startTimer(AnalysisStatus.JOIN_TIME);
	    Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.JOIN_TIME);
        newSet.mergeWith(structure);
	    Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.JOIN_TIME);
        //runtime.status.stopTimer(AnalysisStatus.JOIN_TIME);
	}
	Collection result = new ArrayList(newSet.size());
	for (Iterator i = newSet.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    result.add(structure);
	}
	bindings.update(var,new ValueExpression(result));
    }

    private final void embedBlur(BindingsExpr bindings, RuntimeExpr runtime){
	ValueExpression value = bindings.lookup(var);
	Collection collection = value.getCollectionTVS();
	for (Iterator i = collection.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    if (runtime.status.debugstep) {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nExecuting EmbedBlur");
	    }
	    //runtime.status.startTimer(AnalysisStatus.BLUR_TIME);
	    Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.BLUR_TIME);
        EmbeddingBlur.defaultEmbeddingBlur.blur(structure);
	    Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.BLUR_TIME);
        //runtime.status.stopTimer(AnalysisStatus.BLUR_TIME);
	    if (runtime.status.debugstep) {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nAfter EmbedBlur ");
	    }
	}
    }

    private final void coerce(BindingsExpr bindings, RuntimeExpr runtime){
	//System.err.print("Starting coerce..."); // ADDED BY JASON
	ValueExpression value = bindings.lookup(var);
	Collection collection = value.getCollectionTVS();
	for (Iterator i = collection.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    if (runtime.status.debugstep) {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nExecuting Coerce");
	    }

	    //runtime.status.startTimer(AnalysisStatus.COERCE_TIME);
	    Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.COERCE_TIME);
        boolean valid = structure.coerce();
	    Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.COERCE_TIME);
        //runtime.status.stopTimer(AnalysisStatus.COERCE_TIME);
	    if (valid){
		if (runtime.status.debugstep)
		    IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nAfter Coerce");
	    }
	    else {
		// Remove from the collection
		i.remove();
	    }
	}
	//System.err.println("done with coerce!"); // ADDED BY JASON
    }

    /*
    private final void pop(BindingsExpr bindings, RuntimeExpr runtime){
	ValueExpression value = bindings.lookup(var);
	Collection collection = value.getCollectionTVS();
	for (Iterator i = collection.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    FuncStack stack = structure.stackConstraints;
	    FuncStack newstack = FuncStack.pop(stack);
	    structure.stackConstraints = newstack;
	}
    }

    private final void toConstraints(BindingsExpr bindings, RuntimeExpr runtime){
	ValueExpression value = bindings.lookup(var);
	Collection collection = value.getCollectionTVS();
	Collection result = new ArrayList(collection.size());
	for (Iterator i = collection.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    if (runtime.status.debugstep) {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nExecuting toconstraints (EmbedBlur)");
	    }
	    //runtime.status.startTimer(AnalysisStatus.BLUR_TIME);
	    EmbeddingBlur.defaultEmbeddingBlur.blur(structure);
	    //runtime.status.stopTimer(AnalysisStatus.BLUR_TIME);
	    if (runtime.status.debugstep) {
		IOFacade.instance().printStructure(structure, runtime.prefix + ", Primitive " + runtime.primitiveNumber + "\nAfter toconstraints (EmbedBlur)");
	    }
	    Collection constraints = StructureToConstraints.transform(structure);
	    result.add(constraints);
	}
	bindings.update(var,new ValueExpression(result));
    }
    */
}
