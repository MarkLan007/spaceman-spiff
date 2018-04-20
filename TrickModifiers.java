package cardGame;

// This  can probably be deleted... mll
public class TrickModifiers {
	/*
	 * Trick Modifiers are flags on a trick that represent game specific things for the trick
	 *  the cards are part of the protocol message
	 */
	boolean bHeartsBroken=false;
	boolean bClosed=false; // Is this trick the final one (closed) or just a table update for the user [redundant? winner != -1?)]
	int trickid=0;		// 0-Ntricks 
	int winnerid=-1;	// winner of trick or -1
	int leader=-1; // 0-nPlayerId who lead the trick
	int player=-1; // last i.e. current player played last card

}
