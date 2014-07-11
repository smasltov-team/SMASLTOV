package tvla.jeannet.expressions;

import tvla.core.*;
import tvla.core.assignments.Assign;
import tvla.formulae.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.io.*;
import tvla.jeannet.language.TVP.PredicateAST;
import tvla.logic.Kleene;
import tvla.predicates.Predicate;
import tvla.predicates.Vocabulary;

import java.util.*;

/** A class for a pre-fabricated set of structures for use in the separation logic semi-decision procedure.
 * @author Jason Breck
 */
public class PrefabExpression implements Expression {
    private Symbol topVar;        // a variable name for a set of structures that represents the "top" structure.
    private List args;            // the arguments that specify what structures should be generated.
    private Prefab prefab = null;
    private String prefabType;
    private String overUnderString;
    private boolean makeStructuresPrecise;
    private Kleene truth;
    private TVS top;

    //private static final boolean thru = true;        // if true, generate the "thru" predicates
    //private static final boolean n_t_r_next = false; // if true, generate n, t[n], r[n,sv] and nxt_contains predicates
    
    public class PrefabExpressionException extends RuntimeException {
        PrefabExpressionException(String message) {
            super(message);
        }
    }
    
    public PrefabExpression(Symbol topVar, String prefabType, String overUnderString, Kleene truth, List args) {
        this.topVar = topVar;
        this.truth = truth;
        this.prefabType = prefabType;
        this.overUnderString = overUnderString;
        this.args = args;
        
        if (overUnderString.equals("over")) {
            makeStructuresPrecise = true;
        } else if (overUnderString.equals("under")) {
            makeStructuresPrecise = false;
        } else {
            throw new PrefabExpressionException("ERROR: Third parameter to prefabExpression (of type '" + prefabType + "') should be either 'over' or 'under', but '" + overUnderString + "' was received instead.");
        }
        
        try {
            if (prefabType.equals("MagicWandOverlap")) { prefab = new MagicWandOverlapPrefab(args.get(0).toString(), args.get(1).toString(), args.get(2).toString()); if (args.size() != 3) { throw new PrefabExpressionException("Wrong number of arguments to prefab() with type = " + prefabType);}}
            if (prefabType.equals("StarPartition"))    { prefab =    new StarPartitionPrefab(args.get(0).toString(), args.get(1).toString(), args.get(2).toString()); if (args.size() != 3) { throw new PrefabExpressionException("Wrong number of arguments to prefab() with type = " + prefabType);}}
            
            if (prefabType.equals("PointsTo")) { prefab = new PointsToPrefab(args.get(0).toString(), args.get(1).toString(), args.get(2).toString()); if (args.size() != 3) { throw new PrefabExpressionException("Wrong number of arguments to prefab() with type = " + prefabType);}}
            if (prefabType.equals("Ls"))       { prefab =       new LsPrefab(args.get(0).toString(), args.get(1).toString(), args.get(2).toString()); if (args.size() != 3) { throw new PrefabExpressionException("Wrong number of arguments to prefab() with type = " + prefabType);}}
            if (prefabType.equals("Eq"))       { prefab =       new EqPrefab(args.get(0).toString(), args.get(1).toString());                         if (args.size() != 2) { throw new PrefabExpressionException("Wrong number of arguments to prefab() with type = " + prefabType);}}
            if (prefabType.equals("Emp"))      { prefab =      new EmpPrefab(args.get(0).toString());                                                 if (args.size() != 1) { throw new PrefabExpressionException("Wrong number of arguments to prefab() with type = " + prefabType);}}
            if (prefabType.equals("True"))     { prefab =     new TruePrefab(args.get(0).toString());                                                 if (args.size() != 1) { throw new PrefabExpressionException("Wrong number of arguments to prefab() with type = " + prefabType);}}
            
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new PrefabExpressionException("ERROR: Wrong number of arguments to prefab() command with type = " + prefabType + " and truth = " + truth);
        }
        
        if (prefab == null) {
            throw new PrefabExpressionException("ERROR: Unrecognized prefab type name: '" + prefabType + "'");
        }
    }
    
