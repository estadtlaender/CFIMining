package de.lab.CFIMining.CFIStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import de.lab.CFIMining.Itemset;
import de.lab.CFIMining.SlidingWindowAlgorithm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** 
 * A class representing the direct update tree (DIU tree) of the CFIStream algorithm. 
 * 
 * @author Eike Stadtlaender
 *
 * @param <T> The type of the items in the transaction stream
 */
public class DirectUpdateTree<T extends Comparable<T>> implements SlidingWindowAlgorithm<T> {
		
	private Node root = null; 
	private Map<T, Integer> history; 
	private int nextItemID = 0; 
	
	/** 
	 * A private class representing a node in the direct update tree. Each node stores an itemset, the 
	 * support of the itemset, the count of the itemset (i.e. the number of times this itemset occured as a 
	 * transaction), its children and its parent nodes. 
	 *  
	 * @author Eike Stadtlaender
	 *
	 */
	private class Node {
		private Itemset<T> itemset; 
		private int support;
		private int count; 
		private ArrayList<Node> children; 
		private Node parent;
		
		/** 
		 * Construct a node with a given itemset, its support and its count. 
		 * 
		 * @param X       The itemset represented by this node 
		 * @param support The support of the itemset represented by this node 
		 * @param count   The number of times the itemset {@code X} represented by this node occurred as a 
		 *                transaction
		 */
		Node(Itemset<T> X, int support, int count) {
			this(null, X, support, count); 
		}
		
		/** 
		 * Construct a node from a parent node, an itemset, the support of the itemset and its count. 
		 * 
		 * @param parent  The parent node of the newly created node
		 * @param X       The itemset this node represents. 
		 * @param support The support of the itemset represented by this node 
		 * @param count   The number of times the itemset {@code X} represented by this node occurred as a 
		 *                transaction 
		 */
		Node(Node parent, Itemset<T> X, int support, int count) {
			this.itemset = X; 
			this.support = support; 
			this.children = new ArrayList<Node>(); 
			this.parent = parent; 
			this.count = count; 
		}
		
		@Override 
		public String toString() {
			String repr = ""; 
			if(this.itemset != null) {
				repr += this.itemset.toString(); 
				repr += " (Support: " + Integer.toString(this.support); 
				repr += ", count: " + Integer.toString(this.count) +") \n";
			} else {
				repr += "ROOT \n"; 
			}
			if(!this.children.isEmpty()) {
				repr += "{\n"; 
			}
			for(Node child : this.children) {
				String childRepr = child.toString();
				childRepr = childRepr.replaceAll("(?m)^", "\t");
				repr += childRepr; 
			}
			if(!this.children.isEmpty()) {
				repr += "}"; 
			} 
			repr += "\n"; 
			return repr; 
		}
		
		@Override 
		public int hashCode() {
			return this.itemset.hashCode(); 
		}
		
		@Override
		public boolean equals(Object other) {
			if(this == other) 
				return true; 
			if(other == null) 
				return false; 
			
			if(other instanceof DirectUpdateTree<?>.Node) {
				DirectUpdateTree<?>.Node oth = (DirectUpdateTree<?>.Node)other; 
				return oth.itemset.equals(this.itemset); 
			}
			
			return false; 
		}
	}
	
	/** 
	 * Construct a new DirectUpdateTree. The newly constructed direct update tree just has a root node 
	 * which represents no itemset. 
	 */
	public DirectUpdateTree() {
		this.root = new Node(null, null, 0, 0); 
		this.history = new HashMap<T, Integer>(); 
	}
	
	/** 
	 * Check whether this DIU tree contains a node representing a given itemset. 
	 * 
	 * @param X The itemset to be searched for
	 * @return Returns true if there is a node in this DIU tree representing this itemset. 
	 */
	public boolean contains(Itemset<T> X) {
		return this.contains(this.root, X);  
	}
	
	/** 
	 * Check whether or not a subtree of this DIU tree contains a node which represents a given itemset.
	 *  
	 * @param node The node determining the subtree 
	 * @param X    The itemset to be searched for
	 * @return Returns {@code true} if there is such a node in the given subtree and {@code false} 
	 *         otherwise. 
	 */
	private boolean contains(Node node, Itemset<T> X) {
		if(node != this.root && node.itemset.equals(X)) {
			return true; 
		} else {
			for(Node child : node.children) {
				if(this.contains(child, X)) {
					return true; 
				}
			}
			return false; 
		}
	}
	
	/** 
	 * Find a node in the tree representing a given itemset (if existent).   
	 * 
	 * @param X The itemset to be searched for. 
	 * @return Returns a {@code Node} representing the itemset {@code X} if existent and {@code null} 
	 *         otherwise. 
	 */
	private Node find(Itemset<T> X) {
		return this.find(this.root, X); 
	}
	
