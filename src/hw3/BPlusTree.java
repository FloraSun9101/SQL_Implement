package hw3;


import java.util.ArrayList;
import java.util.Arrays;

import hw1.Field;
import hw1.RelationalOperator;

public class BPlusTree {
	private Node root;
	private int pInner;
	private int pLeaf;
	
    /**
     * Constructor of the BPlusTree
     * @param pInner is the degree of the Inner node -- Maximum children it can have
     * @param pLeaf is the degree of the Leaf node -- Maximum entries it can have
     */
    public BPlusTree(int pInner, int pLeaf) {
    	this.root = new LeafNode(pLeaf);
    	this.pInner = pInner;
    	this.pLeaf = pLeaf;
    }
    
    /**
     * Search key in the B+ tree
     * @param f is the key we want to search
     * @return the LeafNode that f may be in
     */
    public LeafNode search(Field f) {
    	Node root = this.root;
    	LeafNode lfN = reachLeafNode(root, f); 	// locate the possible node that it might be in
    	if(lfN.findEntry(f) >= 0) {				// when the entry is in the node, return the node itself
    		return lfN;
    	}
    	else {									// when the entry is not in the node, return null
    		return null;
    	}
    }
    
    /**
     * 
     * @param e
     */
    public void insert(Entry e) {
    	if(this.search(e.getField()) == null) {		// if this entry is already in the tree, do nothing.
	    	LeafNode lfN = reachLeafNode(root, e.getField());
	    	if(lfN.addEntries(e)) {	 				// Whether or not the leafNode needs to be Split
	    		InnerNode p = lfN.getParent();
	        	if(p == null) {						// If the parent node do not exist, establish one
	        		p = new InnerNode(this.pInner);	// Establish the parent node
	        		this.root = p;					// set it as the root
	        		Node[] splitNodes = splitNode(lfN);
	        		p.setChildren(new ArrayList<Node>(Arrays.asList(splitNodes))); 	// add the split nodes as its children
	        		p.buildKeys();					// set its keys
	        		for(Node c : p.getChildren()) {	// set it as the parent node for each of its children
	        			c.setParent(p);
	        		}
	        		return;							// end of the split and the insert
	        	}
	        	else {
	        		InnerNode newRoot = InnerNode.replaceChild(lfN, this.pInner);
	        		if(newRoot != null) {
	        			this.root = newRoot;
	        		}
	        	}
	    	}
    	}
    	return;										// If the node does not need to be split, then just return
    }
    
    public void delete(Entry e) {
    	LeafNode lfN = this.search(e.getField());
    	if(lfN != null) {							// If the entry is in the tree, we need to delete it.
	    	int loc = lfN.findEntry(e.getField());
	    	lfN.getEntries().remove(loc);			// Delete from the leafNode;
	    	if(this.root.isLeafNode()) {			// if there is only root, it do not need to obey the degree rules
	    		if(((LeafNode) this.root).getEntries().size() == 0) {   // if root is empty, set it to null
	    			this.root = null;
	    		}														// Otherwise, do nothing
	    		return;
	    	}
	    	
	    	int nodeLoc = lfN.getParent().getChildren().indexOf(lfN);
	    	if( nodeLoc < lfN.getParent().getKeys().size()) {
	    		if(lfN.getEntries().size() > 0 ) {
	    			lfN.getParent().getKeys().set(nodeLoc, lfN.getEntries().get(lfN.getEntries().size() - 1).getField()); // Update the key
	    		}
	    	}
	    	
	    	if(lfN.getEntries().size() < ((lfN.getDegree() + 1) / 2)) { // need borrow?
	    		if(lfN.borrow()) {										// can borrow?
	    			return;
	    		}	
	    		else {													// cannot borrow -- merge LeafNode and check the validation of parent
	    			lfN.merge(); 										// merge LeafNode			
	    			InnerNode p = lfN.getParent();
	    			// if the parent is the root and this LeafNode is the only child of the parent -- collapse -- return;
	    			if(p.equals(this.getRoot())) {
	    				if(((InnerNode)this.getRoot()).getChildren().size() == 1) {
	    					this.root = ((InnerNode)this.getRoot()).getChildren().get(0);
	    				}
	    				return;
	    			}
	    			// InnerNode update =============================
	    			if(p.getChildren().size() < ((p.getDegree() + 1) / 2)) {	// whether parent needs push through or merge
	    				InnerNode newRoot = p.validate(this.getRoot());			// push through or merge
	    				if(newRoot != null) {									// collapse the level if needed
	    					this.root = newRoot;
	    				}
	    			}
	    		}
	    	}
	    	else {
	    		return;									// If the entry is not in the tree, do nothing
	    	}
    	}
    }
    
