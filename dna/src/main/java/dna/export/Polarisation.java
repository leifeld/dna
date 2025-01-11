package dna.export;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import me.tongfei.progressbar.ProgressBar;
import model.Entity;
import model.StatementType;

/*
* Class to calculate the polarisation of a series of networks.
*/
public class Polarisation {
    Exporter exporter;
    final StatementType statementType;
    final String variable1, variable2, qualifier, duplicates, timeWindow, kernel, algorithm;
    final boolean variable1Document, variable2Document, qualifierDocument, invertValues, invertAuthors, invertSources, invertSections, invertTypes, indentTime, normaliseScores;
    final LocalDateTime ldtStart, ldtStop;
    int windowSize;
    final HashMap<String, ArrayList<String>> excludeValueMap;
    final String[] excludeAuthors, excludeSources, excludeSections, excludeTypes;
	ArrayList<Matrix> congruence, conflict;
	final int numParents, numClusters, numIterations;
	final double elitePercentage, mutationPercentage;
	final long randomSeed;
	PolarisationResultTimeSeries results;

    public Polarisation(StatementType statementType, String variable1, boolean variable1Document, String variable2,
            boolean variable2Document, String qualifier, boolean qualifierDocument, String duplicates,
			LocalDateTime ldtStart, LocalDateTime ldtStop, String timeWindow, int windowSize, String kernel,
			boolean indentTime, HashMap<String, ArrayList<String>> excludeValueMap, String[] excludeAuthors,
			String[] excludeSources, String[] excludeSections, String[] excludeTypes, boolean invertValues,
			boolean invertAuthors, boolean invertSources, boolean invertSections, boolean invertTypes,
			String algorithm, boolean normaliseScores, int numClusters, int numParents, int numIterations,
			double elitePercentage, double mutationPercentage, long randomSeed) {

		// Validate input parameters
		if (!algorithm.equals("genetic") && !algorithm.equals("greedy")) {
			this.algorithm = "greedy";
			LogEvent log = new LogEvent(Logger.WARNING, "Invalid algorithm.",
					"Algorithm must be 'genetic' or 'greedy'. Using 'greedy' instead.");
			Dna.logger.log(log);
		} else {
			this.algorithm = algorithm;
		}
		if (numParents <= 0) {
			this.numParents = 50;
			LogEvent log = new LogEvent(Logger.WARNING, "Invalid number of cluster solutions.",
					"Number of cluster solutions (= parents) must be positive. Using 50 parents instead.");
			Dna.logger.log(log);
		} else {
			this.numParents = numParents;
		}
		if (numClusters <= 1) {
			this.numClusters = 2;
			LogEvent log = new LogEvent(Logger.WARNING, "Invalid number of clusters.",
					"Number of clusters (k) must be greater than 1. Using 2 clusters instead.");
			Dna.logger.log(log);
		} else {
			this.numClusters = numClusters;
		}
		if (numIterations <= 0) {
			this.numIterations = 1000;
			LogEvent log = new LogEvent(Logger.WARNING, "Invalid number of iterations.",
					"Number of iterations must be positive. Using 1000 iterations instead.");
			Dna.logger.log(log);
		} else {
			this.numIterations = numIterations;
		}
		if (elitePercentage < 0.0 || elitePercentage > 1.0) {
			this.elitePercentage = 0.1;
			LogEvent log = new LogEvent(Logger.WARNING, "Invalid elite percentage.",
					"Elite percentage must be between 0 and 1 (inclusive). Using 0.1 instead.");
			Dna.logger.log(log);
		} else {
			this.elitePercentage = elitePercentage;
		}
		if (mutationPercentage < 0.0 || mutationPercentage > 1.0) {
			this.mutationPercentage = 0.1;
			LogEvent log = new LogEvent(Logger.WARNING, "Invalid mutation percentage.",
					"Mutation percentage must be between 0 and 1 (inclusive). Using 0.1 instead.");
			Dna.logger.log(log);
		} else {
			this.mutationPercentage = mutationPercentage;
		}

        this.statementType = statementType;
        this.variable1 = variable1;
        this.variable2 = variable2;
        this.qualifier = qualifier;
        this.variable1Document = variable1Document;
        this.variable2Document = variable2Document;
        this.qualifierDocument = qualifierDocument;
        this.duplicates = duplicates;
        this.ldtStart = ldtStart;
        this.ldtStop = ldtStop;
        this.timeWindow = timeWindow;
        this.windowSize = windowSize;
        this.excludeValueMap = excludeValueMap;
        this.excludeAuthors = excludeAuthors;
        this.excludeSources = excludeSources;
        this.excludeSections = excludeSections;
        this.excludeTypes = excludeTypes;
        this.invertValues = invertValues;
        this.invertAuthors = invertAuthors;
        this.invertSources = invertSources;
        this.invertSections = invertSections;
        this.invertTypes = invertTypes;
		this.kernel = kernel;
		this.indentTime = indentTime;
		this.normaliseScores = normaliseScores;
		this.randomSeed = randomSeed;
		this.congruence = new ArrayList<Matrix>();
		this.conflict = new ArrayList<Matrix>();

        // initialize Exporter class
		this.exporter = new Exporter(
            "onemode",
            this.statementType,
            this.variable1,
            this.variable1Document,
            this.variable2,
            this.variable2Document,
            this.qualifier,
            false,
            "subtract",
            "average",
            false,
            this.duplicates,
            this.ldtStart,
            this.ldtStop,
            this.timeWindow,
            this.windowSize,
            this.excludeValueMap,
            Stream.of(this.excludeAuthors).collect(Collectors.toCollection(ArrayList::new)),
            Stream.of(this.excludeSources).collect(Collectors.toCollection(ArrayList::new)),
            Stream.of(this.excludeSections).collect(Collectors.toCollection(ArrayList::new)),
            Stream.of(this.excludeTypes).collect(Collectors.toCollection(ArrayList::new)),
            this.invertValues,
            this.invertAuthors,
            this.invertSources,
            this.invertSections,
            this.invertTypes,
            null,
            null);
	
 		this.exporter.setKernelFunction(this.kernel);
		this.exporter.setIndentTime(this.indentTime);

		this.exporter.loadData();
		this.exporter.filterStatements();

		this.computeKernelSmoothedTimeSlices();

		if (this.algorithm.equals("genetic")) {
			this.results = this.geneticAlgorithm();
		} else if (this.algorithm.equals("greedy")) {
			this.results = this.greedyAlgorithm();
		}
    }

	public PolarisationResultTimeSeries getResults() {
		return this.results;
	}

