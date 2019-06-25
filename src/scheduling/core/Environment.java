package scheduling.core;

import io.ExcelProcessor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import scheduling.core.input.*;
import util.DisjointSets;

import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An environment stores the global information in the scheduling process.
 * The global information does not change dynamically.
 * - all the product categories
 * - all the items and their basic properties during the process
 * - all the plants and their basic properties during the process
 * - all the machines sets and their basic properties during the process
 * - all the transits of items between plants and the cost and lead time
 */

public class Environment {
    private Map<String, ProductCategory> productCategoryMap;
    private Map<String, CapacityType> capacityTypeMap;
    private Map<String, Item> itemMap;
    private Map<String, Plant> plantMap;
    private Map<String, MachineSet> machineSetMap;
    private List<Transit> transits;
//    private Map<Pair<Plant, Plant>, Integer> transitCostMap;
//    private Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap;

    private int startDate; // the start date of the scheduling
    private int endDate; // the end date of the scheduling
    private int period; // the period (number of days) of the scheduling
    private Map<Integer, Integer> remainingWeekDaysMap;

    public Environment(Map<String, ProductCategory> productCategoryMap, Map<String, CapacityType> capacityTypeMap, Map<String, Item> itemMap, Map<String, Plant> plantMap, Map<String, MachineSet> machineSetMap, List<Transit> transits) {
        this.productCategoryMap = productCategoryMap;
        this.capacityTypeMap = capacityTypeMap;
        this.itemMap = itemMap;
        this.plantMap = plantMap;
        this.machineSetMap = machineSetMap;
        this.transits = transits;
    }

    public Map<String, ProductCategory> getProductCategoryMap() {
        return productCategoryMap;
    }

    public void setProductCategoryMap(Map<String, ProductCategory> productCategoryMap) {
        this.productCategoryMap = productCategoryMap;
    }

    public Map<String, CapacityType> getCapacityTypeMap() {
        return capacityTypeMap;
    }

    public void setCapacityTypeMap(Map<String, CapacityType> capacityTypeMap) {
        this.capacityTypeMap = capacityTypeMap;
    }

