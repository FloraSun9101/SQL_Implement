package hw3;

import java.util.ArrayList;
import java.util.Arrays;

import hw1.Field;
import hw1.RelationalOperator;

public class InnerNode implements Node {
	private int degree;
	private ArrayList<Field> keys;
	private ArrayList<Node> children;
	private InnerNode parent;
	
	public InnerNode(int degree) {
		this.degree = degree;
		this.keys = new ArrayList<Field>();
		this.children = new ArrayList<Node>();
		this.parent = null;
	}
	
	public ArrayList<Field> getKeys() {
		return this.keys;
	}
	
	public ArrayList<Node> getChildren() {
		return this.children;
	}

	public int getDegree() {
		return this.degree;
	}
	
	public boolean isLeafNode() {
		return false;
	}


	public static InnerNode replaceChild(Node lfN, int InnoDegree) {
		Node[] splitNodes = BPlusTree.splitNode(lfN);
		InnerNode p = lfN.getParent();
		if(p == null) {
			p = new InnerNode(InnoDegree);
			p.setChildren(new ArrayList<Node>(Arrays.asList(splitNodes)));
			p.buildKeys();
			for(Node c : splitNodes) {
				c.setParent(p);
			}
			return p;
		}
		
		int loc = p.getChildren().indexOf(lfN); // Locate the original node
		p.getChildren().remove(loc);			// Delete the original node
		for(int i = splitNodes.length - 1; i >= 0; i--) {
			p.getChildren().add(loc, splitNodes[i]);	// add each split node as the parent node's children
			p.getChildren().get(loc).setParent(p);		// set the parent node as the parent node for each of the split node
		}
		p.updateKeys(loc);
		if(p.getChildren().size() > p.getDegree()) {
			return replaceChild(p, InnoDegree);
		}
		return null;
	}
	
	
	/**
	 * Set the keys entirely
	 * Called in add Children
	 * If the children are LeafNode, add the largest field in each node to the keys
	 * If the children are InnerNode, move the largest field in each node to the keys
	 * --- Can be optimized
	 */
	public void buildKeys() {
		ArrayList<Field> keys = new ArrayList<Field>();
		if(this.getChildren().get(0).isLeafNode()) {			// If children are LeafNode, add the largest to the key
			for(int i = 0; i < this.children.size() - 1; i++) {
				LeafNode childL = (LeafNode) this.children.get(i);
				keys.add(childL.getEntries().get(childL.getEntries().size() - 1).getField());
			}
		}
		else {													 // If children are InnerNode, move the largest one to the upper node 
			for(int i = 0; i < this.children.size() - 1; i++) {
				InnerNode childI = (InnerNode) this.children.get(i);
				int n = childI.getKeys().size();
				keys.add(childI.getKeys().get(n - 1));
				childI.getKeys().remove(n - 1);
			}
		}
		this.keys = keys;
	}
	
	/**
	 * Update just one Keys for insert -- After split, there would be a key that should be inserted
	 * 
	 */
	public void updateKeys(int loc) {
		if(this.getChildren().get(loc).isLeafNode()) {
			for(int i = 0; i < 2; i++) {
				LeafNode lfN = (LeafNode) this.getChildren().get(loc + i);
				Field maxKey = lfN.getEntries().get(lfN.getEntries().size() - 1).getField();
				if(loc >= this.keys.size() || maxKey.compare(RelationalOperator.LT, this.keys.get(loc))) {
					this.keys.add(loc , maxKey);
					return;
				}
				if(maxKey.compare(RelationalOperator.GT, this.keys.get(loc))) {
					this.keys.add(loc + 1, maxKey);
					return;
				}
			}
		}
		else {
			InnerNode innoN = (InnerNode) this.getChildren().get(loc);;
			Field maxKey = innoN.keys.get(innoN.keys.size() - 1);
			this.keys.add(loc, maxKey);
			innoN.keys.remove(innoN.keys.size() - 1);
		}
	}
	
	public void setChildren(ArrayList<Node> children) {
		this.children = children;
	}	
	
	public void setKeys(ArrayList<Field> keys) {
		this.keys = keys;
	}
	
	public InnerNode getParent() {
		return this.parent;
	}
	
	public void setParent(InnerNode p) {
		this.parent = p;
	}
	
	public boolean pushThrough() {
		InnerNode p = this.getParent();
		int loc = p.getChildren().indexOf(this);
		if(loc > 0) {
			InnerNode leftSib = (InnerNode)p.getChildren().get(loc - 1);
			if(leftSib.getChildren().size() > ((leftSib.getDegree() + 1) / 2)) {
				int lastChildIn = leftSib.getChildren().size() - 1;
				this.getChildren().add(0, leftSib.getChildren().get(lastChildIn));			// get the last child of the leftSib
				leftSib.getChildren().remove(lastChildIn);									// delete the last child from the leftSib
				this.getKeys().add(0, p.getKeys().get(loc - 1));
				p.getKeys().set(loc - 1, leftSib.getKeys().get(lastChildIn - 1));
				leftSib.getKeys().remove(lastChildIn - 1);
				return true;
			}
		}
		if(loc < p.getChildren().size() - 1) {
			InnerNode rightSib = (InnerNode)p.getChildren().get(loc + 1);
			if(rightSib.getChildren().size() > ((rightSib.getDegree() + 1) / 2)) {
				this.getChildren().add(rightSib.getChildren().get(0));						// get the first child of the rightSib
				rightSib.getChildren().remove(0);											// delete the first child from the rightSib
				this.getKeys().set(this.getChildren().size() - 2, p.getKeys().get(loc));
				p.getKeys().set(loc, rightSib.getKeys().get(0));
				rightSib.getKeys().remove(0);
				return true;
			}
		}
		return false;
	}
	
	public void merge() {
		InnerNode p = this.getParent();
		int loc = p.getChildren().indexOf(this);
		if(loc > 0) {
			InnerNode leftSib = (InnerNode)p.getChildren().get(loc - 1);
			for(int i = 0; i < this.getChildren().size(); i++) {
				leftSib.getChildren().add(this.getChildren().get(i));
				if(i < this.getChildren().size() - 1) {
					leftSib.getKeys().add(this.getKeys().get(i));
				}
			}
			leftSib.getKeys().add(p.getKeys().get(loc - 1));
			if(this.getKeys().size() > 0) {
				p.getKeys().set(loc - 1, this.getKeys().get(p.getKeys().size() - 1));
			}
			p.getChildren().remove(loc);
			return;
		}
		if(loc < p.getChildren().size() - 1) {
			InnerNode rightSib = (InnerNode)p.getChildren().get(loc + 1);
			for(int i = this.getChildren().size() - 1; i >= 0; i--) {
				rightSib.getChildren().add(0, this.getChildren().get(i));
				if(i < this.getChildren().size() - 1) {
					rightSib.getKeys().add(0,this.getKeys().get(i));
				}
			}
			p.getChildren().remove(loc);
			p.getKeys().remove(loc);
			return;
		}
	}
	
	public InnerNode validate(Node root) {
		if(this.pushThrough()) {
			return null;
		}
		else {
			this.merge();
			InnerNode p = this.getParent();
			if(p == root) {						// base case 1, collapse the tree level, remove root. This node will be the new root.
				if(p.getChildren().size() == 1) {
    				return (InnerNode)p.getChildren().get(0);
    			}
    			return null;
			}
			if(p.getChildren().size() < ((p.getDegree() + 1) / 2)) {	// parent needs further push through or merge.
				return p.validate(root);
			}
			return null;						// base case 2, parent is valid, do not need to push through or merge.
		}
	}

}