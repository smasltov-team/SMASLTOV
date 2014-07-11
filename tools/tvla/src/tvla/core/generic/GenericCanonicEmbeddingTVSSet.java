package tvla.core.generic;

import java.util.Collection;
import java.util.Iterator;

import tvla.analysis.AnalysisStatus;
import tvla.core.Canonic;
import tvla.core.HighLevelTVS;
import tvla.core.Node;
import tvla.core.NodeTuple;
import tvla.core.TVS;
import tvla.core.common.NodeTupleIterator;
import tvla.logic.Kleene;
import tvla.predicates.Predicate;
import tvla.predicates.Vocabulary;

/** A generic implementation of a TVSSet that merges structures with the same
 * set of canonic names such that one of them embeds the other.
 * @since tvla-2-alpha, August 15, 2002.
 * @author Roman Manevich.
 */
public class GenericCanonicEmbeddingTVSSet extends GenericPartialJoinTVSSet {
	public GenericCanonicEmbeddingTVSSet() {
		super();
	}

	protected boolean mergeCondition(TVS first, TVS second) {
        // NOT USED ANYMORE
	    assert first.getVocabulary() == second.getVocabulary();
		if (!super.mergeCondition(first, second))
			return false;
		
		boolean firstEmbeddedInSecond = true;
		boolean secondEmbeddedInFirst = true;
		
        for (Predicate predicate : first.getVocabulary().all()) {
			Iterator tupleIter = NodeTupleIterator.createIterator(first.nodes(), predicate.arity());
			while (tupleIter.hasNext()) {
				NodeTuple firstTuple = (NodeTuple) tupleIter.next();
				Kleene firstVal = first.eval(predicate, firstTuple);
				
				// map the tuple to the corresponding tuple in the universe of newStructure
				Node [] secondNodesTmp = new Node[firstTuple.size()];
				for (int index = 0; index < firstTuple.size(); ++index) {
					Canonic singleCanonic = (Canonic) singleCanonicName.get(firstTuple.get(index));
					secondNodesTmp[index] = (Node) newInvCanonicName.get(singleCanonic);
				}
				NodeTuple secondTuple = NodeTuple.createTuple(secondNodesTmp);
				Kleene secondVal = second.eval(predicate, secondTuple);
				
				if (firstVal == secondVal || secondVal == Kleene.unknownKleene) {
					// do nothing
				}
				else {
					firstEmbeddedInSecond = false;
				}

				if (secondVal == firstVal || firstVal == Kleene.unknownKleene) {
					// do nothing
				}
				else {
					secondEmbeddedInFirst = false;
				}
			}
			if (!firstEmbeddedInSecond && !secondEmbeddedInFirst)
				break;
		}
		
		return (firstEmbeddedInSecond || secondEmbeddedInFirst);
	}

	/** Applies the Join confluence operator.
	 * @return The difference between the updated set
	 * and the old set or null if there is no difference.
     * @author Jason Breck
     * on 2014-06-03
	 */
	public HighLevelTVS mergeWith(HighLevelTVS newStructure) {
	    assert shareCount == 0;
	    
		AnalysisStatus.getActiveStatus().startTimer(AnalysisStatus.BLUR_TIME);
		newStructure.blur();
		AnalysisStatus.getActiveStatus().stopTimer(AnalysisStatus.BLUR_TIME);

		cleanup();

        boolean haveRemovedAStructure = false;
        HighLevelTVS result = null;
	    
        //Goal: Figure out whether this new structure embeds into any old structure,
        //  or whether one (or more) of the old structures embeds into the new structure.
		for (Iterator<HighLevelTVS> iterator = structures.iterator(); iterator.hasNext(); ) {
			HighLevelTVS oldStructure = iterator.next();
			
            //Goal: Figure out whether our new structure embeds in this old structure
            //  or vice versa.
            assert oldStructure.getVocabulary() == newStructure.getVocabulary();
            if (!super.mergeCondition(oldStructure, newStructure))
                continue;
            
            boolean oldEmbeddedInNew = true;
            boolean newEmbeddedInOld = true;
            
            for (Predicate predicate : oldStructure.getVocabulary().all()) {
                Iterator tupleIter = NodeTupleIterator.createIterator(oldStructure.nodes(), predicate.arity());
                while (tupleIter.hasNext()) {
                    NodeTuple oldTuple = (NodeTuple) tupleIter.next();
                    Kleene oldVal = oldStructure.eval(predicate, oldTuple);
                    
                    // map the tuple to the corresponding tuple in the universe of newStructure
                    Node [] newNodesTmp = new Node[oldTuple.size()];
                    for (int index = 0; index < oldTuple.size(); ++index) {
                        // here singleCanonicName is the map that refers to oldStructure
                        Canonic oldCanonic = (Canonic) singleCanonicName.get(oldTuple.get(index));
                        newNodesTmp[index] = (Node) newInvCanonicName.get(oldCanonic);
                    }
                    NodeTuple newTuple = NodeTuple.createTuple(newNodesTmp);
                    Kleene newVal = newStructure.eval(predicate, newTuple);
                    
                    if (oldVal == newVal || newVal == Kleene.unknownKleene) {
                        // do nothing
                    }
                    else {
                        oldEmbeddedInNew = false;
                    }

                    if (newVal == oldVal || oldVal == Kleene.unknownKleene) {
                        // do nothing
                    }
                    else {
                        newEmbeddedInOld = false;
                    }
                }
                if (!oldEmbeddedInNew && !newEmbeddedInOld) break;
            }
            if (newEmbeddedInOld) {
                assert !haveRemovedAStructure;
                result = null;
                break;
            }
            if (oldEmbeddedInNew) {
                iterator.remove();
                haveRemovedAStructure = true;
                result = newStructure;
            }
		    //if (!oldEmbeddedInNew && !newEmbeddedInOld) continue;
			//
			//iterator.remove();
			//boolean change = mergeStructures(oldStructure, newStructure);
			//
			//structures.add(oldStructure);
			//HighLevelTVS result = (HighLevelTVS) (change ? oldStructure : null);
			//return result;
		}
		structures.add(newStructure);
		return (HighLevelTVS) newStructure;
	}

	public boolean mergeWith(HighLevelTVS S, Collection mergedWith) {
		throw new UnsupportedOperationException() ;
	}

}
