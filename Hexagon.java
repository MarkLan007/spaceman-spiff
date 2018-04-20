package cardGame;

import javax.swing.SwingUtilities;

/*
 * Hexagon drawing
 */
// Hexagon drawing
class Hexagon {
	double[] points;
	double[] proportionatePoints;
	double center;
	//

	public Hexagon(double width, double height) {
		center = hypot(width, height);
		points = new double[12];
		proportionatePoints = new double[12];
		// Hexagon (unscaled) with Radius R=1; between 0 and 2 on each axis
		// X Y
		proportionatePoints[0] = 1.0;
		proportionatePoints[1] = 0.0; // 12 0'Clock
		proportionatePoints[2] = 1.833;
		proportionatePoints[3] = 0.5; // 2... x is Cos and Y is sin of 30 Degrees
		proportionatePoints[4] = 1.833;
		proportionatePoints[5] = 1.5; // 4....
		proportionatePoints[6] = 1.0;
		proportionatePoints[7] = 2.0; // 6 O'Clock
		proportionatePoints[8] = 0.17;
		proportionatePoints[9] = 1.5; // 8 O'Clock
		proportionatePoints[10] = 0.17;
		proportionatePoints[11] = .5; // 10 O'Clock

	}

	private double hypot(double width, double height) {
		return ((Math.sqrt(width * width + height * height)));
	}

	public void computePoints(double width, double height) {
		double h = hypot(width, height);
		double aspectRatio = width / height;
		int i, j;
		double xoffset = 0.0, yoffset = 0.0;
		double scaleFactor = h / aspectRatio;
		double scaleto = 0.0;
		double xscale = 0.0, yscale = 0.0;
		double radius;

		if (width > height) {// scaleto height; that's what the limiting dimension is
			radius = height / 2.0;
			xoffset = width / 2 - radius;
			scaleto = height;
			xscale = height;
			yscale = height;
		} else { // otherwise scale to width;
			radius = width / 2.0;
			yoffset = height / 2 - radius;
			scaleto = width;
			xscale = width;
			yscale = width;
		}
		/*
		 * Turn off offsets...
		 */
		xoffset = 0.0;
		yoffset = 0.0;
		for (j = 0; j < 6; j++) {
			int x, y;
			// position the hexagon by scaling the coordinates to the window size
			x = j * 2;
			y = x + 1;
			points[x] = xoffset + proportionatePoints[x] * radius;
			points[y] = yoffset + proportionatePoints[y] * radius;
			xCoords[j] = points[x];
			yCoords[j] = points[y];
			}
		return ;
		
		}
	
	double [] xCoords=new double[6];
	double [] yCoords=new double[6];

	public double[] getPointsX() {
		return xCoords;
		}
	public double[] getPointsY() {
		return yCoords;
		}
	
	public double[] getPoints(double width, double height) {
		double h = hypot(width, height);
		double aspectRatio = width / height;
		int i, j;
		double xoffset = 0.0, yoffset = 0.0;
		double scaleFactor = h / aspectRatio;
		double scaleto = 0.0;
		double xscale = 0.0, yscale = 0.0;
		double radius;

		if (width > height) {// scaleto height; that's what the limiting dimension is
			radius = height / 2.0;
			xoffset = width / 2 - radius;
			scaleto = height;
			xscale = height;
			yscale = height;
		} else { // otherwise scale to width;
			radius = width / 2.0;
			yoffset = height / 2 - radius;
			scaleto = width;
			xscale = width;
			yscale = width;
		}
		for (j = 0; j < 6; j++) {
			int x, y;
			// position the hexagon by scaling the coordinates to the window size
			x = j * 2;
			y = x + 1;
			points[x] = xoffset + proportionatePoints[x] * radius;
			points[y] = yoffset + proportionatePoints[y] * radius;
		}
		return points;
	}
}
// And you can use it for create a polygon just like this:

// Polygon hexagon = new Polygon(new Hexagon(100d).getPoints());
// End Hexagon drawing
class Test {

	/* public static */ void mainXXXX(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// initAndShowGUI();
			}
		});
	}
}
