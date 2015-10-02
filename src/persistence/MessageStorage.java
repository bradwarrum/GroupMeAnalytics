package persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.Scanner;

public class MessageStorage{
	public class Enumerator implements Enumeration<String>{
		private int fileNum;
		private final MessageStorage parent;
		private String cachedFile = null;
		private boolean cached = false;
		public Enumerator(MessageStorage parent) {
			fileNum = 0;
			this.parent = parent;
		}
		private boolean tryBring(){
			try (BufferedReader reader = Files.newBufferedReader(directory.resolve(BASE + String.format("%09d", fileNum)))) {
				Scanner scanner = new Scanner(reader);
				cachedFile = scanner.useDelimiter("\\A").next();
				scanner.close();
				cached = true;
				return true;
			} catch (IOException e) {
				System.out.println("Error encountered reading main storage file. Manually remove problem files and restart the program.");
				System.exit(1);
				return false;
			}
		}
		@Override
		public boolean hasMoreElements() {
			if (fileNum <= parent.maxFileNum) {
				if (cached || tryBring()) {
					return true;
				}
			}
			return false;
		}
		@Override
		public String nextElement() {
			if (!cached) return null;
			cached = false;
			fileNum++;
			return cachedFile;
		}

	}
	private final static String BASE = "msgdat_";
	private final Path directory;
	private int maxFileNum = -1;
	public MessageStorage(Path directoryPath) throws IOException {
		if (!directoryPath.equals(Files.createDirectories(directoryPath))) {
			throw new IOException("Could not create the directory for message storage.");
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
			for (Path file : stream) {
				if (Files.isRegularFile(file)) {
					String filename = file.getFileName().toString();
					if (filename.matches("^" + BASE+"\\d+$")) {
						int filenum = Integer.valueOf(filename.split("_")[1]);
						if (filenum > maxFileNum) maxFileNum = filenum;
					}
				}
			}
		}
		this.directory = directoryPath;
	}
	public Enumeration<String> getMessageHistory(){
		return new MessageStorage.Enumerator(this);
	}

	public boolean saveChunk(String chunk) {
		Path file = directory.resolve(BASE + String.format("%09d", maxFileNum + 1));
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			writer.write(chunk);
			maxFileNum++;
		} catch (IOException e) {
			return false;
		}
		return true;
	}



}
