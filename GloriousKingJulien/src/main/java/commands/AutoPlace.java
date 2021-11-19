package commands;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;

import bot.Bot;
import bot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoPlace extends ListenerAdapter implements Module {
	volatile static boolean running = false;
	static MessageChannel channel;
	static int canvaspointer = 0;
	public static int n = 200;
	public static int m = 1000 / n;
	static EmoteStorage es = new EmoteStorage();
	public static LinkedList<String> memes = new LinkedList<>();
	ListenerAdapter la;

	static Thread pixelThread;

	public AutoPlace() {
		bot.Bot.jda.addEventListener(this);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		Message message = event.getMessage();
		List<Emote> emotes = message.getEmotes();
		if (emotes != null && !emotes.isEmpty()) {
			for (Emote e : emotes) {
				String url = e.getImageUrl();
				if (url.contains("png") && !e.isAnimated()) {
					es.addEmote(e);
				}
			}
		}
	}

	@Override
	public void run_message(MessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = event.getChannel();

		if (content.startsWith(prefix + "autoplace emote ")) {
			try {
				URL url = new URL(message.getEmotes().get(0).getImageUrl());
				InputStream in = new BufferedInputStream(url.openStream());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int n = 0;
				while (-1 != (n = in.read(buf))) {
					out.write(buf, 0, n);
				}
				out.close();
				in.close();
				byte[] response = out.toByteArray();
				File png = new File("emote.png");
				FileOutputStream fos = new FileOutputStream(png);
				fos.write(response);
				fos.close();

				WritableRaster image = normalizeRaster(ImageIO.read(png).getData());

				ColorModel cm = ImageIO.read(png).getColorModel();
				BufferedImage newimage = new BufferedImage(cm, image, cm.isAlphaPremultiplied(), null);
				File new_file = new File("new_file.png");
				ImageIO.write(newimage, "png", new_file);
				channel.sendFile(new_file).queue();
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
			}
		}

		if (content.equals(prefix + "autoplace stop")) {
			running = false;
			channel.sendMessage("lessgoon't").queue();
		}

		if (content.equals(prefix + "autoplace start")) {
			AutoPlace.channel = channel;
			running = true;
			channel.sendMessage("lessgoo").queue();
			pixelThread.start();
		}

		if (content.startsWith(prefix + "autoplace setc ")) {
			content = content.substring(prefix.length() + 15);
			canvaspointer = Integer.parseInt(content);
			message.delete().queueAfter(5, TimeUnit.SECONDS);
		}

		if (content.equals(prefix + "autoplace inp")) {
			message.delete().queueAfter(5, TimeUnit.SECONDS);
			channel.sendMessage("" + es.in_pointer).queue(msg -> {
				msg.delete().queueAfter(5, TimeUnit.SECONDS);
			});
		}

		if (content.startsWith(prefix + "autoplace meme ")) {
			message.delete().queue();
			channel.sendMessage("less go? :haHaa:").queue();
			AutoPlace.channel = channel;
			MessageHistory mh = message.getMentionedChannels().get(0).getHistoryBefore(message.getIdLong(), 100)
					.complete();
			List<Message> ml = mh.getRetrievedHistory();
			for (Message m : ml) {
				List<Attachment> al = m.getAttachments();
				if (al != null && !al.isEmpty()) {
					Attachment atch = m.getAttachments().get(0);
					if (atch.isImage() && atch.getFileExtension().equals("png")) {
						memes.add(atch.getUrl());
						System.out.println("got maymay");
					}
				}
			}
			running = true;
			LinkedList<String> lines = new LinkedList<>();
			for (String link : memes) {
				lines.addAll(linkToString(link));
			}
			pixelThread = new Thread(new PlaceRunnable(lines, channel));
			pixelThread.start();
		}

		if (content.startsWith(prefix + "autoplace gray ")) {
			LinkedList<String> lines = new LinkedList<>();
			content = content.substring(prefix.length() + 15);
			for (int i = Integer.parseInt(content); i < 1000; i++) {
				for (int j = 0; j < 1000; j++) {
					lines.add(".place setpixel " + i + " " + j + " #36393f");
				}
			}
			running = true;
			pixelThread = new Thread(new PlaceRunnable(lines, channel));
			pixelThread.start();

		}

		if (content.startsWith(prefix + "autoplace andri")) {
			if (la != null && running) {
				Bot.jda.removeEventListener(la);
				running = false;
				Bot.jda.cancelRequests();
				la = null;
				channel.sendMessage("lessgo").queue();
			} else {
				la = new APLA();
				Bot.jda.addEventListener(la);
				channel.sendMessage("nono").queue();
			}
		}

	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
	}

	@Override
	public void run_button(ButtonClickEvent event) {
	}

	@Override
	public boolean has_reaction() {
		return false;
	}

	@Override
	public boolean has_slash() {
		return false;
	}

	@Override
	public boolean has_button() {
		return false;
	}

	@Override
	public boolean has_basic_help() {
		return false;
	}

	@Override
	public EmbedBuilder get_help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> get_slash() {
		return null;
	}

	@Override
	public Field get_basic_help() {
		return null;
	}

	@Override
	public String get_topname() {
		return "AutoPlace";
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		return short_commands;
	}

	@Override
	public void unload() {
		bot.Bot.jda.removeEventListener(this);
		running = false;
		pixelThread.interrupt();
		if (la != null) {
			Bot.jda.removeEventListener(la);
			Bot.jda.cancelRequests();
			la = null;
		}
	}

	private static WritableRaster normalizeRaster(Raster in) {
		double width = 1.0 * in.getWidth() / n;
		double height = 1.0 * in.getHeight() / n;
		WritableRaster out = in.createCompatibleWritableRaster(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				double[] ff = in.getPixel((int) Math.floor(i * width), (int) Math.floor(j * height), new double[4]);
				double[] fc = in.getPixel((int) Math.floor(i * width), (int) Math.ceil(j * height), new double[4]);
				double[] cf = in.getPixel((int) Math.ceil(i * width), (int) Math.floor(j * height), new double[4]);
				double[] cc = in.getPixel((int) Math.ceil(i * width), (int) Math.ceil(j * height), new double[4]);
				double[] avg = new double[4];
				for (int k = 0; k < 4; k++) {
					avg[k] = (ff[k] + fc[k] + cf[k] + cc[k]) / 4;
				}
				out.setPixel(i, j, avg);
			}
		}
		return out;
	}

	static LinkedList<String> linkToString(String link) {
		try {
			URL url = new URL(link);
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int x = 0;
			while (-1 != (x = in.read(buf))) {
				out.write(buf, 0, x);
			}
			out.close();
			in.close();
			byte[] response = out.toByteArray();
			File png = new File("emote.png");
			FileOutputStream fos = new FileOutputStream(png);
			fos.write(response);
			fos.close();

			LinkedList<String> stringlist = new LinkedList<>();

			Raster image = normalizeRaster(ImageIO.read(png).getData());
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					int[] data = image.getPixel(i, j, new int[4]);
					if (data[3] != 0) {
						stringlist.add(".place setpixel " + ((n * (canvaspointer % m)) + i) + " "
								+ ((n * (canvaspointer / m)) + j) + " " + rgbToHex(new Color(data[0], data[1], data[2]))
								+ " c was: " + canvaspointer);
					}
				}
			}
			canvaspointer++;
			if (canvaspointer > m * m) {
				canvaspointer = 0;
			}
			return stringlist;

		} catch (Exception e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	private static String rgbToHex(Color c) {
		return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
	}

}

