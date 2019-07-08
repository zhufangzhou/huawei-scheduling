package scheduling.simulation.rule;

import scheduling.core.SupplyChain;
import scheduling.simulation.PriorityRule;
import scheduling.simulation.State;

/**
 * This is a priority rule to select the next chain from a pool.
 * The pool should contain the supply chains with the same provision date.
 * The shortest supply chain with the smallest number of steps is chosen.
 * If multiple supply chains have the same length, the one with largest max quantit is selectd.
 */

public class ShortestChainFirst extends PriorityRule<SupplyChain> {
    public ShortestChainFirst() {
        super();
        name = "SCF";
    }

    @Override
    public double priority(SupplyChain candidate, State state) {
        return -candidate.getLength();
    }
}
