package tvla.jeannet.util;

import tvla.util.*;
import java.util.*;

/** An ADT for hypergraphs.
    @author Bertrand Jeannet
*/

public class HGraph {
    /** useful constants. */
    private static final Integer zeroInt = new Integer(0);
    private static final Integer maxInt = new Integer(Integer.MAX_VALUE);

    /** Information associated to vertices and edges */
    private static class Node {
	public TreeSet succ;  // Set of successor edges
	public TreeSet pred;  // Set of predecessor edges
	public Object attr;   // User information

	/** Constructor */
	public Node(TreeSet pred, Object attr, TreeSet succ){
	    this.succ = succ;
	    this.pred = pred;
	    this.attr = attr;
	}
	/** Accessors */
	public TreeSet getSucc(){ return this.succ; }
	public TreeSet getPred(){ return this.pred; }
	public Object attr() { return this.attr; }
    }

    /** State of an HGraph. Identifiers are supposed to implement
     * properly Comparable, as well as hashCode() method. */
    private HashMap mapVertex; // Map from vertex identifiers to attribute objects
    private HashMap mapEdge;   // Map from edges identifiers to attribute objects

    /** Constructor */
    public HGraph(){
	mapVertex = new HashMap();
	mapEdge = new HashMap();
    }

    //
    // Accessors
    //
    /** Returns the number of vertices in the hypergraph */
    public int sizeVertex(){
	return this.mapVertex.size();
    }
    /** Returns the number of (hyper)edges in the hypergraph */
    public int sizeEdge(){
	return this.mapEdge.size();
    }
    /** Returns true if the vertex (identifier) belongs to the hypergraph */
    public boolean containsVertex(Comparable id){ return mapVertex.containsKey(id); }
    /** Returns true if the edge (identifier) belongs to the hypergraph */
    public boolean containsEdge(Comparable id){ return mapVertex.containsKey(id); }
    /** Returns the set of vertices */
    public Set vertexSet(){ return mapVertex.keySet(); }
    /** Returns the set of edges */
    public Set edgeSet(){ return mapEdge.keySet(); }
    /** Returns the attribute associated to the vertex identifier in the hypergraph */
    public Object getVertexAttr(Comparable id){
	Node node = (Node)mapVertex.get(id);
	return node.attr;
    }
    /** Returns the set (of identifiers) of successor edges of the vertex */
    public Set getVertexSucc(Comparable id){
	Node node = (Node)mapVertex.get(id);
	return node.succ;
    }
    /** Returns the set (of identifiers) of predecessor edges of the vertex */
    public Set getVertexPred(Comparable id){
	Node node = (Node)mapVertex.get(id);
	//return node.succ;
	return node.pred; // changed by Jason Breck
    }
    /** Returns the set (of identifiers) of successor vertices of the vertex */
    public Set getVertexSuccVertices(Comparable id){
	Set succEdge = getVertexSucc(id);
	Set result = new TreeSet();
	for (Iterator i=succEdge.iterator(); i.hasNext(); ){
	    Comparable idEdge = (Comparable)i.next();
	    Set succVertex = getEdgeSucc(idEdge);
	    result.addAll(succVertex);
	}
	return result;
    }
    /** Returns the set (of identifiers) of predecessor vertices of the vertex */
    public Set getVertexPredVertices(Comparable id){
	Set predEdge = getVertexPred(id);
	Set result = new TreeSet();
	for (Iterator i=predEdge.iterator(); i.hasNext(); ){
	    Comparable idEdge = (Comparable)i.next();
	    Set predVertex = getEdgePred(idEdge);
	    result.addAll(predVertex);
	}
	return result;
    }
    /** Returns the attribute associated to the edge identifier in the hypergraph */
    public Object getEdgeAttr(Comparable id){
	Node node = (Node)mapEdge.get(id);
	return node.attr;
    }
    /** Returns the set (of identifiers) of successors vertices of the edge */
    public Set getEdgeSucc(Comparable id){
	Node node = (Node)mapEdge.get(id);
	return node.succ;
    }
    /** Returns the set (of identifiers) of predecessor vertices of the edge */
    public Set getEdgePred(Comparable id){
	Node node = (Node)mapEdge.get(id);
	return node.pred;
    }
    //
    // Mutators
    //
    /** Add a vertex in the graph */
    public void addVertex(Comparable id, Object attr){
	if (mapVertex.containsKey(id))
	    throw new RuntimeException("tvla.util.HGraph.addVertex: vertex " + id + " already exists");
	Node node = new Node(new TreeSet(), attr, new TreeSet());
	mapVertex.put(id,node);
    }
    /** Add an edge in the graph. Ensures that the predecessor
     * and successor vertices exist. */
    public void addEdge(Comparable id, Set pred, Object attr, Set succ){
	if (mapEdge.containsKey(id))
	    throw new RuntimeException("tvla.util.HGraph.addEdge: edge " + id + " already exists");

	for (Iterator i = pred.iterator(); i.hasNext(); ){
	    Comparable idPred = (Comparable)i.next();
	    Node nodePred = (Node)mapVertex.get(idPred);
	    if (nodePred==null)
		throw new RuntimeException("tvla.util.HGraph.addEdge: unknown predecessor vertex " + pred);
	    nodePred.succ.add(id);
	}
	for (Iterator i = succ.iterator(); i.hasNext(); ){
	    Comparable idSucc = (Comparable)i.next();
	    Node nodeSucc = (Node)mapVertex.get(idSucc);
	    if (nodeSucc==null)
		throw new NoSuchElementException("tvla.util.HGraph.addEdge: unknown succesor vertex " + succ);
	    nodeSucc.pred.add(id);
	}
	Node node = new Node(new TreeSet(pred), attr, new TreeSet(succ));
	mapEdge.put(id,node);
    }
    /** Remove a vertex in the graph. Removes all incoming and outgoing edges. */
    public void removeVertex(Comparable id){
	Node nodeVertex = (Node)mapVertex.get(id);
	for (Iterator i = nodeVertex.succ.iterator(); i.hasNext(); ){
	    Comparable idEdge = (Comparable)i.next();
	    removeEdge(idEdge);
	}
	for (Iterator i = nodeVertex.pred.iterator(); i.hasNext(); ){
	    Comparable idEdge = (Comparable)i.next();
	    removeEdge(idEdge);
	}
	mapVertex.remove(id);
    }
    /** Remove an edge in the graph. */
    public void removeEdge(Comparable id){
	Node nodeEdge = (Node)mapEdge.get(id);
	// Removes the edge from the predecessor edges of successor vertices
	for (Iterator i = nodeEdge.succ.iterator(); i.hasNext(); ){
	    Comparable idVertex = (Comparable)i.next();
	    Node nodeVertex = (Node)mapVertex.get(idVertex);
	    nodeVertex.pred.remove(id);
	}
	// Removes the edge from the successor edges of predecessor vertices
	for (Iterator i = nodeEdge.pred.iterator(); i.hasNext(); ){
	    Comparable idVertex = (Comparable)i.next();
	    Node nodeVertex = (Node)mapVertex.get(idVertex);
	    nodeVertex.succ.remove(id);
	}
	// Removes the edge
	mapEdge.remove(id);
    }

