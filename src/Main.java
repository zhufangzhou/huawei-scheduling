import io.ExcelProcessor;
import scheduling.scheduler.GreedyStaticScheduler;
import scheduling.scheduler.Scheduler;
import scheduling.simulation.State;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        List<Double> fillRates = new ArrayList<>();
        List<Double> holdingCosts = new ArrayList<>();
        List<Double> prodCosts = new ArrayList<>();
        List<Double> transCosts = new ArrayList<>();
        List<Double> durations = new ArrayList<>();

        for (int fid = 1; fid < 32; fid++) {
            String fidStr = String.format("%02d", fid);

            File input = new File("data/e_vuw_test_multi_plant_" + fidStr + ".xlsx");
            FileReader columnNameReader = new FileReader("conf/column_name_v1.json");

            State staticProb = State.staticProbFromFile(input, columnNameReader);

            Scheduler scheduler = new GreedyStaticScheduler();
            long start = System.currentTimeMillis();
            scheduler.planSchedule(staticProb);
            long finish = System.currentTimeMillis();
            double duration = 0.001*(finish-start);

            staticProb.getPlannedSchedule().calcFillRate();

            fillRates.add(staticProb.getPlannedSchedule().getFillRate());
            holdingCosts.add(staticProb.getPlannedSchedule().getHoldingCost());
            prodCosts.add(staticProb.getPlannedSchedule().getProductionCost());
            transCosts.add(staticProb.getPlannedSchedule().getTransitCost());
            durations.add(duration);

            File scheduleFile = new File("schedule-" + fidStr + ".xlsx");
            ExcelProcessor.outputSchedule(staticProb.getPlannedSchedule(), scheduleFile, staticProb);

            System.out.println("fill rate = " + staticProb.getPlannedSchedule().getFillRate());
            System.out.println("holding cost = " + staticProb.getPlannedSchedule().getHoldingCost());
            System.out.println("production cost = " + staticProb.getPlannedSchedule().getProductionCost());
            System.out.println("transit cost = " + staticProb.getPlannedSchedule().getTransitCost());
            System.out.println("finished, duration = " + duration);
        }

        File output = new File("output.csv");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write("fid,fill-rate,holding-cost,prod-cost,trans-cost,seconds");
            writer.newLine();
            for (int fid = 1; fid < 32; fid++) {
                String fidStr = String.format("%02d", fid);

                writer.write(fidStr + "," + fillRates.get(fid-1) + "," +
                        holdingCosts.get(fid-1) + "," + prodCosts.get(fid-1) + "," +
                        transCosts.get(fid-1) + "," + durations.get(fid-1));
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
