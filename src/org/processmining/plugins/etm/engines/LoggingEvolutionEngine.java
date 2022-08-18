package org.processmining.plugins.etm.engines;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.parameters.ETMParamAbstract;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvaluatedCandidate;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionObserver;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.operators.EvolutionPipeline;

/**
 * A generic evolution engine that provides some logging functionalities (UNDER
 * CONSTRUCTION). In itself is not usable but provides some standard features.
 * 
 * @author jbuijs
 * 
 * @param <R>
 *            The Result Type of the engine (f.i. {@link NAryTree} or a
 *            {@link org.processmining.plugins.etm.model.ParetoFront} of these
 *            trees.
 */
public abstract class LoggingEvolutionEngine<R> extends GenerationalEvolutionEngine<NAryTree> {
	//TODO IDEA: implement method that can be called to create overview files from the detailed log files. EG finalize
	//TODO IDEAv2: implement statistics file logging that writes mean/avg/max/min/... for Of and each dimension for Excel plotting
	//TODO Split this engine into a logging engine, extending this engine to seperate code, make generic for Pareto engine too
	//TODO extend to work with mahout/hadoop, should be easy, see https://cwiki.apache.org/confluence/display/MAHOUT/Mahout.GA.Tutorial

	protected CentralRegistry centralRegistry;

	//The engine should be 'post processed' before it can be run
	//private boolean isReadyToRun = false;

	//Keeps track of the current generation
	protected int generation = 0;

	//TODO create getter/setters and use them from parameter file
	//Log only every ... generations
	private int logModulo = 1000;
	//At each logModulo trigger, output currently best tree to System.out
	private boolean sysoBestTreeAtLogModulo = true;

	//FIXME finish stats part (writing the stats file, sorting columns)
	//If true for each generation stats on the fitness values will be recorded for Excel plotting
	private boolean logStats = false;
	@SuppressWarnings("unused")
	private static final String COLSEP = "\t"; //tab separators
	private File statsFile = null;

	protected ETMParamAbstract<R> params;

	public LoggingEvolutionEngine(ETMParamAbstract<R> params) {
		super(params.getFactory(), new EvolutionPipeline<NAryTree>(params.getEvolutionaryOperators()), params
				.getFitnessEvaluator(), params.getSelectionStrategy(), params.getRng());

		// Add the evolutionary observers
		for (EvolutionObserver<NAryTree> obs : params.getEvolutionObservers()) {
			addEvolutionObserver(obs);
		}

		//We do multi-threading ourselves, so disable in framework.
		setSingleThreaded(false);
		setCentralRegistry(params.getCentralRegistry());
		setLogModulo(params.getLogModulo());

		this.params = params;
	}

	public NAryTree evolve() {
		return evolve(params.getPopulationSize(), params.getEliteCount(), params.getSeed(),
				params.getTerminationConditionsAsArray());
	}

	public List<EvaluatedCandidate<NAryTree>> evolvePopulation() {
		return evolvePopulation(params.getPopulationSize(), params.getEliteCount(), params.getSeed(),
				params.getTerminationConditionsAsArray());
	}

	protected List<EvaluatedCandidate<NAryTree>> evaluatePopulation(List<NAryTree> population) {
		//Call the existing function on the whole
		List<EvaluatedCandidate<NAryTree>> result = super.evaluatePopulation(population);

		//Sort the result list to create easier to read/scan logs (last result has highest fitness value!)
		Collections.sort(result);

		logPopulation(result);

		//And the next generation will have one number higher
		generation++;
		return result;
	}

