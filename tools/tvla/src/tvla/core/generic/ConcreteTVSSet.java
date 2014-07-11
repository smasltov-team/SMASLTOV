package tvla.core.generic;

import java.util.Collection;
import java.util.Iterator;
import tvla.core.HighLevelTVS;
import tvla.core.TVSSet;
import tvla.core.meet.Meet;
import tvla.util.HashSetFactory;
import tvla.util.Pair;

/** An implementation of TVSSet that simply collects structures, i.e.,
 * without doing an abstraction.
 */
public class ConcreteTVSSet extends TVSSet {
	protected Collection<HighLevelTVS> structures;

	public ConcreteTVSSet() {
	    structures = HashSetFactory.make();
	}
	
	public ConcreteTVSSet(Collection<HighLevelTVS> set) {
	    structures = set;
    }

    public ConcreteTVSSet(Iterable<HighLevelTVS> structures) {
	    this.structures = HashSetFactory.make();
	    for (HighLevelTVS structure : structures) {
	    	this.structures.add(structure);
	    }
	}

    public TVSSet copy() {
        return new ConcreteTVSSet(this.structures);
    }
    
	public Collection<HighLevelTVS> getStructures() {
		return structures;
	}
	
	/** Applies the Join confluence operator.
	 * @return The difference between the updated set
	 * and the old set or null if there is no difference.
	 */
	public HighLevelTVS mergeWith(HighLevelTVS structure) {
		for (HighLevelTVS old : structures) {
			if (Meet.isomorphic(old, structure))
				return null;
		}
		structures.add(structure);
		return (HighLevelTVS) structure;
	}
	

	public boolean mergeWith(HighLevelTVS structure, Collection<Pair<HighLevelTVS, HighLevelTVS>> mergureMap) {
		structures.add(structure);
		return true;
    }
	
	
	/** The current number of states in this set.
	 */
	public int size() {
		return structures.size();
	}
	
	/** Return an iterator to the states this set 
	 * represents - TVS objects.
	 */
	public Iterator<HighLevelTVS> iterator() {
		return structures.iterator();
	}
	
	public String toString() {
	    return structures.toString();
	}
}