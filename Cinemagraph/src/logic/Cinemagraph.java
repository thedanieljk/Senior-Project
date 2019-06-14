package logic;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
//import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;

import delaunay_triangulation.Delaunay_Triangulation;
import delaunay_triangulation.Point_dt;



public class Cinemagraph {
	/*
	 * The Function takes in an existing JFrame and opens a gif in the same folder as the project
	 */

	ArrayList<Point> pts;
	
	public void playGif(JFrame f, URL url) throws MalformedURLException {

		ImageIcon icon = new ImageIcon(url);
	    JLabel label = new JLabel(icon);
		
	    //JFrame f = new JFrame("Cinemagraph");
	    f.getContentPane().add(label);
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    f.pack();
	    f.setLocationRelativeTo(null);
        f.setVisible(true);
	}
	
	
	/* 
	 * Opens the gif as an ArrayList of BufferedImages, allowing me to look at individual frames 
	 * This is done in order to pull height, width, and RGB data from the gif
	 */
	
	public ArrayList<BufferedImage> getImages(File gif) throws IOException {
		ArrayList<BufferedImage> imgs = new ArrayList<BufferedImage>();
		ImageReader rdr = new GIFImageReader(new GIFImageReaderSpi());
		rdr.setInput(ImageIO.createImageInputStream(gif));
		for (int i=0;i < rdr.getNumImages(true); i++) {
			imgs.add(rdr.read(i));
		}
		return imgs;
	}
	
	
	/*
	 * Creates a transparent buffered image that is overlayed on the gif
	 * This is done to allow the highlighting of the gif (through changing the transparency of this transparent image
	 * in areas in which the user has selected
	 */
	
	// Make a separate function to make an individual pixel transparent?
	public BufferedImage getTransImg(int width, int height) {
		BufferedImage transImg = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		//set the color before the loop
		int alpha = 0;
		Color tColor = new Color(255,0,0,alpha);
		int rgba = tColor.getRGB();
		
		for (int y = 0; y < height ; y++) {
			for (int x = 0; x < width ; x++) {
				//set to transparent
				transImg.setRGB(x, y, rgba);
			}
		}
		
		return transImg;
	}
	
	/*
	 * Plays simple Greeting. (Usage instructions?)
	 */
	public void playGreeting() {
		System.out.println("Welcome to Daniel's Cinemagraph maker.");
		System.out.println("This will only work for uncompressed gifs");
		playUsage();
		
	}
	
	/*
	 * Play Instructions for using the Cinemagraph Maker
	 * Edited based upon people's input
	 */
	public void playInstructions() {
		System.out.println("\nYou are now ready to select areas.");
		System.out.println("In normal mode, areas that you do not select will become still");
		System.out.println("In black and white mode, areas you select will become black and white");
		System.out.println("Press the left mouse button to select a point");
		System.out.println("After 3 or more points, you may press \"p\""
				+ "\nAnd Delaunay Triangulation will be used to mark an area around points you have selected");
		System.out.println("Pressing \"o\" will clear the points you have selected");
		System.out.println("Press SPACE when you are done marking areas");
		System.out.println("You may press ESCAPE to quit at any time");
	}
	
	public void printCoords(int sX, int sY, int eX, int eY) {
		System.out.println("Starting at " + sX + "," + sY);
		System.out.println("Ending at " + eX + "," + eY);	
	}
	
