package de.lab.CFIMining.StreamFCI;

import java.util.Set;

import de.lab.CFIMining.Itemset;
import de.lab.CFIMining.SlidingWindowAlgorithm;

/*
 * FIXME: This implementation of the StreamFCI algorithm does NOT make use of the FCITable which increases 
 *        the running time needed for getting the closed frequent itemsets and decreases the running time 
 *        for adding and deleting transactions slightly. 
 *        
 *        Furthermore the computation of the support is not implemented correctly right now.  
 */

/** 
 * A class implementing the StreamFCI algorithm as described in the paper "A Novel Strategy for Mining 
 * Frequent Closed Itemsets in Data Streams" by Tang et al. 
 * 
 * <b>Note:</b> 
 * 
 * @author Eike Stadtlaender
 *
 * @param <T>
 */
public class StreamFCI<T extends Comparable<T>> implements SlidingWindowAlgorithm<T> {
	
	private DFPTree<T> dfpTree; 
	private FCITable<T> fciTable; 

	/** 
	 * Construct a new instance of the StreamFCI algorithm with an empty dynamical frequent pattern tree and 
	 * an empty frequent closed itemset table. 
	 */
	public StreamFCI() {
		this.dfpTree = new DFPTree<T>(); 
		this.fciTable = new FCITable<T>(this.dfpTree); 
	}
	
	/** 
	 * @deprecated Not yet implemented correctly 
	 */
	@Override
	public int support(Itemset<T> X) {
		// TODO Auto-generated method stub
		return dfpTree.support(X);
	}
	
	@Override
	public void add(Itemset<T> X) {
		this.dfpTree.add(X); 
		this.fciTable.add(X); 
	}

	@Override
	public void delete(Itemset<T> X) {
		this.dfpTree.delete(X);
		this.fciTable.delete(X);
	}

	@Override
	public Set<Itemset<T>> getClosedItemsets() {
		// TODO Auto-generated method stub
		return this.dfpTree.getClosedItemsets();
	}

	/** 
	 * @deprecated Not implemented yet. 
	 */
	@Override
	public Set<Itemset<T>> getClosedFrequentItemsets(int t) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override 
	public String toString() {
		String string = "DFPTree: \n" + this.dfpTree.toString() + "\n";
		string += "FCITable: \n" + this.fciTable.toString() + "\n"; 
		return string; 
	}
	
	public static void main(String[] args) {
		/*// Example 1
		StreamFCI<Character> streamFci = new StreamFCI<>(); 
		
		Itemset<Character> i1 = new Itemset<>('b', 'c'); 
		Itemset<Character> i2 = new Itemset<>('a', 'b'); 
		Itemset<Character> i3 = new Itemset<>('a', 'c', 'd'); 
		Itemset<Character> i4 = new Itemset<>('a', 'c', 'd'); 
		Itemset<Character> i5 = new Itemset<>('a', 'b', 'd'); 
		
		streamFci.add(i1);
		streamFci.add(i2);
		streamFci.add(i3);
		streamFci.add(i4);
		streamFci.add(i5);
		streamFci.delete(i1); 
		System.out.println(streamFci.getClosedItemsets());  //*/
		
		/*// Example 2
		StreamFCI<Character> streamFci = new StreamFCI<>(); 

		Itemset<Character> i1 = new Itemset<>('C', 'D'); 
		Itemset<Character> i2 = new Itemset<>('A', 'B', 'C'); 
		Itemset<Character> i3 = new Itemset<>('A', 'B'); 
		
		streamFci.add(i1);
		streamFci.add(i2);
		streamFci.add(i3); 
		System.out.println(streamFci.getClosedItemsets()); //*/
		
		/*// Example 3
		SlidingWindowAlgorithm<Character> fci = new StreamFCI<>();
		SlidingWindowAlgorithm<Character> mfci = new MFCI<>(); 
		
				
		Itemset<Character> i1 = new Itemset<>('A', 'C', 'T', 'W');
		Itemset<Character> i2 = new Itemset<>('C', 'D', 'W');
		Itemset<Character> i3 = new Itemset<>('A', 'C', 'T', 'W');
		Itemset<Character> i4 = new Itemset<>('A', 'C', 'D', 'W');
		Itemset<Character> i5 = new Itemset<>('A', 'C', 'D', 'T', 'W');
		Itemset<Character> i6 = new Itemset<>('C', 'D', 'T');
		
		fci.add(i1); 
		mfci.add(i1);
		fci.add(i2);
		mfci.add(i2);
		fci.add(i3); 
		mfci.add(i3);
		fci.add(i4); 
		mfci.add(i4);
		fci.delete(i1);
		mfci.delete(i1);
		fci.add(i5); 
		mfci.add(i5);
		fci.delete(i2);
		mfci.delete(i2);
		fci.add(i6); 
		mfci.add(i6);
		System.out.println(fci.getClosedItemsets()); 
		System.out.println(mfci.getClosedItemsets()); //*/
	
		/* // Example 3
		SlidingWindowAlgorithm<Character> fci = new StreamFCI<>();
		

		Itemset<Character> i1 = new Itemset<>('A', 'C', 'D', 'T', 'W');
		Itemset<Character> i2 = new Itemset<>('C', 'D', 'W');
		Itemset<Character> i3 = new Itemset<>('C', 'D', 'T'); 
		
		fci.add(i1);
		fci.add(i2);
		fci.add(i3);
		
		System.out.println(fci.getClosedItemsets()); 
		//*/
		
		/*// Example 4
		SlidingWindowAlgorithm<Integer> fci = new StreamFCI<>(); 
		SlidingWindowAlgorithm<Integer> mfci = new MFCI<>(); 
		
		Itemset<Integer> i1 = new Itemset<>(71, 279); 
		Itemset<Integer> i2 = new Itemset<>(217); 
		Itemset<Integer> i3 = new Itemset<>(71, 217, 279); 

		fci.add(i1);
		fci.add(i2);
		fci.add(i3);
		
		mfci.add(i1);
		mfci.add(i2);
		mfci.add(i3);
		
		System.out.println(fci.getClosedItemsets()); 
		System.out.println(mfci.getClosedItemsets()); //*/
	}

}