    public Map<String, Item> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<String, Item> itemMap) {
        this.itemMap = itemMap;
    }

    public Map<String, Plant> getPlantMap() {
        return plantMap;
    }

    public void setPlantMap(Map<String, Plant> plantMap) {
        this.plantMap = plantMap;
    }

    public Map<String, MachineSet> getMachineSetMap() {
        return machineSetMap;
    }

    public void setMachineSetMap(Map<String, MachineSet> machineSetMap) {
        this.machineSetMap = machineSetMap;
    }

    public List<Transit> getTransits() {
        return transits;
    }

    public void setTransits(List<Transit> transits) {
        this.transits = transits;
    }

    public int getStartDate() {
        return startDate;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public Map<Integer, Integer> getRemainingWeekDaysMap() {
        return remainingWeekDaysMap;
    }

    public void setRemainingWeekDaysMap(Map<Integer, Integer> remainingWeekDaysMap) {
        this.remainingWeekDaysMap = remainingWeekDaysMap;
    }

    /**
     * Read the environment from a .xlsx file.
     *
     * @param file the .xlsx file.
     * @return the environment.
     */
    public static Environment readFromFile(File file, FileReader columnNameReader) {
        Environment environment = null;
        SimpleDateFormat targetSdf = new SimpleDateFormat("yyyyMMdd");

        try {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet;
            Map<String, Integer> colIdxMap;
            XSSFRow row;

            DataFormatter df = new DataFormatter();

            ExcelProcessor.initColNames(columnNameReader);

            // Read the time periods
            Map<Integer, TimePeriod> timePeriodMap = new HashMap<>();
            sheet = wb.getSheet(ExcelProcessor.TIME_PERIOD);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.TIME_PERIOD, sheet.getRow(0));

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String rawDate = df.formatCellValue(row.getCell(colIdxMap.get("timePeriod")));
                int date = Integer.valueOf(targetSdf.format(Environment.parseDateStr(rawDate)));
                String rawWeek = df.formatCellValue(row.getCell(colIdxMap.get("week")));
                int week = Integer.valueOf(targetSdf.format(Environment.parseDateStr(rawWeek)));
                int length = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("length"))));

                TimePeriod timePeriod = new TimePeriod(date, week, length);
                timePeriodMap.put(date, timePeriod);
            }

            // get the start and end dates
            int startDate = Integer.MAX_VALUE;
            int endDate = Integer.MIN_VALUE;
            for (int date : timePeriodMap.keySet()) {
                if (date < startDate)
                    startDate = date;

                if (date > endDate)
                    endDate = date;
            }
            int period = TimePeriod.gap(startDate, endDate) + 1;

            Map<Integer, Integer> remainingWeekDaysMap = new HashMap<>();
            int weekStarts = timePeriodMap.get(startDate).getWeek();
            int remainingWeekDays = 6 - TimePeriod.gap(weekStarts, startDate);
            for (int d = 0; d < period; d++) {
                remainingWeekDaysMap.put(d, remainingWeekDays);

                remainingWeekDays--;
                if (remainingWeekDays < 0)
                    remainingWeekDays = 6;
            }

            // Read the product categories
            Map<String, ProductCategory> productCategoryMap = new HashMap<>();

            sheet = wb.getSheet(ExcelProcessor.PRODUCT_CATEGORY);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.PRODUCT_CATEGORY, sheet.getRow(0));

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String name = df.formatCellValue(row.getCell(colIdxMap.get("productCategory")));
                double fr = Double.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("fillRate"))));

                ProductCategory pc = new ProductCategory(name, fr);
                productCategoryMap.put(name, pc);
            }

            // Read the capacity types
            Map<String, CapacityType> capacityTypeMap = new HashMap<>();

            sheet = wb.getSheet(ExcelProcessor.CAPACITY_TYPE);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.CAPACITY_TYPE, sheet.getRow(0));

            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String name = df.formatCellValue(row.getCell(colIdxMap.get("name")));
                double rate = Double.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("defaultRate"))));

                CapacityType ct = new CapacityType(name, rate);
                capacityTypeMap.put(name, ct);
            }

            // Read the plants
            Map<String, Plant> plantMap = new HashMap<>();

            sheet = wb.getSheet(ExcelProcessor.PLANT);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.PLANT, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String name = df.formatCellValue(row.getCell(colIdxMap.get("plant")));
                int lod = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("lockedOutDays")))); // locked out days
                String type = df.formatCellValue(row.getCell(colIdxMap.get("type")));

                Plant plant = new Plant(name, lod, type);
                plantMap.put(name, plant);
            }

            // Read the machine sets
            Map<String, MachineSet> machineSetMap = new HashMap<>();

            sheet = wb.getSheet(ExcelProcessor.MACHINE_SET);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.MACHINE_SET, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String name = df.formatCellValue(row.getCell(colIdxMap.get("set")));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(colIdxMap.get("plant"))));
                CapacityType ct = capacityTypeMap.get(df.formatCellValue(row.getCell(colIdxMap.get("capacityType"))));
                double sf = ExcelProcessor.readDoubleCell(row.getCell(colIdxMap.get("smoothingFactor"))); // smoothing factor

                MachineSet machineSet = new MachineSet(name, plant, ct, sf);

                machineSetMap.put(name, machineSet);
                plant.putMachineSet(machineSet);
            }

            // Read the items
            Map<String, Item> itemMap = new HashMap<>();

            sheet = wb.getSheet(ExcelProcessor.ITEM);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.ITEM, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String id = df.formatCellValue(row.getCell(colIdxMap.get("item")));
                ItemType type = ItemType.get(df.formatCellValue(row.getCell(colIdxMap.get("itemType"))));
                double mcost = Double.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("materialCost")))); // material cost
                ProductCategory pc = productCategoryMap.get(df.formatCellValue(row.getCell(colIdxMap.get("productCategory")))); // product category
                double hcost = Double.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("holdingCost"))));

                Item item = new Item(id, type, mcost, pc, hcost);

                itemMap.put(item.getId(), item);
            }

            // Read the demands and merge into items
            sheet = wb.getSheet(ExcelProcessor.DEMAND);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.DEMAND, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("item"))));
                Date rawDate = Environment.parseDateStr(df.formatCellValue(row.getCell(colIdxMap.get("timePeriod"))));
                int date = Integer.valueOf(targetSdf.format(rawDate));
                long odem = Long.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("orderDemand")))); // order demand
                long fdem = Long.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("forecastDemand")))); // forecast demand

                TimePeriod timePeriod = timePeriodMap.get(date);
                int dueDate = timePeriod.dueDate();
                int dueDateIndex = TimePeriod.gap(startDate, dueDate);

                if (odem > 0) {
                    item.getOrderDemandMap().put(dueDateIndex, odem);
                }

                if (fdem > 0) {
                    item.getForecastDemandMap().put(dueDateIndex, fdem);
                }
            }

            // Read the transit information and merge into plants and item
