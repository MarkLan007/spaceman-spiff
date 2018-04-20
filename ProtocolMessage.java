package cardGame;

import java.nio.ByteBuffer;

public class ProtocolMessage {
	int sender; // -1 for server; player id [0-n] otherwise
	ProtocolMessageTypes type;
	Boolean bMessageWellFormed = true; 
	Subdeck subdeck=null;
	String usertext;	// the rest of the story...  %[error text] %[trick modifiers] or $player scores
	Trick trick=null;
	//TrickModifiers trickModifiers=null;	// TODO: trickmodifiers should be eliminated, since I have trick
	/*
	 * Messages are of the form
	 	[CommandChar][PlayerIDorX][Zero or more RS pairs representing Cards]%[MessageString]
	 Player->G	 	
	 	= Play Card
	 	~ Pass Card(S)
	 	Q Query
	 	S Text (TBD)
	 G->Player
	 	+ Card(s)
	 	- Card(s)
	 	? Your Turn? [Cards in current trick] <change to ?>
	 	& Trick Update. Cards(s) in the current trick [Player gets one of these every time someone plays]. 
	 		// [Cards]%trick flags [Hwwxxyy[.;] [Hh] ww=winner;xx=trickid yy=lead or taker] [,.]
	 	! Trick Cleared 
	 	B Broken Suit (i.e. hearts, spades) [Could probably just put this in the trick update...]
	 	$ %Player.score;Player.score;
	 	% Player error %Message that can be given to human user i.e.
	 	[11 core key messages]
	 	
	 	ERROR Text messages that should be supported
	 	%!%Not your turn!
	 	%2%Must play 2C
	 	%N%Hearts/Spades are not broken
	 */
	/*
	 * Create the protocol message by parsing the string
	 * TODO: raise an exception if the number of elements in the Protocol MessageTypes is not equal to length of sComChars
	 */
	ProtocolMessage(ProtocolMessageTypes mt) {
		type = mt;
		}

	ProtocolMessage(ProtocolMessageTypes mt, String sText) {
		type = mt;
		usertext = sText;
		}
	

	/*
	 * Only user of this constructor is TRICK_UPDATE 
	 * TODO: why does it need a whole subdeck?
	 */
	ProtocolMessage(ProtocolMessageTypes mt, Card c) {
		type = mt;
		subdeck = new Subdeck();
		subdeck.add(c);
		}

	ProtocolMessage(ProtocolMessageTypes mt, Subdeck sd) {
		type = mt;
		subdeck = sd;		
		}
	
	/*
	 * specify the sender when you create the message
	 */
	ProtocolMessage(int pid, ProtocolMessageTypes mt, Card c) {
		sender = pid;
		type = mt;
		subdeck = new Subdeck();
		subdeck.add(c);
		}

	void setSender(int pid) {
		sender = pid;
		}
	
	String reverse(String s) {
		String t="";
		int i;
		int len=s.length();
		for (i=len-1; i>=0; i--)
			t = t + s.charAt(i);
		return t;
		}
	
	static String trim(String s) {
		int i;
		int len=s.length();
		if (len == 0)
			return s;
		for (i=len-1; i>=0; i--) {
			char c=s.charAt(i);
			if (c == '\n' || c == '\r' || c == '\t')
				;
			else
				break;
			}
		s = s.substring(0, i+1);
		return s;
		}
	
	//static final String sComChars="=~QS+-?&B$%";
	static final String sComChars="=~QS+-?&!B$%";
	ProtocolMessage(String sMsg) {
		sMsg = trim(sMsg);
		
		//
		// A protocol message must have at least a command and a sender
		int len=sComChars.length();
		if (len < 2) {
			bMessageWellFormed = false;
			return;
			}
		
		char cCom=sMsg.charAt(0);
		char cPlayer=sMsg.charAt(1);
		int i;
		//
		// find the command char and set type of message
		//
		for (i=0; i<len; i++) {
			if (sComChars.charAt(i) == cCom) {
				type = ProtocolMessageTypes.values()[i];
				break;
				}
			}
		//
		// set the player id
		//
		if (cPlayer == 'X' || cPlayer == 'x')
			sender = -1;
		else if (cPlayer >= '0' && cPlayer <= '9')
			sender = (int) cPlayer - '0';
		else {
			// oops. bad protocol msg: Can't happen.
			sender = -1;
			bMessageWellFormed = false;
			return;
			}

		//
		// Now walk through the rest of the string depending on the type of message
		//  ... but first preserver usertext as the rest of the string
		//
		usertext=sMsg.substring(2);
		switch (type) { 
		// Client msgs:
		case PLAY_CARD: 	// CARD
		case PASS_CARD: 	// CARD*
		// Server msgs:
		case ADD_CARDS: 	// CARD*
		case DELETE_CARDS: 	// CARD+
		case YOUR_TURN: 	// ?CARD* [cards in trick already played] ??
			subdeck = new Subdeck(usertext);
			break;
		case TRICK_CLEARED:	// ! nn L W B [subdeck] for trick-id Leader Winner H.Broken [Cards+]
			
			String sTrickId=sMsg.substring(2,4);	// 2,4?
			char cLeader=sMsg.charAt(4);
			char cWinner=sMsg.charAt(5);
			char cBroken=sMsg.charAt(6);
			int firstCard=sMsg.indexOf('[') + 1;
			int lastCard=sMsg.indexOf(']');	// not -1?
			String sCards=sMsg.substring(firstCard,lastCard);
			Trick t=new Trick(Integer.parseInt(sTrickId));
			t.leader = cLeader - '0';
			t.winner = cWinner - '0';
			if (cBroken == 'T' || cBroken == 't')
				t.breakHearts();
			//t.bHeartsBroken = Boolean.parseBoolean("" + cBroken); // No workee
			subdeck = new Subdeck(sCards);
			t.subdeck = subdeck;
			trick = t;
			
			break;
		case CURRENT_TRICK:	// &CARD*
			//
			// Cards are the next thing
			//  ... delegate this to the Subdeck string constructor
			//		which shouldn't know anything about the card protocol, I know, I know...
			subdeck = new Subdeck(usertext);
		case GAME_QUERY:
		case SUPER_USER:
		default:
			}
		
		
		}
	
	String encode() {
		String sMsg="";
		
		//
		// Append the type indicator
		int iTypeIndex=type.ordinal();		
		sMsg = sMsg + sComChars.charAt(iTypeIndex);
		//
		// Append the player id
		if (sender == -1)
			sMsg = sMsg + 'X';
		else
			sMsg = sMsg + (char) ((int)'0' + sender);
		
		//
		// Append the encoded cards
		/* No workee here. See subdeck.encode
		 * Let him who is without sin...
		int i;
		for (i=0; i<subdeck.size(); i++) {
			Card c=subdeck.get(i);
			sMsg = sMsg + c.encode();
			}
			*/
		String sCards="";
		if (usertext == null)
			usertext = "";
		if (trick != null) {	// trick-based protocol messages
			String strick=trick.encode();
			sMsg += strick;
			return sMsg;
			}
		if (subdeck == null) {
			// No cards being sent, so attach usertext to the message
			sMsg = sMsg + usertext + "\n";
			return sMsg;
			}
		else {
			sCards=subdeck.encode();
			// TODO: review this code... It seems odd to me now, late at night...
			sMsg = sMsg + sCards;
			if (sMsg.length() > 0)
				return sMsg + "\n";
			else
				return "%X%Error - undefined error\n";
			}
		}
	}
