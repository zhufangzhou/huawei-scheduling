package scheduling.simulation.rule;

import scheduling.core.SupplyChain;

import java.util.Comparator;

/**
 * It is the supply chain ranker, used in rescheduling.
 * It sorts the existing supply chains in the preplanned schedule,
 * add them into the new schedule to fit the new environment.
 *
 * The primary criterion is the provision date = start date + lead time
 * The secondary criterion is the number of steps.
 */

public class UrgentChainFirst implements Comparator<SupplyChain> {

    @Override
    public int compare(SupplyChain o1, SupplyChain o2) {
        double t1 = o1.getDateId()+o1.getLeadTime();
        double t2 = o2.getDateId()+o2.getLeadTime();

        if (t1 < t2)
            return -1;

        if (t1 > t2)
            return 1;

        if (o1.getLength() < o2.getLength())
            return -1;

        if (o1.getLength() > o2.getLength())
            return 1;

        return o1.compareTo(o2);
    }
}