    //
    // Iterators
    //

    /** Iterates on the set of successor vertices of a vertex. A vertex
     * can be returned more than once. If null, activeEdges is
     * not taken into account. Otherwise, it specifies the (sub)set of edges
     * to consider in the iteration.
     */
    public Iterator iteratorSuccVertices(Comparable id, Set activeEdges){
	return new IteratorSucc(id,activeEdges);
    }
    private class IteratorSucc implements Iterator {
	private Comparable vertex;
	private Set activeEdges;
	private Iterator itEdge;
	private Iterator itVertex;

	private void advance(){
	    if (itVertex!=null && itVertex.hasNext())
		return;
	    // We have to find a new valid edge, with at least one successor
	    Comparable edge=null;
	    while(itEdge.hasNext()){
		edge = (Comparable)itEdge.next();
		if (activeEdges==null || activeEdges.contains(edge)){
		    // The edge is valid
		    this.itVertex = getEdgeSucc(edge).iterator();
		    // Has it a successor ?
		    if (itVertex.hasNext())
			break;
		}
		edge = null;
	    }
	    if (edge==null){
		itVertex = null;
	    }
	}

	public IteratorSucc(Comparable vertex, Set activeEdges){
	    this.vertex = vertex;
	    this.activeEdges = activeEdges;
	    this.itEdge = getVertexSucc(vertex).iterator();
	    advance();
	}
	public boolean hasNext(){
	    return (itEdge!=null && itVertex!=null);
	}
	public Object next(){
	    Object result = itVertex.next();
	    advance();
	    return result;
	}
	public void remove(){
	    throw new UnsupportedOperationException();
	}
    }
    /** Iterates on the set of predecessor vertices of a vertex. A vertex
     * can be returned more than once. If null, activeEdges is
     * not taken into account. Otherwise, it specifies the (sub)set of edges
     * to consider in the iteration.
     */
    public Iterator iteratorPredVertices(Comparable id, Set activeEdges){
	return new IteratorPred(id,activeEdges);
    }
    private class IteratorPred implements Iterator {
	private Comparable vertex;
	private Set activeEdges;
	private Iterator itEdge;
	private Iterator itVertex;

