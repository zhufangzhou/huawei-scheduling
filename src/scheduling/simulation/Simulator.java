package scheduling.simulation;

import io.ExcelProcessor;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import scheduling.core.Environment;
import scheduling.core.input.*;
import scheduling.core.output.SupplyInstruction;
import scheduling.simulation.event.*;

import java.io.File;
import java.util.*;

public class Simulator {
    public static final int MAX_PERIOD = 180; // the maximal scheduling period

    protected Environment env;
    protected Rule rule;
    protected State state;
    protected Map<Integer, List<Event>> envUpdateEventMap;
    protected Map<Integer, List<Event>> schedulingEventMap;

//    public Simulator(int startDate, Environment env, Rule rule) {
//        this.env = env;
//        this.rule = rule;
//
//        state = new State(startDate);
//        envUpdateEventMap = new HashMap<>();
//        schedulingEventMap = new HashMap<>();
//        int currDate = startDate;
//        int count = 0;
//        while (count < MAX_PERIOD) {
//            envUpdateEventMap.put(currDate, new ArrayList<>());
//            schedulingEventMap.put(currDate, new ArrayList<>());
//
//            currDate = TimePeriod.nextDate(currDate);
//            count ++;
//        }
//    }
//
    public Environment getEnvironment() {
        return env;
    }

    public Rule getRule() {
        return rule;
    }

    public State getState() {
        return state;
    }

    public Map<Integer, List<Event>> getEnvUpdateEventMap() {
        return envUpdateEventMap;
    }