	public void logPopulation(List<EvaluatedCandidate<NAryTree>> result) {
		//And, if the path is set, write the whole evaluated population to file
		if (params != null && params.getPath() != null) {

			//Write generation logging
			File log;
			if (generation == 0 || ((generation + 1) % logModulo) == 0) {
				log = new File(params.getPath() + File.separator + "generation" + generation + ".log");
			} else {
				log = new File(params.getPath() + File.separator + "000lastGen.log");
			}

			try {
				if (log.getParent() != null) {
					log.getParentFile().mkdirs();
				}
				log.createNewFile();
				FileWriter writer = new FileWriter(log);
				//Clear file contents in case of lastGen
				writer.write("");

				writer.append(logResult(result));

				/*-
				//log stats
				if (logStats) {
					//TODO add overall fitness!!!
					DescriptiveStatistics overallFitnessStats = new DescriptiveStatistics();
					HashMap<TreeFitnessInfo, DescriptiveStatistics> stats = new HashMap<TreeFitnessInfo, DescriptiveStatistics>(
							result.size());

					overallFitnessStats.addValue(centralRegistry.getFitness(tree).overallFitness);
					TObjectDoubleIterator<TreeFitnessInfo> it = centralRegistry.getFitness(tree).fitnessValues
							.iterator();

					while (it.hasNext()) {
						it.advance();
						TreeFitnessInfo dimension = it.key();
						double value = it.value();
						//Make sure there is a stats object
						if (!stats.containsKey(dimension)) {
							stats.put(dimension, new DescriptiveStatistics());
						}

						//Add value to stats
						stats.get(dimension).addValue(value);
					}
				}/**/

				writer.append("Generation: " + generation);

				//IF we should write to syso, then we also write at each logModulo the best tree to syso
				if (sysoBestTreeAtLogModulo && (generation == 0 || ((generation + 1) % logModulo) == 0)) {
					//Get last = best tree since we sorted!
					EvaluatedCandidate<NAryTree> cand = result.get(result.size() - 1);
					NAryTree tree = cand.getCandidate();
					System.out.print(generation + " f:" + cand.getFitness() + "  "
							+ TreeUtils.toString(tree, centralRegistry.getEventClasses()));
					if (centralRegistry != null && centralRegistry.isFitnessKnown(tree)) {
						System.out.print("  " + centralRegistry.getFitness(tree).toString());
					}
					System.out.println();
				}

				//Now write the stats file
				if (logStats) {
					if (statsFile == null) {
						statsFile = new File(params.getPath() + File.separator + "000stats.log");
						if (statsFile.getParent() != null) {
							statsFile.getParentFile().mkdirs();
						}
						statsFile.createNewFile();
						FileWriter statsWriter = new FileWriter(statsFile);
						statsWriter.write("COLUMNS"); //FIXME
					}

					try {
						@SuppressWarnings("unused")
						FileWriter statsWriter = new FileWriter(statsFile);

					} catch (IOException e) {
						System.err.println("LOST: " + params.getPath() + File.separator + "000stats.log");
					}
				}

				writer.close();
			} catch (IOException e) {
				System.err.println("LOST: " + params.getPath() + File.separator + "generation" + generation + ".log");
			}

		}
	}

	/**
	 * Write the current result to a file, should be implemented by each
	 * specific engine
	 * 
	 * @param cand
	 * @return
	 */
	public abstract String logResult(List<EvaluatedCandidate<NAryTree>> result);

	/**
	 * Returns the last completed generation
	 * 
	 * @return
	 */
	public int getGeneration() {
		return generation;
	}

	public void setCentralRegistry(CentralRegistry registry) {
		this.centralRegistry = registry;
	}

	/**
	 * Get after how many generations a full population log should be written
	 * 
	 * @return the logModulo
	 */
	public int getLogModulo() {
		return logModulo;
	}

	/**
	 * Set after how many generations a full population log should be written
	 * 
	 * @param logModulo
	 *            the logModulo to set
	 */
	public void setLogModulo(int logModulo) {
		if (logModulo < 1)
			logModulo = Integer.MAX_VALUE;
		this.logModulo = logModulo;
	}

	/**
	 * Gives the engine a reference to the parameter object if not done so
	 * already in the constructor
	 * 
	 * @param params
	 */
	public void setParameterObject(ETMParamAbstract<R> params) {
		this.params = params;
	}

}
