package cardGame;

/*
 * These are the game callbacks that players (included RobotPlayer and HumanPlayer) call back to the game with
 *  -- RobotPlayer is synchronous
 *  -- HumanPlayer is asynchronous putting data through a socket
 *  
 *  TODO: why can't I set gameName???
 */
public interface GameInterface {
	String gameName="";			// Why can't I set this in CardGame?? A mystery...
	/*
	 * Once I get sockets implemented I can know who the player is from the socket it was received from (but what about robots?)
	 *  (How do robots know who they are if you don't tell them...)
	 */
	void playCard(int fromPlayer, Card card);	// server tells client to delete card if successful; sends errormsg if not
	void passCards(int fromPlayer, Subdeck cards);
	void bidTricks(int fromPlayer, Subdeck subdeck);
	void query(int fromPlayer, String msg);
	void superuser(int fromPlayer, String msg);
	void disconnect(int pid);	// disconnect from game;
	
}