	/** 
	 * Find a node in a subtree representing a given itemset (if existent). 
	 * 
	 * @param node The node determining the subtree to look for the itemset. 
	 * @param X The itemset to be searched for. 
	 * @return Returns a {@code Node} representing the itemset {@code X} if existent in the subtree 
	 *         determined by the input {@code node} and {@code null} otherwise.  
	 */
	private Node find(Node node, Itemset<T> X) {
		// Check this node whether it represents the itemset or not 
		int size = node == this.root ? 0 : node.itemset.size(); 
		if(node != this.root && size == X.size() && node.itemset.equals(X)) {
			return node; 
		}
		
		// If not then check the children (recursively) 
		Node res = null; 
		if(size < X.size()) {
			Iterator<Node> iter = node.children.iterator(); 
			while(res == null && iter.hasNext()) {
				Node child = iter.next(); 
				res = this.find(child, X);
			}
		}
		return res; 
	}

	@Override
	public int support(Itemset<T> X) {
		/* 
		 * This methods assumes that it is possible that there is a node representing the given itemset 
		 * and first checks for that. If this is not the case than the support is computed by hand. It makes 
		 * sense to make this distinction because in the function DirectUpdateTree#add the support is 
		 * computed only if we already know that the itemset is not represented by a node in this DIU tree.  
		 */
		Node nodeX = this.find(X); 
		if(nodeX != null) {
			return nodeX.support; 
		} else {
			return this.supportIfNotContained(X); 
		}
	}
	
	/** 
	 * Compute the support for a given itemset under the assumption that the itemset is not represented by 
	 * a node in the tree. 
	 * 
	 * @param X The itemset for which the support shall be computed. 
	 * @return Returns an integer holding the support of the given itemset in this DIU tree.
	 * @see DirectUpdateTree#add(Itemset, boolean, Node, Set, boolean)
	 */
	private int supportIfNotContained(Itemset<T> X) {
		int sup = 0; 
		// the support is computed by summing up all the immediate supersets of X in this DIU tree. 
		for(Node superset : this.findAllImmediateSupersets(X)) {
			sup += superset.support; 
		}
		return sup;
	}
	
	/** 
	 * Find all nodes which are an immediate superset of a given itemset. 
	 * 
	 * @param X The itemset for which the immediate supersets shall be computed. 
	 * @return Returns a {@code Set<Node>} containing all nodes which represented an immediate superset of 
	 *         {@code X} in this DIU tree. 
	 */
	private Set<Node> findAllImmediateSupersets(Itemset<T> X) {
		return this.findAllImmediateSupersets(this.root, X); 
	}
	
	/** 
	 * Find all nodes in a subtree which represent an immediate superset of a given itemset. 
	 * 
	 * @param node The node determining the subtree of this DIU tree to be searched. 
	 * @param X    The itemset for which the immediate supersets shall be computed. 
	 * @return Returns a {@code Set<Node>} containing all the nodes which represent an immediate superset of 
	 *         {@code X} in this DIU tree. 
	 */
	private Set<Node> findAllImmediateSupersets(Node node, Itemset<T> X) {
		/*
		 * First all supersets are computed with an enabled early stopping flag (since we are only 
		 * interested in the immediate supersets).  
		 */
		Set<Node> supersets = this.findAllSupersets(X, true);
		
		/* 
		 * We need to remove comparable itemsets because different branches can contain subset-comparable 
		 * itemsets. 
		 */
		Iterator<Node> iter1 = supersets.iterator();
		Set<Node> obsolete = new HashSet<Node>(); 
		while(iter1.hasNext()) {
			Node n1 = iter1.next();
			Iterator<Node> iter2 = supersets.iterator();
			while(iter2.hasNext()) { 
				Node n2 = iter2.next(); 
				int size1 = n1.itemset.size(); 
				int size2 = n2.itemset.size(); 
				if(size1 != size2 || !n1.equals(n2)) {
					if(size2 < size1 && n1.itemset.contains(n2.itemset)) {
						obsolete.add(n1); 
						// if n1 is removed we do not need to compare n1 with further elements, so break. 
						break; 
					} else if(size1 < size2 && n2.itemset.contains(n1.itemset)) {
						obsolete.add(n2); 
					}
				}
			}
		}
		// remove the itemsets which are marked obsolete. 
		supersets.removeAll(obsolete); 
		
		return supersets;
	}
	
	/** 
	 * Find all nodes in this DIU tree which represent subsets of a given itemset. Here subset is <b>not</b> 
	 * meant in the strict sense, so if the given itemset is represented by a node in this DIU tree then it 
	 * will also be among the results. 
	 *  
	 * @param X The itemset for which the subsets shall be computed. 
	 * @return Returns a {@code Set<Node>} of all nodes representing a subset of {@code X} 
	 */
	private Set<Node> findAllSubsets(Itemset<T> X) {
		return this.findAllSubsets(this.root, X); 
	}
	
