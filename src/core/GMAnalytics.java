package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Stack;

import lang.handlers.CommandHandler;
import lang.handlers.DefinitionHandler;
import lang.handlers.FrequencyHandler;
import lang.handlers.MetadataHandler;
import lang.parsing.ChatProcessor;
import lang.parsing.Command;
import network.groupme.GroupMeConfig;
import network.groupme.GroupMeRequester;
import network.models.JSONMessageResponse.Message;
import network.server.ChatMessageQueue;
import network.server.Server;
import persistence.FrequencySystem;
import persistence.MessageStorage;
import persistence.SynchronizationSystem;
import persistence.sql.HistoryDatabase;

public class GMAnalytics {

	public static void main(String[] args) throws IOException {
		System.out.println("GROUP ME ANALYTICS SERVER");
		GroupMeConfig configuration = null;
		FrequencySystem frequencySystem = null;
		ChatProcessor chatParser = null;
		try{
			System.out.println("\nLoading configuration data from file...");
			configuration = GroupMeConfig.fromConfigFile(new File("./data/gmconfig.txt"));
			System.out.println("Configuration data loaded");
		}catch (IOException e) {
			System.out.println("Error loading configuration file.");
			System.exit(1);
		}
		try{
			System.out.println("\nBooting frequency system...");
			frequencySystem = new FrequencySystem();
			System.out.println("Frequency system loaded");
		}
		catch (Exception e) {
			System.out.println("Error loading frequency system.  Check persistence data and retry");
			System.exit(1);
		}

		System.out.println("\nInitializing history database...");
		HistoryDatabase histdb = new HistoryDatabase();
		System.out.println("History database successfully loaded");
		
		System.out.println("\nInitializing message backup...");
		MessageStorage messageStorage = null;
		try {
			messageStorage = new MessageStorage(Paths.get("./data/messages/"));
		} catch (IOException e) {
			System.out.println("Could not load messages from main store");
			System.exit(1);
		}
		System.out.println("Message backup successfully loaded");
		
		System.out.println("\nInitializing remote synchronization system...");
		GroupMeRequester requester = new GroupMeRequester(configuration);
		SynchronizationSystem syncSys = new SynchronizationSystem(requester,frequencySystem, messageStorage, histdb);
		try {
			syncSys.synchronize(Options.REBUILD_ON_STARTUP);
		} catch (Exception e) {
			System.out.println("Synchronization failed");
			e.printStackTrace();
			System.exit(1);
		}
		
		HashMap<Command, CommandHandler> handlers = new HashMap<Command, CommandHandler>();
		Stack<CommandHandler> handlerStack = new Stack<CommandHandler>();
		System.out.println("\nLoading command handler objects...");
		handlerStack.push(new MetadataHandler(frequencySystem, histdb, requester));
		handlerStack.push(new FrequencyHandler(frequencySystem, histdb, requester));
		handlerStack.push(new DefinitionHandler(requester));
		
		for (CommandHandler handler : handlerStack) {
			System.out.println("Loading " + handler.getHandlerName());				
			for (Command c : handler.supportedCommands()) {
				handlers.put(c, handler);
			}
		}
		System.out.println("Finished loading command handlers");

		System.out.println("\nInitializing command processor...");
		chatParser = new ChatProcessor(configuration.botName,handlers);
		System.out.println("Command processor successfully loaded");
				
		
		ChatMessageQueue queue = new ChatMessageQueue();
		Server server = new Server(queue, configuration.botName, histdb);
		server.start();
		System.out.println("\nGROUP ME ANALYTICS SERVER - RUNNING\n");
		
		while (true) {
			Message message = queue.tryDequeue();
			if (message != null) {
				chatParser.process(message);
			}
		}
		
	}
}
