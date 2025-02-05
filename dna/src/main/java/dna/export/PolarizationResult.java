package dna.export;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import dna.Dna;
import logger.*;

/**
 * Stores the results of a single run of the genetic algorithm, i.e., for a
 * single time step of the time window algorithm or the whole network if no
 * time window was set.
 */
public final class PolarizationResult {

    private final double[] maxQ;
    private final double[] avgQ;
    private final double[] sdQ;
    private final double finalMaxQ;
    private final int[] memberships;
    private final String[] names;
    private final boolean earlyConvergence;
    private final LocalDateTime start;
    private final LocalDateTime stop;
    private final LocalDateTime middle;

    /**
     * Creates a PolarizationResult for a single time step.
     *
     * @param maxQ             The maximum quality score for each iteration of the genetic algorithm.
     * @param avgQ             The mean quality score for each iteration of the genetic algorithm.
     * @param sdQ              The standard deviation of the quality scores for each iteration of the genetic algorithm.
     * @param finalMaxQ        The maximum quality score of the final iteration of the genetic algorithm.
     * @param memberships      A membership array containing the cluster levels for each node, starting with 0 and going up to K - 1.
     * @param names            The node labels of the network.
     * @param earlyConvergence A boolean indicating whether the genetic algorithm converged before the last iteration.
     * @param start            The start date and time of the time window network. Cannot be null. If no time window was set, this is the date of the network.
     * @param stop             The end date and time of the time window network. Cannot be null. If no time window was set, this is the date of the network.
     * @param middle           The mid-point date of the time window network. Cannot be null. If no time window was set, this is the date of the network.
     */
    public PolarizationResult(double[] maxQ, double[] avgQ, double[] sdQ, double finalMaxQ,
                              int[] memberships, String[] names, boolean earlyConvergence,
                              LocalDateTime start, LocalDateTime stop, LocalDateTime middle) {

        // Validate input
        if (maxQ == null || avgQ == null || sdQ == null || memberships == null || names == null) {
            LogEvent l = new LogEvent(Logger.ERROR,
                "Input arrays cannot be null.",
                "While creating a PolarizationResult object, null objects were encountered.");
            Dna.logger.log(l);
        }
        if (start == null || stop == null || middle == null) {
            LogEvent l = new LogEvent(Logger.ERROR,
                "Dates cannot be null.",
                "While creating a PolarizationResult object, the start, stop, or middle date was null.");
            Dna.logger.log(l);
        }
        if (maxQ.length != avgQ.length || maxQ.length != sdQ.length) {
            LogEvent l = new LogEvent(Logger.ERROR,
                "maxQ, avgQ, and sdQ must have the same length.",
                "While creating a PolarizationResult object, the maxQ, avgQ, and sdQ arrays had different lengths.");
            Dna.logger.log(l);
        }
        if (memberships.length != names.length) {
            LogEvent l = new LogEvent(Logger.ERROR,
                "Memberships and names must have the same length.",
                "While creating a PolarizationResult object, the memberships and names arrays had different lengths.");
            Dna.logger.log(l);
        }

        // Assign fields
        this.maxQ = Arrays.copyOf(maxQ, maxQ.length);
        this.avgQ = Arrays.copyOf(avgQ, avgQ.length);
        this.sdQ = Arrays.copyOf(sdQ, sdQ.length);
        this.finalMaxQ = finalMaxQ;
        this.memberships = Arrays.copyOf(memberships, memberships.length);
        this.names = Arrays.copyOf(names, names.length);
        this.earlyConvergence = earlyConvergence;
        this.start = start;
        this.stop = stop;
        this.middle = middle;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getStop() {
        return stop;
    }

    public LocalDateTime getMiddle() {
        return middle;
    }

    public int[] getMemberships() {
        return Arrays.copyOf(memberships, memberships.length);
    }

    public String[] getNames() {
        return Arrays.copyOf(names, names.length);
    }

    public double[] getMaxQ() {
        return Arrays.copyOf(maxQ, maxQ.length);
    }

    public double[] getAvgQ() {
        return Arrays.copyOf(avgQ, avgQ.length);
    }

    public double[] getSdQ() {
        return Arrays.copyOf(sdQ, sdQ.length);
    }

    public double getFinalMaxQ() {
        return finalMaxQ;
    }

    public boolean isEarlyConvergence() {
        return earlyConvergence;
    }

    @Override
    public String toString() {
        return "PolarizationResult{" +
                "maxQ=" + Arrays.toString(maxQ) +
                ", avgQ=" + Arrays.toString(avgQ) +
                ", sdQ=" + Arrays.toString(sdQ) +
                ", finalMaxQ=" + finalMaxQ +
                ", memberships=" + Arrays.toString(memberships) +
                ", names=" + Arrays.toString(names) +
                ", earlyConvergence=" + earlyConvergence +
                ", start=" + start +
                ", stop=" + stop +
                ", middle=" + middle +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolarizationResult that = (PolarizationResult) o;
        return Double.compare(that.finalMaxQ, finalMaxQ) == 0 &&
                earlyConvergence == that.earlyConvergence &&
                Arrays.equals(maxQ, that.maxQ) &&
                Arrays.equals(avgQ, that.avgQ) &&
                Arrays.equals(sdQ, that.sdQ) &&
                Arrays.equals(memberships, that.memberships) &&
                Arrays.equals(names, that.names) &&
                Objects.equals(start, that.start) &&
                Objects.equals(stop, that.stop) &&
                Objects.equals(middle, that.middle);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(finalMaxQ, earlyConvergence, start, stop, middle);
        result = 31 * result + Arrays.hashCode(maxQ);
        result = 31 * result + Arrays.hashCode(avgQ);
        result = 31 * result + Arrays.hashCode(sdQ);
        result = 31 * result + Arrays.hashCode(memberships);
        result = 31 * result + Arrays.hashCode(names);
        return result;
    }
}