	/** 
	 * Find all nodes in a subtree of this DIU tree which represent subsets of a given itemset. Here subset 
	 * is <b>not</b> meant in the strict sense. 
	 * 
	 * @param node The node determining the subtree to be searched. 
	 * @param X The itemset for which the subsets shall be computed. 
	 * @return Returns a {@code Set<Node>} of all nodes under the given node representing a subset of the 
	 *         given itemset.  
	 */
	private Set<Node> findAllSubsets(Node node, Itemset<T> X) {
		Set<Node> subsets = new HashSet<Node>();
		
		// Check this node ...
		if(node != this.root && X.contains(node.itemset)) {
			subsets.add(node); 
		}
		
		// ... and all its childs (recursively) 
		for(Node child : node.children)
		{
			if(child.itemset.size() <= X.size()) {
				subsets.addAll(this.findAllSubsets(child, X));
			}
		} 
		
		return subsets; 
	}
	
	/** 
	 * Find all nodes in this DIU tree which represent a superset of a given itemset. Here superset is 
	 * <b>not</b> meant in the strict sense. So, if there is a node representing the given itemset itself 
	 * then this node will be among the results. 
	 * 
	 * @param X The itemset for which the supersets shall be computed. 
	 * @return Returns a {@code Set<Node>} of all nodes which represent supersets of the given itemset.  
	 */
	private Set<Node> findAllSupersets(Itemset<T> X) {
		return this.findAllSupersets(this.root, X, false); 
	}
	
	/** 
	 * Find all nodes in this DIU tree which represent a superset of a given itemset. Here superset is 
	 * <b>not</b> meant in the strict sense. A flag can be set to enable early stopping if only the 
	 * immediate supersets are of interest. 
	 * 
	 * @param X          The itemset for which all the supersets shall be computed. 
	 * @param earlyAbort A flag determining whether or not to use early abort. 
	 * @return Returns a {@code Set<Node>} of all nodes which represent (immediate) supersets of the given 
	 *         itemset. 
	 */
	private Set<Node> findAllSupersets(Itemset<T> X, boolean earlyAbort) {
		return this.findAllSupersets(this.root, X, earlyAbort); 
	}
	
	/** 
	 * Find all nodes in a subtree of this DIU tree which represent a superset of a given itemset. Here 
	 * superset is <b>not</b> meant in the strict sense. A flag can be set to enable early abort if only the 
	 * immediate supersets are of interest. 
	 * 
	 * @param node       The node determining the subtree of this DIU tree which is searched
	 * @param X          The itemset for which the supersets shall be computed. 
	 * @param earlyAbort A flag determining whether or not to use early abort. 
	 * @return Returns a {@code Set<Node>} of all nodes in the given subtree which represent (immediate) 
	 *         supersets of the given itemset. 
	 */
	private Set<Node> findAllSupersets(Node node, Itemset<T> X, boolean earlyAbort) {
		Set<Node> supersets = new HashSet<Node>(); 
		
		// Check this node 
		if(node != this.root && node.itemset.size() > X.size()) {
			if(node.itemset.contains(X)) {
				supersets.add(node); 
				if(earlyAbort) 
					return supersets; 
			}
		}
		// and all its children (recursively) 
		for(Node child : node.children) {
				supersets.addAll(this.findAllSupersets(child, X, earlyAbort));
		}
		
		return supersets; 
	}
	
	/** 
	 * Find a node in this DIU tree which represents a superset of a given itemset having minimum length. 
	 *  
	 * @param X The itemset for which the minimum superset shall be found. 
	 * @return Returns a node of this DIU tree which represents the found superset of minimum length. 
	 */
	private Node findMinimumSuperset(Itemset<T> X) {
		return this.findMinimumSuperset(this.root, X); 
	}
	
	/** 
	 * Find a node in a subtree of this DIU tree which represents a superset of a given itemset having 
	 * minimum length (in this subtree). 
	 * 
	 * @param node The node determining the subtree of this DIU tree 
	 * @param X The itemset for which the minimum superset shall be found. 
	 * @return Returns a node of the subtree of this DIU tree which represents the found superset of 
	 *         minimum length. 
	 */
	private Node findMinimumSuperset(Node node, Itemset<T> X) {
		// Check this node. 
		if(node != this.root && node.itemset.size() >= X.size() && node.itemset.contains(X)) {
			return node; 
		} else {
			/*
			 * And then all its children (recursively).
			 *  
			 * Since the children are all supersets of its parents in DIU trees the recursion does not need 
			 * to be proceeded if a superset is found. Then only the minimum superset among the minimum 
			 * supersets in the branches of the children has to be found.  
			 */
			Node minNode = null; 
			for(Node child : node.children) {
				Node childMinNode = this.findMinimumSuperset(child, X); 
				if(minNode == null || 
						(childMinNode != null && childMinNode.itemset.size() < minNode.itemset.size()))
				{
					minNode = childMinNode; 
				}
			}
			return minNode; 
		}
	}
	
