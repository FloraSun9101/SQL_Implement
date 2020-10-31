package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.StringField;
import hw3.BPlusTree;
import hw3.Entry;
import hw3.InnerNode;
import hw3.LeafNode;
import hw3.Node;

public class YourHW3Tests {

	@Test
	/*
	 * test the situation where String can be insert into the BPlusTree, and deleted.
	 * Also, deletion when there is only one node (root node) in the tree is also tested.
	 */
	public void testDeleteStringOnlyOneNode() {
		BPlusTree bt = new BPlusTree(5,4);
		bt.insert(new Entry(new StringField("test1"), 0));
		bt.insert(new Entry(new StringField("test2"), 1));
		
		bt.delete(new Entry(new StringField("test1"), 0));
		
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode());
		
		LeafNode l = (LeafNode) root;
		
		assertTrue(l.getEntries().get(0).getField().equals(new StringField("test2")));
		assertTrue(l.getEntries().get(0).getPage() == 1);
	}
	
	@Test
	/*
	 * test HigherDegrees deletion for int.
	 * Only involves merge.
	 */
	public void testHigherDegreesDelete() {
		BPlusTree bt = new BPlusTree(4, 3);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		bt.delete(new Entry(new IntField(4), 0));
		bt.delete(new Entry(new IntField(6), 0));	// That is where merge should happen
		
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode() == false);
		
		InnerNode in = (InnerNode)root;
		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();
		
		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(7)));
		assertTrue(k.size() == 1);
		
		LeafNode c0 = (LeafNode)c.get(0);
		LeafNode c1 = (LeafNode)c.get(1);
		assertTrue(c0.isLeafNode());
		assertTrue(c1.isLeafNode());
		
		//check values in left node
		ArrayList<Entry> c0Entities = c0.getEntries();
		assertTrue(c0Entities.get(0).getField().compare(RelationalOperator.EQ, new IntField(1)));
		assertTrue(c0Entities.get(1).getField().compare(RelationalOperator.EQ, new IntField(2)));
		assertTrue(c0Entities.get(2).getField().compare(RelationalOperator.EQ, new IntField(7)));
		
		//check values in right node
		ArrayList<Entry> c1Entities = c1.getEntries();
		assertTrue(c1Entities.get(0).getField().compare(RelationalOperator.EQ, new IntField(9)));
		assertTrue(c1Entities.get(1).getField().compare(RelationalOperator.EQ, new IntField(12)));

	}

}
