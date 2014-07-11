package tvla.jeannet.equationSystem;

import tvla.exceptions.*;
import tvla.core.*;
import tvla.core.assignments.*;
import tvla.predicates.*;
import tvla.formulae.*;
import tvla.jeannet.expressions.*;
import tvla.io.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.analysis.AnalysisStatus;

import java.io.*;
import java.util.*;

/** Fixpoint equation solver
 */
public class Fixpoint {

    protected RuntimeExpr runtime;
    protected int numberOfIterations;
    protected int maxWorkSetSize;
    protected int averageWorkSetSize;

    protected HashMap unprocessed; // Maps vertex id to TVSSet
    protected HashMap processed;   // Maps vertex id to TVSSet

    public static Fixpoint instance = new Fixpoint();

    public long analysisStartTime; // System.nanoTime() when the analysis started (when the load timer was stopped)
    public long stepStartTime;     // System.nanoTime() when the previous equation finished, or analysis started 

    /** Constructs and initializes a new engine.
     */
    public Fixpoint() {
	runtime = new RuntimeExpr(AnalysisStatus.getActiveStatus());
    }

    // ======================================================================
    // Some utility functions
    // ======================================================================
    /** Use the iterator on TVSs to add the corresponding TVSs to the
     * first set, and add the differences to the second set if it not
     * null. */
    protected static void joinTVS(TVSSet set, Iterator it, TVSSet deltaset){
	while (it.hasNext()){
	    HighLevelTVS structure = (HighLevelTVS)it.next();
	    HighLevelTVS delta = set.mergeWith(structure);
	    if (delta!=null && deltaset!=null){
		deltaset.mergeWith(delta);
	    }
	}
    }

    /** Use the iterator on TVSs to compute the difference between the
     * corresponding set and the first set, and adds the difference to
     * the last set if it is not null.

     * This could be implemented in a much better way if a function
     * subsume were available. */

    protected static void diffTVS(Iterator it, TVSSet set, TVSSet deltaset){
	TVSSet newSet = TVSFactory.getInstance().makeEmptySet();
	joinTVS(newSet, set.iterator(), null);
	while (it.hasNext()){
	    HighLevelTVS structure = (HighLevelTVS)it.next();
	    HighLevelTVS delta = newSet.mergeWith(structure);
	    if (delta!=null){
		deltaset.mergeWith(delta);
	    }
	}
    }



    /** Build a ValueExpression by extracting and making a copy of structures in the set. */
    protected static ValueExpression buildValueExpression(TVSSet set){
	Collection collection = new ArrayList(set.size());
	for (Iterator i=set.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    HighLevelTVS newstructure = (HighLevelTVS)structure.copy();
	    collection.add(newstructure);
	}
	return new ValueExpression(collection);
    }
    /** Build a ValueExpression by extracting and making a copy of structures in the two sets. */
    protected static ValueExpression buildValueExpression(TVSSet set1, TVSSet set2){
	Collection collection = new ArrayList(set1.size()+set2.size());
	for (Iterator i=set1.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    HighLevelTVS newstructure = (HighLevelTVS)structure.copy();
	    collection.add(newstructure);
	}
	for (Iterator i=set2.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    HighLevelTVS newstructure = (HighLevelTVS)structure.copy();
	    collection.add(newstructure);
	}
	return new ValueExpression(collection);
    }

