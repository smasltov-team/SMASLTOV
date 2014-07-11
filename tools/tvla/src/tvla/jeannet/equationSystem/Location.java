package tvla.jeannet.equationSystem;

import tvla.logic.*;
import tvla.core.*;
import tvla.exceptions.*;
import tvla.formulae.*;
import tvla.io.*;
import tvla.predicates.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import java.util.*;

/** This class represents a variable with its value in the equation graph.
 * @author Bertrand Jeannet.
 */
public class Location {
    /** The identifier of the location. */
    public Symbol var;
    public TVSSet structures;

    public Location(Symbol var, TVSSet structures){
	this.var = var;
	this.structures = structures;
    }

    public String toString(){
	return var.toString();
    }
}
