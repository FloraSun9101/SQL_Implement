package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw3.BPlusTree;
import hw3.Entry;
import hw3.InnerNode;
import hw3.LeafNode;
import hw3.Node;

public class BPlusTreeTest {

	@Test
	public void testSimpleInsert() {
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 1));
		assertTrue(bt.getRoot().isLeafNode());

		LeafNode l = (LeafNode)bt.getRoot();

		assertTrue(l.getEntries().get(0).getField().equals(new IntField(4)));
		assertTrue(l.getEntries().get(1).getField().equals(new IntField(9)));

		assertTrue(l.getEntries().get(0).getPage() == 1);
		assertTrue(l.getEntries().get(1).getPage() == 0);


	}

	@Test
	public void testComplexInsert() {

		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));

		//verify root properties
		Node root = bt.getRoot();

		assertTrue(root.isLeafNode() == false);

		InnerNode in = (InnerNode)root;

		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();

		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(7)));

		//grab left and right children from root
		InnerNode l = (InnerNode)c.get(0);
		InnerNode r = (InnerNode)c.get(1);

		assertTrue(l.isLeafNode() == false);
		assertTrue(r.isLeafNode() == false);

		//check values in left node
		ArrayList<Field> kl = l.getKeys();
		ArrayList<Node> cl = l.getChildren();

		assertTrue(kl.get(0).compare(RelationalOperator.EQ, new IntField(2)));
		assertTrue(kl.get(1).compare(RelationalOperator.EQ, new IntField(4)));

		//get left node's children, verify
		Node ll = cl.get(0);
		Node lm = cl.get(1);
		Node lr = cl.get(2);

		assertTrue(ll.isLeafNode());
		assertTrue(lm.isLeafNode());
		assertTrue(lr.isLeafNode());

		LeafNode lll = (LeafNode)ll;
		LeafNode lml = (LeafNode)lm;
		LeafNode lrl = (LeafNode)lr;

		ArrayList<Entry> ell = lll.getEntries();

		assertTrue(ell.get(0).getField().equals(new IntField(1)));
		assertTrue(ell.get(1).getField().equals(new IntField(2)));

		ArrayList<Entry> elm = lml.getEntries();

		assertTrue(elm.get(0).getField().equals(new IntField(3)));
		assertTrue(elm.get(1).getField().equals(new IntField(4)));

		ArrayList<Entry> elr = lrl.getEntries();

		assertTrue(elr.get(0).getField().equals(new IntField(6)));
		assertTrue(elr.get(1).getField().equals(new IntField(7)));

		//verify right node
		ArrayList<Field> kr = r.getKeys();
		ArrayList<Node> cr = r.getChildren();

		assertTrue(kr.get(0).compare(RelationalOperator.EQ, new IntField(9)));

		//get right node's children, verify
		Node rl = cr.get(0);
		Node rr = cr.get(1);

		assertTrue(rl.isLeafNode());
		assertTrue(rr.isLeafNode());

		LeafNode rll = (LeafNode)rl;
		LeafNode rrl = (LeafNode)rr;

		ArrayList<Entry> erl = rll.getEntries();

		assertTrue(erl.get(0).getField().equals(new IntField(9)));

		ArrayList<Entry> err = rrl.getEntries();

		assertTrue(err.get(0).getField().equals(new IntField(10)));
		assertTrue(err.get(1).getField().equals(new IntField(12)));
	}

	@Test
	public void testSearch() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));

		//these values should exist
		assertTrue(bt.search(new IntField(12)) != null);
		assertTrue(bt.search(new IntField(3)) != null);
		assertTrue(bt.search(new IntField(7)) != null);

		//these values should not exist
		assertTrue(bt.search(new IntField(8)) == null);
		assertTrue(bt.search(new IntField(11)) == null);
		assertTrue(bt.search(new IntField(5)) == null);

	}
	
	@Test
	public void testHigherDegrees() {
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
		
		//verify root properties
				Node root = bt.getRoot();

				assertTrue(root.isLeafNode() == false);

				InnerNode in = (InnerNode)root;

				ArrayList<Field> k = in.getKeys();
				ArrayList<Node> c = in.getChildren();

				assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(2)));
				assertTrue(k.get(1).compare(RelationalOperator.EQ, new IntField(4)));
				assertTrue(k.get(2).compare(RelationalOperator.EQ, new IntField(7)));

				//grab left and right children from root
				LeafNode c0 = (LeafNode)c.get(0);
				LeafNode c1 = (LeafNode)c.get(1);
				LeafNode c2 = (LeafNode)c.get(2);
				LeafNode c3 = (LeafNode)c.get(3);
				assertTrue(c0.isLeafNode() == true);
				assertTrue(c1.isLeafNode() == true);
				assertTrue(c2.isLeafNode() == true);
				assertTrue(c3.isLeafNode() == true);

				

				//check values in left node
				ArrayList<Entry> c0entries = c0.getEntries();

				assertTrue(c0entries.get(0).getField().equals(new IntField(1)));
				assertTrue(c0entries.get(1).getField().equals(new IntField(2)));

				//check values in second node
				ArrayList<Entry> c1entries = c1.getEntries();

				assertTrue(c1entries.get(0).getField().equals(new IntField(3)));
				assertTrue(c1entries.get(1).getField().equals(new IntField(4)));
				
				//check values in third node
				ArrayList<Entry> c2entries = c2.getEntries();

				assertTrue(c2entries.get(0).getField().equals(new IntField(6)));
				assertTrue(c2entries.get(1).getField().equals(new IntField(7)));
				
				//check values in right most node
				ArrayList<Entry> c3entries = c3.getEntries();

				assertTrue(c3entries.get(0).getField().equals(new IntField(9)));
				assertTrue(c3entries.get(1).getField().equals(new IntField(10)));
				assertTrue(c3entries.get(2).getField().equals(new IntField(12)));



	}

	@Test
	public void testDelete() {
		//Create a tree, then delete some values

		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));

		bt.delete(new Entry(new IntField(7), 0));
		bt.delete(new Entry(new IntField(3), 0));
		bt.delete(new Entry(new IntField(4), 0));
		bt.delete(new Entry(new IntField(10), 0));
		bt.delete(new Entry(new IntField(2), 0));

		//verify root properties
		Node root = bt.getRoot();

		assertTrue(root.isLeafNode() == false);

		InnerNode in = (InnerNode)root;

		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();

		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(6)));

		//grab left and right children from root
		InnerNode l = (InnerNode)c.get(0);
		InnerNode r = (InnerNode)c.get(1);

		assertTrue(l.isLeafNode() == false);
		assertTrue(r.isLeafNode() == false);

		//check values in left node
		ArrayList<Field> kl = l.getKeys();
		ArrayList<Node> cl = l.getChildren();

		assertTrue(kl.get(0).compare(RelationalOperator.EQ, new IntField(1)));

		//get left node's children, verify
		Node ll = cl.get(0);
		Node lr = cl.get(1);

		assertTrue(ll.isLeafNode());
		assertTrue(lr.isLeafNode());

		LeafNode lll = (LeafNode)ll;
		LeafNode lrl = (LeafNode)lr;

		ArrayList<Entry> ell = lll.getEntries();

		assertTrue(ell.get(0).getField().equals(new IntField(1)));

		ArrayList<Entry> elr = lrl.getEntries();

		assertTrue(elr.get(0).getField().equals(new IntField(6)));

		//verify right node
		ArrayList<Field> kr = r.getKeys();
		ArrayList<Node> cr = r.getChildren();

		assertTrue(kr.get(0).compare(RelationalOperator.EQ, new IntField(9)));

		//get right node's children, verify
		Node rl = cr.get(0);
		Node rr = cr.get(1);

		assertTrue(rl.isLeafNode());
		assertTrue(rr.isLeafNode());

		LeafNode rll = (LeafNode)rl;
		LeafNode rrl = (LeafNode)rr;

		ArrayList<Entry> erl = rll.getEntries();

		assertTrue(erl.get(0).getField().equals(new IntField(9)));

		ArrayList<Entry> err = rrl.getEntries();

		assertTrue(err.get(0).getField().equals(new IntField(12)));
	}
	/*
	 * search: if the key exists, what if the key do not exist. only the root node. Appliable for String
	 * insert: no split; do need split(update the search tree ; split leaf not parent, split leaf and the parent, split the root)
	 * 			only the root; insert duplicate(nothing should happen; )
	 * delete: if it does not exist, merging(leaf node can not fulfill the properties), rebalancing(inner node -- push through
	 * 			-- when inner node can not fulfill the properties), borrowing (right or left), simple case, collapsing the level
	 * 			root deletion
	 * 
	 * different degrees and int or String, 
	 */
}
