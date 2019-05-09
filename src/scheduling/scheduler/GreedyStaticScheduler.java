package scheduling.scheduler;

import scheduling.core.Schedule;
import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.simulation.State;

import java.util.*;

public class GreedyStaticScheduler extends Scheduler {
    public GreedyStaticScheduler() {
    }

    @Override
    public Schedule makeSchedule(State state) {
        Schedule schedule = new Schedule();
        schedule.initWithState(state);

        for (int dateId = 0; dateId < state.getEnv().getPeriod(); dateId++) {
            // first priority: supply the order demand
            Map<Item, Long> dailyAccOrderDem = schedule.getAccOrderDemMap().get(dateId);

            


        }

        return schedule;
    }
}
