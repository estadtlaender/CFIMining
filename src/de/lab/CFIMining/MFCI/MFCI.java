package de.lab.CFIMining.MFCI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import de.lab.CFIMining.Itemset;
import de.lab.CFIMining.SlidingWindowAlgorithm;

/** 
 * A class implementing the MFCI algorithm as described in the paper "An efficient algorithm for 
 * incrementally mining frequent closed itemsets" by Yen et al.
 *  
 * @author Eike Stadtlaender
 *
 * @param <T>
 */
public class MFCI<T extends Comparable<T>> implements SlidingWindowAlgorithm<T> {
	
	private ContentTable<T> contentTable; 
	private ItemTable<T> itemTable;
	
	/** 
	 * Construct a new MFCI instance with an empty content table and an empty item table. 
	 */
	public MFCI() {
		this.contentTable = new ContentTable<T>(); 
		this.itemTable = new ItemTable<T>(); 
	}
	
	/** 
	 * Generate the corresponding {@code TempTable<T>} after adding a given transaction. This method is 
	 * described in the paper "An efficient algorithm for incrementally mining frequent closed itemsets" by 
	 * Yen et al. The temporary table is supposed to contain all the new closed itemsets after adding the 
	 * given transaction. 
	 * 
	 * @param X The newly added transaction. 
	 * @return Returns a {@code TempTable<T>} corresponding to the newly added transaction. 
	 */
	private TempTable<T> generateClosedItemsets(Itemset<T> X) {
		TempTable<T> tmpTbl = new TempTable<T>(); 
		Set<Integer> union = new HashSet<Integer>();
		
		/* 
		 * For each item (in lexicographical order) the content table identifiers are retrieved from the 
		 * item table. Then for all those identifiers either a new temporary table entry is created or an 
		 * existing entry is updated by the corresponding item. This process builds the new closed itemsets 
		 * from the ground up.  
		 */
		for(T item : X) {
			List<Integer> cids = this.itemTable.CIDs(item);
			for(int C : cids) {
				if(union.contains(C)) {
					TempTable<T>.Entry entry = tmpTbl.findByClosureId(C); 
					if(entry != null) {
						entry.itemset.add(item);
					}
				} else {
					Itemset<T> itemset = new Itemset<T>(); 
					itemset.add(item);
					tmpTbl.newEntry(C, itemset, 0, this.contentTable.supportByCID(C));
				}
			}
			union.addAll(cids);
		}
		
		// The temp table is sorted so that new closed itemset which occur multiple times can be reduced 
		tmpTbl.sort(); 
		tmpTbl.merge(); 
		
		return tmpTbl; 
	}

	/** 
	 * {@inheritDoc} This algorithm is described in the paper "An efficient algorithm for incrementally 
	 * mining frequent closed itemsets" by Yen et al. 
	 */
	@Override
	public void add(Itemset<T> newItemset) { 
		TempTable<T> tempTbl = this.generateClosedItemsets(newItemset);
		boolean closureFlag = this.contentTable.contains(newItemset); 
		
		// Add new or update already existing entry of the content table for all the temporary table entries
		for(TempTable<T>.Entry X : tempTbl) {
			ContentTable<T>.Entry closureEntry = this.contentTable.get(X.closureId);
			ContentTable<T>.Entry entryOfX = null; 
			if(!X.itemset.equals(closureEntry.itemset)) {
				// entry.itemset is a new closed itemset 
				entryOfX = this.contentTable.newEntry(X.itemset);
				X.cid = entryOfX.cid; 
				
				// update immediate closed sub-/supersets (and remove then obsolete entries
				this.contentTable.addImmediateClosedSuperset(entryOfX.cid, closureEntry.cid);
				this.contentTable.addImmediateClosedSubset(closureEntry.cid, entryOfX.cid);
				
				// update this.itemTable 
				for(T item : entryOfX.itemset) {
					this.itemTable.add(item, entryOfX.cid); 
				}
			} else {
				// if X was already a closed itemset, then it is represented by closureEntry
				entryOfX = closureEntry;  
				X.cid = entryOfX.cid; 
			}
			
			// update the support count
			entryOfX.support = closureEntry.support + 1; 
		}
		
		// Ensure that the newly added itemset is already part of the temporary table 
		if(!tempTbl.contains(newItemset)) {
			ContentTable<T>.Entry newItemsetEntry = this.contentTable.newEntry(newItemset);
			TempTable<T>.Entry e = tempTbl.newEntry(-1, newItemset, 0, 1, true);
			e.cid = newItemsetEntry.cid; 
			newItemsetEntry.support = 1; 
			for(T item : newItemset) {
				this.itemTable.add(item, newItemsetEntry.cid);
			}
		} 
		/*
		 * Note: Here I'm deviating from the algorithm presented in the paper due to some problems with 
		 *       updating the immediate closed sub- and supersets. 
		 */
		/*else {
			return; 
		}//*/
		
		/* 
		 * If the content table already contained the newly added transaction then no further processing 
		 * is needed. 
		 */
		if(closureFlag) {
			return; 
		}
		
		/* 
		 * Process the temp table to ensure the correct entries for immediate closed supersets and 
		 * immediate closed subsets. 
		 */
		ListIterator<TempTable<T>.Entry> i = tempTbl.listIterator(0); 
		while(i.hasNext()) {
			TempTable<T>.Entry X = i.next(); 
			this.processAdd(tempTbl, X, newItemset, i); 
		}
	}
	
