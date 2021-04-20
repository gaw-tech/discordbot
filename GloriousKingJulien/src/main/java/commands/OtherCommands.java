package commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import BetterBot.Vorlage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OtherCommands extends ListenerAdapter implements Vorlage {
	String myID = BetterBot.BetterBot.myID;
	String topname = "other";
	String out = "";
	LinkedList<String> yoinked = new LinkedList<>();

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot() && !event.getAuthor().getId().equals("817846061347242026"))
			return;
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = message.getChannel();
		JDA jda = event.getJDA();

		if (content.equals(prefix + "ping")) {
			long time = System.currentTimeMillis();
			channel.sendMessage("Pong!").queue(response -> {
				response.editMessageFormat("Pong: `%d ms`", System.currentTimeMillis() - time).queue();
			});
		} else if (content.equals(prefix + "pong")) {
			long time = System.currentTimeMillis();
			channel.sendMessage("Ping!").queue(response -> {
				response.editMessageFormat("Ping: `%d ms`", System.currentTimeMillis() - time).queue();
			});
		}
		// owner space
		if (event.getAuthor().getId().equals(myID)) {
			// change nickname of bot
			if (content.startsWith(prefix + "nick ")) {
				String name = content.substring(6);
				event.getGuild().getMember(event.getJDA().getSelfUser()).modifyNickname(name).queue();
				message.delete().queue();
			}
			// change statusbar of bot
			if (content.startsWith(prefix + "sbar ")) {
				content = content.substring(6);

				if (content.startsWith("playing ")) {
					jda.getPresence().setActivity(Activity.playing(content.substring(8)));
				}
				if (content.startsWith("listening ")) {
					jda.getPresence().setActivity(Activity.listening(content.substring(10)));
				}
				if (content.startsWith("watching ")) {
					jda.getPresence().setActivity(Activity.watching(content.substring(9)));
				}
				event.getMessage().delete().queue();
			}
			// list servers
			if (content.equals(prefix + "servers")) {
				out = "";
				event.getJDA().getGuilds().forEach(guild -> {
					out = out + guild.getName() + " " + guild.getId() + "\n";
				});
				channel.sendMessage(out).queue();
			}
			// leave server
			if (content.startsWith(prefix + "leaveguildforrealhellyeah ")) {
				String guildid = content.split(" ")[1];
				event.getJDA().getGuildById(guildid).leave().queue();
				message.delete().queue();
			}
			// purge
			if (content.startsWith(prefix + "purge ")) {
				int count = Integer.parseInt(content.split(" ")[1]);
				LinkedList<String> dellist = new LinkedList<>();
				int max = 0;
				String lastid = message.getId();
				while (dellist.size() < count && max < 10) {
					max++;
					List<Message> ml = channel.getHistoryBefore(lastid, 100).complete().getRetrievedHistory();
					for (Message m : ml) {
						if (m.getAuthor().getId().equals(BetterBot.BetterBot.myID)) {
							dellist.addLast(m.getId());
						}
						lastid = m.getId();
					}
				}
				while (dellist.size() > count) {
					dellist.removeLast();
				}
				TextChannel cnl = (TextChannel) channel;
				cnl.deleteMessagesByIds(dellist).queue();
				message.delete().queue();
			}
			// delete message
			if (content.equals(prefix + "delete")) {
				Message msg = message.getReferencedMessage();
				message.delete().queue();
				System.out.println(msg);
				if (msg != null && msg.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
					msg.delete().queue();
				}
			}
			if (content.startsWith(prefix + "yoink ")) {
				message.delete().queue();
				yoinked.add(message.getMentionedUsers().get(0).getId());
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		Message msg = null;
		if (yoinked.contains(event.getUser().getId())) {
			msg = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
		}
		if (msg == null)
			return;
		if (msg.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())
				|| msg.getAuthor().getId().equals(BetterBot.BetterBot.myID)
						&& event.getReactionEmote().getName().contains("cavebob")) {
			msg.removeReaction(event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
		}
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help Other");
		eb.addField("Description:", "Just some random commands.", true);
		eb.addField("Usage:", "`" + prefix + "ping` returns pong", true);
		return eb;
	}

	@Override
	public Field basic_help() {
		return new Field("Other", "`" + prefix + "ping` pong", true, true);
	}

	@Override
	public String gettopname() {
		return topname;
	}

	@Override
	public void unload() {
		myID = null;
		topname = null;
		out = null;
		yoinked = null;
	}
}