	private void advance(){
	    if (itVertex!=null && itVertex.hasNext())
		return;
	    // We have to find a new valid edge, with at least one predecessor
	    Comparable edge=null;
	    while(itEdge.hasNext()){
		edge = (Comparable)itEdge.next();
		if (activeEdges==null || activeEdges.contains(edge)){
		    // The edge is valid
		    this.itVertex = getEdgePred(edge).iterator();
		    // Has it a predecessor ?
		    if (itVertex.hasNext())
			break;
		}
		edge = null;
	    }
	    if (edge==null){
		itVertex = null;
	    }
	}
	public IteratorPred(Comparable vertex, Set activeEdges){
	    this.vertex = vertex;
	    this.activeEdges = activeEdges;
	    this.itEdge = getVertexPred(vertex).iterator();
	    advance();
	}
	public boolean hasNext(){
	    return (itEdge==null || itVertex==null);
	}
	public Object next(){
	    Object result = itVertex.next();
	    advance();
	    return result;
	}
	public void remove(){
	    throw new UnsupportedOperationException();
	}
    }

    //
    // Algorithms
    //

    //
    // DFS: Depth-First-Search
    //
    /** Performs a depth-first search exploration of the graph,
     * starting from a set of root vertices. If null, activeEdges is
     * not taken into account. Otherwise, it specifies a set of edges
     * through which the exploration can be performed (it acts as a
     * filter on the set of edges to be considered). The result is a
     * sequence of vertex identifiers. */
    public List DFS(Set roots, Set activeEdges){
	DFSalgo algo = new DFSalgo(roots,activeEdges);
	return algo.getResult();
    }
    // The class implementing the algorithm
    private class DFSalgo {
	private Set activeEdges;
	private Set visited;
	private List result;

	// Performs the computations
	public DFSalgo(Set roots, Set activeEdges){
	    this.activeEdges = activeEdges;
	    this.visited = new HashSet();
	    this.result = new ArrayList(vertexSet().size());
	    for (Iterator i=roots.iterator(); i.hasNext(); ){
		Comparable id = (Comparable)i.next();
		visit(id);
	    }
	}
	// Delivers the result
	public List getResult(){
	    return this.result;
	}
	private void visit(Comparable id){
	    if (visited.contains(id))
		return;
	    visited.add(id);
	    result.add(id);
	    // Iterates on successor vertices
	    for (Iterator i=iteratorSuccVertices(id,activeEdges); i.hasNext(); ){
		Comparable idSucc = (Comparable)i.next();
		visit(idSucc);
	    }
	}
    }

    

    //
    // SCSC : Strongly Connected Sub-Components
    //
    /** Decomposed the graph into strongly connected sub-components,
     * starting the exploration from the given root vertices. The
     * result is a "recursive" sequence defined by the grammar rules:
     *
     * Seq ::= Elt+ , Elt ::= Seq | Comparable
     *
     * activeEdges has the same meaning as for DFS. */
    public List SCSC(Comparable root, Set activeEdges){
	SCSCalgo algo = new SCSCalgo(root,activeEdges);
	List result = algo.getResult();
	return result;
    }
    
