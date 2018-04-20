package cardGame;

import java.nio.channels.SocketChannel;

//import java.util.LinkedList;

public class CardGame implements GameInterface {
	int nPlayers = 4;
	private int nCurrentTurn = -1; // index of player with current turn or -1 if undefined
	Trick[] trickArray = new Trick[(52 / nPlayers) + 1];
	final int LAST_TRICK = (52/nPlayers);
	Trick currentTrick;
	int nTrickId = 0;
	boolean bDebugCardCf=true;

	/*
	 * TODO: global game shouldn't be managed this way; 
	 * This is sad, of course; but there is only one game for now, and it is created in ttyhandler.
	 * Let him who is without sin cast the first stone. (Hey, I'll fix it soon.)
	 */
	static public CardGame theGame=null;
	
	Trick getCurrentTrick() {
		return currentTrick;
		}
	
	int getTurn() {
		return nCurrentTurn;
		}

	/*
	 * true if in this GAME cfirst is higher than csecond; assume csecond was the
	 * one lead (or beat the one lead) 
	 * isHigher(2C,AC) -> false 
	 * isHigher(2C,3C) -> false 
	 * isHigher(AC,3C) -> true 
	 * isHigher(AH,2C) -> false
	 */
	boolean isHigher(Card cfirst, Card csecond) {
		/*
		 * if the suits are different the first card wins
		 */
		if (bDebugCardCf)
			gameErrorLog("?isHigher(" + cfirst.encode() + "," + csecond.encode() + ")" );

		if (cfirst.suit != csecond.suit)
			return false;
		/*
		 * otherwise compare ranks, not ace is high
		 */
		if (csecond.rank == Rank.ACE)
			return false;
		if (cfirst.rank == Rank.ACE)
			return true;
		if (cfirst.rank.ordinal() > csecond.rank.ordinal())
			return true;
		else
			return false;
	}

	/*
	 * The hack that never got implemented:
	 * See "step" in ttyhandler
	boolean bStepTurns = false;	// this and game stepping isn't really completely implemented, is it...

	// Torn on game tracing: stepTurns(true);
	void stepTurns(boolean bOnOff) {
		bStepTurns = bOnOff;
	}
	boolean stepTurns() { return bStepTurns; }
	*/	

	Boolean bShuffle = false;
	Player[] playerArray = new Player[nPlayers];
	// Subdeck[] playerCards = new Subdeck[nPlayers]; // Key point: the game's
	// official copy of what's in the hand NOT part of player...
	/*
	 * Note: Must be able to change player without changing what's in the official
	 * part of the hand...
	 */

	// create players list, a deck (without faces), and names
	CardGame() {
		// default constructor; everything hunky dory with default values.
		
		// the name of the game is...
		// gameName = "Hearts";
		
		populatePlayers();
	}
private void populatePlayers() {
	/*
	 * Create robotPlayer interfaces to handle player messages... 
	 */
	int i;
	for (i = 0; i < nPlayers; i++)
		if (playerArray[i] == null) {
			playerArray[i] = new RobotPlayer(i, this);
		}
	for (i=0; i<humanPlayerFree; i++) {
		//playerArray[i].isRobot = false;
		// playerArray[i].humanInterface = humanPlayers[i];
		HumanPlayer hp = new HumanPlayer(i, humanPlayers[i], this);
		/*
		 * This setPID can only be done here because it is game specific, although the NIO layer must remember it.
		 * TODO: bSuperUser should be set up here too.
		 * (It's game specific. Some games may not allow there to be superusers...)
		 */
		hp.nioNetworkAccessMethods.setPID(i);
		
		playerArray[i] = hp;
		/*
		 * Set the player-layer of value
		 */
		playerArray[i].setPID(i);
		playerArray[i].setIsRobot(false);
		playerArray[i].setAsynch(true);
		}

	}
	/*
	 * Player p joins the game.
	 * TODO: (join functions are for remote human players when I implement them)
	 * TODO: Fix join. join is completely bogus now... 
	 *  so... if playertype is robot player, then replace with human player...
	 */
	boolean join(Player p) {
		int i;
		for (i = 0; i < nPlayers; i++)
			if (playerArray[i] == null) {
				playerArray[i] = p;
				p.setPID(i);
				return true;
				}
		return false;
	}

