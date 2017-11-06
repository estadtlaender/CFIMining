package de.lab.CFIMining;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.lab.CFIMining.CFIStream.DirectUpdateTree;
import de.lab.CFIMining.MFCI.MFCI;
import de.lab.CFIMining.StreamFCI.StreamFCI;

/** 
 * A test case to test that the different algorithms produce consistent results. Depending on a flag set at 
 * compile time, the test case tests the MFCI algorithm versus the CFIStream algorithm and the MFCI 
 * algorithm versus the StreamFCI algorithm with or without taking the support into account. 
 * 
 * @author Eike Stadtlaender
 * 
 */
public class TestAlgorithms {
	
	/** 
	 * The flag which determines whether or not the support is also compared. 
	 */
	private static boolean testSupport = true; 
	
	/** 
	 * The flag which determines whether the MFCI algorithm is also tested versus the StreamFCI algorithm. 
	 */
	private static boolean testStreamFCI = false; 

	// Several example streams to be used for the unit testing. 
	private List<Itemset<Character>> tdbDIUExample; 
	private List<Itemset<Character>> tdbDIUExampleModified; 
	private List<Itemset<Character>> tdbMFCIExample; 
	private List<Itemset<Character>> tdbLectureExample1; 
	private List<Itemset<Character>> tdbLectureExample2;
	private List<Itemset<Integer>> tdbLexicoraphicalOrderExample; 
	private List<Itemset<Integer>> tdbT10I4D100K; 
	
	/** 
	 * A static variable determining the number of repetitions in the randomized tests. The number is 
	 * probably chosen too large right now. 
	 */
	private static int repetitions = 200; 

