package export;

import java.util.ArrayList;

public class NestedBackboneResult {

    /**
     * The algorithm used to compute the results. Can be {@code "nested"} (for a nested, agglomerative method),
     * {@code "all"} (for simulated annealing with all backbone sizes and no penalty), {@code "size"} (for simulated
     * annealing for a specific backbone size with no penalty), or {@code "penalty"} (for a penalized simulated
     * annealing approach).
     */
    private String method;

    /**
     * Iteration from 1 to the final size of the redundant set.
     */
    private int[] iteration;

    /**
     * The entity for which the result holds.
     */
    private String[] entities;

    /**
     * Euclidean spectral distance between the full network and the backbone network at each backbone size, without
     * penalty factor for the number of entities in the set, after optimization.
     */
    private double[] backboneLoss;

    /**
     * Euclidean spectral distance between the full network and the redundant network at each backbone size, without
     * penalty factor for the number of entities in the set, after optimization.
     */
    private double[] redundantLoss;

    /**
     * Number of statements used to create the backbone networks.
     */
    private int[] numStatements;

    /**
     * Number of statements used to create the full network.
     */
    private int numStatementsFull;

    /**
     * Start date/time of the network in seconds since 1 January 1970.
     */
    private long start;

    /**
     * End date/time of the network in seconds since 1 January 1970.
     */
    private long stop;

    public NestedBackboneResult(String method, int[] iteration, String[] entities, double[] backboneLoss, double[] redundantLoss, int[] numStatements, int numStatementsFull, long start, long stop) {
        this.method = method;
        this.iteration = iteration;
        this.entities = entities;
        this.backboneLoss = backboneLoss;
        this.redundantLoss = redundantLoss;
        this.numStatementsFull = numStatementsFull;
        this.numStatements = numStatements;
        this.start = start;
        this.stop = stop;
    }

    public String getMethod() {
        return method;
    }

    public int[] getIteration() {
        return iteration;
    }

    public String[] getEntities() {
        return entities;
    }

    public double[] getBackboneLoss() {
        return backboneLoss;
    }

    public double[] getRedundantLoss() {
        return redundantLoss;
    }

    public int getNumStatementsFull() {
        return numStatementsFull;
    }

    public int[] getNumStatements() {
        return numStatements;
    }

    public long getStart() {
        return start;
    }

    public long getStop() {
        return stop;
    }
}