	/*
	 * TODO: allow ability to add multiple humans
	 * Put human player into seat zero always, if there is a current game for now
	 * ... he will be dealt in at reset at any rate
	 */
	NIOClientSession[] humanPlayers=new NIOClientSession[10];
	private int humanPlayerFree=0;
	
	/*
	 * channelHasOwner -- take an accepted channel, and assume that it belongs to the last player to add
	 *  as that players second channel that they will send back their moves on...
	 */
	NIOClientSession getChannelOwner(SocketChannel chann) {
		int i;
		for (i=0; i<humanPlayerFree; i++)
			if (humanPlayers[i].incomingChann == null) {
				humanPlayers[i].incomingChann = chann;
				return humanPlayers[i];
				}
		return null;
		}
	
	boolean join(NIOClientSession human) {
		int replacedPlayer = 0;

		gameErrorLog("Received connection and creating humanplayer.");
		humanPlayers[humanPlayerFree] = human;
		humanPlayerFree++;
		/*
		 * Humans are here; reset the game...
		 */
		reset();
		return true;
		}

	boolean join(String s) {
		Player p = new Player(s);
		return join(p);
	}

	/*
	 * TODO: actually use getPlayer instead of pulling things out of PlayerArray
	 */
	Player getPlayer(int index) {
		if (index >= 0 && index < nPlayers)
			return playerArray[index];
		return null;
	}

	void reset(Boolean shuffle) {
		bShuffle = shuffle;
		reset();
	}

	void gameErrorLog(String sError) {
		MainServer.ttyLogString(sError);
	}

	String getGameStatus() {
		String sStatus = "";
		//
		// Format status string
		for (int i = 0; i < nPlayers; i++) {
			String sCurrent = "";
			if (playerArray[i] != null) {
				Player p = playerArray[i];
				sCurrent = p.playerName + '.' + p.score;
				if (nCurrentTurn == i)
					sCurrent = '#' + sCurrent;
			}
			sStatus = sStatus + '$' + sCurrent;
		}
		sStatus = sStatus + '$';
		return sStatus;
	}

	/*
	 * called when a turn is complete (doesn't consider whether game is over)
	 */
	void updateTurn() {
		/*
		 * Current turn is set for the first turn at deal (2c)
		 */
		if (nCurrentTurn == -1)
			return;

		nCurrentTurn++;
		if (nCurrentTurn >= nPlayers)
			nCurrentTurn = 0;
	}
	
	void updateTurn(int nplayer) {
		nCurrentTurn = nplayer;
		}

	/*
	 * get the game's idea of what cards a player has
	 */
	Subdeck getCards(int iplayer) {
		Player p = playerArray[iplayer];
		if (p == null)
			return null; // can't happen
		return p.subdeck; // subdeck is the server side representation of the cards that are dealt or
							// passed to the hand
	}

	/*
	 * tell the (next or first) player to make a move by sending it YOUR_TURN. cf
	 * updateTurn(); TODO: send the subdeck of the current trick.
	 * 
	 * Ok, this seems like it's really wrong... This was an infinite that continually sent YOUR_TURN to the same client...
	 * It should send it once, and the response will come back either from human player or robot player thereby moving 
	 * the game along...
	 */
	void sendNextMove() {

		//while (nCurrentTurn != -1) {
			ProtocolMessage pm = new ProtocolMessage(ProtocolMessageTypes.YOUR_TURN);
			/*
			 * This message should included the cards already played in the trick..
			 */
			Player p = playerArray[nCurrentTurn];
			/*
			 * check if player has any cards left to play; if not return... ?
			 */
			// This sends to client; fine; pause before processing any response when stepping
			p.sendToClient(pm);
		//}
		if (nCurrentTurn == -1)
			gameErrorLog("Game over.");
	}
	
	void broadcastUpdate(ProtocolMessage pmsg) {
		int i, j;
		j = nCurrentTurn; 
		for (i=0; i<nPlayers; i++) {
			Player p = playerArray[j++];
			p.sendToClient(pmsg);
			if (j >= nPlayers)
				j = 0;
			}
		
		}

