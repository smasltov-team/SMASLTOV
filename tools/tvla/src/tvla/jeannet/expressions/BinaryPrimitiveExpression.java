package tvla.jeannet.expressions;

import java.util.*;

import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.core.*;
import tvla.core.generic.*;
import tvla.jeannet.equationSystem.Function;
import tvla.io.*;

import tvla.core.meet.Meet; // changed by Jason Breck 2014-04
import tvla.analysis.AnalysisStatus;
import tvla.analysis.Engine;

/** A class for all primitive having as parameters two variables on
 * which they act
 * @author Bertrand Jeannet
 * @author Jason Breck (added joinwith, diff_over, diff_under)
 */

public class BinaryPrimitiveExpression implements Expression {
    static public final int MEETWITH = 0;
    // The below three were added by Jason Breck:
    static public final int JOINWITH = 1;
    static public final int DIFF_OVER = 2;  // overapproximating  set difference
    static public final int DIFF_UNDER = 3; // underapproximating set difference

    private int type;
    private Symbol var;
    private Symbol var2;
    
    public BinaryPrimitiveExpression(int type, Symbol var, Symbol var2){
	this.type = type;
	this.var = var;
	this.var2 = var2;
    }

    /** Does the expression have a value ? */
    public boolean hasValue(){
	boolean result = false;
	switch (this.type){
	case MEETWITH:
	    result=false;
	    break;
	case JOINWITH:
	    result=false;
	    break;
        case DIFF_OVER:
            result=true;
            break;
        case DIFF_UNDER:
            result=true;
            break;
        }
	return result;
    }

