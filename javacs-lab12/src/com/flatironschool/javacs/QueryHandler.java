/*
 * Query handler takes in a single string query and returns WIkipedia searches for that query.
 * Full usage examples can be found here: https://github.com/cjqian/codeu_project/blob/master/README.md.
 */

package com.flatironschool.javacs;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import redis.clients.jedis.Jedis;

public class QueryHandler{
	private JedisIndex _jedisIndex;
	private String _query;
	private List<Entry<String, Double>> _result;
	private Scanner _scanner;

	public QueryHandler(String[] args) throws Exception{
		if (args.length != 1){
			throw new Exception("Invalid query length. Please enter 1 query in CNF form.");
		}

		// Instantiates scanner.
		_scanner = new Scanner(System.in);

		// Makes jedis index.
		Jedis jedis = JedisMaker.make();
		_jedisIndex = new JedisIndex(jedis);

		// Parses query and stores result.
		String query = args[0];
		ArrayList<ArrayList<String>> tokens = parse(query);
		WikiSearch searchResult = orLists(tokens);

		// Prints the result menu and stores result.
		searchResult.print();
		_result = searchResult.sort();
	}


	// Parses the query and returns a list of list of strings, where each list of strings 
	// is a list of AND clauses, and each instance in the larger list is to be OR'd with the remainder.
	private ArrayList<ArrayList<String>> parse(String query) throws Exception{
		String[] tokens = query.split("\\s+");

		// Special case: only one query.
		if (tokens.length == 1){
			ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
			ArrayList<String> oneResult = new ArrayList<String>();
			oneResult.add(tokens[0]);
			result.add(oneResult);

			return result;
		}

		// Else, we check tokens for validity, set up and enter DFA.
		if (!tokens[0].equals("(")) throw new Exception("Invalid token format.");

		ArrayList<String> curList = new ArrayList<String>();
		int i = 1;
		boolean inWordState = true;
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

		while (i < tokens.length){
			String curToken = tokens[i];

			if (inWordState){
				// When in a word, we either will go on to an AND, and continue the current list, or
				// go to ), which terminates the current list.
				curList.add(curToken);

				String peekToken = peek(tokens, i + 1);
				if (peekToken.toUpperCase().equals("AND")){
					i = i + 2;
					inWordState = true;
				} else if (peekToken.equals(")")){
					i = i + 1;
					inWordState = false;
				} else {
					throw new Exception("Invalid token format.");
				}
			} else {
				// When not in a word, we will either finish the query or go on to tokens 
				// OR (, which will create a new list.

				// Adds the last finished conjunction.
				result.add(curList);
				curList = new ArrayList<String>();

				if (i == tokens.length - 1) break;


				if (peek(tokens, i + 1).toUpperCase().equals("OR") &&
						peek(tokens, i + 2).equals("(")){
					i = i + 3;
					inWordState = true;
				} else {
					throw new Exception("Invalid token format.");
				}
			}
		}

		return result;
	}

	// Takes a look at the given index in the tokens, and returns an empty string if
	// i is out of bounds.
	private String peek(String[] tokens, int i){
		if (i < 0 || i >= tokens.length) return "";
		return tokens[i];

	}

	// Each ArrayList<String> in queries will create a list of WikiSearchs.
	// We combine these WikiSearch instances with an OR statement and return.
	private WikiSearch orLists(ArrayList<ArrayList<String>> queries) throws Exception{
		if (queries.size() <= 0){
			throw new Exception("No valid query detected.");
		}

		// Search the initial list.
		ArrayList<String> curList = queries.get(0);
		WikiSearch result = andList(curList);

		// Adds the remaining lists.
		for (int i = 1; i < queries.size(); i++){
			ArrayList<String> curQuery = queries.get(i);
			WikiSearch curResult = andList(curQuery);
			result = result.or(curResult);
		}

		return result;
	}

	// Each string in the list of queries will be searched. 
	// The result of these searches will have the AND operator performed to join.
	private WikiSearch andList(ArrayList<String> queries){
		// This should never happen because of how we separate tokens; 
		// still, this logic is more safe and shouldn't affect performance/error checking.
		if (queries.size() <= 0){
			return null;
		}

		// Get the initial string's search results.
		String curQuery = queries.get(0);
		WikiSearch result = search(curQuery);

		// ANDs the remaining queries. 
		for (int i = 1; i < queries.size(); i++){
			curQuery = queries.get(i);
			WikiSearch curResult = search(curQuery);

			result = result.and(curResult);
		}

		return result;
	}

	// Creates a new search instance from a term.
	private WikiSearch search(String term){
		Map<String, Double> map = _jedisIndex.getTfIdf(term);
		WikiSearch result = new WikiSearch(map, term);

		return result;
	}

	// Takes in integer inputs from standard input and launches the nth search result in our list.
	public void runLauncher() throws Exception{
		if (_result.size() == 0){
			System.out.println("No results found. Goodbye!");
			return;
		}

		while (true){
			System.out.println("Enter the number of the page you'd like to access, or press [Ctrl]/[Cmd]-C to quit.");
			
			// If the input is in bounds, we'll launch the valid query. 
			// Else, nothing will happen. 
			int value = _scanner.nextInt();
			if (value >= 0 && value < _result.size()){
				String url = _result.get(value).getKey();
				Desktop.getDesktop().browse(new URI(url));
			}
		}
	}

	public static void main(String[] args) throws Exception{
		QueryHandler queryHandler = new QueryHandler(args);
		queryHandler.runLauncher();
	}
}
