package com.flatironschool.javacs;

import java.awt.Desktop;
import java.net.URI;
import java.util.Scanner;

import redis.clients.jedis.Jedis;

public class QueryHandler{
	private	int OP_STATE = 0; // open parenthesis state
	private	int CP_STATE = 1; // closed parenthesis state
	private	int AND_STATE = 2;	// and state
	private	int OR_STATE = 3;	// or state
	private	int WORD_STATE = 4; // in state

	private JedisIndex _jedisIndex;
	private String _query;

	private List<Entry<String, Double>> _results;

	// Handles queries entered in CNF.
	// Example: java QueryHandler '( dog AND cat ) OR ( zoo ) "
	public QueryHandler(String[] args){
		if (args.length != 1){
			throw new Exception("Invalid query length. Please enter 1 query in CNF form.");
		}

		// Makes jedis index.
		Jedis jedis = JedisMaker.make();
		_jedisIndex = new JedisIndex(jedis);

		// Parses query and stores result.
		String query = args[0];
		List<List<String>> tokens = parseQuery(query);
		WikiSearch searchResult = orLists(tokens);
		_results = searchResult.sort();

	}

	// NOTE: Currently does not support multi-word queries. 

	// Parses the query and returns a list of list of strings, where each list of strings 
	// is a list of AND clauses, and each instance in the larger list is to be OR'd with the remainder.
	private List<List<String>> parse(String query){
		String[] tokens = query.split("\\s+");

		// Special case: only one query.
		if (tokens.Length == 1){
			List<List<String>> result = new List<List<String>>();
			List<String> oneResult = new List<String>();
			oneResult.add(tokens[0]);
			result.add(oneResult);

			return result;
		}
	
		// Else, make sure the query has the correct format and enter DFA.
		if (tokens[0] != "(") throw new Exception("Invalid token format.");

		List<List<String>> result = new List<List<String>>();

		int i = 0;
		List<String> curList = new List<String>();
		int curState = WORD_STATE; // Initial state.

		// OK since and/or are stopwords.
		while (i < tokens.Length()){
			String curToken = tokens[i];

			switch (curState){
				case CP_STATE:
					results.Add(curList);
					curList = new List<String>();

					if (peek(tokens, i).equals("OR") &&
						peek(tokens, i + 1).equals("(")){
						curState = WORD_STATE;
					} else if (i != tokens.Length - 1) {
						throw new Exception("Invalid token format.");
					}

					break;
				case WORD_STATE:
					curList.Add(curToken);

					// Peek.
					String peekToken = peek(tokens, i);
					if (peekToken.equals("AND")){
						curState = WORD_STATE;
					} else if (peekToken.equals(")")){
						curState = CP_STATE;
					} else {
						throw new Exception("Invalid token format.");
					}
					break;

				default:
					throw new Exception("Unhandled error. Shit.");
					break;
			}

			i++;
		}

	}

	private String peek(String[] tokens, int i){
		if (i < 0 || i >= tokens.Length() - 1) return "";
		return tokens[i];

	}

	// Performs the search.
	private WikiSearch orLists(List<List<String>> queries){
		if (queries.length <= 0){
			throw new Exception("No valid query detected.");
		}

		// Search the initial list.
		List<String> curList = queries.Get(0);
		WikiSearch result = andList(curList);

		for (int i = 1; i < queries.Size(); i++){
			curQuery = queries.Get(i);
			WikiSearch curResult = andList(curQuery);
			result = result.or(curResult);
		}

		return result;
	}

	private WikiSearch andList(List<String> queries){
		if (queries.Length <= 0){
			return null;
		}

		String curQuery = queries.Get(0);
		WikiSearch result = WikiSearch.search(curQuery, _jedisIndex);
		for (int i = 1; i < queries.Size(); i++){
			curQuery = queries.Get(i);
			WikiSearch curResult = WikiSearch.search(curQuery, _jedisIndex);
			result = result.and(curResult);
		}

		return result;
	}

	public void printResults(){
		for (Entry<String, Double> entry: _results) {
			System.out.println(entry);
		}
	}

	/* Usage:
	 */
	public static void main(String[] args){
		// Handles query, prints results, and opens quick access goto.
		QueryHandler queryHandler = new QueryHandler(args);
		queryHandler.printResults();
	}
	
	/*
	// Prints out a list, numerically. 
	private void DisplayResults(){
		_results.print();

		// Prompt.
		System.out.println("Press [i] to open the [i]th entry. Or, press ctrl-C to exit.");

		// Get keypress. This runs in an infinite loop.
		while (true){
			if (Integer.parseInt(System.console().readLine()) < results.Length()){
				Desktop.getDesktop().browse(new URI(results.get(i)));
			}		
		}
	}*/
}
