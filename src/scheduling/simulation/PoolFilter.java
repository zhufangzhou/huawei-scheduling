package scheduling.simulation;

import java.util.List;

/**
 * A pool filter uses some criteria to filter out candidate from a pool given a state.
 * This is a preprocessing to help improve the effectiveness and efficiency of
 * decision making during the simulation (decision making process).
 */

public abstract class PoolFilter <T> {
    public abstract List<T> filter(List<T> pool, State state);
}
