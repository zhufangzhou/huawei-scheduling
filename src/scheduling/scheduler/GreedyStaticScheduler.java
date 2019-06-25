package scheduling.scheduler;

import org.apache.commons.math3.util.Pair;
import scheduling.core.Schedule;
import scheduling.core.SupplyChain;
import scheduling.core.input.*;
import scheduling.core.output.SupplyInstruction;
import scheduling.simulation.PriorityRule;
import scheduling.simulation.State;
import scheduling.simulation.TieBreaker;
import scheduling.simulation.rule.ShortestChainFirst;
import scheduling.simulation.rule.UrgentDemandFirst;
import scheduling.simulation.tiebreaker.SimpleTieBreaker;

import java.util.*;

public class GreedyStaticScheduler extends Scheduler {
    private PriorityRule<SupplyChain> chainRule = new ShortestChainFirst();
    private TieBreaker<SupplyChain> chainTB = new SimpleTieBreaker<>();
    private Comparator<Demand> demandRanker = new UrgentDemandFirst();

    public GreedyStaticScheduler() {
    }

    @Override
    public void planSchedule(State state) {
        Schedule schedule = state.getPlannedSchedule();

        // generate the list of all demands
        List<Demand> demands = new ArrayList<>();
        for (Item item : state.getEnv().getItemMap().values()) {
            for (int dateId : item.getOrderDemandMap().keySet()) {
                Demand od = new OrderDemand(dateId, item, item.getOrderDemandMap().get(dateId));
                demands.add(od);
            }

            TreeMap<Integer, Demand> fdMap = new TreeMap<>();
            for (int dateId : item.getForecastDemandMap().keySet()) {
                Demand fd = new ForecastDemand(dateId, item, item.getForecastDemandMap().get(dateId));
                demands.add(fd);
            }
        }

        Collections.sort(demands, demandRanker);

        for (Demand dem : demands) {
            supplyDemand(dem, schedule, state, chainRule, chainTB);
        }

//        for (int d = schedule.getStartDateId(); d < schedule.getEndDateId(); d++) {
//            for (SupplyInstruction supplyInstruction : schedule.getSupplySchedule().get(d).values()) {
//                if (supplyInstruction.getQuantity() <= 0) {
//                    System.out.println("negative supply!");
//                }
//            }
//        }

//        for (int dateId = 0; dateId < state.getEnv().getPeriod(); dateId++) {
//            Map<Item, Long> dailyAccOrderDem = schedule.getAccOrderDemMap().get(dateId);
//
//            for (Item item : dailyAccOrderDem.keySet()) {
//                long left = dailyAccOrderDem.get(item);
//                while (left > 0) {
//                    SupplyChain nextChain = nextSupplyChain(dateId, item, schedule, state, chainRule, chainTB);
//
//                    if (nextChain == null)
//                        break;
//
//                    if (nextChain.getLength() > 1)
//                        System.out.println("debug");
//
//                    long suppQuantity = nextChain.getMaxQuantity();
//                    if (suppQuantity > left)
//                        suppQuantity = left;
//
////                    System.out.println(nextChain.toString() + ": " + suppQuantity + "/" + left);
//
//                    nextChain.addToSchedule(dateId, suppQuantity, schedule);
//                    // supply the order demand
//                    Pair<Item, Plant> supply = new Pair<>(item, nextChain.getPlant());
//                    schedule.addOrderSupply(dateId, supply, suppQuantity);
//
//                    left -= suppQuantity;
//                }
//            }
//        }

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

//    public List<Demand> calcDemandPool(Schedule schedule, State state) {
//        List<Demand> demPool = new LinkedList<>();
//        for (Item item : schedule.getRemOrderDemMap().keySet()) {
//            TreeMap<Integer, Demand> odMap = schedule.getRemOrderDemMap().get(item);
//            int odDate1 = odMap.firstKey();
//            Demand od = odMap.get(odDate1);
//            demPool.add(od);
//        }
//
//        for (Item item : schedule.getRemForcastDemMap().keySet()) {
//            TreeMap<Integer, Demand> fdMap = schedule.getRemForcastDemMap().get(item);
//            int fdDate1 = fdMap.firstKey();
//            Demand fd = fdMap.get(fdDate1);
//            demPool.add(fd);
//        }
//
//        // calculate the next supply chains for each demand in the pool
//        Map<Item, Map<Integer, SupplyChain>> nextChainMap = new HashMap<>();
//        List<Demand> giveup = new LinkedList<>();
//        for (Demand dem : demPool) {
//            int dateId = dem.getDateId();
//            Item item = dem.getItem();
//
//            Map<Integer, SupplyChain> map = new HashMap<>();
//
//            if (nextChainMap.containsKey(item))
//                map = nextChainMap.get(item);
//
//            if (map.containsKey(dateId))
//                continue;
//
//            SupplyChain nextChain = nextSupplyChain(dateId, item, schedule, state, priorityRule1, tieBreaker1);
//
//            // remove this demand if no next chain can be found
//            if (nextChain == null) {
//                giveup.add(dem);
//                continue;
//            }
//
//            map.put(dateId, nextChain);
//            nextChainMap.put(item, map);
//        }
//
//        demPool.removeAll(giveup);
//
//        for (Demand dem : demPool) {
//            int dateId = dem.getDateId();
//            Item item = dem.getItem();
//            SupplyChain nextChain = nextChainMap.get(item).get(dateId);
//
//            dem.setSupplyChain(nextChain);
//        }
//
//        return demPool;
//    }
}
