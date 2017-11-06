package de.lab.CFIMining.MFCI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.lab.CFIMining.Itemset;

/** 
 * A class representing the temporary table used in the addition of the MFCI algorithm. Each entry in this 
 * table consists of a closure id (an content table identifier of the itemsets closure), an itemset, its 
 * status, support and a corresponding content table identifier of the itemset (if exists). 
 * 
 * @author Eike Stadtlaender
 *
 * @param <T>
 */
public class TempTable<T extends Comparable<T>> implements Iterable<TempTable<T>.Entry> {
	
	/** 
	 * A class to represent an entry in the temporary table. 
	 * 
	 * @author Eike Stadtlaender
	 *
	 */
	public class Entry {
		
		public int closureId = -1; 
		public Itemset<T> itemset = null; 
		public int status = 0; 
		public int support = 0;
		public int cid = -1; 
		
	}
	
	private List<Entry> entries; 
	
	/** 
	 * Construct a new empty temporary temple.  
	 */
	TempTable() {
		this.entries = new ArrayList<Entry>(); 
	}
	
	/** 
	 * Create a new temporary table entry by a set of properties. 
	 * 
	 * @param closureId The content table identifier of the closure of the respective itemset. 
	 * @param itemset   The itemset of the temporary table's entry. 
	 * @param status    The status (processed or not visited yet).
	 * @param support   The support of the itemset. 
	 * @return Returns the newly created entry in the temporary table. 
	 */
	public Entry newEntry(int closureId, Itemset<T> itemset, int status, int support) {
		return this.newEntry(closureId, itemset, status, support, false); 
	}
	
	/** 
	 * Create a new temporary table entry by a given set of properties. 
	 * 
	 * @param closureId The content table identifier of the closure of the respective itemset. 
	 * @param itemset   The itemset of the temporary table's entry. 
	 * @param status    The status (processed or not visited yet).
	 * @param support   The support of the itemset. 
	 * @param front     If this flag is true the new entry will be added to the front of the temporary 
	 * 	                table.
	 * @return Returns the newly created entry in the temporary table. 
	 */
	public Entry newEntry(int closureId, Itemset<T> itemset, int status, int support, boolean front) {
		Entry e = new Entry(); 
		e.closureId = closureId; 
		e.itemset = itemset; 
		e.status = status; 
		e.support = support; 
		if(front) {
			this.entries.add(0, e); 
		} else {
			this.entries.add(e);
		} 
		return e; 
	}
	
	/** 
	 * Retrieve a temporary table entry by a given closure identifier. 
	 * 
	 * @param closureId The closure identifier to look for in the temporary table. 
	 * @return Returns the (first) entry having the given closure identifier. 
	 */
	public Entry findByClosureId(int closureId) {
		for(Entry e : this.entries) {
			if(e.closureId == closureId) {
				return e; 
			}
		}
		return null; 
	}
	
	/** 
	 * Sort the temporary table by descending itemset size and itemsets of equal length according to the 
	 * lexicographical ordering. 
	 */
	public void sort() {
		
		this.entries.sort(new Comparator<Entry>() {

			@Override
			public int compare(Entry a, Entry b) {
				// a < b if the len(a) < len(b)
				if(a.itemset.size() < b.itemset.size()) {
					return 1; 
				} else if(b.itemset.size() < a.itemset.size()) {
					return -1; 
				} else {
					return a.itemset.compareTo(b.itemset); 
				}
			}
			
		});
		
	}
	
	/** 
	 * Merge entries of the temporary table which represent the same itemset. Use the entry with the 
	 * maximal support. Note that {@link TempTable#sort()} <b>must</b> be called before this function. 
	 */
	public void merge() {
		// Find the obsolete temporary table entries 
		Set<Integer> obsolete = new HashSet<Integer>();  
		for(int i = 0; i+1 < this.entries.size(); i++) {
			for(int j = i+1; j < this.entries.size(); j++) {
				Entry a = this.entries.get(i); 
				Entry b = this.entries.get(j); 
				if(a.itemset.equals(b.itemset)) {
					if(a.support < b.support) {
						obsolete.add(i);
						break; 
					} else {
						obsolete.add(j); 
					}
				} else {
					break; 
				}
			}
		}
		
		// And then remove them
		List<Integer> obsoleteList = new ArrayList<Integer>(); 
		obsoleteList.addAll(obsolete); 
		Collections.sort(obsoleteList, Collections.reverseOrder());
		for(int idx : obsoleteList) {
			this.entries.remove(idx); 
		}
	}
	
	/** 
	 * Retrieve the number of entries currently in this temporary table. 
	 * 
	 * @return Returns an integer with the current size of this temporary table. 
	 */
	public int size() {
		return this.entries.size(); 
	}
	
	/** 
	 * Determine whether or not there is an entry in this temporary table representing a given itemset. 
	 * 
	 * @param itemset The itemset to be searched for. 
	 * @return Returns {@code true} if the given itemset is represented by one of this table's entries and 
	 *         {@code false} otherwise.  
	 */
	public boolean contains(Itemset<T> itemset) {
		for(Entry e : this.entries) {
			if(e.itemset.equals(itemset)) {
				return true; 
			}
		}
		return false; 
	}
	
	@Override 
	public Iterator<Entry> iterator() {
		return this.entries.iterator(); 
	}

	public ListIterator<Entry> listIterator(int index) {
		return this.entries.listIterator(index); 
	}

	@Override
	public String toString() {
		int maxCidLength = 0; 
		int maxItemsetLength = 0; 
		int maxStatusLength = 0; 
		for(Entry e : this.entries) {
			int cidLength = Integer.toString(e.closureId).length();
			int itemsetLength = e.itemset.toString().length();
			int statusLength = Integer.toString(e.status).length();
			if(maxCidLength < cidLength) {
				maxCidLength = cidLength; 
			}
			if(maxItemsetLength < itemsetLength) {
				maxItemsetLength = itemsetLength; 
			}
			if(maxStatusLength < statusLength) {
				maxStatusLength = statusLength; 
			}
		} 
		
		List<String> lines = new ArrayList<String>(); 
		for(Entry e : this.entries) {
			String line = "| "; 
			line += String.format("%" + maxCidLength + "s", Integer.toString(e.closureId)) + " | ";
			line += String.format("%" + maxItemsetLength + "s", e.itemset.toString()) + " | "; 
			line += String.format("%" + maxStatusLength + "s", Integer.toString(e.status)) + " |";
			lines.add(line); 
		}
		return String.join("\n", lines);  
	}
	
}