	void totalScores() {
		int i, iTrickTotal;
		for (i=0; i<nTrickId; i++) {
			Trick t=trickArray[i];
			// sum up the no of hearts in the trick, the QS and give to the winner
			iTrickTotal = 0;
			for (Card c : t.subdeck.subdeck) {
				if (c.suit == Suit.HEARTS)
					iTrickTotal = iTrickTotal + 1;
				else if (c.rank == Rank.QUEEN && c.suit == Suit.SPADES)
					iTrickTotal = iTrickTotal + 13;
				}
			playerArray[t.winner].score = playerArray[t.winner].score + iTrickTotal; 
			}
		String sTemp="";
		for (i=0; i<nPlayers; i++)
			sTemp = sTemp + "<" + playerArray[i].pid + "." + playerArray[i].score + ">";
		
		ProtocolMessage pm=new ProtocolMessage(ProtocolMessageTypes.PLAYER_SCORES, sTemp);
		broadcastUpdate(pm);		
		}
	
	/*
	 * Update the trick with the (legally played) card, send updated trick to
	 * players
	 */
	void trickUpdate(int nsender, Card card) {
		/*
		 * add the card to the trick's subdeck, and see if hearts are broken
		 */
		currentTrick.subdeck.add(card);
		if (card.suit == Suit.HEARTS) {
			currentTrick.breakHearts();
			}
		
		/*
		 * broadcast the played card to all the players (no matter what: whether last card or not)
		 */
		ProtocolMessage bmsg = new ProtocolMessage(ProtocolMessageTypes.CURRENT_TRICK, card);
		bmsg.setSender(nsender);
		broadcastUpdate(bmsg);
		updateTurn();
		
		/*
		 * i.e Everyone has played, determine the winner.
		 */
		if (currentTrick.subdeck.size() >= nPlayers) {
			currentTrick.bClosed = true;
			// Figure out who won, set bWinner;
			int i=0;
			Card leadingCard = null;
			/*
			 * everyone has played, determine who won the trick, and set it closed
			 */
			for (Card c : currentTrick.subdeck.subdeck) {
				// the actual player who won the trick is n players away from the leader or the leader
				if (i == 0) {
					leadingCard = c;
					currentTrick.winner = currentTrick.leader;
					}
				else if (isHigher(c, leadingCard)) {	// true if c is higher than leadingcard
					if (bDebugCardCf)
						gameErrorLog("->T");
					leadingCard = c;
					currentTrick.winner = (i + currentTrick.leader) % nPlayers;
					}
				else {
					if (bDebugCardCf)
						gameErrorLog("->F");
					// card was sloughed
					}
				i++;
				}
			/*
			 * now broadcast the completed trick with a CURRENT_TRICK message for the closed trick
			 *  Encode the trick and send it.
			 */	
			/*
			 * Do NOT send out current_trick with a subdeck anymore...
			 */
			
			nTrickId ++;
			if (nTrickId >= LAST_TRICK) {
				// Game is over; total score and reset
				totalScores();
				nCurrentTurn = -1;
				return;
				}
			/*
			 * now broadcast the completed trick with a CURRENT_TRICK message for the closed trick
			 *  aaa
			 *  modifying for TRICK_CLEARED -- under construction
			 *  Encode the trick and send it.
			 */
			bmsg = new ProtocolMessage(ProtocolMessageTypes.TRICK_CLEARED);
			bmsg.trick = currentTrick;
			broadcastUpdate(bmsg);			

			Trick lastTrick=currentTrick;
			gameErrorLog("TrickCleared:" + nTrickId + " Leader:" + currentTrick.leader +
					"Winner:" + lastTrick.winner + "<" + currentTrick.subdeck.encode() + ">");
			currentTrick = new Trick(nTrickId);
			if (lastTrick.heartsBroken())
				currentTrick.breakHearts();
			currentTrick.leader = lastTrick.winner; // the leader is the player who won the last trick...
			trickArray[nTrickId] = currentTrick;
			
			/*
			 * the person who plays next is the current trick winner!
			 */
			updateTurn(lastTrick.winner);
			} // if everyone has played; trick is cleared

		}	// trickUpdate
	