	/** 
	 * Insert a newly created note in this DIU tree and update the tree structure according to the 
	 * lexicographical order. 
	 * 
	 * @param node The newly created node which is to be inserted into this DIU tree. 
	 */
	private void insertNode(Node node) {
		Node parent = this.root;
		boolean foundBetterParent = true; 
		while(foundBetterParent)
		{
			foundBetterParent = false; 
			for(Node child : parent.children) 
			{
				/*
				 * Compare the itemset of the new node with the children of the best known parent of the
				 * node according to the lexicographical order and the subset relation. This ensures the 
				 * correct tree structure which is desired.  
				 */				
				if(child.itemset.compareTo(node.itemset, this.history) <= 0 
						&& node.itemset.contains(child.itemset)) 
				{
					// if a better parent is found then recurse this procedure on its children
					foundBetterParent = true; 
					parent = child; 
					break; 
				}
			}
		}
		
		/* 
		 * Ensure that the subset relation is not broken and if necessary move the subset-comparable 
		 * children of the parent to be children of the newly created node. 
		 */
		
		Iterator<Node> iter = parent.children.iterator(); 
		while(iter.hasNext()) {
			Node child = iter.next(); 

			if(child.itemset.compareTo(node.itemset, this.history) > 0 
					&& child.itemset.contains(node.itemset)) 
			{
				iter.remove();
				node.children.add(child); 
				child.parent = node; 
			}
		}

		/* 
		 * After the best parent is found and the tree structure among the other children is regained, find 
		 * the correct position of the new node among the other children of that parent.  
		 */
		int idx = 0; 
		while(idx < parent.children.size()) {
			Node c = parent.children.get(idx);
			if(c.itemset.compareTo(node.itemset, this.history) > 0) {
				break; 
			}
			idx++; 
		}
		parent.children.add(idx, node); 
		node.parent = parent; 
	}
	
	/** 
	 * Insert a batch of newly created nodes in this DIU tree and update the tree structure to reflect the 
	 * lexicographical order and the subset-relation as desired. 
	 * 
	 * @param Cnew A {@code Set<Node>} of newly created nodes which are to be inserted
	 */
	private void insertNodes(Set<Node> Cnew) {
		// this function calls this.insertNode for each of the new nodes
		for(Node node : Cnew) {
			this.insertNode(node);
		}
	}

	/** 
	 * @see DirectUpdateTree#add(Itemset, boolean, Node, Set, boolean)
	 */
	@Override
	public void add(Itemset<T> X) {
		// update history 
		for(T item : X) {
			if(!this.history.containsKey(item)) {
				this.history.put(item, this.nextItemID++);
			} 
		}
		// create an empty set of nodes Cnew (with correct order comparator) 
		Set<Node> Cnew = new TreeSet<Node>(new Comparator<Node>() {
	
			@Override
			public int compare(DirectUpdateTree<T>.Node o1, DirectUpdateTree<T>.Node o2) {
				return o1.itemset.compareTo(o2.itemset, DirectUpdateTree.this.history);
			}
			
		}); 
		// call the recursive version of the add method 
		this.add(X, true, null, Cnew, true); 
	}
	
