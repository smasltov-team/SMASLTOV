package tvla.jeannet.expressions;

import tvla.exceptions.*;
import tvla.util.*;
import java.util.*;
import tvla.analysis.AnalysisStatus;

/** A class for runtime informations during the evaluation of an expression
 * @author Bertrand Jeannet
 */

public class RuntimeExpr {
    public AnalysisStatus status;

    /** A prefix for messages emitted during evaluation of expressions. */
    public String prefix = "";

    /** Counter indicating the number of functions so far evaluated. */
    /* Allows to go to the right place more easily in debugstep mode. */
    public int stepNumber = 0;

    /** Counter indicating the number of primitive expressions so far
	evaluated in the current step (or enclosing function) */
    public int primitiveNumber = 0;

    /** See commented property files */
    public boolean doCoerceAfterFocus = false;
    public boolean interleaveFocusCoerce = true;


    /** Constructor */
    public RuntimeExpr(AnalysisStatus status) {
	this.status = status;
	this.stepNumber = 0;
	this.primitiveNumber = 0;
	doCoerceAfterFocus = ProgramProperties.getBooleanProperty("tvla.engine.doCoerceAfterFocus",false);
	interleaveFocusCoerce = ProgramProperties.getBooleanProperty("tvla.engine.interleaveFocusCoerce",true);
    }
}
