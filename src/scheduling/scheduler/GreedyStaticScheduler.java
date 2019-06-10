package scheduling.scheduler;

import org.apache.commons.math3.util.Pair;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;
import scheduling.core.input.Item;
import scheduling.core.input.Plant;
import scheduling.core.input.Production;
import scheduling.core.output.SupplyInstruction;
import scheduling.simulation.State;

import java.util.*;

public class GreedyStaticScheduler extends Scheduler {
    public GreedyStaticScheduler() {
    }

    @Override
    public void planSchedule(State state) {
        Schedule schedule = state.getPlannedSchedule();

        for (int dateId = 0; dateId < state.getEnv().getPeriod(); dateId++) {
            Map<Item, Long> dailyAccOrderDem = schedule.getAccOrderDemMap().get(dateId);

            for (Item item : dailyAccOrderDem.keySet()) {
                long left = dailyAccOrderDem.get(item);
                while (left > 0) {
                    SupplyChain nextChain = nextSupplyChain(dateId, item, schedule, state);

                    if (nextChain == null)
                        break;

                    if (nextChain.getLength() > 1)
                        System.out.println("debug");

                    long suppQuantity = nextChain.getMaxQuantity();
                    if (suppQuantity > left)
                        suppQuantity = left;

//                    System.out.println(nextChain.toString() + ": " + suppQuantity + "/" + left);

                    nextChain.addToSchedule(dateId, suppQuantity, schedule);
                    // supply the order demand
                    Pair<Item, Plant> supply = new Pair<>(item, nextChain.getPlant());
                    schedule.addOrderSupply(dateId, supply, suppQuantity);

                    left -= suppQuantity;
                }
            }
        }

//        for (int d = schedule.getStartDateId(); d < schedule.getEndDateId(); d++) {
//            for (SupplyInstruction supplyInstruction : schedule.getSupplySchedule().get(d).values()) {
//                if (supplyInstruction.getQuantity() <= 0) {
//                    System.out.println("debug");
//                }
//            }
//        }
//
//        /**
//         * Step 1: supply the demand as early as possible.
//         * this can (1) increase fill rate, and (2) reduce inventory cost
//         */
//        // we calculate the min production lead time for each item.
//        // if an order demand comes earlier than some forecast demand
//        // by the min production time, then it will be supplied earlier.
//        // because the forecast demand will cause delay for only one day.
//        for (int dateId = 0; dateId < state.getEnv().getPeriod(); dateId++) {
//            Map<Item, Long> dailyAccOrderDem = schedule.getAccOrderDemMap().get(dateId);
//
//            for (Item item : dailyAccOrderDem.keySet()) {
//                if (dailyAccOrderDem.get(item) > 0) {
//                    schedule.supplyOrderDemand(item, dateId);
//                }
//            }
//
//            // check the forecast demands
//            List<Item> dailyForecastItems = state.getForecastDemMap().get(dateId);
//            if (dailyForecastItems != null) {
//                for (Item item : dailyForecastItems) {
//                    // check and supply for the more important order demand
//                    // which cannot be supplied by production in time
//                    int lookAheadDays = item.getMinProductionLeadTime()-1;
//
//                    // supply the order demand that are more prior to the forecast demand
//                    for (int d = dateId + 1; d < dateId + lookAheadDays; d++) {
//                        if (d == state.getEnv().getPeriod())
//                            break;
//
//                        dailyAccOrderDem = schedule.getAccOrderDemMap().get(d);
//
//                        if (dailyAccOrderDem.containsKey(item) && dailyAccOrderDem.get(item) > 0) {
//                            schedule.supplyOrderDemand(item, d);
//                        }
//                    }
//
//                    // supply the forecast demand
//                    schedule.supplyForecastDemand(item, dateId);
//                }
//            }
//        }
//
//        for (int d = schedule.getStartDateId(); d < schedule.getEndDateId(); d++) {
//            for (SupplyInstruction supplyInstruction : schedule.getSupplySchedule().get(d).values()) {
//                if (supplyInstruction.getQuantity() <= 0) {
//                    System.out.println("debug");
//                }
//            }
//        }
//
//        /**
//         * Step 2: add production chains.
//         */
////        for (int dateId = 0; dateId < state.getEnv().getPeriod(); dateId++) {
////            // check the order demand first
////            Map<Item, Long> dailyAccOrderDem = schedule.getAccOrderDemMap().get(dateId);
////
////            List<ProductionChain> chains = schedule.productionChainList(dateId);
////
////            for (Item item : dailyAccOrderDem.keySet()) {
////                long dem = dailyAccOrderDem.get(item);
////
////                if (dem == 0)
////                    continue;
////
////                for (Production prod : item.getProductionMap().values()) {
////                    int leadTime = prod.getLeadTime();
////
////                    if (leadTime > dateId) {
////                        // this production cannot supply the demand in this date
////                        continue;
////                    }
////
////                    Plant plant = prod.getPlant();
////                    int prodStartDate = dateId-leadTime;
////                    long maxProdQuantity = schedule.maxProductionQuantity(prod, prodStartDate);
////                    long maxProdLots = maxProdQuantity / prod.getLotSize();
////
////                    if (maxProdLots == 0)
////                        continue;
////
////                    // the lots required to supply the demand
////                    long demLots = (long)(Math.ceil(1.0 * dem / prod.getLotSize()));
////
////                    // the production lots is the minimum between demLots and maxProdLots
////                    long prodLots = demLots;
////                    if (prodLots > maxProdLots)
////                        prodLots = maxProdLots;
////
////                    schedule.addProduction(prodStartDate, prod, prodLots);
////
////                    // supply the produced item
////                    long suppliedQuantity = prodLots * prod.getLotSize();
////                    if (suppliedQuantity > dem)
////                        suppliedQuantity = dem;
////
////                    schedule.addOrderSupply(dateId, new Pair<>(item, plant), suppliedQuantity);
////
////                    dem -= suppliedQuantity;
////
////                    if (dem == 0)
////                        break;
////                }
////            }
////        }
//
//        for (int d = schedule.getStartDateId(); d < schedule.getEndDateId(); d++) {
//            for (SupplyInstruction supplyInstruction : schedule.getSupplySchedule().get(d).values()) {
//                if (supplyInstruction.getQuantity() <= 0) {
//                    System.out.println("debug");
//                }
//            }
//        }
    }
}
