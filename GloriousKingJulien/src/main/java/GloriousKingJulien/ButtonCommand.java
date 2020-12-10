package GloriousKingJulien;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ButtonCommand {
	char prefix = MyBot.prefix;
	HashMap<Long, ButtonGame> servers = new HashMap<>();
	String emote = ":sabotage:766987943734411265";

	JsonObject player; // player id für lambda schissi
	int stupidlambdacounter = 0;

	public void run(MessageReceivedEvent event, JDA jda) {
		if (event.getAuthor().isBot())
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
			currentBgame = new ButtonGame(event.getMessageIdLong(), event.getChannel().getIdLong(),
					event.getGuild().getIdLong());
			servers.put(event.getGuild().getIdLong(), currentBgame);
		}

		if (content.startsWith(this.prefix + "setbscore ") && event.getAuthor().getAsTag().equals("Georg#3258")) {
			event.getMessage().delete().queue();
			int value = Integer.parseInt(content.replaceAll("[\\D]", ""));
			currentBgame.setValue(value);
		}

		// command to get the button
		if (content.equals(this.prefix + "button") || content.equals(this.prefix + "b")) {
			event.getMessage().delete().queue(); // delete the "?b" or "?button" message

			// get the channel of the previous button message
			MessageChannel oldchannel = event.getGuild().getTextChannelById(currentBgame.channelId);

			// replace the channelId with the new channelId
			currentBgame.channelId = channel.getIdLong();

			// delete the old message
			if (currentBgame.initiated)
				oldchannel.deleteMessageById(currentBgame.msgId).queue();

			// makes that it not always throws errors if noone has typed "?b" or "?button"
			currentBgame.initiated = true;

			// send the new button message
			channel.sendMessage("Current value: `"
					+ currentBgame.getValue() + "`\nAdd a reaction to this message and claim your points!")
					.queue((msg) -> {

						// updates the msgId of the game. Its kinda wierd because i can't the
						// currenBgame variable in this block.
						servers.get(event.getGuild().getIdLong()).msgId = msg.getIdLong();

						// adds the emote to the message
						msg.addReaction(emote).queue();
					});
		}

		if (content.equals(this.prefix + "buttonboard") || content.equals(this.prefix + "bb")) {
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
			eb.setDescription(
					"Top 10 Players of the button game.\nThe first player will win a smul reward at the end of this year.\n");

			for (int i = 0; i < 10; i++) {
				nickname = (jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id)
						.getNickname() != null)
								? jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id).getNickname()
								: jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id)
										.getEffectiveName();
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

		if (content.equals(this.prefix + "ba") && event.getAuthor().getAsTag().equals("Georg#3258")) {
			BgscoreUser[] toplist = printBest(currentBgame.serverId);
			String nickname = "";
			String users = "";
			String scores = "";

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("ButtonBoard", null);
			eb.setColor(1);

			for (int i = 0; i < toplist.length; i++) {
				nickname = (jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id)
						.getNickname() != null)
								? jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id).getNickname()
								: jda.getGuildById(event.getGuild().getId()).getMemberById(toplist[i].id)
										.getEffectiveName();
				users = users + nickname + ":\n";
				scores = scores + toplist[i].bgscore + "\n";
			}
			eb.addField("Player: ", users, true);
			eb.addField("Score: ", scores, true);
			nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
			channel.sendMessage(eb.build()).queue();
		}

		if (content.equals(this.prefix + "bs")) {
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
		if (content.startsWith(this.prefix + "bs ")) {
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
		if (content.equals(this.prefix + "nrofgames")) {
			channel.sendMessage("" + servers.size() + "games that i'm aware of.").queue();
		}
	}

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

	// claim pointes

	public void claim(MessageReactionAddEvent event, JDA jda) {

		// get the game from the correct guild
		ButtonGame currentBgame = servers.get(event.getGuild().getIdLong());

		// if the game for the guild does not exist create an instance of the game and
		// add it to the HashMap
		if (currentBgame == null) {
			currentBgame = new ButtonGame(event.getMessageIdLong(), event.getChannel().getIdLong(),
					event.getGuild().getIdLong());
			servers.put(event.getGuild().getIdLong(), currentBgame);
		}

		if (currentBgame.msgId == event.getMessageIdLong() && !event.getUser().isBot()) {
			MessageChannel channel = jda.getTextChannelById(currentBgame.channelId);

			// writeScore returns true if the current value of the button is higher than the
			// score of the one who pressed the button
			if (writeScore(event.getUserIdLong(), event.getGuild().getIdLong(), currentBgame)) {
				channel.editMessageById(currentBgame.msgId,
						event.getUser().getAsMention() + " has claimed " + currentBgame.getValue() + " points.")
						.queue();
				channel.sendMessage("Current value: `10" + "`\nAdd a reaction to this message and claim your points!")
						.queue((msg) -> {
							servers.get(event.getGuild().getIdLong()).msgId = msg.getIdLong();
							msg.addReaction(emote).queue();
						});
			}
		}
	}

//write the score to the json fil
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
}