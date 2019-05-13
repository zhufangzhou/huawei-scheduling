package scheduling.core;

import io.ExcelProcessor;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import scheduling.core.input.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Environment(Map<String, ProductCategory> productCategoryMap, Map<String, Item> itemMap, Map<String, Plant> plantMap, Map<String, MachineSet> machineSetMap, List<Transit> transits) {
        this.productCategoryMap = productCategoryMap;
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
     * @param file the .xlsx file.
     * @return the environment.
     */
    public static Environment readFromFile(File file) {
        Environment environment = null;

        try {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet;
            XSSFRow row;

            DataFormatter df = new DataFormatter();

            // Read the time periods
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

            // get the start and end dates
            int startDate = Integer.MAX_VALUE;
            int endDate = Integer.MIN_VALUE;
            for (int date : timePeriodMap.keySet()) {
                if (date < startDate)
                    startDate = date;

                if (date > endDate)
                    endDate = date;
            }
            int period = TimePeriod.gap(startDate, endDate)+1;

            Map<Integer, Integer> remainingWeekDaysMap = new HashMap<>();
            int weekStarts = timePeriodMap.get(startDate).getWeek();
            int remainingWeekDays = 6-TimePeriod.gap(weekStarts, startDate);
            for (int d = 0; d < period; d++) {
                remainingWeekDaysMap.put(d, remainingWeekDays);

                remainingWeekDays --;
                if (remainingWeekDays < 0)
                    remainingWeekDays = 6;
            }

            // Read the product catogories
            Map<String, ProductCategory> productCategoryMap = new HashMap<>();

            sheet = wb.getSheetAt(ExcelProcessor.PRODUCT_CATEGORY_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String name = df.formatCellValue(row.getCell(0));
                double fr = Double.valueOf(df.formatCellValue(row.getCell(1)));

                ProductCategory pc = new ProductCategory(name, fr);
                productCategoryMap.put(name, pc);
            }

            // Read the plants
            Map<String, Plant> plantMap = new HashMap<>();

            sheet = wb.getSheetAt(ExcelProcessor.PLANT_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String name = df.formatCellValue(row.getCell(0));
                int lod = Integer.valueOf(df.formatCellValue(row.getCell(1))); // locked out days
                String type = df.formatCellValue(row.getCell(1));

                Plant plant = new Plant(name, lod, type);
                plantMap.put(name, plant);
            }

            // Read the machine sets
            Map<String, MachineSet> machineSetMap = new HashMap<>();

            sheet = wb.getSheetAt(ExcelProcessor.MACHINE_SET_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String name = df.formatCellValue(row.getCell(0));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(1)));
                CapacityType ct = CapacityType.get(df.formatCellValue(row.getCell(2)));
                double sf = ExcelProcessor.readDoubleCell(row.getCell(3)); // smoothing factor

                MachineSet machineSet = new MachineSet(name, plant, ct, sf);

                machineSetMap.put(name, machineSet);
                plant.putMachineSet(machineSet);
            }

            // Read the items
            Map<String, Item> itemMap = new HashMap<>();

            sheet = wb.getSheetAt(ExcelProcessor.ITEM_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                String id = df.formatCellValue(row.getCell(0));
                ItemType type = ItemType.get(df.formatCellValue(row.getCell(1)));
                double mcost = Double.valueOf(df.formatCellValue(row.getCell(2))); // material cost
                ProductCategory pc = productCategoryMap.get(df.formatCellValue(row.getCell(5))); // product category
                double hcost = Double.valueOf(df.formatCellValue(row.getCell(6)));

                Item item = new Item(id, type, mcost, pc, hcost);

                itemMap.put(item.getId(), item);
            }

            // Read the demands and merge into items
            sheet = wb.getSheetAt(ExcelProcessor.DEMAND_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                int date = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                long odem = Long.valueOf(df.formatCellValue(row.getCell(2))); // order demand
                long fdem = Long.valueOf(df.formatCellValue(row.getCell(3))); // forecast demand

                TimePeriod timePeriod = timePeriodMap.get(date);
                int dueDate = timePeriod.dueDate();
                int dueDateIndex = TimePeriod.gap(startDate, dueDate);

                if (odem > 0)
                    item.getOrderDemandMap().put(dueDateIndex, odem);

                if (fdem > 0)
                    item.getForecastDemandMap().put(dueDateIndex, fdem);
            }

            // Read the transit information and merge into plants and item
//            Map<Pair<Plant, Plant>, Integer> transitCostMap = new HashMap<>();
//            Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap = new HashMap<>();

            List<Transit> transits = new ArrayList<>();
            sheet = wb.getSheetAt(ExcelProcessor.TRANSIT_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                Plant fromPlant = plantMap.get(df.formatCellValue(row.getCell(1)));
                Plant toPlant = plantMap.get(df.formatCellValue(row.getCell(2)));
                int cost = Integer.valueOf(df.formatCellValue(row.getCell(3)));
                int leadTime = Integer.valueOf(df.formatCellValue(row.getCell(4)));

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
            sheet = wb.getSheetAt(ExcelProcessor.CAPACITY_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                MachineSet set = machineSetMap.get(df.formatCellValue(row.getCell(0)));
                int date = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                double capacity = Double.valueOf(df.formatCellValue(row.getCell(4)));

                int dateIndex = TimePeriod.gap(startDate, date);
                set.putCapacity(dateIndex, capacity);
            }

            // Read the initial inventories and merge into plants
            sheet = wb.getSheetAt(ExcelProcessor.INIT_INVENTORY_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                long quantity = Long.valueOf(df.formatCellValue(row.getCell(1)));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(2)));

                item.getInitInventoryMap().put(plant, quantity);
            }

            // Read the production and merge into items
            sheet = wb.getSheetAt(ExcelProcessor.PRODUCTION_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(1)));
                double cost = Double.valueOf(df.formatCellValue(row.getCell(2)));
                int leadTime = Integer.valueOf(df.formatCellValue(row.getCell(10))); // Integer.valueOf(df.formatCellValue(row.getCell(3)));
                int preWeekProd = Integer.valueOf(df.formatCellValue(row.getCell(4)));
                int wtdProd = Integer.valueOf(df.formatCellValue(row.getCell(5))); // week-to-date productions
                int lotSize = 1; //[data error, all zeros] Integer.valueOf(df.formatCellValue(row.getCell(6)));
                int minProd = Integer.valueOf(df.formatCellValue(row.getCell(7)));
                int maxProd = Integer.valueOf(df.formatCellValue(row.getCell(8)));
                int fds = Integer.valueOf(df.formatCellValue(row.getCell(9)));

                Production production = new Production(item, plant, cost, leadTime,
                        preWeekProd, wtdProd, lotSize, minProd, maxProd, fds);

                item.putProduction(production);
            }

            // Read the bom and merge into productions
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

            // Read the item capacity type and rate
            sheet = wb.getSheetAt(ExcelProcessor.RATE_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                CapacityType capacityType = CapacityType.get(df.formatCellValue(row.getCell(1)));
                double rate = Double.valueOf(df.formatCellValue(row.getCell(2)));

                item.setCapacityType(capacityType);
                item.setRate(rate);
            }

            // Read the work in process and merge into plants
            sheet = wb.getSheetAt(ExcelProcessor.WORK_IN_PROCESS_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                int date = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                long quantity = Long.valueOf(df.formatCellValue(row.getCell(2)));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(3)));

                int dateIndex = TimePeriod.gap(startDate, date);
                plant.putWorkInProcess(item, dateIndex, quantity);
            }

            // Read the item sets and merge into items
            sheet = wb.getSheetAt(ExcelProcessor.ITEM_SETS_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                MachineSet machineSet = machineSetMap.get(df.formatCellValue(row.getCell(0)));
                Item item = itemMap.get(df.formatCellValue(row.getCell(1)));

                item.getMachineMap().put(machineSet.getPlant(), machineSet);
            }

            // Read the frozen productions and merge into items
            sheet = wb.getSheetAt(ExcelProcessor.FROZEN_PROD_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(1)));
                if (plant == null) {
                    System.out.println("Unknown plant: " + df.formatCellValue(row.getCell(1)));
                }
                int date = Integer.valueOf(df.formatCellValue(row.getCell(2)));
                TimePeriod timePeriod = timePeriodMap.get(date);
                long quantity = Long.valueOf(df.formatCellValue(row.getCell(3)));

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
            sheet = wb.getSheetAt(ExcelProcessor.RAW_MATERIAL_PO_IDX);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);

                Item item = itemMap.get(df.formatCellValue(row.getCell(0)));
                if (item == null) {
                    System.out.println("Unknown item:" + df.formatCellValue(row.getCell(0)));
                }
                long quantity = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                int date = Integer.valueOf(df.formatCellValue(row.getCell(2)));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(3)));

                int dateIndex = TimePeriod.gap(startDate, date);

                plant.putRawMaterialPo(item, quantity, dateIndex);
            }

            environment =
                    new Environment(productCategoryMap, itemMap, plantMap, machineSetMap, transits);
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

            // Closing the workbook
            wb.close();
        } catch(Exception ioe) {
            ioe.printStackTrace();
        }

        return environment;
    }

    public static void main(String[] args) {
        File file = new File("data/e_vuw_test_multi_plant_01.xlsx");

        Environment environment = Environment.readFromFile(file);

        System.out.println("finished");
    }
}
