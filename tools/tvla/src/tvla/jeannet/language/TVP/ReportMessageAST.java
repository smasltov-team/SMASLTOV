package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;
import tvla.jeannet.expressions.*;

public class ReportMessageAST extends AST {
    FormulaAST formula;
    MessageAST message;

    public ReportMessageAST(FormulaAST formula, MessageAST message) {
	this.formula = formula;
	this.message = message;
    }

    public AST copy() {
	return new ReportMessageAST((FormulaAST) formula.copy(), (MessageAST) message.copy());
    }

    public void substitute(String from, String to) {
	formula.substitute(from, to);
	message.substitute(from, to);
    }
    public void substitute(List froms, List tos) {
	formula.substitute(froms, tos);
	message.substitute(froms, tos);
    }

    public ReportMessage getReportMessage(){
	return new ReportMessage(formula.getFormula(),message.getMessage());
    }
}