    // ======================================================================
    // Evaluate the equation system, assuming it is a DAG
    // ======================================================================
    /** Evaluate the equation system, assuming it is a DAG; if we detect cycles
     *    we will exit with an error.
     * @author Jason Breck
     */
    private Set<Symbol> finishedNodes;
    public Map evalDAG(Map initial) {
        
	EquationGraph equationGraph = EquationGraph.instance;
	
	// Find strongly-connected components and do a topological sort
	Set roots = new TreeSet();
	for (Iterator i=initial.keySet().iterator(); i.hasNext(); ){
	    Symbol var = (Symbol)i.next();
	    roots.add(var);
	}
	List horder = EquationGraph.HierarchicalOrder.build(equationGraph.graph, roots);
	List scc = EquationGraph.HierarchicalOrder.linearize2(horder);	

        // No longer used; now we execute the equations in the "given" order-- the order in which the
        //   user wrote them in the TVP file.
        //if (runtime.status.debug){
        //    Logger.println("\nevalDAG Iteration order: " + EquationGraph.instance.componentToString(scc));
        //}
	    
	// Check that the result has no cycles
	// And that it's sortable alphabetically...
        TreeSet<String> vertexNames = new TreeSet<String>();
	for (Iterator itComponent = scc.iterator(); itComponent.hasNext(); ) {
            Object object = itComponent.next();
            Symbol vertex = null;
            if (object instanceof List) {
        	List cycle = (List) object;
        	if (cycle.size() == 1 && cycle.get(0) instanceof Symbol) {
        	    // JTB: This seems to happen in two cases:
        	    //   (1) self-loops (I think???)
        	    //   (2) multiple equations writing into to the same node
        	    vertex = (Symbol) cycle.get(0);
        	} else {
                    Logger.print("ERROR: Equation graph cycle found, while using Fixpoint.evalDAG.");
                    String cycleAsString = "[";
                    for(Iterator iCycle = cycle.iterator(); iCycle.hasNext(); ) {
                        cycleAsString += iCycle.next().toString();
                        if (iCycle.hasNext()) cycleAsString += " ";
                    }
                    cycleAsString += "]";
                    Logger.print("  The cycle is: " + cycleAsString);
                    throw new TVLAException("Equation graph cycle found while using Fixpoint.evalDAG.");
        	}
            } else if (object instanceof Symbol) {
                vertex = (Symbol)object;
            } else throw new TVLAException("Fixpoint.evalDAG: internal error 1\n");
            
            vertexNames.add(vertex.toString());

            Set succEdges = EquationGraph.instance.graph.getVertexSucc(vertex);
	    for (Iterator itEdge = succEdges.iterator(); itEdge.hasNext(); ) {
	        Integer edge = (Integer)itEdge.next();
                Equation eqn = EquationGraph.instance.getEquation(edge);
	        if (eqn.getResult() == vertex) {
		    throw new TVLAException("evalDAG was called on an equation system containing a self-loop at " + vertex.toString());
	        }
                // BEGIN section (1/2) that enforces the given order:
                int equationOrder = EquationGraph.instance.equationsInGivenOrder.indexOf(eqn);
                int vertexOrder   = EquationGraph.instance.resultsInGivenOrder.lastIndexOf(vertex);
                if (equationOrder != -1 && vertexOrder != -1 && (vertexOrder >= equationOrder)) {
                    throw new TVLAException("evalDAG found that a location depends on a later location: " +
                                            vertex.toString() + " is used in equation: " + 
                                            EquationGraph.instance.equationsInGivenOrder.get(vertexOrder).toString() +
                                            " but then it is written to later, in equation: " + eqn.toString() );
                }
                //if ( (!initial.containsKey(vertex)) &&
                //    ((vertex.toString().compareTo(eqn.getResult().toString())) >= 0)) {
                //    throw new TVLAException("evalDAG found that a location depends on an alphabetically-earlier-named location: " 
                //                            + vertex.toString() + " --> " + eqn.getResult().toString());
                //}
                //if (initial.containsKey(eqn.getResult())) {
                //    throw new TVLAException("evalDAG found an equation that writes output to an initial location (one in the TVS file): "
                //                            + eqn.getResult().toString());
                //}
                // END section
	    }
	}
	
        // 3. Initializes processed structures map
        Set vertexSet = equationGraph.graph.vertexSet();
        processed = new HashMap();
        for (Iterator i = vertexSet.iterator(); i.hasNext(); ){
            Symbol id = (Symbol)i.next();
            processed.put(id, TVSFactory.getInstance().makeEmptySet());
            // evalDAG() does not use the unprocessed structures map.
        }
        // Put initial values into the processed structures map
        finishedNodes = new TreeSet<Symbol>();
	for (Iterator i = initial.keySet().iterator(); i.hasNext(); ){
	    Symbol id = (Symbol)i.next();
            Location location = (Location)initial.get(id);
	    processed.put(id, location.structures);
	    runtime.status.numberOfStructures += location.structures.size();
	    finishedNodes.add(id);
	}
	// At this point, finishedNodes should be exactly the nodes whose structures were specified in the TVS file.
	
        AnalysisStatus.loadTimer.stop(); // newly added 2014_06_12  
        analysisStartTime = stepStartTime = System.nanoTime();

	Logger.println("\nAnalysis...");
	//runtime.status.startTimer(AnalysisStatus.TOTAL_ANALYSIS_TIME);
        //
        // ALTERNATIVE not enforcing alphabetical order is:
	//for (Iterator itComponent = scc.iterator(); itComponent.hasNext(); ){
	//    Object object = itComponent.next();
        //
        // BEGIN section (2/2) that enforces given order:
	//for (Iterator vnIter = vertexNames.iterator(); vnIter.hasNext(); ) {
        //   Object object = Symbol.ofString(vnIter.next().toString());
	for (Iterator resultsIter = EquationGraph.instance.resultsInGivenOrder.iterator(); resultsIter.hasNext(); ) {
           Object object = resultsIter.next();
        // END section
	   if (object instanceof Symbol){
	       // Process the corresponding vertex
	       // In evalDAG, this means we will now run the *input* functions
	       //   for this vertex.  That is in contrast to eval() below,
	       //   which processes a vertex by running all its *output* functions.
	       Symbol vertex = (Symbol)object;
               //Logger.println("\nProcessing a vertex...");
	       processVertexInputs(vertex);
	   }
	   else throw new TVLAException("Fixpoint.evalDAG: internal error 1\n");
	}
	Logger.println("\nAnalysis finished.");
	//runtime.status.stopTimer(AnalysisStatus.TOTAL_ANALYSIS_TIME);        
	
	// We add to saveVertices the nodes to be printed
	Set saveVertices = new TreeSet();
	if (equationGraph.toPrint!=null)
	    saveVertices.addAll(equationGraph.toPrint);
	// Create the resulting map
	Map result = new HashMap(processed.size());
	Iterator i = equationGraph.toPrint==null ? saveVertices.iterator() : equationGraph.toPrint.iterator();
	while (i.hasNext()){
	    Symbol var = (Symbol)i.next();
	    TVSSet structures = (TVSSet)processed.get(var);
	    Location loc = new Location(var,structures);
	    result.put(var,loc);
	}
	
	//Logger.println("Preparing statistics ...");
	//printStatistics();

	processed = null;
	unprocessed = null; // not used.
	
	return result;
    }
    
