package scheduling.simulation;

import java.util.List;

public class DecisionSituation <T> {
    protected List<T> pool;
    protected State state;

    public DecisionSituation(List<T> pool, State state) {
        this.pool = pool;
        this.state = state;
    }

    public List<T> getPool() {
        return pool;
    }

    public void setPool(List<T> pool) {
        this.pool = pool;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
