package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class LeafNode implements Node {
	private int degree;
	private ArrayList<Entry> entries;
	private InnerNode parent;
	
	public LeafNode(int degree) {
		this.degree = degree;
		this.entries = new ArrayList<Entry>();
		this.parent = null;
	}
	
	public ArrayList<Entry> getEntries() {
		return this.entries;
	}

	public int getDegree() {
		return this.degree;
	}
	
	public boolean isLeafNode() {
		return true;
	}
	
	public void setParent(InnerNode p) {
		this.parent = p;
	}
	
	public InnerNode getParent() {
		return this.parent;
	}
	
	public boolean addEntries(Entry en) {	// add entries
		int n = entries.size();
		int i = 0;
		for(i = 0; i < n; i++) {
			if(en.getField().compare(RelationalOperator.EQ, entries.get(i).getField())) {
				return false;  				// If the entrance already exists in the tree, do nothing 
			}
			else if(en.getField().compare(RelationalOperator.LT, entries.get(i).getField())) {
				this.entries.add(i, en);
				break;
			}
		}
		if(i == n) {
			this.entries.add(en);
		}
		
		if(n + 1 > this.degree) {
			return true;            // need split
		}
		else {
			return false;			// do not need split 
		}
	}
	
	public void setEntries(ArrayList<Entry> ens) {
		this.entries = ens;
	}
	
	public int findEntry(Field f) {
    	ArrayList<Entry> entries = this.entries;
		for(int i = 0; i < entries.size(); i++) {
			if(f.compare(RelationalOperator.EQ, entries.get(i).getField())) {
				return i;
			}
		}
		return -1;
    }
	
	public boolean borrow() {
		InnerNode p = this.getParent();
		int loc = p.getChildren().indexOf(this);
		if(loc > 0) {
			LeafNode leftSib = (LeafNode)p.getChildren().get(loc - 1);
			if(leftSib.getEntries().size() > ((leftSib.getDegree() + 1) / 2)) {
				this.getEntries().add(0, leftSib.getEntries().get(leftSib.getEntries().size() - 1));
				leftSib.getEntries().remove(leftSib.getEntries().size() - 1);
				p.getKeys().set(loc - 1, leftSib.getEntries().get(leftSib.getEntries().size() - 1).getField());
				return true;
			}
		}
		if(loc < p.getChildren().size() - 1) {
			LeafNode rightSib = (LeafNode)p.getChildren().get(loc + 1);
			if(rightSib.getEntries().size() > ((rightSib.getDegree() + 1) / 2)) {
				this.getEntries().add(this.getEntries().size(), rightSib.getEntries().get(rightSib.getEntries().size() - 1));
				rightSib.getEntries().remove(rightSib.getEntries().size() - 1);
				p.getKeys().set(loc, this.getEntries().get(this.getEntries().size() - 1).getField());
				return true;
			}
		}
		return false;
	}
	
	public void merge() {
		InnerNode p = this.getParent();
		int loc = p.getChildren().indexOf(this);
		if(loc > 0) {
			LeafNode leftSib = (LeafNode)p.getChildren().get(loc - 1);
			for(int i = 0; i < this.getEntries().size(); i++) {
				int tail = leftSib.getEntries().size();
				leftSib.getEntries().add(tail, this.getEntries().get(i));
			}
			if(loc < p.getKeys().size()) {
				p.getKeys().remove(loc);
			}
			p.getChildren().remove(loc);
			return;
		}
		if(loc < p.getChildren().size() - 1) {
			LeafNode rightSib = (LeafNode)p.getChildren().get(loc + 1);
			for(int i = this.getEntries().size() - 1; i >= 0; i--) {
				rightSib.getEntries().add(0, this.getEntries().get(i));
			}
			p.getKeys().remove(loc);
			p.getChildren().remove(loc);
			return;
		}
	}
}