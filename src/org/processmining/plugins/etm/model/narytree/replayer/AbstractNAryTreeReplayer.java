package org.processmining.plugins.etm.model.narytree.replayer;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import nl.tue.astar.AStarException;
import nl.tue.astar.AStarObserver;
import nl.tue.astar.AStarThread;
import nl.tue.astar.AStarThread.ASynchronousMoveSorting;
import nl.tue.astar.AStarThread.Canceller;
import nl.tue.astar.AStarThread.QueueingModel;
import nl.tue.astar.Tail;
import nl.tue.astar.Trace;
import nl.tue.astar.impl.AbstractAStarThread;
import nl.tue.astar.impl.State;
import nl.tue.astar.impl.memefficient.MemoryEfficientAStarAlgorithm;
import nl.tue.storage.StorageException;

import org.processmining.plugins.boudewijn.treebasedreplay.TreeDelegate;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.replayer.hybridilp.NAryTreeHybridILPTail;

public abstract class AbstractNAryTreeReplayer<H extends NAryTreeHead, T extends Tail, D extends TreeDelegate<H, T>>
		implements NAryTreeReplayer<H, T, D> {

	protected final Canceller canceller;
	protected final D delegate;
	protected final int maxNumOfStates = 500 * 1000;
	protected final AStarAlgorithm aStarLogAbstraction;
	protected final NAryTree tree;

	protected TObjectIntHashMap<H> head2int;
	protected List<State<H, T>> stateList;
	protected long statecount = 0;

	protected MemoryEfficientAStarAlgorithm<H, T> algorithm;
	protected final int threads;
	private PerformanceType type;

	private long queuedStateCount;
	private long traversedArcCount;
	private final ExecutorService executor;
	private final AStarObserver[] observers;
	private AtomicBoolean wasReliable = new AtomicBoolean(true);

	/**
	 * Indicates how many blocks can be used in the store/statespace. If more
	 * than this amount is measured IN BETWEEN TRACE ALIGNMENTS the store will
	 * be emptied. Note that if at the beginning of a trace alignment maxBlocks
	 * - 1 has been in use and the current trace alignment adds 2 or more
	 * blocks, the algorithm will (try to) allocate those blocks (and thus can
	 * run out of memory). This amount should be set using
	 * setMaxNumberofBlockToUse (23-09-2013)
	 * 
	 * @author jbuijs
	 */
	private long maxNrOfBlocksToUse = Integer.MAX_VALUE;
	//(jbuijs 23-9-13) To be set in this class (setType) and to be used in setMaxNumberOfBlocksToUse
	private int blockSize;
	private boolean debugMode = false;

	/**
	 * Single threaded variant
	 * 
	 * @param aStarLogAbstraction
	 *            AStarAlgorithm to run
	 * @param canceller
	 *            Canceller used to listen to, to stop execution
	 * @param tree
	 *            NAryTree to align on
	 * @param configurationNumber
	 *            Configuration to use
	 * @param node2Cost
	 *            Costs assigned to nodes in tree
	 * @param useOrRows
	 *            Whether OR rows should be used
	 * @param observers
	 */
	public AbstractNAryTreeReplayer(AStarAlgorithm aStarLogAbstraction, Canceller canceller, NAryTree tree,
			int configurationNumber, int[] node2Cost, boolean useOrRows, AStarObserver... observers) {
		//Pass on to single threaded variant which we always use
		this(aStarLogAbstraction, canceller, tree, configurationNumber, node2Cost, useOrRows, 1, observers);
	}

	/**
	 * 
	 * 
	 * @param aStarLogAbstraction
	 *            AStarAlgorithm to run
	 * @param canceller
	 *            Canceller used to listen to, to stop execution
	 * @param tree
	 *            NAryTree to align on
	 * @param configurationNumber
	 *            Configuration to use
	 * @param node2Cost
	 *            Costs assigned to nodes in tree
	 * @param useOrRows
	 *            Whether OR rows should be used
	 * @param multiThreaded
	 *            Specify the number of threads to run in parallel. ADVICE: on
	 *            hyperthreading cores take half number of cores. If number is
	 *            below 1, 1 is used.
	 * @param observers
	 */
	public AbstractNAryTreeReplayer(AStarAlgorithm aStarLogAbstraction, Canceller canceller, NAryTree tree,
			int configurationNumber, int[] node2Cost, boolean useOrRows, int nrThreads, AStarObserver... observers) {
		this.aStarLogAbstraction = aStarLogAbstraction;
		this.observers = observers;
		if (nrThreads > 1) {
			//Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
			threads = nrThreads;
		} else {
			threads = 1;
		}
		this.delegate = constructDelegate(aStarLogAbstraction, tree, configurationNumber, node2Cost, threads, useOrRows);

		this.canceller = canceller;
		this.tree = tree;

		if (threads > 1) {
			executor = Executors.newFixedThreadPool(threads);
		} else {
			executor = null;
		}

		/*
		 * jbuijs 23-9-2013 NOTE since we cannot assume that T has the correct
		 * type for CACHEDMEMORYEFFICIENT we assume 'normal' mem. efficiency and
		 * require any calling class to manually set the type to cached if this
		 * is possible
		 */
		setType(PerformanceType.MEMORYEFFICIENT);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pr-ocessmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #setType(org.processmining.plugins.etm.model.narytree.replayer.
	 * AbstractNAryTreeReplayer.PerformanceType)
	 */
	public void setType(PerformanceType type) {
		setType(type, 16 * 1024, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pr-ocessmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #setType(org.processmining.plugins.etm.model.narytree.replayer.
	 * AbstractNAryTreeReplayer.PerformanceType)
	 */
	public void setType(PerformanceType type, int blockSize, int alignment) {
		this.type = type;
		if (type != PerformanceType.CPUEFFICIENT) {
			//- The parameters here determine the size of the storage used. 
			// 1) The first integer is the blocksize which indicates in what chunks memory is allocated. 
			//     A reasonable size is at least 1024 * the expected size of a state
			// 2) The second integer is the initial capacity of the underlying hashtable. A reasonable size is the expected number of states
			// 3) The third integer is the alignment, i.e. every state stored in memory uses a multiple of this many bytes. Using 1 allows a
			// storage size of 4 G, while using 8 allows for 32 G.
			this.blockSize = alignment * (blockSize / alignment);
			algorithm = new MemoryEfficientAStarAlgorithm<H, T>(delegate, blockSize, blockSize, alignment);
			head2int = null;
			stateList = null;
		} else {
			algorithm = null;
			head2int = new TObjectIntHashMap<H>(100000);
			stateList = new ArrayList<State<H, T>>(1024);
		}
	}

	private AtomicLong rawCost;
	private AtomicLong penaltyCost;
	private boolean stubborn;
	private boolean caching;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #run(org.processmining.plugins.etm.model.narytree.replayer.
	 * AbstractNAryTreeReplayer.VerboseLevel, int, double)
	 */
	public int run(final VerboseLevel verbose, final int stopAt, final int minModelCost, final double timeLimit)
			throws AStarException {
		rawCost = new AtomicLong(0);
		penaltyCost = new AtomicLong(0);

		/*
		 * Calculate the replay fitness and at the same time align the log and
		 * the model
		 */
		//TESTING code for size/nr blocks testing
		/*-*/
		NAryTreeHybridILPTail.LPSolved = 0;
		NAryTreeHybridILPTail.LPDerived = 0;
		long size = 0;
		long stateSpace = 0;
		int blocks = 0;
		long queued = 0;
		long qtot = 0;
		/**/
		int i = 0;
		Iterator<Trace> it = aStarLogAbstraction.traceIterator();
		while (it.hasNext() && !canceller.isCancelled() && wasReliable.get() && rawCost.get() <= stopAt) {
			final Trace trace = it.next();
			if (threads == 1) {
				queuedStateCount = 0;
				int minCost = minModelCost;
				if (minCost < Integer.MAX_VALUE) {
					for (int e = 0; e < trace.getSize(); e++) {
						minCost += delegate.getLogMoveCost(trace.get(e));
					}
				}
				if (minCost < 0) {
					// overflow.
					minCost = Integer.MAX_VALUE;
				}

				//Clean up memory if necessary
				if (algorithm.getStore().getBlocksInUse() > maxNrOfBlocksToUse) {
					//System.out.println("Resetting Statespace: " + algorithm.getStore().getBlocksInUse() + ">"		+ maxNrOfBlocksToUse);
					algorithm.getStatespace().removeAll();
					System.gc();
				}

				execute(trace, minCost, timeLimit, verbose);

				if (verbose == VerboseLevel.SOME && (i % 50 == 0)) {
					System.out.println();
				}
				/*-*/
				if (debugMode) {
					queued = Math.max(queued, queuedStateCount);
					qtot += queuedStateCount;
					blocks = Math.max(blocks, algorithm.getStore().getBlocksInUse());
					size = Math.max(size, algorithm.getStore().getMemory());
					stateSpace = Math.max(stateSpace, algorithm.getStatespace().size());
				}
				/**/
				i++;
			} else {
				executor.submit(new Runnable() {

					public void run() {
						int minCost = minModelCost;
						for (int e = 0; e < trace.getSize(); e++) {
							minCost += delegate.getLogMoveCost(trace.get(e));
						}
						try {
							execute(trace, Math.min(stopAt, minCost), timeLimit, verbose);
						} catch (AStarException e) {
							e.printStackTrace();
						}

					}

				});
			}
		}//while for each trace

		/*-*/
		if (i > 1 && debugMode) {
			System.out.print(";Size; " + size + " ;Blocks; " + blocks + " ;Space; " + stateSpace + " ;LP; "
					+ NAryTreeHybridILPTail.LPSolved + " ;Queued max; " + queued + " ;queued total; " + qtot);
		}

		/*-* /
		System.out.println("Size:   " + size);
		System.out.println("Blocks: " + blocks);
		System.out.println("Space:  " + stateSpace);
		System.out.println("LP:     " + NAryTreeHybridILPTail.LPSolved);
		/**/
		if (threads > 1) {
			executor.shutdown();
			while (!executor.isTerminated()) {
				try {
					executor.awaitTermination(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException _) {
				}
			}
		}

		for (AStarObserver observer : observers) {
			observer.close();
		}

		if (threads > 1)
			executor.shutdown();

		if (rawCost.get() < 0) {
			System.out.println("Oops");
		}

		//In these cases we need to return high cost since we did not calculate the full alignments
		if (canceller.isCancelled() || !wasReliable.get())
			return Integer.MAX_VALUE;

		long localPenaltyCost = penaltyCost.get();
		assert localPenaltyCost % ((TreeDelegate<?, ?>) delegate).getScaling() == 0;

		return (int) (localPenaltyCost / ((TreeDelegate<?, ?>) delegate).getScaling());
	}

	/**
	 * returns the raw cost of aligning a log and a model after a call to run.
	 * If the call to run was unreliable, the raw cost is a best guess.
	 * 
	 * @return
	 */
	public long getRawCost() {
		return rawCost.get();
	}

	private void execute(Trace trace, int minCost, double timeLimit, VerboseLevel verbose) throws AStarException {
		int frequency = aStarLogAbstraction.getTraceFreq(trace);

		H initial = createInitialHead(trace);

		long start = System.nanoTime();
		TreeRecord r;
		AbstractAStarThread<H, T> thread;

		if (type == PerformanceType.MEMORYEFFICIENT) {
			if (isStubborn()) {
				thread = new StubbornNAryTreeAStarThread.MemoryEfficient<H, T>(tree, algorithm, initial, trace,
						maxNumOfStates);
			} else {
				thread = new AStarThread.MemoryEfficient<H, T>(algorithm, initial, trace, maxNumOfStates);
			}
		} else {
			if (isStubborn()) {
				thread = new StubbornNAryTreeAStarThread.CPUEfficient<H, T>(tree, getDelegate(), head2int, stateList,
						initial, trace, maxNumOfStates);
			} else {
				thread = new AStarThread.CPUEfficient<H, T>(getDelegate(), head2int, stateList, initial, trace,
						maxNumOfStates);
			}
		}

		thread.setQueueingModel(nl.tue.astar.AStarThread.QueueingModel.DEPTHFIRST);
		thread.setASynchronousMoveSorting(ASynchronousMoveSorting.MODELMOVEFIRST);

		for (AStarObserver observer : observers) {
			thread.addObserver(observer);
		}
		r = (TreeRecord) thread.getOptimalRecord(canceller, minCost, timeLimit);
		//synchronized (this) {
		wasReliable.compareAndSet(true, thread.wasReliable());
		long end = System.nanoTime();

		// The three lines below allow the user to find all states leading to an optimal alignment.
		// This is only relevant for debugging purposes, for example to print the search space.
		//		while (thread.wasReliable()) {
		//			thread.getOptimalRecord(canceller, r.getTotalCost());
		//		}

		if (wasReliable.get() && (r != null)) {
			penaltyCost.addAndGet(calculateCostAndPostProcess(verbose, trace, r, frequency));
			rawCost.addAndGet((long) r.getTotalCost());
		} else {
			// hmm, no fitness found, make expensive as this model is not even close to a fitting candidate
			penaltyCost.addAndGet(frequency * (trace.getSize() + tree.numLeafs()));
			rawCost.addAndGet(frequency * (trace.getSize() + tree.numLeafs()));
			//TEMP: do it again for debug purposes
			//thread.getOptimalRecord(canceller, minCost, timeLimit);
		}

		if (verbose != VerboseLevel.NONE) {
			if (r != null) {
				switch (verbose) {
					case ALL :
						System.out.println("Time: " + ((end - start) / 1000.0 / 1000.0 / 1000.0) + " seconds");
						TreeRecord.printRecord(getDelegate(), trace, r);
						System.out.println("States visited: " + thread.getQueuedStateCount());
						if (algorithm == null) {
							System.out.println("Unique states : " + stateList.size());
						} else {
							System.out.println("Unique states : " + algorithm.getStatespace().size());
						}
						break;
					case SOME :
						System.out.print('.');
						//$FALL-THROUGH$
					default :
				}
			} else {
				//System.err.println(root);
				System.out.print("X");
			}
		}
		queuedStateCount += thread.getQueuedStateCount();
		traversedArcCount += thread.getTraversedArcCount();
	}

	//}

	/**
	 * do post-processing on the given alignment for the given trace. The
	 * TreeRecord r is the record storing the alignment for the given trace
	 * which is contained in the log frequency times.
	 * 
	 * This method returns the costs to be added to the total cost for the
	 * replay procedure, where only the penalties should be counted.
	 * 
	 * @param verbose
	 * @param trace
	 * @param r
	 * @param frequency
	 * @return
	 */
	protected abstract int calculateCostAndPostProcess(VerboseLevel verbose, Trace trace, TreeRecord r, int frequency);

	/**
	 * create the initial head for this replayer. Usually, this task is deferred
	 * to the delegate.
	 * 
	 * @param trace
	 * @return
	 */
	protected abstract H createInitialHead(Trace trace);

	/**
	 * construct the delegate of the right type for the version of the replayer
	 * to be used.
	 * 
	 * @param algorithm
	 * @param tree
	 * @param node2Cost
	 * @param threads
	 * @return
	 */
	protected abstract D constructDelegate(AStarAlgorithm algorithm, NAryTree tree, int configurationNumber,
			int[] node2Cost, int threads, boolean useOrRows);

	public static List<TreeRecord> getHistory(TreeRecord r) {
		if (r == null) {
			return Collections.emptyList();
		}
		List<TreeRecord> history = new ArrayList<TreeRecord>(r.getModelMoveCount());
		while (r.getPredecessor() != null) {
			history.add(0, r);
			r = r.getPredecessor();
		}
		return history;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #getState(long)
	 */
	public State<H, T> getState(long index) {
		if (type != PerformanceType.CPUEFFICIENT) {
			try {
				return algorithm.getStatespace().getObject(index);
			} catch (StorageException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return stateList.get((int) index);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #getQueuedStateCount()
	 */
	public long getQueuedStateCount() {
		return queuedStateCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #getTraversedArcCount()
	 */
	public long getTraversedArcCount() {
		return traversedArcCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #getDelegate()
	 */
	public D getDelegate() {
		return delegate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #getNumStoredStates()
	 */
	public long getNumStoredStates() {
		if (type != PerformanceType.CPUEFFICIENT) {
			return this.algorithm.getStatespace().size();
		} else {
			return this.stateList.size();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.replayer.NAryTreeReplayer
	 * #wasReliable()
	 */
	public boolean wasReliable() {
		return wasReliable.get();
	}

	/**
	 * Tell the algorithm to clear the memory (in between trace alignments) if
	 * it is close to using the specified amount of megabytes of memory. When
	 * calling this method make sure you take the number of threads into account
	 * and that you leave some 'general' memory space available. This will not
	 * prevent {@link OutOfMemoryError}s at all times but it will try to, at the
	 * cost of performance for the larger trace/model combinations since it
	 * empties the cache.
	 * 
	 * @param maxMemUsage
	 *            maximum memory usage in bytes (negative value indicates no
	 *            limit)
	 * @return
	 */
	public long setMaxNumberOfBlocksToUse(long maxBytestoUse) {
		if (maxBytestoUse <= 0) {
			//Negative value indicates no limit
			maxNrOfBlocksToUse = Integer.MAX_VALUE;
		} else {
			//better safe then sorry, round down in the division, and reduce one block, but always have at least 1 block
			maxNrOfBlocksToUse = Math.max((maxBytestoUse) / blockSize, 1);
		}

		return maxNrOfBlocksToUse;
	}

	public boolean isStubborn() {
		return stubborn;
	}

	public void setStubborn(boolean stubborn) {
		this.stubborn = stubborn;
	}

}
