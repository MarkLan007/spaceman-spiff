package cardGame;

import java.util.Comparator;
/*
 * Subdeck -- manage a portion of a deck, from 0-N cards
 */
//import java.util.Collection;
//import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/*
 * TODO: can I implement Iteration interface? Can I delegate to LinkedList.next? etc?
 */
public class Subdeck {
	LinkedList<Card> subdeck ;
	boolean isSorted=false;
	/*
	 * TODO: Implement these by delegating to LinkedList. Will that work?
	 * First iteration stuff...
	 *   boolean hasNext();
	 *   E next();
	 *   void remove();
	 */
	/* Not now... a bridge too far...
	 * public interface Iterator<E> {
		boolean hasNext();
		Card next();
		void remove();		
		}
	*/
	
	/*
	 * Subdeck - simple working man's Subdeck. Just the list, please, Ma'am, empty.
	 */
	public Subdeck() {
		subdeck = new LinkedList<Card>();
	}
	
	public Subdeck(String sCards) {
		this();	// call default constructor first. 
		/*
		 * Then create cards from the string [This is the only place that does this.]
		 * TODO: It shouldn't be in this module, really.
		 * Let him who is without sin cast the first stone.
		 */
		sCards = ProtocolMessage.trim(sCards);
		int i;
		int len = sCards.length();
		if (len % 2 == 1) {
			System.out.println("Error: Trying to make subdeck with odd number of characters:" + len);
			len--;
			System.out.println("Making do. Likely ignoring a card.");
			}
		for (i=0; i<len; i=i+2) {
			String sCard=sCards.substring(i, i+2);
			Card c=new Card(sCard);
				subdeck.add(c);
			}

		}
	
	boolean isVoid(Suit st) {
		for (Card c: subdeck)
			if (c.suit == st)
				return false;
		return true;
		}
	
	boolean hasOnly(Suit st) {
		for (Card c: subdeck)
			if (c.suit != st)
				return false;
		return true;		
		}
	
	public String encode() {
		String s="";
		for (Card c: subdeck)
			s = s + c.encode();
		return s;
		}

	/*
	 * return cards of the suit from subdeck
	 */
	public Subdeck getSuit(Suit st) {
		Subdeck returnSubdeck = null;
		
		for (Card c: subdeck) {
			if (c.suit == st) {
				if (returnSubdeck == null)
					returnSubdeck = new Subdeck();
				returnSubdeck.add(c);
				}
			}
		return returnSubdeck;
		}
	public boolean find(Card card) {
		for (Card c: subdeck)
			if (card.rank == c.rank && card.suit == c.suit)
				return true;
		return false;
		}
	
	public boolean find(Rank rk, Suit sd) {
		for (Card c: subdeck)
			if (rk == c.rank && sd == c.suit)
				return true;
		return false;
		}
	/*
	 * Create a new pack of nCards
	 *  valid values of nCards now are 
	 *  	52 (4 handed game)
	 *  	102 (6 handed game/2 packs)
	 *  TODO: throw an exception and a hissy fit of it makes no sense
	 *  TODO: arguably this has too much card game related information for this level. 
	 *  Maybe a deck or pack should extend a subdeck... 
	 */
	public Subdeck(int nCards) {
		this();	// Call Subdeck() as first thing.
		
		int i;		
		switch (nCards) {
		case 102:
			// Create 2 new decks and delete 1 AC and 1 2D
		       for (i=0; i<52; i++) {
		           	add(new Card(i));
		        	}
		       for (i=0; i<52; i++) {
		           	add(new Card(i));
		        	}
		       /*
		        * delete the first 2C and 2D
		        */
		       delete(Rank.DEUCE, Suit.CLUBS);	// Really? I have to do this? I can't just say DEUCE, CLUBS?? guess not.
		       delete(Rank.DEUCE, Suit.DIAMONDS);
		       break;
		default:
		case 52:
		       for (i=0; i<52; i++) {
		           	add(new Card(i));
		        	}
		
			} // switch()
		
		}
	
	/*
	 * delete -- delete the FIRST instance of card(Rank,Suit) from the subdeck
	 *   -- only delete the first one so this is suitable for removing a card when it is played...
	 */
	public void delete(Rank r, Suit st) {

		for (Card c:subdeck) {
			if (c.rank == r && c.suit == st) {
				// Found it!
				subdeck.remove(c);
				break; // remember only delete first one!
				}	
			}
		
		}
	
	void delete(Card c) {
		delete(c.rank, c.suit);
		}
	

	public Card peek() {
		if (subdeck.size() > 0)
			return subdeck.peek();
		return null;
		}

	public void add(Card c) {
		subdeck.add(c);
		}
	
	public void put(int index, Card c) {
		subdeck.set(index, c);
		}
	
	public String sdump() {
	int hlen=subdeck.size();
	String sHand = "<" + hlen + ">{";
	for (Card c: subdeck)
		sHand = sHand + c.encode() + ",";
	sHand = sHand + "}";
	return sHand;
	}

	
	/*
	 * Deal -- get the top card from a subdeck
	 */
	public Card pullTopCard() {
		if (subdeck.size() == 0)
			return null;
		Card c=subdeck.pop();
		return c;
	}
	
	public Card peekLast() {
		if (subdeck.size() == 0)
			return null;
		return subdeck.peekLast();
		}
	
	public int size() {
		return subdeck.size();
	}
	
	public Card get(int n) {
		return subdeck.get(n);
	}
	
	Random rand = new Random();	// TODO: should seed with system seconds.... 
	private int randomCardIndex() {
  	int  n = rand.nextInt(52) ;
  	return n;
  	}

	static Random staticRand=new Random();
	static public Card randomCard() {
	  	int  n = staticRand.nextInt(52) ;
		Card c=new Card(n);
		return c;
		}
		
	public void shuffle() {
		int iLen = size();
		int i;
		for (i=0; i<iLen; i++) {
			Card t1, t2;
			int index;
			t1 = get(i);
			index = randomCardIndex();	// random element
			t2 = get(index);
			put(i, t2);
			put(index, t1);
			}
		
		}

	public Card pop() {
		// TODO Auto-generated method stub
		if (subdeck.size() > 0) {
			Card c=subdeck.pop();
			return c;
			}			
		return null;
	}
	
	/*
		btnConnectButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	doConnectMethod();
		        //label.setText("Accepted");
		    }
		});

	 */
	
	 
	class SortbyCardvalue implements Comparator<Card>
	{

		public int compare(Card c1, Card c2)
	    {
	    	if (c1.suit.ordinal() > c2.suit.ordinal())
	    		return -1;
	    	else if (c1.suit.ordinal() < c2.suit.ordinal())
	    		return 1;
	    	else {	// suits are equal
	    		if (c1.rank.ordinal() > c2.rank.ordinal())
	    			return -1;
	    		else if (c1.rank.ordinal() < c2.rank.ordinal())
	    			return 1;
	    		else
	    			return 0;
	    		}
	    	}
		}
	    
	
public void sort() {
	subdeck.sort(new SortbyCardvalue());
	isSorted = true;
	}

}