	/** 
	 * Add a new transaction to the DIU tree. This method and why it works is described in the paper 
	 * "CFI-Stream: mining closed frequent itemsets in data streams" by Jiang and Gruenwald. 
	 * 
	 * @param X             The transaction to be added to this DIU tree  
	 * @param Xclose        A flag to determine how to deal with recursive calls where the itemset is 
	 *                      already contained in this DIU tree
	 * @param X0            The node of the new transaction (used for the recursive call of this function) 
	 * @param Cnew          A {@code Set<Node>} of nodes to be created in this DIU tree after the addition 
	 *                      of the new transaction
	 * @param recursionFlag A flag indicating whether or not to recurse further (currently this will 
	 */
	private void add(Itemset<T> X, boolean Xclose, Node X0, Set<Node> Cnew, boolean recursionFlag) {
		Node nodeX = this.find(X);  
		if(nodeX != null)
		{
			/* 
			 * If there already is a node representing the new transaction then only the supports of the 
			 * relevant nodes shall be updated.  
			 */
			if(Cnew.isEmpty()) {
				/* 
				 * If this is the toplevel call then update the count (i.e. number of times the itemset
				 * showed up in the datastream)  
				 */				
				nodeX.count += 1;
			}
			// Update the support for the nodes 
			nodeX.support += 1; 
			if(Xclose) {
				if(recursionFlag) {
					/* 
					 * And all the relevant candidate nodes if this is the case where the new transaction 
					 * was already in this DIU tree
					 */
					List<Itemset<T>> candidates = this.candidatesInDescendingLength(X); 
					candidates.remove(X); 
					for(Itemset<T> Y : candidates) { 
						Node nodeY = this.find(Y);  
						if(nodeY != null) {
							nodeY.support += 1; 
						}
					}
				} 
				return; 
			}
		} else {
			/* 
			 * If the itemset is not already contained in the tree then then cases where the support is 
			 * positive or zero has to be handled differently according to the lemmata in the paper 
			 * mentioned above. 
			 */
			int X_support = this.supportIfNotContained(X); 
			if(X_support > 0) {
				if(Cnew.isEmpty()) {
					X0 = new Node(X, X_support + 1, 1);
					Cnew.add(X0); 
					Xclose = false; 
				} else {
					if(this.closureCheckForAdd(X, X0)) {
						Node N = new Node(X, X_support + 1, 0); 
						Cnew.add(N);
					}
				}
			} else {
				if(Cnew.isEmpty())
				{
					X0 = new Node(X, 1, 1); 
					Cnew.add(X0);
				}
			}
			
			if(recursionFlag && X.size() > 1) { 
				/* 
				 * Here the implementation deviates from the description in the paper. Instead of 
				 * considering all subsets which turned out to be very inefficient only the intersections 
				 * with existing nodes in this DIU tree are candidates for new nodes.  
				 * 
				 * Also the recursion is handles differently by first determining all the candidate 
				 * itemsets by decreasing length. 
				 */
				List<Itemset<T>> candidates = this.candidatesInDescendingLength(X); 
				candidates.remove(X); 
				for(Itemset<T> Y : candidates) {
					this.add(Y, Xclose, X0, Cnew, false);
				}
			}
		}
		
		if(X0 != null && X0.itemset.equals(X)) {
			this.insertNodes(Cnew); 
		}
	}

	/** 
	 * Check if a given itemset is closed after a transaction was added to the DIU tree. This closure check 
	 * is described in the paper "CFI-Stream: mining closed frequent itemsets in data streams" by Jiang and 
	 * Gruenwald. 
	 * 
	 * @param X The itemset to be checked for closedness 
	 * @param X0 The node representing the newly added transaction
	 * @return Returns {@code true} if the given itemset is closed after adding a transaction and 
	 *         {@code false} otherwise. 
	 */
	private boolean closureCheckForAdd(Itemset<T> X, Node X0)
	{
		Node X_c = this.findMinimumSuperset(X); 
		if(X_c != null) 
		{
			for(T item : X_c.itemset) {
				if(!X.contains(item) && X0.itemset.contains(item)) {
					return false; 
				}
			} 
			return true; 
		} else { 
			return false; 
		} 
	}

	/** 
	 * Check whether or not a given node can be pruned when determining the candidates during addition of a 
	 * given transaction by considering the lexicographical order.  
	 * 
	 * @param X The new transaction 
	 * @param Y The node to be checked for "prunability"
	 * @return Returns {@code true} is the given node (and its children or successors) can be pruned and 
	 *         {@code false} otherwise. 
	 */
	private boolean canPrune(Itemset<T> X, Node Y) {
		Itemset<T> checkSet = null; 
		Itemset<T> parentSet = Y.parent.itemset; 
		if(parentSet != null) {
			checkSet = parentSet.intersection(X); 
		}
		
		/* 
		 * We can be sure that a node can be pruned if the largest item of the itemset is smaller than the
		 * minimal item of the node in question. We also compare with the intersection of the nodes parent to 
		 * accomodate for common elements in the sets. 
		 */
		int maxX = -1; 
		for(T item : X) {
			int pos = this.history.get(item); 
			if(pos > maxX && (checkSet == null || !checkSet.contains(item))) {
				maxX = pos; 
			}
		}
		
		int minY = Integer.MAX_VALUE; 
		for(T item : Y.itemset) {
			int pos = this.history.get(item); 
			if(pos < minY && (checkSet == null || !checkSet.contains(item))) {
				minY = pos; 
			}
		}
		
		return maxX < minY; 
	}
	
	/** 
	 * Compute all nodes in this DIU tree which are relevant when adding a new given transaction to this DIU 
	 * tree. 
	 * 
	 * @param X The newly added transaction. 
	 * @return Returns a {@code Set<Node>} containing all the relevant nodes (after pruning) which need to 
	 *         be updated or checked after adding the given transaction.
	 */
	private Set<Node> allNodesPruned(Itemset<T> X) {
		return this.allNodesPruned(this.root, X); 
	}
	