	/*
	 * playCard - detect if the sender can legally play the card; emit error message if not
	 *  if legal, send updates and progress turn
	 */
	public void playCard(int nsender, Card c) {
		Player p = playerArray[nsender];
		Subdeck pcards = p.subdeck; // server side official representation of the player's hand
		ProtocolMessage returnMessage = null;
		ProtocolMessageTypes mtype=ProtocolMessageTypes.PLAY_CARD;
		
		gameErrorLog("Msg<" + mtype + ">from(" + nsender + ") " + pcards.encode());
		if (c == null) {
			returnMessage = new ProtocolMessage(ProtocolMessageTypes.PLAYER_ERROR, "%MSG:Protocol error no card!%");
			p.sendToClient(returnMessage);
			return;
			}
		gameErrorLog("Housekeeping: player(" + nsender + ") plays <" + c.encode() + ">");

		if (!pcards.find(c)) {
			gameErrorLog("Housekeeping: find failed<" + c.encode() + "> from(" + nsender + ") subdeck size("
					+ pcards.size() + "){" + pcards.encode() + "}");
			returnMessage = new ProtocolMessage(ProtocolMessageTypes.PLAYER_ERROR,
					"%MSG:Player" + p.pid + " doesn't have <" + c.rank + c.suit + ">!%");
			p.sendToClient(returnMessage);
			return;
		}

		// So I know that the player has that card.
		// Is this a legal follow?
		// It's a legal follow if it's the same as the lead
		// or if it's a slough then the player doesn't have any cards the same suit as the lead
		if (currentTrick.subdeck.size() > 0) {	// a follow
			Card cLead=currentTrick.subdeck.peek();
			Suit leadSuit=cLead.suit;
			if (c.suit == leadSuit) {
				// Yea! Player followed suit. It's legal.
				}
			else {
				// didn't follow lead. Is that ok? 
				if (!p.subdeck.isVoid(leadSuit)) {
					// Uh oh. Player could have followed suit but didn't. Error detected!
					returnMessage = new ProtocolMessage(ProtocolMessageTypes.PLAYER_ERROR,
							"%MSG:Player" + p.pid + " required to follow suit <" + leadSuit + ">!%");
					p.sendToClient(returnMessage);
					return;
					}
				if (c.suit == Suit.HEARTS) {
					// Hearts are broken
					currentTrick.breakHearts();
					// broadcast message?
					}
				}
			}
		else {	// lead...
			// Is this a legal lead? hhh
			// If it's a heart, then hearts must be broken, or only has hearts
			if (c.suit == Suit.HEARTS) {
				// lead a heart. Is that ok?
				if (currentTrick.heartsBroken())
					; // ok
				else if (p.subdeck.hasOnly(Suit.HEARTS))
					; // ok
				else {
					returnMessage = new ProtocolMessage(ProtocolMessageTypes.PLAYER_ERROR,
							"%MSG:Player" + p.pid + " Cannot lead a heart until hearts are broken!%");
					p.sendToClient(returnMessage);
					return;					
					}
				}
			}
		
		// TODO: add to the trick and send to players
		trickUpdate(nsender, c);

		/*
		 * Delete the games copy of the card in hand, and then send message to client to
		 * delete the same card
		 */
		gameErrorLog("Housekeeping: delete<" + c.encode() + "> from {" + pcards.encode() + "}");

		pcards.delete(c.rank, c.suit);
		returnMessage = new ProtocolMessage(ProtocolMessageTypes.DELETE_CARDS, c);
		p.sendToClient(returnMessage);

		gameErrorLog("Play_Card successful.");
		/*
		 * A turn has successfully completed. the next player is up (updateTurn). then
		 * call nextMove() to enqueue message.
		 */
		// No: don't do this if there is a current turn winner which was already set by trickupdate...
		// Note: updateturn is set in trickupdate...
		//updateTurn();
		sendNextMove();
		//break;		 
		}
	
	 //* xxx	new (but unimplemented) gameInterface methods here...

	public void passCards(int nsender, Subdeck cards) {
		gameErrorLog("Can't happen: Game received unimplemented PASS_CARDS request.");		
		}

	public void bidTricks(int nsender, Subdeck cards) {
		gameErrorLog("Can't happen: Game received unimplemented BID request. Bidding not yet implemented.");		
		}

	public void query(int nsender, String s) {
		gameErrorLog("Can't happen: Game received unimplemented QUERY_* request.");		
		}

	public void superuser(int nsender, String s) {
		gameErrorLog("Can't happen: Game received unimplemented SUPERUSER request.");		
		}
	

