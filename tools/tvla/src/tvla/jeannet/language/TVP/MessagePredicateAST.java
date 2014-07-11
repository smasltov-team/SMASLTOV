package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

public class MessagePredicateAST extends MessageAST {
	PredicateAST pred;
	public MessagePredicateAST(PredicateAST pred) {
		this.pred = pred;
	}

	public AST copy() {
		return new MessagePredicateAST((PredicateAST) pred.copy());
	}

	public void substitute(String from, String to) {
		pred.substitute(from, to);
	}
	public void substitute(List froms, List tos){
		pred.substitute(froms, tos);
	}

	public String getMessage() {
		return pred.generateName();
	}
}