    public Node getRoot() {
    	return this.root;
    }
    
    /**
     * 
     * @param a
     * @return
     */
    public static Node[] splitNode(Node a) {
    	if(a.isLeafNode()) {											// split the leaf node needs to split its entries
    		LeafNode aLeaf = (LeafNode) a;
    		ArrayList<Entry> entries = aLeaf.getEntries();
    		
    		LeafNode aLeaf1 = new LeafNode(a.getDegree());
    		LeafNode aLeaf2 = new LeafNode(a.getDegree());
    		ArrayList<Entry> entries1 = new ArrayList<Entry>(entries.subList(0, (a.getDegree() + 2)/2));
    		ArrayList<Entry> entries2 = new ArrayList<Entry>(entries.subList((a.getDegree() + 2)/2, entries.size()));
    		aLeaf1.setEntries(entries1);
    		aLeaf2.setEntries(entries2);
    		//aLeaf1.setParent(aLeaf.getParent());
    		//aLeaf2.setParent(aLeaf.getParent());
    		return new Node[] {aLeaf1, aLeaf2};
    	}
    	else {															// split the inner node needs to split its children and update keys
    		InnerNode aInno = (InnerNode) a;
    		ArrayList<Node> children = aInno.getChildren();
    		ArrayList<Field> keys = aInno.getKeys();
    		
    		InnerNode aInno1 = new InnerNode(a.getDegree());
    		InnerNode aInno2 = new InnerNode(a.getDegree());
    		ArrayList<Node> children1 = new ArrayList<Node>(children.subList(0, (a.getDegree() + 2)/2));
    		ArrayList<Node> children2 = new ArrayList<Node>(children.subList((a.getDegree() + 2)/2, children.size()));
    		ArrayList<Field> keys1 = new ArrayList<Field>(keys.subList(0, (a.getDegree() + 2)/2));
    		ArrayList<Field> keys2 = new ArrayList<Field>(keys.subList((a.getDegree() + 2)/2, keys.size()));
    		aInno1.setChildren(children1);
    		aInno2.setChildren(children2);
    		aInno1.setKeys(keys1);
    		aInno2.setKeys(keys2);
    		for(Node child : aInno1.getChildren()) {
    			child.setParent(aInno1);
    		}
    		
    		for(Node child : aInno2.getChildren()) {
    			child.setParent(aInno2);
    		}
    		return new Node[]{aInno1, aInno2};
    	}
    }
    
    /**
     * A recursive method used to
     * Find the possible LeafNode that key may be in.
     * Called in search
     * @param a is the node where we start
     * @param key is the key we want to search
     * @return LeafNode that key may be in.
     */
    private static LeafNode reachLeafNode(Node a, Field key) {
    	if(a.isLeafNode()) {
    		return (LeafNode)a;
    	}
    	else {
    		InnerNode aInno = (InnerNode) a;
    		ArrayList<Field> keys = aInno.getKeys();
    		int i;
    		for(i = 0; i < keys.size(); i++) {
    			if(key.compare(RelationalOperator.LTE, keys.get(i))) {
    				return reachLeafNode(aInno.getChildren().get(i), key);
    			}
    		}
    		return reachLeafNode(aInno.getChildren().get(i), key);
    	}
    }
    
}
