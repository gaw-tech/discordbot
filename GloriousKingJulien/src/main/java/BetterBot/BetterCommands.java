package BetterBot;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BetterCommands extends ListenerAdapter {
	String myId = BetterBot.myID;
	String prefix = BetterBot.prefix;
	HashMap<String, Vorlage> loaded = new HashMap<>();

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = message.getChannel();
		// help
		if (content.startsWith(prefix + "help") && !event.getAuthor().isBot()) {
			if (content.length() == 5) {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle(
						"Help from " + ((event.getGuild().getMember(event.getJDA().getSelfUser()).getNickname() == null)
								? event.getJDA().getSelfUser().getName()
								: event.getGuild().getMember(event.getJDA().getSelfUser()).getNickname()));
				eb.addField("Help", "`" + prefix + "help` for this message\n`" + prefix
						+ "help <topic>` for more detailed help of a command", true);
				for (String s : loaded.keySet()) {
					eb.addField(loaded.get(s).basic_help());
				}
				String nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
						: event.getMember().getEffectiveName();
				eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
				channel.sendMessage(eb.build()).queue();
			} else {
				String[] split_help = content.split(" ");
				String key = "";
				for (String s : loaded.keySet()) {
					if (loaded.get(s).gettopname().equalsIgnoreCase(split_help[1])) {
						key = s;
						break;
					}
				}
				if (key.length() == 0) {
					message.addReaction(":cringe:826487190183215205").queue(); // cringe emote id
					return;
				}
				EmbedBuilder eb = loaded.get(key).help();
				String nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
						: event.getMember().getEffectiveName();
				eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
				channel.sendMessage(eb.build()).queue();
			}
		}
		//owner only
		if (!event.getAuthor().getId().equals(myId))
			return;
		// LOAD command
		if (content.startsWith(prefix + "load ")) {
			try {
				String dataname = content.split(" ")[1];
				if (loaded.containsKey(dataname)) {
					channel.sendMessage(dataname + " is already loaded.").queue();
					return;
				}
				ClassLoader parentClassLoader = MyClassLoader.class.getClassLoader();
				MyClassLoader classLoader = new MyClassLoader(parentClassLoader);
				Vorlage instance = (Vorlage) classLoader.loadClass(dataname).getConstructor().newInstance();
				event.getJDA().addEventListener(instance);
				loaded.put(dataname, instance);
				channel.sendMessage(dataname + " was loaded.").queue();

			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException
					| NullPointerException e) {
				channel.sendMessage("Loading failed.").queue();
				e.printStackTrace();
			}
		}

		// RELOAD command
		if (content.startsWith(prefix + "reload ")) {
			try {
				String dataname = content.split(" ")[1];
				if (!loaded.containsKey(dataname)) {
					channel.sendMessage(dataname + " is not loaded").queue();
					return;
				}
				MyClassLoader newclass = new MyClassLoader(MyClassLoader.class.getClassLoader());
				Vorlage instance = (Vorlage) newclass.loadClass(dataname).getDeclaredConstructor().newInstance();
				event.getJDA().addEventListener(instance);
				event.getJDA().removeEventListener(loaded.get(dataname));
				loaded.get(dataname).unload();
				loaded.put(dataname, instance);
				channel.sendMessage(dataname + " was reloaded.").queue();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException
					| NullPointerException e) {
				channel.sendMessage("Reloading failed.").queue();
				e.printStackTrace();
			}
		}

		// YEET command
		if (content.startsWith(prefix + "yeet ")) {
			try {
				String dataname = content.split(" ")[1];
				if (!loaded.containsKey(dataname)) {
					channel.sendMessage(dataname + " is not loaded").queue();
					return;
				}
				event.getJDA().removeEventListener(loaded.get(dataname));
				loaded.get(dataname).unload();
				loaded.remove(dataname);
				channel.sendMessage(dataname + " was yeeted.").queue();
			} catch (NullPointerException e) {
				channel.sendMessage("Yeeting failed.").queue();
				e.printStackTrace();
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
	}
}