	/*
	 * process a (well-formed previously parsed) protocol message from a client in a
	 * game context
	 * 
	 * .. call serverErroLog(string) to log errors
	 */
	void process(ProtocolMessage m) {

		// ..is there such a player?
		int nSender = m.sender;
		if (nSender < 0 || nSender >= nPlayers) {
			// no such player; I'm somehow processing a kernel message; can't happen
			gameErrorLog("Can't happen: Game sent a game kernel message. msg ignored.");
			return;
		}
		Player p = playerArray[nSender];
		Subdeck pcards = p.subdeck; // server side official representation of the player's hand
		ProtocolMessage returnMessage = null;

		gameErrorLog("Msg<" + m.type + ">from(" + nSender + ") " + pcards.encode());

		switch (m.type) {
		case PLAY_CARD: // CARD
			// Whose turn is it? Is it from the right user
			// is it a legal play?
			// ..does player have that card?
			// ..hearts/spade? Are they Broken
			// if legal, add card to the trick
			// Send msg to delete card from the players hand with a protocol msg.
			// delete card from players internal representation subdeck
			// update the turn, and send trick to next player
			// is the sender the player with the turn?
			if (nSender != nCurrentTurn) {
				returnMessage = new ProtocolMessage(ProtocolMessageTypes.PLAYER_ERROR,
						"%MSG:Not your turn player" + nSender + ". It's Player" + nCurrentTurn + "'s turn!%");
				p.sendToClient(returnMessage);
				return;
			}
			// get the card played
			// gameErrorLog("Housekeeping: play<" + m.subdeck.encode() + ">");
			Card c = m.subdeck.peek();
			playCard(nSender, c);
			break;

		case PASS_CARD: // CARD*
			// is it appropriate to pass card now?
			// does the player exist and have that card?
			// determine who card should be sent to
			// Send msg to delete card from the senders hand
			// has the player to whom the card should be sent passed cards? If not queue it
			// up
			// empty queue of passes if any built up
			break;
		case GAME_QUERY:
		case SUPER_USER:
			String sStatus = getGameStatus();
			break;
		default:
			// Unhandled message in process game kernel.
			// TODO: Log errors, etc.
		}
	}

	void reset() {
		//
		// TODO: Make sure there are nPlayers and add robots if there aren't
		//

		/*
		 * seat the human players at the table, and add robots to fill in the game
		 */
		populatePlayers();
		
		//
		// Create a new pack of cards and shuffle them
		//
		Subdeck pack = new Subdeck(52);
		/*
		 * shuffle
		 */
		if (bShuffle)
			pack.shuffle();
		//
		// deal official copy of cards
		//
		int i;
		Player p;
		for (i = 0; pack.size() > 0; i++) {
			i = i % nPlayers;
			Card c = pack.pullTopCard();
			/*
			 * if there aren't a full complement of players cards get thrown away for now...
			 * should throw an exception and a hissy fit.
			 */
			if (playerArray[i] != null) {
				if (c.equals(Rank.DEUCE, Suit.CLUBS))
					updateTurn(i);	//	nCurrentTurn = i;
				if (playerArray[i].subdeck == null)
					gameErrorLog("can't happen:null subdeck.");
				//gameErrorLog("adding:<" + c.encode() + "> to" + playerArray[i].subdeck.encode());
				//
				// TODO: call ssAddCard
				playerArray[i].subdeck.add(c);
			}
		}
		/*
		 * create the first trick and initialize for gameplay
		 */
		nTrickId = 0;
		Trick t = new Trick(nTrickId);
		t.leader = nCurrentTurn;
		trickArray[nTrickId] = t;
		currentTrick = t;

		//
		// Now send the protocol message to add them to the players hand
		for (i = 0; i < nPlayers; i++) {
			p = playerArray[i];
			ProtocolMessage pm = new ProtocolMessage(ProtocolMessageTypes.ADD_CARDS, p.subdeck);
			p.sendToClient(pm);
		}

		// Did something whack the subdeck?
		for (i = 0; i < nPlayers; i++) {
			Subdeck sd = playerArray[i].subdeck;
			gameErrorLog("Housekeeping: subdeck size(" + sd.size() + "){" + sd.encode() + "}");
			}

		//
		// Send the message to the first player to start...
		sendNextMove();
	}

	/*
	 * disconnect -- delete human player entry that this was attached to, and add robot player
	 */
	@Override
	public void disconnect(int pid) {
		RobotPlayer robot = new RobotPlayer(pid, this);
		/*
		 * TODO: Should transfer the cards that are in the hand; and 
		 * if it is his turn (as it probably is, since everyone responds quickly...)
		 * then pass a YOUR_TURN message.
		 */
		playerArray[pid] = robot;
		if (nCurrentTurn == pid) {
			ProtocolMessage pm=new ProtocolMessage(ProtocolMessageTypes.YOUR_TURN);
			robot.process(pm);
			}
		}
			
		

}
