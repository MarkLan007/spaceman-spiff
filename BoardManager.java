package cardGame;

/*
 * Manage the virtual board
 *  update board for trick messages
 *  show last trick (eventually)
 *  keep track of who played what
 *  keep track of who is in the game, their score, etc
 *  parse and process game update messages
 *  call back into the gui (ClientScreen) to update the visual game board
 */
public class BoardManager {
	
	/*
	 * called when clientscene is building the scene
	 */
	static BoardManager theBoard=null;
	
	int nPlayers=-1;
	GamePlayer[] playerArray=null;
	
	class GamePlayer {
		int playerid;	// player id in the rotation (0-4 or 0-6)
		String name;
		Card currentCard;
		}
	
	public static void init() {
		theBoard = new BoardManager(6);
		/*
		 * Assume 6 person hand at first; parameterize later
		 */
		}
	
	BoardManager(int nplayers) {
		nPlayers = nplayers;
		playerArray = new GamePlayer[nplayers];
		int i;
		for (i=0; i<nplayers; i++)
			playerArray[i] = new GamePlayer();
		}

	/*
	 * update -- update theBoard with update messages and call back to the gui to display changes
	 */
	static void update(ProtocolMessage pm) {
		System.out.println("game message>>>" + pm.type);
		
		switch (pm.type) {
		case YOUR_TURN:
			System.out.println("game message>>>" + pm.usertext);
			// How do you get back to the UIThread?
			break;
		case CURRENT_TRICK:
			System.out.println("current trick>>>" + pm.encode());
			
			break;
		default:
			System.out.println("update: unhandled message>>>" + pm.type + pm.usertext);

		
			}

		
		}
	
}
