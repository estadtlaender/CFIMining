package de.lab.CFIMining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import de.lab.CFIMining.CFIStream.DirectUpdateTree;
import de.lab.CFIMining.MFCI.MFCI;
import de.lab.CFIMining.StreamFCI.StreamFCI;

/** 
 * A class which implements the measurements and experiments needed to compare the algorithms.  
 * 
 * @author Eike Stadtlaender
 *
 */
public class Measurements {
	
	/** 
	 * Load an dataset from a CSV file where the items are integers. 
	 * 
	 * @param filename 
	 * @return Returns a {@code List<Itemset<Integer>>} containing the dataset. 
	 * @throws Exception The methods throws an exception if one of the file operations fail. 
	 */
	private static List<Itemset<Integer>> loadIntegerDataset(String filename) throws Exception {
		List<Itemset<Integer>> tdb = new ArrayList<Itemset<Integer>>(); 
		
		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line; 
			// load the file line by line, split the resulting string by comma as separator and parse 
			// the result as an Integer
			while(null != (line = br.readLine())) {
				String[] splittedLine = line.split(","); 
				Set<Integer> items = new HashSet<Integer>(); 
				for(String itemString : splittedLine) {
					int item = Integer.parseInt(itemString); 
					items.add(item); 
				}
				tdb.add(new Itemset<>(items)); 
			}
		}
		
