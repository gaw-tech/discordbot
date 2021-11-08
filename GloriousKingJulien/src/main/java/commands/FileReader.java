package commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import bot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class FileReader implements Module {

	@Override
	public void run_message(MessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = event.getChannel();
		if (content.equals(prefix + "read")) {
			try {
				File file = message.getAttachments().get(0).downloadToFile().get();
				Scanner scanner = new Scanner(file);
				LinkedList<String> list = new LinkedList<>();
				while (scanner.hasNext()) {
					list.add(scanner.nextLine());
				}
				while (!list.isEmpty()) {
					Message m = channel.sendMessage(list.removeFirst()).complete();
					System.out.println(m);
				}
				channel.sendMessage(event.getJDA().getUserById(bot.Bot.myID).getAsMention() + " done").queue();
				scanner.close();
			} catch (InterruptedException | ExecutionException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
	}

	@Override
	public void run_button(ButtonClickEvent event) {
	}

	@Override
	public boolean has_reaction() {
		return false;
	}

	@Override
	public boolean has_slash() {
		return false;
	}

	@Override
	public boolean has_button() {
		return false;
	}

	@Override
	public boolean has_basic_help() {
		return false;
	}

	@Override
	public EmbedBuilder get_help() {
		return null;
	}

	@Override
	public HashMap<String, String> get_slash() {
		return null;
	}

	@Override
	public Field get_basic_help() {
		return null;
	}

	@Override
	public String get_topname() {
		return "FileReader";
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		short_commands.add("read");
		return short_commands;
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

}
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment
//uselesss coment