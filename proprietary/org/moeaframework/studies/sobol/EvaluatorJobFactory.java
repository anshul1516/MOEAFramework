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
package org.moeaframework.studies.sobol;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.moeaframework.util.CommandLineUtility;
import org.moeaframework.util.pbs.PBSJob;
import org.moeaframework.util.pbs.PBSSubmitter;

/**
 * Command line utility for launching a number of Evaluator instances for the 
 * specified algorithms, problems and seeds.  Refer to the {@code -h} command 
 * line option for usage.
 */
public class EvaluatorJobFactory extends CommandLineUtility {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private EvaluatorJobFactory() {
		super();
	}

	/**
	 * Command line utility for launching a number of Evaluator instances
	 * for the specified algorithms, problems and seeds.
	 * 
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		new EvaluatorJobFactory().start(args);
	}

	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options options = super.getOptions();

		options.addOption(OptionBuilder.hasArgs().withArgName("value")
				.withDescription("seed(s) to run").create("seed"));
		
		options.addOption(OptionBuilder.hasArgs().withArgName("value")
				.withDescription("algorithm(s) to run").create("algorithm"));
		
		options.addOption(OptionBuilder.hasArgs().withArgName("value")
				.withDescription("problem(s) to run").create("problem"));
		
		return options;
	}

	@Override
	public void run(CommandLine commandLine) throws Exception {
		String[] seeds = new String[] { "0" };
		
		String[] algorithms = new String[] { "BorgA", "BorgB", "BorgC", "BorgD",
				"BorgE", "BorgF", "BorgG", "BorgH", "IBEA", "eNSGAII" };
		
		String[] problems = new String[] { "UF1", "UF2", "UF3", "UF4", "UF5",
				"UF6", "UF7", "UF8", "UF9", "UF10", "UF11", "UF12", "UF13",
				"DTLZ1_2", "DTLZ2_2", "DTLZ3_2", "DTLZ4_2", "DTLZ7_2",
				"DTLZ1_4", "DTLZ2_4", "DTLZ3_4", "DTLZ4_4", "DTLZ7_4",
				"DTLZ1_6", "DTLZ2_6", "DTLZ3_6", "DTLZ4_6", "DTLZ7_6",
				"DTLZ1_8", "DTLZ2_8", "DTLZ3_8", "DTLZ4_8", "DTLZ7_8" };
		
		if (commandLine.hasOption("seed")) {
			seeds = commandLine.getOptionValues("seed");
		}
		
		if (commandLine.hasOption("algorithm")) {
			algorithms = commandLine.getOptionValues("algorithm");
		}
		
		if (commandLine.hasOption("problem")) {
			problems = commandLine.getOptionValues("problem");
		}

		for (String problem : problems) {
			for (String algorithm : algorithms) {
				for (String seed : seeds) {
					String command = "java -Xmx1g -server org.moeaframework.analysis.sensitivity.Evaluator"
							+ " --parameterFile ./params/"
							+ algorithm
							+ "_Params --input ./params/"
							+ algorithm
							+ "_LHS --problem "
							+ problem
							+ " --algorithm "
							+ algorithm + " --seed " + seed;

					PBSJob job = new PBSJob(algorithm + "_" + problem + "_" + seed, 96, 1, command);
				
					PBSSubmitter.submit(job);
				}
			}
		}
	}

}
