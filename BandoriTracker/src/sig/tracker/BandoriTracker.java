package sig.tracker;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class BandoriTracker {
	public static BufferedImage capture;
	public static BufferedImage footprint_img;
	public static Robot robot = null;
	static UpdateEvent runEvent = new UpdateEvent();
	static Timer programClock = new Timer(16,runEvent);
	public static DrawPanel panel;
	public static final ModeType MODE = ModeType.FOOTPRINT;
	public static ModeType FOOTPRINT_MODE = ModeType.FOOTPRINT_ORIGIN1;
	public static Point origin1,origin2;
	public static Character character;
	public static DChar displayedFootprint;
	public static List<DChar> footprint_database = new ArrayList<DChar>();
	public static void main(String[] args) {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		capture = robot.createScreenCapture(new Rectangle(0,0,(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
		
		JFrame f = new JFrame();
		panel = new DrawPanel();
		f.add(panel);
		f.pack();
		f.setSize(500, 500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				f.setVisible(true);
			}
			
		});
		programClock.start();
		
		switch (MODE) {
			case FOOTPRINT:{
				JFileChooser browse = new JFileChooser();
				browse.setDialogTitle("Select Footprint File");
				if (browse.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String fileloc = browse.getSelectedFile().getAbsolutePath();
					try {
						footprint_img = ImageIO.read(new File(fileloc));
						JOptionPane.showMessageDialog(null, "Click on origin location.");
						FOOTPRINT_MODE = ModeType.FOOTPRINT_ORIGIN1;
						ChangeCursor(Cursor.CROSSHAIR_CURSOR);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}break;
			default:{
				
			}
		}
	}
	public static void ChangeCursor(int cursor) {
		BandoriTracker.panel.setCursor(Cursor.getPredefinedCursor(cursor));
	}
}

class DrawPanel extends JPanel implements MouseListener{
	
	DrawPanel() {
		addMouseListener(this);
	}

    public Dimension getPreferredSize() {
        return new Dimension(1280,480);
    }

    public void paintComponent(Graphics g) {
    	switch (BandoriTracker.MODE) {
	    	case FOOTPRINT:{
	    		if (DisplaySampledFootprint()) {
	    			g.clearRect(0, 0, getWidth(), getHeight());
	    			BandoriTracker.displayedFootprint.draw(14,new Point(0,0),g);
	    			BandoriTracker.displayedFootprint.draw(BandoriTracker.displayedFootprint.initial_size,new Point(32,0),g);
	    			BandoriTracker.displayedFootprint.draw(32,new Point(64,0),g);
	    		} else 
	    		if (BandoriTracker.footprint_img!=null) {
	    			g.drawImage(BandoriTracker.footprint_img, 0,0, null);
	    		}
	    	}break;
	    	default:{
		    	if (BandoriTracker.capture!=null) {
		    		g.drawImage(BandoriTracker.capture, 0, 0, BandoriTracker.capture.getWidth()/2, BandoriTracker.capture.getHeight()/2, null);
		    	}
	    	}
    	}
    }

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (GettingFirstClick()) {
			BandoriTracker.origin1 = e.getPoint();
			BandoriTracker.FOOTPRINT_MODE = ModeType.FOOTPRINT_ORIGIN2;
			JOptionPane.showMessageDialog(null, "Click on ending location.");
		} else
		if (GettingSecondClick()) {
			BandoriTracker.origin2 = e.getPoint();
			BandoriTracker.FOOTPRINT_MODE = ModeType.FOOTPRINT_CHARACTER;
			BandoriTracker.ChangeCursor(Cursor.DEFAULT_CURSOR);
			String input = JOptionPane.showInputDialog("What character is this representing?");
			BandoriTracker.character = input.charAt(0);
			
			DChar footprint = new DChar(BandoriTracker.footprint_img,BandoriTracker.character,BandoriTracker.origin1,BandoriTracker.origin2);
			BandoriTracker.footprint_database.add(BandoriTracker.displayedFootprint = footprint);
			BandoriTracker.FOOTPRINT_MODE = ModeType.FOOTPRINT_PRINT;
		}
		
		//System.out.println("Clicked: "+e.getX()+","+e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	boolean GettingFirstClick() {
		return BandoriTracker.MODE == ModeType.FOOTPRINT && BandoriTracker.FOOTPRINT_MODE == ModeType.FOOTPRINT_ORIGIN1;
	}
	boolean GettingSecondClick() {
		return BandoriTracker.MODE == ModeType.FOOTPRINT && BandoriTracker.FOOTPRINT_MODE == ModeType.FOOTPRINT_ORIGIN2;
	}
	boolean DisplaySampledFootprint() {
		return BandoriTracker.MODE == ModeType.FOOTPRINT && BandoriTracker.FOOTPRINT_MODE == ModeType.FOOTPRINT_PRINT;
	}
}
class UpdateEvent implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		BandoriTracker.capture = BandoriTracker.robot.createScreenCapture(new Rectangle(9+66,30+10,(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()-18-66*2,(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()-62-10*2));
		BandoriTracker.panel.repaint();
	}
}

class DChar {
	Character character;
	Point[] points;
	Integer initial_size;
	
	DChar(Image ref_img, Character c, Point origin1, Point origin2){
		Color back_col;
		this.character=c;
		BufferedImage img = toBufferedImage(ref_img);
		int width = origin2.x-origin1.x;
		int[] pixels = img.getRGB(origin1.x, origin1.y, width, origin2.y-origin1.y, null, 0, width);
		System.out.println(Arrays.toString(pixels));
		//First trim the image.
		int top_height = -1;
		int bottom_height = -1;
		for (int j=0;j<pixels.length/width;j++) {
			for (int i=0;i<width;i++) {
				if (ColorMeetsThreshold(pixels[i+j*width],25)) {
					top_height=j;
					break;
				}
			}
			if (top_height!=-1) {
				break;
			}
		}
		for (int j=pixels.length/width-1;j>=0;j--) {
			for (int i=width-1;i>=0;i--) {
				if (ColorMeetsThreshold(pixels[i+j*width],25)) {
					//Found bottom-most pixel.
					bottom_height=j+1;
					break;
				}
			}
			if (bottom_height!=-1) {
				break;
			}
		}
		int left_width = -1;
		int right_width = -1;
		for (int i=0;i<width;i++) {
			for (int j=0;j<pixels.length/width;j++) {
				if (ColorMeetsThreshold(pixels[i+j*width],25)) {
					left_width=i;
					break;
				}
			}
			if (left_width!=-1) {
				break;
			}
		}
		for (int i=width-1;i>=0;i--) {
			for (int j=pixels.length/width-1;j>=0;j--) {
				if (ColorMeetsThreshold(pixels[i+j*width],25)) {
					right_width=i;
					break;
				}
			}
			if (right_width!=-1) {
				break;
			}
		}
		//System.out.println("TOP:"+top_height+",BOTTOM:"+bottom_height+",LEFT:"+left_width+",RIGHT:"+right_width+". Size: "+pixels.length);
		pixels = Arrays.copyOfRange(pixels, top_height*width, bottom_height*width);
		int goodpixels = 0;
		for (int i=0;i<pixels.length;i++) {
			if (i%width<left_width || i%width>right_width) {
				pixels[i]=Integer.MAX_VALUE;
			} else {
				goodpixels++;
			}
		}
		int[] trim_pixels = new int[goodpixels];
		int goodpixelslot = 0;
		for (int i=0;i<pixels.length;i++) {
			if (pixels[i]!=Integer.MAX_VALUE) {
				trim_pixels[goodpixelslot++]=pixels[i];
			}
		}
		width = right_width-left_width+1;
		int height = initial_size = bottom_height-top_height;
		List<Point> pointlist = new ArrayList<Point>();
		for (int j=0;j<height;j++) {
			for (int i=0;i<width;i++) {
				if (ColorMeetsThreshold(trim_pixels[i+j*width],25)) {
					pointlist.add(new Point(i,j));
				}
			}
		}
		System.out.println(pointlist.toString());
		//System.out.println("Character height: "+height);
		points = pointlist.toArray(new Point[pointlist.size()]);
		System.out.println(Arrays.toString(points));
	}

	private boolean ColorMeetsThreshold(int color, int threshold) {
		Color col = new Color(color);
		return col.getRed()+col.getGreen()+col.getBlue()<=(255-threshold)*3;
	}

	public void draw(int text_size, Point offset, Graphics g) {
		for (int i=0;i<points.length;i++) {
			g.drawOval(offset.x+(int)(points[i].x*((double)text_size/initial_size)), offset.y+(int)(points[i].y*((double)text_size/initial_size)), 1, 1);
		}
	}
	
	public void draw(Graphics g) {
		draw(initial_size,new Point(0,0),g);
	}
	
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
}

enum ModeType {
	NORMAL,
	FOOTPRINT,
	FOOTPRINT_ORIGIN1,
	FOOTPRINT_ORIGIN2,
	FOOTPRINT_CHARACTER,
	FOOTPRINT_PRINT
	
}