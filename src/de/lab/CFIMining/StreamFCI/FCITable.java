package de.lab.CFIMining.StreamFCI;

import java.util.SortedSet;
import java.util.TreeSet;

import de.lab.CFIMining.Itemset;

/* 
 * FIXME: This class is not implemented yet and not actually used in the StreamFCI algorithm right now. 
 */

public class FCITable<T extends Comparable<T>> {

	class Entry  {
		
		int support; 
		Itemset<T> itemset; 
		
	}
	
	private DFPTree<T> dfpTree; 
	private SortedSet<Entry> entries; 
	
	FCITable(DFPTree<T> dfpTree) {
		this.dfpTree = dfpTree; 
		this.entries = new TreeSet<Entry>(); 
	}
	
	public void add(Itemset<T> X) {
		// TODO ...
	}
	
	public void delete(Itemset<T> X) {
		// TODO ...
	}
	
	@Override 
	public String toString() {
		int maxSupportLength = -1; 
		int maxItemsetLength = -1;
		
		for(Entry entry : this.entries) {
			int supportLength = Integer.toString(entry.support).length(); 
			int itemsetLength = entry.itemset.toString().length(); 
			if(supportLength > maxSupportLength) {
				maxSupportLength = supportLength; 
			}
			if(itemsetLength > maxItemsetLength) {
				maxItemsetLength = itemsetLength; 
			}
		}
		
		String repr = ""; 
		for(Entry entry : this.entries) {
			repr += "| "; 
			repr += String.format("%" + maxSupportLength + "s", Integer.toString(entry.support)) + " | ";
			repr += String.format("%" + maxItemsetLength + "s", entry.itemset.toString()); 
			repr += " |\n"; 
		}
		return repr; 
	}
	
}
