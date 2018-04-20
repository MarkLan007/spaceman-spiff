package cardGame;

/*
 * This is the server side data structures for the player and the player's hand.
 *  
 *  Player manages the server-side representation of what cards the player has (i.e. referee).
 *  and the game sends the messages to the actual remotePlayer or RobotPlayer through PlayerInterface methods
 *   methods that have a correlate are prefixed with ss (server-side) so the ssAddCards is disambiguated from sendAddCard, etc. 
 *  
 *  These are all game/server side instance variables and methods.
 *  To make it a little clearer the subdeck on the game/server side does not reference a "hand." That's player stuff.
 *  And take NOTE! This doesn't implement PlayerInterface.
 */
public class Player /*implements PlayerInterface */{
	/*
	 * These are NOT the same thing; may have a remote robot; for example a neural net implementation; 
	 *  but the internal robots are not isAsynch.
	 */
	private boolean isAsynch=false;
	private boolean isRobotPlayer=true;
	void setAsynch(boolean b) { isAsynch = b; }
	boolean getAsynch() { return isAsynch ; }
	void setIsRobot(boolean b) { isRobotPlayer = b; }
	boolean isRobot() { return isRobotPlayer ; }

	protected CardGame cardGame=null;
	Boolean bIsMyMove = false;
	public Subdeck subdeck=new Subdeck();	/* this is the server's copy of the cards */
	String playerName="";
	int score=0;
	protected int pid; 	// Player-id for protocol messages
	/*
	 * NIOClientSession is private to make sure that it is only called through HumanPlayer
	 */
	private NIOClientSession humanInterface=null;
	/*
	 * Player implements core functions addCard, deleteCard, grantTurn
	 */
	
	// 
	// create a player in a given game
	//
	Player(CardGame g) {
		cardGame = g;
		}
	
	//@Override
	public void logClientError(String s) {
		MainServer.echo(s);
		}
	
	//
	// create a player to be added to a game later
	//
	Player() {
		
		}
	
	Player (CardGame g, String sPlayerName) {
		cardGame = g;
		setName(sPlayerName);		
		}
	
	Player (String sPlayerName) {
		setName(sPlayerName);		
		}
	
	/*
	 * Set the player id to use for protocol messages. Must get done or else everyone is pid=0
	 */
	void setPID(int id) {
		pid = id;
		}
	
	int getPID() {
		return pid;
		}
	
	/*
	 * Player implements core functions addCard, deleteCard, grantTurn
	 */
	
	
	/*
	 * accept a card (by reference) in the subdeck that should be what the player has
	 */
	void ssAddCard(Card c) {
		subdeck.add(c);
		}
	
	boolean ssHasCard(Card c) {
		return subdeck.find(c);
		}
	
	/*
	 * delete this card from server-side representation (i.e. find it in the hand and delete the first one of the same value...)
	 */
	void ssDeleteCard(Card c) {
		subdeck.delete(c.rank, c.suit);
		}
	
	/*
	 * Indicates the right to make a move (asynchronously)
	 */
	void grantTurn() {
		bIsMyMove = true;		
		}
	
	/*
	 * TODO: is this used? It shouldn't be. server side things are NOT called hand
	 */
	Subdeck NoLongerUsedgetHand() {
		return subdeck;
		}
	//
	// setName
	//
	void setName(String s) {
		playerName = s;
		}
	
	String getName() {
		return playerName;
	}

	void playerErrorLog(String sError) {
		MainServer.ttyClientLogString(sError);
		}

	//@Override
	public void sendToClient(ProtocolMessage pm) {
		
		/*
		 * Human client; send remote message
		 */
		playerErrorLog("Can't Happen: player::sendToClient should not be called. ***");
		/*
		 * Uh oh... should not be calling this here... Not in Player::settoclient...
		 *  ... should be called from HumanPlayer.sendToClient
		 */
		if (!isRobot()) {
			String sMsg=pm.encode();
			humanInterface.write(sMsg);
			return;
			}
		/*
		 * Otherwise, Just digest and process the message synchronously
		 *  -- unless we are in gamestepping through it... in which case this should enqueue and pause
		 *  Not sure whether everything should be handled as a queue message... This should only be seen
		 *  for robotplayers. RobotXXX
		 */
		process(pm);

		}

	//@Override
	public void sendToServer(ProtocolMessage pm) {
		playerErrorLog("player **sendToServer**:<" +
				pm.type +
				":" +
				pm.usertext + "> unimplemented.");
		
		}
	
	/*
	 * process - stub for processing messages locally
	 */
	void process(ProtocolMessage m) {
		switch(m.type) {
		case ADD_CARDS: // CARD*
		case DELETE_CARDS: // CARD+
		case YOUR_TURN: // !CARD* [cards in trick already played]
		case CURRENT_TRICK: // &CARD*
		case BROKEN_SUIT: // B hearts/spades are broken
		case PLAYER_SCORES: // $ Player-score, Player-score...
		case PLAYER_ERROR: // %Text {Please play 2c, Follow Suit, Hearts/Spades not broken, not-your-turn, don't-have-that-card, user-error}

		case PLAY_CARD: // CARD
		case PASS_CARD: // CARD*
		case SUPER_USER: // S Text
			// sent unhandled message
			playerErrorLog("player msg:<" +
					m.type +
					":" +
					m.usertext + "> unimplemented.");
			}
		}
	
	public Subdeck getRemoteHand() { return subdeck; }
}
