package scheduling.simulation.poolfilter;

import scheduling.simulation.PoolFilter;
import scheduling.simulation.State;

import java.util.List;

/**
 * The identity pool filter does nothing, but simply returns the pool.
 * It is called "identity" since the filtered pool is the same as the given pool.
 */

public class IdentityPoolFilter<T> extends PoolFilter<T> {
    @Override
    public List<T> filter(List<T> pool, State state) {
        return pool;
    }
}
