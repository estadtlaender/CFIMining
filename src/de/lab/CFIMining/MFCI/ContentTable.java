package de.lab.CFIMining.MFCI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.lab.CFIMining.Itemset;

/** 
 * A class representing the content table used in the MFCI algorithm. 
 * 
 * @author Eike Stadtlaender
 *
 * @param <T>
 */
public class ContentTable<T extends Comparable<T>> {

	/** 
	 * A class representing an entry in the content table of the MFCI algorithm. Each entry consists of a 
	 * unique identifier, a (closed) itemset, its support and references to the entries of the immediate 
	 * closed supersets and subsets of the entry's itemset.   
	 * 
	 * @author Eike Stadtlaender
	 *
	 */
	public class Entry {
		
		public int cid; 
		public Itemset<T> itemset; 
		public int support; 
		public List<Integer> immediateClosedSubsets = new ArrayList<Integer>(); 
		public List<Integer> immediateClosedSupersets = new ArrayList<Integer>();
		
	}

	private Map<Integer, Entry> lookupTable = new HashMap<Integer, Entry>(); 
	private List<Entry> entries = new ArrayList<Entry>(); 
	private int nextCid = 0;
	
	/** 
	 * Create a new entry for a given itemset. This includes assignment of a unique content table 
	 * identifier (cid). 
	 * 
	 * @param itemset The itemset for which a new entry in the content table is to be created. 
	 * @return Returns the newly created {@code Entry}. 
	 */
	public Entry newEntry(Itemset<T> itemset) {
		Entry entry = new Entry(); 
		entry.cid = this.nextCid++; 
		entry.itemset = itemset; 
		entry.support = 0; 
		this.entries.add(entry); 
		this.lookupTable.put(entry.cid, entry); 
		return entry; 
	}
	
	/** 
	 * Obtain an entry by a given identifier. 
	 * 
	 * @param cid The identifier of the requested content table entry. 
	 * @return Returns an {@code Entry} corresponding to the given identifier
	 */
	public Entry get(int cid) {
		return this.lookupTable.get(cid);
	}
	
	/** 
	 * Delete an entry from this content table. 
	 * 
	 * @param cid The entry to be deleted. 
	 */
	public void delete(int cid) {
		// remove cid from all ic super- and subsets 
		Entry entry = this.get(cid); 
		for(int id : entry.immediateClosedSubsets) {
			Entry e = this.get(id);
			e.immediateClosedSupersets.remove((Integer) cid); 			
		}
		for(int id : entry.immediateClosedSupersets) {
			Entry e = this.get(id); 
			e.immediateClosedSubsets.remove((Integer) cid); 
		}
		// remove the actual entry from both, the entry list and the entry lookup table
		this.entries.removeIf(s -> s.cid == cid); 
		this.lookupTable.remove(cid); 
	}
	
	/** 
	 * Obtain the itemset of a content table entry by a given identifier. 
	 * 
	 * @param cid The identifier of the requested content table entry. 
	 * @return Returns an {@code Itemset<T>} of the entry which is looked for. 
	 */
	public Itemset<T> itemset(int cid) {
		Entry e = this.get(cid); 
		if(e != null) {
			return e.itemset; 
		} else {
			return null; 
		}
	}
	
	/** 
	 * Obtain the identifiers of all content table entries which contain a given item. 
	 *  
	 * @param item The item to be looked for. 
	 * @return Returns a list of content table identifiers where the corresponding itemsets contain the 
	 *         given item 
	 * @deprecated 
	 * @see ItemTable#CIDs(Comparable)
	 */
	public List<Integer> CIDs(T item) {
		List<Integer> findings = new ArrayList<Integer>(); 
		for(Entry e : this.entries) {
			if(e.itemset.contains(item)) {
				findings.add(e.cid); 
			}
		}
		return findings;
	}
	
