package cardGame;

/*  
 *  RobotPlayer -- implement clientSend by processing synchronously...
 */

public class RobotPlayer extends Player implements PlayerInterface {

	
	// TODO: hand should be private to robotPlayer; (Afraid to do this right now...)
	Subdeck hand=null; // new Subdeck();	// This is the robot player's copy of the cards; sent in via ADD_CARD etc.
	
	/*
	 * this is only a testing and debugging function for robotplayers
	 */
	@Override
	public Subdeck getRemoteHand() { return hand; }
	
	@Override
	public void sendToClient(ProtocolMessage pm) {
		/*
		 * Just digest and process the message synchronously
		 */
		processLocalMessage(pm);
		}

	@Override
	public void sendToServer(ProtocolMessage pm) {
		logClientError("RobotPlayer:" + pm.sender + " Sending<" + pm.type + ">" + pm.subdeck.encode() + " to server.");
		MainServer.cgk.enqueue(pm);		
		}
	
	@Override
	public void logClientError(String s) {
		MainServer.echo(s);
		}
	
	/*
	 * Note. Can only create a robot player with a pid (Probably true for human players, too...)
	 *  Also requires a compatible GameInterface for callbacks to playCard, passCards and query!
	 */
	GameInterface cardgame=null;	// this is here; RobotPlayer and later HumanPlayer have different implementations of these interfaces

	@Override
	public void setCardgame(GameInterface gameInterfaceCallbacks) 
	{ cardgame = gameInterfaceCallbacks; }
	
	void setPID(int id) {
		pid = id;
		}
	
	int getPID() {
		return pid;
		}

	RobotBrain robotBrain = null; // new RobotBrain();
	RobotPlayer(int pid, GameInterface gameInterfaceCallbacks) {
		//super();
		setAsynch(false); // { Because I am a robot, I can be called synchronously }

		hand = new Subdeck();
		robotBrain = new RobotBrain();
		robotBrain.setPID(pid);
		setPID(pid);
		setCardgame(gameInterfaceCallbacks);
		}
	
	/*
	 * robotPlay -- play a card from subdeck
	 * -- Note: subdeck sd is ignored. Uses hand and robotbrain... this is new.
	 */
	void robotPlay(Subdeck sd) {
		Card c;
		/* NoBrainer:
		 * Always play the 2C if you have it
		 */
		if (hand.find(Rank.DEUCE, Suit.CLUBS)) {
			c=new Card(Rank.DEUCE, Suit.CLUBS);
			cardgame.playCard(getPID(), c);	// yyy
			/*
			outmsg = new ProtocolMessage(getPID(), ProtocolMessageTypes.PLAY_CARD, c);
			playerErrorLog("RobotPlayer" + getPID() + ": Playing 2C.<" + c.encode() + ">");
			sendToServer(outmsg);
			*/
			return;
			}

		/*
		 * Otherwise use robotBrain to determine what to play...
		 */
		c = robotBrain.playCard();
		cardgame.playCard(getPID(), c);	// yyy		
		}