	/**
	 * Calculates the quality of polarisation based on the absolute differences 
	 * between observed and expected congruence and conflict within and between clusters.
	 *
	 * @param memberships An array where each element represents the cluster membership of a node.
	 * @param congruenceNetwork A 2D array representing the congruence network.
	 * @param conflictNetwork A 2D array representing the conflict network.
	 * @param normaliseScores Should the result be divided by its theoretical maximum (the sum of the two matrix norms)?
	 * @param numClusters The number of clusters.
	 * @return The quality of polarization as a double value.
	 */
	private double qualityAbsdiff(int[] memberships, double[][] congruenceNetwork, double[][] conflictNetwork, boolean normalise, int numClusters) {
		double congruenceNorm = calculateMatrixNorm(congruenceNetwork);
		double conflictNorm = calculateMatrixNorm(conflictNetwork);

		int[] clusterMembers = new int[numClusters];
		for (int i = 0; i < memberships.length; i++) {
			clusterMembers[memberships[i]]++;
		}
		int numWithinClusterDyads = 0;
		for (int i = 0; i < numClusters; i++) {
			numWithinClusterDyads += clusterMembers[i] * (clusterMembers[i] - 1);
		}
		int numBetweenClusterDyads = memberships.length * (memberships.length - 1) - numWithinClusterDyads;
		double[] expectedWithinClusterCongruence = new double[numClusters];
		for (int i = 0; i < numClusters; i++) {
			double clusterFactor = (double) clusterMembers[i] * (clusterMembers[i] - 1) / numWithinClusterDyads; // Proportion of within-cluster dyads that fall into cluster i (around 0.5 for two clusters)
			expectedWithinClusterCongruence[i] = clusterFactor * (congruenceNorm / numWithinClusterDyads); // Expected congruence for within-cluster dyads by cluster
		}

		double absdiff = 0.0;
		for (int i = 0; i < congruenceNetwork.length; i++) {
			for (int j = 0; j < congruenceNetwork[0].length; j++) {
				if (i != j) {
					if (memberships[i] == memberships[j]) {
						absdiff += Math.abs(congruenceNetwork[i][j] - expectedWithinClusterCongruence[memberships[i]]); // Within-cluster congruence
						// absdiff += Math.abs(conflictNetwork[i][j]); // Conflict within clusters // not necessary, would count deviation twice
					} else {
						// absdiff += Math.abs(congruenceNetwork[i][j]); // Between-cluster congruence // not necessary, would count deviation twice
						double betweenFactor = (double) clusterMembers[memberships[i]] * clusterMembers[memberships[j]] / numBetweenClusterDyads;
						double expectedBetweenClusterConflict = betweenFactor * (conflictNorm / numBetweenClusterDyads);
						absdiff += Math.abs(conflictNetwork[i][j] - expectedBetweenClusterConflict); // Between-cluster conflict
					}
				}
			}
		}
		if (normalise) {
			return (absdiff / (congruenceNorm + conflictNorm));
		} else {
			return absdiff;
		}
	}

	/**
	 * For a given double array, rank its values in descending order, starting at 0.
	 *
	 * @param arr A double array.
	 * @return An array of ranks, starting with 0.
	 */
	private int[] calculateRanks(double... arr) {
		class Pair {
			final double value;
			final int index;

			Pair(double value, int index) {
				this.value = value;
				this.index = index;
			}
		}

		Pair[] pairs = new Pair[arr.length];
		for (int index = 0; index < arr.length; ++index) {
			pairs[index] = new Pair(arr[index], index);
		}

		// Sort pairs by value in descending order
		Arrays.sort(pairs, (p1, p2) -> -Double.compare(p1.value, p2.value));

		int[] ranks = new int[arr.length];
		for (int i = 0; i < pairs.length; ++i) {
			ranks[pairs[i].index] = i;
		}

		return ranks;
	}

	/**
	 * For the genetic algorithm: Define a class that represents pairs of two
	 * indices of membership bits (i.e., index of the first node and index of
	 * the second node in a membership solution, with a maximum of N nodes).
	 */
	private class MembershipPair {
		int firstIndex; // Index of the first member
		int secondIndex; // Index of the second member

		/**
		 * Constructs a MembershipPair with the specified indices.
		 *
		 * @param firstIndex  the index of the first member
		 * @param secondIndex the index of the second member
		 */
		MembershipPair(int firstIndex, int secondIndex) {
			this.firstIndex = firstIndex;
			this.secondIndex = secondIndex;
		}

		/**
		 * Returns the first index.
		 *
		 * @return the first index
		 */
		int getFirstIndex() {
			return this.firstIndex;
		}

		/**
		 * Retrieves the value of the second index.
		 *
		 * @return the value of the second index.
		 */
		int getSecondIndex() {
			return this.secondIndex;
		}
	}

	/**
	 * This class represents a cluster solution in the genetic algorithm,
	 * including the membership vector, which contains information on cluster
	 * membership for each node in the network. It also contains the number of
	 * nodes N and the number of clusters K.
	 */
	private class ClusterSolution implements Cloneable {

		private final int[] memberships; // cluster memberships of all nodes, starting with 0
		private final int N; // number of nodes
		private final int K; // number of clusters

		/**
		 * Constructs a ClusterSolution with the specified parameters.
		 *
		 * @param n           The number of nodes (must be positive).
		 * @param k           The number of clusters (must be positive).
		 * @param memberships The membership vector (length must equal n, values must be in the range [0, k - 1]).
		 * @throws IllegalArgumentException If any parameter is invalid.
		 */
		ClusterSolution(int n, int k, int[] memberships) {
			if (n <= 0) {
				throw new IllegalArgumentException("N must be positive.");
			}
			if (k <= 1) {
				throw new IllegalArgumentException("K must be larger than 1.");
			}
			if (n <= k) {
				throw new IllegalArgumentException("N must be larger than K.");
			}
			if (memberships == null || memberships.length != n) {
				throw new IllegalArgumentException("Memberships must have length equal to N.");
			}
			validateMemberships(memberships, k);
			this.N = n;
			this.K = k;
			this.memberships = memberships.clone(); // defensive copy to avoid external modification
		}

		/**
		 * Constructs a ClusterSolution with random memberships.
		 *
		 * @param n The number of nodes (must be positive).
		 * @param k The number of clusters (must be positive).
		 * @param rng The random number generator.
		 * @throws IllegalArgumentException If any parameter is invalid.
		 */
		ClusterSolution(int n, int k, Random rng) {
			if (n <= 0) {
				throw new IllegalArgumentException("N must be positive.");
			}
			if (k <= 1) {
				throw new IllegalArgumentException("K must be larger than 1.");
			}
			if (n <= k) {
				throw new IllegalArgumentException("N must be larger than K.");
			}
			this.N = n;
			this.K = k;
			this.memberships = createRandomMemberships(n, k, rng);
		}

		/**
		 * Returns a copy of the membership vector.
		 *
		 * @return A copy of the membership vector.
		 */
		public int[] getMemberships() {
			return memberships.clone(); // defensive copy to avoid external modification
		}

		/**
		 * Creates a deep clone of this ClusterSolution.
		 *
		 * @return A deep clone of this object.
		 * @throws CloneNotSupportedException If the object cannot be cloned.
		 */
		protected ClusterSolution clone() throws CloneNotSupportedException {
			return new ClusterSolution(this.N, this.K, this.memberships.clone());
		}

		/**
		 * Validates that all memberships are within the range [0, K - 1].
		 */
		private void validateMemberships(int[] memberships, int k) {
			for (int membership : memberships) {
				if (membership < 0 || membership >= k) {
					throw new IllegalArgumentException("Membership values must be in the range [0, K - 1].");
				}
			}
		}

		/**
		 * Randomly assigns N items into K groups, ensuring a roughly even distribution.
		 * 
		 * The group assignments are generated such that each group index (0 to K - 1)
		 * appears equally in the output unless N is not a multiple of K. The order of
		 * assignments is randomised to ensure a fair distribution. The function is used
		 * to create a balanced initial state for community detection algorithms.
		 *
		 * @param N Total number of items to assign to groups.
		 * @param K Number of groups.
		 * @param rng Random number generator.
		 * @return A shuffled array of group memberships.
		 */
		private int[] createRandomMemberships(int N, int K, Random rng) {
			// Preallocate ArrayList with an exact capacity of N to avoid resizing
			ArrayList<Integer> membership = new ArrayList<>(N);

			// Calculate the number of complete repetitions needed to cover N items
			int repetitions = (N + K - 1) / K; // Ceiling of N / K

			// Populate the membership list with repeated group indices
			for (int rep = 0; rep < repetitions; rep++) {
				for (int i = 0; i < K && membership.size() < N; i++) {
					// Add group index 'i' to the list, stopping early if size reaches N
					membership.add(i);
				}
			}

			// Shuffle the membership list to randomize the group assignments
			Collections.shuffle(membership);

			// Convert the ArrayList<Integer> to int[] for the final result
			return membership.stream().mapToInt(Integer::intValue).toArray();
		}

