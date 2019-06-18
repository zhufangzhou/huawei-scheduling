package scheduling.simulation.rule;

import scheduling.core.input.Demand;

import java.util.Comparator;

public class UrgentDemandFirst implements Comparator<Demand> {

    @Override
    public int compare(Demand o1, Demand o2) {
        return o1.compareTo(o2);
    }
}