    /** Converts to a string. */
    public String toString(){
	StringBuffer result = new StringBuffer();
	switch (this.type){
	case MEETWITH:
	    result.append("meetwith");
	    break;
	case JOINWITH:
	    result.append("joinwith");
	    break;
        case DIFF_OVER:
            result.append("diff_over");
            break;
        case DIFF_UNDER:
            result.append("diff_under");
            break;
        }
	result.append("(" + var.toString() + ")");
	return result.toString();
    }
    
    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime){
	runtime.primitiveNumber++;

	ValueExpression result = null;
	switch (this.type){
	case MEETWITH:
	    meetwithPrimitive(bindings, runtime);
	    result = null;
	    break;
	case JOINWITH:
	    joinWith(bindings,runtime);
	    result = null;
	    break;
	case DIFF_OVER:
	    result = diffOver(bindings, runtime);
	    break;
	case DIFF_UNDER:
	    result = diffOver(bindings, runtime);
	    break;
	/*
        case DIFF_OVER:
        case DIFF_UNDER:
            if (Function.currentlyRunningFunctionEvaluationCount == 0) {
        	System.err.println("ERROR: " + this.toString() + " was called when Function.currentlyRunningFunctionEvaluationCount was zero.");
        	System.err.println("  See the source code for tvla.equationSystem.Function for an explanation. -- Jason Breck");
        	throw new RuntimeException();
            }
            if (Function.currentlyRunningFunctionEvaluationCount == 1) {
        	System.err.println("NOTICE: " + this.toString() + " is returning bottom because this is the first time it was called.");
        	return new ValueExpression(new ArrayList()); //
            }
            if (Function.currentlyRunningFunctionEvaluationCount == 2) {
        	System.err.println("NOTICE: " + this.toString() + " is now actually doing its computation, because this is the second time it was called.");
        	// now proceed to call diffOver() or diffUnder()
            }
            if (Function.currentlyRunningFunctionEvaluationCount > 2) {
        	System.err.println("ERROR: " + this.toString() + " was called when Function.currentlyRunningFunctionEvaluationCount was > 2.");
        	System.err.println("  See the source code for tvla.equationSystem.Function for an explanation. -- Jason Breck");
        	throw new RuntimeException();
            }
            if (this.type == DIFF_OVER) {
        	result = diffOver(bindings, runtime);
            }
            if (this.type == DIFF_UNDER) {
        	result = diffUnder(bindings, runtime);
            }
            break;
        */
        }
        
	return result;
    }

    private final void meetwithPrimitive(BindingsExpr bindings, RuntimeExpr runtime) {
	Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.MEET_TIME);
    TVSSet set = bindings.lookup(var).getGenericTVSSet();
	TVSSet set2 = bindings.lookup(var2).getGenericTVSSet();
    //System.out.println("Meetwith called");

	/*
	  System.out.println("MeetWith(" + var.toString() + "," + var2.toString() + ")");
	  bindings.dump();
	  System.out.println (">> set.size()=" + set.size());	
	  System.out.println (">> set2.size()=" + set2.size());	
	*/
	//System.err.print("Starting meet...");
	//set.meetWith(set2,runtime.status);
	  // changed by Jason Breck 2014-04
	
	//Iterator i = set.iterator();
	//Iterator j = set2.iterator();
	ArrayList<HighLevelTVS> resultSet = new ArrayList<HighLevelTVS>();
	Iterable<HighLevelTVS> resultIterator = Meet.meet(set, set2);
	//Iterable<HighLevelTVS> resultIterator = Meet.meet((Iterable<HighLevelTVS>) i, (Iterable<HighLevelTVS>) j);
	for (Iterator k = resultIterator.iterator(); k.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)k.next();
        //System.out.println("Saving a structure...");
	    resultSet.add(structure);
	}
	//System.err.println("done with meet!");
	
	//System.out.println (">> set.size()=" + set.size());	
	//bindings.update(var, new ValueExpression(set));
	bindings.update(var, new ValueExpression(resultSet));
	//bindings.dump();
    Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.MEET_TIME);
    }
    
    
    private final void joinWith(BindingsExpr bindings, RuntimeExpr runtime) {
    Engine.activeEngine.getAnalysisStatus().startTimer(AnalysisStatus.JOIN_TIME);
	Collection set1 = bindings.lookup(var).getCollectionTVS();
	Collection set2 = bindings.lookup(var2).getCollectionTVS();

	//GenericTVSSet joinResult = new tvla.core.generic.GenericTVSSet();
	TVSSet joinResult = TVSFactory.getInstance().makeEmptySet();

	// Perform the join
	for (Iterator i = set1.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    joinResult.mergeWith(structure);
	}
	for (Iterator i = set2.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    joinResult.mergeWith(structure);
	}
	
	// Now, convert the result back to a collection of HighLevelTVS 
	Collection<HighLevelTVS> finalResult = new ArrayList<HighLevelTVS>(joinResult.size());
	for (Iterator i = joinResult.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    finalResult.add(structure);
	    //HighLevelTVS newstructure = (HighLevelTVS)structure.copy();
	    //finalResult.add(newstructure);
	}
	
	// Update our left operand to contain the result
	bindings.update(var, new ValueExpression(finalResult));
    Engine.activeEngine.getAnalysisStatus().stopTimer(AnalysisStatus.JOIN_TIME);
    } 
    
    
    private final ValueExpression diffOver(BindingsExpr bindings, RuntimeExpr runtime) {
        //throw new RuntimeException("diffOver() feature not implemented");
	
	// Rule: throw out only the structures where lhs embeds in some rhs
	Collection lhs = bindings.lookup(var).getCollectionTVS();
	Collection rhs = bindings.lookup(var2).getCollectionTVS();
	Collection resultSet = new ArrayList();
	final boolean debugPrint = runtime.status.debug;
	int n = lhs.size();
	int i = 0;
	int rej = 0;
	tvla.io.StructureToTVS outputHelperTVS = null;
	if (debugPrint) {
	    System.out.print("\n" + i + "/" + n);
	    outputHelperTVS =  tvla.io.StructureToTVS.defaultInstance;
	}
	for (Iterator iLhs = lhs.iterator(); iLhs.hasNext(); ){
	    TVS lhsStructureOriginal = ( (TVS) iLhs.next() );
	    boolean keepLhs = true;
	    for (Iterator iRhs = rhs.iterator(); iRhs.hasNext(); ){
		TVS rhsStructureOriginal = ( (TVS) iRhs.next() );
		//if (GenericTVSSet.isEmbedded(lhsStructureOriginal,rhsStructureOriginal)) { 
		if (Meet.isEmbedded(lhsStructureOriginal,rhsStructureOriginal)) { // changed by Jason Breck 2014-04
		    rej++;
		    keepLhs = false;
		    if (debugPrint) {
		        /*
			System.out.println("\nDiffOver Rejected structure: ");
			System.out.println(outputHelperTVS.convert(lhsStructureOriginal));
			System.out.println("");
			*/
		    }
		    break;
		}
	    }
	    if (keepLhs) {
		resultSet.add(lhsStructureOriginal.copy());
	    }
	    if (debugPrint) {
		i++;
		System.out.print("\r" + i + "/" + n);
	    }
	}	
	if (debugPrint) { 
	    System.out.print("\n");
	    System.out.println("DiffOver Rejected " + rej + "/" + n);
	}
	return new ValueExpression(resultSet);	
    }
    
    private final ValueExpression diffUnder(BindingsExpr bindings, RuntimeExpr runtime) {
        throw new RuntimeException("diffUnder() feature not implemented"); // changed by Jason Breck 2014-04
//	//System.out.println("Feature not implemented (diff_under) !");
//	//throw new RuntimeException();
//	//TVSSet resultSet = new tvla.core.generic.GenericTVSSet(); // empty.  now that's what I call an underapproximation!
//	
//	// Rule: keep only the structures from the LHS whose meet with every structure from the RHS is bottom.
//	Collection lhs = bindings.lookup(var).getCollectionTVS();
//	Collection rhs = bindings.lookup(var2).getCollectionTVS();
//	Collection resultSet = new ArrayList();
//	final boolean debugPrint = runtime.status.debug;
//	int n = lhs.size();
//	int i = 0;
//	int rej = 0;
//	tvla.io.StructureToTVS outputHelperTVS = null;
//	if (debugPrint) {
//	    System.out.print("\n" + i + "/" + n);
//	    outputHelperTVS =  tvla.io.StructureToTVS.defaultInstance;
//	}
//	for (Iterator iLhs = lhs.iterator(); iLhs.hasNext(); ){
//	    TVS lhsStructureOriginal = ( (TVS) iLhs.next() );
//	    boolean keepLhs = true;
//	    for (Iterator iRhs = rhs.iterator(); iRhs.hasNext(); ){
//		TVS rhsStructureCopy = ( (TVS) iRhs.next() ).copy();
//		GenericTVSSet rhsSet = new tvla.core.generic.GenericTVSSet();
//		rhsSet.joinWith( rhsStructureCopy );
//		TVS lhsStructureCopy = lhsStructureOriginal.copy();
//		GenericTVSSet pairwiseResult = new tvla.core.generic.GenericTVSSet();
//		pairwiseResult.joinWith( lhsStructureCopy );
//		pairwiseResult.meetWith( rhsSet, runtime.status );
//		// Could add a coerce check here.
//		if (pairwiseResult.size() != 0) {
//		    rej++;
//		    keepLhs = false;
//		    if (debugPrint) {
//		        /*
//			System.out.println("\nDiffUnder Rejected structure: ");
//			System.out.println(outputHelperTVS.convert(lhsStructureCopy));
//			System.out.println("");
//			*/
//		    }
//		    break;
//		}
//	    }
//	    if (keepLhs) {
//		resultSet.add(lhsStructureOriginal.copy());
//	    }
//	    if (debugPrint) {
//		i++;
//		System.out.print("\r" + i + "/" + n);
//	    }
//	}	
//	if (debugPrint) { 
//	    System.out.print("\n");
//	    System.out.println("DiffUnder Rejected " + rej + "/" + n);
//	}
//	return new ValueExpression(resultSet);
    }
    
    private final Collection<HighLevelTVS> copyCollectionTVS(Collection set) {
	Collection<HighLevelTVS> result = new ArrayList<HighLevelTVS>(set.size());
	for (Iterator i = set.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    HighLevelTVS newstructure = (HighLevelTVS)structure.copy();
	    result.add(newstructure);
	}
	return result;
    }
}
