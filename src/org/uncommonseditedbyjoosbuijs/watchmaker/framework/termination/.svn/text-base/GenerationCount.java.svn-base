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
package org.uncommonseditedbyjoosbuijs.watchmaker.framework.termination;

import org.uncommonseditedbyjoosbuijs.watchmaker.framework.PopulationData;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.TerminationCondition;

/**
 * Terminates evolution after a set number of generations have passed.
 * 
 * @version UPDATED by jbuijs: added getGenerationCount function
 * 
 * @author Daniel Dyer
 * @author jbuijs
 */
public class GenerationCount implements TerminationCondition {
	private final int generationCount;

	/**
	 * @param generationCount
	 *            The maximum number of generations that the evolutionary
	 *            algorithm will permit before terminating.
	 */
	public GenerationCount(int generationCount) {
		if (generationCount <= 0) {
			throw new IllegalArgumentException("Generation count must be positive.");
		}
		this.generationCount = generationCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean shouldTerminate(PopulationData<?> populationData) {
		return populationData.getGenerationNumber() + 1 >= generationCount;
	}

	public int getGenerationCount() {
		return generationCount;
	}

	public String toString() {
		return "Termination by max. generation count (" + generationCount + ")";
	}
}
