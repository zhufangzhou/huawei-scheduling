package scheduling.simulation.rule;

import scheduling.core.input.Demand;

import java.util.Comparator;

/**
 * This is a demand ranker.
 * It sorts the demands based on the priority of the urgency of the demands.
 */

public class UrgentDemandFirst implements Comparator<Demand> {

    @Override
    public int compare(Demand o1, Demand o2) {
        return o1.compareTo(o2);
    }
}
