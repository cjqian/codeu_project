/**
 * 
 */
package com.flatironschool.javacs;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

/**
 * @author downey
 *
 */
public class WikiSearchTest {

	private WikiSearch search1;
	private WikiSearch search2;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Map<String, Double> map1 = new HashMap<String, Double>();
		map1.put("Page1", 1.0);
		map1.put("Page2", 2.0);
		map1.put("Page3", 3.0);
		search1 = new WikiSearch(map1, "search1");
		
		Map<String, Double> map2 = new HashMap<String, Double>();
		map2.put("Page2", 4.0);
		map2.put("Page3", 5.0);
		map2.put("Page4", 7.0);
		search2 = new WikiSearch(map2, "search2");

	}

	/**
	 * Test method for {@link com.flatironschool.javacs.WikiSearch#or(com.flatironschool.javacs.WikiSearch)}.
	 */
	@Test
	public void testOr() {
		WikiSearch search = search1.or(search2);
		assertThat(search.getRelevance("Page1"), is(1.0));
		assertThat(search.getRelevance("Page2"), is(6.0));
		assertThat(search.getRelevance("Page3"), is(8.0));
		assertThat(search.getRelevance("Page4"), is(7.0));
		assertThat(search.getRelevance("Page5"), is(0.0));
	}

	/**
	 * Test method for {@link com.flatironschool.javacs.WikiSearch#and(com.flatironschool.javacs.WikiSearch)}.
	 */
	@Test
	public void testAnd() {
		WikiSearch search = search1.and(search2);
		assertThat(search.getRelevance("Page1"), is(0.0));
		assertThat(search.getRelevance("Page2"), is(2.0));
		assertThat(search.getRelevance("Page3"), is(3.0));
		assertThat(search.getRelevance("Page4"), is(0.0));
		assertThat(search.getRelevance("Page5"), is(0.0));
	}

	/**
	 * Test method for {@link com.flatironschool.javacs.WikiSearch#minus(com.flatironschool.javacs.WikiSearch)}.
	 */
	@Test
	public void testMinus() {
		WikiSearch search = search1.minus(search2);
		assertThat(search.getRelevance("Page1"), is(1.0));
		assertThat(search.getRelevance("Page2"), is(0.0));
		assertThat(search.getRelevance("Page3"), is(0.0));
		assertThat(search.getRelevance("Page4"), is(0.0));
		assertThat(search.getRelevance("Page5"), is(0.0));
	}

	/**
	 * Test method for {@link com.flatironschool.javacs.WikiSearch#sort()}.
	 */
	@Test
	public void testSort() {
		List<Entry<String, Double>> list = search2.sort();
		assertThat(list.get(2).getValue(), is(4.0));
		assertThat(list.get(1).getValue(), is(5.0));
		assertThat(list.get(0).getValue(), is(7.0));
	}
}
