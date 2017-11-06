package de.lab.CFIMining.StreamFCI;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import de.lab.CFIMining.Itemset;

/** 
 * A class representing a dynamical frequent pattern tree as described in the paper "A Novel Strategy for 
 * Mining Frequent Closed Itemsets in Data Streams" by Tang et al. 
 * 
 * @author Eike Stadtlaender
 *
 * @param <T>
 */
public class DFPTree<T extends Comparable<T>> {

	/** 
	 * A class representing a node in a dynamical frequent pattern tree. Each node stores an item, a count, 
	 * a node link (used for the header table), its children and its parent nodes. 
	 * 
	 * @author Eike Stadtlaender
	 *
	 */
	class Node {
		
		public T item = null;
		public int count = 0; 
		public Node nodeLink = null; 
		
		public Map<T, Node> children = new HashMap<T, Node>(); 
		public Node parent = null; 
		
		@Override
		public String toString() {
			String repr = ""; 
			repr += this.item.toString() + " (count: " + this.count + ", id: " + this.hashCode()+ ")";
			if(!this.children.isEmpty()) {
				repr += "\n{\n"; 
				for(T item : this.children.keySet()) {
					Node child = this.children.get(item); 
					String childRepr = child.toString(); 
					childRepr = childRepr.replaceAll("(?m)^", "\t"); 
					repr += childRepr; 
				}
				repr += "}\n"; 
			} else {
				repr += "\n"; 
			}
			return repr; 
		}
		
	}
	
	/** 
	 * A class representing a header table entry of a frequent pattern tree. The header table consists of 
	 * all occurring items and a linked list connecting all nodes storing the respective item. Each header 
	 * entry also stores the support of the item in the whole DFPTree.  
	 * 
	 * @author Eike Stadtlaender
	 *
	 */
	class HeaderEntry {
		
		public int support; 
		public T item; 
		public Node nodeLink; 
		
	}
	
	private Node root; 
	private Map<T, HeaderEntry> headerTable; 
	
	/** 
	 * Construct a new DFPTree with an empty root and an empty header table. 
	 */
	DFPTree() {
		this.root = new Node(); 
		this.root.children = new HashMap<T, Node>();
		this.headerTable = new HashMap<T, HeaderEntry>(); 
	}
	
	/** 
	 * Add a new transaction to the DFPTree. 
	 * 
	 * @param X The transaction to be added. 
	 */
	public void add(Itemset<T> X) {
		this.add(this.root, this.itemsetToQueue(X));
		this.adjust(X, false); 
	}
	
	/** 
	 * Add or update the path of given items starting from a given node in this DFPTree. 
	 * 
	 * @param node The node to start from (used for recursion). 
	 * @param items A {@code Queue<T>} of items to be yet processed. 
	 */
	private void add(Node node, Queue<T> items) {
		T item = items.remove(); 
		Node child = node.children.get(item); 
		if(child != null) {
			// if there is already a child for the current item then update this and follow along 
			child.count += 1; 
			HeaderEntry headerEntry = this.headerTable.get(item); 
			headerEntry.support += 1; 
		} else {
			// Create new node for the item
			Node itemNode = new Node(); 
			itemNode.item = item; 
			itemNode.count = 1;
			
			node.children.put(item, itemNode); 
			itemNode.parent = node; 
			
			// update node links for the header table 
			HeaderEntry headerEntry = this.headerTable.get(item); 
			if(headerEntry == null) {
				headerEntry = new HeaderEntry(); 
				headerEntry.item = itemNode.item;
				headerEntry.support = itemNode.count; 
				headerEntry.nodeLink = itemNode;
				this.headerTable.put(item, headerEntry); 
			} else if(headerEntry.nodeLink == null) {
				headerEntry.nodeLink = itemNode; 
				headerEntry.support = itemNode.count; 
			} else {
				Node link = headerEntry.nodeLink; 
				while(link.nodeLink != null) {
					link = link.nodeLink; 
				}
				link.nodeLink = itemNode; 
				headerEntry.support += itemNode.count; 
			}
			child = itemNode; 
		}
		
		// if there is work left to do then proceed
		if(!items.isEmpty()) {
			this.add(child, items);
		}
	}
	
	/** 
	 * Delete a given transaction from this dynamical frequent pattern tree. 
	 * 
	 * @param X The transaction which shall be deleted. 
	 */
	public void delete(Itemset<T> X) {
		this.delete(this.root, this.itemsetToQueue(X));
		this.adjust(X, true); 
	}
	