		/**
		 * Crossover breeding. Combines the membership vectors of the current solution and
		 * a foreign solution  to produce an offspring with balanced cluster distribution.
		 *
		 * @param foreignMemberships A membership vector of a foreign cluster solution.
		 * @param rng                The random number generator to use.
		 * @throws IllegalArgumentException If the input vector is invalid or incompatible.
		 */
		int[] crossover(int[] foreignMemberships, Random rng) {
			// Validate input
			if (foreignMemberships == null || foreignMemberships.length != this.memberships.length) {
				throw new IllegalArgumentException("Incompatible membership vector lengths.");
			}
			validateMemberships(foreignMemberships, K);

			// Step 1: Relabel clusters to align with maximum overlap
			int[][] overlapMatrix = calculateOverlapMatrix(this.memberships, foreignMemberships, K);
			int[] newMemberships = performRelabeling(this.memberships, foreignMemberships, overlapMatrix);

			// Step 2: Perform random crossover between relabeled membership vectors
			newMemberships = performCrossover(newMemberships, foreignMemberships, rng);

			// Step 3: Adjust cluster distribution to achieve balance
			newMemberships = balanceClusterDistribution(newMemberships, K);
			return newMemberships;
		}

		/**
		 * Calculates the overlap matrix between two membership vectors.
		 */
		private int[][] calculateOverlapMatrix(int[] memberships1, int[] memberships2, int k) {
			int[][] matrix = new int[k][k];
			for (int i = 0; i < memberships1.length; i++) {
				matrix[memberships1[i]][memberships2[i]]++;
			}
			return matrix;
		}

		/**
		 * Relabels clusters to maximize overlap between two membership vectors.
		 */
		private int[] performRelabeling(int[] memberships1, int[] memberships2, int[][] overlapMatrix) {
			int k = overlapMatrix.length; // Number of clusters
			int[] relabelMap = new int[k]; // Map from original cluster to new cluster
		
			// For each row, find the column with the maximum overlap
			boolean[] assigned = new boolean[k]; // Track assigned columns
			Arrays.fill(relabelMap, -1);
		
			for (int row = 0; row < k; row++) {
				double[] rowValues = new double[k];
				for (int col = 0; col < k; col++) {
					rowValues[col] = overlapMatrix[row][col];
				}
		
				// Use `calculateRanks` to rank columns for this row
				int[] ranks = calculateRanks(rowValues);
		
				// Assign the best column for this row
				for (int rank = 0; rank < ranks.length; rank++) {
					int col = ranks[rank];
					if (!assigned[col]) {
						relabelMap[row] = col;
						assigned[col] = true;
						break;
					}
				}
			}
		
			// Apply the relabeling map to the original memberships
			int[] relabeledMemberships = new int[memberships1.length];
			for (int i = 0; i < memberships1.length; i++) {
				relabeledMemberships[i] = relabelMap[memberships1[i]];
			}
		
			return relabeledMemberships;
		}
		
		/**
		 * Performs crossover by randomly combining bits from two membership vectors.
		 * 
		 * @param memberships1 The first membership vector.
		 * @param memberships2 The second membership vector.
		 * @param rng          The random number generator to use.
		 * @return recombined membership vector
		 */
		private int[] performCrossover(int[] memberships1, int[] memberships2, Random rng) {
			int[] result = new int[memberships1.length];
			for (int i = 0; i < memberships1.length; i++) {
				result[i] = (rng.nextBoolean()) ? memberships1[i] : memberships2[i];
			}
			return result;
		}

		/**
		 * Balances cluster distribution by adjusting over- and under-represented clusters.
		 */
		private int[] balanceClusterDistribution(int[] memberships, int k) {
			int[] counts = new int[k];
			List<List<Integer>> clusterIndices = new ArrayList<>();
			for (int i = 0; i < k; i++) clusterIndices.add(new ArrayList<>());
		
			// Populate counts and cluster indices
			for (int i = 0; i < memberships.length; i++) {
				counts[memberships[i]]++;
				clusterIndices.get(memberships[i]).add(i);
			}
		
			// Compute target sizes
			int base = memberships.length / k; // Base size for each cluster
			int extra = memberships.length % k; // Number of clusters with one extra element
			int[] maxAllowed = new int[k];
			for (int i = 0; i < k; i++) {
				maxAllowed[i] = base + ((i < extra) ? 1 : 0); // Add 1 to the first 'extra' clusters (due to modulo)
			}
		
			// Balance clusters
			for (int i = 0; i < k; i++) {
				while (counts[i] > maxAllowed[i]) {
					for (int j = 0; j < k; j++) {
						if (counts[j] < maxAllowed[j]) {
							// Move an element from cluster i to cluster j
							int idx = clusterIndices.get(i).remove(0);
							memberships[idx] = j; // Swap cluster membership from i to j
							counts[i]--;
							counts[j]++;
							clusterIndices.get(j).add(idx); // Mark index as belonging to cluster j
							break;
						}
					}
				}
			}
			return memberships;
		}
	}

	/**
	 * This class represents a single iteration of the genetic algorithm, including
	 * quality evaluation, elite retention, crossover, and mutation.
	 */
	private class GeneticIteration {
		final double[][] congruenceNetwork;
		final double[][] conflictNetwork;
		final int n;
		final int numElites;
		final int numMutations;
		final ArrayList<ClusterSolution> clusterSolutions;
		final boolean normalise;
		double[] q; // quality scores for each cluster solution
		ArrayList<ClusterSolution> children; // children cluster solutions

		/**
		 * Performs a single iteration of the genetic algorithm, including quality
		 * evaluation, elite retention, crossover, and mutation.
		 *
		 * @param clusterSolutions The cluster solutions (= parents).
		 * @param congruenceNetwork The congruence matrix.
		 * @param conflictNetwork The conflict matrix.
		 * @param normalise Should the quality/fitness scores be normalised?
		 * @param numClusters The number of clusters.
		 * @param rng The random number generator to use.
		 * @return A list of children cluster solutions.
		 */
		GeneticIteration(ArrayList<ClusterSolution> clusterSolutions, double[][] congruenceNetwork, double[][] conflictNetwork, boolean normalise, int numClusters, Random rng) {
			this.clusterSolutions = new ArrayList<>(clusterSolutions);
			this.normalise = normalise;
			this.congruenceNetwork = congruenceNetwork.clone();
			this.conflictNetwork = conflictNetwork.clone();
			this.n = this.congruenceNetwork.length;
			
			// Calculate the number of elites based on the percentage
			this.numElites = Math.max(1, (int) Math.round(elitePercentage * numParents)); // At least one elite
			LogEvent log = new LogEvent(Logger.MESSAGE, "Number of elites: " + this.numElites,
					"Number of elite solutions based on the elite percentage.");
			Dna.logger.log(log);

			// Calculate the number of mutations based on the percentage
			this.numMutations = (int) Math.round((mutationPercentage * n) / 2.0); // Half the number of nodes because we swap pairs
			log = new LogEvent(Logger.MESSAGE, "Number of mutations: " + this.numMutations,
					"Number of mutations based on the mutation percentage.");
			Dna.logger.log(log);

			this.q = evaluateQuality(this.congruenceNetwork, this.conflictNetwork, normalise, numClusters);
			this.children = eliteRetentionStep(this.clusterSolutions, this.q, this.numElites);
			this.children = crossoverStep(this.clusterSolutions, this.q, this.children, rng);
			this.children = mutationStep(this.children, this.numMutations, this.n,  rng);
		}

