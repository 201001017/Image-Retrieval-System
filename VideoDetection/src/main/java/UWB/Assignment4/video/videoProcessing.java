package UWB.Assignment4.video;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;


import org.bytedeco.javacv.Java2DFrameConverter;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

public class videoProcessing extends JFrame {
	private JButton[] button; // creates an array of JButtons
	private GridLayout gridLayout1;
	private GridLayout gridLayout2;
	private GridLayout gridLayout3;
	private JPanel buttonPanel;
	private JPanel panel;
	private static final int width = 600;

    private static final int height = 1000;

    private final JPanel videoSurface;

    private final BufferedImage image;

    private final DirectMediaPlayerComponent mediaPlayerComponent;
	private static HashMap<Integer, BufferedImage> imageMap = new HashMap<Integer, BufferedImage>();
	private static HashMap<Integer, Long> secondsMap = new HashMap<Integer, Long>();
	private static ArrayList<Integer> frameList = new ArrayList<Integer>();
	private static ArrayList<Integer> finalList;
	private static HashMap<Integer, Integer> cutMap;
	
	public static void main(String args[]) throws Exception, IOException {		
		readVideo();
		double distance[] = readHistogram();
		findThreshold(distance);
		finalList = sortList(frameList);
		System.out.println(finalList);
		new NativeDiscovery().discover();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				videoProcessing app = new videoProcessing();
				app.setVisible(true);
			}
		});
	}
	
	public videoProcessing() {
		// The following lines set up the interface including the layout of the
		// buttons and JPanels.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("VIDEO SHOT BOUNDARY DETECTION SYSTEM");
		buttonPanel = new JPanel();
		gridLayout1 = new GridLayout(1, 2, 5, 5);
		gridLayout2 = new GridLayout(6, 5, 5, 5);
		gridLayout3 = new GridLayout(1, 1, 5, 5);
		setLayout(gridLayout1);
		buttonPanel.setLayout(gridLayout2);
		add(buttonPanel);
		setSize(1100, 750);
		// this centers the frame on the screen
		setLocationRelativeTo(null);
		panel = new JPanel();
		panel.setLayout(gridLayout3);
        videoSurface = new VideoSurfacePanel();
        panel.setBounds(200, 200, width, height);
        // Add media player to panel
        panel.add(videoSurface);
            image = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration()
            .createCompatibleImage(width, height);
        BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                return new RV32BufferFormat(width, height);
            }
        };
        mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            protected RenderCallback onGetRenderCallback() {
                return new TutorialRenderCallbackAdapter();
            }
        };
		add(panel);
		button = new JButton[finalList.size()];
		for (int i = 0; i < finalList.size(); i++) {
			long sec = finalList.get(i)/25;
			// add seconds corresponding to frames in map
			secondsMap.put(finalList.get(i)-1, sec);
			secondsMap.put(finalList.get(i), sec);
			ImageIcon icon;
			BufferedImage image = imageMap.get(finalList.get(i));
			icon = new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
			if (icon != null) {
				button[i] = new JButton(icon);
				button[i].addActionListener(new IconButtonHandler(i, icon));
				buttonPanel.add(button[i]);
			}
		}
		
		/*
		 * This for loop goes through the images in the database and stores them
		 * as icons and adds the images to JButtons and then to the JButton
		 * array
		 */
	}
	private class IconButtonHandler implements ActionListener {
		int pNo = 0;
		IconButtonHandler(int i, ImageIcon j) {
			pNo = i;
		}

		public void actionPerformed(ActionEvent e) {

	        File file = new File("src/main/java/20020924_juve_dk_02a_1.mp4");
				int startFrame = finalList.get(pNo);
				System.out.println("startFrame ====>>> "+pNo+" : "+startFrame);
				Duration startTime = new Duration(secondsMap.get(startFrame));
				int endFrame;
				Duration endTime;
				if(pNo < finalList.size()-1) {
					endFrame = finalList.get(pNo+1);
					endTime = new Duration(secondsMap.get(endFrame-1));
					// set start and end time of shots
					mediaPlayerComponent.getMediaPlayer().playMedia(file.getAbsolutePath(), ":start-time="+startTime, ":stop-time="+endTime); 
				}
				else {
					endTime = new Duration(939);
					// set start and end time of the last shot
					mediaPlayerComponent.getMediaPlayer().playMedia(file.getAbsolutePath(), ":start-time="+startTime, ":stop-time="+endTime); 
				}
				
		}
	}
	 private class VideoSurfacePanel extends JPanel {

	        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
			private VideoSurfacePanel() {
	            setBackground(Color.black);
	            setOpaque(true);
	            setPreferredSize(new Dimension(width, height));
	            setMinimumSize(new Dimension(width, height));
	            setMaximumSize(new Dimension(width, height));
	        }
	        protected void paintComponent(Graphics g) {
	            Graphics2D g2 = (Graphics2D)g;
	            g2.drawImage(image, null, 0, 0);
	        }
	    }

	    private class TutorialRenderCallbackAdapter extends RenderCallbackAdapter {

	        private TutorialRenderCallbackAdapter() {
	            super(new int[width * height]);
	        }
	        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
	            // Simply copy buffer to the image and repaint
	            image.setRGB(0, 0, width, height, rgbBuffer, 0, width);
	            videoSurface.repaint();
	        }
	    }
	    
	   // sort frames
	private static ArrayList<Integer> sortList(ArrayList<Integer> list) {
		int [] frames = new int [list.size()];
		for(int i = 0 ; i < list.size() ; i++) 
			frames[i] = list.get(i);
		Arrays.sort(frames);
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for(int j = 0 ; j < frames.length ; j++) 
			newList.add(frames[j]);
		return newList;
	}
	
	// read and grab frames from video using FFmpegFrameGrabber
	// convert frames to buffered images

	private static void readVideo() throws Exception, IOException {
		File video = new File("src/main/java/20020924_juve_dk_02a_1.mp4");
		String path = video.getAbsolutePath();
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(path);
		try {
            Java2DFrameConverter paintConverter = new Java2DFrameConverter();
            grabber.start();
            int frame_count = grabber.getLengthInFrames();
            System.out.println(frame_count/562.8);
            File file = new File("features.txt");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			double[][] histogram = new double[5000][26];
			for (int img = 0; img < 5000; img++) {
                Frame frame = grabber.grabImage();
                BufferedImage image = imageClone(paintConverter.getBufferedImage(frame));
                // add frames and buffered images to map
                imageMap.put(img, image);
				int intensity = 0;
				
				// calculate intensity and write histogram values to file
				for (int i = 0; i < image.getWidth(); i++) {
					for (int j = 0; j < image.getHeight(); j++) {
						int color = image.getRGB(i, j);
						int red = (color & 0x00ff0000) >> 16;
						int green = (color & 0x0000ff00) >> 8;
						int blue = color & 0x000000ff;
						intensity = (int) ((0.299 * red) + (0.587 * green) + (0.114 * blue));
						for (int his = 0; his < 25; his++) {
							if(intensity / 10 > 24) {
								histogram[img][24] = histogram[img][24] + 1;
							}
							else if (intensity / 10 == his) {
								histogram[img][his] = histogram[img][his] + 1;
							}
						}
					}
				}
			}		
			for(int i = 1000 ; i < 5000 ; i++) {
				for(int j = 0 ; j < 25 ; j++) {
					bw.write(String.valueOf(histogram[i][j]));
					bw.write(", ");
				}
				bw.write("\n");
			}
			bw.close();	
		}
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private static BufferedImage imageClone(BufferedImage bi) {
			 ColorModel cm = bi.getColorModel();
			 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			 WritableRaster raster = bi.copyData(null);
			 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	// Read histogram values from file and calculate distance, sd
	private static double[] readHistogram() throws IOException {
		double histogram[][] = new double[4000][25];
		Path path = Paths.get("features.txt");
	    Charset charset = Charset.forName("UTF-8");
	    try {
	      List<String> lines = Files.readAllLines(path, charset);
	      for(int i = 0 ; i < lines.size() ; i++) {
	    		  String[] line = lines.get(i).split(", ");
		    	  for(int j = 0 ; j < 25 ; j++) {
		    		  histogram[i][j] = Double.parseDouble(line[j]);
	    	  }
	      }
	    } 
	    catch (IOException e) {
	    	System.out.println(e);
	    }
	    double distance[] = getDistance(histogram);
	    return distance;
	}

	// calculate sd
	private static double[] getDistance(double [][] histogram) {
		double distance[] = new double[3999];
		for (int img = 0; img < 3999; img++) {
			for (int his = 0; his < 25; his++) {
				double x1 = histogram[img][his];
				double x2 = histogram[img+1][his];
				distance[img] += Math.abs(x1 - x2);
			}
		}
		return distance;
	}
	
	// calculate threshold
	private static void findThreshold(double[] distance) {
		double sum = 0.0;
		double avg = 0.0;
		double std = 0.0;
		double var;
		double stddev;
		for (int img = 0; img < 3999; img++) {
			sum = sum + distance[img];		
		}
		avg = sum / 3999;
		for (int img = 0; img < 3999; img++) {
			std = std + ((distance[img] - avg) * (distance[img] - avg));		
		}
		var = std / 3999;
		stddev = Math.sqrt(var);
		double tb = avg + (stddev * 11);
		double ts = avg * 2;
		System.out.println("TB is "+tb);
		System.out.println("TS is "+ts);
		findCut(distance, tb);
		findTransition(distance, tb, ts);
	}

	// calculate cuts
	public static void findCut(double[] distance, double tb) {
		cutMap = new HashMap<Integer, Integer>();
		for(int i = 0 ; i < 3999 ; i++) {
			if(distance[i] >= tb) {
				cutMap.put(i+1000, i+1000+1);
			}
		}
		for(int key : cutMap.keySet())
			frameList.add(cutMap.get(key));
	}

	// calculate gradual transition
	private static void findTransition(double[] distance, double tb, double ts) {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0 ; i < 3998 ; i++) {
			if(ts <= distance[i] && distance[i] < tb) { 
				if(!list.contains(i+1000))
					list.add(i+1000);
				if(ts <= distance[i+1] && !cutMap.containsKey(i+1+1000)) {
					if(!list.contains(i+1+1000))
						list.add(i+1+1000);
					if(ts <= distance[i+2] && !cutMap.containsKey(i+2+1000)) {
						if(!list.contains(i+2+1000))
							list.add(i+2+1000);
					}
				}
				if(ts >= distance[i+1]) {
					if(ts <= distance[i+2] && !cutMap.containsKey(i+2+1000)) {
						if(!list.contains(i+1+1000))
							list.add(i+1+1000);
						if(!list.contains(i+2+1000))
							list.add(i+2+1000);
					}
				}
			}	
		}
		int start = 0;
		int end = 0;
		while(start < list.size()-1) {
			if(list.get(start+1) - list.get(start) > 1) {
				start++;
				continue;
			}
			end = getEnd(list, start);
			map.put(list.get(start), list.get(end));
			start = end+1;
		}
		findSum(map, distance, tb);
	}

	// calculate end frame of shot
	private static int getEnd(ArrayList<Integer> list, int start) {
		int end = 0;
		while(start < list.size()-1) {
			if(list.get(start+1) - list.get(start) == 1) 
				start++;
			else
				break;
		}
		end = start;	
		return end;
	}
	
	// check if sum is greater than threshold
	private static void findSum(HashMap<Integer, Integer> map, double[] distance, double tb) {
		HashMap<Integer, Integer> newMap = new HashMap<Integer, Integer>();
		for(int key : map.keySet()) {
			int sum = 0;
			for(int i = key ; i <= map.get(key) ; i++) {
				sum += distance[i-1000];
			}
			if(sum >= tb) 
				newMap.put(key, map.get(key));
		}
		System.out.println(newMap);
		for(int key : newMap.keySet())
			frameList.add(key+1);
	}
}


