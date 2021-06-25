package BetterBot;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

public class BetterCommands extends ListenerAdapter {
	String myId = BetterBot.myID;
	String prefix = BetterBot.prefix;
	private static HashMap<String, Module> loaded = new HashMap<>();
	private static LinkedList<Field> basic_help_fields = new LinkedList<>();
	private static HashMap<String, Module> short_commands = new HashMap<>();
	private static LinkedList<Module> reaction_modules = new LinkedList<>();
	private static HashMap<String, Module> slash_command_modules = new HashMap<>();
	private static LinkedList<Module> button_modules = new LinkedList<>();
	// private static HashMap<String, String> slash_command_ids = new HashMap<>();
	private static HashMap<String, HashMap<String, String>> slash_command_ids_by_guilds = new HashMap<>(); // guildid
																											// cmd cmdid
	static ArrayList<String> slash_channels;
	static JDA jda;

	BetterCommands(JDA jda) {
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BetterCommands.jda = jda;
		for (Guild g : jda.getGuilds()) {
			for (Command c : g.retrieveCommands().complete()) {
				c.delete().queue();
			}
		}
		slash_channels = Config.get("slash_channels").readStringArray();
	}

	public static int load(String modulename) {
		try {
			if (loaded.containsKey(modulename)) {
				return 0;
			}
			ClassLoader parentClassLoader = MyClassLoader.class.getClassLoader();
			MyClassLoader classLoader = new MyClassLoader(parentClassLoader);
			Module instance = (Module) classLoader.loadClass(modulename).getConstructor().newInstance();
			if (instance.has_basic_help()) {
				basic_help_fields.add(instance.get_basic_help());
			}
			if (instance.has_reaction()) {
				reaction_modules.add(instance);
			}
			if (instance.has_slash()) {
				for (String cmd : instance.get_slash().keySet()) {
					slash_command_modules.put(cmd, instance);
					for (Guild g : jda.getGuilds()) {
						HashMap<String, String> command_id = (!slash_command_ids_by_guilds.containsKey(g.getId()))
								? new HashMap<>()
								: (slash_command_ids_by_guilds.get(g.getId()) == null) ? new HashMap<>()
										: slash_command_ids_by_guilds.get(g.getId());
						g.upsertCommand(cmd, instance.get_slash().get(cmd)).queue(command -> {
							command_id.put(cmd, command.getId());
						});

					}
				}
			}
			if (instance.has_button()) {
				button_modules.add(instance);
			}
			for (String cmd : instance.get_short_commands()) {
				short_commands.put(cmd, instance);
			}
			loaded.put(modulename, instance);
			return 1;
		} catch (Exception | Error e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static int unload(String modulename) {
		try {
			if (!loaded.containsKey(modulename)) {
				return 0;
			}
			Module instance = loaded.get(modulename);
			for (String cmd : instance.get_short_commands()) {
				short_commands.remove(cmd);
			}
			if (instance.has_slash()) {
				for (Guild g : jda.getGuilds()) {
					if (slash_command_ids_by_guilds.containsKey(g.getId())) {
						HashMap<String, String> command_id = slash_command_ids_by_guilds.get(g.getId());
						for (String cmd : instance.get_slash().keySet()) {
							g.deleteCommandById(command_id.get(cmd)).queue();
							command_id.remove(cmd);
						}
					}
				}
				for (String cmd : instance.get_slash().keySet()) {
					slash_command_modules.remove(cmd);
				}
			}
			loaded.remove(modulename);
			reload_basic_help_fields();
			reload_reaction_modules();
			reload_button_modules();
			instance.unload();
			return 1;
		} catch (Exception | Error e) {
			e.printStackTrace();
			return -1;
		}

	}

	private static void reload_button_modules() {
		button_modules = new LinkedList<>();
		for (String key : loaded.keySet()) {
			Module module = loaded.get(key);
			if (module.has_button()) {
				button_modules.add(module);
			}
		}
	}

	private static void reload_basic_help_fields() {
		basic_help_fields = new LinkedList<>();
		for (String key : loaded.keySet()) {
			Module module = loaded.get(key);
			if (module.has_basic_help()) {
				basic_help_fields.add(module.get_basic_help());
			}
		}
	}

	private static void reload_reaction_modules() {
		reaction_modules = new LinkedList<>();
		for (String key : loaded.keySet()) {
			Module module = loaded.get(key);
			if (module.has_reaction()) {
				reaction_modules.add(module);
			}
		}
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = message.getChannel();
		if (content.startsWith(prefix)) {
			String first_argument = (content.substring(prefix.length()).contains(" "))
					? content.substring(prefix.length()).split(" ")[0]
					: content.substring(prefix.length());
			for (String module : loaded.keySet()) {
				if (loaded.get(module).get_topname().equalsIgnoreCase(first_argument)) {
					loaded.get(module).run_message(event);
				}
			}
			for (String cmd : short_commands.keySet()) {
				if (cmd.equalsIgnoreCase(first_argument)) {
					short_commands.get(cmd).run_message(event);
				}
			}
			// help
			if (content.startsWith(prefix + "help") && !event.getAuthor().isBot()) {
				if (content.length() == 4 + prefix.length()) {
					EmbedBuilder eb = new EmbedBuilder();
					eb.setTitle("Help from "
							+ ((event.getGuild().getMember(event.getJDA().getSelfUser()).getNickname() == null)
									? event.getJDA().getSelfUser().getName()
									: event.getGuild().getMember(event.getJDA().getSelfUser()).getNickname()));
					eb.addField("Help", "`" + prefix + "help` for this message\n`" + prefix
							+ "help <topic>` for more detailed help of a command", true);
					for (Entry<String, Module> entry : loaded.entrySet()) {
						eb.addField(entry.getValue().get_basic_help());
					}
					String nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
							: event.getMember().getEffectiveName();
					eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
					channel.sendMessage(eb.build()).queue();
				} else {
					String[] split_help = content.split(" ");
					String key = "";
					for (String s : loaded.keySet()) {
						if (loaded.get(s).get_topname().equalsIgnoreCase(split_help[1])) {
							key = s;
							break;
						}
					}
					if (key.length() == 0) {
						message.addReaction(":cringe:826487190183215205").queue(); // cringe emote id
						return;
					}
					EmbedBuilder eb = loaded.get(key).get_help();
					String nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
							: event.getMember().getEffectiveName();
					eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
					channel.sendMessage(eb.build()).queue();
				}
			}
			// owner only
			if (!event.getAuthor().getId().equals(myId))
				return;
			// LOAD command
			if (content.startsWith(prefix + "load ")) {
				String dataname = content.split(" ")[1];
				switch (load(dataname)) {
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
			// RELOAD command
			if (content.startsWith(prefix + "reload ")) {
				String dataname = content.split(" ")[1];
				switch (unload(dataname)) {
				case -1: {
					channel.sendMessage("Unloading failed.").queue();
					break;
				}
				case 0: {
					channel.sendMessage(dataname + " is not loaded.").queue();
					break;
				}
				case 1: {
					switch (load(dataname)) {
					case -1: {
						channel.sendMessage("Reloading failed.").queue();
						break;
					}
					case 0: {
						channel.sendMessage(dataname + " is already loaded. Which should not be possible. Yikes.")
								.queue();
						break;
					}
					case 1: {
						channel.sendMessage(dataname + " was reloaded.").queue();
						break;
					}
					}
				}
				}
			}

			// YEET command
			if (content.startsWith(prefix + "yeet ") || content.startsWith(prefix + "unload ")) {
				String dataname = content.split(" ")[1];
				switch (unload(dataname)) {
				case -1: {
					channel.sendMessage("Loading failed.").queue();
					break;
				}
				case 0: {
					channel.sendMessage(dataname + " is already loaded.").queue();
					break;
				}
				case 1: {
					channel.sendMessage(dataname + " was unloaded.").queue();
					break;
				}
				}
			}

			// list the loaded classes
			if (content.equals(prefix + "list")) {
				String out = "";
				for (String i : loaded.keySet()) {
					out = out + i + "\n";
				}
				channel.sendMessage("Loaded modules:\n" + out).queue();
			}

			// reload stuff conected to config
			if (content.equals(prefix + "config reload")) {
				message.delete().queue();
				try {
					Config.load();
				} catch (FileNotFoundException e) {
					channel.sendMessage("Configs were not reloaded").queue();
					e.printStackTrace();
					return;
				}
				slash_channels = Config.get("slash_channels").readStringArray();
				channel.sendMessage("Config reloaded.").queue(msg ->{
					msg.delete().queueAfter(15, TimeUnit.SECONDS);
				});
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		for (Module module : reaction_modules) {
			module.run_reaction(event);
		}
	}

	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		if (slash_channels.contains(event.getChannel().getId())) {
			slash_command_modules.get(event.getName()).run_slash(event);
		} else {
			event.reply("Sorry but i cant respond to you in this channel.").setEphemeral(true).queue();
		}
	}

	@Override
	public void onButtonClick(ButtonClickEvent event) {
		for (Module module : button_modules) {
			module.run_button(event);
		}
	}
}