		/**
		 * Evaluates the quality of cluster solutions using the specified quality function.
		 * The quality scores are transformed to the range [-Inf, 0] where 0 is high fitness
		 * or [-1, 0] if normalisation is used.
		 *
		 * @param congruenceNetwork The congruence network matrix.
		 * @param conflictNetwork   The conflict network matrix.
		 * @param normalise         Normalise the results?
		 * @param numClusters       The number of clusters.
		 * @return An array of quality scores for each cluster solution.
		 */
		private double[] evaluateQuality(double[][] congruenceNetwork, double[][] conflictNetwork, boolean normalise, int numClusters) {
			double[] q = new double[clusterSolutions.size()];
			for (int i = 0; i < clusterSolutions.size(); i++) {
				int[] mem = clusterSolutions.get(i).getMemberships();
				q[i] = qualityAbsdiff(mem, congruenceNetwork, conflictNetwork, normalise, numClusters);
			}
			return q;
		}

		/**
		 * Creates an initially empty children generation and adds elites from the parent generation of cluster solutions.
		 *
		 * @param clusterSolutions The list of parent generation cluster solutions.
		 * @param q                The array of quality values for the parent generation (their modularity or EI scores transformed to [0, 1] where 1 is high fitness).
		 * @param numElites        The number of elite solutions to retain for the children generation.
		 * @return A list of children containing the cloned elite solutions from the parent generation.
		 * @throws IllegalArgumentException If the elite percentage is outside the valid range [0, 1].
		 */
		private ArrayList<ClusterSolution> eliteRetentionStep (ArrayList<ClusterSolution> clusterSolutions, double[] q, int numElites) {
			int[] qRanks = calculateRanks(q); // Rank the quality values in descending order

			ArrayList<ClusterSolution> children = new ArrayList<>();
			for (int i = 0; i < qRanks.length; i++) {
				if (qRanks[i] < numElites) {
					try {
						children.add((ClusterSolution) clusterSolutions.get(i).clone());
					} catch (CloneNotSupportedException e) {

						LogEvent log = new LogEvent(Logger.ERROR, "Elite solution at index " + i + " could not be cloned.", "Elite solutions are not copied to the children generation.");
						Dna.logger.log(log);
					}
				}
			}
			return children;
		}

		/**
		 * Performs the crossover step by generating additional children using roulette wheel sampling,
		 * based on the quality scores of cluster solutions, and appends them to an existing children list.
		 *
		 * @param clusterSolutions The list of parent cluster solutions.
		 * @param q                An array of quality scores corresponding to the cluster solutions.
		 * @param children         The existing children list produced by the elite retention step.
		 * @param rng              The random number generator to use.
		 * @return The updated children list with additional solutions generated through roulette sampling and crossover.
		 */
		private ArrayList<ClusterSolution> crossoverStep(ArrayList<ClusterSolution> clusterSolutions, double[] q, ArrayList<ClusterSolution> children, Random rng) {

			// adjust fitness scores to ensure that they are all non-negative and the sum is positive to make roulette wheel selection work
			double qMinimum = 0.0, qMaximum = 0.0, qTotal = 0.0;
			for (int i = 0; i < q.length; i++) {
				if (i == 0) {
					qMinimum = q[i];
					qMaximum = q[i];
				} else {
					if (q[i] < qMinimum) {
						qMinimum = q[i];
					}
					if (q[i] > qMaximum) {
						qMaximum = q[i];
					}
				}
				qTotal += q[i];
			}
			if (qMinimum < 0) { // either completely in the [-Inf, 0] range or in [-x, x] with unknown x (e.g., modularity, where x = 1) -> shift to [0, 2x] by subtracting lowest (negative) value
				qTotal = 0.0;
				for (int i = 0; i < q.length; i++) {
					q[i] = q[i] - qMinimum;
					qTotal += q[i];
				}
			}
			if (qTotal == 0.0) { // all values are 0 -> replace by uniform probabilities
				for (int i = 0; i < q.length; i++) {
					q[i] = 1.0;
					qTotal += 1.0;
				}
			}

			// hybrid roulette wheel sampling for fitness-proportional sampling with uniform random sampling element to create more diversity in the gene pool
			while (children.size() < numParents) {
				int firstParentIndex = -1, secondParentIndex = -1;

				//  select first parent with roulette wheel sampling (= probability proportional to fitness)
				double r = rng.nextDouble() * qTotal;
				double cumulative = 0.0;
				for (int i = 0; i < q.length; i++) {
					cumulative += q[i];
					if (r <= cumulative) {
						firstParentIndex = i;
						secondParentIndex = i; // provisional value to avoid breeding with oneself
						break;
					}
				}

				// select second parent with roulette wheel sampling or uniform random sampling
				while (secondParentIndex == firstParentIndex) { // avoid breeding with oneself
					// flip a coin to decide whether the second parent is selected via roulette wheel sampling or uniform random sampling
					if (rng.nextDouble() <= 0.5) {
						// select second parent with roulette wheel sampling (= probability proportional to fitness)
						r = rng.nextDouble() * qTotal;
						cumulative = 0.0;
						for (int i = 0; i < q.length; i++) {
							cumulative += q[i];
							if (r <= cumulative) {
								secondParentIndex = i;
								break;
							}
						}
					} else {
						// select second parent with uniform random sampling (to create more diversity in the gene pool)
						secondParentIndex = rng.nextInt(q.length);
					}
				}

				// create child by crossover of the two selected parents
				ClusterSolution c = clusterSolutions.get(firstParentIndex);
				int[] child = c.crossover(clusterSolutions.get(secondParentIndex).getMemberships(), rng);
				children.add(new ClusterSolution(n, numClusters, child));
			}

			return children;
		}

		/**
		 * Mutation step: Randomly select some pairs of cluster memberships ("chromosomes") in non-elite solutions and swap around their cluster membership.
		 * 
		 * @param children     The children generation of cluster solutions as an array list.
		 * @param numMutations The number of mutations to perform.
		 * @param n            The number of nodes in the network.
		 * @param rng          The random number generator to use.
		 * @return An array list with the mutated children generation of cluster solutions.
		 */
		private ArrayList<ClusterSolution> mutationStep(ArrayList<ClusterSolution> children, int numMutations, int n, Random rng) {
			if (numMutations <= 0) {
				return children; // No mutations to perform
			}
			if (numMutations < 0) {
				throw new IllegalArgumentException("Number of mutations must be non-negative.");
			}

			for (int i = numElites; i < numParents; i++) {
				int[] memberships = children.get(i).getMemberships();
				Set<MembershipPair> mutationPairs = new HashSet<>();

				// Generate unique mutation pairs
				while (mutationPairs.size() < numMutations) {
					int firstIndex = rng.nextInt(n);
					int secondIndex = rng.nextInt(n);

					// Ensure valid and unique pairs
					if (firstIndex != secondIndex && memberships[firstIndex] != memberships[secondIndex]) {
						MembershipPair pair = new MembershipPair(
								Math.min(firstIndex, secondIndex),
								Math.max(firstIndex, secondIndex));
						mutationPairs.add(pair);
					}
				}

				// Apply mutations by swapping memberships
				for (MembershipPair pair : mutationPairs) {
					int firstIndex = pair.getFirstIndex();
					int secondIndex = pair.getSecondIndex();
					int temp = memberships[firstIndex];
					memberships[firstIndex] = memberships[secondIndex];
					memberships[secondIndex] = temp;
				}
			}
			return children;
		}

		/**
		 * Returns the list of children cluster solutions.
		 *
		 * @return The list of children cluster solutions.
		 */
		public ArrayList<ClusterSolution> getChildren() {
			return this.children;
		}

