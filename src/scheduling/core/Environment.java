package scheduling.core;

import io.ExcelProcessor;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import scheduling.core.input.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * An environment stores the global information in the scheduling process.
 * The global information does not change dynamically.
 * - all the product categories
 * - all the items and their basic properties during the process
 * - all the plants and their basic properties during the process
 * - all the machines sets and their basic properties during the process
 * - transits between the plants
 */

public class Environment {
    private Map<String, ProductCategory> productCategoryMap;
    private Map<String, Item> itemMap;
    private Map<String, Plant> plantMap;
    private Map<String, MachineSet> machineSetMap;
    private Map<Pair<Plant, Plant>, Integer> transitCostMap;
    private Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap;

    public Environment(Map<String, ProductCategory> productCategoryMap, Map<String, Plant> plantMap, Map<Pair<Plant, Plant>, Integer> transitCostMap, Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap, Map<String, MachineSet> machineSetMap, Map<String, Item> itemMap) {
        this.productCategoryMap = productCategoryMap;
        this.plantMap = plantMap;
        this.transitCostMap = transitCostMap;
        this.transitLeadTimeMap = transitLeadTimeMap;
        this.machineSetMap = machineSetMap;
        this.itemMap = itemMap;
    }

    public Map<String, ProductCategory> getProductCategoryMap() {
        return productCategoryMap;
    }

    public Map<String, Plant> getPlantMap() {
        return plantMap;
    }

    public Map<Pair<Plant, Plant>, Integer> getTransitCostMap() {
        return transitCostMap;
    }

    public Map<Pair<Plant, Plant>, Integer> getTransitLeadTimeMap() {
        return transitLeadTimeMap;
    }

    public Map<String, MachineSet> getMachineSetMap() {
        return machineSetMap;
    }

    public Map<String, Item> getItemMap() {
        return itemMap;
    }

    public void setProductCategoryMap(Map<String, ProductCategory> productCategoryMap) {
        this.productCategoryMap = productCategoryMap;
    }

    public void setPlantMap(Map<String, Plant> plantMap) {
        this.plantMap = plantMap;
    }

    public void setTransitCostMap(Map<Pair<Plant, Plant>, Integer> transitCostMap) {
        this.transitCostMap = transitCostMap;
    }

    public void setTransitLeadTimeMap(Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap) {
        this.transitLeadTimeMap = transitLeadTimeMap;
    }

    public void setMachineSetMap(Map<String, MachineSet> machineSetMap) {
        this.machineSetMap = machineSetMap;
    }

    public void setItemMap(Map<String, Item> itemMap) {
        this.itemMap = itemMap;
    }

    /**
     * Read the environment from a .xlsx file.
     * @param file the .xlsx file.
     * @param startDate the start date.
     * @return the environment.
     */
    public static Environment readFromFile(File file, int startDate) {
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
                int lod = Integer.valueOf(df.formatCellValue(row.getCell(1)));
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
                double sf = ExcelProcessor.readDoubleCell(row.getCell(3));

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
                double mcost = Double.valueOf(df.formatCellValue(row.getCell(2)));
                ProductCategory pc = productCategoryMap.get(df.formatCellValue(row.getCell(5)));
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
                long odem = Long.valueOf(df.formatCellValue(row.getCell(2)));
                long fdem = Long.valueOf(df.formatCellValue(row.getCell(3)));

                TimePeriod timePeriod = timePeriodMap.get(date);
                int dueDate = timePeriod.dueDate();
                int dueDateIndex = TimePeriod.gap(startDate, dueDate);

                if (odem > 0)
                    item.getOrderDemandMap().put(dueDateIndex, odem);

                if (fdem > 0)
                    item.getForecastDemandMap().put(dueDateIndex, fdem);
            }

            // Read the transit information and merge into plants and item
            Map<Pair<Plant, Plant>, Integer> transitCostMap = new HashMap<>();
            Map<Pair<Plant, Plant>, Integer> transitLeadTimeMap = new HashMap<>();

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
                long quantity = Integer.valueOf(df.formatCellValue(row.getCell(1)));
                int date = Integer.valueOf(df.formatCellValue(row.getCell(2)));
                Plant plant = plantMap.get(df.formatCellValue(row.getCell(3)));

                int dateIndex = TimePeriod.gap(startDate, date);

                plant.putRawMaterialPo(item, quantity, dateIndex);
            }

            environment =
                    new Environment(productCategoryMap, plantMap, transitCostMap, transitLeadTimeMap, machineSetMap, itemMap);

            // Closing the workbook
            wb.close();
        } catch(Exception ioe) {
            ioe.printStackTrace();
        }

        return environment;
    }

    public static void main(String[] args) {
        File file = new File("data/e_vuw_test_multi_plant_01.xlsx");
        Environment environment = Environment.readFromFile(file, 20161203);

        System.out.println("finished");
    }
}
