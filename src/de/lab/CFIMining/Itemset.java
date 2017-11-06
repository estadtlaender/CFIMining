package de.lab.CFIMining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generic class representing an itemset. Internally a TreeSet is used to keep the itemset in order 
 * according to the implementation of {@code Comparable<T>}.  
 * @author Eike Stadtlaender
 *
 * @param <T> The type or class representing the items in the itemset. {@code Itemset<Integer>} represents 
 * 			  an itemset where the items are integers. 
 */
public class Itemset<T extends Comparable<T>> implements Iterable<T>, Comparable<Itemset<T>> {
	private Set<T> items;
	
	/** 
	 * Constructs an empty itemset. 
	 */
	public Itemset() {
		this.items = new TreeSet<T>(); 
	}
	
	/**
	 * Constructs an itemset from a list of items given as parameters. 
	 * 
	 * @param ts A list of items the constructed itemset shall hold. 
	 */
	@SafeVarargs
	public Itemset(T ...ts) {
		this.items = new TreeSet<T>(Arrays.asList(ts)); 
	}
	
	/** 
	 * Constructs an itemset from a {@Set<T>}. The itemsets holds the same items as the given set.
	 *  
	 * @param items The items that are supposed to be in the constructed itemset. 
	 */
	public Itemset(Set<T> items) {
		this.items = new TreeSet<T>(items); 
	}
	
	/**
	 * Construct a copy of a given itemset. 
	 * 
	 * @param other The other itemset from which the new one is copied. 
	 */
	public Itemset(Itemset<T> other) {
		this(other.items); 
	}
	
	/** 
	 * Add a new item to this itemset. If the item is already included in this itemset then nothing changes.
	 *  
	 * @param item The item to be added.
	 */
	public void add(T item) 
	{
		items.add(item); 
	}
	
	/** 
	 * Add all items of another itemset. If an item is already contained in this itemset then it will not 
	 * be added again. 
	 * 
	 * @param other The other itemset to take the new items from. 
	 */
	public void add(Itemset<T> other) {
		this.items.addAll(other.items); 
	}
	
	/** 
	 * Remove all items which are not contained in another itemset {@code other}. <b>Note:</b> This will 
	 * change the itemset and not create a new itemset. 
	 * 
	 * @param other The other itemset to build the intersection from. 
	 * 
	 * @see Itemset#intersection(Itemset)
	 */
	public void retainAll(Itemset<T> other) {
		this.items.retainAll(other.items); 
	}
	
	/** 
	 * Check if this itemset is a superset of a given itemset {@code other}.
	 *  
	 * @param other The other itemset 
	 * @return Returns {@code true} if this itemset is a superset of {@code other} and {@code false} 
	 *         otherwise.  
	 */
	public boolean contains(Itemset<T> other) {
		return this.items.containsAll(other.items); 
	}
	
	/** 
	 * Check if this itemset contains a certain item. 
	 * 
	 * @param item The item to be checked for. 
	 * @return Returns {@code true} if {@code item} is contained in this itemset and {@code false} 
	 *         otherwise.
	 */
	public boolean contains(T item) {
		return this.items.contains(item); 
	}
	
	/** 
	 * Obtain the number of items in this itemset. 
	 * 
	 * @return Returns an {@code int} with the current number of (distinct) items contained in this itemset.
	 */
	public int size() {
		return this.items.size(); 
	}
	
	/** 
	 * Compute the intersection of this itemset with another and return a new itemset. This method is 
	 * different from {@link Itemset#retainAll(Itemset)} in that it creates a new itemset and leaves this 
	 * itemset unchanged.
	 *  
	 * @param other The other itemset to build the intersection from. 
	 * @return Returns an {@code Itemset<T>} holding the items which are contained in both itemsets {@code 
	 *         this} and {@code other}. 
	 */
	public Itemset<T> intersection(Itemset<T> other) {
		// copy this itemset and call retain on it with the other itemset as input. 
		Set<T> intersect = new TreeSet<T>(this.items); 
		intersect.retainAll(other.items); 
		return new Itemset<T>(intersect); 
	}
	
	/** 
	 * Compute the union of this itemset with another and return a new itemset. This method is different 
	 * from {@link Itemset#add(Itemset)} in that it creates a new itemset and leaves this itemset unchanged. 
	 * 
	 * @param other The other itemset to build the union from. 
	 * @return Returns an {@code Itemset<T>} holding the items which are contained in at least one of the 
	 *         itemsets {@code this} or {@code other}. 
	 */
	public Itemset<T> union(Itemset<T> other) {
		Set<T> union = new TreeSet<T>(this.items); 
		union.addAll(other.items); 
		return new Itemset<T>(union); 
	}
	