	/** 
	 * Delete a path of items from this dynamical frequent pattern tree starting from a given node. 
	 * 
	 * @param node  The node to start the update from (used for recursion)  
	 * @param items The items left to process. 
	 */
	private void delete(Node node, Queue<T> items) {
		T item = items.remove(); 
		Node child = node.children.get(item); 
		if(child != null) {
			// update the count and the support in the header table corresponding to the current item 
			child.count -= 1; 
			HeaderEntry headerEntry = this.headerTable.get(child.item);
			headerEntry.support -= 1; 
			
			// if the support drops to zero the entry should removed from the header table 
			if(headerEntry.support == 0) {
				this.headerTable.remove(child.item); 
			}
			
			// if the count of the child drops to zero the node should be removed from this tree 
			if(child.count == 0) {
				// update header table
				Node link = headerEntry.nodeLink; 
				if(link == child) {
					headerEntry.nodeLink = child.nodeLink; 
				} else {
					while(link.nodeLink != child) {
						link = link.nodeLink; 
					}
					link.nodeLink = child.nodeLink; 
				}
				
				// remove node from tree
				node.children.remove(child.item); 
				for(T childItem : child.children.keySet()) {
					Node grandchild = child.children.get(childItem); 
					grandchild.parent = node; 
					this.merge(node, grandchild);
				}
				
				// call delete on node instead of child for further processing 
				child = node; 
			}
			
			// if there is work left to do proceed 
			if(!items.isEmpty()) {
				this.delete(child, items);
			}
		}
	}
	
	/** 
	 * Regain the desired tree structure (nodes are first sorted by descending support according to the 
	 * header table and then sorted according to the lexicographical order) after adding or deleting a 
	 * transaction in this tree. This method is described in the paper "A Novel Strategy for Mining Frequent
	 * Closed Itemsets in Data Streams" by Tang et al. 
	 * 
	 * @param X          The itemset which was added to or deleted from the tree 
	 * @param deleteFlag A flag indicating if this is an adjustment after a transaction was 
	 */
	private void adjust(Itemset<T> X, boolean deleteFlag) {
		Node node; 
		/* 
		 * While there is a pair of nodes in the wrong order swap the order for this branch and update the
		 * header table accordingly.  
		 */ 
		while(null != (node = this.findInversePair(X, deleteFlag))) {
			Node nodeY = node; 
			Node nodeX = node.parent; 
			Node nodeW = nodeX.parent; 

			nodeX.children.remove(nodeY.item); 
			nodeX.count -= nodeY.count; 
			
			Node nodeV = new Node(); 
			nodeV.item = nodeX.item; 
			nodeV.count = nodeY.count;

			nodeV.nodeLink = nodeX.nodeLink; 
			nodeX.nodeLink = nodeV;
			
			nodeV.parent = nodeY; 
			nodeV.children = nodeY.children; 
			for(T item : nodeY.children.keySet()) {
				Node child = nodeY.children.get(item); 
				child.parent = nodeV; 
			}
			nodeY.children = new HashMap<T, Node>(); 
			nodeY.children.put(nodeV.item, nodeV); 
			
			nodeY.parent = null; 
			
			// If the count of the node drops to zero it should be removed from the tree 
			if(nodeX.count == 0) {
				nodeW.children.remove(nodeX.item);
				HeaderEntry headerEntry = this.headerTable.get(nodeX.item); 
				if(headerEntry.nodeLink == nodeX) {
					headerEntry.nodeLink = nodeV; 
				} else {
					Node link = headerEntry.nodeLink; 
					while(link.nodeLink != nodeX) {
						link = link.nodeLink; 
					}
					link.nodeLink = nodeV; 
				}
			}
			
			/* 
			 * Merge the new path into the existing DFPTree starting from the grandparent of the inversed 
			 * pair.  
			 */
			this.merge(nodeW, nodeY);
		}
	}
	
