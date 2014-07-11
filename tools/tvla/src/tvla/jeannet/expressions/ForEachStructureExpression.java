package tvla.jeannet.expressions;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.core.*;
import tvla.core.generic.*;
import tvla.io.*;

import tvla.core.meet.Meet; // changed by Jason Breck 2014-04

/** A class for all primitive having as parameters two variables on
 * which they act
 * @author Jason Breck
 */

public class ForEachStructureExpression implements Expression {
    static public final int MEET = 1;
    static public final int JOIN = 2;
    
    static PrintStream logFile;

    private int type;
    private Symbol inputSetVar;
    private Symbol iterVar;
    private Expression body;

    private void log(String s) {
       if (logFile!=null) { logFile.println(s); }
    }
    
    static int highest_debug_id = 0;
    private int debug_id;

    public ForEachStructureExpression(int type, Symbol inputSetVar, Symbol iterVar, Expression body){

        debug_id = highest_debug_id++;
	
        /*
	if (logFile==null) {
	    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
	    try {
		logFile = new PrintStream("foreach_log_"+dateFormat.format(new Date())+".txt");
	    } catch (FileNotFoundException e) {
		
	    }
	}
	*/
	
	this.type = type;
	this.inputSetVar = inputSetVar;
	this.iterVar = iterVar;
	this.body = body;
	/*
	switch(this.type) {
	    case MEET :
	    case JOIN :
		break;
            default :
                throw new IllegalArgumentException("ForEachStructureExpression called with bad value for type.");
	}
	if (body == null) {
	    throw new IllegalArgumentException("ForEachStructureExpression called with null body.");
	}
	*/
	/*
	// Could check these like this:
        doCoerceAfterFocus = ProgramProperties.getBooleanProperty("tvla.engine.doCoerceAfterFocus",false);
	interleaveFocusCoerce = ProgramProperties.getBooleanProperty("tvla.engine.interleaveFocusCoerce",true);
	 */
    }

    /** Does the expression have a value ? */
    public boolean hasValue(){
    	return true;
    }
    
