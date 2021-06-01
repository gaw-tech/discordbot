package commands;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import BetterBot.BetterBot;
import BetterBot.BetterCommands;
import BetterBot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class OtherCommands implements Module {
	String myID = BetterBot.myID;
	String topname = "other";
	String out = "";
	LinkedList<String> yoinked = new LinkedList<>();
	LinkedList<String> cleanlist = new LinkedList<>();

	@Override
	public void run_message(MessageReceivedEvent event) {
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
				message.delete().queue();
				int count = Integer.parseInt(content.split(" ")[1]);
				LinkedList<String> dellist = new LinkedList<>();
				int max = 0;
				String lastid = message.getId();
				while (dellist.size() < count && max < 10) {
					max++;
					List<Message> ml = channel.getHistoryBefore(lastid, 100).complete().getRetrievedHistory();
					for (Message m : ml) {
						if (m.getAuthor().getId().equals(myID)) {
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
			// block reactions
			if (content.startsWith(prefix + "yoink ")) {
				message.delete().queue();
				yoinked.add(content.split(" ")[1]);
			}
			// cleanlist handeling
			if (content.startsWith(prefix + "cleanlist add ")) {
				content = content.substring(prefix.length() + 14).toLowerCase();
				cleanlist.add(content);
				channel.sendMessage("Added `" + content + "` to the list.").queue();
			}
			// cleanlist cleaner
			if (cleanlist.contains(content.toLowerCase())) {
				message.delete().queue();
			}
			// get a module to display
			if (content.startsWith(prefix + "getmodule ")) {
				File file = new File("/home/pi/commands/" + content.split(" ")[1] + ".java");
				channel.sendFile(file).queue();
			}
			// load a module from discord
			if (content.startsWith(prefix + "dload ")) {
				try {
					Attachment atch = message.getAttachments().get(0);
					if (atch.getFileExtension().equals("java") || atch.getFileExtension().equals("txt")) {
						String dataname = content.substring(prefix.length()).split(" ")[1];
						File file = atch.downloadToFile(new File("/home/pi/commands/" + dataname+".java")).get();
						switch (BetterCommands.load(dataname)) {
						case -1: {
							channel.sendMessage("Loading failed.").queue();
						}
						case 0: {
							channel.sendMessage(dataname + " is already loaded.").queue();
						}
						case 1: {
							channel.sendMessage(dataname + " was loaded.").queue();
						}
						}
					}
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
		Message msg = null;
		if (yoinked.contains(event.getUser().getId())) {
			msg = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
		}
		if (msg == null)
			return;
		if (msg.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())
				|| msg.getAuthor().getId().equals(myID)
						&& event.getReactionEmote().getName().contains("cavebob")) {
			msg.removeReaction(event.getReaction().getReactionEmote().getEmote(), event.getUser()).queue();
		}
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public void unload() {
		myID = null;
		topname = null;
		out = null;
		yoinked = null;
		cleanlist = null;
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help Other");
		eb.addField("Description:", "Just some random commands.", true);
		eb.addField("Usage:", "`" + prefix + "ping` returns pong", true);
		return eb;
	}

	@Override
	public boolean has_basic_help() {
		return true;
	}

	@Override
	public Field get_basic_help() {
		return new Field("Other", "`" + prefix + "ping` pong", true, true);
	}

	@Override
	public boolean has_reaction() {
		return true;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		short_commands.add("ping");
		short_commands.add("pong");
		short_commands.add("nick");
		short_commands.add("sbar");
		short_commands.add("servers");
		short_commands.add("leaveguildforrealhellyeah");
		short_commands.add("purge");
		short_commands.add("delete");
		short_commands.add("yoink");
		short_commands.add("cleanlist");
		short_commands.add("getmodule");
		short_commands.add("dload");
		return short_commands;
	}
}