//            Map<Pair<Plant, Plant>, Integer> transitCostMap = new HashMap<>();
//            Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap = new HashMap<>();

            List<Transit> transits = new ArrayList<>();
            sheet = wb.getSheet(ExcelProcessor.TRANSIT);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.TRANSIT, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("item"))));
                Plant fromPlant = plantMap.get(df.formatCellValue(row.getCell(colIdxMap.get("sourcePlant"))));
                Plant toPlant = plantMap.get(df.formatCellValue(row.getCell(colIdxMap.get("destinationPlant"))));
                int cost = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("cost"))));
                int leadTime = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("leadTime"))));

                Transit transit = new Transit(item, fromPlant, toPlant, cost, leadTime);
                transits.add(transit);

//                // transit maps
//                transitCostMap.put(new Pair(fromPlant, toPlant), cost);
//                transitLeadTimeMap.put(new Pair(fromPlant, toPlant), leadTime);

                // plant transit maps
                fromPlant.putTransitOutMap(toPlant, item);
                toPlant.putTransitInMap(fromPlant, item);

                // item transits
                item.addTransit(transit);
            }

            // Read the capacity and merge into machine set
            sheet = wb.getSheet(ExcelProcessor.CAPACITY);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.CAPACITY, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                MachineSet set = machineSetMap.get(df.formatCellValue(row.getCell(colIdxMap.get("set"))));
                Date rawDate = Environment.parseDateStr(df.formatCellValue(row.getCell(colIdxMap.get("timePeriod"))));
                int date = Integer.valueOf(targetSdf.format(rawDate));
                double capacity = Double.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("capacity"))));

                int dateIndex = TimePeriod.gap(startDate, date);

