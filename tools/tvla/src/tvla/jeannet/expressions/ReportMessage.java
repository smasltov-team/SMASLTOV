package tvla.jeannet.expressions;

import java.util.*;
import tvla.formulae.*;

public class ReportMessage {
    private Formula formula;
    private String message;

    public ReportMessage(Formula formula, String message) {
	this.formula = formula;
	this.message = message;
    }
    
    public Formula getFormula() {
	return formula;
    }

    public String getMessage() {
	return message;
    }
}