		/**
		 * Returns the quality scores for each cluster solution.
		 * 
		 * @return The quality scores for each cluster solution.
		 */
		public double[] getQ() {
			return q;
		}
	}

	/**
	 * Prepare the genetic algorithm and run all the iterations over all time steps.
	 * Take out the maximum quality measure at the last step and create an object
	 * that stores the polarization results.
	 * 
	 * @return A PolarisationResultTimeSeries object containing the results of the genetic algorithm for each time step and iteration.
	 */
	public PolarisationResultTimeSeries geneticAlgorithm () {
		Random rng = (this.randomSeed == 0) ? new Random() : new Random(this.randomSeed); // Initialize random number generator
		ArrayList<PolarisationResult> polarisationResults = new ArrayList<>();
		try (ProgressBar pb = new ProgressBar("Genetic algorithm", this.congruence.size())) {
			for (int t = 0; t < this.congruence.size(); t++) {
				if (this.congruence.get(t).getMatrix().length > 0 && calculateMatrixNorm(this.congruence.get(t).getMatrix()) + calculateMatrixNorm(this.conflict.get(t).getMatrix()) != 0) { // if the network has no nodes or activity, skip this step and return 0 directly
					double[] qualityScores; // Quality scores for each time step
					double maxQ = -1;
					double avgQ, sdQ;
					int maxIndex = -1;
					boolean earlyConvergence = false;
					int lastIndex = -1;
		
					double[] maxQArray = new double[numIterations];
					double[] avgQArray = new double[numIterations];
					double[] sdQArray = new double[numIterations];
		
					// Create initially random cluster solutions; supply the number of nodes and clusters
					ArrayList<ClusterSolution> cs = new ArrayList<ClusterSolution>();
					for (int i = 0; i < numParents; i++) {
						cs.add(new ClusterSolution(this.congruence.get(t).getMatrix().length, numClusters, rng));
					}
		
					// Run through iterations and do the breeding, then collect results and stats
					lastIndex = numIterations - 1; // choose last possible value here as a default if early convergence does not happen
					for (int i = 0; i < numIterations; i++) {
						GeneticIteration geneticIteration = new GeneticIteration(cs, this.congruence.get(t).getMatrix(), this.conflict.get(t).getMatrix(), this.normaliseScores, this.numClusters, rng);
						cs = geneticIteration.getChildren();
		
						// compute summary statistics based on iteration step and retain them
						qualityScores = geneticIteration.getQ();
						maxQ = -1.0;
						avgQ = 0.0;
						sdQ = 0.0;
						maxIndex = -1;
						for (int j = 0; j < cs.size(); j++) {
							avgQ += qualityScores[j];
							if (qualityScores[j] > maxQ) {
								maxQ = qualityScores[j];
								maxIndex = j;
							}
						}
						avgQ = avgQ / numParents;
						for (int j = 0; j < numParents; j++) {
							sdQ = sdQ + Math.sqrt(((qualityScores[j] - avgQ) * (qualityScores[j] - avgQ)) / numParents);
						}
						maxQArray[i] = maxQ;
						avgQArray[i] = avgQ;
						sdQArray[i] = sdQ;
		
						// check early convergence
						earlyConvergence = true;
						if (i >= 10 && (double) Math.round(sdQ * 100) / 100 == 0.00 && (double) Math.round(maxQ * 100) / 100 == (double) Math.round(avgQ * 100) / 100) {
							for (int j = i - 10; j < i; j++) {
								if ((double) Math.round(maxQArray[j] * 100) / 100 != (double) Math.round(maxQ * 100) / 100 ||
										(double) Math.round(avgQArray[j] * 100) / 100 != (double) Math.round(avgQ * 100) / 100 ||
										(double) Math.round(sdQArray[j] * 100) / 100 != 0.00) {
									earlyConvergence = false;
								}
							}
						} else {
							earlyConvergence = false;
						}
						if (earlyConvergence == true) {
							lastIndex = i;
							break;
						}
					}
		
					// correct for early convergence in results vectors
					int finalIndex = lastIndex;
					for (int i = lastIndex; i >= 0; i--) {
						if (maxQArray[i] == maxQArray[lastIndex]) {
							finalIndex = i;
						} else {
							break;
						}
					}
					
					double[] maxQArrayTemp = new double[finalIndex + 1];
					double[] avgQArrayTemp = new double[finalIndex + 1];
					double[] sdQArrayTemp = new double[finalIndex + 1];
					for (int i = 0; i < finalIndex + 1; i++) {
						maxQArrayTemp[i] = maxQArray[i];
						avgQArrayTemp[i] = avgQArray[i];
						sdQArrayTemp[i] = sdQArray[i];
					}
					maxQArray = maxQArrayTemp;
					avgQArray = avgQArrayTemp;
					sdQArray = sdQArrayTemp;
		
					// save results in array as a complex object
					PolarisationResult pr = new PolarisationResult(
							maxQArray.clone(),
							avgQArray.clone(),
							sdQArray.clone(),
							maxQ,
							cs.get(maxIndex).getMemberships().clone(),
							this.congruence.get(t).getRowNames(),
							earlyConvergence,
							this.congruence.get(t).getStart(),
							this.congruence.get(t).getStop(),
							this.congruence.get(t).getDateTime());
					polarisationResults.add(pr);
				} else { // zero result because network is empty
					PolarisationResult pr = new PolarisationResult(
							new double[] { 0 },
							new double[] { 0 },
							new double[] { 0 },
							0.0,
							new int[0],
							new String[0],
							true,
							this.congruence.get(t).getStart(),
							this.congruence.get(t).getStop(),
							this.congruence.get(t).getDateTime());
							polarisationResults.add(pr);
				}
				pb.step();
			}
		}
		PolarisationResultTimeSeries polarisationResultTimeSeries = new PolarisationResultTimeSeries(polarisationResults);
		return polarisationResultTimeSeries;
	}

	/** Calculate the entrywise 1-norm (= the sum of absolute values) of a matrix. The
	 *  input matrix is represented by a two-dimensional double array.
	 * 
	 *  @param matrix The matrix for which to calculate the norm.
	 *  @return The entrywise 1-norm (= sum of all absolute cell values) of the matrix.
	 */
	private double calculateMatrixNorm(double[][] matrix) {
		if (matrix == null) {
			LogEvent log = new LogEvent(Logger.ERROR, "Matrix is null.", "Error when trying to calculate the matrix norm in the genetic algorithm. Matrix cannot be null.");
			Dna.logger.log(log);
			throw new IllegalArgumentException("Matrix cannot be null.");
		}
		double absoluteSum = 0.0;
		for (double[] row : matrix) {
			for (double value : row) {
				absoluteSum += Math.abs(value);
			}
		}
		return absoluteSum;
	}