    // ======================================================================
    // Propagation of information *into* one vertex
    // ======================================================================
    /** Process a single vertex.  That is, run all the functions that are
     *    inputs to this vertex.  We assume that all the inputs have been
     *    processed completely.  We assume no self-loops.
     *  @author Jason Breck */
    private void processVertexInputs(Symbol vertex){
	
	//if (runtime.status.debug){
	    Logger.println("\nProcessing inputs to node " + vertex);
	//}

	// Iterates on the predecessor edges of the vertex and for each
	//   edge, propagate processed forward.
	Set prededges = EquationGraph.instance.graph.getVertexPred(vertex);
	for (Iterator itEdge = prededges.iterator(); itEdge.hasNext(); ){

	    long startTime = System.nanoTime();

	    if (finishedNodes.contains(vertex)) {
	        throw new TVLAException("processVertexInputs tried to write to input node " + vertex + " after that node was marked as 'finished'.");
	    }
	        
	    Integer edge = (Integer)itEdge.next();
	    Equation eqn = EquationGraph.instance.getEquation(edge);

	    List args = eqn.getArguments();
	    List actuals = new ArrayList(args.size());

	    runtime.stepNumber ++;
	    runtime.prefix = "Calculating input to Node " + vertex + "\nFunction '" + eqn.getFunction().getTitle() + "'";
	    //if (runtime.status.debug){
		Logger.println("    Evaluating function '" + eqn.getFunction().getTitle() + "'");
	    //}

	    // 1. Builds the actual parameters. As they can be modified by side-effects, perform duplication.
	    List<Integer> argSizes = new ArrayList<Integer>();
	    for (Iterator i=args.iterator(); i.hasNext(); ){
		Symbol arg = (Symbol)i.next();
		ValueExpression actual;
		
		if (!finishedNodes.contains(arg)) {
		    throw new TVLAException("processVertexInputs tried to read from input node " + arg + " before that node's structure set had been calculated.");
		}
		
	        if (arg == vertex) {
	            throw new TVLAException("processVertexInputs was called on an equation node with a self-loop.");
	        }
	        
	        argSizes.add(((TVSSet)processed.get(arg)).size());
		
		actual = buildValueExpression((TVSSet)processed.get(arg));
		actuals.add(actual);

		// Debugging information
		if (runtime.status.debug){
		    Collection structures = actual.getCollectionTVS();
		    if (structures.isEmpty()){
			IOFacade.instance().printStructure(TVS.EMPTY_TVS, runtime.prefix + "\nActual parameter " + arg + "\n=\nBOTTOM");
		    }
		    else {
			for (Iterator itStruct = structures.iterator(); itStruct.hasNext(); ){
			    HighLevelTVS structure = (HighLevelTVS)itStruct.next();
			    IOFacade.instance().printStructure(structure, runtime.prefix + "\nActual parameter " + arg + "\n=");
			}
		    }
		}
	    }
	    //if (runtime.status.debug){
	        Logger.print("      Its input TVSSet sizes are: " + argSizes);
	    //}    

	    //long startTime = System.nanoTime(); // I used to measure time from this point
	    
	    // 2. Evaluate the function with the actual parameters
	    Function func = eqn.getFunction();
	    ValueExpression resultV;
	    try {
		resultV = func.eval(actuals,runtime);
	    }
	    catch (AnalysisHaltException halt) {
	        /*
		HighLevelTVS structure = halt.getStructure();
		Assign assign = halt.getAssign();
		Formula condition = halt.getCondition();
		String msg =
		    runtime.prefix
		    + "\nHalt condition satisfied !\nCondition = " +  condition
		    + "\nAssignement = " + (assign.isEmpty() ? "empty" : "" + assign);

		Logger.println("\n" + msg);
		IOFacade.instance().printStructure(structure, msg);
		*/
	        Logger.println("Analysis Halted."); // FIXME Jason commented out code that change between jeannet&tvla3
		throw (halt);

	    }
	    
            // I moved the result-joining here, so that it's included in the timing
	    Collection result = resultV.getCollectionTVS();
            TVSSet processedResult = (TVSSet)processed.get(vertex);
            joinTVS(processedResult, result.iterator(), null);

	    //  Debugging information
	    if (runtime.status.debug){
	        //Logger.println("    (Time taken = " + String.format("%.4f", (System.nanoTime() - startTime)/1000000000.0 )+ " sec)");
	        
		if (result.isEmpty()){
		    IOFacade.instance().printStructure(TVS.EMPTY_TVS, runtime.prefix + "\nResult (node " + vertex + ")\n=\nBOTTOM");
		}
		else {
		    for (Iterator itStruct = result.iterator(); itStruct.hasNext(); ){
			HighLevelTVS structure = (HighLevelTVS)itStruct.next();
			IOFacade.instance().printStructure(structure, runtime.prefix + "\nResult (node " + vertex + ")\n=");
		    }
		}
	    }
           
            // Timing ends here
	    //Logger.println("    (@" + 
            //               vertex.toString() + 
            //               " Time taken = " + String.format("%.4f", (System.nanoTime() - startTime)/1000000000.0 )+ " sec)");
	    //Logger.println("    (@" + 
            //               vertex.toString() + 
            //               " Time taken = " + String.format("%.4f", (newTime - startTime)/1000000000.0 )+ " sec)");
            long newTime = System.nanoTime(); // The next step's timing begins here
	    Logger.println("    (@" + 
                           vertex.toString() + 
                           " Time taken = " + String.format("%.4f", (newTime - stepStartTime)/1000000000.0 )+ " sec " + 
                           " Cumul = " + String.format("%.9f", (newTime - analysisStartTime)/1000000000.0 )+ " sec" + 
                           ")");
            stepStartTime = newTime; 

            // I used to join in the outputs here,
            //    but that put the joining after the timer.
            //TVSSet processedResult = (TVSSet)processed.get(vertex);
            //joinTVS(processedResult, result.iterator(), null);
            
	    
	} // End of iterations on predecessor edges
	
        //if (runtime.status.debug){
            Logger.println("  Finished " + vertex + "; it has " + ((TVSSet)processed.get(vertex)).size() + " structures.");
        //}
        
        finishedNodes.add(vertex); 
	
	// End of processVertexInputs
    }
    
    
    // ======================================================================
    // Global fixpoint iteration
    // ======================================================================
    /** Performs the fixpoint computations. The input and output maps
     * are from Var to Location.
     */
    public Map eval(Map initial){
        
	//EquationGraph equationGraph = EquationGraph.instance;

	//// 1. Perform SCSC
	//Set roots = new TreeSet();
	//for (Iterator i=initial.keySet().iterator(); i.hasNext(); ){
	//    Symbol var = (Symbol)i.next();
	//    roots.add(var);
	//}
	//List horder = EquationGraph.HierarchicalOrder.build(equationGraph.graph, roots);
	//List scc = EquationGraph.HierarchicalOrder.linearize2(horder);

	//if (runtime.status.debug){
	//	Logger.println("\nIteration order: " + EquationGraph.instance.componentToString(scc));
	//}

	//// 2. Compute saveVertices
	//Set saveVertices = EquationGraph.HierarchicalOrder.getWideningVertices(horder);

	//// We add to saveVertices the nodes to be printed
	//if (equationGraph.toPrint!=null)
	//    saveVertices.addAll(equationGraph.toPrint);
	//// We have also to add to saveVertices all nodes used in n-ary functions
	//for (Iterator i = equationGraph.graph.edgeSet().iterator(); i.hasNext(); ){
	//    Integer idEdge = (Integer)i.next();
	//    Set predEdge = equationGraph.graph.getEdgePred(idEdge);
	//    if (predEdge.size()>1){
	//	saveVertices.addAll(predEdge);
	//    }
	//}

	//if (runtime.status.debug){
	//	Logger.print("\nSaved nodes: ");
	//	for (Iterator i=saveVertices.iterator(); i.hasNext(); ){
	//	    Symbol id = (Symbol)i.next();
	//	    Logger.print(id + " ");
	//	}
	//	Logger.println("");
	//}
	//// 3. Initializes unprocessed and processed stuff
	//Set vertexSet = equationGraph.graph.vertexSet();
	//unprocessed = new HashMap(vertexSet.size());
	//processed = new HashMap(saveVertices.size());
	//for (Iterator i = vertexSet.iterator(); i.hasNext(); ){
	//    Symbol id = (Symbol)i.next();
	//    unprocessed.put(id, TVSFactory.getInstance().makeEmptySet());
	//    if (saveVertices.contains(id)){
	//	processed.put(id, TVSFactory.getInstance().makeEmptySet());
	//    }
	//}

	//// 4. Put initial values
	//for (Iterator i = initial.keySet().iterator(); i.hasNext(); ){
	//    Symbol id = (Symbol)i.next();
	//    Location location = (Location)initial.get(id);
	//    unprocessed.put(id, location.structures);
	//    runtime.status.numberOfStructures += location.structures.size();
	//}

	//// 5. Fixpoint

	//Logger.println("\nAnalysis...");
	//runtime.status.startTimer(AnalysisStatus.TOTAL_ANALYSIS_TIME);
	//numberOfIterations = 0;

	//for (Iterator itComponent = scc.iterator(); itComponent.hasNext(); ){
	//   Object object = itComponent.next();
	//   if (object instanceof Symbol){
	//       // Process the corresponding vertex
	//       Symbol vertex = (Symbol)object;
	//       if (((TVSSet)unprocessed.get(vertex)).size()==0)
	//	   continue;
	//       else
	//	   processVertex(vertex);
	//   }
	//   else if (object instanceof List){
	//       List component = (List)object;
	//       // Process the component up to stabilization
	//       boolean stable = false;
	//       while (! stable){
	//	   numberOfIterations++;
	//	   stable = true;
	//	   for (Iterator it = component.iterator(); it.hasNext(); ){
	//	       Symbol vertex = (Symbol)it.next();
	//	       if (((TVSSet)unprocessed.get(vertex)).size()==0)
	//		   continue;
	//	       else if (processed.containsKey(vertex))
	//		   stable = false;

	//	       // We process here the vertex
	//	       processVertex(vertex);
	//	   }
	//       }
	//   }
	//   else
	//       throw new TVLAException("Fixpoint.eval: internal error 1\n");
	//}
	//Logger.println("\nAnalysis finished.");
	//runtime.status.stopTimer(AnalysisStatus.TOTAL_ANALYSIS_TIME);

	//// Create the resulting map
	//Map result = new HashMap(processed.size());
	//Iterator i = equationGraph.toPrint==null ? saveVertices.iterator() : equationGraph.toPrint.iterator();
	//while (i.hasNext()){
	//    Symbol var = (Symbol)i.next();
	//    TVSSet structures = (TVSSet)processed.get(var);
	//    Location loc = new Location(var,structures);
	//    result.put(var,loc);
	//}

	//Logger.println("Preparing statistics ...");
	//printStatistics();

	//processed = null;
	//unprocessed = null;

	//return result;
    return null;
    }

