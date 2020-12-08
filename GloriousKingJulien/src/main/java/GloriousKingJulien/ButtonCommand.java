package GloriousKingJulien;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	ButtonGame bgame = new ButtonGame();
	long channelId;
	long messageId;
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
		// getContentRaw() is an atomic getter
		// getContentDisplay() is a lazy getter which modifies the content for e.g.
		// console view (strip discord formatting)
		if (content.equals(this.prefix + "bg")) {
			channel.sendMessage("Value: " + bgame.getValue()).queue(); // Important to call .queue() on the RestAction
																		// returned by
			// sendMessage(...)
		}
		if (content.startsWith(this.prefix + "setbscore ") && event.getAuthor().getAsTag().equals("Georg#3258")) {
			event.getMessage().delete().queue();
			int value = Integer.parseInt(content.replaceAll("[\\D]", ""));
			bgame.setValue(value);
		}
		
		if (content.equals(this.prefix + "button") || content.equals(this.prefix + "b")) {
			event.getMessage().delete().queue();
			channelId = channel.getIdLong();
			channel.deleteMessageById(messageId).queue();
			channel.sendMessage(
					"Current value: `" + bgame.getValue() + "`\nAdd a reaction to this message and claim your points!")
					.queue((msg) -> {
						messageId = msg.getIdLong();
						bgame.startGame(messageId, this.channelId);
						msg.addReaction(emote).queue();
					});
		}

		if (content.equals(this.prefix + "buttonboard") || content.equals(this.prefix + "bb")) {
			BgscoreUser[] toplist = printBest();
			String nickname = "";
			String users = "";
			String scores = "";
			String rank = "";

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("ButtonBoard", null);
			eb.setColor(1);
			eb.setDescription(
					"Top 10 Players of the button game.\nThe first player will win a smol reward at the end of this year.\n");

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
			eb.addField("", "**Player:** \n" +users, true);
			eb.addField("", "**Score:** \n" + scores, true);
			nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
			channel.sendMessage(eb.build()).queue();
		}

		if (content.equals(this.prefix + "ba") && event.getAuthor().getAsTag().equals("Georg#3258")) {
			BgscoreUser[] toplist = printBest();
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
			BgscoreUser[] toplist = printBest();
			boolean founduser = false;
			int rank = 0;
			for(int i = 0; i < toplist.length;i++) {
				if(toplist[i].id == event.getAuthor().getIdLong()) {
					founduser =true;
					rank = i+1;
					break;
				}
			}

			if (!founduser) {
				channel.sendMessage("You have no score.").queue();
			} else {
				channel.sendMessage(
						event.getAuthor().getAsMention() + ", you have " + toplist[rank-1].bgscore + " points. You are nr. " + rank)
						.queue();
			}
		}

		// check for a score
		if (content.startsWith(this.prefix + "bs ")) {
			BgscoreUser[] toplist = printBest();
			List<Member> mentions = event.getMessage().getMentionedMembers();
			boolean founduser = false;
			int rank = 0;
			for(int i = 0; i < toplist.length;i++) {
				if(toplist[i].id == mentions.get(0).getIdLong()) {
					founduser =true;
					rank = i+1;
					break;
				}
			}
			
			if (!founduser) {
				channel.sendMessage(mentions.get(0).getAsMention() + " has no score.").queue();
			} else {
				channel.sendMessage(
						mentions.get(0).getAsMention() + ", has " + toplist[rank-1].bgscore + " points and is nr. " + rank)
						.queue();
			}
		}
	}

	// leaderboard
	private BgscoreUser[] printBest() {
		try {
			Reader reader = Files.newBufferedReader(Paths.get("buttonscores.json"));
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
		if (this.messageId == event.getMessageIdLong() && !event.getUser().isBot()) {
			MessageChannel channel = jda.getTextChannelById(channelId);
			if (writeScore(event.getUserIdLong())) {
				channel.editMessageById(this.messageId,
						event.getUser().getAsMention() + " has claimed " + bgame.getValue() + " points.").queue();
				channel.sendMessage("Current value: `10" + "`\nAdd a reaction to this message and claim your points!")
						.queue((msg) -> {
							messageId = msg.getIdLong();
							bgame.reStartGame(messageId, this.channelId);
							msg.addReaction(emote).queue();
						});
			} else {

			}
		}
	}

	// write the score to json file
	private boolean writeScore(long playerId) {
		try {
			this.player = null;
			Reader reader = Files.newBufferedReader(Paths.get("buttonscores.json"));
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
				BufferedWriter writer = Files.newBufferedWriter(Paths.get("buttonscores.json"));
				JsonObject player = new JsonObject();
				player.put("id", playerId);
				player.put("bgscore", bgame.getValue());
				users.add(player);
				parser.put("users", users);
				Jsoner.serialize(parser, writer);
				writer.close();
				return true;
			} else if (((BigDecimal) this.player.get("bgscore")).longValue() < bgame.getValue()) {
				BufferedWriter writer = Files.newBufferedWriter(Paths.get("buttonscores.json"));
				this.player.put("bgscore", bgame.getValue());
				parser.put("users", users);
				Jsoner.serialize(parser, writer);
				writer.close();
				return true;
			}
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("button bad file");
			e.printStackTrace();
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