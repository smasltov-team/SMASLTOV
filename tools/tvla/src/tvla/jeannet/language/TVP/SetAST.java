package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

/** A base class for sets.
 * @author Tal Lev-Ami.
 */
public abstract class SetAST extends AST {
    abstract public Set getMembers();
}