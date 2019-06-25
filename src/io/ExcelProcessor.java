package io;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import scheduling.core.Schedule;
import scheduling.core.input.Production;
import scheduling.core.input.TimePeriod;
import scheduling.core.input.Transit;
import scheduling.core.output.ProductionInstruction;
import scheduling.core.output.SupplyInstruction;
import scheduling.core.output.TransitInstruction;
import scheduling.simulation.State;

import java.io.*;

public class ExcelProcessor {

    public static final int CAPACITY_IDX = 0;
    public static final int CAPACITY_TYPE_IDX = 1;
    public static final int PLANT_BOM_IDX = 2;
    public static final int DEMAND_IDX = 3;
    public static final int FROZEN_PROD_IDX = 4;
    public static final int PLANT_IDX = 5;
    public static final int ITEM_SETS_IDX = 6;
    public static final int PRODUCT_CATEGORY_IDX = 8;
    public static final int PRODUCTION_IDX = 9;
    public static final int ITEM_IDX = 10;
    public static final int RATE_IDX = 12;
    public static final int MACHINE_SET_IDX = 13;
    public static final int TIME_PERIOD_IDX = 15;
    public static final int TRANSIT_IDX = 17;
    public static final int WORK_IN_PROCESS_IDX = 18;
    public static final int RAW_MATERIAL_PO_IDX = 19;
    public static final int INIT_INVENTORY_IDX = 20;

//    public static void guessTomorrowInput(String todayFile,
//                                          RandomDataGenerator rdg)
//            throws IOException {
//        // copy the file to tomorrow
//        String tomorrowFile = todayFile.substring(0, todayFile.length()-5) + "1.xlsx";
//        copyFileUsingStream(new File(todayFile), new File(tomorrowFile));
//
//        try {
//            XSSFWorkbook wb = new XSSFWorkbook(tomorrowFile);
//            XSSFSheet sheet;
//            XSSFRow row;
//
//            sheet = wb.getSheetAt(PRODUCTION_PROP_IDX);
//            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                row = sheet.getRow(i);
//                DataFormatter df = new DataFormatter();
//                int num = readIntCell(row.getCell(2));
//                System.out.println(i + ", " + num);
//            }
//
//            // guess capacity
//            sheet = wb.getSheetAt(CAPACITY_IDX);
//            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                row = sheet.getRow(i);
//                guessCapacity(row, rdg);
//            }
//
//            // guess demand
//            sheet = wb.getSheetAt(DEMAND_IDX);
//            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
//                row = sheet.getRow(i);
//                guessDemand(row, rdg);
//            }
//
////            int cols = 0; // No of columns
////            int tmp;
////
////            // This trick ensures that we get the data properly even if it doesn't start from first few rows (i.e. check first 10 rows to see if there's data)
////            for(int i = 0; i < 10 || i < rows; i++) {
////                row = sheet.getRow(i);
////                if(row != null) {
////                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
////                    if(tmp > cols) cols = tmp;
////                }
////            }
////
////            double dummyData = 1;
////
////            for(int r = 1; r < rows; r++) {
////                row = sheet.getRow(r);
////                if(row != null) {
////                    int columnIndex = 2; // I'm putting dummy data into column index 2 as an example
////                    cell = row.getCell(columnIndex);
////                    if(cell != null) { // cell already has a value and we're overwriting it
////                        cell.setCellValue(cell.getNumericCellValue() + 1);
////                    } if(cell == null) { // cell is currently void and we're putting new values in
////                        cell = row.createCell(columnIndex); // ask the *row* to populate the cell
////                        cell.setCellValue(dummyData++);
////                    }
////                }
////            }
//
//            // overwrite the existing file:
//            FileOutputStream fileOut = new FileOutputStream("huawei-input.xlsx");
//            // or write to a new one, to play it safe:
////            FileOutputStream fileOut = new FileOutputStream("huawei-input2.xlsx");
//            wb.write(fileOut);
//            fileOut.close();
//
//            // Closing the workbook
//            wb.close();
//        } catch(Exception ioe) {
//            ioe.printStackTrace();
//        }
//    }

    private static void guessCapacity(XSSFRow capacityRow, RandomDataGenerator rdg) {
        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        XSSFCell capacityCell = capacityRow.getCell(4);
        int capacity = Integer.parseInt(dataFormatter.formatCellValue(capacityCell));

        double BREAKDOWN_PROB = 0.0001;

        int guessedCapacity = capacity - rdg.nextBinomial(capacity, BREAKDOWN_PROB);

        capacityCell.setCellValue(guessedCapacity);

        System.out.println(capacity + ", " + guessedCapacity);
    }

