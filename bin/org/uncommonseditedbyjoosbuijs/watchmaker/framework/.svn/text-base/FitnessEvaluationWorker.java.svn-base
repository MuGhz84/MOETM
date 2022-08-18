// =============================================================================
// Copyright 2006-2010 Daniel W. Dyer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// =============================================================================
package org.uncommonseditedbyjoosbuijs.watchmaker.framework;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.uncommonseditedbyjoosbuijs.util.concurrent.ConfigurableThreadFactory;
import org.uncommonseditedbyjoosbuijs.util.id.IDSource;
import org.uncommonseditedbyjoosbuijs.util.id.IntSequenceIDSource;
import org.uncommonseditedbyjoosbuijs.util.id.StringPrefixIDSource;

/**
 * This is the class that actually runs the fitness evaluation tasks created by
 * a {@link EvolutionEngine}. This responsibility is abstracted away from the
 * evolution engine to permit the possibility of creating multiple instances
 * across several machines, all fed by a single shared work queue, using
 * Terracotta (http://www.terracotta.org) or similar.
 * 
 * @author Daniel Dyer
 */
public class FitnessEvaluationWorker {
	// Provide each worker instance with a unique name with which to prefix its threads.
	private static final IDSource<String> WORKER_ID_SOURCE = new StringPrefixIDSource("FitnessEvaluationWorker",
			new IntSequenceIDSource());

	/**
	 * Share this field to use Terracotta to distribute fitness evaluations.
	 */
	private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

	/**
	 * Thread pool that performs concurrent fitness evaluations.
	 */
	private final ThreadPoolExecutor executor;

	/**
	 * Creates a FitnessEvaluationWorker that uses daemon threads.
	 */
	FitnessEvaluationWorker() {
		this(true);
	}

	/**
	 * @param daemonWorkerThreads
	 *            If true, any worker threads created will be daemon threads.
	 */
	private FitnessEvaluationWorker(boolean daemonWorkerThreads) {
		ConfigurableThreadFactory threadFactory = new ConfigurableThreadFactory(WORKER_ID_SOURCE.nextID(),
				Thread.NORM_PRIORITY, daemonWorkerThreads);

		int poolSize = Math.max(Runtime.getRuntime().availableProcessors() / 2, 1);
		//		this.executor = new ThreadPoolExecutor(poolSize,//
		//		poolSize, 60, TimeUnit.SECONDS,//
		//		workQueue, threadFactory);

		// the size of the threadpool used here is at minimal poolsize, but at most 32 times poolSize. 
		// This allows any fitness computation to block without immediately blocking a full core.
		this.executor = new ThreadPoolExecutor(poolSize,//
				32 * poolSize, 10, TimeUnit.SECONDS,//
				workQueue, threadFactory);
		executor.prestartAllCoreThreads();
		executor.allowCoreThreadTimeOut(false);
	}

	public void setThreadCount(int threads) {
		executor.setCorePoolSize(threads);
		executor.setMaximumPoolSize(threads);
	}

	public <T extends Comparable<? super T>> Future<EvaluatedCandidate<T>> submit(FitnessEvalutationTask<T> task) {
		return executor.submit(task);
	}

	/**
	 * Entry-point for running this class standalone, as an additional node for
	 * fitness evaluations. If this method is invoked without using Terracotta
	 * (or similar) to share the work queue, the program will do nothing.
	 * 
	 * @param args
	 *            Program arguments, should be empty.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// The program will not exit immediately upon completion of the main method because
		// the worker is configured to use non-daemon threads that keep the JVM alive.
		new FitnessEvaluationWorker(false);
	}

	/**
	 * A FitnessWorker cannot be garbage-collected if its thread pool has not
	 * been shutdown. This method, invoked on garabage collection (or maybe not
	 * at all), shuts down the thread pool so that the threads can be released.
	 * 
	 * @throws Throwable
	 *             Any exception or error that occurs during finalisation.
	 */
	@Override
	protected void finalize() throws Throwable {
		executor.shutdown();
		super.finalize();
	}
}
