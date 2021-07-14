package commands;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import BetterBot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerStats extends ListenerAdapter implements Module {
	static DateTime starttime = new DateTime();
	static HashMap<Long, Server> servers = new HashMap<>();
	private static JDA jda;
	String topname = "Stats";
	static int dayofyear;
	Runnable run = new Runnable() {
		@Override
		public void run() {
			if (dayofyear != new DateTime().getDayOfYear()) {
				dayofyear = new DateTime().getDayOfYear();
				newDay();
			}
			minuteCheck();
		}
	};
	ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	public ServerStats() {
		exec.scheduleAtFixedRate(run, 0, 1, TimeUnit.MINUTES);
		jda = BetterBot.BetterBot.jda;
		jda.addEventListener(this);
		dayofyear = new DateTime().getDayOfYear();
	}

	// message event only for stats counter
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (servers.containsKey(event.getGuild().getIdLong())) {
			Server server = servers.get(event.getGuild().getIdLong());
			if (event.getAuthor().isBot()) {
				server.add_bot_msg();
			} else {
				server.add_user_msg();
			}
		}
	}

	static void newDay() {
		for (Entry<Long, Server> e : servers.entrySet()) {
			e.getValue().new_day();
		}
	}

	static void minuteCheck() {
		for (Guild g : jda.getGuilds()) {
			if (!servers.containsKey(g.getIdLong())) {
				Server server = new Server(g.getIdLong(), getOnlineUsers(g));
				servers.put(g.getIdLong(), server);
			} else {
				Server server = servers.get(g.getIdLong());
				int onlineusers = getOnlineUsers(g);
				server.minutedata[new DateTime().getMinuteOfDay()].users_online = onlineusers;
				if (onlineusers > server.maxusers) {
					server.maxusers = onlineusers;
					server.maxtime = new DateTime();
				} else if (onlineusers < server.minusers) {
					server.minusers = onlineusers;
					server.mintime = new DateTime();
				}
			}
		}
	}

	private static int getOnlineUsers(Guild guild) {
		int usersonline = 0;
		for (Member m : guild.getMembers()) {
			if (!m.getUser().isBot()) {
				OnlineStatus os = m.getOnlineStatus();
				if (!(os.equals(OnlineStatus.OFFLINE) || os.equals(OnlineStatus.UNKNOWN))) {
					usersonline++;
				}
			}
		}
		return usersonline;
	}

	@Override
	public void run_message(MessageReceivedEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		String content = message.getContentRaw();
		if (content.equals(prefix + "os")) {
			message.delete().queue();
			// to get stuff like # bots and # normal members
			int bots = 0;
			int members = 0;
			for (Member m : event.getGuild().getMembers()) {
				if (m.getUser().isBot()) {
					bots++;
				} else {
					members++;
				}
			}
			Server server = servers.get(event.getGuild().getIdLong());
			EmbedBuilder eb = new EmbedBuilder();
			eb.setDescription("I'm only tracking almost nothing but i'll add more stuff.");
			eb.setTitle("Well, i'm not sure about the title.");
			eb.addField("Members:",
					"Total Members: " + event.getGuild().getMemberCount() + "\nHumans: " + members + "\nBots: " + bots,
					false);
			eb.addField("Online Users:",
					"max: " + server.maxusers + " reached at " + server.maxtime.toString("dd.MM.yyyy HH:mm") + "\nmin: "
							+ server.minusers + " reached at " + server.mintime.toString("dd.MM.yyyy HH:mm")
							+ "\ncurrent: " + getOnlineUsers(event.getGuild()),
					false);
			eb.addField("Messages sent since midnight:",
					"by bots: " + server.daymessagesbot + "\nby humans: " + server.daymessagesuser, false);
			eb.setFooter("Tracking since " + starttime.toString("dd.MM.yyyy"));
			channel.sendMessage(eb.build()).queue(msg -> {
				msg.delete().queueAfter(1, TimeUnit.MINUTES);
			});
		}

		// graph
		if (content.equals(prefix + "graph")) {
			Server server = servers.get(event.getGuild().getIdLong());
			// get string for labels and data
			String labelnewmessages = "";
			String datanewmessages = "";
			String dataoldmessages = "";
			String datanewonlineusers = "";
			String dataoldonlineusers = "";
			DateTime dt = new DateTime();
			for (int i = 0; i < 96; i++) {
				labelnewmessages += i / 4 + "." + i % 4 + ",";
				int dnms = 0;
				int doms = 0;
				int dnou = 0;
				int doou = 0;
				for (int j = 0; j < 15; j++) {
					dnms += server.minutedata[i * 15 + j].count_messages;
					doms += server.old_minutedata[i * 15 + j].count_messages;
					dnou += server.minutedata[i * 15 + j].users_online;
					doou += server.old_minutedata[i * 15 + j].users_online;
				}
				datanewmessages += dnms + ",";
				dataoldmessages += doms + ",";
				datanewonlineusers += ((i * 15 < dt.getMinuteOfDay() - 1) ? dnou / 15 : "0") + ",";
				dataoldonlineusers += doou / 15 + ",";
			}
			labelnewmessages = labelnewmessages.substring(0, labelnewmessages.length() - 1);
			datanewmessages = datanewmessages.substring(0, datanewmessages.length() - 1);
			dataoldmessages = dataoldmessages.substring(0, dataoldmessages.length() - 1);
			datanewonlineusers = datanewonlineusers.substring(0, datanewonlineusers.length() - 1);
			dataoldonlineusers = dataoldonlineusers.substring(0, dataoldonlineusers.length() - 1);
			// request string builder
			try {
				String url_in = "{\"backgroundColor\":\"#36393f\",\"width\":1000,\"height\":600,\"format\":\"png\",\"chart\":{\"type\":\"bar\",\"yAxisID\":\"y1\",\"data\":{\"labels\":["
						+ labelnewmessages + "],\"datasets\":[{\"label\":\"messages sent today\",\"data\":["
						+ datanewmessages + "]}" + ",{\"label\":\"messages sent yesterday\",\"data\":["
						+ dataoldmessages + "]}"
						+ ",{\"type\":\"line\",\"fill\":\"false\",\"label\":\"members online today\",\"yAxisID\":\"y2\",\"data\":["
						+ datanewonlineusers
						+ "]},{\"type\":\"line\",\"fill\":\"false\",\"label\":\"members online yesterday\",\"yAxisID\":\"y2\",\"data\":["
						+ dataoldonlineusers
						+ "]}]},\"options\":{\"scales\":{\"xAxes\":[{\"stacked\":false}],\"yAxes\":[{\"id\":\"y1\",\"display\":true,\"position\":\"left\",\"stacked\":false},{\"id\":\"y2\",\"display\":true,\"position\":\"right\",\"gridLines\":{\"drawOnChartArea\":false}}]}}}}";

				// get thse image
				URL url = new URL("https://quickchart.io/chart");

				// channel.sendMessage(url_in).queue(); //TODO
				System.out.println(url_in);// TODO

				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json");
				con.setRequestProperty("Accept", "application/json");
				con.setDoOutput(true);

				System.out.println("point a");
				try (OutputStream os = con.getOutputStream()) {
					byte[] input = url_in.getBytes("utf-8");
					os.write(input);
				} catch (IOException e) {
					System.out.println("outputstream error");
					e.printStackTrace();
				}
				System.out.println("point b");

				try (InputStream in = new BufferedInputStream(con.getInputStream());) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[1024];
					int n = 0;
					while (-1 != (n = in.read(buf))) {
						out.write(buf, 0, n);
					}
					out.close();
					in.close();
					byte[] response = out.toByteArray();
					FileOutputStream fos = new FileOutputStream("stats_char.png");
					fos.write(response);
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
					BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"));
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					channel.sendMessage("ERROR: " + response.toString()).queue(msg -> {
						msg.delete().queueAfter(15, TimeUnit.SECONDS);
					});
					br.close();
				}

				channel.sendFile(new File("stats_char.png")).queue();
				new File("stats_char.png").delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has_button() {
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
		short_commands.add("os");
		short_commands.add("graph");
		return short_commands;
	}

	@Override
	public void unload() {
		exec.shutdownNow();
		run = null;
		topname = null;
		jda.removeEventListener(this);
		jda = null;
		servers = null;
		starttime = null;
		dayofyear = 0;
	}
}

class Server {
	long id;
	int minusers = 0;
	int maxusers = 0;
	DateTime mintime;
	DateTime maxtime;
	int daymessagesbot = 0;
	int daymessagesuser = 0;

	MinuteChunk[] minutedata = new MinuteChunk[1440];
	MinuteChunk[] old_minutedata = new MinuteChunk[1440];
	int dayofyear;

	Server(long id, int currentusers) {
		this.id = id;
		minusers = currentusers;
		maxusers = currentusers;
		mintime = new DateTime();
		maxtime = new DateTime();
		dayofyear = new DateTime().getDayOfYear();
		for (int i = 0; i < 1440; i++) {
			minutedata[i] = new MinuteChunk();
		}
		old_minutedata = minutedata;
	}

	void add_user_msg() {
		daymessagesuser++;
		if (dayofyear != new DateTime().getDayOfYear()) {
			dayofyear = new DateTime().getDayOfYear();
			old_minutedata = minutedata;
			minutedata = new MinuteChunk[1440];
			for (int i = 0; i < 1440; i++) {
				minutedata[i] = new MinuteChunk();
			}
		}
		minutedata[new DateTime().getMinuteOfDay()].add_message_count();
	}

	void add_bot_msg() {
		daymessagesbot++;
	}

	void new_day() {
		daymessagesuser = 0;
		daymessagesbot = 0;
	}
}

class MinuteChunk {
	int count_messages = 0;
	int users_online = 0;

	MinuteChunk() {
	}

	void add_message_count() {
		count_messages++;
	}
}