	/** 
	 * Compute all nodes in a subtree of this DIU tree which need to be checked and/or updated after adding 
	 * a given transaction to this DIU tree. 
	 * 
	 * @param node The node determining the subtree of this DIU tree 
	 * @param X    The newly added transaction
	 * @return Returns a {@code Set<Node>} containing all the relevant nodes (after pruning) which need to 
	 *         be updated or checked after adding the given transaction. 
	 * @see DirectUpdateTree#canPrune(Itemset, Node)
	 */
	private Set<Node> allNodesPruned(Node node, Itemset<T> X) {
		Set<Node> nodes = new HashSet<>(); 
		
		for(Node child : node.children) {
			if(!canPrune(X, child)) {
				nodes.add(child); 
				nodes.addAll(this.allNodesPruned(child, X));
			} else {
				/* 
				 * the following children are all lexicographically larger than the itemset due to the tree
				 * structure 
				 */
				break; 
			}
		}
		
		return nodes; 
	}
	
	/** 
	 * Compute all the itemsets which need to be checked and/or updated after adding a given transaction to 
	 * this DIU tree. The result is sorted by descending lengths because this is the order in which the 
	 * updates must happen. 
	 * 
	 * @param X The newly added transaction 
	 * @return Returns a {@code List<Itemset<T>>} of itemsets which might be new closed itemset after adding 
	 *         the given transaction. 
	 */
	private List<Itemset<T>> candidatesInDescendingLength(Itemset<T> X) {
		Set<Itemset<T>> intersectionsSet = new HashSet<>();
		// First, compute all the relevant nodes 
		Set<Node> nodes = this.allNodesPruned(X); 

		for(Node node : nodes) {
			// compute the intersections of the itemset with the relevant nodes
			Itemset<T> intersection = node.itemset.intersection(X); 
			if(intersection.size() > 0) {
				intersectionsSet.add(intersection);
			}
		}
		
		// Sort the intersections according to lengths and create the actual list object
		List<Itemset<T>> intersections = new ArrayList<>();
		intersections.addAll(intersectionsSet); 
		Collections.sort(intersections, new Comparator<Itemset<T>>() {

			@Override
			public int compare(Itemset<T> o1, Itemset<T> o2) {
				Integer l1 = o1.size(); 
				Integer l2 = o2.size(); 
				return l2.compareTo(l1);
			}
			
		});
		return intersections; 
	}
	
	/** 
	 * Check whether or not the itemset represented by a given node stays closed after deleting a 
	 * transaction. This method is described in the paper "CFI-Stream: mining closed frequent itemsets in 
	 * data streams" by Jiang and Gruenwald. 
	 * 
	 * @param nodeY The node in question.
	 * @param Cobsolete The already obsolete nodes (which are not to be considered!) 
	 * @return Returns {@code true} if the given node stays closed and {@code false} otherwise
	 */
	private boolean closureCheckForDelete(Node nodeY, Set<Node> Cobsolete) {
		Itemset<T> Y = nodeY.itemset; 
		Set<Node> supersets = this.findAllSupersets(Y);
		supersets.remove(nodeY); 
		supersets.removeAll(Cobsolete); 
		
		Iterator<Node> iter = supersets.iterator(); 
		Itemset<T> M = null; 
		if(iter.hasNext()) {
			Node node = iter.next(); 
			M = new Itemset<T>(node.itemset); 
			while(iter.hasNext()) {
				node = iter.next(); 
				M.retainAll(node.itemset);
			}
		}
		
		/* 
		 * According to the paper mentioned above, the node stays closed if the intersection of all the 
		 * strict supersets without the already obsolete itemsets is the itemset itself which is represented 
		 * by a node here. This rule only holds if the itemset does not occur as a transaction in the rest 
		 * of the sliding window. 
		 */
		return (Y.equals(M) || nodeY.count > 0); 
	}
	
	/** 
	 * Remove an obsolete node from this DIU tree. 
	 * 
	 * @param node The node to be removed. 
	 * 
	 */
	private void removeNode(Node node) {
		// Remove the actual node ... 
		Node parent = node.parent; 
		parent.children.remove(node); 
		
		// ... and re-insert its children at the right place. 
		for(Node child : node.children) {
			this.restructure(parent, child); 
		}
		
		node.parent = null;  
	}
	
	/** 
	 * Reinsert a node at the right place according to the lexicographical order and the subset-comparison 
	 * starting from an alleged new parent node.  
	 * 
	 * @param newParent The alleged new parent node of the child (it will definitely be a descendent of 
	 *                  {@code newParent}
	 * @param child     The child node which is to be re-inserted.  
	 */
	private void restructure(Node newParent, Node child) {
		boolean foundBetterParent = true; 
		while(foundBetterParent) {
			foundBetterParent = false; 
			for(Node c : newParent.children) {
				/* 
				 * If a node is lexicographically smaller than and a subset of the child node, then this
				 * would be a better parent node than the current parent node 
				 */
				if(c.itemset.compareTo(child.itemset, this.history) <= 0 && child.itemset.contains(c.itemset)) {
					newParent = c;  
					foundBetterParent = true; 
					break; 
				}
			}
		}
		
		/* 
		 * When the best new parent node is found than insert the child node at the right position among 
		 * the other children 
		 */
		child.parent = newParent; 
		int idx = 0; 
		while(idx < newParent.children.size()) {
			Node c = newParent.children.get(idx);
			if(c.itemset.compareTo(child.itemset, this.history) > 0) {
				break; 
			}
			idx += 1; 
		}
		newParent.children.add(idx, child); 
	}
	