    public Map<Integer, List<Event>> getSchedulingEventMap() {
        return schedulingEventMap;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setEnvUpdateEventMap(Map<Integer, List<Event>> envUpdateEventMap) {
        this.envUpdateEventMap = envUpdateEventMap;
    }

    public void setSchedulingEventMap(Map<Integer, List<Event>> schedulingEventMap) {
        this.schedulingEventMap = schedulingEventMap;
    }
//
//    public void run() {
//        int currDate = state.getDate();
//        int duration = 0;
//
//        Map<Item, Map<Integer, Demand>> demandMap = state.getDemandMap();
//        Map<Item, Map<Plant, Integer>> inventoryMap = state.getInventoryMap();
//
//        while (duration < MAX_PERIOD) {
//            // trigger all the environment update events
//            for (Event event : envUpdateEventMap.get(currDate)) {
//                event.trigger(this);
//            }
//
//            // get the requested items, which with positive demands today
//            List<Item> requestedItems = new ArrayList<>();
//            for (Item item : demandMap.keySet()) {
//                Demand dem = demandMap.get(item).get(currDate);
//                if (dem == null)
//                    continue;
//
//                int orderDemand = dem.getOrderDemand();
//
//                if (orderDemand > 0)
//                    requestedItems.add(item);
//            }
//
//            // make/adjust the schedule
//
//            // supply the demand as soon as there are inventory
//            for (Item item : requestedItems) {
//                for (Plant plant : inventoryMap.get(item).keySet()) {
//                    int inventory = inventoryMap.get(item).get(plant);
//
//                    if (inventory > 0) {
//                        SupplyInstruction supply = new SupplyInstruction(currDate, currDate, item, plant, inventory);
//                        schedulingEventMap.get(currDate).add(new SupplyEvent(currDate, supply));
//                    }
//                }
//            }
//
//            // trigger all the scheduling events
//            for (Event event : schedulingEventMap.get(currDate)) {
//                event.trigger(this);
//            }
//
//            // finally, calculate the delay of the requested items for that day.
//            for (Item item : requestedItems) {
//                Demand dem = demandMap.get(item).get(currDate);
//                int orderDemand = dem.getOrderDemand();
//
//                if (orderDemand > 0) {
//                    state.getDelayMap().get(item).put(currDate, orderDemand);
//                    state.setTotalDelay(state.getTotalDelay()+orderDemand);
//                }
//            }
//
//            // go to the next day
//            currDate = TimePeriod.nextDate(currDate);
//            duration ++;
//        }
//
//
//    }
//
//    public void applyRule() {
//
//    }
//
//    public static Simulator readFromFiles(int startDate, List<File> files) {
//        // read the global environment from all the files
//        Map<String, ProductCategory> productCategoryMap = new HashMap<>();
//        Map<String, Plant> plantMap = new HashMap<>();
//        Map<String, MachineSet> machineSetMap = new HashMap<>();
//        Map<String, Item> itemMap = new HashMap<>();
//        Map<Pair<Plant, Plant>, Integer> transitCostMap = new HashMap<>();
//        Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap = new HashMap<>();
//
//        for (int k = files.size()-1; k >= 0; k--) {
//            // read from last to first, so the first occurrence will be stored
//            File file = files.get(k);
//
//            System.out.println("reading " + file.toString());
//
//            try {
//                XSSFWorkbook wb = new XSSFWorkbook(file);
//                XSSFSheet sheet;
//                XSSFRow row;
//
//                DataFormatter df = new DataFormatter();
//
//                // read the product catogories
//                sheet = wb.getSheetAt(ExcelProcessor.PRODUCT_CATEGORY_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    String name = df.formatCellValue(row.getCell(0));
//                    double fr = Double.valueOf(df.formatCellValue(row.getCell(1)));
//
//                    ProductCategory pc = new ProductCategory(name, fr);
//                    productCategoryMap.put(name, pc);
//                }
//
//                // read the plants
//                sheet = wb.getSheetAt(ExcelProcessor.PLANT_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    String name = df.formatCellValue(row.getCell(0));
//                    int lod = Integer.valueOf(df.formatCellValue(row.getCell(1)));
//                    String type = df.formatCellValue(row.getCell(1));
//
//                    Plant plant = new Plant(name, lod, type);
//                    plantMap.put(name, plant);
//                }
//
//                // read the machine sets
//                sheet = wb.getSheetAt(ExcelProcessor.MACHINE_SET_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    String name = df.formatCellValue(row.getCell(0));
//                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(1)));
//                    CapacityType ct = CapacityType.get(df.formatCellValue(row.getCell(2)));
//                    double sf = ExcelProcessor.readDoubleCell(row.getCell(3));
//
//                    MachineSet machineSet = new MachineSet(name, plant, ct, sf);
//
//                    machineSetMap.put(name, machineSet);
//                    plant.putMachineSet(machineSet);
//                }
//
//                // read the items
//                sheet = wb.getSheetAt(ExcelProcessor.ITEM_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    String id = df.formatCellValue(row.getCell(0));
//                    ItemType type = ItemType.get(df.formatCellValue(row.getCell(1)));
//                    double mcost = Double.valueOf(df.formatCellValue(row.getCell(2)));
//                    ProductCategory pc = productCategoryMap.get(df.formatCellValue(row.getCell(5)));
//                    double hcost = Double.valueOf(df.formatCellValue(row.getCell(6)));
//
//                    Item item = new Item(id, type, mcost, pc, hcost);
//
//                    itemMap.put(item.getId(), item);
//                }
//
//                // read the transit information and merge into plants and item
//                sheet = wb.getSheetAt(ExcelProcessor.TRANSIT_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
//                    Plant fromPlant = plantMap.get(df.formatCellValue(row.getCell(1)));
//                    Plant toPlant = plantMap.get(df.formatCellValue(row.getCell(2)));
//                    int cost = Integer.valueOf(df.formatCellValue(row.getCell(3)));
//                    int leadTime = Integer.valueOf(df.formatCellValue(row.getCell(4)));
//
//                    // transit maps
//                    transitCostMap.put(new Pair(fromPlant, toPlant), cost);
//                    transitLeadTimeMap.put(new Pair(fromPlant, toPlant), leadTime);
//
//                    // plant transit maps
//                    fromPlant.putTransitOutMap(toPlant, item);
//                    toPlant.putTransitInMap(fromPlant, item);
//
//                    // item transits
//                    item.addTransit(fromPlant, toPlant);
//                }
//
//                // read the production and merge into items
//                sheet = wb.getSheetAt(ExcelProcessor.PRODUCTION_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
//                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(1)));
//                    double cost = Double.valueOf(df.formatCellValue(row.getCell(2)));
//                    int leadTime = Integer.valueOf(df.formatCellValue(row.getCell(4)));
//                    int preWeekProd = Integer.valueOf(df.formatCellValue(row.getCell(5)));
//                    int wtdProd = Integer.valueOf(df.formatCellValue(row.getCell(6)));
//                    int lotSize = Integer.valueOf(df.formatCellValue(row.getCell(7)));
//                    int minProd = Integer.valueOf(df.formatCellValue(row.getCell(8)));
//                    int maxProd = Integer.valueOf(df.formatCellValue(row.getCell(9)));
//                    int fds = Integer.valueOf(df.formatCellValue(row.getCell(10)));
//
//                    Production production = new Production(item, plant, cost, leadTime,
//                            preWeekProd, wtdProd, lotSize, minProd, maxProd, fds);
//
//                    item.putProduction(production);
//                }
//
//                // read the bom and merge into productions
//                sheet = wb.getSheetAt(ExcelProcessor.PLANT_BOM_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    Item assembly = itemMap.get(df.formatCellValue(row.getCell(0)));
//                    Item component = itemMap.get(df.formatCellValue(row.getCell(1)));
//                    int quantity = Integer.valueOf(df.formatCellValue(row.getCell(2)));
//                    SupplyType st = SupplyType.get(df.formatCellValue(row.getCell(3)));
//                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(8)));
//
//                    BomComponent bom = new BomComponent(component, quantity, st);
//                    assembly.getProduction(plant).addBom(bom);
//                }
//
//                // read the item capacity type and rate
//                sheet = wb.getSheetAt(ExcelProcessor.RATE_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
//                    CapacityType capacityType = CapacityType.get(df.formatCellValue(row.getCell(1)));
//                    double rate = Double.valueOf(df.formatCellValue(row.getCell(2)));
//
//                    item.setCapacityType(capacityType);
//                    item.setRate(rate);
//                }
//
//                // read the item sets and merge into items
//                sheet = wb.getSheetAt(ExcelProcessor.ITEM_SETS_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    MachineSet machineSet = machineSetMap.get(df.formatCellValue(row.getCell(0)));
//                    Item item = itemMap.get(df.formatCellValue(row.getCell(1)));
//
//
//                    item.getMachineMap().put(machineSet.getPlant(), machineSet);
//                }
//
//                // Read the initial inventories and merge into items
//                sheet = wb.getSheetAt(ExcelProcessor.INIT_INVENTORY_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
//                    int quantity = Integer.valueOf(df.formatCellValue(row.getCell(1)));
//                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(2)));
//
//                    item.getInitInventoryMap().put(plant, quantity);
//                }
//
//                // closing the workbook
//                wb.close();
//            } catch(Exception ioe) {
//                ioe.printStackTrace();
//            }
//        }
//
//        Environment env = new Environment(productCategoryMap, plantMap, transitCostMap, transitLeadTimeMap, machineSetMap, itemMap);
//
//        Simulator simulator = new Simulator(startDate, env, null);
//
//        // set the starting date
//        int currDate = startDate;
//
//        // read the first file for the initial work-in-process and frozen productions
//        try {
//            XSSFWorkbook wb = new XSSFWorkbook(files.get(0));
//            XSSFSheet sheet;
//            XSSFRow row;
//
//            DataFormatter df = new DataFormatter();
//
//            // read the time periods
//            Map<Integer, TimePeriod> timePeriodMap = new HashMap<>();
//            sheet = wb.getSheetAt(ExcelProcessor.TIME_PERIOD_IDX);
//            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                row = sheet.getRow(i);
//
//                int date = Integer.valueOf(df.formatCellValue(row.getCell(0)));
//                int week = Integer.valueOf(df.formatCellValue(row.getCell(2)));
//                int length = Integer.valueOf(df.formatCellValue(row.getCell(3)));
//
//                TimePeriod timePeriod = new TimePeriod(date, week, length);
//                timePeriodMap.put(date, timePeriod);
//            }
//
//            // event: work-in-process production finish
//            sheet = wb.getSheetAt(ExcelProcessor.WORK_IN_PROCESS_IDX);
//            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                row = sheet.getRow(i);
//
//                Item item = env.getItemMap().get(df.formatCellValue(row.getCell(0)));
//                int date = Integer.valueOf(df.formatCellValue(row.getCell(1)));
//                int quantity = Integer.valueOf(df.formatCellValue(row.getCell(2)));
//                Plant plant = env.getPlantMap().get(df.formatCellValue(row.getCell(3)));
//
//                Event event = new FrozenWorkEndEvent(date, item, plant, quantity);
//                simulator.getSchedulingEventMap().get(date).add(event);
//            }
//
//            // frozon productions finish
//            sheet = wb.getSheetAt(ExcelProcessor.FROZEN_PROD_IDX);
//            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                row = sheet.getRow(i);
//
//                Item item = env.getItemMap().get(df.formatCellValue(row.getCell(0)));
//                Plant plant = env.getPlantMap().get(df.formatCellValue(row.getCell(1)));
//                int date = Integer.valueOf(df.formatCellValue(row.getCell(2)));
//                TimePeriod timePeriod = timePeriodMap.get(date);
//                int quantity = Integer.valueOf(df.formatCellValue(row.getCell(3)));
//
//                Event event = new FrozenWorkEndEvent(timePeriod.getEndDate(), item, plant, quantity);
//                simulator.getSchedulingEventMap().get(timePeriod.getEndDate()).add(event);
//            }
//
//            // Closing the workbook
//            wb.close();
//        } catch(Exception ioe) {
//            ioe.printStackTrace();
//        }
//
//        // create the events
//        for (int k = 0; k < files.size(); k++) {
//            File file = files.get(k);
//
//            System.out.println("reading " + file.toString());
//
//            try {
//                XSSFWorkbook wb = new XSSFWorkbook(file);
//                XSSFSheet sheet;
//                XSSFRow row;
//
//                DataFormatter df = new DataFormatter();
//
//                // read the time periods
//                Map<Integer, TimePeriod> timePeriodMap = new HashMap<>();
//                sheet = wb.getSheetAt(ExcelProcessor.TIME_PERIOD_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    int date = Integer.valueOf(df.formatCellValue(row.getCell(0)));
//                    int week = Integer.valueOf(df.formatCellValue(row.getCell(2)));
//                    int length = Integer.valueOf(df.formatCellValue(row.getCell(3)));
//
//                    TimePeriod timePeriod = new TimePeriod(date, week, length);
//                    timePeriodMap.put(date, timePeriod);
//                }
//
//                // event: open new plants
//                sheet = wb.getSheetAt(ExcelProcessor.PLANT_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    String name = df.formatCellValue(row.getCell(0));
//
//                    if (simulator.getState().getPlantMap().containsKey(name))
//                        continue;
//
//                    Plant plant = plantMap.get(name);
//                    Event event = new NewPlantEvent(currDate, plant);
//                    simulator.getEnvUpdateEventMap().get(currDate).add(event);
//                }
//
//                // event: see new item, initialise its inventory
//                sheet = wb.getSheetAt(ExcelProcessor.INIT_INVENTORY_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    Item item = env.getItemMap().get(df.formatCellValue(row.getCell(0)));
//
//                    if (simulator.getState().getInventoryMap().containsKey(item))
//                        continue;
//
//                    Event event = new NewItemEvent(currDate, item);
//                    simulator.getEnvUpdateEventMap().get(currDate).add(event);
//                }
//
//                // event: update item demands
//                sheet = wb.getSheetAt(ExcelProcessor.DEMAND_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
//                    int date = Integer.valueOf(df.formatCellValue(row.getCell(1)));
//                    int odem = Integer.valueOf(df.formatCellValue(row.getCell(2)));
//                    int fdem = Integer.valueOf(df.formatCellValue(row.getCell(3)));
//
//                    TimePeriod timePeriod = timePeriodMap.get(date);
//                    Demand dem = new Demand(odem, fdem);
//
//                    Event event = new OrderDemandUpdateEvent(currDate, item, timePeriod.getEndDate(), dem);
//                    simulator.getEnvUpdateEventMap().get(currDate).add(event);
//                }
//
//                // event: update the capacity of the machine sets
//                Map<MachineSet, Map<TimePeriod, Capacity>> capacityMap = new HashMap<>();
//                sheet = wb.getSheetAt(ExcelProcessor.CAPACITY_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    MachineSet set = machineSetMap.get(df.formatCellValue(row.getCell(0)));
//                    int date = Integer.valueOf(df.formatCellValue(row.getCell(1)));
//                    double capacity = Double.valueOf(df.formatCellValue(row.getCell(4)));
//
//                    TimePeriod timePeriod = timePeriodMap.get(date);
//
//                    Map<TimePeriod, Capacity> setCapacityMap = capacityMap.get(set);
//                    if (setCapacityMap == null) {
//                        setCapacityMap = new HashMap<>();
//                        capacityMap.put(set, setCapacityMap);
//                    }
//
//                    setCapacityMap.put(timePeriod, new Capacity(capacity));
//                }
//
//                for (MachineSet set : capacityMap.keySet()) {
//                    Event event = new CapacityUpdateEvent(currDate, set, capacityMap.get(set));
//                    simulator.getEnvUpdateEventMap().get(currDate).add(event);
//                }
//
//                // event: the raw material po for today
//                sheet = wb.getSheetAt(ExcelProcessor.RAW_MATERIAL_PO_IDX);
//                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                    row = sheet.getRow(i);
//
//                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
//                    int quantity = Integer.valueOf(df.formatCellValue(row.getCell(1)));
//                    int date = Integer.valueOf(df.formatCellValue(row.getCell(2)));
//                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(3)));
//
//                    Event event = new PoEvent(date, item, plant, quantity);
//                    simulator.getEnvUpdateEventMap().get(date).add(event);
//                }
//
//                // Closing the workbook
//                wb.close();
//            } catch(Exception ioe) {
//                ioe.printStackTrace();
//            }
//
//            currDate = TimePeriod.nextDate(currDate);
//        }
//
//        return simulator;
//    }
//
//    /**
//     * Read frozen days from the Excel file.
//     * Merge the frozen days into the plants.
//     * @param file the frozen day file.
//     */
//    public void readFrozenDays(File file) {
//        try {
//            XSSFWorkbook wb = new XSSFWorkbook(file);
//            XSSFSheet sheet;
//            XSSFRow row;
//
//            DataFormatter df = new DataFormatter();
//
//            sheet = wb.getSheetAt(0);
//            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                row = sheet.getRow(i);
//
//                Plant plant = env.getPlantMap().get(df.formatCellValue(row.getCell(0)));
//                ItemType itemType = ItemType.get(df.formatCellValue(row.getCell(1)));
//                int frozenDays = Integer.valueOf(df.formatCellValue(row.getCell(2)));
//
//                plant.getFrozenDaysMap().put(itemType, frozenDays);
//            }
//
//            // closing the workbook
//            wb.close();
//        } catch(Exception ioe) {
//            ioe.printStackTrace();
//        }
//    }
//
//    public Simulator deepClone() {
//        Simulator cloned = new Simulator(state.getDate(), env, rule);
//        cloned.setState(state.deepClone());
//        cloned.setEnvUpdateEventMap(new HashMap<>(envUpdateEventMap));
//        cloned.setSchedulingEventMap(new HashMap<>(schedulingEventMap));
//
//        return cloned;
//    }

//    public static void main(String[] args) {
//        int startDate = 20161203;
//
//        List<File> files = new ArrayList<>();
//
//        for (int num = 1; num < 32; num++) {
//            String text = (num < 10 ? "0" : "") + num;
//            File file = new File("data/e_vuw_test_multi_plant_" + text + ".xlsx");
//            files.add(file);
//        }
//
//        Simulator simulator = Simulator.readFromFiles(startDate, files);
//        simulator.readFrozenDays(new File("data/o_vuw_frozen_days.xlsx"));
//
////        File file = new File("data/e_vuw_test_multi_plant_01.xlsx");
////        Environment env = Environment.readFromFile(file);
////
////        Simulator simulator = new Simulator(startDate, env, null);
//
//        System.out.println("starting simulation.");
//
//        simulator.run();
//
//        System.out.println("finished.");
//    }
}
