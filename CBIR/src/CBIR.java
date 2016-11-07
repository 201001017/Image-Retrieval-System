import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.*;

public class CBIR extends JFrame {

	private JLabel photographLabel = new JLabel(); // container to hold a large
	private JButton[] button; // creates an array of JButtons

	private int[] buttonOrder = new int[101]; // creates an array to keep up
												// with the image order
	private GridLayout gridLayout1;
	private GridLayout gridLayout2;
	private GridLayout gridLayout3;
	private GridLayout gridLayout4;
	private GridLayout gridLayout5;
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelTop;
	private JPanel buttonPanel;
	private JPanel buttonPanelTop;
	private JPanel buttonPanelBot;
	private Map<Integer, Double> map;
	int picNo = 0;
	int imageCount = 0; // keeps up with the number of images displayed since
						// the first page.
	int pageNo = 1;
	int histogram[][] = new int[101][26];
	int colorHistogram[][] = new int[101][65];
	double features[][] = new double[101][90];
	double normalizedFeatures[][] = new double[101][90];
	JCheckBox relevance = new JCheckBox("Relevance Feedback");
	List<Integer> list = new ArrayList<>();

	public static void main(String args[]) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CBIR app = new CBIR();
				app.setVisible(true);
			}
		});
	}

	public CBIR() {
		// The following lines set up the interface including the layout of the
		// buttons and JPanels.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("CONTENT-BASED IMAGE RETRIEVAL: Please select an Image");
		panelBottom1 = new JPanel();
		panelBottom2 = new JPanel();
		panelTop = new JPanel();
		buttonPanel = new JPanel();
		buttonPanelTop = new JPanel();
		buttonPanelBot = new JPanel();
		gridLayout1 = new GridLayout(4, 5, 5, 5);
		gridLayout2 = new GridLayout(2, 1, 5, 5);
		gridLayout3 = new GridLayout(1, 2, 5, 5);
		gridLayout4 = new GridLayout(2, 1, 5, 5);
		gridLayout5 = new GridLayout(4, 1, 5, 5);
		setLayout(gridLayout2);
		panelBottom1.setLayout(gridLayout1);
		panelBottom2.setLayout(gridLayout1);
		panelTop.setLayout(gridLayout3);
		add(panelTop);
		add(panelBottom1);
		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		buttonPanel.setLayout(gridLayout4);
		buttonPanelTop.setLayout(gridLayout3);
		buttonPanelBot.setLayout(gridLayout5);
		buttonPanel.add(buttonPanelTop);
		buttonPanel.add(buttonPanelBot);
		panelTop.add(photographLabel);
		panelTop.add(buttonPanel);
		JButton previousPage = new JButton("Back");
		JButton nextPage = new JButton("Next");
		JButton intensity = new JButton("Retrieve by Intensity Method");
		JButton colorCode = new JButton("Retrieve by Color-Code Method");
		JButton intensityAndColor = new JButton("Retrieve by Intensity and Color-Code Method");
	//	JCheckBox relevance = new JCheckBox("Relevance Feedback");
		JButton reset = new JButton("Reset");
		buttonPanelTop.add(previousPage);
		buttonPanelTop.add(nextPage);
		buttonPanelBot.add(intensity);
		buttonPanelBot.add(colorCode);
		buttonPanelBot.add(intensityAndColor);
		buttonPanelBot.add(relevance);
		buttonPanelBot.add(reset);

		nextPage.addActionListener(new nextPageHandler());
		previousPage.addActionListener(new previousPageHandler());
		intensity.addActionListener(new intensityHandler());
		colorCode.addActionListener(new colorCodeHandler());
		intensityAndColor.addActionListener(new intensityAndColorHandler());
		relevance.addActionListener(new relevanceHandler());
		reset.addActionListener(new resetHandler());
		setSize(1100, 750);
		// this centers the frame on the screen
		setLocationRelativeTo(null);

		button = new JButton[101];

		/*
		 * This for loop goes through the images in the database and stores them
		 * as icons and adds the images to JButtons and then to the JButton
		 * array
		 */
		for (int i = 1; i < 101; i++) {
			ImageIcon icon;
			icon = new ImageIcon(new ImageIcon(getClass().getResource("images/" + i + ".jpg")).getImage()
					.getScaledInstance(100, 100, Image.SCALE_DEFAULT));

			if (icon != null) {
				button[i] = new JButton(icon);
				button[i].addActionListener(new IconButtonHandler(i, icon));
				buttonOrder[i] = i;

			}
		}

		displayFirstPage();
	}


	private void displayFirstPage() {
		int imageButNo = 0;
		imageCount = 1;
		panelBottom1.removeAll();

		for (int i = 1; i < 21; i++) {
			imageButNo = buttonOrder[i];
			panelBottom1.add(button[imageButNo]);
			imageCount++;
		}
		panelBottom1.revalidate();
		panelBottom1.repaint();

	}

	private void displayFirstPage(int a) {

		int imageButNo = 0;
		imageCount = 1;
		panelBottom1.removeAll();
		for (int i = 1; i < 21; i++) {
			imageButNo = buttonOrder[i];

			JCheckBox check = new JCheckBox(new checkBox(imageButNo));
			if (list.contains(imageButNo)) {
				check.setSelected(true);
				check.setEnabled(false);
			}
			panelBottom1.add(button[imageButNo]);
			panelBottom1.add(check);
			imageCount++;
		}
		panelBottom1.revalidate();
		panelBottom1.repaint();

	}

	private class checkBox extends AbstractAction {
		int imgNo = 0;

		public checkBox(int imageButNo) {
			imgNo = imageButNo;
		}

		public void actionPerformed(ActionEvent e) {
			list.add(imgNo);
			System.out.println(list.toString());
			JCheckBox check = (JCheckBox) e.getSource();
			check.setSelected(true);
		}

	}

	/*
	 * This class implements an ActionListener for each iconButton. When an icon
	 * button is clicked, the image on the the button is added to the
	 * photographLabel and the picNo is set to the image number selected and
	 * being displayed.
	 */
	private class IconButtonHandler implements ActionListener {
		int pNo = 0;
		ImageIcon iconUsed;

		IconButtonHandler(int i, ImageIcon j) {
			pNo = i;
			iconUsed = j; // sets the icon to the one used in the button
		}

		public void actionPerformed(ActionEvent e) {
			// photographLabel.setIcon(iconUsed);
			photographLabel.setIcon(new ImageIcon(CBIR.class.getResource("images/" + pNo + ".jpg")));
			picNo = pNo;
		}

	}

	private class resetHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			picNo = 0;
			photographLabel.setIcon(null);
			imageCount = 1;
			if (!list.isEmpty())
				list.clear();
			for (int i = 1; i < 101; i++) {
				ImageIcon icon;
				icon = new ImageIcon(new ImageIcon(getClass().getResource("images/" + i + ".jpg")).getImage()
						.getScaledInstance(100, 100, Image.SCALE_DEFAULT));

				if (icon != null) {
					button[i] = new JButton(icon);
					button[i].addActionListener(new IconButtonHandler(i, icon));
					buttonOrder[i] = i;

				}
				relevance.setSelected(false);
			}
			displayFirstPage();

		}

	}

	private class relevanceHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (picNo != 0)
				displayFirstPage(1);

		}
	}

	private class intensityAndColorHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (picNo != 0) {
				if (list.isEmpty()) // before checking relevance check box
				{
					System.out.println("picNo" + picNo);
					createNormalizedFeatureMatrix();
					double[] distance = new double[101];
					double difference[] = new double[101];
					map = new HashMap<Integer, Double>();
					int count = 1;
					for (int img = 1; img <= 100; img++) {
						for (int his = 1; his <= 89; his++) {

							double x = normalizedFeatures[picNo][his];
							double y = normalizedFeatures[img][his];
							difference[img] = Math.abs(normalizedFeatures[picNo][his] - normalizedFeatures[img][his]);
							distance[img] += 0.01123595505 * difference[img];
						}
						map.put(img, distance[img]);

					}

					Set set = map.entrySet();
					Iterator iterator = set.iterator();
					while (iterator.hasNext()) {
						Map.Entry me = (Map.Entry) iterator.next();
						// System.out.print(me.getKey() + ": ");
						// System.out.println(me.getValue());
					}
					Map<Integer, Double> map1 = sortByValues(map);
					System.out.println("After Sorting:");
					Set set2 = map1.entrySet();
					Iterator iterator2 = set2.iterator();
					while (iterator2.hasNext()) {
						Map.Entry me2 = (Map.Entry) iterator2.next();
						// System.out.print(me2.getKey() + ": ");
						// System.out.println(me2.getValue());
						buttonOrder[count] = (int) me2.getKey();
						count++;
					}
					displayFirstPage();
				} else // When relevance check box is checked
				{

					double sum1 = 0.0;
					System.out.println("ITERATION 2");
					System.out.println("picNo" + picNo);
					double avgRel[] = new double[90];
					double stdRel[] = new double[90];
					double stddevRel[] = new double[90];
					double weight[] = new double[90];
					double normalizedWeight[] = new double[90];
					double[][] relevanceMatrix = createNormalizedFeatureMatrix(list);
					double sum = 0.0;
					for (int his = 1; his <= 89; his++) {
						for (int img = 0; img < list.size(); img++) {
							avgRel[his] = avgRel[his] + relevanceMatrix[img][his];
						}
						avgRel[his] = avgRel[his] / list.size();
						System.out.println("avgRel" + avgRel[his]);
						for (int img = 0; img < list.size(); img++) {
							stdRel[his] = stdRel[his] + (Math.pow((relevanceMatrix[img][his] - avgRel[his]), 2));
						}
						stdRel[his] = stdRel[his] / (list.size() - 1);
						stddevRel[his] = Math.sqrt(stdRel[his]);
						System.out.println("stddevRel" + stddevRel[his]);
						if (stddevRel[his] == 0.0) {
							if (avgRel[his] == 0.0) {
								weight[his] = 0.0;
							} else {
								double min = getMinStdDev(relevanceMatrix, list);
								weight[his] = 1.0 / min;
							}
						} else {
							weight[his] = 1.0 / stddevRel[his];
						}
						sum += weight[his];
						// System.out.println("sum" + sum);
					}
					for (int his = 1; his <= 89; his++) {

						normalizedWeight[his] = weight[his] / sum;
						// System.out.println("normalizedWeight"+his+"
						// :"+normalizedWeight[his]);
						sum1 = sum1 + normalizedWeight[his];

						// System.out.println("Sum of Normalized weights"+sum1);
					}
					createNormalizedFeatureMatrix();
					double[] distance = new double[101];
					double difference[] = new double[101];
					map = new HashMap<Integer, Double>();
					int count = 1;
					for (int img = 1; img <= 100; img++) {
						for (int his = 1; his <= 89; his++) {

							double x = normalizedFeatures[picNo][his];
							double y = normalizedFeatures[img][his];
							difference[img] = Math.abs(normalizedFeatures[picNo][his] - normalizedFeatures[img][his]);
							distance[img] += normalizedWeight[his] * difference[img];
						}
						map.put(img, distance[img]);

					}

					Set set = map.entrySet();
					Iterator iterator = set.iterator();
					while (iterator.hasNext()) {
						Map.Entry me = (Map.Entry) iterator.next();
						System.out.print(me.getKey() + ": ");
						System.out.println(me.getValue());
					}
					Map<Integer, Double> map1 = sortByValues(map);
					System.out.println("After Sorting:");
					Set set2 = map1.entrySet();
					Iterator iterator2 = set2.iterator();
					while (iterator2.hasNext()) {
						Map.Entry me2 = (Map.Entry) iterator2.next();
						System.out.print(me2.getKey() + ": ");
						System.out.println(me2.getValue());
						buttonOrder[count] = (int) me2.getKey();
						count++;
					}
					displayFirstPage(1);
				}
			}
		}

	}

	private double getMinStdDev(double[][] relevanceMatrix, List<Integer> list) {
		double average[] = new double[90];
		double stdDevn[] = new double[90];
		double stdDevnRel[] = new double[90];
		System.out.println("INSIDE FIND MINIMUM");
		double mininumStd = Double.MAX_VALUE;
		for (int his = 1; his <= 89; his++) {
			for (int img = 0; img < list.size(); img++) {
				average[his] = average[his] + relevanceMatrix[img][his];
			}
			average[his] = average[his] / list.size();
			for (int img = 0; img < list.size(); img++) {
				stdDevn[his] = stdDevn[his] + (Math.pow((relevanceMatrix[img][his] - average[his]), 2));
			}
			stdDevn[his] = stdDevn[his] / (list.size() - 1);
			stdDevnRel[his] = Math.sqrt(stdDevn[his]);
			if (stdDevnRel[his] != 0)
				mininumStd = Math.min(mininumStd, stdDevnRel[his]);
		}
		System.out.println("MINIMUM VALUE OF STD IS" + mininumStd * 0.5);
		return mininumStd * 0.5;
	}

	public void createFeatureMatrix() {
		createHistogram();
		getColorHistogram();
		for (int img = 1; img <= 100; img++) {
			int size = histogram[img][0];
			for (int his = 1; his <= 25; his++) {
				features[img][his] = (double) histogram[img][his] / size;

			}
			for (int his = 1; his <= 64; his++) {
				features[img][his + 25] = (double) colorHistogram[img][his] / size;

			}
		}
	}

	public void createNormalizedFeatureMatrix() {
		createFeatureMatrix();
		double avg[] = new double[90];
		double std[] = new double[90];
		double stddev[] = new double[90];
		for (int his = 1; his <= 89; his++) {
			for (int img = 1; img <= 100; img++) {
				avg[his] = avg[his] + features[img][his];
			}
			avg[his] = avg[his] / 100;
			for (int img = 1; img <= 100; img++) {
				std[his] = std[his] + (Math.pow((features[img][his] - avg[his]), 2));
			}
			std[his] = std[his] / 99;
			stddev[his] = Math.sqrt(std[his]);
		}
		for (int img = 1; img <= 100; img++) {
			for (int his = 1; his <= 89; his++) {
				if (stddev[his] != 0.0)
					normalizedFeatures[img][his] = (features[img][his] - avg[his]) / stddev[his];
			}
		}
	}

	double[][] createNormalizedFeatureMatrix(List<Integer> list) {
		double relevanceMatrix[][] = new double[101][90];
		for (int img = 0; img < list.size(); img++) {
			for (int his = 1; his <= 89; his++) {
				relevanceMatrix[img][his] = normalizedFeatures[list.get(img)][his];
			}
		}
		return relevanceMatrix;
	}

	/*
	 * This class implements an ActionListener for the nextPageButton. The last
	 * image number to be displayed is set to the current image count plus 20.
	 * If the endImage number equals 101, then the next page button does not
	 * display any new images because there are only 100 images to be displayed.
	 * The first picture on the next page is the image located in the
	 * buttonOrder array at the imageCount
	 */
	private class nextPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int imageButNo = 0;
			int endImage = imageCount + 20;
			if (endImage <= 101) {
				panelBottom1.removeAll();
				for (int i = imageCount; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					panelBottom1.add(button[imageButNo]);
					imageCount++;

				}
				relevance.setSelected(false);
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}

	}

	/*
	 * This class implements an ActionListener for the previousPageButton. The
	 * last image number to be displayed is set to the current image count minus
	 * 40. If the endImage number is less than 1, then the previous page button
	 * does not display any new images because the starting image is 1. The
	 * first picture on the next page is the image located in the buttonOrder
	 * array at the imageCount
	 */
	private class previousPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int imageButNo = 0;
			int startImage = imageCount - 40;
			int endImage = imageCount - 20;
			if (startImage >= 1) {
				panelBottom1.removeAll();
				/*
				 * The for loop goes through the buttonOrder array starting with
				 * the startImage value and retrieves the image at that place
				 * and then adds the button to the panelBottom1.
				 */
				for (int i = startImage; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					JCheckBox check = new JCheckBox(new checkBox(imageButNo));
					if (list.contains(imageButNo)) {
						check.setSelected(true);
						check.setEnabled(false);

					}
					else
					{
						check.hide();
					}
					panelBottom1.add(button[imageButNo]);

					panelBottom1.add(check);
					imageCount--;

				}
				relevance.setSelected(false);
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}

	}

	/*
	 * This class implements an ActionListener when the user selects the
	 * intensityHandler button. The image number that the user would like to
	 * find similar images for is stored in the variable pic. pic takes the
	 * image number associated with the image selected and subtracts one to
	 * account for the fact that the intensityMatrix starts with zero and not
	 * one. The size of the image is retrieved from the imageSize array. The
	 * selected image's intensity bin values are compared to all the other
	 * image's intensity bin values and a score is determined for how well the
	 * images compare. The images are then arranged from most similar to the
	 * least.
	 */
	private class intensityHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (picNo != 0) {
				double[] distance = new double[101];
				for (int ini = 1; ini < 101; ini++) {
					distance[ini] = 0;
				}
				map = new HashMap<Integer, Double>();

				int count = 0;
				System.out.println("when intensity button is pressed");
				createHistogram();

				for (int img = 1; img <= 100; img++) {
					for (int his = 1; his <= 25; his++) {
						double x = histogram[picNo][his];
						double y = histogram[picNo][0];
						double x1 = histogram[img][his];
						double y1 = histogram[img][0];
						distance[img] += Math.abs((x / y) - (x1 / y1));
					}
					map.put(img, distance[img]);

				}

				Set set = map.entrySet();
				Iterator iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry me = (Map.Entry) iterator.next();
					System.out.print(me.getKey() + ": ");
					System.out.println(me.getValue());
				}
				Map<Integer, Double> map1 = sortByValues(map);
				System.out.println("After Sorting:");
				Set set2 = map1.entrySet();
				Iterator iterator2 = set2.iterator();
				while (iterator2.hasNext()) {
					Map.Entry me2 = (Map.Entry) iterator2.next();
					System.out.print(me2.getKey() + ": ");
					System.out.println(me2.getValue());
					buttonOrder[count] = (int) me2.getKey();

					count++;
				}
				displayFirstPage();
			}
		}

	}

	// sorting the hashmap
	private static HashMap sortByValues(Map<Integer, Double> map2) {
		List list = new LinkedList(map2.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;

	};

	// create histogram for intensityHandler
	public void createHistogram() {
		try {
			for (int img = 1; img <= 100; img++) {
				BufferedImage image = ImageIO.read(new File("images/" + img + ".jpg"));
				int intensity = 0;
				for (int i = 0; i < image.getWidth(); i++) {
					for (int j = 0; j < image.getHeight(); j++) {
						int color = image.getRGB(i, j);
						int red = (color & 0x00ff0000) >> 16;
						int green = (color & 0x0000ff00) >> 8;
						int blue = color & 0x000000ff;
						intensity = (int) ((0.299 * red) + (0.587 * green) + (0.114 * blue));
						histogram[img][0] = image.getWidth() * image.getHeight();
						for (int his = 1; his <= 25; his++) {
							if (intensity / 10 == his - 1) {
								histogram[img][his] = histogram[img][his] + 1;
							}
						}
					}

				}

			}

		} catch (IOException e1) {
			System.out.println(e1);
		}

	}

	/*
	 * This class implements an ActionListener when the user selects the
	 * colorCode button. The image number that the user would like to find
	 * similar images for is stored in the variable pic. pic takes the image
	 * number associated with the image selected and subtracts one to account
	 * for the fact that the intensityMatrix starts with zero and not one. The
	 * size of the image is retrieved from the imageSize array. The selected
	 * image's intensity bin values are compared to all the other image's
	 * intensity bin values and a score is determined for how well the images
	 * compare. The images are then arranged from most similar to the least.
	 */
	private class colorCodeHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (picNo != 0) {
				double[] distance = new double[101];
				for (int ini = 1; ini < 101; ini++) {
					distance[ini] = 0;
				}
				int count = 1;
				map = new HashMap<Integer, Double>();
				getColorHistogram();
				for (int img = 1; img <= 100; img++) {

					for (int his = 1; his <= 64; his++) {
						double x = colorHistogram[picNo][his];
						double y = colorHistogram[picNo][0];
						double x1 = colorHistogram[img][his];
						double y1 = colorHistogram[img][0];
						distance[img] += Math.abs((x / y) - (x1 / y1));
					}
					map.put(img, distance[img]);
					System.out.println("Image" + img + ": " + distance[img]);

				}
				Set set = map.entrySet();
				Iterator iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry me = (Map.Entry) iterator.next();
					System.out.print(me.getKey() + ": ");
					System.out.println(me.getValue());
				}
				Map<Integer, Double> map1 = sortByValues(map);
				System.out.println("After Sorting:");
				Set set2 = map1.entrySet();
				Iterator iterator2 = set2.iterator();
				while (iterator2.hasNext()) {
					Map.Entry me2 = (Map.Entry) iterator2.next();
					System.out.print(me2.getKey() + ": ");
					System.out.println(me2.getValue());
					buttonOrder[count] = (int) me2.getKey();

					count++;
				}
				displayFirstPage();
			}

		}

	}

	// create histogram for colorCodeHandler
	public void getColorHistogram() {
		int colorCodeNo = 0;
		try {
			for (int img = 1; img <= 100; img++) {
				BufferedImage image = ImageIO.read(new File("images/" + img + ".jpg"));
				for (int i = 0; i < image.getWidth(); i++) {
					for (int j = 0; j < image.getHeight(); j++) {
						Color myColor = new Color(image.getRGB(i, j));
						// Retrieving first two bits from RGB and Concatenating
						// with "|"
						int colorCode = ((myColor.getRed() >> 6) << 4)
								| (((myColor.getGreen() >> 6) << 2) | (myColor.getBlue() >> 6));
						colorHistogram[img][0] = image.getWidth() * image.getHeight();
						for (int his = 1; his <= 64; his++) {
							if (colorCode == his - 1) {
								colorHistogram[img][his] = colorHistogram[img][his] + 1;
							}

						}
					}

				}

			}
		} catch (IOException e1) {
			System.out.println(e1);
		}
	}

	// Convert int to binary value
	public String getColorCode(int color) {
		String color1 = Integer.toBinaryString(color);
		int length = color1.length();
		StringBuilder buf = new StringBuilder();
		if (length < 8) {
			while (8 - length > 0) {
				buf.append("0");// add zero until length =8
				length++;
			}
		}
		String colorb = buf.toString() + color1;// binary string with leading

		return colorb;
	}

	// Convert binary to int value
	public int binaryToInteger(String binary) {
		char[] numbers = binary.toCharArray();
		int result = 0;
		for (int i = numbers.length - 1; i >= 0; i--)
			if (numbers[i] == '1')
				result += Math.pow(2, (numbers.length - i - 1));
		return result;
	}

}
