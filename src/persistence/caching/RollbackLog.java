package persistence.caching;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import core.Options;

public class RollbackLog {
	private final FileChannel channel;
	private final int PAGE_SIZE;
	private final ByteBuffer buffer;

	public RollbackLog(String filePath, int pageSize) throws IOException {
		if (Options.ROLLBACK_ENABLED) {
			channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
		}else {
			channel = null;
		}
		PAGE_SIZE = pageSize;
		this.buffer = ByteBuffer.allocate(pageSize + 4);
	}

	public void rollback(FileChannel toChannel) throws Exception {
		int size = (int)channel.size();
		int extraBytes = size % (PAGE_SIZE + 4);
		int upper = size - extraBytes;
		while (upper > 0) {
			int lower = upper - (PAGE_SIZE + 4);
			
			buffer.clear();
			channel.position(lower);
			int transferred = channel.read(buffer);
			if (transferred != PAGE_SIZE + 4) throw new Exception();
			buffer.flip();
			int pageID = buffer.getInt();
			buffer.position(0);
			toChannel.position(PAGE_SIZE * pageID);
			transferred = toChannel.write(buffer);
			if (transferred != PAGE_SIZE + 4) throw new Exception();
			upper = lower;
		}
		toChannel.force(false);
	}

	public void copyPageFrom(FileChannel fromChannel, int pageID) throws Exception {
		buffer.clear();
		buffer.putInt(pageID);
		fromChannel.position(pageID * PAGE_SIZE);
		int transferred = fromChannel.read(buffer);
		if (transferred != PAGE_SIZE) throw new Exception();
		buffer.flip();
		channel.position(channel.size());
		transferred = channel.write(buffer);
		if (transferred != PAGE_SIZE + 4) throw new Exception();
		//Fence to ensure we have written to the rollback before we attempt to write the main file
		channel.force(false);
	}
	
	public void commit() throws Exception {
		channel.truncate(0);
	}
}
