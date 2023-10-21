package export;

import java.io.Serializable;

/**
 * Class representing backbone results.
 */
public class SimulatedAnnealingBackboneResult implements Serializable {
    private static final long serialVersionUID = -2275971337294798275L;

    /**
     * The algorithm used to compute the results. Can be {@code "nested"} (for a nested, agglomerative method),
     * {@code "all"} (for simulated annealing with all backbone sizes and no penalty), {@code "size"} (for simulated
     * annealing for a specific backbone size with no penalty), or {@code "penalty"} (for a penalized simulated
     * annealing approach).
     */
    private String method;

    /**
     * The entities found to be in the backbone set, as an array of strings.
     */
    private String[] backboneEntities;

    /**
     * The entities found to be in the redundant set, as an array of strings.
     */
    private String[] redundantEntities;

    /**
     * Euclidean spectral distance between the full network and the backbone network, without penalty factor for
     * the number of entities in the set, after optimization (i.e., for the final solution).
     */
    private double unpenalizedBackboneLoss;

    /**
     * Euclidean spectral distance between the full network and the redundant network, without penalty factor for
     * the number of entities in the set, after optimization (i.e., for the final solution).
     */
    private double unpenalizedRedundantLoss;

    /**
     * Penalty parameter. Typical values could be {@code 5.5}, {@code 7.5}, or {@code 12}, depending on application.
     */
    private double penalty;

    /**
     * Iterations parameter. For example, {@code 50000} should be sufficient for most applications, if not less.
     */
    private int iterations;

    /**
     * The temperature according to the cooling schedule, for each iteration.
     */
    private double[] temperature;

    /**
     * The probability of accepting the proposed backbone solution (the candidate) in each iteration. {@code 1.0} if
     * the candidate is better than the current solution.
     */
    private double[] acceptanceProbability;

    /**
     * A boolean/dummy variable indicating if the proposed solution (the candidate) was accepted as the new current
     * solution, for each iteration.
     */
    private int[] acceptance;

    /**
     * Euclidean spectral distance between the full network and the backbone network, penalized by a function of the
     * number of entities in the set, for the current solution adopted or kept in each iteration.
     */
    private double[] penalizedBackboneLoss;

    /**
     * Number of entities in the proposed (candidate) backbone set in each iteration.
     */
    private int[] proposedBackboneSize;

    /**
     * Number of entities in the current backbone set at the end of each iteration.
     */
    private int[] currentBackboneSize;

    /**
     * Number of entities in the most optimal backbone set that has been identified so far.
     */
    private int[] optimalBackboneSize;

    /**
     * The share of iterations among the last 100 iterations (or fewer if 100 iterations have not been reached yet)
     * in which a proposed backbone candidate was accepted. This can serve as a convergence criterion.
     */
    private double[] acceptanceRatioMovingAverage;

    /**
     * The full network matrix.
     */
    private double[][] fullNetwork;

    /**
     * The network matrix based only on the backbone concepts, after optimization.
     */
    private double[][] backboneNetwork;

    /**
     * The network matrix based only on the redundant concepts, after optimization.
     */
    private double[][] redundantNetwork;

    /**
     * Row and column names for the network matrices.
     */
    private String[] labels;

    /**
     * Start date/time of the network in seconds since 1 January 1970.
     */
    private long start;

    /**
     * End date/time of the network in seconds since 1 January 1970.
     */
    private long stop;

    /**
     * Number of statements used to create the network.
     */
    private int numStatements;

    /**
     * Create a new backbone result.
     *
     * @param method The backbone algorithm.
     * @param backboneEntities The entities found to be in the backbone set, as an array of strings.
     * @param redundantEntities The entities found to be in the redundant set, as an array of strings.
     * @param unpenalizedBackboneLoss Euclidean spectral distance between the full network and the backbone network
     *                                      without penalty factor for the number of entities in the set, after
     *                                      optimization (i.e., for the final solution).
     * @param unpenalizedRedundantLoss Euclidean spectral distance between the full network and the redundant
     *                                       optimization (i.e., for the final solution).
     * @param penalty Penalty parameter. Typical values could be {@code 5.5}, {@code 7.5}, or {@code 12}, depending
     *                      on application.
     * @param iterations Iterations parameter. For example, {@code 50000} should be sufficient for most
     *                        applications, if not less.
     * @param temperature The temperature according to the cooling schedule, for each iteration.
     * @param acceptanceProbability The probability of accepting the proposed backbone solution (the candidate) in
     *                                   each iteration. {@code 1.0} if the candidate is better than the current
     *                                   solution.
     * @param acceptance A boolean/dummy variable indicating if the proposed solution (the candidate) was accepted
     *                        as the new current solution, for each iteration.
     * @param penalizedBackboneLoss Euclidean spectral distance between the full network and the backbone network,
     *                                   penalized by a function of the number of entities in the set, for the
     *                                   current solution adopted or kept in each iteration.
     * @param proposedBackboneSize Number of entities in the proposed (candidate) backbone set in each iteration.
     * @param currentBackboneSize Number of entities in the current backbone set at the end of each iteration.
     * @param optimalBackboneSize Number of entities in the most optimal backbone set that has been identified so far.
     * @param acceptanceRatioMovingAverage The share of iterations among the last 100 iterations (or fewer if 100
     *                                          iterations have not been reached yet) in which a proposed backbone
     *                                          candidate was accepted. This can serve as a convergence criterion.
     * @param fullNetwork The full network matrix as a 2D double array.
     * @param backboneNetwork The network matrix based only on the backbone concepts, after optimization as a 2D double
     *                        array.
     * @param redundantNetwork The network matrix based only on the redundant concepts, after optimization as a 2D
     *                        double array.
     * @param labels The network labels.
     * @param start Start date and time.
     * @param stop Stop date and time.
     * @param numStatements The number of filtered statements contributing to the full network.
     */
    public SimulatedAnnealingBackboneResult(String method,
                                            String[] backboneEntities,
                                            String[] redundantEntities,
                                            double unpenalizedBackboneLoss,
                                            double unpenalizedRedundantLoss,
                                            double penalty,
                                            int iterations,
                                            double[] temperature,
                                            double[] acceptanceProbability,
                                            int[] acceptance,
                                            double[] penalizedBackboneLoss,
                                            int[] proposedBackboneSize,
                                            int[] currentBackboneSize,
                                            int[] optimalBackboneSize,
                                            double[] acceptanceRatioMovingAverage,
                                            double[][] fullNetwork,
                                            double[][] backboneNetwork,
                                            double[][] redundantNetwork,
                                            String[] labels,
                                            long start,
                                            long stop,
                                            int numStatements) {
        this.method = method;
        this.backboneEntities = backboneEntities;
        this.redundantEntities = redundantEntities;
        this.unpenalizedBackboneLoss = unpenalizedBackboneLoss;
        this.unpenalizedRedundantLoss = unpenalizedRedundantLoss;
        this.penalty = penalty;
        this.iterations = iterations;
        this.temperature = temperature;
        this.acceptanceProbability = acceptanceProbability;
        this.acceptance = acceptance;
        this.penalizedBackboneLoss = penalizedBackboneLoss;
        this.proposedBackboneSize = proposedBackboneSize;
        this.currentBackboneSize = currentBackboneSize;
        this.optimalBackboneSize = optimalBackboneSize;
        this.acceptanceRatioMovingAverage = acceptanceRatioMovingAverage;
        this.fullNetwork = fullNetwork;
        this.backboneNetwork = backboneNetwork;
        this.redundantNetwork = redundantNetwork;
        this.labels = labels;
        this.start = start;
        this.stop = stop;
        this.numStatements = numStatements;
    }

