package persistence.caching;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CacheFileHandler {
	private final int PAGE_SIZE;
	private final byte [] NULLDATA;
	private final FileChannel channel;
	private final ByteBuffer buffer;
	
	private final RollbackLog rollbackLog;
	
	public CacheFileHandler(String mainFilePath, RollbackLog rollbackLog, int pageSize) throws IOException {
		channel = FileChannel.open(Paths.get(mainFilePath), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
		buffer = ByteBuffer.allocate(pageSize);
		this.rollbackLog = rollbackLog;
		NULLDATA = new byte[pageSize];
		PAGE_SIZE = pageSize;
	}
	
	/*TODO: Exception handling */
	public void writePage(Page page) throws Exception {
		if (PAGE_SIZE != page.size()) 							throw new IllegalArgumentException("Page size does not match for writeback");
		if (page.getPageID() * PAGE_SIZE > channel.size())		throw new IllegalArgumentException("Page ID is out of range");
		rollbackLog.copyPageFrom(channel, page.getPageID());
		
		buffer.clear();
		page.fill(buffer);
		buffer.flip();
		
		if (PAGE_SIZE != channel.write(buffer, page.getPageID() * PAGE_SIZE))
			throw new Exception("Did not write the correct amount of bytes to the file");
		
	}
	
	public Page createPage() throws Exception {
		int pageID = (int) (channel.size() / buffer.capacity());
		buffer.clear();
		buffer.put(NULLDATA);
		buffer.flip();
		Page p = new Page(pageID, buffer, PAGE_SIZE);
		buffer.position(0);
		if (PAGE_SIZE != channel.write(buffer, pageID * PAGE_SIZE))
			throw new Exception("Did not write the correct amount of bytes to the file");
		return p;
	}
	
	public Page readPage(int pageID) throws Exception {
		int pageLoc = pageID * PAGE_SIZE;		
		if (buffer.capacity() < PAGE_SIZE) 		throw new Exception("Buffer overflow in file handler");
		if (pageLoc > channel.size() - PAGE_SIZE) throw new Exception("Page ID is out of range");		
		buffer.clear();
		buffer.limit(PAGE_SIZE);
		if (PAGE_SIZE != channel.read(buffer, pageLoc))
			throw new Exception("Did not read the correct amount of bytes to the file");
		buffer.flip();
		return new Page(pageID, buffer, PAGE_SIZE);
	}
	
	public void truncateTo(int numPages) throws IOException {
		channel.truncate(numPages * PAGE_SIZE);
	}
	
	public void rollback(int pageSize) throws Exception {
		rollbackLog.rollback(channel);
	}
	
	public void commit() throws Exception {
		rollbackLog.commit();
	}
	
	public int pageCount() throws IOException {
		return (int) (channel.size() / PAGE_SIZE);
	}
	
	
}
