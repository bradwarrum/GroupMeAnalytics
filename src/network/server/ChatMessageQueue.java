package network.server;

import java.util.concurrent.ArrayBlockingQueue;

import network.groupme.GroupMeRequester;
import network.models.JSONMessageResponse;
import network.models.JSONMessageResponse.Message;

public class ChatMessageQueue {
	private ArrayBlockingQueue<JSONMessageResponse.Message> messageQueue;
	public ChatMessageQueue(){
		messageQueue = new ArrayBlockingQueue<JSONMessageResponse.Message>(16);
	}
	
	public synchronized Message tryDequeue() {
		while (true)
		try {
			wait();
			break;
		} catch (InterruptedException e) {
			continue;
		}
		return messageQueue.poll();
	}
	
	public synchronized void tryEnqueue(String rawMessage) {
		JSONMessageResponse.Message message = GroupMeRequester.getMessageFromString(rawMessage);
		if (message != null) {
			messageQueue.offer(message);
			notify();
		}
	}
	
	public void interrupt() {
		notifyAll();
	}
}