	/*
	 * Function to fill mask - false
	 */
	public boolean[][] fillMask(boolean arr[][]) {
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0;j < arr[0].length; j++) {
				arr[i][j] = false;
			}
		}
		return arr;
	}
	
	/*
	 * Update the Mask based upon where it was clicked/released
	 */
	
	public void updateMask(Mask m, BufferedImage tImg, Delaunay_Triangulation dt) {
		//Setup red overlay
		int alpha = 128;
		Color tColor = new Color(255,0,0,alpha);
		int rgba = tColor.getRGB();
		
		//Get bbox from dt
		Point_dt min = dt.bb_min();
		Point_dt max = dt.bb_max();
		
		for (int i = (int)min.x(); i <= max.x() ; i++) {
			for (int j = (int)min.y(); j <= max.y(); j++) {
				if (dt.contains(i, j)) {
					m.mask[i][j] = true;
					tImg.setRGB(i, j, rgba);
				}
			}
		}

	}
	
	public void updatePixel(Mask m, int x, int y) {
		int rgb = m.imgs.get(0).getRGB(x, y);
		for (int i = 0; i < m.imgs.size() ; i++ ) {
			//System.out.println("Updating at " + x + "," + y);
			try {
				m.imgs.get(i).setRGB(x,y,rgb);
			} catch (Exception e) {
				//System.out.println( e.getClass().getCanonicalName() + " at index " + i);
			}
		}
	}
	
	public void bnwPixel(Mask m, int x, int y) {
		for (int i = 0; i < m.imgs.size() ; i++ ) {
			try {
				if (m.mask[x][y] == true) {
					Color tempColor = new Color(m.imgs.get(i).getRGB(x, y));
					
					
					int red = (int)(tempColor.getRed() * 0.21);
					int green = (int)(tempColor.getGreen() * 0.72);
					int blue = (int)(tempColor.getBlue() * 0.07);
					
					Color bnwColor = new Color(red,green,blue,255);
					int bnw = bnwColor.getRGB();
					
					m.imgs.get(i).setRGB(x, y, bnw);

				}
				
			} catch (Exception e) {
				System.out.println("Exception in bNw");
			}
		}
		
	}

	/*
	 * Function to edit each Image
	 */
	public void editImage(Mask m, boolean bNw) {	
		for (int i = 0; i < m.mask.length; i++) {
			for (int j = 0; j < m.mask[0].length; j++) {
				if (m.mask[i][j] == false && bNw == false) {
						updatePixel(m,i,j);
					}
				else {
					if (bNw) {
						bnwPixel(m,i,j);
					}
				}
			
			}
		}
	}
	
	public void convertGif(Mask m, String outputName) throws IIOException, IOException {
		BufferedImage firstImage = m.imgs.get(0);
		ImageOutputStream output = new FileImageOutputStream(new File(outputName));
		GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 1, true);
		
		writer.writeToSequence(firstImage);
		for (int i = 1; i < m.imgs.size();i++) {
			BufferedImage nextImage = m.imgs.get(i);
			writer.writeToSequence(nextImage);
		}
		System.out.println("The gif has been written to " + outputName);
		writer.close();
		output.close();
	}
	
	public static void playUsage() {
		System.out.println("Usage: java Cinemagraph path-to-input-file outputName [-b]");
		System.out.println("Where [-b] is an optional flag to select areas to be in black and white");
	}
	
	public static void argsError() {
		playUsage();
		System.exit(1);
	}
	
	public static Data processArgs(String[] args) {
		Data d;
		if (args.length == 3 ) { //Flag
			if (args[2].equals("-b")) {
				System.out.println("\nBlack and white mode has been selected");
				System.out.println("Select areas you wish to make black and white");
				d = new Data(true, args[0], args[1]);
			}
			else {
				//Error, incorrect flag
				System.out.println("Incorrect flag!");
				argsError();
				d = new Data(false, "error", "error");
			}
		}	
		else if (args.length == 2) { //No flag
			d = new Data(false, args[0], args[1]);
		}
		else { //Incorrect args (should be 2 or 3)
			System.out.println("Incorrect number of arguments!");
			argsError();
			d = new Data(false, "error", "error");
		}
		return d;
		
	}
	
	public static void printData(Data d) {
		System.out.println("black and white boolean: " + d.bNw);
		System.out.println("Input name: " + d.inputName);
		System.out.println("Output name: " + d.outputName);
	}
	
	public static void printPt(Point p) {
		int tempX = p.x;
		int tempY = p.y;
		System.out.println("Point selected" + ": (" + tempX + "," + tempY + ")");
	}
	
	public static void printPts(ArrayList<Point> pts) {
		System.out.println("Using Delaunay triangulation on " + pts.size() + " points");
		for (int i=0; i<pts.size(); i++) {
			Point temp = pts.get(i);
			System.out.println("Point " + i + ": (" + temp.x + "," + temp.y + ")");
		}
	}
	
	public static void ptsToDt(ArrayList<Point> pts, ArrayList<Point_dt> pts_dt) {
		for (int i=0;i<pts.size(); i++) {
			Point temp = pts.get(i);
			pts_dt.add(new Point_dt(temp.x,temp.y));
		}
	}
	
	public static void printDt(Delaunay_Triangulation dt) {
		Point_dt min = dt.bb_min();
		Point_dt max = dt.bb_max();
		
		System.out.println("MIN: (" + min.x() + "," + min.y() + ")");
		System.out.println("MAX: (" + max.x() + "," + max.y() + ")");
		System.out.println("Size: " + dt.CH_size());
	}
	
	@SuppressWarnings("serial")
	public static void main(String[] args) throws MalformedURLException, IOException, IIOException {
		Cinemagraph cin = new Cinemagraph();
		cin.playGreeting();
		Data data = processArgs(args);
		
		//Get gif as ArrayList of BufferedImages, create JFrame using this data
		URL path = Cinemagraph.class.getResource(data.inputName);
		
		if (path == null) {
			argsError();
		}
		ArrayList<BufferedImage> imgs = cin.getImages(new File(path.getFile()));

		int width = imgs.get(0).getWidth();
		int height = imgs.get(0).getHeight();
		boolean mask[][] = new boolean[width][height];
		mask = cin.fillMask(mask);
		Mask m = new Mask(mask,imgs);

		JFrame f = new JFrame("Cinemagraph");
			
		//Make a transparent image that is on top of the gif	
		BufferedImage transImg = cin.getTransImg(width, height);
		f.getContentPane().add(new JLabel(new ImageIcon(transImg)));
		f.pack();
		f.setVisible(true);
		//should be transparent layer now
		
		cin.playGif(f, path); //play the gif (command line arg 0)
		
		//Play instructions once the gif has been displayed
		cin.playInstructions();
		
		ArrayList<Point> pts = new ArrayList<Point>();
		ArrayList<Point_dt> pts_dt = new ArrayList<Point_dt>();
		
		//Allow user to select the area
		JPanel p = new JPanel() {
	        Point pointStart = null;
	        Point pointEnd   = null;
	        
	        Cinemagraph c = new Cinemagraph();
	        
	        {
	            addMouseListener(new MouseAdapter() {
	                public void mousePressed(MouseEvent e) {
	                	Point temp = e.getPoint();
	                    //pointStart = e.getPoint();
	                	printPt(temp);
	                    pts.add(temp);
	                }

	                /*
	                public void mouseReleased(MouseEvent e) {
	                    pointEnd = e.getPoint();
	                    //c.printCoords(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
	                    c.updateMask(m,pointStart.x, pointStart.y, pointEnd.x, pointEnd.y, transImg);
	                    repaint();
	                }
	                */
	            });
	        }
	    };
	    f.add(p);
	    f.setVisible(true);
	    
	    KeyStroke oKey = KeyStroke.getKeyStroke(KeyEvent.VK_O,0,false);
		Action oAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Cinemagraph c = new Cinemagraph();
				System.out.println("\nO has been pressed, clearing points");
				//c.editImage(m,data.bNw);
				//Print ArrayList
				try {
					//c.convertGif(m,data.outputName);
					System.out.println("Clearing " + pts.size() + " points");
					pts.clear();
				} catch (Exception ex) {
					System.out.println("error triangulating points");
					System.out.println( e.getClass().getCanonicalName());
				}
			}
		};
		
		f.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(oKey, "O");
		f.getRootPane().getActionMap().put("O", oAction);
	    
	    /*Set up KeyBindings for Space, which should trigger ActionEvent to begin the process of adjusting pixels*/
		KeyStroke pKey = KeyStroke.getKeyStroke(KeyEvent.VK_P,0,false);
		Action pAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Cinemagraph c = new Cinemagraph();
				System.out.println("\nP has been pressed, triangulating points and adding to mask");
				try {
					//Check if enough points to triangulate
					if (pts.size() >= 3) {
						//print out the points in list
						//printPts(pts);
						ptsToDt(pts, pts_dt);
						Delaunay_Triangulation dt = new Delaunay_Triangulation(pts_dt.toArray(new Point_dt[pts_dt.size()]));
						//At the end, clear both lists
						//printDt(dt);
						
						//update the mask and overlay based upon delaunay triangulation
	                    c.updateMask(m,transImg,dt);
	                    
	                    //clear arraylists for further use
						pts.clear();
						pts_dt.clear();
					}
					else {
						System.out.println("You must select 3 or more points to triangulate. Press \"o\" to clear points");
					}
				} catch (Exception ex) {
					System.out.println("error triangulating points");
					System.out.println( e.getClass().getCanonicalName());
				}
			}
		};
		
		f.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(pKey, "P");
		f.getRootPane().getActionMap().put("P", pAction);
	    
	    
		/*Set up KeyBindings for Space, which should trigger ActionEvent to begin the process of adjusting pixels*/
		KeyStroke space = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0,false);
		Action spaceAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Cinemagraph c = new Cinemagraph();
				System.out.println("\nSpace has been pressed, editing gif now");
				c.editImage(m,data.bNw);
				try {
					c.convertGif(m,data.outputName);
				} catch (Exception ex) {
					System.out.println("error converting to gif");
					System.out.println( e.getClass().getCanonicalName());
				}
			}
		};
		
		f.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(space, "SPACE");
		f.getRootPane().getActionMap().put("SPACE", spaceAction);
		
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0,false);
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Escape pressed, exiting.");
				System.exit(0);
			}
		};
		
		f.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		f.getRootPane().getActionMap().put("ESCAPE", escapeAction);
		
		
	}
}
