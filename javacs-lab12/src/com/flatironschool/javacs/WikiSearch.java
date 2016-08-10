package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Double> map;

	private String original_term;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */

	public WikiSearch(Map<String, Double> map, String term) {
		this.map = map;
		original_term = term;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Double getRelevance(String url) {
		Double relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	public void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			Entry<String, Double> entry = entries.get(i);

			// just print out name of wikipedia page, ignore URL prefix
			String url = entry.getKey();
			String wikiTitle = url.substring(30,url.length());
			String relevance = String.format("%.3f", entry.getValue());
			System.out.println(i + ". " + wikiTitle + ": " + relevance);
		}
	}

	/**
	 * Retrieves the original term for the WikiSearch.
	 * 
	 * @return term.
	 */
	public String getTerm() {
		return original_term;
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
		Map<String, Double> union = new HashMap<String, Double>(map);
		for (String term: that.map.keySet()) {
			double relevance = totalRelevance(this.getRelevance(term), that.getRelevance(term));
			union.put(term, relevance);
		}
		return new WikiSearch(union, original_term + " or " + that.getTerm());
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
		Map<String, Double> intersection = new HashMap<String, Double>();
		System.out.println(that.map.keySet().toString());
		System.out.println(map.keySet().toString());
		for (String term: map.keySet()) {
			if (that.map.containsKey(term)) {

				double relevance;

				if (original_term.indexOf(that.getTerm()) >= 0) { // term1 contains term2
					relevance = this.map.get(term);
				} else if (that.getTerm().indexOf(original_term) >= 0) { // term2 contains term1
					relevance = that.map.get(term);
				} else {
					relevance = totalRelevance(this.map.get(term), that.map.get(term));
				}
				intersection.put(term, relevance);
			}
		}
		return new WikiSearch(intersection, original_term + " and " + that.getTerm());
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
		Map<String, Double> difference = new HashMap<String, Double>(map);
		for (String term: that.map.keySet()) {
			difference.remove(term);
		}
		return new WikiSearch(difference, original_term + " minus " + that.getTerm());
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected double totalRelevance(Double rel1, Double rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Double>> sort() {
		// NOTE: this can be done more concisely in Java 8.  See
		// http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java

		// make a list of entries
		List<Entry<String, Double>> entries = 
				new LinkedList<Entry<String, Double>>(map.entrySet());
		// make a Comparator object for sorting
		Comparator<Entry<String, Double>> comparator = new Comparator<Entry<String, Double>>() {
            @Override
            public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        };
        
        // sort and return the entries
		Collections.sort(entries, comparator);
		return entries;
	}


	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */

	public static WikiSearch search(String term, JedisIndex index) {
        Map<String, Double> map = index.getTfIdf(term);
        return new WikiSearch(map, term);
    }

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "java";
		System.out.println("Results for query: " + term1);

		Map<String, Double> map1 = index.getTfIdf(term1);
		WikiSearch search1 = new WikiSearch(map1, term1);
		search1.print();
		System.out.println();
		
		// search for the second term
		String term2 = "programming";
		System.out.println("Results for query: " + term2);
		Map<String, Double> map2 = index.getTfIdf(term2);
		WikiSearch search2 = new WikiSearch(map2, term2);
		search2.print();
		System.out.println();

		// search for the third term
		String term3 = "language";
		System.out.println("Results for query: " + term3);
		Map<String, Double> map3 = index.getTfIdf(term3);
		WikiSearch search3 = new WikiSearch(map3, term3);
		search3.print();
		System.out.println();

		// compute the intersection of the searches
		System.out.println("Results for query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
		System.out.println();
	}
}