	/** 
	 * Remove a batch of obsolete nodes from this DIU tree 
	 * @param nodes A {@code Set<Node>} of nodes which are to be removed. 
	 */
	private void removeNodes(Set<Node> nodes) {
		for(Node node : nodes) {
			this.removeNode(node);
		}
	}

	/** 
	 * {@inheritDoc} This algorithm is specified in the paper "CFI-Stream: mining closed frequent itemset in 
	 * data streams" by Jiang and Gruenwald. 
	 */
	@Override
	public void delete(Itemset<T> X) {
		Set<Node> Cobsolete = new HashSet<Node>();
		Node nodeX = this.find(X);  
		if(nodeX != null && nodeX.count >= 2) {
			/* 
			 * If the itemset showed up still as a transaction after deletion, then we only need to update
			 * the supports and counts.  
			 */			
			nodeX.count -= 1; 
			for(Node node : this.findAllSubsets(X)) {
				node.support -= 1; 
			}
		} else {
			Set<Node> subsetsSet = this.findAllSubsets(X); 
			List<Node> subsets = new ArrayList<Node>(subsetsSet);
			subsets.sort(new Comparator<Node>() {

				@Override
				public int compare(DirectUpdateTree<T>.Node o1, DirectUpdateTree<T>.Node o2) {
					Integer l1 = o1.itemset.size(); 
					Integer l2 = o2.itemset.size(); 
					return l2.compareTo(l1); 
				}
				
			});

			nodeX.count -= 1; 
			for(Node node : subsets) {
				if(node.count >= 2) {
					node.support -= 1; 
				} else {
					if(this.closureCheckForDelete(node, Cobsolete) /*&& node != nodeX*/) {
						node.support -= 1; 
					} else {
						Cobsolete.add(node); 
					}
				}
			}
		}
		this.removeNodes(Cobsolete);
	}

	@Override
	public Set<Itemset<T>> getClosedItemsets() {
		return this.getClosedFrequentItemsets(0); 
	}

	@Override
	public Set<Itemset<T>> getClosedFrequentItemsets(int t)
	{
		return this.getClosedFrequentItemsets(this.root, t); 
	}
	
	private Set<Itemset<T>> getClosedFrequentItemsets(Node node, int t)
	{
		Set<Itemset<T>> frequent = new HashSet<Itemset<T>>(); 
		
		if(node != this.root && node.support >= t)
		{
			frequent.add(node.itemset); 
		}
		
		for(Node child : node.children) 
		{
			frequent.addAll(this.getClosedFrequentItemsets(child, t)); 
		}
		
		return frequent; 
	}
	
	@Override 
	public String toString() {
		return this.root.toString();  
	}
	
	/** 
	 * Compute the (maximal) depth of this DIU tree. 
	 * 
	 * @return Returns an integer with the maximal depth of this DIU tree 
	 * @deprecated 
	 */
	public int depth() {
		return this.depth(this.root, 0); 
	}
	
	/** 
	 * Compute the (maximal) depth of this DIU tree in a given subtree. 
	 *  
	 * @param node The node determining the subtree
	 * @param d A recursion value carried along to calculate the maximal depth 
	 * @return Return the (maximal) depth of this DIU tree in a given subtree.  
	 * @deprecated 
	 */
	private int depth(Node node, int d) {
		int maxDepth = d; 
		
		for(Node child : node.children) {
			int depth = this.depth(child, d+1); 
			if(maxDepth < depth) {
				maxDepth = depth; 
			}
		}
		
		return maxDepth; 
	}
	
