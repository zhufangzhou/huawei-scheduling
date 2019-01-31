package scheduling.core.simulation;

import io.ExcelProcessor;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import scheduling.core.Environment;
import scheduling.core.input.*;
import scheduling.core.simulation.event.*;

import java.io.File;
import java.util.*;

public class Simulator {
    private Environment env;
    private Rule rule;
    private State state;
    private PriorityQueue<Event> eventQueue;

    public Simulator(int startDate, Environment env, Rule rule) {
        this.env = env;
        this.rule = rule;

        state = new State(startDate);
        eventQueue = new PriorityQueue<>();
    }

    public Environment getEnvironment() {
        return env;
    }

    public Rule getRule() {
        return rule;
    }

    public State getState() {
        return state;
    }

    public PriorityQueue<Event> getEventQueue() {
        return eventQueue;
    }

    public void run() {

    }

    public void applyRule() {

    }

    public static Simulator readFromFiles(int startDate, List<File> files) {
        // read the global environment from all the files
        Map<String, ProductCategory> productCategoryMap = new HashMap<>();
        Map<String, Plant> plantMap = new HashMap<>();
        Map<String, MachineSet> machineSetMap = new HashMap<>();
        Map<String, Item> itemMap = new HashMap<>();
        Map<Pair<Plant, Plant>, Integer> transitCostMap = new HashMap<>();
        Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap = new HashMap<>();

        for (int k = files.size()-1; k >= 0; k--) {
            // read from last to first, so the first occurrence will be stored
            File file = files.get(k);

            System.out.println("reading " + file.toString());

            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet;
                XSSFRow row;

                DataFormatter df = new DataFormatter();

                // read the product catogories
                sheet = wb.getSheetAt(ExcelProcessor.PRODUCT_CATEGORY_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    String name = df.formatCellValue(row.getCell(0));
                    double fr = Double.valueOf(df.formatCellValue(row.getCell(1)));

                    ProductCategory pc = new ProductCategory(name, fr);
                    productCategoryMap.put(name, pc);
                }

                // read the plants
                sheet = wb.getSheetAt(ExcelProcessor.PLANT_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    String name = df.formatCellValue(row.getCell(0));
                    int lod = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                    String type = df.formatCellValue(row.getCell(1));

                    Plant plant = new Plant(name, lod, type);
                    plantMap.put(name, plant);
                }

                // read the machine sets
                sheet = wb.getSheetAt(ExcelProcessor.MACHINE_SET_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    String name = df.formatCellValue(row.getCell(0));
                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(1)));
                    CapacityType ct = CapacityType.get(df.formatCellValue(row.getCell(2)));
                    double sf = ExcelProcessor.readDoubleCell(row.getCell(3));

                    MachineSet machineSet = new MachineSet(name, plant, ct, sf);

                    machineSetMap.put(name, machineSet);
                    plant.putMachineSet(machineSet);
                }

                // read the items
                sheet = wb.getSheetAt(ExcelProcessor.ITEM_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    String id = df.formatCellValue(row.getCell(0));
                    ItemType type = ItemType.get(df.formatCellValue(row.getCell(1)));
                    double mcost = Double.valueOf(df.formatCellValue(row.getCell(2)));
                    ProductCategory pc = productCategoryMap.get(df.formatCellValue(row.getCell(5)));
                    double hcost = Double.valueOf(df.formatCellValue(row.getCell(6)));

                    Item item = new Item(id, type, mcost, pc, hcost);

                    itemMap.put(item.getId(), item);
                }

                // read the transit information and merge into plants and item
                sheet = wb.getSheetAt(ExcelProcessor.TRANSIT_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                    Plant fromPlant = plantMap.get(df.formatCellValue(row.getCell(1)));
                    Plant toPlant = plantMap.get(df.formatCellValue(row.getCell(2)));
                    int cost = Integer.valueOf(df.formatCellValue(row.getCell(3)));
                    int leadTime = Integer.valueOf(df.formatCellValue(row.getCell(4)));

                    // transit maps
                    transitCostMap.put(new Pair(fromPlant, toPlant), cost);
                    transitLeadTimeMap.put(new Pair(fromPlant, toPlant), leadTime);

                    // plant transit maps
                    fromPlant.putTransitOutMap(toPlant, item);
                    toPlant.putTransitInMap(fromPlant, item);

                    // item transits
                    item.addTransit(fromPlant, toPlant);
                }

