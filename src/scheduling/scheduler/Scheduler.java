package scheduling.scheduler;

import scheduling.core.Schedule;
import scheduling.core.SupplyChain;
import scheduling.core.input.Item;
import scheduling.simulation.State;

import java.util.PriorityQueue;

/**
 * An scheduler in this problem is essentially a (re-)scheduler.
 * Given a state (the problem given), it makes a schedule for it.
 */

public abstract class Scheduler {
    public Scheduler() {
    }

    public abstract void planSchedule(State state);

    /**
     * Calculate the next supply chain to supply an item at a date.
     * @param dateId the date id to provide the item.
     * @param item the item to be provided.
     * @param schedule the schedule.
     * @param state the state.
     * @return the next supply chain.
     */
    public SupplyChain nextSupplyChain(int dateId, Item item, Schedule schedule, State state) {
        // set all the chains of the item to be unvisited
        for (SupplyChain chain : state.getSupplyChainMap().get(item).values()) {
            chain.setVisited(false);
        }

        // reactivate all the chains related to this item
        PriorityQueue<SupplyChain> activeChains = new PriorityQueue<>();

        for (SupplyChain chain : state.getSupplyChainMap().get(item).values()) {
            chain.activateStreams(schedule, dateId);

            if (chain.isActive())
                activeChains.add(chain);
        }

        if (activeChains.isEmpty())
            return null;

        SupplyChain nextChain = activeChains.poll();

        return nextChain;
    }
}