    public boolean hasValue(){ return true; }

    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime){
        runtime.primitiveNumber++;

        ValueExpression topValueExpression = bindings.lookup(topVar);
        Collection topCollection = topValueExpression.getCollectionTVS();
        // NOTE: depending on how the equation system works, we might want
        //   to allow eval() to sometimes be called with zero input structures
        //   and when that happens we could send zero output structures out,
        //   hopefully waiting until some later time when we get eval'ed with
        //   our real (top) input structure, at which point we would build our
        //   real output structures based upon it.
        if (topCollection.size() != 1) {
            throw new PrefabExpressionException("Error: A prefab expression must be given a single 'top' structure as input, but it received " + (topCollection.size()) + " structures instead.");
        }
        // Set the "top" class member...
        top = (HighLevelTVS) topCollection.iterator().next();

        return new ValueExpression(prefab.getStructures());
    }
    
    /** Check that the given string is a valid predicate name, and return the named predicate.  Throw PrefabExpressionExpression otherwise.
     */
    public Predicate getPredicateByName(String name) {
        Predicate result = Vocabulary.getPredicateByName(name.toString());
        if (result == null) {
            throw new PrefabExpressionException("ERROR: PrefabExpression.getPredicateByName() could not find predicate with name = '" + name + "'");
        }
        return result;
    }
    /** Check that the given string is a valid predicate name, and return the name.  Throw PrefabExpressionExpression otherwise.
     */
    public String verifyPredicateName(String name) {
        String nameString = name.toString();
        getPredicateByName(nameString);
        return nameString;
    }    
    
    abstract class Prefab {
        protected Collection<TVS> structures;
        protected TVS current = null;
        protected Node topHeapNode = null;
        public Collection<TVS> getStructures() {
            if (structures == null) {
                structures = new ArrayList<TVS>();
                current = null;
                buildStructures();
                saveStructure();
            }
            return structures;
        }
        abstract protected void buildStructures();
        protected void saveStructure() { 
            if (current != null) { structures.add(current); current = null; } 
        }
        protected void newStructure()  { 
            saveStructure(); 
            current = top.copy();
            topHeapNode = findUniqueNodeWithPredicateTrue("heapSort");
        }
        protected Node makeHeapNode() {
            Node newNode = current.duplicateNode(topHeapNode);
            // Question: should I set topHeapNode to be outside the domain...?
            // Answer: for now, I will do that manually in each leaf node.
            return newNode;
        }
        /** Return the unique node in the current structure where the predicate
         *    with the given name evaluates to true; if there is not exactly
         *    one such node, then throw an error. 
         */
        protected Node findUniqueNodeWithPredicateTrue(String predicateName) {
            Predicate predicate = getPredicateByName(predicateName);
            Var v = new Var("v");
            PredicateFormula formula = new PredicateFormula(predicate, v);
            Iterator iter = current.evalFormulaForValue(formula, Assign.EMPTY, Kleene.trueKleene);

            if (!iter.hasNext()) {
                throw new PrefabExpressionException("findUniqueNodeWithPredicateTrue found zero nodes where predicate '" + predicateName + "' was true.");
            }
            
            Assign assign = (Assign) iter.next();
            Node node = assign.get(formula.getVariable(0));
            
            if (iter.hasNext()) {
                throw new PrefabExpressionException("findUniqueNodeWithPredicateTrue found more than one node where predicate '" + predicateName + "' was true.");
            }
            
            return node;
        }
        protected List stringTriple(Object first, Object second, Object third) {
            List list = new ArrayList(3);
            list.add(first.toString());
            list.add(second.toString());
            list.add(third.toString());
            return list;
        }
        protected List stringPair(Object first, Object second) {
            List list = new ArrayList(2);
            list.add(first.toString());
            list.add(second.toString());
            return list;
        }
        protected List stringSingleton(Object first) {
            List list = new ArrayList(1);
            list.add(first.toString());
            return list;
        }
        /** Update the current structure to have the null predicate with the given name to evaluate to the given value.*/
        protected void update(String predicateName, Kleene newValue) {
            current.update(getPredicateByName(predicateName), newValue);
        }       
        /** Update the current structure to have the null predicate with the given name to evaluate to the given value.
         *     This version takes a list of predicate name parameters, so you can build names like "nlink[d1,a2]" or "nlink[d2,a2]".
         * */
        protected void update(String predicateName, List predicateNameParams, Kleene newValue) {
            current.update(getPredicateByName(PredicateAST.generateName(predicateName, predicateNameParams)), newValue);
        }       
        /** Update the current structure to have the predicate with the given name to evaluate to the given value on the given node or nodes.
         *     FYI: The class Node extends NodeTuple, so you may pass a single node. */
        protected void update(String predicateName, NodeTuple nodes, Kleene newValue) {
            current.update(getPredicateByName(predicateName), nodes, newValue);
        }
        /** Update the current structure to have the predicate with the given name to evaluate to the given value on the given node or nodes.
         *     This version takes a list of predicate name parameters, so you can build names like "nlink[d1,a2]" or "nlink[d2,a2]".
         *     FYI: The class Node extends NodeTuple, so you may pass a single node. */
        protected void update(String predicateName, List predicateNameParams, NodeTuple nodes, Kleene newValue) {
            current.update(getPredicateByName(PredicateAST.generateName(predicateName, predicateNameParams)), nodes, newValue);
        }
    }

    // Parameters taken by prefab types: 
    //
    // Name             Kleene  d0  a1  a2  d1  d2  (eventually, may add selector, may remove some d predicates...)
    // -------------------------------------------
    // True             y       y?           
    // Emp              y       y           
    // PointsTo         y       y   y   y   
    // Ls               y       y   y   y   
    // Eq               y           y   y
    // StarPartition    y       y           y   y
    // MagicWandOverlap y       y           y   y
    
    // In the change from the n_t_r_next set of predicates to the thru set of predicates,
    //    we have these (*approximate*) replacements of predicates:
    // 
    //   n_t_r_next               thru
    //   ------------------       ----------------
    //   n(v1,v2)           -->   n[d](v1,v2)
    //   t[n](v1,v2)        -->   t[d,n](v1,v2)
    //   r[n,x](v)          -->   r[d,n,x](v)
    //   nlink[d,x](v)      -->   link[d,n,y](v)
    //   nxt_contains[y](v) -->      (none)
    //      (none)          -->   singleton[d]()
    
    // Note to self about a possible future version that allows cyclic structures:
    //   Currently, these rules are designed with the assumption that cyclic
    //   structures are prohibited everywhere, as a global integrity constraint.
    //   It would not be safe to simply remove that constraint and assume that 
    //   cyclic structures would then be allowed.  In particular, the structures
    //   below were designed in such a way that cyclic structures might be 
    //   included in neither the ls() nor the not-ls() (and likewise points-to)
    //   structures, so that (ls() & not-ls()) would not be an actual tautology
    //   if the integrity constraints allowed cyclic structures.  If we want to
    //   add cyclic structures, one approach would be to add structures containing
    //   nodes on a cycle (w/ c[n](v) true) to both the not-ls and not-points-to
    //   structure sets below.  Also, we would need to very carefully check our
    //   integrity constraints...

    /* For readability, I use the following abbreviations in the representation of prefab structures.*/
    private static final Kleene T   = Kleene.trueKleene;
    private static final Kleene F   = Kleene.falseKleene;
    
    class LsPrefab extends Prefab {
        // Ls( x, y ) in d. 
        String d;
        String x;
        String y;
        String n;
        LsPrefab(String dName, String xName, String yName) { 
            d = dName; x = xName; y = yName; 
            n = "n"; // TODO: change this if we move to allow multiple selectors
        }
        protected void buildStructures() {
            if (Kleene.agree(truth, Kleene.trueKleene)) {
        	/*
                    * Note that x==y==null can satisfy ls(x,y) if d is empty, 
                        (and it would fall into the first structure) but if
                        d is non-empty, then it's a not-ls case.
                    * We could conceivably collapse the second and third structures
                        so there is a single node in d, which is 1/2 x, and
                        the outside world is definitely not x, and x is not equal to
                        null, and the single node has all the Ls properties, which
                        is to say: it's r[n,x] & nlink[d,y] & !r[n,y]
                        If you were to do that, Aditya and I worried that you might
                        no longer get UNSAT from the formula:
                           ( a1 |-> a2 --(*) ls(a1,a3) ) /\ !ls(a2,a3)
                        We're not sure, though.
     	        */
                
                // Structure Ls1
                // #1 In this structure x==y and d is empty.
                newStructure();
                update("is_eq", stringPair(x,y), T);
                update("is_eq", stringPair(y,x), T);
                update(d, topHeapNode, F);
                
                // Structure Ls2
                // #2 In this structure x!=y and x is alone in d.
                newStructure();
                Node xNode = makeHeapNode();
                update(d, xNode, T);
                update(x, xNode, T);
                update("is_eq", stringPair(x,y), F);
                update("is_eq", stringPair(y,x), F);
                update(y, xNode, F);
                update("r", stringTriple(d, "n", y), xNode, F);
                update("r", stringTriple(d, "n", x), xNode, T);
                update("link", stringTriple(d, "n", y), xNode, T);
                if (makeStructuresPrecise) {
                    // I'm not sure about whether it's better to have not is_eq[x,y] or not y(x) or not r[n,y](x) ...?
                    update("is_eq", stringPair(x,"null"), F);
                    update("is_eq", stringPair("null",x), F);
                    //update("nxt_contains", stringSingleton(y), xNode, T);
                    update("sm", xNode, F);
                    update("singleton", stringSingleton(d), T);
                    update(x, topHeapNode, F);
                }
                update(d, topHeapNode, F);
                
                // Structure Ls3
                // #3 In this structure x!=y and x is *not* alone in d.
                newStructure();
                xNode = makeHeapNode();
                Node otherNode = makeHeapNode();
                update(d, xNode, T);
                update(d, otherNode, T);
                update(x, xNode, T);
                update("is_eq", stringPair(x,y), F);
                update("is_eq", stringPair(y,x), F);
                update(y, xNode, F);
                update(y, otherNode, F);
                update("r", stringTriple(d, "n", y), xNode, F);
                update("r", stringTriple(d, "n", y), otherNode, F);
                update("link", stringTriple(d, "n", y), xNode, T);
                update("link", stringTriple(d, "n", y), otherNode, T);
                update("r", stringTriple(d, "n", x), otherNode, T);
                if (makeStructuresPrecise) {
                    update("is_eq", stringPair(x,"null"), F);
                    update("is_eq", stringPair("null",x), F);
                    update(x, otherNode, F);
                    //update("nxt_contains", stringSingleton(y), xNode, F);
                    update("r", stringTriple(d, "n", x), xNode, T);
                    update("t", stringPair(d, "n"), NodeTuple.createPair(xNode, otherNode), T); // unique feature: definite t and n
                    update("t", stringPair(d, "n"), NodeTuple.createPair(otherNode, xNode), F);
                    update("n", stringSingleton(d), NodeTuple.createPair(xNode, topHeapNode), F);
                    update("singleton", stringSingleton(d), F);
                    update("sm", xNode, F);
                    update(x, topHeapNode, F);
                }
                update(d, topHeapNode, F);
            }
            if (Kleene.agree(truth, Kleene.falseKleene)) {
        	/*
        	   A couple of brief notes:
        	   
        	     * The unusual case of ls(x,y) with x==null=/=y could fall into
        	          either structure #1 or #3 below, depending on whether
        	          or not d is empty.
        	     * Note that x==y==null can satisfy ls(x,y) if d is empty, but
        	          if d is non-empty, then it falls into structure #2. 
        	 */
                
                // Structure NotLs1
                // #1 In this structure x!=y but d is empty
                newStructure();
                update("is_eq", stringPair(x,y), F); // not-ls because x!=y while d empty
                update("is_eq", stringPair(y,x), F);
                // x could be null
                update(d, topHeapNode, F);
                
                // Structure NotLs2 
                // #2 In this structure x==y but d is non-empty
                //    Note that this structure (and maybe others??) involve an 
                //      implicit assumption that cyclic structures are not allowed,
                //      because if d contained a cyclic list, then that cyclic
                //      list could both start and end with cell x, which would
                //      fall into this case even though ls would actually be true!
                newStructure();
                Node otherNode = makeHeapNode();
                update("is_eq", stringPair(x,y), T);
                update("is_eq", stringPair(y,x), T);
                // x could be null
                update(d, otherNode, T); // not-ls because x==y while d non-empty
                update(d, topHeapNode, F);
                
                // Structure NotLs3 (corresponding test formulas are NotLs3_a and NotLs3_b)
                // #3 In this structure x!=y, d is non-empty, but x not in d (or, a node in d is !r[d,n,x])
                newStructure();
                otherNode = makeHeapNode();
                // x could be null
                update(d, otherNode, T);
                if (makeStructuresPrecise) {
                    update("is_eq", stringPair(x,y), F);
                    update("is_eq", stringPair(y,x), F);
                    update(x, otherNode, F); // x not in d, hence not-ls
                } else {
                    // The non-precise version here is weaker;
                    //   instead of saying, there's a node in d which isn't x,
                    //   we say, "There's a node in d which isn't r[n,d,x]"
                    update("r", stringTriple(d, "n", x), otherNode, F);
                }
                update(d, topHeapNode, F);
                
                // Structure NotLs4
                // #4 In this structure x!=y, d is non-empty, {x}=d, but !link[d,n,y](x)
                newStructure();
                Node xNode = makeHeapNode();
                update(d, xNode, T);
                // I'm not 100% sure that x should be true (vs. 1/2) in this case.
                //   In the precise version, that's fine, but in the non-precise
                //   version, this structure could say just "no nodes are nlink..."
                update(x, xNode, T);
                update("link", stringTriple(d, "n", y), xNode, F); // hence not-ls
                // The non-precise version skips the following steps:
                //   It says just, "x is in d, and it is not link[d,n,y]"
                if (makeStructuresPrecise) {
                    update("is_eq", stringPair(x,y), F);
                    update("is_eq", stringPair(y,x), F);
                    update("is_eq", stringPair(x,"null"), F);
                    update("is_eq", stringPair("null",x), F);
                    update("r", stringTriple(d, "n", x), xNode, T);
                    update("sm", xNode, F); // could move this line outside this if
                    update("singleton", stringSingleton(d), T);
                    update(x, topHeapNode, F);
                }
                update(d, topHeapNode, F);

//                    // Old#5 In this structure x!=y, d is non-empty, {x}=d, link[d,n,y](x), r[n,y](x)
//                    //
//                    // This structure is unnecessary now that we're using the thru predicates.
//                    // That's because we no longer have reachability from y as part of the 
//                    //   definition of ls.
//                    newStructure();
//                    xNode = makeHeapNode();
//                    update(d, xNode, T);
//                    if (makeStructuresPrecise) {
//                        // The non-precise version skips these steps.
//                        //   It says just, "Single (summary) node in d, which is r[n,y]"
//                        update("is_eq", stringPair(x,y), F);
//                        update("is_eq", stringPair(y,x), F);
//                        update("is_eq", stringPair(x,"null"), F);
//                        update("is_eq", stringPair("null",x), F);
//                        update(x, xNode, T);
//                        //if (thru) {
//                    	//    //update("link", stringTriple(d, "n", y), xNode, T); // NO!
//                        //    update("r", stringTriple(d, "n", x), xNode, T);
//                        //}
//                        update("sm", xNode, F); // could move this outside if
//                        update("singleton", stringSingleton(d), T);
//                        update(x, topHeapNode, F);
//                    }
//                    update(d, topHeapNode, F);
                
                // Structure NotLs5
                // New#5 In this structure x!=y, d is non-empty, x in d, {trouble,rest}=d, !r[n,x](trouble)
                // (this was the old structure 6)
                newStructure();
                Node troubleNode = makeHeapNode();
                Node restNode = makeHeapNode();
                update(d, troubleNode, T);
                update(d, restNode, T);
                update("r", stringTriple(d, "n", x), troubleNode, F); // hence not-ls
                if (makeStructuresPrecise) {
                    // The non-precise version skips these steps.
                    //   It says just, "Some node in d is !r[n,x]"
                    update("is_eq", stringPair(x,y), F);
                    update("is_eq", stringPair(y,x), F);
                    update("is_eq", stringPair(x,"null"), F);
                    update("is_eq", stringPair("null",x), F);
                    update(x, troubleNode, F);
                    // ... x stays at 1/2 on rest ...
                    update(x, topHeapNode, F); // x is F on the outside world, i.e. x is in d
                    update("singleton", stringSingleton(d), F);
                }
                update(d, topHeapNode, F);
                
                // Structure NotLs6
                // New#6 In this structure x!=y, d is non-empty, x in d, {trouble,rest}=d, r[n,x](trouble,rest), !link[d,n,y](trouble)
                // (this was the old structure 7)
                newStructure();
                troubleNode = makeHeapNode();
                restNode = makeHeapNode();
                update(d, troubleNode, T);
                update(d, restNode, T);
                update("link", stringTriple(d, "n", y), troubleNode, F); // hence not-ls  
                if (makeStructuresPrecise) {
                    // The non-precise version skips these steps.
                    //   It says just, "Some node in d is !nlink"
                    update("is_eq", stringPair(x,y), F);
                    update("is_eq", stringPair(y,x), F);
                    update("is_eq", stringPair(x,"null"), F);
                    update("is_eq", stringPair("null",x), F);
                    // ... x stays at 1/2 on both trouble and rest ...
                    update("r", stringTriple(d, "n", x), troubleNode, T);
                    update("r", stringTriple(d, "n", x), restNode, T);
                    update("singleton", stringSingleton(d), F);
                    update(x, topHeapNode, F); // x is F on the outside world, i.e. x is in d
                }
                update(d, topHeapNode, F);
                
//                    // Old#8 In this structure x!=y, d is non-empty, x in d, {trouble,rest}=d, r[n,x](trouble,rest), nlink[d,y](trouble,rest), r[n,y](trouble)
//                    //
//                    // This structure is unnecessary now that we're using the thru predicates.
//                    // That's because we no longer have reachability from y as part of the 
//                    //   definition of ls.
//                    newStructure();
//                    troubleNode = makeHeapNode();
//                    restNode = makeHeapNode();
//                    update(d, troubleNode, T);
//                    update(d, restNode, T);
//                    if (n_t_r_next) {
//                        update("r", stringPair("n",y), troubleNode, T); // hence not-ls
//                    }
//                    if (makeStructuresPrecise) {
//                        // The non-precise version skips these steps.
//                        //   It says just, "Some node in d is r[n,y]"
//                        update("is_eq", stringPair(x,y), F);
//                        update("is_eq", stringPair(y,x), F);
//                        update("is_eq", stringPair(x,"null"), F);
//                        update("is_eq", stringPair("null",x), F);
//                        // ... x stays at 1/2 on both trouble and rest ...
//                        if (n_t_r_next) {
//                            update("r", stringPair("n",x), troubleNode, T);
//                            update("r", stringPair("n",x), restNode, T);
//                            update("nlink", stringPair(d,y), troubleNode, T);
//                            update("nlink", stringPair(d,y), restNode, T);
//                        }
//                        if (thru) {
//                            update("r", stringTriple(d, "n", x), troubleNode, T);
//                            update("r", stringTriple(d, "n", x), restNode, T);
//                            update("link", stringTriple(d, "n", y), troubleNode, T);  
//                            update("link", stringTriple(d, "n", y), restNode, T);  
//                            update("singleton", stringSingleton(d), F);
//                        }
//                        update(x, topHeapNode, F); // x is F on the outside world, i.e. x is in d
//                    }                
//                    update(d, topHeapNode, F);
                
            }
        }
    }
    class PointsToPrefab extends Prefab {
        // [ x |-> y ] in d. 
        String d;
        String x;
        String y;
        String n;
        PointsToPrefab(String dName, String xName, String yName) { 
            d = dName; x = xName; y = yName;
            n = "n"; // TODO: change this if we move to allow multiple selectors
        }
        protected void buildStructures() {
            if (Kleene.agree(truth, Kleene.trueKleene)) {
                
                // Structure Pts1
                newStructure();
                Node xNode = makeHeapNode();
                update(d, xNode, T);
                update(x, xNode, T);
                update("r", stringTriple(d, "n", y), xNode, F);
                update("singleton", stringSingleton(d), T);
                update("is_eq", stringPair(x,y), F);
                update("is_eq", stringPair(y,x), F);
                update("sm", xNode, F);
                
                update(y, xNode, F);
                update("r", stringTriple(d, "n", x), xNode, T);                 
                update(x, topHeapNode, F);
                //update("nxt_contains", stringSingleton(y), xNode, T);
                update("link", stringTriple(d, "n", y), xNode, T);
                if (makeStructuresPrecise) {
                    // All "precise" predicates have been moved to the non-precise version.
                }
                update(d, topHeapNode, F);
                
            }
            if (Kleene.agree(truth, Kleene.falseKleene)) {
        	/*
               	   A couple of brief notes:
     	   
     	           * The unusual case of [ x |-> y ] with x==null=/=y could fall into
     	               either structure #1 or #2 below, depending on whether or
     	               not d is empty.
     	           * The case of x==y is prohibited by our acyclicity integrity rule
     	               from being a case of true points-to.  It is possible, though,
     	               that x==y and x does not next-contain y, or x==y and x not in d, 
     	               so that x==y is true in either structure #1 or #2 or #3.
     	        */
                
                // Structure NotPts1
                // #1 In this structure d is empty.
                newStructure();
                // x could be null
                update(d, topHeapNode, F);

                // Structure NotPts2 (corresponding test formulas are NotPts2_a and NotPts2_b)
                // #2 In this structure d is non-empty, but x is not in d.
                newStructure();
                Node otherNode = makeHeapNode();
                // x could be null
                update(d, otherNode, T);
                update(x, otherNode, F);
                update(d, topHeapNode, F);
                
                // Structure NotPts3
                // #3 In this structure, d={x}, but x doesn't have link[d,n,y]
                newStructure();
                Node xNode = makeHeapNode();
                update(d, xNode, T);
                //update("nxt_contains", stringSingleton(y), xNode, F);
                update("link", stringTriple(d, "n", y), xNode, F);  
                // The non-precise version skips the following steps.
                //   It says just, "The node in d is not nxt_contains[y] / not link[d,n,y]"
                if (makeStructuresPrecise) {
                    update("is_eq", stringPair(x,"null"), F);
                    update("is_eq", stringPair("null",x), F);
                    update(x, xNode, T);
                    update("r", stringTriple(d, "n", x), xNode, T);
                    update("sm", xNode, F);
                    update(x, topHeapNode, F);
                    update("singleton", stringSingleton(d), T);
                }
                update(d, topHeapNode, F);
                
                /*
                // OLD VERSION with nxt_contains 1/2 ************************************
                // #4 In this structure, x is in d, but x is not alone
                newStructure();
                xNode = makeHeapNode();
                otherNode = makeHeapNode();
                update("is_eq", stringPair(x,"null"), F);
                update("is_eq", stringPair("null",x), F);
                update(d, xNode, T);
                update(d, otherNode, T);
                update(x, xNode, T);
                update(x, otherNode, F);
                update("sm", xNode, F);
                update(d, topHeapNode, F);
                update(x, topHeapNode, F);
                // OLD VERSION with nxt_contains 1/2 ************************************
                */
                
                // Structure NotPts4
                // #4 In this structure, x in d, but x is not alone, with link[d,n,y](x) false
                newStructure();
                xNode = makeHeapNode();
                update(d, xNode, T);
                // The non-precise version skips the following steps.
                //   It says just, "d is a non-singleton region..."
                if (makeStructuresPrecise) {
                    otherNode = makeHeapNode();
                    update("is_eq", stringPair(x,"null"), F);
                    update("is_eq", stringPair("null",x), F);
                    update(d, otherNode, T);
                    update(x, xNode, T);
                    update(x, otherNode, F);
                    //update("nxt_contains", stringSingleton(y), xNode, F);
                    update("link", stringTriple(d, "n", y), xNode, F);  
                    update("singleton", stringSingleton(d), F);
                    update("r", stringTriple(d, "n", x), xNode, T);
                    update("sm", xNode, F);
                    update(x, topHeapNode, F);
                } else {
                    // This non-precise version is unique.
                    // Here, we need to say, "the mere fact that this domain is not a singleton
                    //   is enough to know that it's not a points-to structure."
                    update("singleton", stringSingleton(d), F);
                }
                update(d, topHeapNode, F);
                
                if (makeStructuresPrecise) {
                    // This structure is skipped in the non-precise version,
                    //   because this case is covered by the non-singleton case (#4) above.

                    // Structure NotPts5
                    // #5 In this structure, x in d, but x is not alone, with link[d,n,y](x) true
                    newStructure();
                    xNode = makeHeapNode();
                    otherNode = makeHeapNode();
                    update("is_eq", stringPair(x,"null"), F);
                    update("is_eq", stringPair("null",x), F);
                    update(d, xNode, T);
                    update(d, otherNode, T);
                    update(x, xNode, T);
                    update(x, otherNode, F);
                    //update("nxt_contains", stringSingleton(y), xNode, T);
                    update("singleton", stringSingleton(d), F);
                    update("link", stringTriple(d, "n", y), xNode, T);
                    update("r", stringTriple(d, "n", x), xNode, T);
                    update("sm", xNode, F);
                    update(d, topHeapNode, F);
                    update(x, topHeapNode, F);
                    
                }
                
            }
        }
    }
    class EqPrefab extends Prefab {
        // The x==y literal
        String x;
        String y;
        EqPrefab(String xName, String yName) { 
            x = xName; y = yName;
        }
        protected void buildStructures() {
            if (Kleene.agree(truth, Kleene.trueKleene)) {
                
                // x == y
                newStructure();
                update("is_eq", stringPair(x,y), T);
                update("is_eq", stringPair(y,x), T);
                
            }
            if (Kleene.agree(truth, Kleene.falseKleene)) {
                
                // x != y
                newStructure();
                update("is_eq", stringPair(x,y), F);
                update("is_eq", stringPair(y,x), F);
                
            }
        }
    }   
    class EmpPrefab extends Prefab {
        // The "empty domain" literal.
        String d;
        EmpPrefab(String dName) { 
            d = dName;
        }
        protected void buildStructures() {
            if (Kleene.agree(truth, Kleene.trueKleene)) {
                
                // D is not inhabited because Emp is true
                newStructure();
                update(d, topHeapNode, F);
                
            }
            if (Kleene.agree(truth, Kleene.falseKleene)) {
                
                // D is inhabited because Emp is false
                newStructure();
                Node tNode = makeHeapNode();
                update(d, tNode, T);
                update(d, topHeapNode, F);
                
            }
        }
    }    
    class TruePrefab extends Prefab {
        // The true literal.
        String d;
        TruePrefab(String dName) { 
            d = dName;
        }
        protected void buildStructures() {
            if (Kleene.agree(truth, Kleene.trueKleene)) {
                // You could just pass top through.
                // That would be this next, commented-out line:
                //newStructure();
                // In that case, you could comment out the below
                //  two cases.
                // Possible advantage of the way below:
                //    It makes D relevant...
                
                // D is not inhabited
                newStructure();
                update(d, topHeapNode, F);
                
                // D is inhabited
                newStructure();
                Node tNode = makeHeapNode();
                update(d, tNode, T);
                update(d, topHeapNode, F);
                
            }
            if (Kleene.agree(truth, Kleene.falseKleene)) {
                
                // No structures a.k.a. trivial false a.k.a. bottom.
             
            }
        }
    }
    class MagicWandOverlapPrefab extends Prefab {
        // A set of structures designed to consider the case where
        //   the domains of the magic wand's left-hand-side (LHS),
        //   which is d1, and the magic wand's wand-satisfying-domain,
        //   which is d0, overlap.
        String d0, d1, d2;
        // Diagram:
        //    When a magic wand phi1 ---* phi2 is satisfied, normally we have:
        //       
        //         |-------d2-----------|  d1 |= phi1
        //         |---d1-----|----d0---|  d2 |= phi2
        //                                 d0 |= (phi1 --* phi2)
        // 
        //    But when the wand is false, sometimes one of the cases of its
        //      falsehood is when there is an overlap between the d1 domain
        //      and the d0 domain.  That is:
        //
        //         |------d2------------|
        //         |---d1-------|
        //                  |------d0---|
        //
        //    An example is something like (a1 |-> a2) /\ ( (a1 |-> a2) --* TRUE )
        //      because both the d0 domain and the d1 domain need to contain the
        //      heap cell whose address is a1.
        //
        MagicWandOverlapPrefab(String d0name, String d1name, String d2name) { 
            d0 = d0name; d1 = d1name; d2 = d2name;
        }
        protected void buildStructures() {
            if (truth != Kleene.trueKleene) { 
                throw new PrefabExpressionException("MagicWandOverlap requires a Kleene value of 1.");
            } 
            
            // Overlap node (and outside world node)
            newStructure();
            Node overlapNode = makeHeapNode();
            update(d0, overlapNode, T); // overlap node
            update(d1, overlapNode, T);
            update(d2, overlapNode, T);
            update(d0, topHeapNode, F); // outside world node
            update(d1, topHeapNode, F);
            update(d2, topHeapNode, F);
                
            // Overlap node + extra d1 node (and outside world node)
            newStructure();
            overlapNode = makeHeapNode();
            Node extraD1 = makeHeapNode();
            update(d0, overlapNode, T); // overlap node
            update(d1, overlapNode, T);
            update(d2, overlapNode, T);
            update(d0, extraD1, F);     // extra d1 node
            update(d1, extraD1, T);
            update(d2, extraD1, T);         
            update(d0, topHeapNode, F); // outside world node
            update(d1, topHeapNode, F);
            update(d2, topHeapNode, F);
            
            // Overlap node + extra d0 node (and outside world node)
            newStructure();
            overlapNode = makeHeapNode();
            Node extraD0 = makeHeapNode();
            update(d0, overlapNode, T); // overlap node
            update(d1, overlapNode, T);
            update(d2, overlapNode, T);
            update(d0, extraD0, T);     // extra d0 node
            update(d1, extraD0, F);
            update(d2, extraD0, T);         
            update(d0, topHeapNode, F); // outside world node
            update(d1, topHeapNode, F);
            update(d2, topHeapNode, F);
            
            // Overlap node + extra d0 node + extra d1 node (and outside world node)
            newStructure();
            overlapNode = makeHeapNode();
            extraD0 = makeHeapNode();
            extraD1 = makeHeapNode();
            update(d0, overlapNode, T); // overlap node
            update(d1, overlapNode, T);
            update(d2, overlapNode, T);
            update(d0, extraD0, T);     // extra d0 node
            update(d1, extraD0, F);
            update(d2, extraD0, T);         
            update(d0, extraD1, F);     // extra d1 node
            update(d1, extraD1, T);
            update(d2, extraD1, T);         
            update(d0, topHeapNode, F); // outside world node
            update(d1, topHeapNode, F);
            update(d2, topHeapNode, F);
                       
        }
    }
    class StarPartitionPrefab extends Prefab {
	// A set of structures depicting the ways that three domain
	//   predicates should be allowed to combine, given that
	//   we have:
	//
	//        d0 = disjoin_union( d1, d2 )
	//
	//   which is what occurs in a star.
	//
	// I typically draw this as: 
        //         |-------d0-----------|
        //         |---d1-----|----d2---|
        //                               
	// There are four possible structures, which are simply:
	//
	//    ( d1 is empty vs. non-empty) X ( d2 is empty vs. non-empty )
	//
        String d0, d1, d2;
        StarPartitionPrefab(String d0name, String d1name, String d2name) { 
            d0 = d0name; d1 = d1name; d2 = d2name;
        }
        protected void buildStructures() {
            if (truth != Kleene.trueKleene) { 
                throw new PrefabExpressionException("StarPartition requires a Kleene value of 1.");
            } 
            
            // d1 and d2 both empty.  We have only the outside world node.
            newStructure();
            update(d0, topHeapNode, F); // outside world node
            update(d1, topHeapNode, F);
            update(d2, topHeapNode, F);
                
            // d1 non-empty. d2 empty.  d1 node and outside world node
            newStructure();
            Node d1Node = makeHeapNode();
            update(d0, d1Node, T);      // extra d1 node
            update(d1, d1Node, T);
            update(d2, d1Node, F);         
            update(d0, topHeapNode, F); // outside world node
            update(d1, topHeapNode, F);
            update(d2, topHeapNode, F);
            
            // d2 non-empty. d1 empty.  d2 node and outside world node
            newStructure();
            Node d2Node = makeHeapNode();
            update(d0, d2Node, T);      // extra d2 node
            update(d1, d2Node, F);
            update(d2, d2Node, T);         
            update(d0, topHeapNode, F); // outside world node
            update(d1, topHeapNode, F);
            update(d2, topHeapNode, F);
                       
            // d1 and d2 both non-empty. d1 node, d2 node, and outside world node
            newStructure();
            d1Node = makeHeapNode();
            d2Node = makeHeapNode();
            update(d0, d1Node, T);      // extra d1 node
            update(d1, d1Node, T);
            update(d2, d1Node, F);         
            update(d0, d2Node, T);      // extra d2 node
            update(d1, d2Node, F);
            update(d2, d2Node, T);         
            update(d0, topHeapNode, F); // outside world node
            update(d1, topHeapNode, F);
            update(d2, topHeapNode, F);
        }
    }    
}