    // ======================================================================
    // Forward propagation of information associated to one vertex
    // ======================================================================
    /** Process a single vertex.  Propagate "unprocessed" TVS to
	successors.  For n-ary function, "unprocessed" TVS of the considered
	vertex are combined with both "unprocessed and processed" stuff of
	other vertices */
    private void processVertex(Symbol vertex){
	if (runtime.status.debug){
	    int size_unprocessed = ((TVSSet)unprocessed.get(vertex)).size();
	    int size_processed = 0;
	    if (processed.containsKey(vertex)){
		size_processed = ((TVSSet)processed.get(vertex)).size();
	    }
	    Logger.println("\nProcessing node " + vertex + "  (" + size_processed + " processed, " + size_unprocessed + " unprocessed)");
	}

	// This fields are used whenever the vertex can be both argument and result of the same function
	TVSSet newUnprocessed = null;
	TVSSet newProcessed = null;

	// Iterates on the successor edges of the vertexm and for each
	// edge, propagate unprocessed forward
	Set succedges = EquationGraph.instance.graph.getVertexSucc(vertex);
	for (Iterator itEdge = succedges.iterator(); itEdge.hasNext(); ){
	    Integer edge = (Integer)itEdge.next();

	    Equation eqn = EquationGraph.instance.getEquation(edge);
	    List args = eqn.getArguments();
	    List actuals = new ArrayList(args.size());

	    runtime.stepNumber ++;
	    runtime.prefix = "Node " + vertex + "\nFunction " + eqn.getFunction().getTitle() + "\nStep " + runtime.stepNumber;
	    if (runtime.status.debug){
		Logger.println("Function " + eqn.getFunction().getTitle() + "\t Step " + runtime.stepNumber);
	    }

	    // 1. Builds the actual parameters. As they can be
	    // modified by side-effects, perform duplication.
	    for (Iterator i=args.iterator(); i.hasNext(); ){
		Symbol arg = (Symbol)i.next();
		ValueExpression actual;
		if (arg.equals(vertex)){
		    // Takes only unprocessed.
		    actual = buildValueExpression((TVSSet)unprocessed.get(arg));
		} else {
		    // Takes both unprocessed and processed. We know
		    // that processed exists, because we have here
		    // n-ary function
		    actual = buildValueExpression((TVSSet)processed.get(arg),(TVSSet)unprocessed.get(arg));
		}
		actuals.add(actual);

		// Debugging information
		if (runtime.status.debug){
		    Collection structures = actual.getCollectionTVS();
		    if (structures.isEmpty()){
			IOFacade.instance().printStructure(TVS.EMPTY_TVS, runtime.prefix + "\nActual parameter " + arg + "\n=\nBOTTOM");
		    }
		    else {
			for (Iterator itStruct = structures.iterator(); itStruct.hasNext(); ){
			    HighLevelTVS structure = (HighLevelTVS)itStruct.next();
			    IOFacade.instance().printStructure(structure, runtime.prefix + "\nActual parameter " + arg + "\n=");
			}
		    }
		}
	    }

	    // 2. Evaluate the function with the actual parameters
	    Function func = eqn.getFunction();
	    ValueExpression resultV;
	    try {
		resultV = func.eval(actuals,runtime);
	    }
	    catch (AnalysisHaltException halt) {
	        /*
		HighLevelTVS structure = halt.getStructure();
		Assign assign = halt.getAssign();
		Formula condition = halt.getCondition();
		String msg =
		    runtime.prefix
		    + "\nHalt condition satisfied !\nCondition = " +  condition
		    + "\nAssignement = " + (assign.isEmpty() ? "empty" : "" + assign);

		Logger.println("\n" + msg);
		IOFacade.instance().printStructure(structure, msg);
		*/
		Logger.println("Analysis Halted."); // FIXME Jason commented out code that change between jeannet&tvla3
		throw (halt);

	    }
	    Collection result = resultV.getCollectionTVS();
	    Symbol idResult = eqn.getResult();

	    //  Debugging information
	    if (runtime.status.debug){
		if (result.isEmpty()){
		    IOFacade.instance().printStructure(TVS.EMPTY_TVS, runtime.prefix + "\nResult (node " + idResult + ")\n=\nBOTTOM");
		}
		else {
		    for (Iterator itStruct = result.iterator(); itStruct.hasNext(); ){
			HighLevelTVS structure = (HighLevelTVS)itStruct.next();
			IOFacade.instance().printStructure(structure, runtime.prefix + "\nResult (node " + idResult + ")\n=");
		    }
		}
	    }

	    // 3. Store the result
	    TVSSet unprocessedResult = (TVSSet)unprocessed.get(idResult);
	    if (! vertex.equals(idResult)){
		// 3.1. Normal case: resulting location != location being processed
		if (processed.containsKey(idResult)){
		    // Add to unprocessed the difference between result and processed
		    TVSSet processedResult = (TVSSet)processed.get(idResult);
		    diffTVS(result.iterator(), processedResult, unprocessedResult);
		} else {
		    // Just add to unprocessed set
		    joinTVS(unprocessedResult, result.iterator(), null);
		}
	    }
	    else {
		// 3.2. BE CAUTIOUS: resulting location = location being processed !
		// init newUnprocessed and Processed if necessary
		if (newUnprocessed==null)
		    newUnprocessed = TVSFactory.getInstance().makeEmptySet();
		if (processed.containsKey(idResult)){
		    // Save location
		    if (newProcessed==null){
			newProcessed = TVSFactory.getInstance().makeEmptySet();
			TVSSet processedResult = (TVSSet)processed.get(idResult);
			joinTVS(newProcessed, processedResult.iterator(), null);
			joinTVS(newProcessed, unprocessedResult.iterator(), null);
		    }
		    // Add to newUnprocessed the difference between result and newProcessed
		    diffTVS(result.iterator(), newProcessed, newUnprocessed);
		} else {
		    // Just add to unprocessed set
		    joinTVS(newUnprocessed,result.iterator(), null);
		}
	    }
	    // End of iterations on successor edges
	}

	// Update information attached to current location
	if (newUnprocessed == null){
	    // If this location hasn't been a destination, normal treatment
	    TVSSet unprocessedVertex = (TVSSet)unprocessed.get(vertex);
	    if (processed.containsKey(vertex)){
		TVSSet processedVertex = (TVSSet)processed.get(vertex);
		/*
		IOFacade.instance().printStructure(TVS.EMPTY_TVS,"before 2");
		for (Iterator it = processedVertex.iterator(); it.hasNext(); ){
		    IOFacade.instance().printStructure(it.next(), "processedVertex");
		}
		for (Iterator it = unprocessedVertex.iterator(); it.hasNext(); ){
		    IOFacade.instance().printStructure(it.next(), "unprocessedVertex");
		}
		*/
		joinTVS(processedVertex, unprocessedVertex.iterator(), null);
	    }
	    unprocessed.put(vertex,TVSFactory.getInstance().makeEmptySet());
	} else {
	    // This location has been a destination
	    if (newProcessed != null){
		processed.put(vertex, newProcessed);
	    }
	    unprocessed.put(vertex,newUnprocessed);
	}
    }

