package scheduling.simulation.rule;

import scheduling.core.SupplyChain;
import scheduling.simulation.PriorityRule;
import scheduling.simulation.State;

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
