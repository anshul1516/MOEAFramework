/* Copyright 2009-2011 David Hadka
 * 
 * This file is part of the MOEA Framework.
 * 
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * The MOEA Framework is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moeaframework.core.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.moeaframework.core.Solution;

/**
 * Compares solutions lexicographically.
 */
public class LexicographicalComparator implements Comparator<Solution>,
Serializable {

	private static final long serialVersionUID = 2303639747960671103L;

	/**
	 * Constructs a comparator for comparing solutions lexicographically.
	 */
	public LexicographicalComparator() {
		super();
	}

	@Override
	public int compare(Solution a, Solution b) {
		for (int i = 0; i < a.getNumberOfObjectives(); i++) {
			if (a.getObjective(i) < b.getObjective(i)) {
				return -1;
			} else if (a.getObjective(i) > b.getObjective(i)) {
				return 1;
			}
		}

		return 0;
	}

}