    private static void guessDemand(XSSFRow demandRow, RandomDataGenerator rdg) {
        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        XSSFCell orderDemandCell = demandRow.getCell(2);
        int orderDemand = Integer.parseInt(dataFormatter.formatCellValue(orderDemandCell));
        XSSFCell forcastDemandcell = demandRow.getCell(3);
        int forcastDemand = Integer.parseInt(dataFormatter.formatCellValue(forcastDemandcell));

        double arriveRate = rdg.nextUniform(0, 1);
        double guessedOrderDemand = orderDemand + Math.round(arriveRate * forcastDemand);

        orderDemandCell.setCellValue(guessedOrderDemand);

        System.out.println(orderDemand + ", " + forcastDemand + ", " + guessedOrderDemand);
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static int readIntCell(Cell cell) {
        DataFormatter df = new DataFormatter();

        String content = df.formatCellValue(cell);

        if (content.equals(""))
            return 0;

        return Integer.valueOf(content);
    }

    public static double readDoubleCell(Cell cell) {
        DataFormatter df = new DataFormatter();

        String content = df.formatCellValue(cell);

        if (content.equals(""))
            return 0;

        if (content.equals("inf"))
            return Double.POSITIVE_INFINITY;

        return Double.valueOf(content);
    }

    public static void outputSchedule(Schedule schedule, File file, State state) {
        int startDate = state.getEnv().getStartDate();

        Workbook workbook = new XSSFWorkbook();

        // create the production sheet
        String[] prodCols = {"item_code", "qty", "period", "plant"};
        Sheet sheet = workbook.createSheet("prod");
        Row headerRow = sheet.createRow(0);
        for(int i = 0; i < prodCols.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(prodCols[i]);
        }

        int rowNum = 1;
        for (int dateId : schedule.getProductionSchedule().keySet()) {
            for (Production production : schedule.getProductionSchedule().get(dateId).keySet()) {
                ProductionInstruction instruction = schedule.getProductionSchedule().get(dateId).get(production);

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(production.getItem().toString());
                row.createCell(1).setCellValue(instruction.getQuantity());
                row.createCell(2).setCellValue(TimePeriod.dateOfId(startDate, dateId));
                row.createCell(3).setCellValue(production.getPlant().toString());
            }
        }

        // create the transit sheet
        String[] transitCols = {"item_code", "qty", "src_plant", "dest_plant", "period"};
        sheet = workbook.createSheet("transit");
        headerRow = sheet.createRow(0);
        for(int i = 0; i < transitCols.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(transitCols[i]);
        }

        rowNum = 1;
        for (int dateId : schedule.getTransitSchedule().keySet()) {
            for (Transit transit : schedule.getTransitSchedule().get(dateId).keySet()) {
                TransitInstruction instruction = schedule.getTransitSchedule().get(dateId).get(transit);

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transit.getItem().toString());
                row.createCell(1).setCellValue(instruction.getQuantity());
                row.createCell(2).setCellValue(transit.getFromPlant().toString());
                row.createCell(3).setCellValue(transit.getToPlant().toString());
                row.createCell(4).setCellValue(TimePeriod.dateOfId(startDate, dateId));
            }
        }

        // create the supply sheet
        String[] supplyCols = {"item_code", "period", "qty", "plant"};
        sheet = workbook.createSheet("supply");
        headerRow = sheet.createRow(0);
        for(int i = 0; i < transitCols.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(transitCols[i]);
        }

        rowNum = 1;
        for (int dateId : schedule.getSupplySchedule().keySet()) {
            for (SupplyInstruction instruction : schedule.getSupplySchedule().get(dateId).values()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(instruction.getItem().toString());
                row.createCell(1).setCellValue(TimePeriod.dateOfId(startDate, dateId));
                row.createCell(2).setCellValue(instruction.getQuantity());
                row.createCell(3).setCellValue(instruction.getSupply().getSecond().toString());
            }
        }

        try {
            // write the workbook to file
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) throws IOException {
//        RandomDataGenerator rdg = new RandomDataGenerator();
//        rdg.reSeed(0);
//        guessTomorrowInput("data/huawei-input.xlsx", rdg);
//    }
}
