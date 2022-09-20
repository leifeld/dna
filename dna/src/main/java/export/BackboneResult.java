package export;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class representing backbone results.
 */
public class BackboneResult implements Serializable {
    private static final long serialVersionUID = -2275971337294798275L;

    /**
     * The entities found to be in the backbone set, as an array list of strings.
     */
    private ArrayList<String> backboneEntities;

    /**
     * The entities found to be in the redundant set, as an array list of strings.
     */
    private ArrayList<String> redundantEntities;

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
    private ArrayList<Double> temperature;

    /**
     * The probability of accepting the proposed backbone solution (the candidate) in each iteration. {@code 1.0} if
     * the candidate is better than the current solution.
     */
    private ArrayList<Double> acceptanceProbability;

    /**
     * A boolean/dummy variable indicating if the proposed solution (the candidate) was accepted as the new current
     * solution, for each iteration.
     */
    private ArrayList<Integer> acceptance;

    /**
     * Euclidean spectral distance between the full network and the backbone network, penalized by a function of the
     * number of entities in the set, for the current solution adopted or kept in each iteration.
     */
    private ArrayList<Double> penalizedBackboneLoss;

    /**
     * Number of entities in the proposed (candidate) backbone set in each iteration.
     */
    private ArrayList<Integer> proposedBackboneSize;

    /**
     * Number of entities in the current backbone set at the end of each iteration.
     */
    private ArrayList<Integer> currentBackboneSize;

    /**
     * Number of entities in the most optimal backbone set that has been identified so far.
     */
    private ArrayList<Integer> optimalBackboneSize;

    /**
     * The share of iterations among the last 100 iterations (or fewer if 100 iterations have not been reached yet)
     * in which a proposed backbone candidate was accepted. This can serve as a convergence criterion.
     */
    private ArrayList<Double> acceptanceRatioMovingAverage;

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
     * Create a new backbone result.
     *
     * @param backboneEntities The entities found to be in the backbone set, as an array list of strings.
     * @param redundantEntities The entities found to be in the redundant set, as an array list of strings.
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
     */
    public BackboneResult(ArrayList<String> backboneEntities,
                          ArrayList<String> redundantEntities,
                          double unpenalizedBackboneLoss,
                          double unpenalizedRedundantLoss,
                          double penalty,
                          int iterations,
                          ArrayList<Double> temperature,
                          ArrayList<Double> acceptanceProbability,
                          ArrayList<Integer> acceptance,
                          ArrayList<Double> penalizedBackboneLoss,
                          ArrayList<Integer> proposedBackboneSize,
                          ArrayList<Integer> currentBackboneSize,
                          ArrayList<Integer> optimalBackboneSize,
                          ArrayList<Double> acceptanceRatioMovingAverage,
                          double[][] fullNetwork,
                          double[][] backboneNetwork,
                          double[][] redundantNetwork,
                          String[] labels) {
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
    }

    public ArrayList<String> getBackboneEntities() {
        return backboneEntities;
    }

    public void setBackboneEntities(ArrayList<String> backboneEntities) {
        this.backboneEntities = backboneEntities;
    }

    public ArrayList<String> getRedundantEntities() {
        return redundantEntities;
    }

    public void setRedundantEntities(ArrayList<String> redundantEntities) {
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

    public ArrayList<Double> getTemperature() {
        return temperature;
    }

    public void setTemperature(ArrayList<Double> temperature) {
        this.temperature = temperature;
    }

    public ArrayList<Double> getAcceptanceProbability() {
        return acceptanceProbability;
    }

    public void setAcceptanceProbability(ArrayList<Double> acceptanceProbability) {
        this.acceptanceProbability = acceptanceProbability;
    }

    public ArrayList<Integer> getAcceptance() {
        return acceptance;
    }

    public void setAcceptance(ArrayList<Integer> acceptance) {
        this.acceptance = acceptance;
    }

    public ArrayList<Double> getPenalizedBackboneLoss() {
        return penalizedBackboneLoss;
    }

    public void setPenalizedBackboneLoss(ArrayList<Double> penalisedBackboneLoss) {
        this.penalizedBackboneLoss = penalisedBackboneLoss;
    }

    public ArrayList<Integer> getProposedBackboneSize() {
        return proposedBackboneSize;
    }

    public void setProposedBackboneSize(ArrayList<Integer> proposedBackboneSize) {
        this.proposedBackboneSize = proposedBackboneSize;
    }

    public ArrayList<Integer> getCurrentBackboneSize() {
        return currentBackboneSize;
    }

    public void setCurrentBackboneSize(ArrayList<Integer> currentBackboneSize) {
        this.currentBackboneSize = currentBackboneSize;
    }

    public ArrayList<Integer> getOptimalBackboneSize() {
        return optimalBackboneSize;
    }

    public void setOptimalBackboneSize(ArrayList<Integer> optimalBackboneSize) {
        this.optimalBackboneSize = optimalBackboneSize;
    }

    public ArrayList<Double> getAcceptanceRatioMovingAverage() {
        return acceptanceRatioMovingAverage;
    }

    public void setAcceptanceRatioMovingAverage(ArrayList<Double> acceptanceRatioMovingAverage) {
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
}