package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

public class MessageStringAST extends MessageAST {
	String str;
	public MessageStringAST(String str) {
		this.str = str;
	}

	public AST copy() {
		// No need to copy;
		return this;
	}

	public void substitute(String from, String to) {
		// Do nothing.
	}
	public void substitute(List froms, List tos){
		// Do nothing.
	}

	public String getMessage() {
		return str;
	}
}
