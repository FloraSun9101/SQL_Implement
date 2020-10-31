package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import hw4.BufferPool;
import hw4.Permissions;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {
	
	public static final int PAGE_SIZE = 4096;
	private TupleDesc td;
	private File file;
	
	
	/**
	 * Creates a new heap file in the given location that can accept tuples of the given type
	 * @param f location of the heap file
	 * @param types type of tuples contained in the file
	 */
	public HeapFile(File f, TupleDesc type) {
		td = type;
		file = f;
	}
	
	public File getFile() {
		return file;
	}
	
	public TupleDesc getTupleDesc() {
		return td;
	}
	
	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
	 * should be used here.
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 */
	public HeapPage readPage(int id) {
		byte[] data = new byte[PAGE_SIZE];
		HeapPage page;
		try {
			RandomAccessFile f = new RandomAccessFile(this.getFile(),"r");
			f.seek(id * PAGE_SIZE);
			f.read(data);
			page = new HeapPage(id,data,this.getId());
			f.close();
			return page;
		}
		catch(IOException e) {
			System.out.println("Fail to read the file.");
			return null;
		}
	}
	
	/**
	 * Returns a unique id number for this heap file. Consider using
	 * the hash of the File itself.
	 * @return
	 */
	public int getId() {
		return this.hashCode();
	}
	
	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the file,
	 * a RandomAccessFile object should be used in this method.
	 * @param p the page to write to disk
	 */
	public void writePage(HeapPage p) {
		byte[] data = p.getPageData();
		try {
			RandomAccessFile f = new RandomAccessFile(this.getFile(),"rw");
			f.seek(p.getId() * PAGE_SIZE);
			f.write(data);
			f.close();
		}
		catch(IOException e) {
			System.out.println("Fail to read the file.");
		}
	}
	
	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating a new page
	 * if all others are full. It then passes the tuple to this page to be stored. It then writes
	 * the page to disk (see writePage)
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 */
	public HeapPage addTuple(Tuple t) {
		try {
			if (this.getNumPages() == 0) {
				byte[] data = new byte[PAGE_SIZE];
				HeapPage hp = new HeapPage(0, data, this.getId());
				hp.addTuple(t);
				// this.writePage(hp);
				return hp;
			}
			else {
				int i = 0;
				while(i < this.getNumPages()) {
					try {
						HeapPage hp = this.readPage(i);
						hp.addTuple(t);
						// this.writePage(hp);
						return hp;
					}
					catch (Exception e) {
						i++;
					}
				}
			
				byte[] data = new byte[PAGE_SIZE];
				HeapPage hp = new HeapPage(i, data, this.getId());
				hp.addTuple(t);
				// this.writePage(hp);
				return hp;
			}
		}
		catch(Exception e) {
			return null;
		}
	}
	
	/** 
	 * Add tuple into the table when we introduced transaction and locks
	 * @param t
	 * @return
	 */
	/*
	public HeapPage addTuple(int transid, Tuple t) {
		BufferPool bp = Database.getBufferPool();
		try {
			if (this.getNumPages() == 0) {
				byte[] data = new byte[PAGE_SIZE];
				HeapPage hp = new HeapPage(0, data, this.getId());
				hp = bp.getPage(transid, this.getId(), 0, Permissions.READ_WRITE);
				if(hp != null) {
					hp.addTuple(t);
				}
				return hp;
			}
			else {
				int i = 0;
				while(i < this.getNumPages()) {
					try {
						HeapPage hp = this.readPage(i);
						hp = bp.getPage(transid, this.getId(), 0, Permissions.READ_WRITE);
						if(hp != null) {
							hp.addTuple(t);
						}
						return hp;
					}
					catch (Exception e) {
						i++;
					}
				}
			
				byte[] data = new byte[PAGE_SIZE];
				HeapPage hp = new HeapPage(i, data, this.getId());
				hp.addTuple(t);
				this.writePage(hp);
				return hp;
			}
		}
		catch(Exception e) {
			return null;
		}
	}
	*/
	
	/**
	 * This method will examine the tuple to find out where it is stored, then delete it
	 * from the proper HeapPage. It then writes the modified page to disk.
	 * @param t the Tuple to be deleted
	 */
	public HeapPage deleteTuple(Tuple t){
		int i = 0;
		while(i < this.getNumPages()) {
			HeapPage hp = this.readPage(i);
			try {
				hp.deleteTuple(t);
				// this.writePage(hp);
				return hp;
			}
			catch(Exception e) {
				i++;
			}
		}
		return null;
	}

	
	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * @return
	 */
	public ArrayList<Tuple> getAllTuples() {
		ArrayList<Tuple> t = new ArrayList<Tuple>(); 
		for (int i = 0; i < this.getNumPages(); i++) {
			HeapPage hp = this.readPage(i);
			Iterator<Tuple> it = hp.iterator();
			while(it.hasNext()) {
				t.add(it.next());
			}
		}
		
		return t;
	}
	
	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * @return the number of pages
	 */
	public int getNumPages() {
		File f = this.getFile();
		return (int)(f.length() / PAGE_SIZE);
	}
}
