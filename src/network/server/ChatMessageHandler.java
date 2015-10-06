package network.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ChatMessageHandler implements HttpHandler {

	private ChatMessageQueue queue;
	public ChatMessageHandler(ChatMessageQueue queue) {
		this.queue = queue;
	}
	@Override
	public void handle(HttpExchange xchg) {
		try {
			if (!xchg.getRequestMethod().toUpperCase().equals("POST")) {
				xchg.sendResponseHeaders(404, 0);
			} else {
				String req = getRequest(xchg.getRequestBody());
				if (!req.isEmpty()) {
					queue.tryEnqueue(req);					
				}
				xchg.sendResponseHeaders(200,0);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		finally{
			xchg.close();
		}
	}

	protected String getRequest(InputStream stream) {
		byte[] chunk = new byte[512];
		StringBuffer request = new StringBuffer();
		int ofs = 0;
		int read = 0;
		try {
			do {
				read = stream.read(chunk, 0, 512);
				if (read > 0) {
					request.append(new String(chunk, 0, read));
					ofs += read;
					//Cut off chunking at 20KB
					if (ofs > 20000) return "";
				}
			}while (read>=0);
		}catch (IOException e) {
			System.out.println("WARNING: REQUEST BUFFER FAILURE");
			return "";
		}

		return request.toString();
	}

}
