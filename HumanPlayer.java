package cardGame;

/*
 * To be clear: this is the human interface (counterpart to RobotPlayer) that handles messages to be sent to a HumanPlayer that exists
 *  on the other side of a network connection, and is to be contacted via the nioAccessMethods, who will call us back via the listener.
 *  Key: when nioAccessMethods are created the pid is determined there, so the client can't (accidently) cheat and or be confused about the pid
 */
public class HumanPlayer extends Player implements PlayerInterface {

	GameInterface cardgame=null;	// serverside callbacks into the cardgame; these are actually the same for Human and RobotPlayer;
	NIOClientSession nioNetworkAccessMethods;
	
	@Override
	public void setCardgame(GameInterface gameInterfaceCallbacks) 
	{ cardgame = gameInterfaceCallbacks; }
	
	@Override
	public void addCards(Subdeck subdeck) {
		// TODO Auto-generated method stub
		System.out.println("SendToClient addCards Unimplmented");

		return; 

	}

	@Override
	public void deleteCard(Card card) {
		// TODO Auto-generated method stub

	}

	@Override
	public void errorMsg(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateCard(int nplayer, Card c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTrick(int nleader, int nwinner, Subdeck trick) {
		// TODO Auto-generated method stub

	}

	@Override
	public void yourTurn(int nleader, Subdeck trick) {
		// TODO Auto-generated method stub
		System.out.println("SendToClient youturn Unimplmented");

	}

	@Override
	public void yourPass(int toPlayer) {
		// TODO Auto-generated method stub
		System.out.println("SendToClient yourpass Unimplmented");

	}

	@Override
	public void sendToClient(ProtocolMessage pm) {
		// TODO Auto-generated method stub
		System.out.println("SendToClient...");
		String sProtocolMessage = pm.encode();
		System.out.println("<" + sProtocolMessage + ">");
		nioNetworkAccessMethods.write(sProtocolMessage);
	}

	@Override
	public void sendToServer(ProtocolMessage pm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logClientError(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public Subdeck getRemoteHand() {
		// TODO Auto-generated method stub
		return null;
	}

	//
	// Primary Constructor for HumanPlayer; called from CardGame.
	//	Note: pid not managed here. In the actual NIOClientSession
	// This is done in CardGame.java.

	HumanPlayer(int pid, NIOClientSession nioAccessMethods, GameInterface gameInterfaceCallbacks) {

		//isAsynch = true;
		setPID(pid);
		setAsynch(true);
		/*
		 * This next thing is very important, conceptually. The Game's pid must be set
		 *  at the nioClientSession level WHEN IT IS PUT IN A GAME. Subsequently the Player level can tell if a player is spoofing
		 *  another player, something that can only be done if player has set the bSuperuserPrivilege boolean
		 */
		setIsRobot(false);
		setCardgame(gameInterfaceCallbacks);
		nioNetworkAccessMethods = nioAccessMethods;
		
	}	// HumanPlayer()

	void write(String smsg) {
		nioNetworkAccessMethods.write(smsg);
		}
}
