package tvla.exceptions;

import tvla.core.*;
import tvla.core.assignments.*;
import tvla.formulae.*;
import tvla.exceptions.*;

import tvla.transitionSystem.Action;

/** An exception used to terminate the execution of the analysis when
 * a user-defined condition is met.
 * @author Eran Yahav
 * @since tvla-0.91
 */
/*
public class AnalysisHaltException extends TVLAException {
	protected Action theAction;
	protected String theLabel;
	
	public AnalysisHaltException(String label, Action act) {
		super("");
		theAction = act;
		theLabel = label;
	}
	
	public Action getAction() {
		return theAction;
	}

	public String getLabel() {
		return theLabel;
	}
	
	public String getMessage() {
		if (theAction.isHalting())
			return "Analysis stopped at " + theLabel 
				   + " " + theAction 				
				   + " when evaluating " + theAction.haltCondition();
		else
			return "Analysis stopped at " 
				   + theLabel + " " + theAction + " at user request";
	}
}
*/
// Jason Breck created the following dummy definition to ease the transition
//   between TVLA3 and Bertrand Jeannet's modified TVLA.
public class AnalysisHaltException extends TVLAException {
    private HighLevelTVS structure;
    private Assign assign;
    private Formula condition;

    public AnalysisHaltException(HighLevelTVS structure, Assign assign, Formula condition){
        super("");
        throw new RuntimeException("FEATURE NOT IMPLEMENTED: Analysis Halt");
    }
    public HighLevelTVS getStructure() { return null; }
    public Assign getAssign() { return null; }
    public Formula getCondition() { return null; }

    // Jason Breck added the following parts from the TVLA3 definition
	public Action getAction() {
		return null;
	}

	public String getLabel() {
		return null;
	}

    public AnalysisHaltException(String label, Action action) {
        super("");
        throw new RuntimeException("FEATURE NOT IMPLEMENTED: Analysis Halt");
        
    }
}
