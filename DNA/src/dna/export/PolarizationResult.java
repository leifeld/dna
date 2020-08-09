package dna.export;

import java.util.Date;

/**
 * Store the results of a single run of the genetic algorithm, i.e., for a
 * single time step of the time window algorithm or the whole network if no
 * time window was set.
 */
class PolarizationResult {
	double[] maxQ;
	double[] avgQ;
	double[] sdQ;
	double finalMaxQ;
	int[] memberships;
	String[] names;
	boolean earlyConvergence;
	Date start, stop, middle;

	/**
	 * Create a polarization result for a single time step.
	 * 
	 * @param maxQ              The maximum quality score for each iteration of the genetic algorithm.
	 * @param avgQ              The mean quality score for each iteration of the genetic algorithm.
	 * @param sdQ               The standard deviation of the quality scores for each iteration of the genetic algorithm.
	 * @param finalMaxQ         The maximum quality score of the final iteration of the genetic algorithm.
	 * @param memberships       A membership array containing the cluster levels for each node, starting with 0 and going up to K - 1.
	 * @param names             The node labels of the network.
	 * @param earlyConvergence  A boolean indicating whether the genetic algorithm converged before the last iteration.
	 * @param start             The start date and time of the time window network. Can be arbitrarily small if it is only a single network.
	 * @param stop              The end date and time of the time window network. Can be arbitrarily large if it is only a single network.
	 * @param middle            The mid-point date of the time window network. This is used to position the time polarization score on the time axis.
	 */
	PolarizationResult(double[] maxQ, double[] avgQ, double[] sdQ, double finalMaxQ, int[] memberships, String[] names, boolean earlyConvergence, Date start, Date stop, Date middle) {
		this.maxQ = maxQ;
		this.avgQ = avgQ;
		this.sdQ = sdQ;
		this.finalMaxQ = finalMaxQ;
		this.memberships = memberships;
		this.names = names;
		this.earlyConvergence = earlyConvergence;
		this.start = start;
		this.stop = stop;
		this.middle = middle;
	}

	public Date getStart() {
		return start;
	}

	public Date getStop() {
		return stop;
	}

	public Date getMiddle() {
		return middle;
	}

	public int[] getMemberships() {
		return memberships;
	}

	public String[] getNames() {
		return names;
	}

	public double[] getMaxQ() {
		return maxQ;
	}

	public double[] getAvgQ() {
		return avgQ;
	}

	public double[] getSdQ() {
		return sdQ;
	}

	public double getFinalMaxQ() {
		return finalMaxQ;
	}
}