	/** 
	 * Obtain the support of an itemset corresponding to the entry with the given identifier. 
	 *  
	 * @param cid The identifier of the content table entry of interest. 
	 * @return Returns an integer with the support of the itemset in question. 
	 */
	public int supportByCID(int cid) {
		Entry e = this.get(cid); 
		if(e != null) {
			return e.support; 
		} else {
			return -1; 
		}
	}
	
	/** 
	 * Add an immediate closed subset (given by a content table identifier) to a content table entry (also 
	 * given by a content table identifier). If this contradicts the table structure nothing happens. If 
	 * other subsets are not immediate anymore, they will be removed.  
	 * 
	 * @param cidTarget The identifier of the receiving entry. 
	 * @param cidNew    The identifier of the subset to be added (if that's correct) 
	 */
	public void addImmediateClosedSubset(int cidTarget, int cidNew) {
		Entry eTarget = this.get(cidTarget); 
		Entry eNew = this.get(cidNew); 
		
		if(eTarget.immediateClosedSubsets.contains(cidNew)) {
			return; 
		}
		
		List<Integer> obsoleteCids = new ArrayList<Integer>(); 
		for(int cid : eTarget.immediateClosedSubsets) {
			Entry e = this.get(cid); 
			if(eNew.itemset.contains(e.itemset)) {
				obsoleteCids.add(cid); 
			}
		}
		eTarget.immediateClosedSubsets.add(cidNew); 
		eTarget.immediateClosedSubsets.removeAll(obsoleteCids); 
	}
	
	/** 
	 * Add an immediate closed superset (given by a content table identifier) to a content table entry (also 
	 * given by a content table identifier). If this contradicts the table structure nothing happens. If 
	 * other supersets are not immediate anymore, they will be removed. 
	 * 
	 * @param cidTarget The identifier of the receiving entry. 
	 * @param cidNew    The identifier of the superset to be added (if that's correct).
	 */
	public void addImmediateClosedSuperset(int cidTarget, int cidNew) {
		Entry eTarget = this.get(cidTarget); 
		Entry eNew = this.get(cidNew); 
		
		if(eTarget.immediateClosedSupersets.contains(cidNew)) {
			return; 
		}
		
		List<Integer> obsoleteCids = new ArrayList<Integer>(); 
		for(int cid : eTarget.immediateClosedSupersets) {
			Entry e = this.get(cid); 
			if(e.itemset.contains(eNew.itemset)) {
				obsoleteCids.add(cid); 
			}
		}
		
		eTarget.immediateClosedSupersets.add(cidNew); 
		eTarget.immediateClosedSupersets.removeAll(obsoleteCids); 
	}
	
	/** 
	 * Compute the number of entries currently in the content table. 
	 * 
	 * @return Returns an integer with the number of content table entries. 
	 */
	public int size() {
		return this.entries.size(); 
	}
	
	/** 
	 * Check whether or not a given itemset is already in this content table. 
	 * 
	 * @param X The itemset which shall be checked for. 
	 * @return Returns {@code true} if the content table has an entry for the given itemset and {@code false} 
	 *         otherwise. 
	 */
	public boolean contains(Itemset<T> X) {
		for(Entry e : this.entries) {
			if(e.itemset.equals(X)) {
				return true; 
			}
		}
		return false; 
	}
	
	/** 
	 * Check whether or not there is a path of immediate closed supersets from a given entry to a given 
	 * entry.
	 *  
	 * @param from The entry from which the path is supposed to originate from. 
	 * @param to   The entry which shall be reached from the starting point. 
	 * @return Returns {@code true} if there is a path from the first entry to the second entry via 
	 *         immediate closed supersets. 
	 */
	public boolean hasPath(Entry from, Entry to) {
		/*
		 * Basically a simple recursion. There is such a path if there is a path originating from one of the 
		 * first entry's immediate closed supersets. 
		 */
		for(int cid : from.immediateClosedSupersets) {
			Entry e = this.get(cid);
			if(to.itemset.contains(e.itemset) && 
					(this.hasPath(e, to) || from.immediateClosedSupersets.contains((Integer) to.cid))) {
				return true; 
			}
		}
		return false; 
	}
	
