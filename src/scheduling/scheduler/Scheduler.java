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
        long left = demand.getQuantity();
        while (left > 0) {
            SupplyChain nextChain = nextSupplyChain(demand.getDateId(), demand.getItem(), schedule, state, chainRule, chainTB);

            if (nextChain == null)
                break;

            if (nextChain.getLength() > 1)
                System.out.println("long chain found");

            long suppQuantity = nextChain.getMaxQuantity();
            if (suppQuantity > left)
                suppQuantity = left;

//                    System.out.println(nextChain.toString() + ": " + suppQuantity + "/" + left);

            nextChain.addToSchedule(nextChain.getDateId(), suppQuantity, schedule);
            demand.supplied(nextChain, suppQuantity, schedule);
            schedule.getSupplyChainMap().put(nextChain.cloneActive(), suppQuantity);

            left -= suppQuantity;
        }
    }

    /**
     * Calculate the next supply chain that can supply an item requested at a specific date.
     * The supply chain will be the earliest, and lead to the minimum delay.
     * @param dateId the date id the item is requested.
     * @param item the item to be provided.
     * @param schedule the schedule.
     * @param state the state.
     * @return the next supply chain.
     */
    public SupplyChain nextSupplyChain(int dateId, Item item, Schedule schedule, State state,
                                       PriorityRule<SupplyChain> chainRule, TieBreaker<SupplyChain> chainTB) {
        // reactivate all the chains related to this item
        List<SupplyChain> activeChains = new ArrayList<>();

        for (int d = dateId; d < schedule.getEndDateId(); d++) {
            // set all the chains of the item to be unvisited
            for (SupplyChain chain : state.getSupplyChainMap().get(item).values()) {
                chain.setVisited(false);
            }

            for (SupplyChain chain : state.getSupplyChainMap().get(item).values()) {
                chain.activateStreams(schedule, dateId);

                if (chain.isActive()) {
                    chain.setDateId(d);
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