    /** Prints statistica gathered during the analysis to the log stream.
     * @author Roman Manevich.
     * @since 21.7.2001
     */
    protected void printStatistics() {
	if (numberOfIterations != 0)
	    averageWorkSetSize = averageWorkSetSize / numberOfIterations;
	int numNullaryAbsPredicates = 0;
	int numUnaryAbsPredicates = 0;
	for (Iterator i = Vocabulary.allNullaryPredicates().iterator(); i.hasNext(); ) {
	    Predicate predicate = (Predicate) i.next();
	    if (predicate.abstraction())
		++numNullaryAbsPredicates;
	}
	for (Iterator i = Vocabulary.allUnaryPredicates().iterator(); i.hasNext(); ) {
	    Predicate predicate = (Predicate) i.next();
	    if (predicate.abstraction())
		++numUnaryAbsPredicates;
	}

	Logger.println();
	Logger.println("max work set            : " + maxWorkSetSize);
	Logger.println("average work set        : " + averageWorkSetSize);
	Logger.println("#iterations             : " + numberOfIterations);

	Logger.println("#locations              : " + EquationGraph.instance.graph.sizeVertex());
	Logger.println("#functions              : " + EquationGraph.instance.graph.sizeEdge());
	Logger.println("#predicates             : " + Vocabulary.size());
	Logger.println("#nullary predicates     : " + Vocabulary.allNullaryPredicates().size());
	Logger.println("#nullary abs predicates : " + numNullaryAbsPredicates);
	Logger.println("#unary predicates       : " + Vocabulary.allUnaryPredicates().size());
	Logger.println("#unary abs predicates   : " + numUnaryAbsPredicates);
	Logger.println("#binary predicates      : " + Vocabulary.allBinaryPredicates().size());
	Logger.println("#constraints            : " + Constraints.getInstance().size());

	runtime.status.exhaustiveGC();
	runtime.status.updateStatus();
	runtime.status.printStatistics();
    }

}
