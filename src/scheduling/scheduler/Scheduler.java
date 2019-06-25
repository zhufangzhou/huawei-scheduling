package scheduling.scheduler;

import org.apache.commons.math3.util.Pair;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;
import scheduling.core.input.Demand;
import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.simulation.DecisionSituation;
import scheduling.simulation.PriorityRule;
import scheduling.simulation.State;
import scheduling.simulation.TieBreaker;

import java.util.*;

/**
 * An scheduler in this problem is essentially a (re-)scheduler.
 * Given a state (the problem given), it makes a schedule for it.
 */

public abstract class Scheduler {
    public Scheduler() {
    }

    public abstract void planSchedule(State state);

    /**
     * Supply a demand using a rule and tie breaker to select the next supply chain.
     * The supply updates the schedule.
     * @param demand the demand to be supplied.
     * @param schedule the schedule to be updated.
     * @param state the state.
     * @param chainRule the rule to select the next supply chain.
     * @param chainTB the tie breaker for the rule.
     */
    public void supplyDemand(Demand demand, Schedule schedule, State state,
                             PriorityRule<SupplyChain> chainRule, TieBreaker<SupplyChain> chainTB) {
//        if (demand.getDateId() == 59 && demand.getItem().toString().equals("243"))
//            System.out.println("debug Schedule.java");

        long left = demand.getQuantity();
        int nextDateId = demand.getDateId();
        while (left > 0) {
            SupplyChain nextChain = nextSupplyChain(nextDateId, demand.latestSupplyDate(schedule), demand.getItem(), schedule, state, chainRule, chainTB);

            if (nextChain == null)
                break;

//            if (nextChain.getLength() > 1)
//                System.out.println("long chain found");

            long suppQuantity = nextChain.getMaxQuantity();
            if (suppQuantity > left)
                suppQuantity = left;

//            System.out.println(demand.getDateId() + "; " + nextChain.toString() + ", " + nextChain.getDateId() + ": " + suppQuantity + "/" + left);

            nextChain.addToSchedule(nextChain.getDateId(), suppQuantity, schedule);
            demand.supplied(nextChain, suppQuantity, schedule);
            schedule.getSupplyChainMap().put(nextChain.cloneActive(), suppQuantity);

            left -= suppQuantity;
            nextDateId = nextChain.getDateId(); // the next chains cannot be earlier than this one
        }
    }

    /**
     * Calculate the next supply chain that can supply an item requested at a specific date.
     * The supply chain will be the earliest, and lead to the minimum delay.
     * @param dateId the date id the item is requested.
     * @param latestDateId the latest date id to provide the item.
     * @param item the item to be provided.
     * @param schedule the schedule.
     * @param state the state.
     * @param chainRule the rule to select the next chain.
     * @param chainTB the tie breaker for chain ties.
     * @return the next supply chain.
     */
    public SupplyChain nextSupplyChain(int dateId, int latestDateId, Item item, Schedule schedule, State state,
                                       PriorityRule<SupplyChain> chainRule, TieBreaker<SupplyChain> chainTB) {
        // reactivate all the chains related to this item
        List<SupplyChain> activeChains = new ArrayList<>();

        for (int d = dateId; d < latestDateId; d++) {
            Set<SupplyChain> visited = new HashSet<>();
            for (SupplyChain chain : state.getSupplyChainMap().get(item).values()) {
                chain.activateStreams(schedule, d, visited);

                if (chain.isActive()) {
                    // if zero inventory and no production, then only transit
                    // skip since we don't need transit as the last step of supply
                    if (chain.getInventory() == 0 && !chain.isProdActive())
                        continue;

                    activeChains.add(chain);
                }
            }

            if (!activeChains.isEmpty())
                break;
        }

        if (activeChains.isEmpty())
            return null;

        DecisionSituation<SupplyChain> ds = new DecisionSituation<>(activeChains, state);
        SupplyChain nextChain = chainRule.next(ds, chainTB);

        return nextChain;
    }
}