    // trivialMeetJoin:
    // When this is true, we don't actually do meet; instead, we check the
    //   size of every set of structures that comes out of the body of our
    //   joinforeach/meetforeach loop.  When we find the body result size
    //   to be zero, we return bottom as the result of meetforeach, and
    //   if we never see a result with size zero, we just return the same
    //   set of structures that was input to meetforeach.  This is an
    //   overapproximation of the meet, and it is far faster that calling meet.
    // When this is true and we're asked for a join, here is what we do:
    //   Suppose we have:
    //      let A = joinforeach B in C
    //              begin
    //                 ... // where the result of this is called BODYRESULT
    //              end
    //   then normally the final result A is something like:
    //     BODYRESULT(1) join BODYRESULT(2) join ... join BODYRESULT(n)
    //   whereas with trivialMeetJoin==true the final result A is:
    //     B(1) join B(2) join ... join B(n)
    //      such that for each i, B(i) is not included when BODYRESULT(i)==bottom
    private static final boolean trivialMeetJoin = true;

    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime){
	tvla.io.StructureToTVS outputHelperTVS = tvla.io.StructureToTVS.defaultInstance;
	tvla.io.StructureToDOT outputHelperDOT = tvla.io.StructureToDOT.defaultInstance;
	
        // When using JoinForEach or MeetForEach, we might want the tvla properties 
	//   doCoerceAfterFocus and interleaveFocusCoerce to both be false.
	//assert (!runtime.doCoerceAfterFocus) && (!runtime.interleaveFocusCoerce);
        ValueExpression inputSetValue = bindings.lookup(inputSetVar);
        Collection inputSetCollection = inputSetValue.getCollectionTVS();
        //System.out.println("ForEachStructureExpression::eval was run!");
        tvla.core.generic.GenericTVSSet resultSet = null; 
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        
        if (type==MEET) {
            // JTB: FIXME: This initial value is created by repeatedly calling
            //   joinWith, and I need to think about whether that's okay or not.
            resultSet = (GenericTVSSet) inputSetValue.getGenericTVSSet();
            log("    Beginning meetforeach at " + dateFormat.format(new Date()));
        } else if (type==JOIN) {
            // Initialize result set to empty
            resultSet = new tvla.core.generic.GenericTVSSet();
            log("Beginning joinforeach at " + dateFormat.format(new Date()));
        } else {
            assert false;
        }
        
        log("    this is being called on ForEachStructureExpression with debug_id " + debug_id);
        log("    begin loop over inputSetCollection, which is of size " + inputSetCollection.size() + ":");
        
        if (type==JOIN) {
            //System.out.println("JOINFOREACH WAS CALLED");
        }
        
        // Iterate over the individual structures in inputSet 
        for (Iterator otherIt = inputSetCollection.iterator(); otherIt.hasNext(); ) {
            HighLevelTVS structure = (HighLevelTVS) otherIt.next();
            
            if (type==JOIN) {
                //System.out.println("   JOINFOREACH HAD AN ITERATION");
            }

            log("---- begin input structure : ");
            log(outputHelperTVS.convert(structure));
            log("---- end input structure: ");
            
            /*
            if (type==MEET) {
    	        runtime.status.startTimer(AnalysisStatus.COERCE_TIME);
    	        boolean valid = structure.coerce();
    	        runtime.status.stopTimer(AnalysisStatus.COERCE_TIME);
    	    
    	        if (!valid) {
    	            log("    meetforeach initial incremental coerce threw out this structure.");
    	            continue; 
    	        }
            }
            */
            
            Collection singletonCollection = new ArrayList(1);
            singletonCollection.add(structure);
            ValueExpression iterValue = new ValueExpression(singletonCollection);
            BindingsExpr bodyBindings = new BindingsExpr(iterVar, iterValue, bindings);
            
            if (type==MEET) {
        	log("      meetforeach calling body.eval at " + dateFormat.format(new Date()));
            } else {
        	log("  joinforeach calling body.eval at " + dateFormat.format(new Date()));
            }
            ValueExpression bodyResult = body.eval(bodyBindings, runtime);
            
            if (type==MEET) {
        	// This might not actually have to be meet, we could just sequentially coerce,
        	//   if we could have a focus that would step through all possibilities...
        	log("      meetforeach calling meetWith at " + dateFormat.format(new Date()));
        	log("        bodyResult size=" + bodyResult.getCollection().size() + " and running result size=" + resultSet.size());
        	log("        bodyResult contains these structures:");
                for (Iterator resultIt = bodyResult.getCollection().iterator(); resultIt.hasNext(); ) {
                    log("---- begin structure: ");
                    log(outputHelperTVS.convert(resultIt.next()));
                    log("---- end structure: ");
                }
                if (trivialMeetJoin) {
                    log("        (skipping meet because trivialMeetJoin=true)");
                    if (bodyResult.getCollection().size()==0) {
                	log("      (bottom was seen inside meetforeach with trivialMeetJoin=true; short-circuiting...)");
                	// We received bottom from body, so we return bottom.
                	return new ValueExpression(new tvla.core.generic.GenericTVSSet());
                    }
                } else {
                    log("        running resultSet contains these structures:");
                    for (Iterator resultIt = resultSet.iterator(); resultIt.hasNext(); ) {
                        log("---- begin structure: ");
                        log(outputHelperTVS.convert(resultIt.next()));
                        log("---- end structure: ");
                    }
                    //resultSet.meetWith(bodyResult.getGenericTVSSet(), runtime.status); 
                    System.err.println("FEATURE NOT IMPLEMENTED: trivialMeetJoin=false."); // changed by Jason Breck 2014-04
                    System.exit(-1);
                }
                
                log("        after meet, new running result size=" + resultSet.size());
            } else if (type==JOIN) {
        	if (trivialMeetJoin) {
        	    log("  (skipping join because trivialMeetJoin=true)");
        	    if (bodyResult.getCollection().size()==0) {
               	        log("    (not performing a join because bottom was seen)");
        		// Nothing to do...
        	    } else {
               	        log("    (joining back original structure)");
        	        //resultSet.joinWith(structure);
        	        resultSet.mergeWith(structure); // changed by Jason Breck 2014-04
        	    }
        	} else {
            	log("  joinforeach calling joinWith at " + dateFormat.format(new Date()));
        	    
            	//resultSet.joinWith(bodyResult.getGenericTVSSet());
            	for (Iterator resultIt = bodyResult.getCollection().iterator(); resultIt.hasNext(); ) {
            		resultSet.mergeWith( (HighLevelTVS) resultIt.next() );
            	} // changed by Jason Breck 2014-04
        	     
        	}
            }
            if (type==MEET) {
        	log("      meetforeach end of an iteration at " + dateFormat.format(new Date()));
            } else {
        	log("  joinforeach end of an iteration at " + dateFormat.format(new Date()));
            }            
    	}
        if (type==MEET) {
            log("    meetforeach loop finished at " + dateFormat.format(new Date()));
        } else {
            log("joinforeach loop finished at " + dateFormat.format(new Date()));
        }
        if (type==MEET && trivialMeetJoin) {
            // When trivialMeetJoin is true, if we get here, then we never saw a
            //   bottom result from body, so we return the whole inputSet.
            return new ValueExpression(resultSet);
        }
    	return new ValueExpression(resultSet);
    }
    
    private String getOperationName() {
        switch(this.type) {
            case MEET :
                return "meet";
            case JOIN :
                return "join";
        }
        return "UNKNOWN";
    }
}
