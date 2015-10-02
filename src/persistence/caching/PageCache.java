package persistence.caching;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.omg.CORBA.INITIALIZE;

public class PageCache implements OnWriteEventHandler {
	private static Random rand = new Random(123);

	private final int MAX_CACHED_PAGES;
	private final int PAGE_SIZE;
	private final int ENTRY_SIZE;

	private CacheFileHandler cacheFile;
	private HashMap<Integer, PageReference> pageCache = new HashMap<Integer, PageReference>();
	private HashSet<Integer> dirtyPages = new HashSet<Integer>();
	private int[] markerMap;
	private int numMarkers = 0;
	private int maxFetchedPage = -1;


	public PageCache(String mainFile, String rollbackFile, int maxCachedPage, int pageSize, int entrySize) throws IOException {
		cacheFile = new CacheFileHandler(mainFile, new RollbackLog(rollbackFile, pageSize), pageSize);
		this.MAX_CACHED_PAGES = maxCachedPage;
		this.PAGE_SIZE = pageSize;
		this.ENTRY_SIZE = entrySize;

		markerMap = new int[MAX_CACHED_PAGES];
	}

	private void evict() throws Exception {
		if (numMarkers != MAX_CACHED_PAGES) return;
		int markerInd = rand.nextInt(numMarkers);
		
		int count = 0;
		while (count < MAX_CACHED_PAGES) {
			int pageID = markerMap[markerInd];
			PageReference pageRef = pageCache.get(pageID);
			if (!pageRef.isReferenced()) {
				Page page = pageRef.getPage();
				pageRef.invalidate();
				
				pageCache.remove(pageID);
				if (dirtyPages.remove(page.getPageID())) cacheFile.writePage(page);
				
				if (markerInd != --numMarkers) {
					markerMap[markerInd] = markerMap[numMarkers];
				}
				System.out.println("Cache miss, evicting page " + pageID);
				return;
			}
			count++;
			markerInd = (markerInd + 1) % numMarkers;
		}
		throw new IllegalStateException("Too many leased pages, cannot evict");
	}


	private PageReference bring(int pageID) throws Exception {
		PageReference pageRef = pageCache.get(pageID);
		if (pageRef == null) {
			evict();
			pageRef = new PageReference(cacheFile.readPage(pageID));
			pageCache.put(pageID, pageRef);
			markerMap[numMarkers++] = pageID;
			if (pageID > maxFetchedPage) maxFetchedPage = pageID;
		}
		if (pageRef.getPage() == null) throw new AssertionError("Page reference should not hold a null page");
		return pageRef;
	}	

	public PageEntry entryAt(int pageID, int entryIndex) throws Exception {
		PageReference p = bring(pageID);
		return new PageEntry(this, p,entryIndex ,ENTRY_SIZE);
	}
	
	public int createPage() throws Exception {
		Page p = cacheFile.createPage();
		evict();
		int pageID = p.getPageID();
		pageCache.put(pageID, new PageReference(p));
		markerMap[numMarkers++] = pageID;
		if (pageID > maxFetchedPage) maxFetchedPage = pageID;
		return pageID;
	}
	
	public int numPages() throws Exception {
		return cacheFile.pageCount();
	}
	
	public void commit() throws Exception {
		for (int pageID : dirtyPages) {
			PageReference pRef = pageCache.get(pageID);
			if (pRef == null) throw new AssertionError("This should never ever happen");
			Page p = pRef.getPage();
			if (p == null) throw new AssertionError("What the hell is going on?");
			cacheFile.writePage(p);
		}
		dirtyPages.clear();
		cacheFile.commit();
	}
	
	public void rollback() throws Exception {
		cacheFile.rollback(PAGE_SIZE);
	}
	
	public void truncateTo(int pageCount) throws IOException {
		if (pageCount < maxFetchedPage) throw new IllegalArgumentException("Cannot clear a page that has been brought to the cache.");
		cacheFile.truncateTo(pageCount);
	}
	
	public void truncateToZero() throws IOException {
		for(PageReference p : pageCache.values()) {
			p.invalidate();
		}
		dirtyPages.clear();
		pageCache.clear();
		numMarkers = 0;
		maxFetchedPage = -1;
		cacheFile.truncateTo(0);
	}

	@Override
	public void onWritePage(int pageID) {
		dirtyPages.add(pageID);
	}

}
