package scheduling.scheduler;

import scheduling.core.Schedule;
import scheduling.simulation.State;

public class Rescheduler extends Scheduler {

    @Override
    public void planSchedule(State state) {
        Schedule adjustedSchedule = new Schedule();
        adjustedSchedule.initWithState(state);


    }
}