	/** 
	 * Obtain a copy of the items contained in this itemset. 
	 * 
	 * @return A {@code Set<T>} containing all the items which are in this itemset. 
	 */
	public Set<T> getItems() {
		return new TreeSet<T>(this.items); 
	}
	
	/** 
	 * Computes a {@code SortedSet<T>} of the items according to some order given by a 
	 * {@code Map<T, Integer>}. 
	 *  
	 * @param history The ranks of items. Item {@code item} has rank {@code history.get(item)} in this 
	 *                order. 
	 * @return Returns a {@code SortedSet<T>} of the items (sorted according to the given order). 
	 */
	private SortedSet<Integer> mapToHistory(Map<T, Integer> history) {
		SortedSet<Integer> items = new TreeSet<Integer>(); 
		for(T item : this.items) {
			items.add(history.get(item)); 
		}
		return items; 
	}
	
	/** 
	 * Compare two itemsets according to some given order of items. 
	 * 
	 * @param other   The other itemset to compare this itemset with. 
	 * @param history The order of the items. The rank of item {@code item} is given by 
	 *                {@code history.get(item)}. 
	 * @return Returns a negative integer, zero or a positive integer if this itemset is less than, equal 
	 *         or larger than the other itemset.
	 */
	public int compareTo(Itemset<T> other, Map<T, Integer> history) {
		// sort the items in both itemset according to the given order 
		SortedSet<Integer> items = this.mapToHistory(history); 
		SortedSet<Integer> itemsOther = other.mapToHistory(history); 
		
		// then check the lexicographical order 
		Iterator<Integer> iter = items.iterator(); 
		Iterator<Integer> iterOther = itemsOther.iterator(); 
		while(iter.hasNext() && iterOther.hasNext()) {
			int i = iter.next(); 
			int iOther = iterOther.next(); 
			if(i < iOther) {
				return -1; 
			} else if(i > iOther) {
				return 1; 
			}
		}
		
		// this code is only reached if one of the itemsets is a prefix of the other. The prefix is then 
		// the smaller itemset (in alignment with the lexicographical order). 
		if(items.size() < itemsOther.size()) {
			return -1; 
		} else if(items.size() > itemsOther.size()) {
			return 1; 
		} else {
			return 0; 
		}
	}
	
	/** 
	 * Generate the subsets of a given itemset in descending length. 
	 * 
	 * @return Returns a {@code List<Itemset<T>>} containing the subsets (including the itemset itself) 
	 *         sorted by length in descending order. 
	 */
	public List<Itemset<T>> subsetsInDescendingLength() {
		List<Itemset<T>> subsets = new ArrayList<Itemset<T>>(); 
		List<Integer> indices = new ArrayList<Integer>(); 
		List<T> items = new ArrayList<T>(this.items); 
		
		// create the singletons from the items in the itemset. 
		int counter = 1; 
		for(T item : items) {
			subsets.add(new Itemset<T>(item)); 
			indices.add(counter++); 
		}
		
		// combine the generated subsets in a clever way to generate all subsets in ascending order by 
		// length 
		for(int j = 0; j < subsets.size(); j++) {
			for(int k = indices.get(j); k < items.size(); k++) {
				Itemset<T> newItemset = subsets.get(j).union(subsets.get(k)); 
				subsets.add(newItemset); 
				indices.add(k+1); 
			}
		}
		
		// reverse the order 
		Collections.reverse(subsets);
		return subsets; 
	}

	@Override 
	public String toString() {
		return this.items.toString(); 
	}

	@Override
	public Iterator<T> iterator() {
		return this.items.iterator(); 
	}

	@Override 
	public int hashCode() {
		return this.items.hashCode(); 
	}

	@Override 
	public boolean equals(Object other) {
		if(this == other) 
			return true;
		if(other == null) 
			return false; 
		
		if(other instanceof Itemset<?>) {
			Itemset<?> oth = (Itemset<?>) other;
			return this.items.equals(oth.items); 
		} 
		
		return false; 
	}

	@Override
	public int compareTo(Itemset<T> other) {
		Iterator<T> iter = this.items.iterator(); 
		Iterator<T> iterOther = other.items.iterator(); 
		while(iter.hasNext() && iterOther.hasNext()) {
			T item = iter.next();
			T itemOther = iterOther.next(); 
			if(item.compareTo(itemOther) < 0) {
				return -1; 
			} else if(item.compareTo(itemOther) > 0) {
				return 1; 
			}
		}
	
		if(this.size() < other.size()) {
			return -1; 
		} else if(this.size() > other.size()) {
			return 1; 
		} else {
			return 0; 
		}
	}
	
}