	/*
	 * this is just a backup copy... Can be deleted when construction complete...
	 */
	void robotPlayOld(Subdeck sd) {
		Card c;
		/* NoBrainer:
		 * Always play the 2C if you have it
		 */
		if (hand.find(Rank.DEUCE, Suit.CLUBS)) {
			c=new Card(Rank.DEUCE, Suit.CLUBS);
			cardgame.playCard(getPID(), c);	// yyy
			/*
			outmsg = new ProtocolMessage(getPID(), ProtocolMessageTypes.PLAY_CARD, c);
			playerErrorLog("RobotPlayer" + getPID() + ": Playing 2C.<" + c.encode() + ">");
			sendToServer(outmsg);
			*/
			return;
			}

		/*
		 * See the state of the trick and follow suit. Otherwise just guess
		 */
		if (cardLead == null) {
			c=hand.peek();
			playerErrorLog("RobotPlayer Leading" + getPID() + ": No idea what to play. Guessing:" + c.encode());
			if (c != null) {
				cardgame.playCard(getPID(), c);	// yyy
				return;	
				}
			/*else {	// dead code???
				logClientError("Play Card requested with null trick passed and when hand is empty.");		
				return;
				} */
			
			}
		/*
		 * Try to follow suit, ducking tricks if possible
		 * 
		 * get the cards of that suit; play lowest. if trick will get taken anyway, play highest
		 */
		//Card cardLead = sd.peek();	-- Set by newtrick, etc.
		// TODO: intelligent sloughing...
		Suit currentSuit = cardLead.suit; // Suit.CLUBS; 
		Subdeck cs = new Subdeck();
		for (Card card : hand.subdeck) {
			if (cardLead.suit == cardLead.suit)
				cs.add(card);			
			}
		// if no cards of the suit, slough
		if (cs.size() == 0) {
			if (sd.find(Rank.QUEEN, Suit.SPADES)) {
				c = new Card(Rank.QUEEN, Suit.SPADES);	// yay! slough the queen!
				cardgame.playCard(getPID(), c);	// yyy
				return;
				}
			// slough a heart!
			}
		else {
			c = cs.peek();	// return the first one; this is simple-minded, wrong and temporary
			cardgame.playCard(getPID(), c);	// yyy
			return;
			}
		
		/*
		 * Otherwise play the first card in the hand...
		 * 
		 */
		c=hand.peek();
		playerErrorLog("RobotPlayer" + getPID() + ": No idea what to play. Guessing:" + c.encode());
		if (c != null) {
			// this is all this should do: callback into cardgame
			cardgame.playCard(getPID(), c);	// yyy
			/*
			outmsg = new ProtocolMessage(getPID(), ProtocolMessageTypes.PLAY_CARD, c);
			sendToServer(outmsg);
			*/
			return;	
			}
		logClientError("Play Card requested when hand is empty.");		
		}

	void robotPlay(ProtocolMessage m) {
		robotPlay(m.subdeck);
		}
	
	
	void addToHand(Card c) {
		hand.add(c);
		}
	
	boolean deleteFromHand(Card c) {
		if (hand.find(c)) {
			hand.delete(c.rank, c.suit);
			return true;
			}
		return false;
		}
	
	/*
	 * Implement the PlayerInterface methods here
	 *  these get called from cardgame as player.addCards(), etc.
	 * xxx
	 */
	//called  from the server
	// TODO: these should be sendAddCards, sendDeleteCards, shouldn't they?
	public void addCards(Subdeck sd) {
		String sTemp="";
		playerErrorLog("RobotPlayer" + getPID() + ": adding " + sd.size() + " cards.");
		//Subdeck sd = m.subdeck.subdeck;
		for (Card cd : sd.subdeck) {
			sTemp = sTemp + cd.encode();
			addToHand(cd);
			}			
		playerErrorLog("RobotPlayer" + getPID() + ": Dealt/passed " + sd.sdump() + " cards:" + sTemp);		
		}
	
	public void deleteCard(Card c) {
		if (deleteFromHand(c)) {
			playerErrorLog("RobotPlayer" + getPID() + ": successfully deleted: <" + c.rank + c.suit + ">.");
			}
		else {
			playerErrorLog("RobotPlayer" + getPID() + ": cannot delete <" + c.rank + c.suit + ">.");
			}

		}
	
	//
	// TODO: notePlayed et. all: This is a total hack and should be revisited
	Trick currentTrick=null;
	int currentLeader=-1;
	Card cardLead=null;
	
	//
	// Obsolete: Replaced by RobotBrain functions
	void notePlayed(int nplayer, Card c) {
		if (currentTrick == null) 
			newTrick();
		else if (currentTrick.subdeck.size() == 0) {
			cardLead = c;
			currentTrick.leader = nplayer;
			}
		currentTrick.subdeck.add(c);
		}
	
	// called by trick_update
	void newTrick() {
		cardLead = null;
		currentTrick=new Trick(4);
		}
	
	public void updateCard(int player, Card c) {
		playerErrorLog("RobotPlayer"+"Unimplementd updateCard"); 
		}
	public void updateTrick(int lplayer, int wplayer, Subdeck sd) {
		playerErrorLog("RobotPlayer"+"Unimplementd updateTrick"); 
		}
	
	public void yourPass(int toplayer) {playerErrorLog("RobotPlayer"+"Passing Unimplementd updateCard"); }
	public void errorMsg(String st) {playerErrorLog("RobotPlayer"+" ErrorMsg ignored"); }

	
	/*
	 * (non-Javadoc)
	 * @see cardGame.PlayerInterface#yourTurn(int, cardGame.Subdeck)
	 * 
	 * this is completely whack as far as being strategic of course. yourTurn ignores who lead for right now, as well as what's been played ...
	 */
	public void yourTurn(int nleader, Subdeck sd) {
		String sTemp="";
		playerErrorLog("RobotPlayer" + getPID() + ": My turn.");
		/*
		 * current trick has accumulated the trick
		 */
		robotPlay(sd);
		}
	
