package lang.handlers;

import lang.parsing.Command;
import network.groupme.GroupMeRequester;
import persistence.SynchronizationSystem;

public class RefreshHandler implements CommandHandler {

	private static final Command[] supportedCommands = new Command[] {Command.REFRESH};
	private final SynchronizationSystem syncSys;
	private final GroupMeRequester gmReq;
	public RefreshHandler(SynchronizationSystem syncSys, GroupMeRequester gmReq) {
		this.syncSys = syncSys;
		this.gmReq = gmReq;
	}
	@Override
	public void process(Command cmd, String senderID, String extra) {
		try {
			int downloaded = syncSys.synchronize(false);
			gmReq.send("Message and user definitions are up to date.  Downloaded " + downloaded + " new messages.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}

	@Override
	public String getHandlerName() {
		return "REFRESH HANDLER";
	}

	@Override
	public Command[] supportedCommands() {
		return supportedCommands;
	}

}
