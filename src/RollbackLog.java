import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class RollbackLog {
	private final FileChannel channel;
	private final ByteBuffer buffer;

	public RollbackLog(String filePath) throws IOException {
		channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
		this.buffer = ByteBuffer.allocate(Integer.BYTES);
	}

	private int readInt(int position) throws Exception {
		buffer.clear();
		if (Integer.BYTES != channel.read(buffer, position)) throw new Exception();
		buffer.flip();
		return buffer.getInt();
	}

	private void writeInt(int value, int position) throws Exception {
		buffer.clear();
		buffer.putInt(value).flip();
		if (Integer.BYTES != channel.write(buffer, position)) throw new Exception();
	}

	public void rollback(FileChannel toChannel, int pageSize) throws Exception {
		int size = (int)channel.size();
		int extraBytes = size % (pageSize + 4);
		int upper = size - extraBytes;
		while (upper > 0) {
			int lower = upper - (pageSize + 4);
			int pageID = readInt(lower);
			toChannel.position(pageSize * pageID);
			channel.position(lower + 4);
			if (pageSize != toChannel.transferFrom(channel, lower + 4, pageSize)) throw new Exception();
			upper = lower;
		}
	}

	public void copyPageFrom(FileChannel fromChannel, int pageID, int pageSize) throws Exception {
		writeInt(pageID, (int)channel.size());
		channel.position(channel.size());
		fromChannel.position(pageID * pageSize);
		int transferred = (int) channel.transferFrom(fromChannel, pageID * pageSize, pageSize);
		if (pageSize != transferred) throw new Exception();

		//Fence to ensure we have written to the rollback before we attempt to write the main file
		channel.force(false);
	}
	
	public void commit() throws Exception {
		channel.truncate(0);
	}
}
