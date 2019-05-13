package scheduling.scheduler;

import org.apache.commons.math3.util.Pair;
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

        /**
         * Step 1: supply the demand as early as possible
         * this can (1) increase fill rate, and (2) reduce inventory cost
         */
        // we calculate the min production lead time for each item
        // if an order demand comes earlier than some forecast demand
        // by the min production time, then it will have higher priority.
        // because the forecast demand will cause delay for only one day.
        for (int dateId = 0; dateId < state.getEnv().getPeriod(); dateId++) {
            Map<Item, Long> dailyAccOrderDem = schedule.getAccOrderDemMap().get(dateId);

            for (Item item : dailyAccOrderDem.keySet()) {
                if (dailyAccOrderDem.get(item) > 0) {
                    schedule.supplyOrderDemand(item, dateId);
                }
            }

            // check the forecast demands
            List<Item> dailyForecastItems = state.getForecastDemMap().get(dateId);
            if (dailyForecastItems != null) {
                for (Item item : dailyForecastItems) {
                    // check and supply for the more important order demand
                    // which cannot be supplied by production in time
                    int lookAheadDays = item.getMinProductionLeadTime() - 1;

                    // supply the order demand that are more prior to the forecast demand
                    for (int d = dateId + 1; d < dateId + lookAheadDays; d++) {
                        if (d == state.getEnv().getPeriod())
                            break;

                        dailyAccOrderDem = schedule.getAccOrderDemMap().get(d);

                        if (dailyAccOrderDem.containsKey(item) && dailyAccOrderDem.get(item) > 0) {
                            schedule.supplyOrderDemand(item, d);
                        }
                    }

                    // supply the forecast demand
                    schedule.supplyForecastDemand(item, dateId);
                }
            }
        }

        /**
         * Step 2: add productions.
         *
         */

        return schedule;
    }
}
