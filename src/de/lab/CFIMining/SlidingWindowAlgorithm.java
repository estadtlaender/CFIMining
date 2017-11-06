package de.lab.CFIMining;

import java.util.Set;

/**
 * Interface for algorithms which implement a sliding window approach for closed frequent itemset mining. 
 * 
 * @author Eike Stadtlaender
 *
 * @param <T> The type of the items in the stream. For instance, if each item is given by an integer, then 
 *            {@code SlidingWindowAlgorithm<Integer>} is used. 
 */
public interface SlidingWindowAlgorithm<T extends Comparable<T>> {
	
	/** 
	 * Compute the support of the given itemset. 
	 * 
	 * @param X The itemset of which the support shall be computed. 
	 * @return Returns the support of the given itemset.
	 */
	public int support(Itemset<T> X); 
	
	/** 
	 * Add a given itemset (transaction) to the sliding window, i.e. add it to the underlying data 
	 * structure of the algorithm.
	 *  
	 * @param X The new itemset to be added. 
	 */
	public void add(Itemset<T> X); 
	
	/** 
	 * Delete a given itemset (transaction) from the sliding window, i.e. delete it from the underlying data 
	 * structure of the algorithm. 
	 * 
	 * @param X The itemset to be deleted. 
	 */
	public void delete(Itemset<T> X); 
	
	/** 
	 * Get a collection (i.e. a {@code Set<T>} of all closed itemsets which are obtained from the current 
	 * sliding window. 
	 * 
	 * @return Returns a {@code Set<T>} containing all closed itemsets if the current sliding window is the 
	 *         complete transaction database. 
	 */
	public Set<Itemset<T>> getClosedItemsets();
	
	/** 
	 * Get a collection (i.e. a {@code Set<T>} of all closed itemsets with a support greater than a given 
	 * threshold which are obtained from the current sliding window. 
	 * 
	 * @param t The frequency threshold. 
	 * @return Returns a {@code Set<T>} containing all closed frequent itemsets if the current sliding 
	 *         window is the complete transaction database. 
	 */
	public Set<Itemset<T>> getClosedFrequentItemsets(int t); 
	
}
