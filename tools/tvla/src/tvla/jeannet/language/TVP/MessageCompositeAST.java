package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

public class MessageCompositeAST extends MessageAST {
	MessageAST left;
	MessageAST right;

	public MessageCompositeAST(MessageAST left, MessageAST right) {
		this.left = left;
		this.right = right;
	}

	public AST copy() {
		return new MessageCompositeAST((MessageAST) left.copy(),
									   (MessageAST) right.copy());
	}

	public void substitute(String from, String to) {
		left.substitute(from, to);
		right.substitute(from, to);
	}
	public void substitute(List froms, List tos) {
		left.substitute(froms, tos);
		right.substitute(froms, tos);
	}

	public String getMessage() {
		return left.getMessage() + right.getMessage();
	}
}