    /** Idem, but here we have a set of root vertices. dummyXXX are
     * fresh vertex and edge identifiers used temporarily. */
    public List SCSC(Set roots, Set activeEdges, Comparable dummyVertexId, Comparable dummyEdgeId){
	// Add a dummy vertex and edge so as to have one single root vertex
	addVertex(dummyVertexId,null);
	Set pred = new TreeSet(); pred.add(dummyVertexId);
	addEdge(dummyEdgeId,pred,null,roots);
	if (activeEdges!=null) activeEdges.add(dummyEdgeId);
	// Performs the algorithm
	SCSCalgo algo = new SCSCalgo(dummyVertexId,activeEdges);
	// Clean
	removeVertex(dummyVertexId);
	if (activeEdges!=null) activeEdges.remove(dummyEdgeId);
	List result = algo.getResult();
	result.remove(0);
	return result;
    }
    // The class implementing the algorithm
    // I followed faithfully the Bourdoncle algorithm in his 93 paper
    private class SCSCalgo {
	private Set activeEdges;
	private int numVertex;
	private Stack stack;    // Stack of vertices
	private Map DFN;        // Mapping vertices identifiers to Integers
	private List result;
	// Performs the computations
	public SCSCalgo(Comparable root, Set activeEdges){
	    this.activeEdges = activeEdges;
	    this.numVertex = -1;
	    this.stack = new Stack();
	    this.DFN = new HashMap(vertexSet().size());
	    for (Iterator i=vertexSet().iterator(); i.hasNext(); ){
		Comparable id = (Comparable)i.next();
		DFN.put(id, zeroInt);
	    }
	    result = new LinkedList();
	    visit(root,result);
	}
	// Delivers the result
	public List getResult(){
	    return result;
	}
	private List component(Comparable id){
	    List partition = new LinkedList();
	    // Iterates on successor vertices
	    for (Iterator i=iteratorSuccVertices(id,activeEdges); i.hasNext(); ){
		Comparable idSucc = (Comparable)i.next();
		Integer dfn = (Integer)DFN.get(idSucc);
		if (dfn.intValue()==0){
		    visit(idSucc, partition);
		}
	    }
	    partition.add(0,id);
	    return partition;
	}
	private int visit(Comparable id, List partition){
	    stack.push(id);
	    numVertex++;
	    DFN.put(id, new Integer(numVertex));
	    int head = numVertex;
	    boolean loop = false;
	    int min;

	    // Iterates on successor vertices
	    for (Iterator i=iteratorSuccVertices(id,activeEdges); i.hasNext(); ){
		Comparable idSucc = (Comparable)i.next();
		Integer dfn = (Integer)DFN.get(idSucc);
		if (dfn.intValue()==0)
		    min = visit(idSucc, partition);
		else
		    min = dfn.intValue();
		if (min<=head){
		    head = min;
		    loop = true;
		}
	    }
	    Integer dfn = (Integer)DFN.get(id);
	    if (head==dfn.intValue()){
		DFN.put(id,maxInt);
		Comparable idElement = (Comparable)stack.pop();
		if (loop){
		    while (! idElement.equals(id)){
			DFN.put(idElement,zeroInt);
			idElement = (Comparable)stack.pop();
		    }
		    List list = component(id);
		    partition.add(0,list);
		}
		else {
		    partition.add(0,id);
		}
	    }
	    return head;
	}
    }

    /** Printing a component (which is a nested list of integers). */
    public static String componentToString(List component){
	StringBuffer buffer = new StringBuffer();
	buffer.append("(");
	boolean first = true;
	for (Iterator i=component.iterator(); i.hasNext(); ){
	    Object subcomp = i.next();
	    if (!first) buffer.append(" "); else first = false;
	    if (subcomp instanceof List){
		buffer.append(componentToString((List)subcomp));
	    } else if (subcomp instanceof Comparable){
		buffer.append((Integer)subcomp);
	    } else {
		throw new RuntimeException("tvla.util.HGraph.componentToString: invalid argument, subcomponent of type " + subcomp.getClass());
	    }
	}
	buffer.append(")");
	return buffer.toString();
    }
}
