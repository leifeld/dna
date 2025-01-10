package dna.export;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * The PolarisationResultTimeSeries class represents a time series of PolarisationResult objects.
 * It provides methods to retrieve various data from the time series, such as final maximum Q values,
 * early convergence flags, date and time arrays, maximum Q values, average Q values, standard deviation
 * of Q values, memberships, and names.
 *
 * <p>This class is designed to facilitate the analysis and export of polarisation results over time.</p>
 *
 * <p>Methods:</p>
 * <ul>
 *   <li>{@link #getResults()}: Returns the list of PolarisationResult objects.</li>
 *   <li>{@link #getFinalMaxQs()}: Returns an array of final maximum Q values from the time series.</li>
 *   <li>{@link #getEarlyConvergence()}: Returns an array of boolean values indicating early convergence for each result.</li>
 *   <li>{@link #getDateTimeArray()}: Returns a 2D array of date and time values for start, middle, and stop times of each result.</li>
 *   <li>{@link #getMaxQs()}: Returns a 2D array of maximum Q values for each result.</li>
 *   <li>{@link #getAvgQs()}: Returns a 2D array of average Q values for each result.</li>
 *   <li>{@link #getSdQs()}: Returns a 2D array of standard deviation of Q values for each result.</li>
 *   <li>{@link #getMemberships()}: Returns a 2D array of memberships for each result.</li>
 *   <li>{@link #getNames()}: Returns a 2D array of names for each result.</li>
 * </ul>
 *
 * <p>Constructor:</p>
 * <ul>
 *   <li>{@link #PolarisationResultTimeSeries(ArrayList<PolarisationResult>)}: Constructs a PolarisationResultTimeSeries with the specified list of PolarisationResult objects.</li>
 * </ul>
 */
public class PolarisationResultTimeSeries {
    final ArrayList<PolarisationResult> results;

    /**
     * Constructs a PolarisationResultTimeSeries with the specified list of PolarisationResult objects.
     *
     * @param results an ArrayList of PolarisationResult objects representing the time series data.
     */
    public PolarisationResultTimeSeries(ArrayList<PolarisationResult> results) {
        this.results = results;
    }

    /**
     * Retrieves the list of PolarisationResult objects.
     *
     * @return an ArrayList containing PolarisationResult objects.
     */
    public ArrayList<PolarisationResult> getResults() {
        return results;
    }

    /**
     * Retrieves an array of final maximum Q values from the results.
     *
     * @return a double array containing the final maximum Q values from each PolarisationResult in the results list.
     */
    public double[] getFinalMaxQs() {
        return this.results.stream().mapToDouble(PolarisationResult::getFinalMaxQ).toArray();
    }

    /**
     * Retrieves an array indicating early convergence status for each result.
     *
     * @return a boolean array where each element represents whether the corresponding result
     *         in the results list has early convergence.
     */
    public boolean[] getEarlyConvergence() {
        boolean[] earlyConvergence = new boolean[this.results.size()];
        for (int i = 0; i < this.results.size(); i++) {
            earlyConvergence[i] = this.results.get(i).isEarlyConvergence();
        }
        return earlyConvergence;
    }

    /**
     * Generates a 2D array representing the date and time components of the start, middle, 
     * and stop times for each result in the results list.
     * 
     * The returned array has a size of [number of results][18], where each row corresponds 
     * to a result and contains the following date and time components:
     * - start year
     * - start month
     * - start day of month
     * - start hour
     * - start minute
     * - start second
     * - middle year
     * - middle month
     * - middle day of month
     * - middle hour
     * - middle minute
     * - middle second
     * - stop year
     * - stop month
     * - stop day of month
     * - stop hour
     * - stop minute
     * - stop second
     * 
     * @return a 2D array of integers representing the date and time components of the results.
     */
    public int[][] getDateTimeArray() {
        int[][] dateTimeArray = new int[this.results.size()][18];
        for (int i = 0; i < this.results.size(); i++) {
            LocalDateTime start = this.results.get(i).getStart();
            LocalDateTime middle = this.results.get(i).getMiddle();
            LocalDateTime stop = this.results.get(i).getStop();
            dateTimeArray[i][0] = start.getYear();
            dateTimeArray[i][1] = start.getMonthValue();
            dateTimeArray[i][2] = start.getDayOfMonth();
            dateTimeArray[i][3] = start.getHour();
            dateTimeArray[i][4] = start.getMinute();
            dateTimeArray[i][5] = start.getSecond();
            dateTimeArray[i][6] = middle.getYear();
            dateTimeArray[i][7] = middle.getMonthValue();
            dateTimeArray[i][8] = middle.getDayOfMonth();
            dateTimeArray[i][9] = middle.getHour();
            dateTimeArray[i][10] = middle.getMinute();
            dateTimeArray[i][11] = middle.getSecond();
            dateTimeArray[i][12] = stop.getYear();
            dateTimeArray[i][13] = stop.getMonthValue();
            dateTimeArray[i][14] = stop.getDayOfMonth();
            dateTimeArray[i][15] = stop.getHour();
            dateTimeArray[i][16] = stop.getMinute();
            dateTimeArray[i][17] = stop.getSecond();
        }
        return dateTimeArray;
    }

    /**
     * Retrieves the maximum Q values from the results.
     *
     * @return a 2D array of double values where each sub-array contains the maximum Q values
     *         for a corresponding result in the results list. Each time step can have different lengths.
     */
    public double[][] getMaxQs() {
        double[][] qs = new double[this.results.size()][];
        for (int i = 0; i < this.results.size(); i++) {
            qs[i] = this.results.get(i).getMaxQ();
        }
        return qs;
    }

    /**
     * Retrieves the average Q values from the results.
     *
     * @return a 2D array of double values where each sub-array represents the average Q values
     *         for a specific result in the results list. Each time step can have different lengths.
     */
    public double[][] getAvgQs() {
        double[][] qs = new double[this.results.size()][];
        for (int i = 0; i < this.results.size(); i++) {
            qs[i] = this.results.get(i).getAvgQ();
        }
        return qs;
    }

    /**
     * Retrieves the standard deviation Q values for each result in the time series.
     *
     * @return A 2D array where each sub-array contains the standard deviation Q values
     *         for a specific result in the time series. Each time step can have different lengths.
     */
    public double[][] getSdQs() {
        double[][] qs = new double[this.results.size()][];
        for (int i = 0; i < this.results.size(); i++) {
            qs[i] = this.results.get(i).getSdQ();
        }
        return qs;
    }

    /**
     * Retrieves the final cluster memberships from the results.
     *
     * @return a 2D array of integers representing the memberships for each result. Each time step can have different lengths.
     */
    public int[][] getMemberships() {
        int[][] memberships = new int[this.results.size()][];
        for (int i = 0; i < this.results.size(); i++) {
            memberships[i] = this.results.get(i).getMemberships();
        }
        return memberships;
    }

    /**
     * Retrieves a 2D array of names from the results.
     * Each element in the outer array corresponds to a result,
     * and each inner array contains the names associated with that result.
     *
     * @return a 2D array of names, where each inner array contains the names for a specific result. Each time step can have different lengths.
     */
    public String[][] getNames() {
        String[][] names = new String[this.results.size()][];
        for (int i = 0; i < this.results.size(); i++) {
            names[i] = this.results.get(i).getNames();
        }
        return names;
    }
}