	@Before
	public void setUp() throws Exception {
		// set up the example from the CFI-Stream paper
		this.tdbDIUExample = new ArrayList<Itemset<Character>>(); 
		this.tdbDIUExample.add(new Itemset<>('C', 'D')); 
		this.tdbDIUExample.add(new Itemset<>('A', 'B')); 
		this.tdbDIUExample.add(new Itemset<>('A', 'B', 'C')); 
		this.tdbDIUExample.add(new Itemset<>('A', 'B', 'C')); 
		
		this.tdbDIUExampleModified = new ArrayList<Itemset<Character>>(); 
		this.tdbDIUExampleModified.add(new Itemset<>('C'));
		this.tdbDIUExampleModified.add(new Itemset<>('C', 'D'));
		this.tdbDIUExampleModified.add(new Itemset<>('A', 'B')); 
		this.tdbDIUExampleModified.add(new Itemset<>('A', 'B', 'C')); 
		this.tdbDIUExampleModified.add(new Itemset<>('A', 'B', 'C')); 
		
		// set up the example from the MFCI paper 
		this.tdbMFCIExample = new ArrayList<Itemset<Character>>(); 
		this.tdbMFCIExample.add(new Itemset<>('A', 'C', 'T', 'W')); 
		this.tdbMFCIExample.add(new Itemset<>('C', 'D', 'W')); 
		this.tdbMFCIExample.add(new Itemset<>('A', 'C', 'T', 'W')); 
		this.tdbMFCIExample.add(new Itemset<>('A', 'C', 'D', 'W')); 
		this.tdbMFCIExample.add(new Itemset<>('A', 'C', 'D', 'T', 'W')); 
		this.tdbMFCIExample.add(new Itemset<>('C', 'D', 'T')); 
		
		// set up the first example from the lecture 
		this.tdbLectureExample1 = new ArrayList<Itemset<Character>>(); 
		this.tdbLectureExample1.add(new Itemset<>('M', 'O', 'N', 'K', 'E', 'Y')); 
		this.tdbLectureExample1.add(new Itemset<>('D', 'O', 'N', 'K', 'E', 'Y')); 
		this.tdbLectureExample1.add(new Itemset<>('M', 'A', 'K', 'E')); 
		this.tdbLectureExample1.add(new Itemset<>('M', 'U', 'C', 'K', 'Y')); 
		this.tdbLectureExample1.add(new Itemset<>('C', 'O', 'K', 'E')); 

		// set up the second example from the lecture 
		this.tdbLectureExample2 = new ArrayList<Itemset<Character>>(); 
		this.tdbLectureExample2.add(new Itemset<>('a', 'b', 'd', 'e'));  
		this.tdbLectureExample2.add(new Itemset<>('b', 'c', 'e'));  
		this.tdbLectureExample2.add(new Itemset<>('a', 'b', 'd', 'e'));  
		this.tdbLectureExample2.add(new Itemset<>('a', 'b', 'c', 'e'));  
		this.tdbLectureExample2.add(new Itemset<>('a', 'b', 'c', 'd', 'e'));  
		this.tdbLectureExample2.add(new Itemset<>('b', 'c', 'd'));
		
		// set up the example arised from the lexicographical order 
		this.tdbLexicoraphicalOrderExample = new ArrayList<>(); 
		this.tdbLexicoraphicalOrderExample.add(new Itemset<>(1, 2)); 
		this.tdbLexicoraphicalOrderExample.add(new Itemset<>(2, 3));
		this.tdbLexicoraphicalOrderExample.add(new Itemset<>(3)); 
		this.tdbLexicoraphicalOrderExample.add(new Itemset<>(1, 2)); 
		
		// set up T10I4D100K dataset 
		this.tdbT10I4D100K = new ArrayList<Itemset<Integer>>(); 
		String filename = "resources/T10I4D100K.csv"; 
		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
			int counter = 0; 
			String line; 
			while(counter < 4000 && null != (line = br.readLine())) {
				String[] splittedLine = line.split(",");
				Set<Integer> items = new HashSet<Integer>(); 
				for(String s : splittedLine) {
					int i = Integer.parseInt(s); 
					items.add(i); 
				}
				Itemset<Integer> itemset = new Itemset<Integer>(items);
				this.tdbT10I4D100K.add(itemset); 
				counter += 1; 
			}
		}
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/** 
	 * Make a test of two given algorithms on a given transaction stream with a given set of parameters. 
	 * 
	 * @param testName          The name of the test which is used for output. 
	 * @param alg1              The first {@code SlidingWindowAlgorithm<T>} to be tested 
	 * @param alg2              The second {@code SlidingWindowAlgorithm<T>} to be tested against the first
	 * @param tdb               The transaction stream or database 
	 * @param num               The maximum number of transactions to be considered from the transaction 
	 *                          stream
	 * @param slidingWindowSize The size of the sliding window during the test
	 * @param checkInterval     The interval after which the consistency is checked (e.g. every 5th 
	 *                          iteration) 
	 *                          
	 * @param <T>               The type of the items in the transaction stream
	 */
	public <T extends Comparable<T>> void tdbTest(
			String testName, 
			SlidingWindowAlgorithm<T> alg1, SlidingWindowAlgorithm<T> alg2, 
			List<Itemset<T>> tdb, int num, int slidingWindowSize, int checkInterval) 
	{
		for(int i = 0; i < Math.min(num, tdb.size()); i++) {
			Itemset<T> itemset = tdb.get(i);
			if(i >= slidingWindowSize) {
				Itemset<T> oldItemset = tdb.get(i-slidingWindowSize); 
				alg1.delete(oldItemset);
				alg2.delete(oldItemset);
				
				if((i+1)%checkInterval == 0) {
					Set<Itemset<T>> alg1Closed = alg1.getClosedItemsets(); 
					Set<Itemset<T>> alg2Closed = alg2.getClosedItemsets(); 
					assertEquals(
							testName + ": The closed itemsets do not coincide. (delete) "
									+ "(No. of transactions = " + num + ", "
									+ "sliding window size = " + slidingWindowSize + ")" 
									+ tdb.subList(i-slidingWindowSize, i),  
							alg1Closed, 
							alg2Closed);
					
					if(TestAlgorithms.testSupport) {
						for(Itemset<T> X : alg1Closed) {
							assertEquals(testName + ": The support of closed itemsets does not coincide.", 
									alg1.support(X), 
									alg2.support(X)); 
						}
					}
				}
			}
			
			alg1.add(itemset);
			alg2.add(itemset);
			
			if((i+1)%checkInterval == 0) {
				System.out.println(testName + ": No. of transactions processed is " + (i+1));
				Set<Itemset<T>> alg1Closed = alg1.getClosedItemsets(); 
				Set<Itemset<T>> alg2Closed = alg2.getClosedItemsets(); 
				assertEquals(
						testName + ": The closed itemsets do not coincide. (add)"
								+ "(No. of transactions = " + num + ", "
								+ "sliding window size = " + slidingWindowSize + ")"
								+ String.join("\n", tdb.subList(Math.max(0, i-slidingWindowSize), i+1)
										.stream().map(s -> s.toString()).collect(Collectors.toList())),  
						alg1Closed, 
						alg2Closed);
				 
				if(TestAlgorithms.testSupport) {
					for(Itemset<T> X : alg1Closed) {
						assertEquals(testName + ": The support of closed itemsets does not coincide.", 
								alg1.support(X), 
								alg2.support(X)); 
					}
				}
			}
		}
	}
	