	/** 
	 * Process the temporary table after a new transaction is added to the sliding window. This is done to 
	 * ensure the correct entries for the immediate closed supersets and immediate closed subsets. This 
	 * method is described in the paper "An efficient algorithm for mining frequent closed itemsets" by 
	 * Yen et al. 
	 *  
	 * @param tempTbl    The temporary table 
	 * @param X          The entry of the temporary table representing the current row to be processed. 
	 * @param newItemset The newly added transaction
	 * @param i          A list iterator representing the current position in the temporary table. 
	 */
	private void processAdd(
			TempTable<T> tempTbl, TempTable<T>.Entry X, 
			Itemset<T> newItemset, ListIterator<TempTable<T>.Entry> i) 
	{ 
		X.status = 1; 
		
		if(!i.hasNext()) {
			return; 
		}
		
		ListIterator<TempTable<T>.Entry> j = tempTbl.listIterator(i.nextIndex()); 
	
		ContentTable<T>.Entry closureEntry = this.contentTable.get(X.closureId);
		
		if(X.closureId != -1 && X.itemset.equals(closureEntry.itemset)) {
			while(j.hasNext()) {
				TempTable<T>.Entry X_j = j.next(); 
				if(X.itemset.contains(X_j.itemset)) {
					X_j.status = 1; 
				}
			}
		} else {
			while(j.hasNext()) {
				TempTable<T>.Entry X_j = j.next(); 
				
				if(X.itemset.contains(X_j.itemset)) {
					boolean superflag = false;
					
					ContentTable<T>.Entry contentEntry = this.contentTable.get(X_j.cid);
					for(int s : contentEntry.immediateClosedSupersets) {
						ContentTable<T>.Entry supersetEntry = this.contentTable.get(s); 
						if(X.itemset.contains(supersetEntry.itemset)) {
							superflag = true; 
						}
					}
					
					if(X_j.status == 0 || (!superflag && !newItemset.equals(X.itemset))) {
						this.contentTable.addImmediateClosedSuperset(X_j.cid, X.cid);
						this.contentTable.addImmediateClosedSubset(X.cid, X_j.cid); 
					}
					
					this.processAdd(tempTbl, X_j, newItemset, j); 
				}
			}
		}
		
	}
	
	/** 
	 * Compute all closed subsets of a given content table entry by following the paths of immediate closed 
	 * subsets. 
	 * 
	 * @param entry The content table entry for which the closed subsets shall be computed. 
	 * @return Returns a {@code Set<Integer>} of content table identifiers of all the closed subsets 
	 *         requested. 
	 */
	private Set<Integer> closedSubsets(ContentTable<T>.Entry entry) {
		Set<Integer> subsets = new HashSet<Integer>(entry.immediateClosedSubsets);
		Set<Integer> additional = new HashSet<Integer>(); 
		for(int s : subsets) {
			ContentTable<T>.Entry e = this.contentTable.get(s); 
			additional.addAll(this.closedSubsets(e)); 
		}
		subsets.addAll(additional); 
		return subsets; 
	}

