package io;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ExcelProcessor {

    // sheetName in getSheet method is case insensitive
    public static final String CAPACITY = "Capacity";
    public static final String CAPACITY_TYPE = "Capacity Types";
    public static final String PLANT_BOM = "Plant Bom";
    public static final String DEMAND = "Demand";
    public static final String FROZEN_PROD = "Frozen Production";
    public static final String PLANT = "Plants";
    public static final String ITEM_SETS = "Item Sets";
    public static final String PRODUCT_CATEGORY = "Product Category";
    public static final String PRODUCTION = "Production";
    public static final String ITEM = "Items";
    public static final String RATE = "Rate";
    public static final String MACHINE_SET = "Sets";
    public static final String TIME_PERIOD = "Time Periods";
    public static final String TRANSIT = "Transit";
    public static final String WORK_IN_PROCESS = "Items WIP";
    public static final String RAW_MATERIAL_PO = "Raw Material Po";
    public static final String INIT_INVENTORY = "Item Initial Inventory";

    // Map column name in different sheets predefined name
    public static Map<String, Map<String, String>> totColumnNameMap = null;

    /**
     * Initialize the column name mapping
     *
     * @param columnNameReader column name configuration file Reader
     * @throws IOException
     */
    public static void initColNames(FileReader columnNameReader) throws IOException {
        // Read json file to construct JSONObject
        BufferedReader br = new BufferedReader(columnNameReader);
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        JSONObject obj = new JSONObject(sb.toString());

        ExcelProcessor.totColumnNameMap = new HashMap<>();
        // iterate over different sheets
        for (String sheetName : obj.keySet()) {
            JSONObject sheetObj = obj.getJSONObject(sheetName);
            Map<String, String> columnNameMap = new HashMap<>();
            // iterate over sheet columns
            for (String rawColumnName : sheetObj.keySet()) {
                columnNameMap.put(rawColumnName, sheetObj.getString(rawColumnName));
            }
            ExcelProcessor.totColumnNameMap.put(sheetName, columnNameMap);
        }

    }

    public static Map parseHeader(String sheetName, Row row) {
        Map<String, Integer> colIdxMap = new HashMap<>();

        if (ExcelProcessor.totColumnNameMap == null) {
            throw new RuntimeException("You should call initColNames first.");
        }

        int minColIdx = row.getFirstCellNum();
        int maxColIdx = row.getLastCellNum();

        DataFormatter df = new DataFormatter();

        Map<String, String> columnNameMap = ExcelProcessor.totColumnNameMap.get(sheetName);

        for (int i = minColIdx; i <= maxColIdx; i++) {
            String colName = df.formatCellValue(row.getCell(i));

            colIdxMap.put(columnNameMap.get(colName), i);
        }

        return colIdxMap;
    }


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

//    public static void main(String[] args) throws IOException {
//        RandomDataGenerator rdg = new RandomDataGenerator();
//        rdg.reSeed(0);
//        guessTomorrowInput("data/huawei-input.xlsx", rdg);
//    }
}
