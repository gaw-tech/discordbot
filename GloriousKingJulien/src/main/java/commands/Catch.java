package commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import bot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Catch extends ListenerAdapter implements Module {
	String topname = "Catch";
	HashMap<Long, CatchGame> servers = new HashMap<>();

	JsonObject player; // player id für lambda schissi
	int stupidlambdacounter = 0;
	Runnable run = new Runnable() {
		@Override
		public void run() {
			for (Long l : servers.keySet()) {
				servers.get(l).run();
			}
		}
	};
	ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	public Catch() {
		exec.scheduleAtFixedRate(run, 0, 1, TimeUnit.MINUTES);
	}

	// leaderboard
	private CatchUser[] printBest(long serverId) {
		try {
			Reader reader = Files.newBufferedReader(Paths.get(serverId + "catch.json"));
			JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
			JsonArray users = (JsonArray) parser.get("users");
			CatchUser[] userarray = new CatchUser[users.size()];
			stupidlambdacounter = 0;
			users.forEach(entity -> {
				JsonObject player = (JsonObject) entity;
				CatchUser current = new CatchUser(((BigDecimal) player.get("id")).longValue(),
						((BigDecimal) player.get("catchscore")).longValue());
				userarray[stupidlambdacounter] = current;
				stupidlambdacounter++;
			});
			for (int j = 0; j < userarray.length; j++) {
				for (int i = 0; i < userarray.length - 1; i++) {
					if (userarray[i].catchscore < userarray[i + 1].catchscore) {
						CatchUser tmp = userarray[i + 1];
						userarray[i + 1] = userarray[i];
						userarray[i] = tmp;
					}
				}
			}
			return userarray;
		} catch (IOException e) {
			System.out.println("print best reader");
			e.printStackTrace();
		} catch (JsonException e) {
			System.out.println("print best json");
			e.printStackTrace();
		}
		return null;
	}

	// claim pointes

	@Override
	public void run_reaction(MessageReactionAddEvent event) {

		// we only want to check stuff if there is a game running and no bots
		if (!servers.keySet().contains(event.getGuild().getIdLong()) || event.getUser().isBot()) {
			return;
		}

		// get the game from the correct guild
		CatchGame currentCgame = servers.get(event.getGuild().getIdLong());

		// stop when its a reaction to selfsteal
		if (currentCgame.itemholder == event.getUserIdLong()) {
			return;
		}

		// steal direct
		if (System.currentTimeMillis() - currentCgame.cooldownstart < currentCgame.cooldownsteal) {
			return;
		}

		// cooldown is still active or self steal
		Message msg = null;
		try {
			msg = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
		} catch (ErrorResponseException e) {
		}
		if (msg != null && msg.getAuthor().getIdLong() == servers.get(event.getGuild().getIdLong()).itemholder) {
			messagematch(event);
		}

		// steal from bot
		// fist check if the reaction was from the old message and if so check if the
		// cooldown is over
		if (event.getMessageIdLong() == currentCgame.oldmessage.getIdLong()) {
			if (System.currentTimeMillis() - currentCgame.cooldownstart < currentCgame.cooldown) {
				return;
			}
			messagematch(event);
		}
	}

	private void messagematch(MessageReactionAddEvent event) {
		CatchGame currentCgame = servers.get(event.getGuild().getIdLong());
		currentCgame.cooldownstart = System.currentTimeMillis();
		currentCgame.itemholder = event.getUserIdLong();
		currentCgame.itemholdertag = event.getUser().getAsTag();
		try {
			currentCgame.oldmessage.delete().complete();
		} finally {

		}
		currentCgame.channel
				.sendMessage("Someone stole the item, the cooldown is " + currentCgame.cooldown / 1000.0 + " seconds.\n"
						+ currentCgame.getTag(true) + " has the thing.")
				.queueAfter(new Random().nextInt(10), TimeUnit.SECONDS, (msg) -> {
					servers.get(event.getGuild().getIdLong()).oldmessage = msg;
				});
		// send message to log
		((MessageChannel) event.getJDA().getGuildChannelById(897075459739775027L))
				.sendMessage(event.getUser().getAsMention() + "(" + event.getUser().getName() + ", "
						+ event.getMember().getNickname() + ") stole the thing.")
				.queueAfter(2, TimeUnit.MINUTES);
	}

	@Override
	public void run_message(MessageReceivedEvent event) {

		// we only want to check stuff if there is a game running
		if (event.getAuthor().isBot() || !servers.keySet().contains(event.getGuild().getIdLong())) {
			// initialize a game command
			if (event.getAuthor().getId().equals(bot.Bot.myID)
					&& event.getMessage().getContentRaw().equals(prefix + "catch start")) {
				Long guildid = event.getGuild().getIdLong();
				servers.put(guildid, new CatchGame(event.getChannel(), guildid));
				event.getMessage().delete().queue();
			}
			return;
		}

		JDA jda = event.getJDA();
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = event.getChannel();

		// get the game from the correct guild
		CatchGame currentCgame = servers.get(event.getGuild().getIdLong());

		// command to get the catch message
		if (content.equals(prefix + "catch") || content.equals(prefix + "c")) {
			// messages from the Catchgame only work in the channel it was initialised in
			if (event.getChannel().getIdLong() != currentCgame.channel.getIdLong()) {
				return;
			}
			// replace the channelId with the new channelId
			currentCgame.channel = channel;

			// send the new catch message
			if (currentCgame.itemholder == 0) {
				Message tmp = channel.sendMessage(
						"Nobody has stolen the item yet. Feel free to steal it by adding a reaction to this message.")
						.complete();
				if (currentCgame.oldmessage != null)
					try {
						currentCgame.oldmessage.delete().complete();
					} catch (ErrorResponseException e) {

					}
				currentCgame.oldmessage = tmp;
			} else {
				if (currentCgame.cooldown > System.currentTimeMillis() - currentCgame.cooldownstart) {
					Message tmp = channel.sendMessage("The game is on cooldown for another "
							+ (currentCgame.cooldown - (System.currentTimeMillis() - currentCgame.cooldownstart))
									/ 1000.0
							+ " seconds.\n" + currentCgame.getTag(false) + " has the thing.").complete();
					if (currentCgame.oldmessage != null)
						try {
							currentCgame.oldmessage.delete().complete();
						} catch (ErrorResponseException e) {

						}
					currentCgame.oldmessage = tmp;
				} else {
					Message tmp = channel
							.sendMessage("Feel free to steal the item by adding a reaction to this message.")
							.complete();
					if (currentCgame.oldmessage != null)
						try {
							currentCgame.oldmessage.delete().complete();
						} catch (ErrorResponseException e) {

						}
					currentCgame.oldmessage = tmp;
				}
			}
			event.getMessage().delete().queue();
		}

		if (content.equals(prefix + "catchboard") || content.equals(prefix + "cb")) {
			CatchUser[] toplist = printBest(currentCgame.serverId);

			String nickname = "";
			String users = "";
			String scores = "";
			String rank = "";

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("CatchBoard", null);
			eb.setColor(1);
			eb.setDescription("Top 10 Players of the catch game.\n");

			for (int i = 0; i < 10 && i < toplist.length; i++) {
				if (event.getGuild().getMemberById(toplist[i].id) != null) {
					nickname = event.getGuild().getMemberById(toplist[i].id).getAsMention();
				} else {
					nickname = "" + toplist[i].id;
				}
				users = users + nickname + ":\n";
				scores = scores + toplist[i].catchscore + "\n";
				rank = rank + "**" + (i + 1) + ".**\n";
			}
			eb.addField("", "**Rank:**\n" + rank, true);
			eb.addField("", "**Player:** \n" + users, true);
			eb.addField("", "**Score:** \n" + scores, true);
			nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
			channel.sendMessageEmbeds(eb.build()).queue();
		}

		if (content.equals(prefix + "ca") && event.getAuthor().getAsTag().equals("Georg#3258")) {
			CatchUser[] toplist = printBest(currentCgame.serverId);
			String nickname = "";
			String users = "";
			String scores = "";

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("CatchBoard", null);
			eb.setColor(1);

			for (int i = 0; i < toplist.length; i++) {
				if (jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id) != null) {
					nickname = (jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id)
							.getNickname() != null)
									? jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id)
											.getNickname()
									: jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id)
											.getEffectiveName();
				} else {
					nickname = "" + toplist[i].id;
				}
				users = users + nickname + ":\n";
				scores = scores + toplist[i].catchscore + "\n";
				if (users.length() > 950) {
					eb.addField("Player: ", users, true);
					eb.addField("Score: ", scores, true);
					users = "";
					scores = "";
					eb.addBlankField(false);
				}
			}
			eb.addField("Player: ", users, true);
			eb.addField("Score: ", scores, true);
			nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
			channel.sendMessageEmbeds(eb.build()).queue();
		}

		if (content.equals(prefix + "cs")) {
			CatchUser[] toplist = printBest(currentCgame.serverId);
			boolean founduser = false;
			int rank = 0;
			for (int i = 0; i < toplist.length; i++) {
				if (toplist[i].id == event.getAuthor().getIdLong()) {
					founduser = true;
					rank = i + 1;
					break;
				}
			}

			if (!founduser) {
				channel.sendMessage("You have no score.").queue();
			} else {
				channel.sendMessage(event.getAuthor().getAsMention() + ", you have " + toplist[rank - 1].catchscore
						+ " points. You are nr. " + rank).queue();
			}
		}

		// check for a score
		if (content.startsWith(prefix + "cs ")) {
			CatchUser[] toplist = printBest(currentCgame.serverId);
			List<Member> mentions = event.getMessage().getMentionedMembers();
			boolean founduser = false;
			int rank = 0;
			for (int i = 0; i < toplist.length; i++) {
				if (toplist[i].id == mentions.get(0).getIdLong()) {
					founduser = true;
					rank = i + 1;
					break;
				}
			}

			if (!founduser) {
				channel.sendMessage(mentions.get(0).getAsMention() + " has no score.").queue();
			} else {
				channel.sendMessage(mentions.get(0).getAsMention() + ", has " + toplist[rank - 1].catchscore
						+ " points and is nr. " + rank).queue();
			}
		}

		// owner commands
		if (!event.getAuthor().getId().equals(bot.Bot.myID)) {
			return;
		}
		if (content.equals(prefix + "catch list")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setAuthor("Eric", "https://theuselessweb.com/");
			for (Long l : servers.keySet()) {
				CatchGame cg = servers.get(l);
				eb.addField(jda.getGuildById(cg.serverId).getName(),
						"Holder: " + ((jda.getUserById(cg.itemholder).getAsMention() == null) ? "none"
								: jda.getUserById(cg.itemholder).getAsMention()) + "\nTag: " + cg.itemholdertag,
						true);
			}
			event.getChannel().sendMessageEmbeds(eb.build()).queue();
		}
	}

	@Override
	public EmbedBuilder get_help() {
		// TODO
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor("Help Catch", "https://theuselessweb.com/");
		eb.setDescription(
				"Basically you react to the person who has it. After it has been taken there is a 2 minute cooldown before it can be taken again.\r\n"
						+ "with '?c' you can see how much time has passed since it has been last taken and also get a hint as to who has it. 10 minutes after someone took it you can also react to the '?c' message directly to get it");
		eb.addBlankField(true);
		return eb;
	}

	@Override
	public Field get_basic_help() {
		return null;
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public void unload() {
		exec.shutdownNow();
		run = null;
		topname = null;
		servers = null;
		player = null;
		stupidlambdacounter = 0;
		exec = null;
	}

	@Override
	public boolean has_reaction() {
		return true;
	}

	@Override
	public boolean has_basic_help() {
		return false;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		short_commands.add("c");
		short_commands.add("ca");
		short_commands.add("cb");
		short_commands.add("cs");
		return short_commands;
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
	public HashMap<String, String> get_slash() {
		// TODO Auto-generated method stub
		return null;
	}

}

class CatchUser {
	public long id;
	public long catchscore;

	public CatchUser(long id, long catchscore) {
		this.id = id;
		this.catchscore = catchscore;
	}
}

class CatchGame {
	MessageChannel channel;
	Message oldmessage;
	long serverId;
	long cooldown = 600000;
	long cooldownsteal = 120000;
	long cooldownstart = 0;
	long itemholder = 0;
	String itemholdertag = "";
	JsonObject player = null;
	String[] shuffle = null;

	// Constructor
	CatchGame(MessageChannel channel, long serverId) {
		this.channel = channel;
		this.serverId = serverId;
		cooldownstart = System.currentTimeMillis() - cooldown;
	}

	public void run() {
		writeScore(itemholder, serverId);
	}

	// write the score to the json fil
	private boolean writeScore(long playerId, long serverId) {
		String filename = serverId + "catch.json";
		try {
			this.player = null;
			Reader reader = Files.newBufferedReader(Paths.get(filename));
			JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
			JsonArray users = (JsonArray) parser.get("users");
			users.forEach(entry -> {
				JsonObject player = (JsonObject) entry;
				if (((BigDecimal) player.get("id")).longValue() == playerId) {
					this.player = player;
				}
			});

			if (this.player == null) {
				System.out.println("2time i think a new player");
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
				JsonObject player = new JsonObject();
				player.put("id", playerId);
				player.put("catchscore", 1L);
				users.add(player);
				parser.put("users", users);
				Jsoner.serialize(parser, writer);
				writer.close();
				return true;
			} else {
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
				this.player.put("catchscore", ((BigDecimal) this.player.get("catchscore")).longValue() + 1);
				parser.put("users", users);
				Jsoner.serialize(parser, writer);
				writer.close();
				return true;
			}
		} catch (IOException e) { // TODO Auto-generated catch block
			try {

				// creates a new file if it does not exist
				System.out.println("chatch file prolly not extist");
				File file = new File(filename);
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()));
				JsonObject parser = new JsonObject();
				JsonArray users = new JsonArray();
				JsonObject player = new JsonObject();
				player.put("id", 0);
				player.put("catchscore", 1L);
				users.add(player);
				parser.put("users", users);
				Jsoner.serialize(parser, writer);
				writer.close();
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("couldnt create file");
				e1.printStackTrace();
			}
		} catch (JsonException e) { // TODO Auto-generated catch block
			System.out.println("something with the reader");
			e.printStackTrace();
		} catch (NullPointerException e) {
		}
		return false;
	}

	public String getTag(boolean newshuffle) {
		if (newshuffle) {
			int length = itemholdertag.length();
			String[] tagshuffle = new String[length];
			int[] sequence = shuffledNrString(length);
			tagshuffle[0] = "";
			for (int i = 0; i < length; i++) {
				tagshuffle[0] = tagshuffle[0] + "_";
			}
			tagshuffle[0] = replaceCharAt(tagshuffle[0], itemholdertag.charAt(sequence[0]), sequence[0]);
			for (int i = 1; i < length; i++) {
				tagshuffle[i] = replaceCharAt(tagshuffle[i - 1], itemholdertag.charAt(sequence[i]), sequence[i]);
			}
			shuffle = tagshuffle;
		}
		int nr = (int) ((System.currentTimeMillis() - cooldownstart) * 1.0 / cooldown * shuffle.length);
		return "`" + shuffle[nr] + "`";
	}

	private int[] shuffledNrString(int length) {
		int[] out = new int[length];
		for (int i = 0; i < length; i++) {
			out[i] = i;
		}
		Random r = new Random();
		for (int i = 0; i < length * 2; i++) {
			int a = r.nextInt(length);
			int b = r.nextInt(length);
			int tmp = out[a];
			out[a] = out[b];
			out[b] = tmp;
		}
		return out;
	}

	private String replaceCharAt(String in, char what, int where) {
		return in.substring(0, where) + what + in.substring(where + 1);
	}
}