		return tdb; 
	}
	
	/** 
	 * Wrapper for {@link Measurements#loadIntegerDataset(String)} to load the T10I4D100K dataset. 
	 * 
	 * @return Returns a {@code List<Itemset<Integer>>} which contains the T10I4D100K dataset. 
	 * @throws Exception
	 */
	public static List<Itemset<Integer>> loadT10I4D100K() throws Exception {
		return loadIntegerDataset("resources/T10I4D100K.csv"); 
	}
	
	/** 
	 * Wrapper method for {@link Measurements#loadIntegerDataset(String)} to load the T40I10D100K dataset.
	 * 
	 * @return Returns a {@code List<Itemset<Integer>>} which contains the T40I10D100K dataset. 
	 * @throws Exception
	 */
	public static List<Itemset<Integer>> loadT40I10D100K() throws Exception {
		return loadIntegerDataset("resources/T40I10D100K.csv"); 
	}
	
	/** 
	 * Wrapper method for {@link Measurements#loadIntegerDataset(String)} to load the Retail dataset. 
	 * 
	 * @return Returns a {@code List<Itemset<Integer>>} which contains the Retail dataset. 
	 * @throws Exception
	 */
	public static List<Itemset<Integer>> loadRetail() throws Exception {
		return loadIntegerDataset("resources/retail.csv");  
	}
	
	/** 
	 * Wrapper method for {@link Measurements#loadIntegerDataset(String)} to load a shuffled version of the 
	 * Retail dataset. 
	 * 
	 * @return Returns a {@code List<Itemset<Integer>>} which contains the shuffled version of the Retail 
	 *         dataset. 
	 * @throws Exception
	 */
	public static List<Itemset<Integer>> loadRetailShuffled() throws Exception {
		return loadIntegerDataset("resources/retail_shuffled.csv");  
	}
	
	/** 
	 * Wrapper method for {@link Measurements#loadIntegerDataset(String)} to load the Kosarak dataset. 
	 * 
	 * @return Returns a {@code List<Itemset<Integer>>} which contains the Kosarak dataset. 
	 * @throws Exception
	 */
	public static List<Itemset<Integer>> loadKosarak() throws Exception {
		return loadIntegerDataset("resources/kosarak.csv");  
	}
	
	/** 
	 * Load the mushroom dataset.  
	 * 
	 * @return Returns a {@code List<Itemset<Integer>>} which contains the Mushroom dataset. 
	 * @throws Exception
	 */
	public static List<Itemset<Integer>> loadMushroom() throws Exception {
		List<Itemset<Integer>> tdb = new ArrayList<>();
		
		try(BufferedReader br = new BufferedReader(new FileReader("resources/mushroom.csv"))) {
			String line;
			br.readLine(); // throw away the first line which is a descriptor of the dataset 
			// load the dataset line by line and split the string by colon as a separator 
			while(null != (line = br.readLine())) {
				String[] splittedLine = line.split(";");
				Set<Integer> items = new HashSet<>();
				int counter = 0; 
				for(String itemString : splittedLine) { 
					/* 
					 * The Mushroom dataset has several columns which can contain the same characters but 
					 * describe different properties of the mushroom. Therefore, the integer value of the 
					 * character is used in the 16 right most bits of an integer while the column number 
					 * is encoded in the 16 left most bits of an integer. 
					 * Since all the other datasets use integers as item representation this is preferred 
					 * over using string representations or other approaches in order to make the results 
					 * more comparable. 
					 */
					int value;
					if(itemString.isEmpty()) {
						value = 0; 
					} else {
						value = itemString.charAt(0); 
					}
					value |= (counter << 16); 
					items.add(value);
					counter += 1; 
				}  
				tdb.add(new Itemset<>(items)); 
			}
		}
		
		return tdb; 
	}
	
	/** 
	 * Load the first 100000 transactions from the Poker dataset. 
	 * 
	 * @return Returns a {@code List<Itemset<Integer>>} which contains the Poker dataset (the first 10000 
	 *         entries). 
	 * @throws Exception
	 */
	public static List<Itemset<Integer>> loadPoker() throws Exception {
		List<Itemset<Integer>> tdb = new ArrayList<>(); 
		
		try(BufferedReader br = new BufferedReader(new FileReader("resources/poker-hand.csv"))) {
			String line; 
			br.readLine(); // throw array the first line which is a descriptor of the dataset
			int counter = 0; 
			// Read the dataset line by line and split according to commas used as a separator 
			while(null != (line = br.readLine()) && counter < 100000) {
				String[] splittedLine = line.split(","); 
				Set<Integer> items = new HashSet<>(); 
				for(int i = 0; i+1 < splittedLine.length; i += 2) {
					/* 
					 * In this dataset two consecutive columns describe one card (suit and rank of a card) 
					 * Those two properties are described by small integers (1-4 and 1-13). One integer is 
					 * computed to represent a card uniquely. The 16 left most bits of the integer are used 
					 * to encode the suit of the card while the 16 right most bits of the integer are used 
					 * to encode the rank of the card. In this way each transaction (i.e. Poker hand) is 
					 * encoded by five integers. All transactions have the same size. 
					 */
					int suit = Integer.parseInt(splittedLine[i]); 
					int rank = Integer.parseInt(splittedLine[i+1]); 
					int value = (suit << 16) | rank; 
					items.add(value); 
				}
				tdb.add(new Itemset<>(items));
				counter += 1; 
			}
		}
		
		return tdb; 
	}
	
	/** 
	 * Perform one measurement of the experiment with a given set of parameters. 
	 * 
	 * @param alg               The {@code SlidingWindowAlgorithm<T>} used in this measurement. 
	 * @param tdb               The transaction stream used in this measurement. 
	 * @param slidingWindowSize The sliding window size used in this measurement. 
	 * @return Returns a {@Map<Integer, Double>} containing the running time measurements of this 
	 *         experiment. 
	 */
	public static <T extends Comparable<T>> SortedMap<Integer, Double> measure(
			SlidingWindowAlgorithm<T> alg, 
			List<Itemset<T>> tdb, int slidingWindowSize) 
	{
		List<Long> measurements = new LinkedList<Long>(); 
		
		// take the measurements, process each transaction in the transaction database. 
		for(int i = 0; i < tdb.size(); i++) {
			if(i % slidingWindowSize == 0) {
				// measure time and add to list of measurements. 
				measurements.add(System.nanoTime()); 
				// Feedback for the curious observer waiting for the results :-D 
				System.out.println(i + ": " + System.nanoTime());
			}
			
			/* 
			 * The actual experiment is done by deleting one old transaction (if the sliding window size is 
			 * achieved) and adding one new transaction. 
			 */
			Itemset<T> itemset = tdb.get(i); 
			if(i >= slidingWindowSize) {
				Itemset<T> oldItemset = tdb.get(i - slidingWindowSize);
				alg.delete(oldItemset);
			}
			alg.add(itemset);
		}
		// measure the time after the last transaction was processed 
		Long stop = System.nanoTime();
		Long start = measurements.get(0); 
		
		/* 
		 * The measurements are given in nano seconds from the Java Virtual Machine's high resolution time 
		 * source. Here the results are converted into seconds and put in a sorted map which maps the number 
		 * of transactions processed to the time passed. (From this, the time used for each advance of the 
		 * sliding window is computed as the increment in time between measurements.) 
		 */
		SortedMap<Integer, Double> results = new TreeMap<Integer, Double>(); 
		Iterator<Long> iter = measurements.iterator(); 
		for(int i = 0; i < tdb.size() && iter.hasNext(); i += slidingWindowSize) {
			results.put(i, 1e-9 * (iter.next() - start));
		}
		results.put(tdb.size(), 1e-9 * (stop - start)); 

		return results; 
	}
	
	/** 
	 * Perform an actual experiment with a given set of parameters. 
	 * 
	 * @param experimentName     The name of the experiment (determines the filenames for better 
	 *                           organization)
	 * @param alg                The {@code SlidingWindowAlgorithm<T>} to be used in this experiment. 
	 * @param tdb                The transaction stream used in this experiment. 
	 * @param slidingWindowSizes A {@code Set<T>} of sliding window sizes. The Experiment is repeated for 
	 *                           each sliding window size. 
	 * @throws Exception
	 */
	public static <T extends Comparable<T>> void makeExperiment(
			String experimentName, SlidingWindowAlgorithm<T> alg, 
			List<Itemset<T>> tdb, Set<Integer> slidingWindowSizes) throws Exception
	{
		for(int slidingWindowSize : slidingWindowSizes) {
			String filename = "results/" + experimentName + "_" + Integer.toString(slidingWindowSize) + ".txt"; 
			File file = new File(filename); 
			if(file.exists() && file.isFile()) 
			{
				// Don't repeat the experiments. This is done to enable an interruption of the experiments 
				// in the worst case. 
				System.out.println("Experiment \"" + filename + "\" already done. Skipped!");  
			} else {
				SortedMap<Integer, Double> results = measure(alg, tdb, slidingWindowSize);
				saveResults(filename, results); 
			}
		} 
	}

	/** 
	 * Save the given set of results in a file on the hard drive. 
	 * 
	 * @param filename The filename in which the results are to be saved. 
	 * @param results  The set of results. 
	 * @throws Exception An exception is thrown if the file system operations fail. 
	 */
	public static <T extends Comparable<T>> void saveResults(
			String filename, SortedMap<Integer, Double> results) throws Exception 
	{
		// Make sure that the file and its parent directories exist. 
		File file = new File(filename);
		file.delete(); 
		file.getParentFile().mkdirs(); 
		file.createNewFile(); 
		// Write the results to the file, line by line 
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			for(int key : results.keySet()) {
				double value = results.get(key); 
				String line = key + " " + value + "\n"; 
				writer.write(line); 
			}
		}
	}
	
	/** 
	 * Compose and perform the actual experiments for the different datasets. 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {		
		Set<Integer> slidingWindows; 
		
		// T10I4D100K
		List<Itemset<Integer>> T10I4D100K = loadT10I4D100K(); 
		slidingWindows = new TreeSet<Integer>(Arrays.asList(250, 500, 1000, 2500)); 
		makeExperiment("MFCI_T10I4D100K", new MFCI<>(), T10I4D100K, slidingWindows); 
		makeExperiment("DIU_T10I4D100K", new DirectUpdateTree<>(), T10I4D100K, slidingWindows); 
		makeExperiment("StreamFCI_T10I4D100K", new StreamFCI<>(), T10I4D100K, slidingWindows); 
		
		// Retail 
		List<Itemset<Integer>> retail = loadRetail(); 
		slidingWindows = new TreeSet<Integer>(Arrays.asList(250, 500, 1000, 2500)); 
		makeExperiment("MFCI_Retail", new MFCI<>(), retail, slidingWindows);  
		makeExperiment("DIU_Retail", new DirectUpdateTree<>(), retail, slidingWindows); 
		makeExperiment("StreamFCI_Retail", new StreamFCI<>(), retail, slidingWindows); 
		
		// T40I10D10K
		// XXX not performant enough. Algorithms depend on transaction length!  
		List<Itemset<Integer>> T40I10D100K = loadT40I10D100K(); 
		List<Itemset<Integer>> T40I10D10K = T40I10D100K.subList(0, 10000); 
		slidingWindows = new HashSet<Integer>(Arrays.asList(250));
		makeExperiment("MFCI_T40I10D10K", new MFCI<>(), T40I10D10K, slidingWindows); 
		makeExperiment("StreamFCI_T40I10D10K", new StreamFCI<>(), T40I10D10K, slidingWindows);
		/* 
		 * The experiment with the CFIStream algorithm is omitted because it would take approximately 
		 * 24 hours and the algorithm in this setting is clearly inferior to the other two. 
		 */ 
		// makeExperiment("CFIStream_T40I10D10K", new DirectUpdateTree<>(), T40I10D10K, slidingWindows); 

		// Retail (Shuffled)
		List<Itemset<Integer>> retailShuffled = loadRetailShuffled();
		slidingWindows = new TreeSet<Integer>(Arrays.asList(250, 500, 1000, 2500));
		makeExperiment("MFCI_Retail_shuffled", new MFCI<>(), retailShuffled, slidingWindows);
		makeExperiment("DIU_Retail_shuffled", new DirectUpdateTree<>(), retailShuffled, slidingWindows);
		makeExperiment("StreamFCI_Retail_shuffled", new StreamFCI<>(), retailShuffled, slidingWindows); 
		
		// Mushroom 
		// XXX not performant enough, itemset size 23 is too large for large sliding windows :-( 
//		List<Itemset<Integer>> mushroom = loadMushroom();
//		slidingWindows = new TreeSet<Integer>(Arrays.asList(250, 500, 1000, 2500)); 
//		makeExperiment("MFCI_Mushroom", new MFCI<>(), mushroom, slidingWindows); 
//		makeExperiment("DIU_Mushroom", new DirectUpdateTree<>(), mushroom, slidingWindows);
//		makeExperiment("StreamFCI_Mushroom", new StreamFCI<>(), mushroom, slidingWindows); 
		
		// Poker (only the first 100k poker hands are used) 
		List<Itemset<Integer>> poker = loadPoker(); 
		slidingWindows = new TreeSet<Integer>(Arrays.asList(25, 50, 100, 250, 500, 1000, 2500)); 
		makeExperiment("MFCI_Poker", new MFCI<>(), poker, slidingWindows); 
		makeExperiment("DIU_Poker", new DirectUpdateTree<>(), poker, slidingWindows); 
		makeExperiment("StreamFCI_Poker", new StreamFCI<>(), poker, slidingWindows); 
	}
	
}
