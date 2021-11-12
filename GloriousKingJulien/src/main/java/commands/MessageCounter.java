package commands;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import bot.Bot;
import bot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageCounter implements Module {
	String topname = "MessageCounter";
	MessageCounterListener mcl;
	JDA jda;
	private String dir = Bot.javaRoot + "/messagecounter";
	private boolean running = false;

	public MessageCounter() {
		mcl = new MessageCounterListener();
		jda = Bot.jda;
		jda.addEventListener(mcl);
		mcl.start();
		running = true;
	}

	@Override
	public void run_message(MessageReceivedEvent event) {
		if (!event.getAuthor().getId().equals(Bot.myID))
			return;
		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = message.getChannel();
		if (content.startsWith(prefix + topname.toLowerCase() + " ")) {
			content = content.substring(prefix.length() + topname.length() + 1);
			if (content.equals("start")) {
				if (running) {
					channel.sendMessage("Already running.").queue();
					return;
				}
				mcl = new MessageCounterListener();
				jda = event.getJDA();
				jda.addEventListener(mcl);
				mcl.start();
				running = true;
				channel.sendMessage("Let's go.").queue();
			}
			if (content.equals("stop")) {
				if (!running) {
					channel.sendMessage("Not running.").queue();
					return;
				}
				jda.removeEventListener(mcl);
				mcl.stop();
				channel.sendMessage("Let's end this.").queue();
			}
			// make image with all messages sent
			if (content.equals("img")) {
				try {
					String file = dir + "/" + event.getGuild().getId() + ".txt";
					int[] points = readFile(file);

					int max = 0;
					for (int i = 0; i < points.length; i++) {
						max = (max < points[i]) ? points[i] : max;
					}

					Drawer img = new Drawer(1600, 800);
					img.drawYLines(0, max, 8, true);
					img.drawLineGraph(points, max, new Color(255, 255, 255));
					img.drawXLinesTime(System.currentTimeMillis() - 60000 * points.length, System.currentTimeMillis(),
							10);
					channel.sendFile(img.getImage(dir + "/" + event.getGuild().getId() + ".png")).queue();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// make image with all messages sent but days are stacked
			if (content.equals("imgd")) {
				try {
					String file = dir + "/" + event.getGuild().getId() + ".txt";
					int[] points = readFile(file);

					int max = 0;
					for (int i = 0; i < points.length; i++) {
						max = (max < points[i]) ? points[i] : max;
					}

					int days = points.length / 1440;
					int[][] points_array = new int[days][];

					for (int i = 0; i < days; i++) {
						int[] tmp = new int[1440];
						for (int j = 0; j < 1440; j++) {
							tmp[j] = points[points.length % 1440 + i * 1440 + j];
						}
						points_array[i] = tmp;
					}

					Drawer img = new Drawer(1600, 800);
					img.drawYLines(0, max, 8, true);
					for (int i = 0; i < points_array.length; i++) {
						img.drawLineGraph(points_array[i], max,
								new Color(255, 255, 255, (int) (1.0 * 255 / points_array.length * (i + 1))));
					}
					img.drawXLinesTime(System.currentTimeMillis() - 60000 * 1440, System.currentTimeMillis(), 10);
					channel.sendFile(img.getImage(dir + "/" + event.getGuild().getId() + ".png")).queue();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// make image with online users
			if (content.equals("imgo")) {
				try {
					String file = Bot.javaRoot + "/onlinecounter/" + event.getGuild().getId() + ".txt";
					int[] points = readFile(file);

					int max = 0;
					for (int i = 0; i < points.length; i++) {
						max = (max < points[i]) ? points[i] : max;
					}

					Drawer img = new Drawer(1600, 800);
					img.drawYLines(0, max, 8, true);
					img.drawLineGraph(points, max, new Color(255, 255, 255));
					img.drawXLinesTime(System.currentTimeMillis() - 60000 * points.length, System.currentTimeMillis(),
							10);
					channel.sendFile(img.getImage(dir + "/" + event.getGuild().getId() + ".png")).queue();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// make image with online users but the days are stacked
			if (content.equals("imgod")) {
				try {
					String file = Bot.javaRoot + "/onlinecounter/" + event.getGuild().getId() + ".txt";
					int[] points = readFile(file);

					int max = 0;
					for (int i = 0; i < points.length; i++) {
						max = (max < points[i]) ? points[i] : max;
					}

					int days = points.length / 1440;
					int[][] points_array = new int[days][];

					for (int i = 0; i < days; i++) {
						int[] tmp = new int[1440];
						for (int j = 0; j < 1440; j++) {
							tmp[j] = points[points.length % 1440 + i * 1440 + j];
						}
						points_array[i] = tmp;
					}

					Drawer img = new Drawer(1600, 800);
					img.drawYLines(0, max, 8, true);
					for (int i = 0; i < points_array.length; i++) {
						img.drawLineGraph(points_array[i], max,
								new Color(255, 255, 255, (int) (1.0 * 255 / points_array.length * (i + 1))));
					}
					img.drawXLinesTime(System.currentTimeMillis() - 60000 * 1440, System.currentTimeMillis(), 10);
					channel.sendFile(img.getImage(dir + "/" + event.getGuild().getId() + ".png")).queue();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// scatter userwise message thingy
			if (content.equals("scat")) {
				String filedir = Bot.javaRoot + "/messagecounter/messagesbyuser.txt";
				File file = new File(filedir);
				try {
					Scanner scanner = new Scanner(file);
					ArrayList<String> id_list = new ArrayList<>();
					ArrayList<Integer> messages_list = new ArrayList<>();
					ArrayList<Integer> char_list = new ArrayList<>();
					while (scanner.hasNext()) {
						id_list.add(scanner.next());
						messages_list.add(scanner.nextInt());
						char_list.add(scanner.nextInt());
					}
					int len = id_list.size();
					String[] labels = new String[len];
					int[] x = new int[len];
					int[] y = new int[len];
					int max_x = Integer.MIN_VALUE;
					int max_y = Integer.MIN_VALUE;
					int min_x = Integer.MAX_VALUE;
					int min_y = Integer.MAX_VALUE;
					for (int i = 0; i < len; i++) {
						User user = event.getJDA().getUserById(id_list.get(i));
						labels[i] = (user != null) ? user.getName() : "null";
						int nrx = messages_list.get(i);
						int nry = char_list.get(i);
						max_x = (max_x < nrx) ? nrx : max_x;
						max_y = (max_y < nry) ? nry : max_y;
						min_x = (min_x > nrx) ? nrx : min_x;
						min_y = (min_y > nry) ? nry : min_y;
						x[i] = nrx;
						y[i] = nry;
					}
					Drawer img = new Drawer(1600, 800);
					img.drawYLinesLog(min_y, max_y, true);
					img.drawScatterLog(labels, x, y, min_x, min_y, max_x, max_y);
					img.drawXLinesLog(min_x, max_x, true);
					channel.sendFile(img.getImage(dir + "/" + event.getGuild().getId() + ".png")).queue();
					scanner.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			message.delete().queue();
		}
	}

	// reads from the files where the numbers are saved and returns the numbers as
	// an int array
	private int[] readFile(String dir) throws FileNotFoundException {
		File file = new File(dir);
		Scanner scanner;
		scanner = new Scanner(file);
		ArrayList<Integer> pointlist = new ArrayList<>();

		while (scanner.hasNext()) {
			pointlist.add(scanner.nextInt());
		}
		scanner.close();

		int[] points = new int[pointlist.size()];
		for (int i = 0; i < pointlist.size(); i++) {
			points[i] = pointlist.get(i);
		}

		return points;
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run_slash(SlashCommandEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run_button(ButtonClickEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean has_reaction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has_slash() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has_button() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has_basic_help() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EmbedBuilder get_help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> get_slash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field get_basic_help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		return short_commands;
	}

	@Override
	public void unload() {
		topname = null;
		if (mcl != null) {
			jda.removeEventListener(mcl);
			mcl.stop();
		}
	}

}

class MessageCounterListener extends ListenerAdapter {

	HashMap<String, Integer> counter = new HashMap<>();
	HashMap<String, Integer[]> usermessagescounter = new HashMap<>(); // userid. [messages charcount]
	ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	public void run() {
		// save messages
		String filedir = Bot.javaRoot + "/messagecounter";
		File root = new File(filedir);
		if (!root.exists()) {
			root.mkdir();
		}
		for (String guild : counter.keySet()) {
			try {
				FileWriter fr = new FileWriter(filedir + "/" + guild + ".txt", true);
				fr.append(counter.get(guild) + "\n");
				fr.close();
				counter.put(guild, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// read and save messages by member
		File mc = new File(filedir + "/messagesbyuser.txt");
		if (!mc.exists()) {
			try {
				mc.createNewFile();
			} catch (IOException e) {
				System.out.println("Tried to create messagesbyuser.txt file but failed.");
				e.printStackTrace();
			}
		}
		try {
			Scanner scanner = new Scanner(mc);
			HashMap<String, Integer[]> read = new HashMap<>();
			while (scanner.hasNext()) {
				String id = scanner.next();
				int msgcount = scanner.nextInt();
				int charcount = scanner.nextInt();
				Integer[] numbers = new Integer[2];
				numbers[0] = msgcount;
				numbers[1] = charcount;
				read.put(id, numbers);
			}
			for (String id : usermessagescounter.keySet()) {
				Integer[] numbers = new Integer[2];
				if (read.containsKey(id)) {
					numbers = read.get(id);
					Integer[] add = usermessagescounter.get(id);
					numbers[0] += add[0].intValue();
					numbers[1] += add[1];
				} else {
					numbers = usermessagescounter.get(id);
				}
				read.put(id, numbers);
			}
			usermessagescounter = new HashMap<>();
			FileWriter fr = new FileWriter(mc);
			for (String id : read.keySet()) {
				Integer[] numbers = read.get(id);
				fr.append(id + " " + numbers[0] + " " + numbers[1] + "\n");
			}
			scanner.close();
			fr.close();
		} catch (FileNotFoundException e1) {
			System.out.println("couldnt read and thus not save userwise message counts");
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error while writing userwise message counts");
			e.printStackTrace();
		}
		// save onlineusers
		filedir = Bot.javaRoot + "/onlinecounter";
		root = new File(filedir);
		if (!root.exists()) {
			root.mkdir();
		}
		JDA jda = Bot.jda;
		for (Guild g : jda.getGuilds()) {
			int counter = 0;
			for (Member m : g.getMembers()) {
				if (m.getOnlineStatus() != OnlineStatus.OFFLINE) {
					counter++;
				}
			}
			try {
				FileWriter fr = new FileWriter(filedir + "/" + g.getId() + ".txt", true);
				fr.append(counter + "\n");
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void start() {
		exec.scheduleAtFixedRate(new MessageCounterThread(this), 0, 1, TimeUnit.MINUTES);
	}

	public void stop() {
		exec.shutdownNow();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		String userid = event.getAuthor().getId();
		if (usermessagescounter.containsKey(userid)) {
			Integer[] numbers = usermessagescounter.get(userid);
			numbers[0] += 1;
			numbers[1] += event.getMessage().getContentRaw().length();
		} else {
			Integer[] numbers = new Integer[2];
			numbers[0] = 1;
			numbers[1] = event.getMessage().getContentRaw().length();
			usermessagescounter.put(userid, numbers);
		}
		String guildid = event.getGuild().getId();
		if (counter.containsKey(guildid)) {
			counter.put(guildid, counter.get(guildid) + 1);
		} else {
			counter.put(guildid, 1);
		}
	}
}

class MessageCounterThread implements Runnable {

	MessageCounterListener mcl;

	MessageCounterThread(MessageCounterListener mcl) {
		this.mcl = mcl;
	}

	@Override
	public void run() {
		mcl.run();
	}

}

class Drawer {
	private int width;
	private int height;
	private BufferedImage img_buf;

	Drawer(int width, int height) {
		this.width = width;
		this.height = height;
		img_buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		// maybe make this optional
		Graphics2D g2d = img_buf.createGraphics();
		g2d.setColor(new Color(54, 57, 63));
		g2d.fillRect(0, 0, width, height);
	}

	void drawLineGraph(int[] points, int max, Color line_color) {
		int height_offset = height / 10;
		int width_offset = width / 10;
		Graphics2D g2d = img_buf.createGraphics();
		g2d.setColor(line_color);

		double height_step = 1.0 * (height - 2 * height_offset) / max;
		double width_step = 1.0 * (width - 2 * width_offset) / (points.length - 1);

		for (int i = 1; i < points.length; i++) {
			g2d.drawLine((int) (width_offset + (i - 1) * width_step),
					(int) (height - height_offset - points[i - 1] * height_step), (int) (width_offset + i * width_step),
					(int) (height - height_offset - points[i] * height_step));
		}

	}

	void drawYLines(int min, int max, int n, boolean labeled) {
		int height_offset = height / 10;
		int width_offset = width / 10;

		Graphics2D g2d = img_buf.createGraphics();
		g2d.setColor(new Color(83, 88, 97));

		double height_step = (1.0 * height - 2 * height_offset) / n;
		for (int i = 1; i <= n; i++) {
			g2d.drawLine((int) (width_offset), (int) (height - height_offset - i * height_step),
					(int) (width - width_offset), (int) (height - height_offset - i * height_step));
		}
		if (labeled) {
			double labelstep = ((1.0 * max - min) / n);
			for (int i = 0; i <= n; i++) {
				g2d.drawString("" + (min + (int) (labelstep * i)), width_offset - (width_offset / 5),
						(int) (height - height_offset - height_step * i));
			}
		}
	}

	void drawYLinesLog(int min, int max, boolean labeled) {
		int height_offset = height / 10;
		int width_offset = width / 10;
		double height_step = (1.0 * height - 2 * height_offset) / (Math.log10(max - min));
		int start = min / 10 * 10;
		if (start == 0)
			start = 1;
		start = Math.abs(start);

		Graphics2D g2d = img_buf.createGraphics();
		g2d.setColor(new Color(83, 88, 97));

		while (start < max) {
			g2d.drawLine((int) (width_offset), (int) (height_offset + (Math.log10(start)) * height_step),
					(int) (width - width_offset), (int) (height_offset + (Math.log10(start)) * height_step));
			if (labeled)
				g2d.drawString("" + start, (int) (width_offset - width_offset / 5),
						(int) (height - height_offset - Math.log10(start) * height_step));
			start *= 10;
		}
	}

	void drawXLines(int min, int max, int n, boolean labeled) {
		int height_offset = height / 10;
		int width_offset = width / 10;
		double width_step = (1.0 * width - 2 * width_offset) / n;

		Graphics2D g2d = img_buf.createGraphics();
		g2d.setColor(new Color(83, 88, 97));

		for (int i = 1; i < n; i++) {
			g2d.drawLine((int) (width_offset + i * width_step), (int) (height_offset),
					(int) (width_offset + i * width_step), (int) (height - height_offset));
		}

		double labelstep = (1.0 * max - min) / n;
		if (labeled) {
			for (int i = 0; i <= n; i++) {
				g2d.drawString("" + ((int) (min + labelstep * (n - i))), (int) (width - width_offset - i * width_step),
						(int) (height - height_offset / 2));
			}
		}

	}

	void drawXLinesLog(int min, int max, boolean labeled) {
		int height_offset = height / 10;
		int width_offset = width / 10;
		double width_step = (1.0 * width - 2 * width_offset) / (Math.log10(max - min));
		int start = min / 10 * 10;
		if (start == 0)
			start = 1;
		start = Math.abs(start);

		Graphics2D g2d = img_buf.createGraphics();
		g2d.setColor(new Color(83, 88, 97));

		while (start < max) {
			g2d.drawLine((int) (width_offset + (Math.log10(start)) * width_step), (int) (height_offset),
					(int) (width_offset + (Math.log10(start)) * width_step), (int) (height - height_offset));
			if (labeled)
				g2d.drawString("" + start, (int) (width_offset + Math.log10(start) * width_step),
						(int) (height - height_offset / 2));
			start *= 10;
		}
	}

	void drawXLinesTime(long min, long max, int n) {
		int height_offset = height / 10;
		int width_offset = width / 10;
		double width_step = (1.0 * width - 2 * width_offset) / n;
		double time_step = (1.0 * max - min) / n;

		Graphics2D g2d = img_buf.createGraphics();
		g2d.setColor(new Color(83, 88, 97));

		for (int i = 1; i < n; i++) {
			g2d.drawLine((int) (width_offset + i * width_step), (int) (height_offset),
					(int) (width_offset + i * width_step), (int) (height - height_offset));
		}

		g2d.setColor(new Color(255, 255, 255));
		for (int i = 0; i <= n; i++) {
			DateTime dt = new DateTime().minusMillis((int) (time_step * i));
			g2d.drawString(dt.toString(DateTimeFormat.forPattern("HH:mm")),
					(int) (width - width_offset - i * width_step), (int) (height - height_offset / 2) + 10);
			g2d.drawString(dt.toString(DateTimeFormat.forPattern("d MMMM")),
					(int) (width - width_offset - i * width_step), (int) (height - height_offset / 2));
			g2d.drawString(dt.toString(DateTimeFormat.forPattern("yyyy")),
					(int) (width - width_offset - i * width_step), (int) (height - height_offset / 2) - 10);
		}
	}

	void drawScatter(String[] label, int[] x, int[] y, int min_x, int min_y, int max_x, int max_y) {
		int height_offset = height / 10;
		int width_offset = width / 10;
		double width_step = (1.0 * width - 2 * width_offset) / (max_x - min_x);
		double height_step = (1.0 * height - 2 * height_offset) / (max_y - min_y);
		Graphics2D g2d = img_buf.createGraphics();

		for (int i = 0; i < label.length; i++) {
			g2d.drawString(label[i], (int) (width_offset + (x[i] - min_x) * width_step),
					(int) (height - height_offset - (y[i] - min_y) * height_step));
		}
	}

	void drawScatterLog(String[] label, int[] x, int[] y, int min_x, int min_y, int max_x, int max_y) {
		int height_offset = height / 10;
		int width_offset = width / 10;
		double width_step = (1.0 * width - 2 * width_offset) / (Math.log10(max_x - min_x));
		double height_step = (1.0 * height - 2 * height_offset) / (Math.log10(max_y - min_y));
		Graphics2D g2d = img_buf.createGraphics();

		for (int i = 0; i < label.length; i++) {
			g2d.drawString(label[i], (int) (width_offset + Math.log10(x[i] - min_x) * width_step),
					(int) (height - height_offset - Math.log10(y[i] - min_y) * height_step));
		}
	}

	File getImage(String dir) {
		File file = new File(dir);
		try {
			ImageIO.write(img_buf, "png", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("failed miserably");
		}
		return file;
	}
}