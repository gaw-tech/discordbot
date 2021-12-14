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
import java.util.Map.Entry;
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
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

public class ButtonCommand extends ListenerAdapter implements Module {
	HashMap<Long, ButtonGame> servers = new HashMap<>();
	String emote = ":POLICE:796671967922749441";
	final static String button_message = "Press the button to claim the points.";
	Runnable run = new Runnable() {
		@Override
		public void run() {
			for (Long l : servers.keySet()) {
				servers.get(l).run();
			}
		}
	};
	ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
	String topname = "Button";

	public ButtonCommand() {
		exec.scheduleAtFixedRate(run, 0, 1, TimeUnit.MINUTES);
	}

	JsonObject player; // player id für lambda schissi
	int stupidlambdacounter = 0;

	// leaderboard
	private BgscoreUser[] printBest(long serverId) {
		try {
			Reader reader = Files.newBufferedReader(Paths.get(serverId + "buttonscores.json"));
			JsonObject parser = (JsonObject) Jsoner.deserialize(reader);
			JsonArray users = (JsonArray) parser.get("users");
			BgscoreUser[] userarray = new BgscoreUser[users.size()];
			stupidlambdacounter = 0;
			users.forEach(entity -> {
				JsonObject player = (JsonObject) entity;
				BgscoreUser current = new BgscoreUser(((BigDecimal) player.get("id")).longValue(),
						((BigDecimal) player.get("bgscore")).longValue());
				userarray[stupidlambdacounter] = current;
				stupidlambdacounter++;
			});
			for (int j = 0; j < userarray.length; j++) {
				for (int i = 0; i < userarray.length - 1; i++) {
					if (userarray[i].bgscore < userarray[i + 1].bgscore) {
						BgscoreUser tmp = userarray[i + 1];
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

	// claim points
	public void claim(ButtonClickEvent event, JDA jda, InteractionHook ih) {
		// get the game from the correct guild
		final ButtonGame currentBgame = servers.get(event.getGuild().getIdLong());

		// if the game for the guild does not exist create an instance of the game and
		// add it to the HashMap
		/*
		 * if (currentBgame == null) { currentBgame = new ButtonGame(event.getChannel(),
		 * event.getGuild().getIdLong(), jda); servers.put(event.getGuild().getIdLong(),
		 * currentBgame); }
		 */ // <this case should not happen so its commented out

		if (currentBgame.message.getIdLong() == event.getMessageIdLong() && !event.getUser().isBot()) {
			MessageChannel channel = currentBgame.channel;

			// writeScore returns true if the current value of the button is higher than the
			// score of the one who pressed the button
			if (writeScore(event.getUser().getIdLong(), event.getGuild().getIdLong(), currentBgame)) {
				event.getMessage().delete().queue();
				channel.sendMessage(
						event.getUser().getAsMention() + " has claimed " + currentBgame.getValue() + " points.")
						.queue();
				long points = currentBgame.getValue();
				currentBgame.setValue(10);
				channel.sendMessage(button_message).setActionRow(currentBgame.button).queue((msg) -> {
					currentBgame.message = msg;
				});
				ih.editOriginal("You claimed the button!").queue();
				((MessageChannel) jda.getGuildChannelById(895969374689701888L))
						.sendMessage(event.getUser().getAsMention() + "(" + event.getUser().getName() + ") has claimed "
								+ points + " points.")
						.queue();
			} else {
				ih.editOriginal("Your score is higher than the one of the button, you can't claim it.").queue();
			}
		}
	}

	// write the score to the json fil
	private boolean writeScore(long playerId, long serverId, ButtonGame bgame) {
		String filename = serverId + "buttonscores.json";
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
				System.out.println("2time");
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
				JsonObject player = new JsonObject();
				player.put("id", playerId);
				player.put("bgscore", bgame.getValue());
				users.add(player);
				parser.put("users", users);
				Jsoner.serialize(parser, writer);
				writer.close();
				return true;
			} else if (((BigDecimal) this.player.get("bgscore")).longValue() < bgame.getValue()) {
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
				this.player.put("bgscore", bgame.getValue());
				parser.put("users", users);
				Jsoner.serialize(parser, writer);
				writer.close();
				return true;
			}
			return false;
		} catch (IOException e) { // TODO Auto-generated catch block
			try {

				// creates a new file if it does not exist
				System.out.println("button bad file");
				System.out.println("2time");
				File file = new File(filename);
				BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()));
				JsonObject parser = new JsonObject();
				JsonArray users = new JsonArray();
				JsonObject player = new JsonObject();
				player.put("id", playerId);
				player.put("bgscore", bgame.getValue());
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
			try {
				JsonObject end = new JsonObject();
				JsonArray users = new JsonArray();
				BufferedWriter writer = Files.newBufferedWriter(Paths.get("buttonscores.json"));
				end.put("users", users);
				Jsoner.serialize(end, writer);
				writer.close();

			} catch (IOException e1) {
				System.out.println("verschachteltes catsch ting");
				e1.printStackTrace();
			}

		}
		return false;
	}

	@Override
	public void run_message(MessageReceivedEvent event) {
		JDA jda = event.getJDA();
		if (event.getAuthor().isBot() || event.getAuthor().getIdLong() == 802472545172455444L
				|| event.getAuthor().getIdLong() == 466292292945313799L) // ignore za
			return;
		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = event.getChannel();

		// get the game from the correct guild
		ButtonGame currentBgame = servers.get(event.getGuild().getIdLong());

		// if the game for the guild does not exist create an instance of the game and
		// add it to the HashMap
		if (currentBgame == null) {
			currentBgame = new ButtonGame(event.getChannel(), event.getGuild().getIdLong(), jda);
			servers.put(event.getGuild().getIdLong(), currentBgame);
		}

		if (content.startsWith(prefix + "setbscore ") && event.getAuthor().getAsTag().equals("Georg#3258")) {
			event.getMessage().delete().queue();
			int value = Integer.parseInt(content.replaceAll("[\\D]", ""));
			currentBgame.setValue(value);
		}

		// command to get the button
		if (content.equals(prefix + "button") || content.equals(prefix + "b")) {
			event.getMessage().delete().queue(); // delete the "?b" or "?button" message replace the channelId with the
													// new channelId
			currentBgame.channel = channel;

			// makes that it not always throws errors if noone has typed "?b" or "?button"
			currentBgame.initiated = true;

			// send the new button message
			channel.sendMessage(button_message).setActionRow(currentBgame.button).queue((msg) -> {
				// updates the msgId of the game. Its kinda wierd because i can't the
				// currenBgame variable in this block.
				ButtonGame cbg = servers.get(msg.getGuild().getIdLong());
				if (cbg.message != null)
					cbg.message.delete().queue();
				cbg.message = msg;
			});
		}

		if (content.equals(prefix + "buttonboard") || content.equals(prefix + "bb")) {
			BgscoreUser[] toplist = printBest(currentBgame.serverId);

			// if not enough ppl
			if (toplist.length < 10) {
				channel.sendMessage("Not enough ppl on the scoreboard").queue();
			}

			String nickname = "";
			String users = "";
			String scores = "";
			String rank = "";

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("ButtonBoard", null);
			eb.setColor(1);
			eb.setDescription("Top 10 Players of the button game.\n");

			for (int i = 0; i < 10; i++) {
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
				scores = scores + toplist[i].bgscore + "\n";
				rank = rank + "**" + (i + 1) + ".**\n";
			}
			eb.addField("", "**Rank:**\n" + rank, true);
			eb.addField("", "**Player:** \n" + users, true);
			eb.addField("", "**Score:** \n" + scores, true);
			nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
			channel.sendMessage(eb.build()).queue();
		}

		if (content.equals(prefix + "ba") && event.getAuthor().getAsTag().equals("Georg#3258")) {
			BgscoreUser[] toplist = printBest(currentBgame.serverId);
			String nickname = "";
			String users = "";
			String scores = "";

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("ButtonBoard", null);
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
				scores = scores + toplist[i].bgscore + "\n";
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
			channel.sendMessage(eb.build()).queue();
		}

		if (content.equals(prefix + "bs")) {
			BgscoreUser[] toplist = printBest(currentBgame.serverId);
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
				channel.sendMessage(event.getAuthor().getAsMention() + ", you have " + toplist[rank - 1].bgscore
						+ " points. You are nr. " + rank).queue();
			}
		}

		// check for a score
		if (content.startsWith(prefix + "bs ")) {
			BgscoreUser[] toplist = printBest(currentBgame.serverId);
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
				channel.sendMessage(mentions.get(0).getAsMention() + ", has " + toplist[rank - 1].bgscore
						+ " points and is nr. " + rank).queue();
			}
		}

		// gives the number of running games
		if (content.equals(prefix + "nrofgames")) {
			channel.sendMessage("" + servers.size() + " games that i'm aware of.").queue();
		}
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help Button");
		eb.addField("Description:",
				"Welcome to the button game. The goal of the button game is to claim as much points as possible. This is done by pressing the \"button\". Note that your score is the highest score you claimed the button at, so if you claim the button your new score is the value of the button.",
				true);
		eb.addField("Usage:",
				"`" + prefix + "b` get the button message\n`" + prefix
						+ "bb` get a list with the top 10 players of the button game\n`" + prefix
						+ "bs` tells you your current score and your rank in the button game\n`" + prefix
						+ "bs @user` returns the score and rank of the mentioned user",
				false);
		return eb;
	}

