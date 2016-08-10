package com.flatironschool.javacs;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.List;
import redis.clients.jedis.Jedis;

public class QueryHandler{
	private static final int WORD_STATE = 0;
	private static final int CP_STATE = 1;
	private JedisIndex _jedisIndex;
	private String _query;

	private List<Entry<String, Double>> _results;

	// Handles queries entered in CNF.
	// Example: java QueryHandler '( dog AND cat ) OR ( zoo ) "
	public QueryHandler(String[] args) throws Exception{
		if (args.length != 1){
			throw new Exception("Invalid query length. Please enter 1 query in CNF form.");
		}

		// Makes jedis index.
		Jedis jedis = JedisMaker.make();
		_jedisIndex = new JedisIndex(jedis);

		// Parses query and stores result.
		String query = args[0];
		ArrayList<ArrayList<String>> tokens = parse(query);
		WikiSearch searchResult = orLists(tokens);
		_results = searchResult.sort();

	}

	// NOTE: Currently does not support multi-word queries. 

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

		// Else, make sure the query has the correct format and enter DFA.
		if (!tokens[0].equals("(")) throw new Exception("Invalid token format.");

		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

		int i = 1;
		ArrayList<String> curList = new ArrayList<String>();
		int curState = WORD_STATE; // Initial state.

		// OK since and/or are stopwords.
		while (i < tokens.length){
			String curToken = tokens[i];

			switch (curState){
				case CP_STATE:
					results.add(curList);

					i = i + 1;
					if (i == tokens.length) break;

					curList = new ArrayList<String>();

					if (peek(tokens, i).toUpperCase().equals("OR") &&
							peek(tokens, i + 1).equals("(")){
						i = i + 2;
						curState = WORD_STATE;
					} else {
						throw new Exception("Invalid token format.");
					}

					break;

				case WORD_STATE:
					curList.add(curToken);

					// Peek.
					String peekToken = peek(tokens, i + 1);
					if (peekToken.toUpperCase().equals("AND")){
						i = i + 2;
						curState = WORD_STATE;
					} else if (peekToken.equals(")")){
						i = i + 1;
						curState = CP_STATE;
					} else {
						throw new Exception("Invalid token format.");
					}
					break;
			}
		}

		return results;
	}

	private String peek(String[] tokens, int i){
		if (i < 0 || i >= tokens.length) return "";
		return tokens[i];

	}

	private void printLists(ArrayList<ArrayList<String>> queries){
		for (int i = 0; i < queries.size(); i++){
			System.out.println("List " + i + ":");

			ArrayList<String> curList = queries.get(i);

			printList(curList);

		}
	}
	private void printList(ArrayList<String> queries){
		for (int i = 0; i < queries.size(); i++){
			System.out.println(queries.get(i));
		}
	}

	// Performs the search.
	private WikiSearch orLists(ArrayList<ArrayList<String>> queries) throws Exception{
		if (queries.size() <= 0){
			throw new Exception("No valid query detected.");
		}

		// Search the initial list.
		ArrayList<String> curList = queries.get(0);
		WikiSearch result = andList(curList);
		result.print();

		for (int i = 1; i < queries.size(); i++){
			ArrayList<String> curQuery = queries.get(i);
			WikiSearch curResult = andList(curQuery);
			result = result.or(curResult);
		}

		return result;
	}

	private WikiSearch andList(ArrayList<String> queries){
		if (queries.size() <= 0){
			return null;
		}

		String curQuery = queries.get(0);
		WikiSearch result = WikiSearch.search(curQuery, _jedisIndex);
		for (int i = 1; i < queries.size(); i++){
			curQuery = queries.get(i);
			WikiSearch curResult = WikiSearch.search(curQuery, _jedisIndex);
			result = result.and(curResult);
			curResult.print();
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
	public static void main(String[] args) throws Exception{
		// Handles query, prints results, and opens quick access goto.

		QueryHandler queryHandler = new QueryHandler(args);
		queryHandler.printResults();
	}
}