	/** 
	 * Convert an itemset to a queue according to the desired order (descending in support and following 
	 * the lexicographical order). 
	 * 
	 * @param X The itemset to be converted. 
	 * @return Returns a {@code Queue<T>} containing the itemsets in the desired order. 
	 */
	private Queue<T> itemsetToQueue(Itemset<T> X) {
		Queue<T> items = new PriorityQueue<>(new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				HeaderEntry he1 = DFPTree.this.headerTable.get(o1); 
				HeaderEntry he2 = DFPTree.this.headerTable.get(o2); 
				Integer s1 = he1 == null ? 0 : he1.support; 
				Integer s2 = he2 == null ? 0 : he2.support;  
				if(s1.compareTo(s2) != 0) {
					return s2.compareTo(s1);
				} else {
					return o1.compareTo(o2); 
				}
			}
			
		}); 
		
		for(T item : X) {
			items.add(item); 
		}
		
		return items; 
	}
	
	/** 
	 * Merge a new path or tree into the existing tree starting from a given parent. 
	 * 
	 * @param target The parent node of the path. 
	 * @param node   The head of the path to be merged. 
	 */
	private void merge(Node target, Node node) {
		if(target.children.containsKey(node.item)) {
			// If the children of the target already contain a node for the node's item then use this 
			Node targetNode = target.children.get(node.item); 
			targetNode.count += node.count;
			
			// remove node from node links
			HeaderEntry headerEntry = this.headerTable.get(node.item); 
			Node link = headerEntry.nodeLink; 
			if(link == node) {
				headerEntry.nodeLink = link.nodeLink; 
			} else {
				while(link.nodeLink != node) {
					link = link.nodeLink; 
				}
				link.nodeLink = node.nodeLink;
			}
			
			for(T item : node.children.keySet()) {
				Node child = node.children.get(item); 
				node.parent = targetNode; 
				merge(targetNode, child);
			} 
		} else {
			// otherwise create a new child. In this case no further processing is needed. 
			target.children.put(node.item, node); 
			node.parent = target; 
		}
	}
	
	/** 
	 * Find the next inversed pair in this tree after a given itemset was added to or deleted from this 
	 * DFPTree. 
	 *  
	 * @param X          The itemset which the adding or deletion was called on 
	 * @param deleteFlag The flag indicating whether the preceding tree operation was an addition or 
	 *                   deletion 
	 * @return Returns a node whose parent should not be the node's parent or null if no inversed pair 
	 *         could be found. 
	 */
	private Node findInversePair(Itemset<T> X, boolean deleteFlag) {
		for(T item : this.headerTable.keySet()) {
			HeaderEntry headerEntry = this.headerTable.get(item);
			// == 1 is only needed in case of deletion 
			if(headerEntry.support > 1 || (deleteFlag && headerEntry.support == 1)) {
				Node link = headerEntry.nodeLink; 
				while(link != null) {
					int supportLink = this.headerTable.get(link.item).support; 
					if(link.parent == this.root) {
						link = link.nodeLink; 
						continue; 
					}
					int supportParent = this.headerTable.get(link.parent.item).support; 
					if(supportParent < supportLink) {
						return link; 
					} else if(supportParent == supportLink && link.parent.item.compareTo(link.item) > 0) {
						return link; 
					} 
					link = link.nodeLink; 
				}
			}
		}
		return null; 
	}
	
	/** 
	 * Compute the sum of the item counts of all descendents of a given node for a given item. 
	 *  
	 * @param node The node determining the descendents. 
	 * @param item The item of interest. 
	 * @return Returns an integer with the correct item count in the corresponding subtree. 
	 */
	private int countSum(Node node, T item) {
		int sum = 0; 
		
		for(T i : node.children.keySet()) {
			Node child = node.children.get(i); 
			if(item.equals(i)) {
				sum += child.count; 
			} else { 
				sum += this.countSum(child, item); 
			}
		}
		
		return sum; 
	}
	
	/** 
	 * Determine all distinct items stored in the descendant nodes of a given node. 
	 * 
	 * @param node The node determining the descendants of interest. 
	 * @return Returns a {@code Set<T>} of distinct items stored in the descendants of the given node. 
	 */
	private Set<T> descendants(Node node) {
		Set<T> descendents = new HashSet<>(); 
		
		for(T item : node.children.keySet()) {
			Node child = node.children.get(item); 
			this.descendants(child, descendents); 
		}
		
		return descendents; 
	}
	
	/** 
	 * Determine all distinct items stored in the descendant nodes of a given node. 
	 * 
	 * @param node        The node determining the descendants of interest. 
	 * @param descendents An accumulator set for all the distinct items. 
	 */
	private void descendants(Node node, Set<T> descendents) {
		descendents.add(node.item); 
		for(T item : node.children.keySet()) {
			Node child = node.children.get(item); 
			this.descendants(child, descendents); 
		}
	}
	
	/** 
	 * Compute all the closed itemsets currently hold in this dynamical frequent pattern tree.  
	 * 
	 * @return Returns a {@code Set<Itemset<T>>} of itemsets which are closed for the current sliding 
	 *         window. 
	 */
	public Set<Itemset<T>> getClosedItemsets() {
		return this.getClosedItemsets(this.root, new HashSet<T>()); 
	}
	
	/**
	 * Compute all the closed itemsets starting from a given node after seeing a given path. 
	 * @param node        The node to start from. 
	 * @param itemsOnPath An accumulator for the items already seen on the path to this node. 
	 * @return Returns a {@code Set<Itemset<T>>} of itemsets which are closed in the respective branch of 
	 *         the DFPTree. 
	 */
	private Set<Itemset<T>> getClosedItemsets(Node node, Set<T> itemsOnPath) {
		Set<Itemset<T>> itemsets = new HashSet<>(); 
		boolean closedFlag = true;
		for(T item : this.descendants(node)) {
			if(node.count == this.countSum(node, item)) {
				/* 
				 * If the count sum of one descendant is equal to the count of the current node then this 
				 * path cannot represent a closed itemset. 
				 */
				closedFlag = false; 
			}
			
			if(node.children.containsKey(item)) {
				Node child = node.children.get(item); 
				itemsOnPath.add(item); 
				// Recurse on the children 
				itemsets.addAll(this.getClosedItemsets(child, itemsOnPath)); 
				itemsOnPath.remove(item); 
			}
		}
		
		if(node != this.root && closedFlag) {
			itemsets.add(new Itemset<T>(itemsOnPath)); 
		}
		
		/* 
		 * Compute the downward closure with respect to the intersection operator by adding new sets 
		 * obtained from pairwise intersection.  
		 */
		Set<Itemset<T>> Cnew; 
		do {
			Cnew = new HashSet<>(); 
			for(Itemset<T> a : itemsets) {
				for(Itemset<T> b : itemsets) {
					Itemset<T> intersection = a.intersection(b); 
					if(intersection.size() > 0 && !itemsets.contains(intersection)) {
						Cnew.add(intersection); 
					}
				}
			}
			itemsets.addAll(Cnew);
		} while(!Cnew.isEmpty()); 
		
		return itemsets; 
	}
	
	// FIXME not working properly, there are cases where this function returns wrong values 
	public int support(Itemset<T> X) {
		Queue<T> queue = this.itemsetToQueue(X); 
		Node node = this.root;
		while(!queue.isEmpty()) {
			T item = queue.remove(); 
			Node child = node.children.get(item); 
			if(child == null) {
				return 0; 
			} else {
				node = child; 
			}
		}
		return node.count; 
	}

	@Override 
	public String toString() {
		// Create the string for this.headerTable 
		int maxSupportLength = -1; 
		int maxItemLength = -1;
		for(T item : this.headerTable.keySet()) {
			HeaderEntry entry = this.headerTable.get(item);  
			int supportLength = Integer.toString(entry.support).length(); 
			int itemLength = entry.item.toString().length(); 
			if(supportLength > maxSupportLength) {
				maxSupportLength = supportLength; 
			}
			if(itemLength > maxItemLength) {
				maxItemLength = itemLength; 
			}
		}
		
		String headerTableString = "\n"; 
		for(T item : this.headerTable.keySet()) {
			HeaderEntry entry = this.headerTable.get(item); 
			headerTableString += "| ";
			String itemString = entry.item.toString(); 
			String supportString = Integer.toString(entry.support); 
			headerTableString += String.format("%" + maxItemLength + "s", itemString) + " | ";
			headerTableString += String.format("%" + maxSupportLength + "s", supportString); 
			headerTableString += " |\n"; 
		}
		
		// Create the string for the actual tree
		String treeString = ""; 
		treeString += "ROOT \n"; 
		if(!this.root.children.isEmpty()) {
			treeString += "{\n"; 
			for(T item : this.root.children.keySet()) {
				Node child = this.root.children.get(item); 
				String childRepr = child.toString(); 
				childRepr = childRepr.replaceAll("(?m)^", "\t"); 
				treeString += childRepr; 
			}
			treeString += "}\n"; 
		}
	
		return treeString + headerTableString; 
	}
	
}
