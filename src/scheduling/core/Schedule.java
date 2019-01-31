package scheduling.core;

import scheduling.core.output.*;

import java.util.*;

public class Schedule {
    private List<ProductionInstruction> productionSchedule;
    private List<TransitInstruction> transitSchedule;
    private List<SupplyInstruction> supplySchedule;

    public Schedule(List<ProductionInstruction> productionSchedule, List<TransitInstruction> transitSchedule, List<SupplyInstruction> supplySchedule) {
        this.productionSchedule = productionSchedule;
        this.transitSchedule = transitSchedule;
        this.supplySchedule = supplySchedule;
    }

    public Schedule() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public List<ProductionInstruction> getProductionSchedule() {
        return productionSchedule;
    }

    public List<TransitInstruction> getTransitSchedule() {
        return transitSchedule;
    }

    public List<SupplyInstruction> getSupplySchedule() {
        return supplySchedule;
    }
}
