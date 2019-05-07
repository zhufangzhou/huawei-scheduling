package scheduling.simulation;

import com.sun.tools.doclint.Env;
import scheduling.core.Environment;
import scheduling.core.Schedule;
import scheduling.core.input.Item;
import scheduling.core.input.Plant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The current state of the scheduler.
 * In the static scheduling environment, it is used to represent the static problem.
 * In the dynamic scheduling environment, it is a state during the simulation.
 */

public class State {
    private Environment env; // the environment

    private int startDate; // the starting date, used to calculate the day id
    private int dateIndex; // the current date index

    private Schedule plannedSchedule; // the planned schedule that is not executed yet.
    private Schedule executedSchedule; // the executed schedule so far (during the simulation)

    public State(Environment env, int startDate, Schedule plannedSchedule, Schedule executedSchedule) {
        this.env = env;
        this.startDate = startDate;
        this.dateIndex = 0;
        this.plannedSchedule = plannedSchedule;
        this.executedSchedule = executedSchedule;
    }

    /**
     * Read the state from a file and the start date.
     * @param file the file.
     * @param startDate the start date.
     * @return the state.
     */
    public static State readFromFile(File file, int startDate) {
        Environment env = Environment.readFromFile(file, startDate);

        return new State(env, startDate, new Schedule(), new Schedule());
    }

    public Environment getEnv() {
        return env;
    }

    public int getStartDate() {
        return startDate;
    }

    public int getDateIndex() {
        return dateIndex;
    }

    public Schedule getPlannedSchedule() {
        return plannedSchedule;
    }

    public Schedule getExecutedSchedule() {
        return executedSchedule;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }

    public void setDateIndex(int dateIndex) {
        this.dateIndex = dateIndex;
    }

    public void setPlannedSchedule(Schedule plannedSchedule) {
        this.plannedSchedule = plannedSchedule;
    }

    public void setExecutedSchedule(Schedule executedSchedule) {
        this.executedSchedule = executedSchedule;
    }
}
