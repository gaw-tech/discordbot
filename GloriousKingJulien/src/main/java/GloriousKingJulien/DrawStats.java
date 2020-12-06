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
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class DrawStats {

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

	static int[] lastMinutesToArray(List<Integer> list, int minutes) {
		minutes++;
		int[] array = new int[minutes];
		Iterator<Integer> itr = list.iterator();
		if (list.size() < minutes) {
			array[0] = 1;
			return array;
		}
		for (int i = 0; i < list.size() - minutes; i++) {
			itr.next();
		}
		for (int i = 0; i < minutes; i++) {
			array[i] = itr.next().intValue();
		}
		return array;
	}

	public static void drawStats(String server) {
		List<Integer> values = toList(server + ".txt");
		int minutes = values.size() - 2;
		drawLastMinutesStats(server, minutes);
	}

	static void yAchse(int min, int max, int numberoflables, int width, int height, int border, Graphics2D g2d) {
		Font myFont = new Font("Courier New", 1, 32);
		g2d.setFont(myFont);

		g2d.setColor(Color.gray);

		for (int i = 0; i <= numberoflables; i++) {
			g2d.drawString("" + ((int) ((min + ((max * 1.0 - min) / numberoflables) * i) * 100)) / 100.0, border / 2,
					height - border - (int) (i * ((height * 1.0 - 2 * border) / numberoflables)));
			g2d.drawLine(border - 10, height - border - (int) (i * ((height * 1.0 - 2 * border) / numberoflables)),
					width - border, height - border - (int) (i * ((height * 1.0 - 2 * border) / numberoflables)));
		}
	}

	// x achse ohne zeit anpassung bzw glaubs nur stunden
	static void xAchse(int numberoflables, int width, int height, int border, Graphics2D g2d) {
		Font myFont = new Font("Courier New", 1, 32);
		g2d.setFont(myFont);

		g2d.setColor(Color.gray);

		Calendar now = Calendar.getInstance();

		for (int i = 0; i <= numberoflables; i++) {
			g2d.drawLine(width - border - (int) ((((width - 2 * border) * 1.0) / numberoflables) * i), border,
					width - border - (int) ((((width - 2 * border) * 1.0) / numberoflables) * i), height - border);
			g2d.drawString(
					(now.get(Calendar.HOUR_OF_DAY) + 2400 - i) % 24 + ":"
							+ ((now.get(Calendar.MINUTE) < 10) ? "0" + now.get(Calendar.MINUTE)
									: now.get(Calendar.MINUTE)),
					width - 50 - border - (int) ((((width - 2 * border) * 1.0) / numberoflables) * i),
					height - border + 50);
		}

	}

	// x achse mit zeit anpassung
	static void xAchse(int numberoflables, int width, int height, int border, Graphics2D g2d, int minutes) {
		minutes--; // weil bei der grossen draw wird min++ gemacht;
		Font myFont = new Font("Courier New", 1, 32);
		g2d.setFont(myFont);

		g2d.setColor(Color.gray);

		Calendar now = Calendar.getInstance();

		int hours;
		int mins;

		for (int i = 0; i <= numberoflables; i++) {
			g2d.drawLine(width - border - (int) ((((width - 2 * border) * 1.0) / numberoflables) * i), border,
					width - border - (int) ((((width - 2 * border) * 1.0) / numberoflables) * i), height - border);

			hours = ((int) ((now.get(Calendar.HOUR_OF_DAY) * 60.0 - (minutes * 1.0) / numberoflables * i) / 60)
					% 24 < 0) ? 24
							- (-(int) ((now.get(Calendar.HOUR_OF_DAY) * 60.0 - (minutes * 1.0) / numberoflables * i)
									/ 60)) % 24
							: (int) ((now.get(Calendar.HOUR_OF_DAY) * 60.0 - (minutes * 1.0) / numberoflables * i) / 60)
									% 24;
			mins = (int) ((-((minutes * 1.0) / numberoflables)) * i + now.get(Calendar.MINUTE) + 60000) % 60;
			g2d.drawString(hours + ":" + ((mins < 10) ? "0" + mins : mins),
					width - 50 - border - (int) ((((width - 2 * border) * 1.0) / numberoflables) * i),
					height - border + 50);
		}
	}

	public static void drawLastMinutesStats(String server, int minutes) {
		minutes++;
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

		List<Integer> valueslist = toList(server);
		int[] values = lastMinutesToArray(valueslist, minutes);
		drawSum(values, g2d, width, height, border);

		int size = values.length;
		int max = 0;
		double avg = 0;
		int total = 0;
		for (int i = 0; i < size; i++) {
			max = (max < values[i]) ? values[i] : max;
			avg += values[i];
		}

		total = (int) avg;
		avg = avg / size;

		// y achse beschriftung
		yAchse(0, max, 10, width, height, border, g2d);
		// x achse beschriftung
		xAchse(8, width, height, border, g2d, minutes);

		// linie
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(3));

		int x1 = border;
		int y1 = height - border - values[0] / max * (height - 2 * border);
		int x2;
		int y2;
		for (int i = 1; i < size; i++) {
			x2 = (int) (border + ((width - (2 * border)) / (double) (size - 1)) * i);
			y2 = (int) (height - border - (double) values[i] / max * (height - 2 * border));
			g2d.drawLine(x1, y1, x2, y2);
			g2d.drawLine(x1, y1 + 1, x2, y2 + 1);
			x1 = x2;
			y1 = y2;
		}

		Font myFont = new Font("Courier New", 1, 100);
		g2d.setFont(myFont);
		g2d.drawString("max msg/min: " + max + "  avg msg/min: " + (int) (avg * 100) / 100.0 + " total: " + total,
				border / 2, border / 2);

		File file = new File(server + ".jpg");
		try {
			ImageIO.write(image, "jpg", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("failed miserably");
		}
	}

	// summierter graph mit List input
	static void drawSum(List<Integer> list, Graphics2D g2d, int width, int height, int border) {
		int[] values = new int[list.size()];
		int size = list.size();
		values[0] = list.get(0).intValue();
		for (int i = 1; i < list.size(); i++) {
			values[i] += values[i - 1] + list.get(i).intValue();
		}
		int max = values[values.length - 1];

		int x1 = border;
		int y1 = height - border - values[0] / max * (height - 2 * border);
		int x2;
		int y2;
		for (int i = 1; i < size; i++) {
			x2 = (int) (border + ((width - (2 * border)) / (double) size) * i);
			y2 = (int) (height - border - (double) values[i] / max * (height - 2 * border));
			g2d.drawLine(x1, y1, x2, y2);
			g2d.drawLine(x1, y1 + 1, x2, y2 + 1);
			x1 = x2;
			y1 = y2;
		}
	}

	// summierter graph mit array input
	static void drawSum(int[] array, Graphics2D g2d, int width, int height, int border) {
		int size = array.length;
		int[] values = new int[size];
		values[0] = array[0];
		for (int i = 1; i < size; i++) {
			values[i] += values[i - 1] + array[i];
		}
		int max = values[values.length - 1];

		int x1 = border;
		int y1 = height - border - values[0] / max * (height - 2 * border);
		int x2;
		int y2;
		for (int i = 1; i < size; i++) {
			x2 = (int) (border + ((width - (2 * border)) / (double) size) * i);
			y2 = (int) (height - border - (double) values[i] / max * (height - 2 * border));
			g2d.drawLine(x1, y1, x2, y2);
			g2d.drawLine(x1, y1 + 1, x2, y2 + 1);
			x1 = x2;
			y1 = y2;
		}

		// draw beschriftung rechts
		Font myFont = new Font("Courier New", 1, 32);
		g2d.setFont(myFont);
		int numberoflables = 10;
		int min = 0;
		for (int i = 0; i <= numberoflables; i++) {
			g2d.drawString("" + (min + (max / numberoflables) * i), width - border + 10,
					height - border - (int) (i * ((height * 1.0 - 2 * border) / numberoflables)));
		}
	}

	public static void drawLastMinutesOnlineStats(String server, int minutes) {
		minutes++;
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

		List<Integer> valueslist = toList(server);
		int[] values = lastMinutesToArray(valueslist, minutes);

		int size = values.length;
		int max = 0;
		int min = 1000000;
		double avg = 0;
		for (int i = 0; i < size; i++) {
			max = (max < values[i]) ? values[i] : max;
			min = (min > values[i]) ? values[i] : min;
			avg += values[i];
		}
		avg = avg / size;

		System.out.println(max);

		// y achse beschriftung
		yAchse(min, max, 10, width, height, border, g2d);
		// x achse beschriftung
		xAchse(8, width, height, border, g2d, minutes);

		// linie
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(3));

		int x1 = border;
		int y1 = (int) (height - border - (1.0 * height - 2 * border) * ((1.0 * values[0] - min) / (max - min)));
		int x2;
		int y2;
		for (int i = 1; i < size; i++) {
			x2 = (int) (border + ((width - (2 * border)) / (double) (size - 1)) * i);
			y2 = (int) (height - border - (1.0 * height - 2 * border) * ((1.0 * values[i] - min) / (max - min)));
			g2d.drawLine(x1, y1, x2, y2);
			g2d.drawLine(x1, y1 + 1, x2, y2 + 1);
			x1 = x2;
			y1 = y2;
		}

		Font myFont = new Font("Courier New", 1, 100);
		g2d.setFont(myFont);
		g2d.drawString("max: " + max + " min: " + min + " avg: " + (int) (avg * 100) / 100.0, border / 2, border / 2);

		File file = new File(server + ".jpg");
		try {
			ImageIO.write(image, "jpg", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("failed miserably");
		}
	}

	public static void drawOnlineStats(String server) {
		List<Integer> values = toList(server + ".txt");
		int minutes = values.size() - 2;
		drawLastMinutesOnlineStats(server, minutes);
	}
}