	@Test
	public void test_DIUExample_addOnly_all() {
		this.tdbTest(
				"MFCI_vs_CFIStream_DIUExample_addOnly", new MFCI<>(), new DirectUpdateTree<>(), 
				this.tdbDIUExample, this.tdbDIUExample.size(), this.tdbDIUExample.size(), 1);
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_DIUExample_addOnly", new MFCI<>(), new StreamFCI<>(), 
					this.tdbDIUExample, this.tdbDIUExample.size(), this.tdbDIUExample.size(), 1);
		}
	}
	
	@Test 
	public void test_DIUExample_severalSlidingWindows() {
		for(int i = 1; i <= this.tdbDIUExample.size(); i++) {
			this.tdbTest(
					"MFCI_vs_CFIStream_DIUExample_slidingWindow_" + i, 
					new MFCI<>(), new DirectUpdateTree<>(),
					this.tdbDIUExample, this.tdbDIUExample.size(), i, 1);
			if(TestAlgorithms.testStreamFCI) {
				this.tdbTest(
						"MFCI_vs_StreamFCI_DIUExample_slidingWindow_" + i, 
						new MFCI<>(), new StreamFCI<>(),
						this.tdbDIUExample, this.tdbDIUExample.size(), i, 1);
			}
		}
	}
	
	@Test 
	public void test_DIUExample_severalSlidingWindows_randomized() {
		List<Itemset<Character>> tdbRandomized = new ArrayList<>(this.tdbDIUExample); 
		for(int j = 0; j < TestAlgorithms.repetitions; j++) {
			Collections.shuffle(tdbRandomized);
			for(int i = 1; i <= tdbRandomized.size(); i++) {
				this.tdbTest(
						"MFCI_vs_CFIStream_DIUExample_slidingWindow_" + i + "_randomized_" + j, 
						new MFCI<>(), new DirectUpdateTree<>(),
						tdbRandomized, tdbRandomized.size(), i, 1);
				if(TestAlgorithms.testStreamFCI) {
					this.tdbTest(
							"MFCI_vs_StreamFCI_DIUExample_slidingWindow_" + i + "_randomized_" + j, 
							new MFCI<>(), new StreamFCI<>(),
							tdbRandomized, tdbRandomized.size(), i, 1);
				}
			}
		}
	}
	
	@Test 
	public void test_DIUExample2_severalSlidingWindows() {
		for(int i = 1; i <= this.tdbDIUExampleModified.size(); i++) {
			this.tdbTest(
					"MFCI_vs_CFIStream_DIUExample2_slidingWindow_" + i, 
					new MFCI<>(), new DirectUpdateTree<>(),
					this.tdbDIUExampleModified, this.tdbDIUExampleModified.size(), i, 1);
			if(TestAlgorithms.testStreamFCI) {
				this.tdbTest(
						"MFCI_vs_StreamFCI_DIUExample2_slidingWindow_" + i, 
						new MFCI<>(), new StreamFCI<>(),
						this.tdbDIUExampleModified, this.tdbDIUExampleModified.size(), i, 1);
			}
		}
	}

	@Test
	public void test_MFCIExample_addOnly_all() {
		this.tdbTest(
				"MFCI_vs_CFIStream_MFCIExample_addOnly_all", 
				new MFCI<>(), new DirectUpdateTree<>(),
				this.tdbMFCIExample, this.tdbMFCIExample.size(), this.tdbMFCIExample.size(), 1); 
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_MFCIExample_addOnly_all", 
					new MFCI<>(), new StreamFCI<>(),
					this.tdbMFCIExample, this.tdbMFCIExample.size(), this.tdbMFCIExample.size(), 1);
		}
	}
	
	@Test
	public void test_MFCIExample_severalSlidingWindows() {
		for(int i = 1; i <= this.tdbMFCIExample.size(); i++) {
			this.tdbTest(
					"MFCI_vs_CFIStream_MFCIExample_slidingWindow_" + i,
					new MFCI<>(), new DirectUpdateTree<>(),
					this.tdbMFCIExample, this.tdbMFCIExample.size(), i, 1);
			if(TestAlgorithms.testStreamFCI) {
				this.tdbTest(
						"MFCI_vs_StreamFCI_MFCIExample_slidingWindow_" + i,
						new MFCI<>(), new StreamFCI<>(),
						this.tdbMFCIExample, this.tdbMFCIExample.size(), i, 1);
			}
		}
	}
	
	@Test 
	public void test_MFCIExample_severalSlidingWindows_randomized() {
		List<Itemset<Character>> tdbRandomized = new ArrayList<>(this.tdbMFCIExample); 
		for(int j = 0; j < TestAlgorithms.repetitions; j++) {
			Collections.shuffle(tdbRandomized);
			for(int i = 1; i <= tdbRandomized.size(); i++) {
				this.tdbTest(
						"MFCI_vs_CFIStream_MFCIExample_slidingWindow_" + i + "_randomized_" + j,
						new MFCI<>(), new DirectUpdateTree<>(),
						tdbRandomized, tdbRandomized.size(), i, 1);
				if(TestAlgorithms.testStreamFCI) {
					this.tdbTest(
							"MFCI_vs_StreamFCI_MFCIExample_slidingWindow_" + i + "_randomized_" + j,
							new MFCI<>(), new StreamFCI<>(),
							tdbRandomized, tdbRandomized.size(), i, 1);
				}
			}
		}
	}
	
	@Test 
	public void test_LectureExample1_addOnly_all() {
		this.tdbTest(
				"MFCI_vs_CFIStream_LectureExample1_addOnly_all",
				new MFCI<>(), new DirectUpdateTree<>(),
				this.tdbLectureExample1, this.tdbLectureExample1.size(), this.tdbLectureExample1.size(), 1);
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_LectureExample1_addOnly_all",
					new MFCI<>(), new StreamFCI<>(),
					this.tdbLectureExample1, this.tdbLectureExample1.size(), 
					this.tdbLectureExample1.size(), 1);
		}
	}
	
	@Test 
	public void test_LectureExample1_severalSlidingWindows() {
		for(int i = 1; i <= this.tdbLectureExample1.size(); i++) {
			this.tdbTest(
					"MFCI_vs_CFIStream_LectureExample1_slidingWindow_" + i,
					new MFCI<>(), new DirectUpdateTree<>(),
					this.tdbLectureExample1, this.tdbLectureExample1.size(), i, 1);
			if(TestAlgorithms.testStreamFCI) {
				this.tdbTest(
						"MFCI_vs_StreamFCI_LectureExample1_slidingWindow_" + i,
						new MFCI<>(), new StreamFCI<>(),
						this.tdbLectureExample1, this.tdbLectureExample1.size(), i, 1);
			}
		}
	}
	
	@Test 
	public void test_LectureExample1_severalSlidingWindows_randomized() {
		List<Itemset<Character>> tdbRandomized = new ArrayList<>(this.tdbLectureExample1); 
		for(int j = 0; j < TestAlgorithms.repetitions; j++) {
			Collections.shuffle(tdbRandomized);
			for(int i = 1; i <= tdbRandomized.size(); i++) {
				this.tdbTest(
						"MFCI_vs_CFIStream_LectureExample1_slidingWindow_" + i + "_randomized_" + j,
						new MFCI<>(), new DirectUpdateTree<>(),
						tdbRandomized, tdbRandomized.size(), i, 1);
				if(TestAlgorithms.testStreamFCI) {
					this.tdbTest(
							"MFCI_vs_StreamFCI_LectureExample1_slidingWindow_" + i + "_randomized_" + j,
							new MFCI<>(), new StreamFCI<>(),
							tdbRandomized, tdbRandomized.size(), i, 1);
				}
			}
		}
	}
	
	@Test 
	public void test_LectureExample2_addOnly_all() {
		this.tdbTest(
				"MFCI_vs_CFIStream_LectureExample2_addOnly_all",
				new MFCI<>(), new DirectUpdateTree<>(),
				this.tdbLectureExample2, this.tdbLectureExample2.size(), this.tdbLectureExample2.size(), 1);
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_LectureExample2_addOnly_all",
					new MFCI<>(), new StreamFCI<>(),
					this.tdbLectureExample2, this.tdbLectureExample2.size(),
					this.tdbLectureExample2.size(), 1);
		}
	}
	
	@Test 
	public void test_LectureExample2_severalSlidingWindows() {
		for(int i = 1; i <= this.tdbLectureExample2.size(); i++) {
			this.tdbTest(
					"MFCI_vs_CFIStream_LectureExample2_slidingWindow_" + i,
					new MFCI<>(), new DirectUpdateTree<>(),
					this.tdbLectureExample2, this.tdbLectureExample2.size(), i, 1);
			if(TestAlgorithms.testStreamFCI) {
				this.tdbTest(
						"MFCI_vs_StreamFCI_LectureExample2_slidingWindow_" + i,
						new MFCI<>(), new StreamFCI<>(),
						this.tdbLectureExample2, this.tdbLectureExample2.size(), i, 1);
			}
		}
	}
	
	@Test 
	public void test_LectureExample2_severalSlidingWindows_randomized() {
		List<Itemset<Character>> tdbRandomized = new ArrayList<>(this.tdbLectureExample2); 
		for(int j = 0; j < TestAlgorithms.repetitions; j++) {
			Collections.shuffle(tdbRandomized);
			for(int i = 1; i <= tdbRandomized.size(); i++) {
				this.tdbTest(
						"MFCI_vs_CFIStream_LectureExample2_slidingWindow_" + i + "_randomized_" + j,
						new MFCI<>(), new DirectUpdateTree<>(),
						tdbRandomized, tdbRandomized.size(), i, 1);
				if(TestAlgorithms.testStreamFCI) {
					this.tdbTest(
							"MFCI_vs_StreamFCI_LectureExample2_slidingWindow_" + i + "_randomized_" + j,
							new MFCI<>(), new StreamFCI<>(),
							tdbRandomized, tdbRandomized.size(), i, 1);
				}
			}
		}
	}
	
	@Test 
	public void test_LexicographicalOrderExample_addOnly_all() {
		this.tdbTest(
				"MFCI_vs_CFIStream_LexicographicalOrderExample_addOnly_all",
				new MFCI<>(), new DirectUpdateTree<>(),
				this.tdbLexicoraphicalOrderExample, this.tdbLexicoraphicalOrderExample.size(), 
				this.tdbLexicoraphicalOrderExample.size(), 1);
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_LexicographicalOrderExample_addOnly_all",
					new MFCI<>(), new StreamFCI<>(),
					this.tdbLexicoraphicalOrderExample, this.tdbLexicoraphicalOrderExample.size(), 
					this.tdbLexicoraphicalOrderExample.size(), 1);
		}
	}
	
	@Test 
	public void test_LexicographicalOrderExample_severalSlidingWindows() {
		for(int i = 1; i <= this.tdbLectureExample2.size(); i++) {
			this.tdbTest(
					"MFCI_vs_CFIStream_LexicographicalOrderExample_slidingWindow_" + i,
					new MFCI<>(), new DirectUpdateTree<>(),
					this.tdbLexicoraphicalOrderExample, this.tdbLexicoraphicalOrderExample.size(), i, 1);
			if(TestAlgorithms.testStreamFCI) {
				this.tdbTest(
						"MFCI_vs_StreamFCI_LexicographicalOrderExample_slidingWindow_" + i,
						new MFCI<>(), new StreamFCI<>(),
						this.tdbLexicoraphicalOrderExample, this.tdbLexicoraphicalOrderExample.size(), 
						i, 1);
			}
		}
	}
	
	@Test 
	public void test_LexicographicalOrderExample_severalSlidingWindows_randomized() {
		List<Itemset<Integer>> tdbRandomized = new ArrayList<>(this.tdbLexicoraphicalOrderExample); 
		for(int j = 0; j < TestAlgorithms.repetitions; j++) {
			Collections.shuffle(tdbRandomized);
			for(int i = 1; i <= tdbRandomized.size(); i++) {
				this.tdbTest(
						"MFCI_vs_CFIStream_LexicographicalOrderExample_slidingWindow_" + i + "_randomized_" + j,
						new MFCI<>(), new DirectUpdateTree<>(),
						tdbRandomized, tdbRandomized.size(), i, 1);
				if(TestAlgorithms.testStreamFCI) {
					this.tdbTest(
							"MFCI_vs_StreamFCI_LexicographicalOrderExample_slidingWindow_" + i + "_randomized_" + j,
							new MFCI<>(), new StreamFCI<>(),
							tdbRandomized, tdbRandomized.size(), i, 1);
				}
			}
		}
	}
	
	@Test 
	public void test_T10I4D100K_addOnly_20() {
		this.tdbTest(
				"MFCI_vs_CFIStream_T10I4D100K_addOnly_20", 
				new MFCI<>(), new DirectUpdateTree<>(),
				this.tdbT10I4D100K, 20, 20, 1);
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_T10I4D100K_addOnly_20", 
					new MFCI<>(), new StreamFCI<>(),
					this.tdbT10I4D100K, 20, 20, 1);
		}
	}
	
	@Test
	public void test_T10I4D100K_severalSlidingWindows_20() {
		for(int i = 1; i <= 20; i++) {
			this.tdbTest(
					"MFCI_vs_CFIStream_T10I4D100_slidingWindow_" + i, 
					new MFCI<>(), new DirectUpdateTree<>(), 
					this.tdbT10I4D100K, 20, i, 1);
			if(TestAlgorithms.testStreamFCI) {
				this.tdbTest(
						"MFCI_vs_StreamFCI_T10I4D100_slidingWindow_" + i, 
						new MFCI<>(), new StreamFCI<>(), 
						this.tdbT10I4D100K, 20, i, 1);
			}
		}
	}
	
	@Test
	public void test_T10I4D100K_severalSlidingWindows_randomized_10() {
		List<Itemset<Integer>> tdbRandomized = new ArrayList<>(this.tdbT10I4D100K.subList(0, 100)); 
		for(int j = 0; j < TestAlgorithms.repetitions; j++) {
			Collections.shuffle(tdbRandomized);
			for(int i = 1; i <= 10; i++) {
				this.tdbTest(
						"MFCI_vs_CFIStream_T10I4D100K_slidingWindow_" + i + "_randomized_" + j,
						new MFCI<>(), new DirectUpdateTree<>(),
						tdbRandomized, 10, i, 1);
				if(TestAlgorithms.testStreamFCI) {
					this.tdbTest(
							"MFCI_vs_StreamFCI_T10I4D100K_slidingWindow_" + i + "_randomized_" + j,
							new MFCI<>(), new StreamFCI<>(),
							tdbRandomized, 10, i, 1);
				}
			}
		}
	}
	
	@Test 
	public void test_T10I4D100K_addOnly_50() {
		this.tdbTest(
				"MFCI_vs_CFIStream_T10I4D100K_addOnly_50", 
				new MFCI<>(), new DirectUpdateTree<>(), 
				this.tdbT10I4D100K, 50, 50, 10); 
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_T10I4D100K_addOnly_50", 
					new MFCI<>(), new StreamFCI<>(), 
					this.tdbT10I4D100K, 50, 50, 10);
		}
	}
	
	@Test 
	public void test_T10I4D100K_addOnly_150() {
		this.tdbTest(
				"MFCI_vs_CFIStream_T10I4D100K_addOnly_150", 
				new MFCI<>(), new DirectUpdateTree<>(), 
				this.tdbT10I4D100K, 150, 150, 10); 
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_T10I4D100K_addOnly_150", 
					new MFCI<>(), new StreamFCI<>(), 
					this.tdbT10I4D100K, 150, 150, 10);
		}
	}
	
	@Test 
	public void test_T10I4D100K_slidingWindow_500_40() {
		this.tdbTest(
				"MFCI_vs_CFIStream_T10I4D100K_slidingWindow_500_40", 
				new MFCI<>(), new DirectUpdateTree<>(), 
				this.tdbT10I4D100K, 500, 40, 1); 
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_T10I4D100K_slidingWindow_500_40", 
					new MFCI<>(), new StreamFCI<>(), 
					this.tdbT10I4D100K, 500, 40, 1);
		}
	}
	
	@Test 
	public void test_T10I4D100K_slidingWindow_4000_100() {
		this.tdbTest(
				"MFCI_vs_CFIStream_T10I4D100K_slidingWindow_4000_100", 
				new MFCI<>(), new DirectUpdateTree<>(), 
				this.tdbT10I4D100K, 4000, 100, 5); 
		if(TestAlgorithms.testStreamFCI) {
			this.tdbTest(
					"MFCI_vs_StreamFCI_T10I4D100K_slidingWindow_4000_100", 
					new MFCI<>(), new StreamFCI<>(), 
					this.tdbT10I4D100K, 4000, 100, 5);
		}
	}

}
