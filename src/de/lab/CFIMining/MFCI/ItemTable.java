package de.lab.CFIMining.MFCI;

import java.util.List;
import java.util.Map;

import de.lab.CFIMining.Itemset;

import java.util.ArrayList;
import java.util.HashMap;

/** 
 * A class representing the item table used in the MFCI algorithm. This table maps items to the content 
 * table identifiers of the entries whose itemset they represent contain the item.  
 * 
 * @author Eike Stadtlaender
 *
 * @param <T>
 */
public class ItemTable<T extends Comparable<T>> {
	
	private Map<T, List<Integer>> entries = new HashMap<T, List<Integer>>(); 
	
	/** 
	 * Obtain the content table identifiers of all entries whose itemset contains a given item. 
	 * 
	 * @param item The item for which the content table identifiers are requested. 
	 * @return Returns a {@code List<Integer>} containing all relevant content table identifiers. This is 
	 *         an empty list if no such identifier exists. 
	 */
	public List<Integer> CIDs(T item) {
		if(this.entries.containsKey(item)) {
			return this.entries.get(item); 
		} else {
			return new ArrayList<Integer>(); 
		}
	}
	
	/** 
	 * Add a new content table identifier for a given item. If no entry for the given item exists then a 
	 * new entry is created. 
	 * 
	 * @param item The item of interest. 
	 * @param cid  The content table identifier to be added. 
	 */
	public void add(T item, int cid) {
		if(this.entries.containsKey(item)) {
			List<Integer> cids = this.entries.get(item); 
			if(!cids.contains(cid)) {
				cids.add(cid); 
			}
		} else {
			List<Integer> cids = new ArrayList<Integer>(); 
			cids.add(cid); 
			this.entries.put(item, cids); 
		}
	}
	
	/** 
	 * Remove a given content table identifier for all items in a given itemset. 
	 * 
	 * @param cid     The given content table identifier. 
	 * @param itemset The given itemset (usually the itemset corresponding to the content table entry of the 
	 *                given content table identifier. 
	 */
	public void delete(int cid, Itemset<T> itemset) {
		for(T item : itemset) {
			List<Integer> cids = this.entries.get(item); 
			cids.remove((Integer) cid); 
		}
	}
	
	@Override 
	public String toString() {
		int maxItemLength = 0; 
		int maxCidsLength = 0; 
		
		for(T item : this.entries.keySet()) {
			List<Integer> cids = this.entries.get(item); 
			int itemLength = item.toString().length(); 
			int cidsLength = cids.toString().length(); 
			if(maxItemLength < itemLength) {
				maxItemLength = itemLength; 
			}
			if(maxCidsLength < cidsLength) {
				maxCidsLength = cidsLength; 
			}
		}

		List<String> lines = new ArrayList<String>(); 
		for(T item : this.entries.keySet()) {
			List<Integer> cids = this.entries.get(item); 
			String line = "| "; 
			line += String.format("%" + maxItemLength + "s", item) + " | "; 
			line += String.format("%" + maxCidsLength + "s", cids) + " |"; 
			lines.add(line); 
		}
		return String.join("\n", lines);
	}

}