//                System.out.println(set.toString() + ": [" + date + ", " + dateIndex + "]");

                set.getCapacityMap().put(dateIndex, new Capacity(capacity));
            }

            // Read the initial inventories and merge into plants
            sheet = wb.getSheet(ExcelProcessor.INIT_INVENTORY);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.INIT_INVENTORY, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                long quantity = Long.valueOf(df.formatCellValue(row.getCell(1)));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(2)));

                item.getInitInventoryMap().put(plant, quantity);
            }

            // Read the production and merge into items
            sheet = wb.getSheet(ExcelProcessor.PRODUCTION);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.PRODUCTION, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("item"))));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(colIdxMap.get("plant"))));
                double cost = Double.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("productionCost"))));
                int leadTime = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("leadTime")))); // Integer.valueOf(df.formatCellValue(row.getCell(3)));
                int preWeekProd = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("previousWeekProduction"))));
                int wtdProd = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("weekToDateProduction")))); // week-to-date productions
                int lotSize = 1; //[data error, all zeros] Integer.valueOf(df.formatCellValue(row.getCell(6)));
                int minProd = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("minProduction"))));
                int maxProd = Integer.MAX_VALUE; // Integer.valueOf(df.formatCellValue(row.getCell(8)));
                int fds = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("maxProduction"))));

                Production production = new Production(item, plant, cost, leadTime,
                        preWeekProd, wtdProd, lotSize, minProd, maxProd, fds);

                item.putProduction(production);
            }

            // Read the boms and merge into productions
            sheet = wb.getSheet(ExcelProcessor.PLANT_BOM);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.PLANT_BOM, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("assembly"))));
                Item material = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("component"))));
                int quantity = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("quantity"))));
                SupplyType st = SupplyType.get(df.formatCellValue(row.getCell(colIdxMap.get("supplyType"))));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(colIdxMap.get("plant"))));

                BomComponent bomComponent = new BomComponent(material, quantity, st);
                item.getProduction(plant).addBom(bomComponent);
            }

            // Read the item capacity type and rate
            sheet = wb.getSheet(ExcelProcessor.RATE);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.RATE, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("item"))));
                CapacityType capacityType = capacityTypeMap.get(df.formatCellValue(row.getCell(colIdxMap.get("capacityType"))));
                double rate = Double.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("rate"))));

                item.getRateMap().put(capacityType, rate);
            }

            // Fill in the missing rates as the default values
            for (Item item : itemMap.values()) {
                for (CapacityType capacityType : capacityTypeMap.values()) {
                    if (item.getRateMap().containsKey(capacityType))
                        continue;

                    item.getRateMap().put(capacityType, capacityType.getDefaultRate());
                }
            }

            // Read the work in process and merge into plants
            sheet = wb.getSheet(ExcelProcessor.WORK_IN_PROCESS);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.WORK_IN_PROCESS, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("item"))));
                Date rawDate = Environment.parseDateStr(df.formatCellValue(row.getCell(colIdxMap.get("availDate"))));
                int date = Integer.valueOf(targetSdf.format(rawDate));
                long quantity = Long.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("quantity"))));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(colIdxMap.get("plant"))));

                int dateIndex = TimePeriod.gap(startDate, date);
                plant.putWorkInProcess(item, dateIndex, quantity);
            }

            // Read the item sets and merge into items
            sheet = wb.getSheet(ExcelProcessor.ITEM_SETS);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.ITEM_SETS, sheet.getRow(0));
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                MachineSet machineSet = machineSetMap.get(df.formatCellValue(row.getCell(colIdxMap.get("set"))));
                Item item = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("item"))));

                Plant plant = machineSet.getPlant();
                List<MachineSet> machineSets = item.getMachineMap().get(plant);

                if (machineSets == null)
                    machineSets = new ArrayList<>();

                machineSets.add(machineSet);
                item.getMachineMap().put(plant, machineSets);
            }

            // Read the frozen productions and merge into items
            sheet = wb.getSheet(ExcelProcessor.FROZEN_PROD);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.FROZEN_PROD, sheet.getRow(0));
            Set<String> unknownPlantSet = new HashSet<>();
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(colIdxMap.get("item"))));
                String plantStr = df.formatCellValue(row.getCell(colIdxMap.get("plant")));
                Plant plant = plantMap.get(plantStr);
                if (plant == null) {
                    String unknownPlant = plantStr;
                    if (!unknownPlantSet.contains(unknownPlant)) {
                        System.out.println("Unknown plant: " + unknownPlant);
                        unknownPlantSet.add(unknownPlant);
                    }
                }
                Date rawDate = Environment.parseDateStr(df.formatCellValue(row.getCell(colIdxMap.get("timePeriod"))));
                int date = Integer.valueOf(targetSdf.format(rawDate));
                TimePeriod timePeriod = timePeriodMap.get(date);
                long quantity = Long.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("quantity"))));

                Map<Plant, Map<Integer, Long>> frozenProdMap = item.getFrozenProductionMap();
                Map<Integer, Long> plantFrozenProdMap = frozenProdMap.get(plant);

                if (plantFrozenProdMap == null) {
                    plantFrozenProdMap = new HashMap<>();
                    frozenProdMap.put(plant, plantFrozenProdMap);
                }

                int dateIndex = TimePeriod.gap(startDate, timePeriod.getDate());
                plantFrozenProdMap.put(dateIndex, quantity);
            }

            // Read the raw material po and merge into plants
            sheet = wb.getSheet(ExcelProcessor.RAW_MATERIAL_PO);
            colIdxMap = ExcelProcessor.parseHeader(ExcelProcessor.RAW_MATERIAL_PO, sheet.getRow(0));

            Set<String> unknownItemSet = new HashSet<>();
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String itemStr = df.formatCellValue(row.getCell(colIdxMap.get("item")));
                Item item = itemMap.get(itemStr);
                if (item == null) {
                    String unknownItem = itemStr;
                    if (!unknownItemSet.contains(unknownItem)) {
                        System.out.println("Unknown item:" + unknownItem);
                        unknownItemSet.add(unknownItem);
                    }
                }
                long quantity = Integer.valueOf(df.formatCellValue(row.getCell(colIdxMap.get("quantity"))));
                Date rawDate = Environment.parseDateStr(df.formatCellValue(row.getCell(colIdxMap.get("supplyDate"))));
                int date = Integer.valueOf(targetSdf.format(rawDate));

                Plant plant = plantMap.get(df.formatCellValue(row.getCell(colIdxMap.get("destinationPlant"))));

                int dateIndex = TimePeriod.gap(startDate, date);

                plant.putRawMaterialPo(item, quantity, dateIndex);
            }

            /**
             * fix bugs in the data file:
             * remove a production if its item does not have machine set in the plant.
             */
            for (Item item : itemMap.values()) {
                Map<Plant, Production> productionMap = item.getProductionMap();

                List<Plant> toRemove = new ArrayList<>();
                for (Production production : productionMap.values()) {
                    Plant plant = production.getPlant();

                    if (item.getMachineMap().isEmpty()) {
                        toRemove.add(plant);
                    } else {
                        List<MachineSet> machineSets = item.getMachineMap().get(plant);
                        if (machineSets == null || machineSets.isEmpty())
                            toRemove.add(plant);
                    }
                }

                for (Plant p : toRemove)
                    productionMap.remove(p);
            }

            // for each production, calculate the rate map
            for (Item item : itemMap.values()) {
                for (Production production : item.getProductionMap().values()) {
                    production.calcRateMap();
                }
            }

            environment =
                    new Environment(productCategoryMap, capacityTypeMap, itemMap, plantMap, machineSetMap, transits);
            environment.setStartDate(startDate);
            environment.setEndDate(endDate);
            environment.setPeriod(period);
            environment.setRemainingWeekDaysMap(remainingWeekDaysMap);

            // calculate the min production lead time for each item
            for (Item item : itemMap.values()) {
                item.calcMinProdLeadTime();
            }

            // calculate the plants that can hold each item
            for (Item item : itemMap.values()) {
                item.calcPlants();
            }
            // the plants purchasing the materials can also hold the items
            for (Plant plant : plantMap.values()) {
                for (Map<Item, Long> dailyMap : plant.getRawMaterialPoMap().values()) {
                    for (Item item : dailyMap.keySet()) {
                        item.getPlants().add(plant);
                    }
                }
            }

            // calculate the dependent items
            environment.calcDependentItems();

            // Closing the workbook
            wb.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        return environment;
    }

    /**
     * Calculate the dependent items for each item.
     */
    public void calcDependentItems() {
        for (Item item : itemMap.values()) {
            item.calcBomItems();
            item.calcSharedMachineSets();
        }

        // group the dependent items into groups
        DisjointSets<Item> groups = new DisjointSets<>(itemMap.values()); // the groups are disjoint sets

        for (Item item : itemMap.values()) {
            // merge all the bom items into the same group
            for (Item bomItem : item.getBomItems()) {
                groups.union(item, bomItem);
            }
        }

        boolean union = true;
        while (union) {
            union = false;

            for (Item item1 : itemMap.values()) {
                for (Item item2 : itemMap.values()) {
                    if (item1.equals(item2))
                        continue;

                    if (groups.disjoint(item1, item2)) {
                        union = true;
                        groups.union(item1, item2);
                    }
                }
            }
        }

        System.out.println("debug getting dependent items in Environment.java");
    }

    /**
     * Whether two items share the machine set or not?
     *
     * @param item1 the item1.
     * @param item2 the item2.
     * @return true if they share the machine set, and false otherwise.
     */
    public boolean shareMachineSet(Item item1, Item item2) {
        Set<MachineSet> set1 = item1.getSharedMachineSets();
        Set<MachineSet> set2 = item2.getSharedMachineSets();

        for (MachineSet m1 : set1) {
            if (set2.contains(m1))
                return true;
        }

        for (MachineSet m2 : set2) {
            if (set1.contains(m2))
                return true;
        }

        return false;
    }

    /**
     * Parse date string to Date object from different time format
     *
     * @param dateStr date string
     * @return date object
     */
    private static Date parseDateStr(String dateStr) throws ParseException {

        // Initialize some candidate date formats
        List<SimpleDateFormat> candFormatList = Arrays.asList(new SimpleDateFormat("yyyyMMdd"),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));

        for (SimpleDateFormat sdf : candFormatList) {
            try {
                Date parsedDate = sdf.parse(dateStr);

                // Check whether the parse is correct
                if (sdf.format(parsedDate).equals(dateStr)) {
                    return parsedDate;
                }
            } catch (ParseException e) {

            }
        }

        // Throw exception when none of the candidate format can parse `dateStr`
        throw new ParseException("Can't parse `dateStr` " + dateStr, 0);
    }

    public static void main(String[] args) {
        File file = new File("data/e_vuw_test_multi_plant_01.xlsx");

//        Environment environment = Environment.readFromFile(file);

        System.out.println("finished");
    }
}
