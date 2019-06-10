package scheduling.simulation.tiebreaker;

import scheduling.simulation.State;
import scheduling.simulation.TieBreaker;

/**
 * A simple tie breaker between two candidates uses the natural comparator.
 */

public class SimpleTieBreaker<T extends Comparable> extends TieBreaker<T> {

    @Override
    public int breakTie(T o1, T o2, State state) {
        return o1.compareTo(o2);
    }
}
