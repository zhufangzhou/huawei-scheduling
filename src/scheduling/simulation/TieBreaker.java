package scheduling.simulation;

/**
 * A tie breaker breaks the tie between two candidates when they have the same priority.
 */

public abstract class TieBreaker <T> {
    public abstract int breakTie(T o1, T o2, State state);
}