	@Override
	public Field get_basic_help() {
		return new Field(topname, "`" + prefix + "b` get the button\n`" + prefix + "bb` shows the ButtonBoard\n`"
				+ prefix + "bs` your rank in the button game", true, true);
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public void unload() {
		for (Entry<Long, ButtonGame> entry : servers.entrySet()) {
			entry.getValue().message.delete().queue();
		}
		exec.shutdownNow();
		run = null;
		exec = null;
		player = null;
		servers = null;
		emote = null;
		stupidlambdacounter = 0;
		topname = null;
	}

	@Override
	public boolean has_reaction() {
		return false;
	}

	@Override
	public boolean has_basic_help() {
		return true;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		short_commands.add("b");
		short_commands.add("bs");
		short_commands.add("ba");
		short_commands.add("bb");
		short_commands.add("buttonscore");
		short_commands.add("bamboozle");
		short_commands.add("nrofgames");
		short_commands.add("setbscore");
		short_commands.add("buttonboard");
		return short_commands;
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
		// get the game from the correct guild
		ButtonGame currentBgame = servers.get(event.getGuild().getIdLong());
		MessageChannel channel = event.getChannel();

		// if the game for the guild does not exist create an instance of the game and
		// add it to the HashMap
		if (currentBgame == null) {
			currentBgame = new ButtonGame(event.getChannel(), event.getGuild().getIdLong(), event.getJDA());
			servers.put(event.getGuild().getIdLong(), currentBgame);
		}

		// command to get the button
		if (event.getName().equals("button")) {
			// delete the thing from the slash cmd
			event.deferReply().queue(ih -> {
				ih.deleteOriginal().queue();
			});

			// replace the channelId with the new channelId
			currentBgame.channel = channel;

			// makes that it not always throws errors if noone has typed "?b" or "?button"
			currentBgame.initiated = true;

			// send the new button message
			channel.sendMessage(button_message).setActionRow(currentBgame.button).queue((msg) -> {
				// updates the msgId of the game. Its kinda wierd because i can't the
				// currenBgame variable in this block.
				ButtonGame bg = servers.get(event.getGuild().getIdLong());
				if (bg.message != null)
					bg.message.delete().queue();
				bg.message = msg;
			});
		}

		// command to get your button score
		if (event.getName().equals("buttonscore")) {
			BgscoreUser[] toplist = printBest(currentBgame.serverId);
			boolean founduser = false;
			int rank = 0;
			for (int i = 0; i < toplist.length; i++) {
				if (toplist[i].id == event.getMember().getIdLong()) {
					founduser = true;
					rank = i + 1;
					break;
				}
			}

			if (!founduser) {
				event.reply("You have no score.").queue();
			} else {
				event.reply(event.getMember().getAsMention() + ", you have " + toplist[rank - 1].bgscore
						+ " points. You are nr. " + rank).queue();
			}
		}

		// command to get the scoreboard
		if (event.getName().equals("buttonboard")) {
			InteractionHook interactionHook = event.deferReply().complete();
			BgscoreUser[] toplist = printBest(currentBgame.serverId);

			// if not enough ppl
			if (toplist.length < 5) {
				channel.sendMessage("Not enough ppl on the scoreboard").queue();
			}

			String nickname = "";
			String users = "";
			String scores = "";
			String rank = "";

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("ButtonBoard", null);
			eb.setColor(1);
			eb.setDescription("Top 10 Players of the button game.\n");

			for (int i = 0; i < 10; i++) {
				if (event.getGuild().getMemberById(toplist[i].id) != null) {
					nickname = (event.getGuild().getMemberById(toplist[i].id).getNickname() != null)
							? event.getGuild().getMemberById(toplist[i].id).getNickname()
							: event.getGuild().getMemberById(toplist[i].id).getEffectiveName();
				} else {
					nickname = "" + toplist[i].id;
				}
				users = users + nickname + ":\n";
				scores = scores + toplist[i].bgscore + "\n";
				rank = rank + "**" + (i + 1) + ".**\n";
			}
			eb.addField("", "**Rank:**\n" + rank, true);
			eb.addField("", "**Player:** \n" + users, true);
			eb.addField("", "**Score:** \n" + scores, true);
			nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			eb.setFooter("Summoned by: " + nickname, event.getUser().getAvatarUrl());
			interactionHook.editOriginalEmbeds(eb.build()).queue();
		}

	}

	@Override
	public HashMap<String, String> get_slash() {
		HashMap<String, String> slash_commands = new HashMap<>();
		slash_commands.put("button", "Sends the button from the ButtonGame.");
		slash_commands.put("buttonscore", "Tells you your rank on the ButtonGame scoreboard.");
		slash_commands.put("buttonboard", "Lists the top 10 players of the ButtonGame.");
		return slash_commands;
	}

	@Override
	public boolean has_slash() {
		return true;
	}

	@Override
	public void run_button(ButtonClickEvent event) {
		if (!event.getButton().getId().startsWith("bgb")) {
			return;
		}
		InteractionHook ih = event.deferReply().setEphemeral(true).complete();
		LinkedList<String> ignore = new LinkedList<>();
		ignore.add("802472545172455444");// za
		ignore.add("466292292945313799");// za
		ignore.add("599179325224386561");// pherb
		ignore.add("304587987956531201");// pherb
		ignore.add("814844933813829652");// popopina
		ignore.add("850353176862916608");// ibot
		ignore.add("289864423768719372");//markies alt
		if (ignore.contains(event.getUser().getId())) {
			return;
		}
		claim(event, event.getJDA(), ih);
	}

	@Override
	public boolean has_button() {
		return true;
	}
}

class BgscoreUser {
	public long id;
	public long bgscore;

	public BgscoreUser(long id, long bgscore) {
		this.id = id;
		this.bgscore = bgscore;
	}
}

class ButtonGame {
	private long value = 10;
	MessageChannel channel;
	long serverId;
	boolean initiated = false;
	boolean candelete = false;
	Emoji emoji = Emoji.fromEmote("sipspin", 831867874506178660L, true);
	Button button = Button.of(ButtonStyle.PRIMARY, "bgb" + serverId, "10", emoji);
	Message message;

	// Constructor
	ButtonGame(MessageChannel channel, long serverId, JDA jda) {
		this.channel = channel;
		this.serverId = serverId;
	}

	public void run() {
		this.value++;
		button = Button.of(ButtonStyle.PRIMARY, "bgb" + serverId, "" + value, emoji);
		if (initiated) {
			message.editMessage(ButtonCommand.button_message).setActionRow(button).queue();
		}
	}

	public long getValue() {
		return this.value;
	}

	public void setValue(long value) {
		this.value = value;
		button = Button.of(ButtonStyle.PRIMARY, "bgb" + serverId, "" + value,
				Emoji.fromEmote("discordloading", 852953358745468938l, true));
	}

}
