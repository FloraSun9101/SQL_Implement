package hw4;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.Tuple;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool which check that the transaction has the appropriate
 * locks to read/write the page.
 */
public class BufferPool {
    /** Bytes per page, including header. */
    public static final int PAGE_SIZE = 4096;

    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;

    private int numPages;
    private Catalog c;
    private Map<String, PageCache> pool;
    private Map<Integer, HashSet<String>> trans;
     
    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        this.numPages = numPages;
        this.pool = new HashMap<>();
        this.trans = new HashMap<>();
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param tableId the ID of the table with the requested page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public HeapPage getPage(int tid, int tableId, int pid, Permissions perm)
        throws Exception {
    	PageCache pageC = this.pool.get(tableId + "-" + pid);									// locate the page in the buffer pool
    	if(pageC == null) {																		// if the page is not in the pool
    		HeapPage page = Database.getCatalog().getDbFile(tableId).readPage(pid);				// fetch it from the disk
    		pageC = new PageCache(page);
    	}
    	if(pool.size() == this.numPages) {														// if the pool is full
    		this.evictPage();																	// evict an un-dirty page from the pool 
   		}
    	
    	HashSet<String> locks = this.trans.getOrDefault(tid, new HashSet<String>());
    	this.trans.put(tid, locks);
    	if(locks.contains(tableId + "-" + pid)) {
    		if(perm.permLevel == 0) {}
    		else if (perm.permLevel == 1 && pageC.getRead() < 2) {
    			if(pageC.getWrite() != tid) {
    				pageC.releaseRead();
    				pageC.setWrite(tid);
    			}
    		}
    		else {
    			transactionComplete(tid, false);													// if the transaction can not get the lock it wants, abort it
	    		return null;
    		}
    	}
    	else {
	    	if(perm.permLevel == 0 && pageC.getWrite() == -1) {										// read lock
	    		pageC.addRead();
	    		locks.add(tableId + "-" + pid);
	    	}
	    	else if(perm.permLevel == 1 && pageC.getRead() == 0 && pageC.getWrite() == -1) { 		// write lock
	    		pageC.setWrite(tid);
	    		locks.add(tableId + "-" + pid);
	    	}
	    	else {
	    		transactionComplete(tid, false);													// if the transaction can not get the lock it wants, abort it
	    		return null;
	    	}
	    }
    	
	    this.pool.put(tableId + "-" + pid, pageC);
	    return pageC.getPage();
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param tableID the ID of the table containing the page to unlock
     * @param pid the ID of the page to unlock
     */
    public void releasePage(int tid, int tableId, int pid) {
    	String key = tableId + "-" + pid;
    	PageCache pageC = this.pool.get(key);
    	pageC.setWrite(-1);
    	pageC.eraseRead();
    	for(Entry en : this.trans.entrySet()) {
    		HashSet<String> locks = (HashSet<String>)en.getValue();
    		locks.remove(key);
    	}
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public boolean holdsLock(int tid, int tableId, int pid) {
    	HashSet<String> locks = this.trans.getOrDefault(tid, new HashSet<String>());
        if(locks.contains(tableId + "-" + pid)) {
        	return true;
        }
        return false;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction. If the transaction wishes to commit, write
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public   void transactionComplete(int tid, boolean commit)
        throws IOException {
    	HashSet<String> locks = this.trans.get(tid);
    	this.trans.remove(tid);
        if(commit) {
        	for(String s : locks) {
        		PageCache pageC = this.pool.get(s);
        		if(pageC.getWrite() == tid) {
        			if(pageC.isDirty()) {
        				String[] a = s.split("-");
                		int tableId = Integer.parseInt(a[0]);
        				Database.getCatalog().getDbFile(tableId).writePage(pageC.getPage());
        			}
        			pageC.setWrite(-1);
        		}
        		else {
        			pageC.releaseRead();
        		}
        	}
        }
        else {
        	for(String s : locks) {
        		PageCache pageC = this.pool.get(s);
        		if(pageC.getWrite() == tid) {
        			if(pageC.isDirty()) {
        				String[] a = s.split("-");
                		int tableId = Integer.parseInt(a[0]);
        				int pid = Integer.parseInt(a[1]);
        				flushPage(tableId, pid);
        			}
        			pageC.setWrite(-1);
        		}
        		else {
        			pageC.releaseRead();
        		}
        	}
        }
    }

    /**
     * Add a tuple to the specified table behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to. May block if the lock cannot 
     * be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public  void insertTuple(int tid, int tableId, Tuple t)
        throws Exception {
    	HeapFile table = Database.getCatalog().getDbFile(tableId);
    	HeapPage hp = table.addTuple(t);
    	PageCache pageC = this.pool.get(tableId + "-" + hp.getId());
    	if(pageC.getWrite() != tid) {
    		this.transactionComplete(tid, false);
    		throw new Exception();
    	}
    	pageC.setPage(hp);
    	pageC.setDirty(true);    	
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from. May block if
     * the lock cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty.
     *
     * @param tid the transaction adding the tuple.
     * @param tableId the ID of the table that contains the tuple to be deleted
     * @param t the tuple to add
     */
    public  void deleteTuple(int tid, int tableId, Tuple t)
        throws Exception {
    	HeapFile table = Database.getCatalog().getDbFile(tableId);
    	HeapPage hp = table.deleteTuple(t);
    	if(hp != null) {
	    	PageCache pageC = this.pool.get(tableId + "-" + hp.getId());
	    	if(pageC.getWrite() != tid) {
	    		this.transactionComplete(tid, false);
	    		throw new Exception();
	    	}
	    	pageC.setPage(hp);
	    	pageC.setDirty(true);
    	}
    }
    
    /**
     * Reread the page from the disk and mark it as clean.
     * @param tableId
     * @param pid
     * @throws IOException
     */
    private synchronized  void flushPage(int tableId, int pid) throws IOException {
        PageCache pageC = this.pool.get(tableId + "-" + pid);
        pageC.setPage(Database.getCatalog().getDbFile(tableId).readPage(pid));
        pageC.setDirty(false);
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void evictPage() throws Exception {
        for(Entry en : this.pool.entrySet()) {
        	PageCache pageC = (PageCache)en.getValue();
        	if(!pageC.isDirty()) {
        		this.pool.remove(en.getKey());
        		return;
        	}
        }
    }

}

class PageCache {
	private HeapPage page;
	private boolean dirty;
	private int read;
	private int write;
	
	public PageCache(HeapPage p) {
		this.page = p;
		this.dirty = false;
		this.read = 0;
		this.write = -1;
	}
	
	public HeapPage getPage() {
		return this.page;
	}
	
	public int getRead() {
		return this.read;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public int getWrite() {
		return this.write;
	}
	
	public void setPage(HeapPage page) {
		this.page = page;
	}
	
	public void addRead() {
		this.read++;
	}
	
	public void releaseRead() {
		this.read--;
	}
	
	public void eraseRead() {
		this.read = 0;
	}
	
	public void setWrite(int w) {
		this.write = w;
	}
	
	public void setDirty(boolean d) {
		this.dirty = d;
	}
}