    /**
     * Compute a series of network matrices using kernel smoothing.
     * This function creates a series of network matrices (one-mode or two-mode) similar to the time window approach,
     * but using kernel smoothing around a forward-moving mid-point on the time axis (gamma). The networks are defined
     * by the mid-point {@code gamma}, the window size {@code w}, and the kernel function. If isolates are included,
	 * all networks will have the same dimensions and labels. If isolates are excluded, the dimensions and labels will
	 * change over time. For polarisation, changing dimensions and labels are recommended.
     */
	public void computeKernelSmoothedTimeSlices() {

		// initialise variables and constants
		ArrayList<ExportStatement> filteredStatements = this.exporter.getFilteredStatements();
		Collections.sort(filteredStatements);
		if (this.windowSize % 2 != 0) { // windowSize is the w constant in the paper; only even numbers are acceptable because adding or subtracting w / 2 to or from gamma would not yield integers
			this.windowSize = this.windowSize + 1;
		}
		LocalDateTime firstDate = filteredStatements.get(0).getDateTime();
		LocalDateTime lastDate = filteredStatements.get(filteredStatements.size() - 1).getDateTime();
		final int W_HALF = windowSize / 2;
		LocalDateTime b = this.ldtStart.isBefore(firstDate) ? firstDate : this.ldtStart;  // start of statement list
		LocalDateTime e = this.ldtStop.isAfter(lastDate) ? lastDate : this.ldtStop;  // end of statement list
		LocalDateTime gamma = b; // current time while progressing through list of statements
		LocalDateTime e2 = e; // indented end point (e minus half w)
		if (this.indentTime) {
			if (timeWindow.equals("minutes")) {
				gamma = gamma.plusMinutes(W_HALF);
				e2 = e.minusMinutes(W_HALF);
			} else if (timeWindow.equals("hours")) {
				gamma = gamma.plusHours(W_HALF);
				e2 = e.minusHours(W_HALF);
			} else if (timeWindow.equals("days")) {
				gamma = gamma.plusDays(W_HALF);
				e2 = e.minusDays(W_HALF);
			} else if (timeWindow.equals("weeks")) {
				gamma = gamma.plusWeeks(W_HALF);
				e2 = e.minusWeeks(W_HALF);
			} else if (timeWindow.equals("months")) {
				gamma = gamma.plusMonths(W_HALF);
				e2 = e.minusMonths(W_HALF);
			} else if (timeWindow.equals("years")) {
				gamma = gamma.plusYears(W_HALF);
				e2 = e.minusYears(W_HALF);
			}
		}

		// save the labels of the qualifier and put indices in hash maps for fast retrieval
		String[] qualValues = new String[] { "" };
		if (this.qualifier != null) {
			 qualValues = this.exporter.extractLabels(filteredStatements, this.qualifier, this.qualifierDocument);
		}
		if (this.qualifier != null && this.exporter.getDataType(this.qualifier).equals("integer")) {
			int[] qual = this.exporter.getOriginalStatements().stream().mapToInt(s -> (int) s.get(this.qualifier)).distinct().sorted().toArray();
			if (qual.length < qualValues.length) {
				qualValues = IntStream.rangeClosed(qual[0], qual[qual.length - 1])
						.mapToObj(String::valueOf)
						.toArray(String[]::new);
			}
		}

		// create an array list of empty Matrix results, store all date-time stamps in them, and save indices in a hash map; also create X arrays for the kernel smoothing
		ArrayList<ArrayList<ExportStatement>[][][]> xArrayList = new ArrayList<>();
		if (this.kernel.equals("gaussian")) { // for each mid-point gamma, create an empty Matrix and save the start, mid, and end time points in it as defined by the start and end of the whole time range; the actual matrix is injected later

			// save the labels of the variables and put indices in hash maps for fast retrieval
			String[] var1Values = this.exporter.extractLabels(filteredStatements, this.variable1, this.variable1Document);
			String[] var2Values = this.exporter.extractLabels(filteredStatements, this.variable2, this.variable2Document);

			// create the empty matrices and X arrays
			if (timeWindow.equals("minutes")) {
				while (!gamma.isAfter(e2)) {
					this.congruence.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					this.conflict.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					xArrayList.add(create3dArray(var1Values, var2Values, qualValues, filteredStatements));
					gamma = gamma.plusMinutes(1);
				}
			} else if (timeWindow.equals("hours")) {
				while (!gamma.isAfter(e2)) {
					this.congruence.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					this.conflict.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					xArrayList.add(create3dArray(var1Values, var2Values, qualValues, filteredStatements));
					gamma = gamma.plusHours(1);
				}
			} else if (timeWindow.equals("days")) {
				while (!gamma.isAfter(e2)) {
					this.congruence.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					this.conflict.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					xArrayList.add(create3dArray(var1Values, var2Values, qualValues, filteredStatements));
					gamma = gamma.plusDays(1);
				}
			} else if (timeWindow.equals("weeks")) {
				while (!gamma.isAfter(e2)) {
					this.congruence.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					this.conflict.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					xArrayList.add(create3dArray(var1Values, var2Values, qualValues, filteredStatements));
					gamma = gamma.plusWeeks(1);
				}
			} else if (timeWindow.equals("months")) {
				while (!gamma.isAfter(e2)) {
					this.congruence.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					this.conflict.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					xArrayList.add(create3dArray(var1Values, var2Values, qualValues, filteredStatements));
					gamma = gamma.plusMonths(1);
				}
			} else if (timeWindow.equals("years")) {
				while (!gamma.isAfter(e2)) {
					this.congruence.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					this.conflict.add(new Matrix(var1Values, var1Values, false, b, gamma, e));
					xArrayList.add(create3dArray(var1Values, var2Values, qualValues, filteredStatements));
					gamma = gamma.plusYears(1);
				}
			}
		} else { // for each mid-point gamma, create an empty Matrix and save the start, mid, and end time points in it as defined by width w; the actual matrix is injected later
			if (timeWindow.equals("minutes")) {
				while (!gamma.isAfter(e2)) {
					final LocalDateTime g = gamma;
					ArrayList<ExportStatement> currentStatements = filteredStatements.stream().filter(s -> s.getDateTime().isAfter(g.minusMinutes(W_HALF).isBefore(b) ? b : g.minusMinutes(W_HALF)) && s.getDateTime().isBefore(g.plusMinutes(W_HALF).isAfter(e) ? e : g.plusMinutes(W_HALF))).collect(Collectors.toCollection(ArrayList::new));
					String[] var1ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable1, this.variable1Document);
					String[] var2ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable2, this.variable2Document);
					this.congruence.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusMinutes(W_HALF).isBefore(b) ? b : gamma.minusMinutes(W_HALF), gamma, gamma.plusMinutes(W_HALF).isAfter(e) ? e : gamma.plusMinutes(W_HALF)));
					this.conflict.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusMinutes(W_HALF).isBefore(b) ? b : gamma.minusMinutes(W_HALF), gamma, gamma.plusMinutes(W_HALF).isAfter(e) ? e : gamma.plusMinutes(W_HALF)));
					xArrayList.add(create3dArray(var1ValuesCurrent, var2ValuesCurrent, qualValues, currentStatements));
					gamma = gamma.plusMinutes(1);
				}
			} else if (timeWindow.equals("hours")) {
				while (!gamma.isAfter(e2)) {
					final LocalDateTime g = gamma;
					ArrayList<ExportStatement> currentStatements = filteredStatements.stream().filter(s -> s.getDateTime().isAfter(g.minusHours(W_HALF).isBefore(b) ? b : g.minusHours(W_HALF)) && s.getDateTime().isBefore(g.plusHours(W_HALF).isAfter(e) ? e : g.plusHours(W_HALF))).collect(Collectors.toCollection(ArrayList::new));
					String[] var1ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable1, this.variable1Document);
					String[] var2ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable2, this.variable2Document);
					this.congruence.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusHours(W_HALF).isBefore(b) ? b : gamma.minusHours(W_HALF), gamma, gamma.plusHours(W_HALF).isAfter(e) ? e : gamma.plusHours(W_HALF)));
					this.conflict.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusHours(W_HALF).isBefore(b) ? b : gamma.minusHours(W_HALF), gamma, gamma.plusHours(W_HALF).isAfter(e) ? e : gamma.plusHours(W_HALF)));
					xArrayList.add(create3dArray(var1ValuesCurrent, var2ValuesCurrent, qualValues, currentStatements));
					gamma = gamma.plusHours(1);
				}
			} else if (timeWindow.equals("days")) {
				while (!gamma.isAfter(e2)) {
					final LocalDateTime g = gamma;
					ArrayList<ExportStatement> currentStatements = filteredStatements.stream().filter(s -> s.getDateTime().isAfter(g.minusDays(W_HALF).isBefore(b) ? b : g.minusDays(W_HALF)) && s.getDateTime().isBefore(g.plusDays(W_HALF).isAfter(e) ? e : g.plusDays(W_HALF))).collect(Collectors.toCollection(ArrayList::new));
					String[] var1ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable1, this.variable1Document);
					String[] var2ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable2, this.variable2Document);
					this.congruence.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusDays(W_HALF).isBefore(b) ? b : gamma.minusDays(W_HALF), gamma, gamma.plusDays(W_HALF).isAfter(e) ? e : gamma.plusDays(W_HALF)));
					this.conflict.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusDays(W_HALF).isBefore(b) ? b : gamma.minusDays(W_HALF), gamma, gamma.plusDays(W_HALF).isAfter(e) ? e : gamma.plusDays(W_HALF)));
					xArrayList.add(create3dArray(var1ValuesCurrent, var2ValuesCurrent, qualValues, currentStatements));
					gamma = gamma.plusDays(1);
				}
			} else if (timeWindow.equals("weeks")) {
				while (!gamma.isAfter(e2)) {
					final LocalDateTime g = gamma;
					ArrayList<ExportStatement> currentStatements = filteredStatements.stream().filter(s -> s.getDateTime().isAfter(g.minusWeeks(W_HALF).isBefore(b) ? b : g.minusWeeks(W_HALF)) && s.getDateTime().isBefore(g.plusWeeks(W_HALF).isAfter(e) ? e : g.plusWeeks(W_HALF))).collect(Collectors.toCollection(ArrayList::new));
					String[] var1ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable1, this.variable1Document);
					String[] var2ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable2, this.variable2Document);
					this.congruence.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusWeeks(W_HALF).isBefore(b) ? b : gamma.minusWeeks(W_HALF), gamma, gamma.plusWeeks(W_HALF).isAfter(e) ? e : gamma.plusWeeks(W_HALF)));
					this.conflict.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusWeeks(W_HALF).isBefore(b) ? b : gamma.minusWeeks(W_HALF), gamma, gamma.plusWeeks(W_HALF).isAfter(e) ? e : gamma.plusWeeks(W_HALF)));
					xArrayList.add(create3dArray(var1ValuesCurrent, var2ValuesCurrent, qualValues, currentStatements));
					gamma = gamma.plusWeeks(1);
				}
			} else if (timeWindow.equals("months")) {
				while (!gamma.isAfter(e2)) {
					final LocalDateTime g = gamma;
					ArrayList<ExportStatement> currentStatements = filteredStatements.stream().filter(s -> s.getDateTime().isAfter(g.minusMonths(W_HALF).isBefore(b) ? b : g.minusMonths(W_HALF)) && s.getDateTime().isBefore(g.plusMonths(W_HALF).isAfter(e) ? e : g.plusMonths(W_HALF))).collect(Collectors.toCollection(ArrayList::new));
					String[] var1ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable1, this.variable1Document);
					String[] var2ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable2, this.variable2Document);
					this.congruence.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusMonths(W_HALF).isBefore(b) ? b : gamma.minusMonths(W_HALF), gamma, gamma.plusMonths(W_HALF).isAfter(e) ? e : gamma.plusMonths(W_HALF)));
					this.conflict.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusMonths(W_HALF).isBefore(b) ? b : gamma.minusMonths(W_HALF), gamma, gamma.plusMonths(W_HALF).isAfter(e) ? e : gamma.plusMonths(W_HALF)));
					xArrayList.add(create3dArray(var1ValuesCurrent, var2ValuesCurrent, qualValues, currentStatements));
					gamma = gamma.plusMonths(1);
				}
			} else if (timeWindow.equals("years")) {
				while (!gamma.isAfter(e2)) {
					final LocalDateTime g = gamma;
					ArrayList<ExportStatement> currentStatements = filteredStatements.stream().filter(s -> s.getDateTime().isAfter(g.minusYears(W_HALF).isBefore(b) ? b : g.minusYears(W_HALF)) && s.getDateTime().isBefore(g.plusYears(W_HALF).isAfter(e) ? e : g.plusYears(W_HALF))).collect(Collectors.toCollection(ArrayList::new));
					String[] var1ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable1, this.variable1Document);
					String[] var2ValuesCurrent = this.exporter.extractLabels(currentStatements, this.variable2, this.variable2Document);
					this.congruence.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusYears(W_HALF).isBefore(b) ? b : gamma.minusYears(W_HALF), gamma, gamma.plusYears(W_HALF).isAfter(e) ? e : gamma.plusYears(W_HALF)));
					this.conflict.add(new Matrix(var1ValuesCurrent, var1ValuesCurrent, false, gamma.minusYears(W_HALF).isBefore(b) ? b : gamma.minusYears(W_HALF), gamma, gamma.plusYears(W_HALF).isAfter(e) ? e : gamma.plusYears(W_HALF)));
					xArrayList.add(create3dArray(var1ValuesCurrent, var2ValuesCurrent, qualValues, currentStatements));
					gamma = gamma.plusYears(1);
				}
			}
		}

		// create kernel-smoothed congruence and conflict networks with parallel streams
		this.congruence = ProgressBar.wrap(
				Stream.iterate(0, i -> i + 1).limit(this.congruence.size()).parallel(), "Congruence")
				.map(index -> this.exporter.processTimeSlice(this.congruence.get(index), xArrayList.get(index)))
				.map(m -> {
					for (int i = 0; i < m.getMatrix().length; i++) {
						m.getMatrix()[i][i] = 0.0; // set diagonal to zero
					}
					return m;
				})
				.collect(Collectors.toCollection(ArrayList::new));
		this.conflict = ProgressBar.wrap(
				Stream.iterate(0, i -> i + 1).limit(this.conflict.size()).parallel(), "Conflict")
				.map(index -> this.exporter.processTimeSlice(this.conflict.get(index), xArrayList.get(index)))
				.map(m -> {
					for (int i = 0; i < m.getMatrix().length; i++) {
						m.getMatrix()[i][i] = 0.0; // set diagonal to zero
							}
					return m;
				})
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/** Create a 3D array of ExportStatements for the kernel smoothing approach (variable 1 x variable 2 x qualifier).
	 *
	 * @param var1Values The values of the first variable.
	 * @param var2Values The values of the second variable.
	 * @param qualValues The values of the qualifier.
	 * @param statements The list of ExportStatements.
	 * @return A 3D array of array lists of ExportStatements.
	 */
	private ArrayList<ExportStatement>[][][] create3dArray(String[] var1Values, String[] var2Values, String[] qualValues, ArrayList<ExportStatement> statements) {
		final HashMap<String, Integer> v1Map = new HashMap<>();
		for (int i = 0; i < var1Values.length; i++) {
			v1Map.put(var1Values[i], i);
		}
		final HashMap<String, Integer> v2Map = new HashMap<>();
		for (int i = 0; i < var2Values.length; i++) {
			v2Map.put(var2Values[i], i);
		}
		final HashMap<String, Integer> qMap = new HashMap<>();
		if (this.qualifier != null) {
			for (int i = 0; i < qualValues.length; i++) {
				qMap.put(qualValues[i], i);
			}
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<ExportStatement>[][][] X = (ArrayList<ExportStatement>[][][]) new ArrayList<?>[var1Values.length][var2Values.length][qualValues.length];
		for (int i = 0; i < var1Values.length; i++) {
			for (int j = 0; j < var2Values.length; j++) {
				if (this.qualifier == null) {
					X[i][j][0] = new ArrayList<ExportStatement>();
				} else {
					for (int k = 0; k < qualValues.length; k++) {
						X[i][j][k] = new ArrayList<ExportStatement>();
					}
				}
			}
		}

		statements.stream().forEach(s -> {
			int var1Index = -1;
			if (this.variable1Document) {
				if (this.variable1.equals("author")) {
					var1Index = v1Map.get(s.getAuthor());
				} else if (this.variable1.equals("source")) {
					var1Index = v1Map.get(s.getSource());
				} else if (this.variable1.equals("section")) {
					var1Index = v1Map.get(s.getSection());
				} else if (this.variable1.equals("type")) {
					var1Index = v1Map.get(s.getType());
				} else if (this.variable1.equals("id")) {
					var1Index = v1Map.get(s.getDocumentIdAsString());
				} else if (this.variable1.equals("title")) {
					var1Index = v1Map.get(s.getTitle());
				}
			} else {
				var1Index = v1Map.get(((Entity) s.get(this.variable1)).getValue());
			}
			int var2Index = -1;
			if (this.variable2Document) {
				if (this.variable2.equals("author")) {
					var2Index = v2Map.get(s.getAuthor());
				} else if (this.variable2.equals("source")) {
					var2Index = v2Map.get(s.getSource());
				} else if (this.variable2.equals("section")) {
					var2Index = v2Map.get(s.getSection());
				} else if (this.variable2.equals("type")) {
					var2Index = v2Map.get(s.getType());
				} else if (this.variable2.equals("id")) {
					var2Index = v2Map.get(s.getDocumentIdAsString());
				} else if (this.variable2.equals("title")) {
					var2Index = v2Map.get(s.getTitle());
				}
			} else {
				var2Index = v2Map.get(((Entity) s.get(this.variable2)).getValue());
			}
			int qualIndex = -1;
			if (this.qualifierDocument && this.qualifier != null) {
				if (this.qualifier.equals("author")) {
					qualIndex = qMap.get(s.getAuthor());
				} else if (this.qualifier.equals("source")) {
					qualIndex = qMap.get(s.getSource());
				} else if (this.qualifier.equals("section")) {
					qualIndex = qMap.get(s.getSection());
				} else if (this.qualifier.equals("type")) {
					qualIndex = qMap.get(s.getType());
				} else if (this.qualifier.equals("id")) {
					qualIndex = qMap.get(s.getDocumentIdAsString());
				} else if (this.qualifier.equals("title")) {
					qualIndex = qMap.get(s.getTitle());
				}
			} else {
				if (this.qualifier == null) {
					qualIndex = 0;
				} else if (this.exporter.getDataType(this.qualifier).equals("integer") || this.exporter.getDataType(this.qualifier).equals("boolean")) {
					qualIndex = qMap.get(String.valueOf((int) s.get(this.qualifier)));
				} else {
					qualIndex = qMap.get(((Entity) s.get(this.qualifier)).getValue());
				}
			}
			X[var1Index][var2Index][qualIndex].add(s);
		});

		return X;
	}


	/**
	 * Prepare the greedy membership swapping algorithm and run all the iterations.
	 * Take out the maximum quality measure at the last step and create an object
	 * that stores the polarisation results. Run the algorithm in parallel for all
	 * time windows.
	 */
	private PolarisationResultTimeSeries greedyAlgorithm () {
		Random rng = (this.randomSeed == 0) ? new Random() : new Random(this.randomSeed); // Initialize random number generator

		ArrayList<PolarisationResult> polarisationResults = ProgressBar
		.wrap(IntStream.range(0, Polarisation.this.congruence.size()).parallel(), "Greedy algorithm")
		.map(t -> greedyTimeStep(Polarisation.this.congruence.get(t),
				Polarisation.this.conflict.get(t),
				Polarisation.this.normaliseScores,
				Polarisation.this.numClusters,
				rng.nextLong()))
		.collect(Collectors.toCollection(ArrayList::new));

		PolarisationResultTimeSeries polarisationResultTimeSeries = new PolarisationResultTimeSeries(polarisationResults);
		return polarisationResultTimeSeries;
	}
	/**
	 * A single run of the greedy algorithm, for one pair of congruence and conflict
	 * network, i.e., for one time slice.
	 * 
	 * @param congruence      A Matrix object containing the 2D congruence array.
	 * @param conflict        A Matrix object containing the 2D conflict array.
	 * @param normaliseScores Normalise the absdiff quality/fitness scores to 1.0?
	 * @param numClusters     The number of clusters.
	 * @param seed            A random seed, which is used to create a new random number generator for this algorithm run. The seed should have been itself generated by a random number generator to ensure variability across time steps and reproducibility.
	 * @return a PolarisationResult object
	 */
	private PolarisationResult greedyTimeStep(Matrix congruence, Matrix conflict, boolean normaliseScores, int numClusters, long seed) {

		// for each time step, run the algorithm over the cluster solutions; retain quality and memberships
		double[][] congruenceMatrix = congruence.getMatrix();
		double[][] conflictMatrix = conflict.getMatrix();
		ArrayList<Double> maxQArray = new ArrayList<Double>();
		double combinedNorm = calculateMatrixNorm(congruenceMatrix) + calculateMatrixNorm(congruenceMatrix);

		if (congruenceMatrix.length > 0 || combinedNorm == 0.0) { // if the network has no nodes or edges, skip this step and return 0 directly

			// Create initially random cluster solution to update
			Random random = new Random(seed);
			ClusterSolution cs = new ClusterSolution(congruenceMatrix.length, numClusters, random);
			int[] mem = cs.getMemberships();

			// evaluate quality of initial solution
			maxQArray.add(qualityAbsdiff(mem, congruenceMatrix, conflictMatrix, normaliseScores, numClusters));
			int[] bestMemberships = mem.clone();
			double maxQ = maxQArray.get(0);

			boolean convergence  = false;
			while (!convergence) { // run the two nested for-loops repeatedly until there are no more swaps
				boolean noChanges = true;
				for (int i = 0; i < mem.length; i++) {
					for (int j = 1; j < mem.length; j++) { // swap positions i and j in the membership vector and see if leads to higher fitness
						if (i < j && mem[i] != mem[j]) {
							int[] mem2 = mem.clone();
							int oldI = mem2[i];
							int oldJ = mem2[j];
							mem2[i] = oldJ;
							mem2[j] = oldI;
							double q1 = qualityAbsdiff(mem, congruenceMatrix, conflictMatrix, normaliseScores, numClusters);
							double q2 = qualityAbsdiff(mem2, congruenceMatrix, conflictMatrix, normaliseScores, numClusters);
							if (q2 > q1) { // candidate solution has higher fitness -> keep it
								mem = mem2.clone(); // accept the new solution if it was better than the previous
								maxQArray.add(q2);
								maxQ = q2;
								bestMemberships = mem.clone();
								noChanges = false;
							}
						}
					}
				}
				if (noChanges) {
					convergence = true;
				}
			}

			double[] maxQArray2 = new double[maxQArray.size()];
			for (int i = 0; i < maxQArray.size(); i++) {
				maxQArray2[i] = maxQArray.get(i);
			}

			// save results in array as a complex object
			double[] avgQArray = maxQArray2;
			double[] sdQArray = new double[maxQArray.size()];
			PolarisationResult pr = new PolarisationResult(
					maxQArray2,
					avgQArray,
					sdQArray,
					maxQ,
					bestMemberships,
					congruence.getRowNames(),
					true,
					congruence.getStart(),
					congruence.getStop(),
					congruence.getDateTime());
			return pr;
		} else { // zero result because network is empty
			PolarisationResult pr = new PolarisationResult(
					new double[] { 0 },
					new double[] { 0 },
					new double[] { 0 },
					0.0,
					new int[0],
					new String[0],
					true,
					congruence.getStart(),
					congruence.getStop(),
					congruence.getDateTime());
			return pr;
		}
	}
}