	/*
	 * processLocalMessage -- make sure you don't modify the sublist.
	 */
	void processLocalMessage(ProtocolMessage m) {
		int i, len;
		String sTemp="";
		boolean bAbEnd=false;
		Card c=null;
		//TrickModifiers tm=null;
		
		switch(m.type) {
		case YOUR_TURN: // !CARD* [cards in trick already played]
			yourTurn(0, m.subdeck);
			break;
		case ADD_CARDS: // CARD*
			addCards(m.subdeck);
			robotBrain.addCards(m.subdeck);
			break;
		case DELETE_CARDS: // CARD+
			len=m.subdeck.size();
			for (i=0; i<len; i++) {
				c=m.subdeck.pop();
				//playerErrorLog("RobotPlayer" + getPID() + ": + <" + c.rank + c.suit + ">.");
				sTemp = sTemp + "<" + c.rank + c.suit + ">" ;
				deleteCard(c);
				robotBrain.deleteCard(c);
				}
			break;
		case TRICK_CLEARED:
			robotBrain.trickCleared(m.trick);
			// TODO: parse and use the actual trick
			/*playerErrorLog("Clearing in RobotPlayer" + getPID() + "***Trick:" + robotBrain.currentTrickId() + " leader:"
					+ m.sender +	// Isn't the message sender the player of this card?
					" winner:" + m.trickModifiers.winnerid +	// Winner isn't known yet... needs to be cleared
					"<" + m.subdeck.encode() + ">");  */
			//newTrick();	// clear the trick
			break;
		case CURRENT_TRICK: // &CARD*
			/*tm = m.trickModifiers;
			if (tm.player != m.sender) {
				System.out.println("Uh oh: player=" + tm.player + " and sender=" + m.sender);
				} */
			c = m.subdeck.subdeck.peek();
			notePlayed(m.sender, c);	// TODO: Isn't it a bit redundant to have BOTH ofo these???
			robotBrain.cardPlayed(m.sender, c);
			//notePlayed(tm.player, c);	// TODO: Isn't it a bit redundant to have BOTH ofo these???
			//robotBrain.cardPlayed(tm.player, c);
			playerErrorLog("RobotPlayer" + getPID() + "***Trick" + robotBrain.currentTrickId() + ": Player"
					+ m.sender +	// Isn't the message sender the player of this card?					
					": Card<" + m.subdeck.encode() + "> ***"); 
			/*playerErrorLog("RobotPlayer" + getPID() + "***Trick:" + "??" + " leader:"
					+ m.sender +	// Isn't the message sender the player of this card?
					" winner:" + m.trickModifiers.winnerid +	// Winner isn't known yet... needs to be cleared
					"<" + m.subdeck.encode() + ">"); */ 
			// TODO: Isn't this all I need???
			
				break;
			/*	
			c=m.subdeck.peekLast();
			if (m.trickModifiers == null ) {
				playerErrorLog("RobotPlayer" + getPID() + ": ill formed update msg." + " Card played <" + c.rank + c.suit + ">.");
				break;
				}
			tm = m.trickModifiers;
			playerErrorLog("RobotPlayer" + getPID() + ": seeing player" + tm.player + " played <" + c.rank + c.suit + ">.");
			if (m.sender != tm.player)
				System.out.println("Can't happen: trickmodifer/sender doesn't agree with msg sender");
			notePlayed(tm.player, c);
			break; */
		case PLAYER_SCORES: // $ Player-score, Player-score...
			playerErrorLog("RobotPlayer" + getPID() + " scores:" + m.usertext); 
			break;
		case BROKEN_SUIT: // B hearts/spades are broken
		case PLAYER_ERROR: // %Text {Please play 2c, Follow Suit, Hearts/Spades not broken, not-your-turn, don't-have-that-card, user-error}

		case PLAY_CARD: // CARD
		case PASS_CARD: // CARD*
		case SUPER_USER: // S Text
			// sent unhandled message
			if (bAbEnd) {
			    playerErrorLog("RobotPlayer: msg:<" +
					m.type +
					":" +
					m.usertext + "> unimplemented.");
			    MainServer.cgk.pause();
				}
		case GAME_QUERY:
			break;
		default:
			break;
			} // switch
		}

}
