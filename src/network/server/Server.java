package network.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import persistence.sql.HistoryDatabase;

public class Server {

	private final HttpServer chatServer;
	public Server(ChatMessageQueue messageQueue, String botname, HistoryDatabase historyDatabase, int hostPort) throws IOException{
		chatServer = HttpServer.create(new InetSocketAddress(hostPort), 16);
		chatServer.createContext("/feed", new ChatMessageHandler(messageQueue));
		//chatServer.createContext("/app", new WebPageHandler(botname, historyDatabase));
	}
	
	public void start(){
		chatServer.start();
	}
}
