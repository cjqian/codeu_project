package com.flatironschool.javacs;

import java.awt.Desktop;
import java.net.URI;
import java.util.Scanner;

public class QueryHandler{
	private String _query;
	private boolean _extraOn;
	private boolean _sortAlpha;
	private List<String> _results;

	public class QueryHandler(String[] args){
		if (args.length <= 0){
			throw new Exception("Invalid query.");
		}

		_query = args[0];

		for (int i = 1; i < args.length(); i++){
			if (args[i].equals("extraOn")){
				_extraOn = true;
			}

			if (args[i].equals("sortAlpha")){
				_sortAlpha = true;
			}
		}

		_results = Search();
	}

	/* Usage:
	 * java QueryHandler caffeine --extraSearch --sortAlpha
	 */
	public static void main(String[] args){
		QueryHandler queryHandler = new QueryHandler(args);
		queryHandler.DisplayResults();
	}

	// Returns a list of relevant URLs for a given term (sorted by relevance).
	private List<String> Search(){
		// If extra search is on, query with extras.
		if (_extraOn){
			System.out.println("Extra search is on!");
		} else {
			System.out.println("Extra search is off!");
		}

		// Sort the queries, if necessary.
		if (_sortAlpha){
			java.util.Collections.sort(_results);
		}
	}

	// Prints out a list, numerically. 
	private void DisplayResults(){
		// Print out all the results.
		for (int i = 0; i < _results.length; i++){
			System.out.println("[" + i + "]\t\t" + results.Get(i));
		}

		// Prompt.
		System.out.println("Press [i] to open the [i]th entry. Or, press ctrl-C to exit.");

		// Get keypress. This runs in an infinite loop.
		while (true){
			if (Integer.parseInt(System.console().readLine()) < results.Length()){
				Desktop.getDesktop().browse(new URI(results.get(i)));
			}		
		}
	}
}