	/** 
	 * {@inheritDoc} This method is described in the paper "An efficient algorithm for mining frequent 
	 * closed itemsets" by Yen et al.  
	 */
	@Override
	public void delete(Itemset<T> X) {
		// First, all the subsets of the deleted transaction get computed and then their support is updated. 
		ContentTable<T>.Entry entryOfX = this.contentTable.getByItemset(X);  
		Set<Integer> subsetsSet = this.closedSubsets(entryOfX);
		
		subsetsSet.add(entryOfX.cid); 
		Map<Integer, Integer> status = new HashMap<>();
		Map<Integer, Integer> lengths = new HashMap<>(); 
		for(int s : subsetsSet) {
			ContentTable<T>.Entry e = this.contentTable.get(s); 
			e.support -= 1; 
			status.put(s, 0); 
			lengths.put(s, e.itemset.size());
		}
		subsetsSet.remove((Integer) entryOfX.cid); 
		
		List<Integer> subsets = new ArrayList<>(subsetsSet);
		subsets.sort(new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				Integer l1 = lengths.get(o1); 
				Integer l2 = lengths.get(o2); 
				return l2.compareTo(l1); 
			}
			
		});
		this.processDelete(entryOfX, status, subsets); 
	}
	
	/** 
	 * Process all the subsets of a deleted transaction and decide whether or not to remove them. Also 
	 * update the immediate closed sub- and superset relations in the content table. 
	 * 
	 * @param entryOfX The content table entry corresponding to the deleted transaction. 
	 * @param status   The status map which subsets are already considered and which are not. 
	 * @param subsets  The list of relevant subsets. 
	 */
	private void processDelete(
			ContentTable<T>.Entry entryOfX, Map<Integer, Integer> status, List<Integer> subsets) 
	{
		if(status.get(entryOfX.cid) == 1) {
			return; 
		}
		status.put(entryOfX.cid, 1); 
		boolean flag = false;
		boolean runElse = false; 
		
		if(entryOfX.support == 0) {
			// if the support drops to zero then the content table entry shall be removed 
			this.contentTable.delete(entryOfX.cid);
			this.itemTable.delete(entryOfX.cid, entryOfX.itemset); 
			if(subsets != null) {
				for(int s : subsets) {
					ContentTable<T>.Entry S = this.contentTable.get(s); 
					this.processDelete(S, status, null);
				}
			}
		} else if(entryOfX.immediateClosedSupersets.size() == 1) {
			/* 
			 * if there is exactly one immediate closed superset then we have to decide whether or not the 
			 * processed itemset shall be removed or not and whether or not to consider the subsets. This 
			 * is done according to the theoretical results of the paper.   
			 */			
			int cidOfY = entryOfX.immediateClosedSupersets.get(0); 
			ContentTable<T>.Entry entryOfY = this.contentTable.get(cidOfY); 
			if(entryOfX.support - entryOfY.support == 0) {
				for(int s : entryOfX.immediateClosedSubsets) {
					flag = false; 
					ContentTable<T>.Entry entryOfS = this.contentTable.get(s); 
					for(int r : entryOfS.immediateClosedSupersets) {
						if(r == entryOfX.cid) continue; 
						ContentTable<T>.Entry entryOfR = this.contentTable.get(r); 
						if(this.contentTable.hasPath(entryOfR, entryOfY)) {
							flag = true; 
						}
					}
					if(!flag) {
						entryOfS.immediateClosedSupersets.add(entryOfY.cid); 
						entryOfY.immediateClosedSubsets.add(entryOfS.cid); 
					}
				}
				this.contentTable.delete(entryOfX.cid);
				this.itemTable.delete(entryOfX.cid, entryOfX.itemset); 
				if(subsets != null) {
					for(int s : subsets) {
						ContentTable<T>.Entry S = this.contentTable.get(s); 
						this.processDelete(S, status, null);
					}
				}
			} else {
				runElse = true; 
			}
		} else {
			runElse = true; 
		}
		
		if(runElse) {
			// otherwise the visited subsets can be marked as visited 
			Set<Integer> U = this.closedSubsets(entryOfX); 
			for(int j : U) {
				status.put(j, 1); 
			}
		}
	}
	
	public String toString() {
		String res = "Content Table: \n"; 
		res += this.contentTable + "\n\n"; 
		res += "Item Table: \n"; 
		res += this.itemTable + "\n\n"; 
		return res; 
	}
	
	/**
	 * Compute a list of closed itemsets from the current content table.
	 * @return A list comprising the closed itemsets. 
	 */
	@Override
	public Set<Itemset<T>> getClosedItemsets() {
		return this.getClosedFrequentItemsets(0); 
	}

	@Override
	public Set<Itemset<T>> getClosedFrequentItemsets(int t) { 
		return this.contentTable.getClosedFrequentItemsets(t); 
	}

	@Override
	public int support(Itemset<T> X) {
		return this.contentTable.support(X); 
	}
	
	public static void main(String args[]) {
		/*// Example 1
		Itemset<String> i1 = new Itemset<String>("A", "C", "T", "W"); 
		Itemset<String> i2 = new Itemset<String>("C", "D", "W"); 
		Itemset<String> i3 = new Itemset<String>("A", "C", "T", "W"); 
		Itemset<String> i4 = new Itemset<String>("A", "C", "D", "W"); 
		Itemset<String> i5 = new Itemset<String>("A", "C", "D", "T", "W"); 
		Itemset<String> i6 = new Itemset<String>("C", "D", "T"); 
		
		MFCI<String> mfci = new MFCI<String>(); 
		
		mfci.add(i1);
		mfci.add(i2);
		mfci.add(i3);
		mfci.add(i4);
		mfci.add(i5);
		mfci.add(i6);
		mfci.delete(i1);
		mfci.delete(i2);
		mfci.delete(i3);
		mfci.delete(i4);
		mfci.delete(i5);
		mfci.delete(i6);
		
		System.out.println("Content Table: ");
		System.out.println(mfci.contentTable);
		System.out.println("Item Table: ");
		System.out.println(mfci.itemTable); //*/
		
		/*// Example 2
		Itemset<String> i1 = new Itemset<String>("M", "O", "N", "K", "E", "Y"); 
		Itemset<String> i2 = new Itemset<String>("D", "O", "N", "K", "E", "Y"); 
		Itemset<String> i3 = new Itemset<String>("M", "A", "K", "E"); 
		Itemset<String> i4 = new Itemset<String>("M", "U", "C", "K", "Y"); 
		Itemset<String> i5 = new Itemset<String>("C", "O", "K", "E"); 
		
		MFCI<String> mfci = new MFCI<String>(); 
		
		mfci.add(i1);
		mfci.add(i2);
		mfci.add(i3);
		mfci.delete(i1); 
		mfci.add(i4);
		mfci.delete(i2); 
		mfci.add(i5);
		
		System.out.println(mfci.getClosedItemsets());
		System.out.println(mfci); //*/
		
		/*// Example 3
		Itemset<Character> i1 = new Itemset<Character>('a', 'b', 'd', 'e'); 
		Itemset<Character> i2 = new Itemset<Character>('b', 'c', 'e'); 
		Itemset<Character> i3 = new Itemset<Character>('a', 'b', 'd', 'e'); 
		Itemset<Character> i4 = new Itemset<Character>('a', 'b', 'c', 'e'); 
		Itemset<Character> i5 = new Itemset<Character>('a', 'b', 'c', 'd', 'e'); 
		Itemset<Character> i6 = new Itemset<Character>('b', 'c', 'd');
		
		MFCI<Character> mfci = new MFCI<Character>();
		
		mfci.add(i1);
		mfci.add(i2);
		mfci.add(i3);
		mfci.add(i4);
		mfci.add(i5);
		mfci.delete(i1);
		mfci.add(i6);
	
		System.out.println(mfci);
		System.out.println(mfci.getClosedFrequentItemsets(3)); //*/

		/*// Example 4
		Itemset<String> i1 = new Itemset<String>("M", "A", "K", "E"); 
		Itemset<String> i2 = new Itemset<String>("C", "O", "K", "E"); 
		Itemset<String> i3 = new Itemset<String>("M", "O", "N", "K", "E", "Y"); 
		Itemset<String> i4 = new Itemset<String>("D", "O", "N", "K", "E", "Y"); 
		Itemset<String> i5 = new Itemset<String>("M", "U", "C", "K", "Y"); 
		
		MFCI<String> mfci = new MFCI<String>();
		
		mfci.add(i1);
		mfci.add(i2);
		mfci.add(i3);
		mfci.add(i4);
		mfci.delete(i1);
		mfci.add(i5); //*/
	}
}