	public static void main(String[] args) {
		/*// Example 1
		DirectUpdateTree<String> tree = new DirectUpdateTree<String>(); 

		Itemset<String> I1 = new Itemset<String>("C"); 
		Itemset<String> I2 = new Itemset<String>("C", "D"); 
		Itemset<String> I3 = new Itemset<String>("A", "B"); 
		Itemset<String> I4 = new Itemset<String>("A", "B", "C"); 
		Itemset<String> I5 = new Itemset<String>("A", "B", "C"); 

		tree.add(I1);
		tree.add(I2);
		tree.add(I3);
		tree.add(I4);
		tree.delete(I1);
		tree.add(I5);
		tree.delete(I2);
		
		tree.delete(I1);
		tree.delete(I2);
		tree.delete(I3);
		tree.delete(I4);
		tree.delete(I5); //*/
		
		/*// Example 2
		DirectUpdateTree<String> tree = new DirectUpdateTree<String>(); 
		
		Itemset<String> I1 = new Itemset<String>("M", "O", "N", "K", "E", "Y"); 
		Itemset<String> I2 = new Itemset<String>("D", "O", "N", "K", "E", "Y"); 
		Itemset<String> I3 = new Itemset<String>("M", "A", "K", "E"); 
		Itemset<String> I4 = new Itemset<String>("M", "U", "C", "K", "Y"); 
		Itemset<String> I5 = new Itemset<String>("C", "O", "K", "E"); 
		
		tree.add(I1);
		tree.add(I2);
		tree.add(I3);
		tree.add(I4);
		tree.add(I5);
		
		System.out.println(tree.findAllImmediateSupersets(new Itemset<>("K", "M", "Y")));
		System.out.println(tree.getClosedItemsets());
		System.out.println(tree); //*/
		
		/*// Example 3
		Itemset<Integer> I1 = new Itemset<Integer>(25,52,164,240,274,328,368,448,538,561,630,687,730,775,825,834); 
		Itemset<Integer> I2 = new Itemset<Integer>(39,120,124,205,401,581,704,814,825,834); 
		Itemset<Integer> I3 = new Itemset<Integer>(35,249,674,712,733,759,854,950); 
		Itemset<Integer> I4 = new Itemset<Integer>(39,422,449,704,825,857,895,937,954,964); 
		
		DirectUpdateTree<Integer> tree = new DirectUpdateTree<>(); 
		tree.add(I1);
		tree.add(I2);
		tree.add(I3);
		tree.add(I4);
		
		System.out.println(tree); //*/
		

		/*// Example 4
		Itemset<Character> i1 = new Itemset<Character>('a', 'b', 'd', 'e'); 
		Itemset<Character> i2 = new Itemset<Character>('b', 'c', 'e'); 
		Itemset<Character> i3 = new Itemset<Character>('a', 'b', 'd', 'e'); 
		Itemset<Character> i4 = new Itemset<Character>('a', 'b', 'c', 'e'); 
		Itemset<Character> i5 = new Itemset<Character>('a', 'b', 'c', 'd', 'e'); 
		Itemset<Character> i6 = new Itemset<Character>('b', 'c', 'd');
		
		DirectUpdateTree<Character> tree = new DirectUpdateTree<Character>();
		
		tree.add(i1);
		tree.add(i2);
		tree.delete(i1);
		tree.add(i3);
		tree.delete(i2);
		tree.add(i4);
		tree.delete(i3);
		tree.add(i5);
		tree.delete(i4);
		tree.add(i6); //*/
		
		/*// Example 5
		DirectUpdateTree<String> tree = new DirectUpdateTree<String>(); 

		Itemset<String> I1 = new Itemset<String>("M", "A", "K", "E"); 
		Itemset<String> I2 = new Itemset<String>("C", "O", "K", "E"); 
		Itemset<String> I3 = new Itemset<String>("M", "O", "N", "K", "E", "Y"); 
		Itemset<String> I4 = new Itemset<String>("D", "O", "N", "K", "E", "Y"); 
		Itemset<String> I5 = new Itemset<String>("M", "U", "C", "K", "Y"); 
		
		tree.add(I1);
		tree.add(I2);
		tree.add(I3);
		tree.add(I4);
		tree.delete(I1);
		tree.add(I5); //*/
		
		/*// Example 6
		DirectUpdateTree<String> tree = new DirectUpdateTree<String>(); 
		MFCI<String> mfci = new MFCI<>(); 

		Itemset<String> I1 = new Itemset<String>("D", "O", "N", "K", "E", "Y"); 
		Itemset<String> I2 = new Itemset<String>("M", "U", "C", "K", "Y"); 
		Itemset<String> I3 = new Itemset<String>("M", "A", "K", "E"); 
		Itemset<String> I4 = new Itemset<String>("M", "O", "N", "K", "E", "Y"); 
		Itemset<String> I5 = new Itemset<String>("C", "O", "K", "E"); 
		
		tree.add(I1);
		mfci.add(I1);
		tree.add(I2);
		mfci.add(I2);
		tree.add(I3);
		mfci.add(I3);
		tree.add(I4);
		mfci.add(I4);
		tree.add(I5);
		mfci.add(I5);//*/
		
		/*// Example 7
		DirectUpdateTree<Integer> tree = new DirectUpdateTree<>();

		Itemset<Integer> I1 = new Itemset<>(1, 2);
		Itemset<Integer> I2 = new Itemset<>(2, 3);
		Itemset<Integer> I3 = new Itemset<>(3);
		Itemset<Integer> I4 = new Itemset<>(1, 2); 
		Itemset<Integer> I5 = new Itemset<>(1, 2, 3, 4); 

		tree.add(I1);
		tree.add(I2);
		tree.add(I3);
		tree.delete(I1);
		tree.add(I4);
		tree.add(I5);//*/
	}
	
}