	/** 
	 * Get an entry of the content table by a given itemset. 
	 * 
	 * @param X The itemset to be looked for in this content table. 
	 * @return Returns an {@code Entry} holding the given itemset of {@code null} if there is no such entry. 
	 */
	public Entry getByItemset(Itemset<T> X) {
		for(Entry e : this.entries) {
			if(e.itemset.equals(X)) {
				return e; 
			}
		}
		return null; 
	}
	
	/** 
	 * Compute a list of closed itemsets from the current content table. 
	 * 
	 * @return A list comprising all closed itemsets currently in this content table. 
	 */ 
	public Set<Itemset<T>> getClosedItemsets() {
		return this.getClosedFrequentItemsets(0); 
	}
	
	/** 
	 * Compute a list of closed frequent itemsets with a support count of at least {@code t} from the 
	 * content table.  
	 * 
	 * @param t  The threshold value of the support count
	 * @return Returns a {@code Set<Itemset<T>>} comprising all closed {@code t}-frequent itemsets
	 */
	public Set<Itemset<T>> getClosedFrequentItemsets(int t) {
		Set<Itemset<T>> cfi = new HashSet<Itemset<T>>(); 
		for(Entry e : this.entries) {
			if(e.support >= t) {
				cfi.add(e.itemset); 
			}
		}
		return cfi; 
	}
	
	/** 
	 * Compute the support of a given arbitrary itemset in the current sliding window. 
	 * 
	 * @param X The itemset of which the support shall be computed. 
	 * @return Returns an integer holding the requested support. 
	 */
	public int support(Itemset<T> X) {
		/* 
		 * The support of a given itemset is actually equal to the minimal support of its supersets in this 
		 * content table. This is searched in the following. 
		 */
		int sup = 0;
		Entry minEntry = null; 
		for(Entry e : this.entries) {
			if(e.itemset.contains(X) && (minEntry == null || e.itemset.size() <= minEntry.itemset.size())) {
				minEntry = e; 
				if(minEntry.itemset.equals(X)) {
					return minEntry.support; 
				}
			}
		}
		if(minEntry != null) {
			sup = minEntry.support; 
		}
		return sup; 
	}
	
	@Override 
	public String toString() {

		int maxCidLength = 0; 
		int maxItemsetLength = 0; 
		int maxSupportLength = 0; 
		int maxICSubLength = 0; 
		int maxICSupLength = 0; 
		for(Entry e : this.entries) {
			int cidLength = Integer.toString(e.cid).length();
			int itemsetLength = e.itemset.toString().length();
			int statusLength = Integer.toString(e.support).length();
			int ICSubLength = e.immediateClosedSubsets.toString().length();
			int ICSupLength = e.immediateClosedSupersets.toString().length(); 
			if(maxCidLength < cidLength) {
				maxCidLength = cidLength; 
			}
			if(maxItemsetLength < itemsetLength) {
				maxItemsetLength = itemsetLength; 
			}
			if(maxSupportLength < statusLength) {
				maxSupportLength = statusLength; 
			}
			if(maxICSubLength < ICSubLength) {
				maxICSubLength = ICSubLength; 
			}
			if(maxICSupLength < ICSupLength) {
				maxICSupLength = ICSupLength; 
			}
		} 
		
		List<String> lines = new ArrayList<String>(); 
		for(Entry e : this.entries) {
			String line = "| "; 
			line += String.format("%" + maxCidLength + "s", Integer.toString(e.cid)) + " | ";
			line += String.format("%" + maxItemsetLength + "s", e.itemset) + " | "; 
			line += String.format("%" + maxSupportLength + "s", Integer.toString(e.support)) + " | ";
			line += String.format("%" + maxICSubLength + "s",  e.immediateClosedSubsets) + " | "; 
			line += String.format("%" + maxICSupLength + "s", e.immediateClosedSupersets) + " |"; 
			lines.add(line); 
		}
		return String.join("\n", lines);  
	}
	
}