class EmoteStorage {
	int n = AutoPlace.n;
	int m = AutoPlace.m;
	Emote[][] emotes = new Emote[m][m];
	int in_pointer = 0;
	int out_pointer = 0;

	void addEmote(Emote emote) {
		emotes[in_pointer / m][in_pointer % m] = emote;
		in_pointer++;
		if (in_pointer > m * m - 1) {
			in_pointer = 0;
		}
	}

	String getEmote() {
		String out = (emotes[out_pointer / m][out_pointer % m] != null)
				? emotes[out_pointer / m][out_pointer % m].getImageUrl()
				: "https://cdn.discordapp.com/emojis/797839072939868220.png";
		out_pointer++;
		if (out_pointer > m * m) {
			out_pointer = 0;
		}
		return out;
	}
}

class PlaceRunnable implements Runnable {

	LinkedList<String> lines;
	MessageChannel channel;

	PlaceRunnable(LinkedList<String> lines, MessageChannel channel) {
		this.lines = lines;
		this.channel = channel;
	}

	@Override
	public void run() {
		while (AutoPlace.running) {
			while (!lines.isEmpty() && AutoPlace.running) {
				String send = lines.removeFirst();
				try {
					Message m = channel.sendMessage(send).complete();
				} finally {

				}
			}
		}
		System.out.println("PIXEL THREAD STOPPED");
	}

}

class APLA extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().getId().equals("817846061347242026")) {
			String content = event.getMessage().getContentRaw();
			if (content.toLowerCase().startsWith(".place setpixel")) {
				String[] split = content.split(" ");
				String nr = split[4];
				String x = split[2];
				String y = split[3];
				Color color = new Color(Integer.valueOf(nr.substring(1, 3), 16),
						Integer.valueOf(nr.substring(3, 5), 16), Integer.valueOf(nr.substring(5, 7), 16));
				int sum = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
				event.getMessage()
						.reply(".place setpixel " + x + " " + y + " " + String.format("#%02x%02x%02x", sum, sum, sum)
								+ " | <t:" + (1800+event.getMessage().getTimeCreated().toEpochSecond()) + ":R>")
						.queueAfter(30, TimeUnit.MINUTES);
			}
		}
	};
}