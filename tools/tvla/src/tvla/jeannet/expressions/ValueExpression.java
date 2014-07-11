package tvla.jeannet.expressions;

import tvla.core.*;
import java.util.*;
import tvla.jeannet.util.Symbol;
import tvla.util.*;

/** An class for expression values Presently, a value is a collection
 * of TVSs.
 * @author Bertrand Jeannet
 */

public class ValueExpression {
    private Collection value;

    public ValueExpression(Collection value){
	this.value = value;
    }
    public ValueExpression(TVSSet set){
	this.value = new ArrayList();
	for (Iterator i = set.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    this.value.add(structure);
	}
    }
    public Collection getCollectionTVS(){
	Iterator it = value.iterator();
	if (it.hasNext()){
	    Object elt = it.next();
	    if (! (elt instanceof HighLevelTVS))
		throw (new ExceptionExpr("Collection of HighLevelTVS expected"));
	}
	return this.value;
    }
    public Collection getCollection(){
	return this.value;
    }
    public TVSSet getGenericTVSSet(){
	tvla.core.generic.GenericTVSSet set = new tvla.core.generic.GenericTVSSet();
	for (Iterator i = value.iterator(); i.hasNext(); ){
	    HighLevelTVS structure = (HighLevelTVS)i.next();
	    //set.joinWith(structure);
	    set.mergeWith(structure); // changed by Jason Breck 2014-04 
	}
	return set;
    }
}
