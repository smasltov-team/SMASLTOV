package tvla.jeannet.equationSystem;

import tvla.io.*;
import tvla.core.*;
import tvla.exceptions.*;
import tvla.jeannet.expressions.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.jeannet.util.HGraph;
import java.util.*;

/** Class for the equation system.
    The class contains a static instance which is initialized
    directly during the parsing.
*/

public class EquationGraph {
    /** A (hyper)graph where vertex identifiers are of type Symbol,
	vertex attributes are null, edge identifiers are of type
	Integer and attributes associated to hyperedges are of type
	Equation */
    public HGraph graph = new HGraph();
    private int maxIdEdge = 0;      // First free edge identifier
    public List toPrint = null; // List of vertices (Symbol) to be printed, in that order

    public static EquationGraph instance; // Initialized by tvla.language.TVP.TVP.cup

    public List<Equation> equationsInGivenOrder = new ArrayList<Equation>(); // added by Jason Breck
    public List<Symbol> resultsInGivenOrder = new ArrayList<Symbol>();       // added by Jason Breck

    // ======================================================================
    // We define some wrappers
    // ======================================================================
    /** Add an equation to the graph. */
    public void addEquation(Equation eqn){

    equationsInGivenOrder.add(eqn);
    resultsInGivenOrder.add(eqn.getResult());

	Integer idEqn = new Integer(maxIdEdge++);
	Symbol result = eqn.getResult();
	List args = eqn.getArguments();

	// Add the result vertex if necessary
	if (! graph.containsVertex(result)){
	    graph.addVertex(result,null);
	}
	Set succ = new TreeSet();
	succ.add(result);

	// Add the args vertices if necessary
	Set pred = new TreeSet();
	for (Iterator i=args.iterator(); i.hasNext(); ){
	    Symbol arg = (Symbol)i.next();
	    if (! graph.containsVertex(arg)){
		graph.addVertex(arg,null);
	    }
	    pred.add(arg);
	}
	graph.addEdge(idEqn,pred,eqn,succ);
    }

    /** Returns an equation defined by its id. */
    public Equation getEquation(Integer id){
	return (Equation)graph.getEdgeAttr(id);
    }

    // ======================================================================
    // Hierarchical order
    // ======================================================================
    /** Converts a (hierarchical) component (which is a nested list of
     * integers) to a string, using variable's names. */
    public String componentToString(List component){
	StringBuffer buffer = new StringBuffer();
	buffer.append("(");
	boolean first = true;
	for (Iterator i=component.iterator(); i.hasNext(); ){
	    Object subcomp = i.next();
	    if (!first) buffer.append(" "); else first = false;
	    if (subcomp instanceof List){
		buffer.append(componentToString((List)subcomp));
	    } else if (subcomp instanceof Symbol){
		buffer.append(subcomp.toString());
	    } else {
		throw new RuntimeException("tvla.util.HGraph.componentToString: invalid argument, subcomponent of type " + subcomp.getClass());
	    }
	}
	buffer.append(")");
	return buffer.toString();
    }

    /** Hierarchical order. */
    public static class HierarchicalOrder {

	/** Computes an iteration order of nodes */
	public static List build(HGraph graph, Set inits){
	    // Generates a fresh vertex identifier
	    Symbol fresh = Symbol.freshSymbol();
	    List horder = graph.SCSC(inits, null, fresh, new Integer(Integer.MIN_VALUE));
	    fresh.remove();
	    return horder;
	}

	/** Linearize an hierarchical order into a two level list.  For
	    instance, (1 2 (3 4 (5 6 7 (8 9) 10)) 11) becomes (1 2 (3
	    4 5 6 7 8 9 10) 11). */
	public static List linearize2(List horder){
	    List result = new ArrayList();
	    for (Iterator i = horder.iterator(); i.hasNext(); ){
		Object elt = i.next();
		if (elt instanceof Symbol){
		    result.add(elt);
		}
		else if (elt instanceof List){
		    List result1 = new ArrayList();
		    linearizeAux((List)elt, result1);
		    result.add(result1);
		}
		else {
		    throw (new TVLAException("EquationGraph.HierarchicalOrder.linearizeOrderAux: unexpected input\n"));
		}
	    }
	    return result;
	}
	private static void linearizeAux(List horder, List result){
	    for (Iterator i = horder.iterator(); i.hasNext(); ){
		Object elt = i.next();
		if (elt instanceof Symbol){
		    result.add(elt);
		}
		else if (elt instanceof List){
		    linearizeAux((List)elt, result);
		}
		else {
		    throw (new TVLAException("EquationGraph.HierarchicalOrder.linearizeOrderAux: unexpected input\n"));
		}
	    }
	}

	/** Returns a set of widening/save points induced by the
	 * hierarchical order (head of subcomponents are chosen) */
	public static Set getWideningVertices(List horder){
	    Set set = new HashSet();

	    for (Iterator i = horder.iterator(); i.hasNext(); ){
		Object elt = i.next();
		if (elt instanceof List){
		    getWideningVerticesAux((List)elt, set);
		}
		else if (! (elt instanceof Symbol)){
		    throw (new TVLAException("EquationGraph.HierarchicalOrder.linearizeOrderAux: unexpected input\n"));
		}
	    }
	    return set;
	}

	private static void getWideningVerticesAux(List horder, Set set){
	    boolean first = true;
	    for (Iterator i = horder.iterator(); i.hasNext(); ){
		Object elt = i.next();

		if (elt instanceof Symbol){
		    if (first) set.add(elt);
		}
		else if (elt instanceof List){
		    getWideningVerticesAux((List)elt,set);
		}
		else {
		    throw (new TVLAException("EquationGraph.HierarchicalOrder.linearizeOrderAux: unexpected input\n"));
		}
		first = false;
	    }
	}
    }

}
