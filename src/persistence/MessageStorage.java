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

import network.groupme.GroupMeRequester;
import network.models.JSONMessageResponse;

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
	private final static int MIN_MESSAGES_PER_FILE = 450;
	private final Path directory;
	private int maxFileNum = 0;
	private JSONMessageResponse currentResponse = null;
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
		if (maxFileNum >= 0) {
			try (BufferedReader reader = Files.newBufferedReader(directory.resolve(BASE + String.format("%09d", maxFileNum)))) {
				Scanner scanner = new Scanner(reader);
				String content = scanner.useDelimiter("\\A").next();
				scanner.close();
				currentResponse = GroupMeRequester.getMessagesFromString(content);
				
			} catch (IOException e) {
				System.out.println("Error encountered reading main storage file. Manually remove problem files and restart the program.");
				System.exit(1);
			}
		}

	}
	public Enumeration<String> getMessageHistory(){
		return new MessageStorage.Enumerator(this);
	}
	
	private void saveCurrentResponse() {
		Path file = directory.resolve(BASE + String.format("%09d", maxFileNum));
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			writer.write(GroupMeRequester.getStringFromMessages(currentResponse));
		} catch (IOException e) {
			System.out.println("Error saving messages to file");
		} finally {
		}
	}

	public void saveChunk(JSONMessageResponse response) {
		if (currentResponse == null) {
			currentResponse = response;
			return;
		}
		currentResponse.data.messages.addAll(response.data.messages);
		if (currentResponse.data.messages.size() > MIN_MESSAGES_PER_FILE) {
			saveCurrentResponse();
			maxFileNum++;
			currentResponse = null;
		}

	}
	
	public void commitMessages() {
		saveCurrentResponse();
	}
	
	public String largestMessageID() {
		if (currentResponse == null) return "0";
		return currentResponse.data.messages.get(currentResponse.data.messages.size() - 1).messageID;
	}



}
