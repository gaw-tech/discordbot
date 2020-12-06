package GloriousKingJulien;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class DrawStats {

	public static void draw(String server) {
		int width = 800;
		int height = 300;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, width, height);
		g2d.setColor(Color.black);

		File file = new File(server + ".jpg");
		try {
			ImageIO.write(image, "jpg", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("failed miserably");
		}
	}

	static List<Integer> toList(String filename) {
		List<Integer> values = new ArrayList<Integer>();
		try {
			Scanner file = new Scanner(new File(filename));
			file.nextLine();
			while (file.hasNextInt()) {
				values.add(file.nextInt());
			}	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("bad file");
		}

		return values;
	}

	public static void drawStats(String server) {
		server = server + ".txt";
		int width = 4000;
		int height = 1500;
		int border = 200;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, width, height);

		// y achse linie
		g2d.setColor(Color.gray);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine(border, border, border, height - border);

		List<Integer> values = toList(server);
		int size = values.size();
		int max = 0;
		double avg = 0;
		int total = 0;
		for (int i = 0; i < size; i++) {
			max = (max < values.get(i).intValue()) ? values.get(i).intValue() : max;
			avg += values.get(i).intValue();
		}

		total = (int) avg;
		avg = avg / size;

		// y achse beschriftung
		yAchse(0, max, 10, width, height, border, g2d);

		// linie
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(3));

		int x1 = border;
		int y1 = height - border - values.get(0).intValue() / max * (height - 2 * border);
		int x2;
		int y2;
		for (int i = 1; i < values.size(); i++) {
			x2 = (int) (border + ((width - (2 * border)) / (double) size) * i);
			y2 = (int) (height - border - (double) values.get(i).intValue() / max * (height - 2 * border));
			g2d.drawLine(x1, y1, x2, y2);
			g2d.drawLine(x1, y1 + 1, x2, y2 + 1);
			x1 = x2;
			y1 = y2;
		}

		Font myFont = new Font("Courier New", 1, 100);
		g2d.setFont(myFont);
		g2d.drawString("max msg/min: " + max + "  avg msg/min: " + (int) (avg * 100) / 100.0 + " total: " + total, border / 2, border / 2);

		File file = new File(server + ".jpg");
		try {
			ImageIO.write(image, "jpg", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("failed miserably");
		}
	}

	static void yAchse(int min, int max, int numberoflables, int width, int height, int border, Graphics2D g2d) {
		Font myFont = new Font("Courier New", 1, 32);
		g2d.setFont(myFont);

		g2d.setColor(Color.gray);

		for (int i = 0; i <= numberoflables; i++) {
			g2d.drawString("" + ((int) ((min + ((max * 1.0) / numberoflables) * i) * 100)) / 100.0, border / 2,
					height - border - (int) (i * ((height * 1.0 - 2 * border) / numberoflables)));
			g2d.drawLine(border - 10, height - border - (int) (i * ((height * 1.0 - 2 * border) / numberoflables)),
					width - border, height - border - (int) (i * ((height * 1.0 - 2 * border) / numberoflables)));
		}
	}
}
