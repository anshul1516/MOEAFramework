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
package org.moeaframework.problem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.util.io.RedirectStream;

/**
 * Evaluate solutions using an externally defined problem that conforms to the
 * following protocol. The process specified by the constructor is launched and
 * waits to receive a line of input. This input line will consist of one or more
 * variables separated by whitespace and terminated by a newline. The process
 * evaluates the problem for the given variables and outputs the objectives
 * separated by whitespace and terminated by a newline. If the problem also has
 * constraints, each constraint is returned after the objectives on the same
 * line. No other output should be generated by the process. The process must 
 * only terminate when the end of stream is reached. In addition, the process 
 * should flush the output stream to ensure the output is processed immediately.
 * <p>
 * Whitespace is one or more spaces, tabs or any combination thereof. The
 * newline is either the line feed ('\n'), carriage return ('\r') or a carriage
 * return followed immediately by a line feed ("\r\n").
 * <p>
 * <b>It is critical that the {@link #close()} method be invoked to ensure the
 * external process is shutdown cleanly.</b>
 */
public abstract class ExternalProblem implements Problem {

	/**
	 * Reader connected to the process' standard output.
	 */
	private final BufferedReader reader;

	/**
	 * Writer connected to the process' standard input.
	 */
	private final BufferedWriter writer;

	/**
	 * Constructs an external problem using {@code new
	 * ProcessBuilder(command).start()}.  If the command contains arguments,
	 * the arguments should be passed in as separate strings, such as
	 * <pre>
	 *   new ExternalProblem("command", "arg1", "arg2");
	 * </pre>
	 * 
	 * @param command a specified system command
	 * @throws IOException if an I/O error occurs
	 */
	public ExternalProblem(String... command) throws IOException {
		this(new ProcessBuilder(command).start());
	}

	/**
	 * Constructs an external problem using the specified process.
	 * 
	 * @param process the process used to evaluate solutions
	 */
	ExternalProblem(Process process) {
		this(process.getInputStream(), process.getOutputStream());
		RedirectStream.redirect(process.getErrorStream(), System.err);
	}
	
	/**
	 * Constructs an external problem using the specified input and output 
	 * streams.
	 * 
	 * @param input the input stream
	 * @param output the output stream
	 */
	ExternalProblem(InputStream input, OutputStream output) {
		super();
		reader = new BufferedReader(new InputStreamReader(input));
		writer = new BufferedWriter(new OutputStreamWriter(output));
	}

	/**
	 * Closes the connection to the process. No further invocations of
	 * {@code evaluate} are permitted.
	 */
	@Override
	public synchronized void close() {
		try {
			writer.close();
			reader.close();
		} catch (IOException e) {
			throw new ProblemException(this, e);
		}
	}

	/**
	 * Evaluates the specified solution using the process defined by this class'
	 * constructor.
	 */
	@Override
	public synchronized void evaluate(Solution solution) 
	throws ProblemException {
		// send variables to external process
		try {
			writer.write(toString(solution.getVariable(0)));
			for (int i = 1; i < solution.getNumberOfVariables(); i++) {
				writer.write(" ");
				writer.write(toString(solution.getVariable(i)));
			}
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			throw new ProblemException(this, "error sending variables to external process", e);
		}

		// receive objectives from external process
		try {
			String line = reader.readLine();

			if (line == null) {
				throw new ProblemException(this, "end of stream reached when response expected");
			}

			String[] tokens = line.split("\\s+");

			if (tokens.length != (solution.getNumberOfObjectives() + 
					solution.getNumberOfConstraints())) {
				throw new ProblemException(this, "response contained fewer tokens than expected");
			}
			
			int index = 0;

			for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
				solution.setObjective(i, Double.parseDouble(tokens[index]));
				index++;
			}
			
			for (int i = 0; i < solution.getNumberOfConstraints(); i++) {
				solution.setConstraint(i, Double.parseDouble(tokens[index]));
				index++;
			}
		} catch (IOException e) {
			throw new ProblemException(this, "error receiving variables from external process", e);
		} catch (NumberFormatException e) {
			throw new ProblemException(this, "error receiving variables from external process", e);
		}
	}

	/**
	 * Serializes a variable to a string form.
	 * 
	 * @param variable the variable whose value is serialized
	 * @return the serialized version of the variable
	 * @throws IOException if an error occurs during serialization
	 */
	private String toString(Variable variable) throws IOException {
		if (variable instanceof RealVariable) {
			return Double.toString(((RealVariable)variable).getValue());
		} else {
			throw new IOException("unable to serialize variable");
		}
	}

}
