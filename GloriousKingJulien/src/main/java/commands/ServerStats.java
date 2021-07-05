package commands;

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

	Server(long id, int currentusers) {
		this.id = id;
		minusers = currentusers;
		maxusers = currentusers;
		mintime = new DateTime();
		maxtime = new DateTime();
	}

	void add_user_msg() {
		daymessagesuser++;
	}

	void add_bot_msg() {
		daymessagesbot++;
	}

	void new_day() {
		daymessagesuser = 0;
		daymessagesbot = 0;
	}
}