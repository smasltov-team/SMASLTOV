package tvla.jeannet.util;

import java.lang.*;
import java.util.*;

/** A symbol to be used anywhere.

    Allows to replace String by a unique identifier, which provides an
    hash value, equality and comparison functions for use in sets and
    maps.

    Symbols can be compared directly by address equality (==, !=)

    @author Bertrand Jeannet
*/

public final class Symbol implements Comparable {
    private String string;
    private int hashCode;

    /** A map from String to Symbol. */
    static private Map map = new HashMap();
    static private int maxCode = 0;

    /** Create a symbol with the given name. Assume that the symbol is
     * not yet registered. */
    private Symbol(String string){
	this.hashCode = maxCode++;
	this.string = string;
	map.put(this.string, this);
    }

    /** Return the symbol associated to the given name, and create if it does not exists yet. */
    public static Symbol ofString(String name){
	Symbol symbol = (Symbol)map.get(name);
	if (symbol==null)
	    symbol = new Symbol(name);
	return symbol;
    }

    /** Returns a fresh symbol */
    public static Symbol freshSymbol(){
	// Looks for a String which is not already associated to an
	// identifier in the map.
	String name = "symbol" + maxCode;
	Symbol symbol = (Symbol)map.get(name);
	while (symbol!=null){
	    name = name + "b";
	    symbol = (Symbol)map.get(name);
	}
	// Found it
	return new Symbol(name);
    }

    /** Removes a registered symbol. Use with caution !. */
    public void remove(){
	if (this.hashCode == maxCode-1) maxCode--; // It was the last symbol created
	map.remove(this.string);
    }

    /** Standard methods. */
    public Object clone(){ return this; }
    public boolean equals(Object o){ return this==o; }

    public int hashCode() { return this.hashCode; }
    public String toString(){ return this.string; }

    /** Comparable. */
    public int compareTo(Symbol o){
	return this.hashCode - o.hashCode;
    }
    public int compareTo(Object o){
	if (o instanceof Symbol)
	    return this.hashCode - ((Symbol)o).hashCode;
	else
	    throw new ClassCastException("tvla.util.Symbol expected");
    }

}
