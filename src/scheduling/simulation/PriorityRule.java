package scheduling.simulation;

import java.util.List;

/**
 * A priority rule selects the next from the pool based on priority.
 * @param <T> the type of the elements in the pool.
 */

public abstract class PriorityRule<T> {
    protected String name;

    public PriorityRule() {
    }

    public PriorityRule(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Select the next candidate from the decision situation and the tie breaker.
     * @param decisionSituation the decision situation.
     * @param tieBreaker the tie breaker.
     * @return the next candidate.
     */
    public T next(DecisionSituation<T> decisionSituation, TieBreaker<T> tieBreaker) {
        List<T> pool = decisionSituation.getPool();
        State state = decisionSituation.getState();

        T best = pool.get(0);
        double bestPriority = priority(best, state);

        for (int i = 1; i < pool.size(); i++) {
            T tmp = pool.get(i);
            double tmpPriority = priority(tmp, state);

            // the higher priority is preferred
            if (Double.compare(tmpPriority, bestPriority) > 0 ||
                    (Double.compare(tmpPriority, bestPriority) == 0 &&
                    tieBreaker.breakTie(tmp, best, state) < 0)) {
                best = tmp;
                bestPriority = tmpPriority;
            }
        }

        return best;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Calculate the priority of a candidate given a state.
     * @param candidate the candidate in the pool.
     * @param state the state.
     * @return the priority of the candidate.
     */
    public abstract double priority(T candidate, State state);
}
