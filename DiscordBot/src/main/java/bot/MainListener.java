package bot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/*
 * The main listener. Implements a few basic commands and distributes events as needed.
 */
public class MainListener extends ListenerAdapter {
	/*
	 * TODO: Change how slash commands are treated.
	 */
	String myId = Bot.myID;
	String prefix = Bot.prefix;
	private static HashMap<String, Module> loaded = new HashMap<>();
	private static LinkedList<Field> basic_help_fields = new LinkedList<>();
	private static HashMap<String, Module> short_commands = new HashMap<>();
	private static LinkedList<Module> reaction_modules = new LinkedList<>();
	private static HashMap<String, Module> slash_command_modules = new HashMap<>();
	private static LinkedList<Module> button_modules = new LinkedList<>();
	private static HashMap<String, String> slash_command_ids = new HashMap<>(); // cmd cmdid
	static ArrayList<String> slash_channels;
	static JDA jda;

	MainListener(JDA jda) {
		MainListener.jda = jda;
		slash_channels = Config.get("slash_channels").readStringArray();
	}

	/*
	 * Loads the class with the name <modulename>. The class must implement the
	 * interface Module. Returns 1 if the class was successfully loaded 0 if the
	 * class already was loaded -1 if there was an error
	 */
	public static int load(String modulename) {
		try {
			if (loaded.containsKey(modulename)) {
				return 0;
			}
			MyClassLoader classLoader = new MyClassLoader();
			Module instance = (Module) classLoader.loadClass(modulename).getConstructor().newInstance();
			if (instance.has_reaction()) {
				reaction_modules.add(instance);
			}
			if (instance.has_button()) {
				button_modules.add(instance);
			}
			if (instance.get_basic_help() != null) {
				basic_help_fields.add(instance.get_basic_help());
			}
			if (instance.get_slash() != null) {
				for (String cmd : instance.get_slash().keySet()) {
					slash_command_modules.put(cmd, instance);
					jda.upsertCommand(cmd, instance.get_slash().get(cmd)).queue(command -> {
						slash_command_ids.put(cmd, command.getId());
					});

				}
			}
			if (instance.get_short_commands() != null) {
				for (String cmd : instance.get_short_commands()) {
					short_commands.put(cmd, instance);
				}
			}
			loaded.put(modulename, instance);
			updateSavedModules();
			return 1;
		} catch (Exception | Error e) {
			e.printStackTrace();
			return -1;
		}
	}

	/*
	 * Returns: -1 if there was an error while loading 0 if the module was not
	 * loaded 1 if the unloading was succesful
	 */
	public static int unload(String modulename) {
		try {
			if (!loaded.containsKey(modulename)) {
				return 0;
			}
			Module instance = loaded.get(modulename);
			if (instance.get_short_commands() != null) {
				for (String cmd : instance.get_short_commands()) {
					short_commands.remove(cmd);
				}
			}
			if (instance.get_slash() != null) {
				for (String cmd : instance.get_slash().keySet()) {
					jda.deleteCommandById(slash_command_ids.get(cmd)).queue();
					slash_command_ids.remove(cmd);
					slash_command_modules.remove(cmd);
				}

			}
			loaded.remove(modulename);
			reload_basic_help_fields();
			reload_reaction_modules();
			reload_button_modules();
			updateSavedModules();
			instance.unload();
			return 1;
		} catch (Exception | Error e) {
			e.printStackTrace();
			return -1;
		}

	}

	/*
	 * Updates the modules line in the config and saves it to the config.txt file.
	 */
	private static void updateSavedModules() {
		String data = "{";
		for (String m : loaded.keySet()) {
			data += "\"" + m + "\",";
		}
		Config.setLine(ConfigType.ARRAY_STRING, "modules", data + "}");
		try {
			Config.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Makes sure that the list of modules implementing buttons matches the loaded
	 * modules.
	 */
	private static void reload_button_modules() {
		button_modules = new LinkedList<>();
		for (Module module : loaded.values()) {
			if (module.has_button()) {
				button_modules.add(module);
			}
		}
	}

	/*
	 * Makes sure that the list of modules with basic help fields matches the loaded
	 * modules.
	 */
	private static void reload_basic_help_fields() {
		basic_help_fields = new LinkedList<>();
		for (Module module : loaded.values()) {
			if (module.get_basic_help() != null) {
				basic_help_fields.add(module.get_basic_help());
			}
		}
	}

	/*
	 * Makes sure that the list of modules which need reaction events matches the
	 * loaded modules.
	 */
	private static void reload_reaction_modules() {
		reaction_modules = new LinkedList<>();
		for (String key : loaded.keySet()) {
			Module module = loaded.get(key);
			if (module.has_reaction()) {
				reaction_modules.add(module);
			}
		}
	}

	/*
	 * The main message listener.
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		// We do not want to interact with any bot.
		if (event.getAuthor().isBot()) {
			return;
		}
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = message.getChannel();
		if (content.startsWith(prefix)) {
			// input distribution to the modules
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
					channel.sendMessageEmbeds(eb.build()).queue();
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
					channel.sendMessageEmbeds(eb.build()).queue();
				}
			}
			// Bot functionality that only is important for the bot owner.
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
			// RELOAD command to reload a module
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

			// UNLOAD command
			if (content.startsWith(prefix + "unload ")) {
				String dataname = content.split(" ")[1];
				switch (unload(dataname)) {
				case -1: {
					channel.sendMessage("Loading failed.").queue();
					break;
				}
				case 0: {
					channel.sendMessage(dataname + " is not loaded.").queue();
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
				channel.sendMessage("Config reloaded.").queue(msg -> {
					msg.delete().queueAfter(15, TimeUnit.SECONDS);
				});
			}
		}
	}

	/*
	 * Main reaction listener.
	 */
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		for (Module module : reaction_modules) {
			module.run_reaction(event);
		}
	}

	/*
	 * Main slash command listener.
	 */
	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		if (slash_channels.contains(event.getChannel().getId())) {
			slash_command_modules.get(event.getName()).run_slash(event);
		} else {
			event.reply("Sorry but i cant respond to you in this channel.").setEphemeral(true).queue();
		}
	}

	/*
	 * Main button press listener.
	 */
	@Override
	public void onButtonClick(ButtonClickEvent event) {
		for (Module module : button_modules) {
			module.run_button(event);
		}
	}
}