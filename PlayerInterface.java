package cardGame;

/*
 * The point is that there basically two types of players, RobotPlayers and HumanPlayers depending on whether 
 *  they get user input or not. HumanPlayer will be extended by a local communication with tty and gui client,
 *  and remote communication with a socket. (that's the whole point of this.)
 *  RobotPlayers compute moves and strategies running on the server.
 *  
 *  The simplest player (default) is a robot that is always ready and works synchronously. It hands the pm 
 *  directly to the strategy module.
 *  
 *  RobotPlayer -- implement clientSend by processing synchronously...
 */

/*
 * TODO: HumanPlayer Asynch version is to pm.encode the message into a string and send it to the client through a socket
 */

public interface PlayerInterface {
	boolean isReady=true;
	String sPlayerName="";
	void setCardgame(GameInterface gameInterfaceCallbacks); // { cardgame = gameInterfaceCallbacks };
	/*
	 * The calls that players make to the game; RemotePlayer will make these by serializing their arguments, and sending across a socket
	 */
	void addCards(Subdeck subdeck);			// player has been dealt (or passed) these cards; player adds them to hand
	void deleteCard(Card card);				// player should delete this card from hand; (after a successful play)
	void errorMsg(String s);				// sent to player after unsuccessful play; robotplayers shouldn't get these because they know the rules...
	void updateCard(int nplayer, Card c);	// nplayer played this card	into the current trick
	void updateTrick(int nleader, int nwinner, Subdeck trick);	// nleader lead; trick won by nwinner; 
	void yourTurn(int nleader, Subdeck trick);	// nleader lead; it's your turn; (null if you are the leader)
	void yourPass(int toPlayer);			// pass cards to toPlayer by call passcards(subdeck) in the gameInterface
	
	
	// subclasses of PlayerInterface implement "protected Subdeck hand;"
	// the strategy/player/client's copy of the cards that are dealt and send through protocol messages ADD and DELETE
	//Subdeck hand=null; // This goes in robot player...
		
	// These will get deleted (except logClientError)
	/*
	 * client send -- send client a message, possibly across a socket/connection
	 */
	void sendToClient(ProtocolMessage pm) ;
	/*
	 * server send -- send a protocol message to the server, possibly across a network connection
	 */
	void sendToServer(ProtocolMessage pm) ;
	
	void logClientError(String s) ;
	
	Subdeck getRemoteHand() ;	// this should probably get deleted
	}
