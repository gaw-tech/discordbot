package commands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import bot.Bot;
import bot.MainListener;
import bot.Config;
import bot.ConfigType;
import bot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

public class OtherCommands implements Module {
	String myID = Bot.myID;
	String topname = "Other";
	String out = "";
	LinkedList<String> yoinked = new LinkedList<>();

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
		if (content.equals(prefix + "time")) {
			channel.sendMessage("It is: <t:" + System.currentTimeMillis() / 1000 + ">").queue();
			message.delete().queue();
		}
		// source cmd
		if (content.equals(prefix + "source")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setDescription("Repo of <@381154302720213002>'s bot.");
			eb.addField("Link:", "https://github.com/gaw-tech/discordbot", true);
			channel.sendMessageEmbeds(eb.build()).queue();
		}
		// owner space
		if (event.getAuthor().getId().equals(myID)) {
			// send a pm to someone
			if (content.startsWith(prefix + "pm ")) {
				message.delete().queue();
				content = content.substring(prefix.length() + "pm ".length());
				String uid = content.split(" ")[0];
				content = content.substring(uid.length() + 1);
				MessageChannel cnl = event.getJDA().getUserById(uid).openPrivateChannel().complete();
				cnl.sendMessage(content).queue();
				return;
			}
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
				out = "" + event.getJDA().getGuilds().size() + "\n";
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
				if (msg != null && msg.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
					msg.delete().queue();
				}
			}
			// block reactions
			if (content.startsWith(prefix + "yoink ")) {
				message.delete().queue();
				yoinked.add(content.split(" ")[1]);
			}
			// get a module to display
			if (content.startsWith(prefix + "getmodule ")) {
				File file = new File(Bot.path + "/commands/" + content.split(" ")[1] + ".java");
				channel.sendFile(file).queue();
			}
			// get file
			if (content.startsWith(prefix + "getfile ")) {
				File file = new File(content.substring(prefix.length() + "getfile ".length()));
				channel.sendFile(file).queue();
			}
			// load a module from discord
			if (content.startsWith(prefix + "dload ")) {
				try {
					message.delete().queue();
					Attachment atch = message.getAttachments().get(0);
					if (atch.getFileExtension().equals("java") || atch.getFileExtension().equals("txt")) {
						String dataname = content.substring(prefix.length()).split(" ")[1];
						atch.downloadToFile(new File(Bot.path + "/commands/" + dataname + ".java")).get();
						switch (MainListener.load(dataname)) {
						case -1: {
							channel.sendMessage("Loading failed.").queue();
							break;
						}
						case 0: {
							channel.sendMessage(dataname + " is already loaded.").queue();
							break;
						}
						case 1: {
							channel.sendMessage(dataname + " was loaded.").queue();
							break;
						}
						}
					}
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// check slash commands
			if (content.equals(prefix + "other listslash")) {
				String out = "Guild commands:\n";
				List<Command> commands = event.getGuild().retrieveCommands().complete();
				for (Command cmd : commands) {
					out += "-" + cmd.getName() + "\n";
				}
				out += "Global commands:\n";
				commands = event.getJDA().retrieveCommands().complete();
				for (Command cmd : commands) {
					out += "-" + cmd.getName() + " " + cmd.getId() + "\n";
				}
				channel.sendMessage((out.length() == 0) ? "I found no slash commands" : out).queue();
			}
			// remove global command
			if (content.startsWith(prefix + "other rcg ")) {
				message.delete().queue();
				content = content.substring(prefix.length());
				String command_id = content.split(" ")[2];
				Command command = event.getJDA().retrieveCommandById(command_id).complete();
				event.getJDA().deleteCommandById(command_id).queue();
				channel.sendMessage("Deleted global command: " + command.getName()).queue();
			}
			// remove all guild commands on all guilds
			if (content.equals(prefix + "other dagc")) {
				for (Guild g : event.getJDA().getGuilds()) {
					try {
						List<Command> commands = g.retrieveCommands().complete();
						if (commands != null) {
							for (Command c : g.retrieveCommands().complete()) {
								System.out.println("Deleting " + c.getName());
								c.delete().queue();
							}
						} else {
							System.out.println(g.getName() + " could not retrieve any slash commands");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// set channels where slash commands are allowed
			if (content.startsWith(prefix + "other allow slash ")) {
				List<TextChannel> channels = message.getMentionedChannels();
				String data = "{";
				for (TextChannel c : channels) {
					data += "\"" + c.getId() + "\",";
				}
				Config.setLine(ConfigType.ARRAY_STRING, "slash_channels", data + "}");

				try {
					Config.save();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				channel.sendMessage("Set the channels for slash commands.").queue(msg -> {
					msg.delete().queueAfter(15, TimeUnit.SECONDS);
				});
			}
			// list event listeners
			if (content.equals(prefix + "other listeners")) {
				channel.sendMessage(jda.getRegisteredListeners() + "").queue();
			}
			// remove an event listener... i really hope it retruns them always in the same
			// order.
			if (content.startsWith(prefix + "other remove listener ")) {
				content = content.substring(prefix.length() + "other remove listener ".length());
				int i = Integer.parseInt(content);
				jda.removeEventListener(jda.getRegisteredListeners().get(i));
				channel.sendMessage("yeetet listener").queue();
			}
			// empty queued messages and stuff
			if (content.equals(prefix + "other clear queue")) {
				jda.cancelRequests();
				channel.sendMessage("cleared queue?").queue();
			}

			if (content.startsWith(prefix + "spam ")) {
				message.delete().queue();
				content = content.substring(prefix.length() + "spam ".length());
				int n = Integer.parseInt(content.split(" ")[0]);
				content = content.substring(content.split(" ")[0].length());
				for (int i = 0; i < n; i++) {
					channel.sendMessage(content).queue();
				}
			}
		}
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
		Message msg = null;
		if (yoinked.contains(event.getUser().getId())) {
			msg = event.retrieveMessage().complete();
		}
		if (msg == null)
			return;
		if (msg.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())
				|| msg.getAuthor().getId().equals(myID)) {
			msg.removeReaction(event.getReactionEmote().getEmote(), event.retrieveUser().complete()).queue();
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
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help Other");
		eb.addField("Description:", "Just some random commands!", true);
		eb.addField(
				"Usage:", "`" + prefix + "ping` returns pong!\n`" + prefix
						+ "source` view the repo with the source code of <@" + Bot.jda.getSelfUser().getId() + ">. ",
				true);
		return eb;
	}

	@Override
	public Field get_basic_help() {
		return new Field(topname, "`" + prefix + "ping` pong!\n`" + prefix
				+ "source` view the repo with the source code of <@" + Bot.jda.getSelfUser().getId() + ">. ", true,
				true);
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
		short_commands.add("time");
		short_commands.add("nick");
		short_commands.add("sbar");
		short_commands.add("servers");
		short_commands.add("leaveguildforrealhellyeah");
		short_commands.add("purge");
		short_commands.add("delete");
		short_commands.add("yoink");
		short_commands.add("cleanlist");
		short_commands.add("getmodule");
		short_commands.add("getfile");
		short_commands.add("dload");
		short_commands.add("source");
		short_commands.add("pm");
		short_commands.add("spam");
		return short_commands;
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
	}

	@Override
	public HashMap<String, String> get_slash() {
		return null;
	}

	@Override
	public void run_button(ButtonClickEvent event) {
	}

	@Override
	public boolean has_button() {
		return false;
	}
}