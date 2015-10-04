package network.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class Server {

	private final HttpServer chatServer;
	public Server(ChatMessageQueue messageQueue) throws IOException{
		chatServer = HttpServer.create(new InetSocketAddress(56789), 16);
		chatServer.createContext("/", new ChatMessageHandler(messageQueue));
	}
	
	public void start(){
		chatServer.start();
	}
}