                // read the production and merge into items
                sheet = wb.getSheetAt(ExcelProcessor.PRODUCTION_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(1)));
                    double cost = Double.valueOf(df.formatCellValue(row.getCell(2)));
                    int leadTime = Integer.valueOf(df.formatCellValue(row.getCell(4)));
                    int preWeekProd = Integer.valueOf(df.formatCellValue(row.getCell(5)));
                    int wtdProd = Integer.valueOf(df.formatCellValue(row.getCell(6)));
                    int lotSize = Integer.valueOf(df.formatCellValue(row.getCell(7)));
                    int minProd = Integer.valueOf(df.formatCellValue(row.getCell(8)));
                    int maxProd = Integer.valueOf(df.formatCellValue(row.getCell(9)));
                    int fds = Integer.valueOf(df.formatCellValue(row.getCell(10)));

                    Production production = new Production(item, plant, cost, leadTime,
                            preWeekProd, wtdProd, lotSize, minProd, maxProd, fds);

                    item.putProduction(production);
                }

                // read the bom and merge into productions
                sheet = wb.getSheetAt(ExcelProcessor.PLANT_BOM_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    Item assembly = itemMap.get(df.formatCellValue(row.getCell(0)));
                    Item component = itemMap.get(df.formatCellValue(row.getCell(1)));
                    int quantity = Integer.valueOf(df.formatCellValue(row.getCell(2)));
                    SupplyType st = SupplyType.get(df.formatCellValue(row.getCell(3)));
                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(8)));

                    Bom bom = new Bom(component, quantity, st);
                    assembly.getProduction(plant).addBom(bom);
                }

                // read the item capacity type and rate
                sheet = wb.getSheetAt(ExcelProcessor.RATE_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                    CapacityType capacityType = CapacityType.get(df.formatCellValue(row.getCell(1)));
                    double rate = Double.valueOf(df.formatCellValue(row.getCell(2)));

                    item.setCapacityType(capacityType);
                    item.setRate(rate);
                }

                // read the item sets and merge into items
                sheet = wb.getSheetAt(ExcelProcessor.ITEM_SETS_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    MachineSet machineSet = machineSetMap.get(df.formatCellValue(row.getCell(0)));
                    Item item = itemMap.get(df.formatCellValue(row.getCell(1)));


                    item.getMachineMap().put(machineSet.getPlant(), machineSet);
                }

                // Read the initial inventories and merge into items
                sheet = wb.getSheetAt(ExcelProcessor.INIT_INVENTORY_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                    int quantity = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(2)));

                    item.getInitInventoryMap().put(plant, quantity);
                }

                // closing the workbook
                wb.close();
            } catch(Exception ioe) {
                ioe.printStackTrace();
            }
        }

        Environment env = new Environment(productCategoryMap, plantMap, transitCostMap, transitLeadTimeMap, machineSetMap, itemMap);

        Simulator simulator = new Simulator(startDate, env, null);

        // set the starting date
        int currDate = startDate;

        // create the events
        for (int k = 0; k < files.size(); k++) {
            File file = files.get(k);

            System.out.println("reading " + file.toString());

            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet;
                XSSFRow row;

                DataFormatter df = new DataFormatter();

                // read the time periods
                Map<Integer, TimePeriod> timePeriodMap = new HashMap<>();
                sheet = wb.getSheetAt(ExcelProcessor.TIME_PERIOD_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    int date = Integer.valueOf(df.formatCellValue(row.getCell(0)));
                    int week = Integer.valueOf(df.formatCellValue(row.getCell(2)));
                    int length = Integer.valueOf(df.formatCellValue(row.getCell(3)));

                    TimePeriod timePeriod = new TimePeriod(date, week, length);
                    timePeriodMap.put(date, timePeriod);
                }

                // event: open new plants
                sheet = wb.getSheetAt(ExcelProcessor.PLANT_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    String name = df.formatCellValue(row.getCell(0));

                    if (simulator.getState().getPlantMap().containsKey(name))
                        continue;

                    Plant plant = plantMap.get(name);
                    Event event = new NewPlantEvent(currDate, plant);
                    simulator.getEventQueue().add(event);

                }

                // event: see new item, initialise its inventory
                sheet = wb.getSheetAt(ExcelProcessor.INIT_INVENTORY_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    Item item = env.getItemMap().get(df.formatCellValue(row.getCell(0)));

                    if (simulator.getState().getInventoryMap().containsKey(item))
                        continue;

                    Event event = new NewItemEvent(currDate, item);
                    simulator.getEventQueue().add(event);
                }

                // event: update item demands
                sheet = wb.getSheetAt(ExcelProcessor.DEMAND_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                    int date = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                    int odem = Integer.valueOf(df.formatCellValue(row.getCell(2)));
                    int fdem = Integer.valueOf(df.formatCellValue(row.getCell(3)));

                    TimePeriod timePeriod = timePeriodMap.get(date);
                    Demand dem = new Demand(odem, fdem);

                    Event event = new DemandUpdateEvent(currDate, item, timePeriod.getEndDate(), dem);
                    simulator.getEventQueue().add(event);
                }

                // event: update the capacity of the machine sets
                Map<MachineSet, Map<TimePeriod, Capacity>> capacityMap = new HashMap<>();
                sheet = wb.getSheetAt(ExcelProcessor.CAPACITY_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    MachineSet set = machineSetMap.get(df.formatCellValue(row.getCell(0)));
                    int date = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                    double capacity = Double.valueOf(df.formatCellValue(row.getCell(4)));

                    TimePeriod timePeriod = timePeriodMap.get(date);

                    Map<TimePeriod, Capacity> setCapacityMap = capacityMap.get(set);
                    if (setCapacityMap == null) {
                        setCapacityMap = new HashMap<>();
                        capacityMap.put(set, setCapacityMap);
                    }

                    setCapacityMap.put(timePeriod, new Capacity(capacity));
                }

                for (MachineSet set : capacityMap.keySet()) {
                    Event event = new CapacityUpdateEvent(currDate, set, capacityMap.get(set));
                    simulator.getEventQueue().add(event);
                }

                // event: the raw material po for today
                sheet = wb.getSheetAt(ExcelProcessor.RAW_MATERIAL_PO_IDX);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);

                    Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                    int quantity = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                    int date = Integer.valueOf(df.formatCellValue(row.getCell(2)));
                    Plant plant = plantMap.get(df.formatCellValue(row.getCell(3)));

                    Event event = new PoEvent(date, item, plant, quantity);
                    simulator.getEventQueue().add(event);
                }

                // event: work-in-process production finish
                // only read the first one, and optimise the remaining

                // frozon productions finish
                // only read the first one, and create new ones from optimisation


                // Closing the workbook
                wb.close();
            } catch(Exception ioe) {
                ioe.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Initialise from the initial file.
     * Initialise the plants in the system, and the initial inventories.
     * @param file the initial file.
     */
    public void initialiseFromFile(File file) {
        try {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet;
            XSSFRow row;

            DataFormatter df = new DataFormatter();

            // initialise the plant map
            sheet = wb.getSheetAt(ExcelProcessor.PLANT_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String name = df.formatCellValue(row.getCell(0));

                Plant plant = env.getPlantMap().get(name);
                state.getPlantMap().put(plant.getName(), plant);
            }

            // initialise item inventory
            sheet = wb.getSheetAt(ExcelProcessor.INIT_INVENTORY_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = env.getItemMap().get(df.formatCellValue(row.getCell(0)));
                int quantity = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                Plant plant = env.getPlantMap().get(df.formatCellValue(row.getCell(2)));

                Map<Plant, Integer> itemInvMap = state.getInventoryMap().get(item);
                if (itemInvMap == null) {
                    itemInvMap = new HashMap<>();
                    state.getInventoryMap().put(item, itemInvMap);
                }
                itemInvMap.put(plant, quantity);
            }

            // Closing the workbook
            wb.close();
        } catch(Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int startDate = 20161203;

        List<File> files = new ArrayList<>();

        for (int num = 1; num < 32; num++) {
            String text = (num < 10 ? "0" : "") + num;
            File file = new File("data/e_vuw_test_multi_plant_" + text + ".xlsx");
            files.add(file);
        }

        Simulator simulator = Simulator.readFromFiles(startDate, files);


//        File file = new File("data/e_vuw_test_multi_plant_01.xlsx");
//        Environment env = Environment.readFromFile(file);
//
//        Simulator simulator = new Simulator(startDate, env, null);

        System.out.println("finished");
    }
}
