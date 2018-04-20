package cardGame;

/*
 * This is moved to a different package... Had to recreate by copying... Bad library management, ok.
 */

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.util.Random;

/*
 * On init...
 * Open the resource files and store into an array.
 * Subsequent uses access the array of buffered images
 */
public class CardFace {
    static Boolean resourcesLoaded=false;
	public CardFace() {
    	if (resourcesLoaded)
    		return;

    	initCardImages();
	}
	
	/*
	 * Class function that doesn't require an instance of a CardFace
	 */
	public static BufferedImage getCardFace(int iOrdinal) {
		if (!resourcesLoaded) {
			new CardFace();
			}
		return cardFaces[iOrdinal];
	}
	
	BufferedImage CardFace(int iOrdinal) {
		//
		// if we survive init, the array is filled in...
		if (iOrdinal < 0 || iOrdinal >= 53)	// 52 cards + 1 (i.e. the back)
			return null;
		return cardFaces[iOrdinal];
		}
	
    static long cardWidth=200;
    static int cardHeight=500;
    /*
     * cardFaces and image files are true class variables.
     * They are to be initialized (from the resource files) only once per application
     */
    static BufferedImage cardFaces[];
    
    /*
     * These (renamed) files royalty free and courtesy of 
     * http://all-free-download.com/free-vector/playing-cards-images-download.html
     * They are stored as a resource using Eclipse/Import
     */
    static String imageFiles[]= {
        	"Clubs.JPG",
        	"Diamonds.JPG",
        	"Hearts.JPG",
        	"Spades.JPG",
        };
    
    // Index 52 is the back...
    
    //
    // return next available index
    //
    // private int extractCardImages (BufferedImage cfaces[], int index, String sFile) {     }
    
    private void initCardImages() {
    	
    	if (resourcesLoaded)
    		return;
    	
    	//URL url = new URL(getCodeBase(), "examples/strawberry.jpg");
    	//ii = createImageIcon("c:\\temp/KC.JPG","");
    	//
    	// Make sure that the resource images have been imported into the project
    	// using the blackmagic boogiewoogie of file import
    	//
    	//ii = createImageIcon("/9C.JPG","");
    	
        try {
            // image = ImageIO.read(getClass().getResource("/KC.JPG"));
        	//URL file = getClass().getResource("/KC.JPG");
        	// URL file = getClass().getResource("/Clubs.JPG");
        	cardFaces = new BufferedImage[53];	// 52 cards + the back of a card
        	int iFile, index=0;
        	for (iFile=0; iFile<4; iFile++) {
        		String fn = imageFiles[iFile];
        		URL file = getClass().getResource(fn);
        		if (file == null)
        			System.out.println("error: Cannot open card resource file:" + fn);
        		else {
        			String tmpFname = file.getFile();
        			// System.out.println(tmpFname);
        			BufferedImage image = null;
        		    BufferedImage subimage = null;
        			image = ImageIO.read(file);     		
        			// Now get subimages for each card... (5 items per row x 3 columns)
        			int i, j, rank, startX = 0, startY = 0;
        			for (rank=i=0; i<3; i++) {	// 3 rows
            			cardWidth = Math.round(image.getWidth() / 5);	// Math.round??
            			cardHeight = Math.round(image.getHeight() / 3);
        				for (startX=j=0; j<5 && rank < 13; j++,rank++)	{	// 5 cards per line are packed in the images
        					// Rescue the edge of the last card in a row
        					if (j == 4 ||
        							(startX + cardWidth) > image.getWidth()) {
        						cardWidth = cardWidth - 17;	// fudge factor... 17 is close!
        						startX = (int) (image.getWidth() - cardWidth);
        						}
        					if (i == 2 ||
        							(startY + cardHeight > image.getHeight())) {
        						cardHeight = cardHeight - 3;
        						startY = image.getHeight() - cardHeight ;
        						}
        					subimage = image.getSubimage(startX,  startY,  (int) cardWidth,  cardHeight);
        					cardFaces[index++] = subimage;
        					startX = (int) (startX + cardWidth) + 5;	// I hate these fudge factors
        					}
        				startY = startY + cardHeight + 3;
        				}
            		// Get the back of the card... It's the last card in the file (joker is penultimate)
        			// Make sure you only do this ONCE... Not once per file!
        			if (index > 50 ) {
        				startX = (int) (image.getWidth() - cardWidth);
        				startY = image.getHeight() - cardHeight ;
        				subimage = image.getSubimage(startX,  startY,  (int) cardWidth,  cardHeight);
        				cardFaces[index++] = subimage;
        				}
        			}
        		
        	
            // getClass().getResource("/resources/icon.gif")
        	}    
        }
        catch (IOException e) {
        	/* Todo: implement good hygiene
        	 *     e.printStackTrace();
        	 *     */
        	e.printStackTrace();
        }
        resourcesLoaded = true;	// Only load files once
     }

	

}