    public String getMethod() {
        return this.method;
    }

    public String[] getBackboneEntities() {
        return backboneEntities;
    }

    public void setBackboneEntities(String[] backboneEntities) {
        this.backboneEntities = backboneEntities;
    }

    public String[] getRedundantEntities() {
        return redundantEntities;
    }

    public void setRedundantEntities(String[] redundantEntities) {
        this.redundantEntities = redundantEntities;
    }

    public double getUnpenalizedBackboneLoss() {
        return unpenalizedBackboneLoss;
    }

    public void setUnpenalizedBackboneLoss(double unpenalisedBackboneLoss) {
        this.unpenalizedBackboneLoss = unpenalisedBackboneLoss;
    }

    public double getUnpenalizedRedundantLoss() {
        return unpenalizedRedundantLoss;
    }

    public void setUnpenalizedRedundantLoss(double unpenalisedRedundantLoss) {
        this.unpenalizedRedundantLoss = unpenalisedRedundantLoss;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public double[] getTemperature() {
        return temperature;
    }

    public void setTemperature(double[] temperature) {
        this.temperature = temperature;
    }

    public double[] getAcceptanceProbability() {
        return acceptanceProbability;
    }

    public void setAcceptanceProbability(double[] acceptanceProbability) {
        this.acceptanceProbability = acceptanceProbability;
    }

    public int[] getAcceptance() {
        return acceptance;
    }

    public void setAcceptance(int[] acceptance) {
        this.acceptance = acceptance;
    }

    public double[] getPenalizedBackboneLoss() {
        return penalizedBackboneLoss;
    }

    public void setPenalizedBackboneLoss(double[] penalisedBackboneLoss) {
        this.penalizedBackboneLoss = penalisedBackboneLoss;
    }

    public int[] getProposedBackboneSize() {
        return proposedBackboneSize;
    }

    public void setProposedBackboneSize(int[] proposedBackboneSize) {
        this.proposedBackboneSize = proposedBackboneSize;
    }

    public int[] getCurrentBackboneSize() {
        return currentBackboneSize;
    }

    public void setCurrentBackboneSize(int[] currentBackboneSize) {
        this.currentBackboneSize = currentBackboneSize;
    }

    public int[] getOptimalBackboneSize() {
        return optimalBackboneSize;
    }

    public void setOptimalBackboneSize(int[] optimalBackboneSize) {
        this.optimalBackboneSize = optimalBackboneSize;
    }

    public double[] getAcceptanceRatioMovingAverage() {
        return acceptanceRatioMovingAverage;
    }

    public void setAcceptanceRatioMovingAverage(double[] acceptanceRatioMovingAverage) {
        this.acceptanceRatioMovingAverage = acceptanceRatioMovingAverage;
    }

    public double[][] getFullNetwork() {
        return fullNetwork;
    }

    public void setFullNetwork(double[][] fullNetwork) {
        this.fullNetwork = fullNetwork;
    }

    public double[][] getBackboneNetwork() {
        return backboneNetwork;
    }

    public void setBackboneNetwork(double[][] backboneNetwork) {
        this.backboneNetwork = backboneNetwork;
    }

    public double[][] getRedundantNetwork() {
        return redundantNetwork;
    }

    public void setRedundantNetwork(double[][] redundantNetwork) {
        this.redundantNetwork = redundantNetwork;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStop() {
        return stop;
    }

    public void setStop(long stop) {
        this.stop = stop;
    }

    public int getNumStatements() {
        return numStatements;
    }

    public void setNumStatements(int numStatements) {
        this.numStatements = numStatements;
    }
}