package scheduling.scheduler;

import scheduling.core.Schedule;
import scheduling.simulation.State;

/**
 * An scheduler in this problem is essentially a (re-)scheduler.
 * Given a state (the problem given), it makes a schedule for it.
 */

public abstract class Scheduler {
    public Scheduler() {
    }

    public abstract Schedule makeSchedule(State state);
}
