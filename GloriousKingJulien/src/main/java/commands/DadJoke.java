package commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import bot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DadJoke extends ListenerAdapter implements Module {
	String topname = "DadJoke";

	@Override
	public void run_message(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;
		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = message.getChannel();
		if (content.equals(prefix + "dj") || content.equals(prefix + "dadjoke")) {
			message.delete().queueAfter(1, TimeUnit.MINUTES);
			try {
				URL url;
				url = new URL("https://icanhazdadjoke.com/");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestProperty("accept", "application/json");
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer webcontent = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					webcontent.append(inputLine);
				}
				in.close();
				JsonObject parser = (JsonObject) Jsoner.deserialize(webcontent.toString());
				String joke = (String) parser.get("joke");
				channel.sendMessage(joke).queue();

			} catch (IOException | JsonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help Dad Jokes");
		eb.addField("Description:", "Dad jokes from: https://icanhazdadjoke.com/", true);
		eb.addField("Usage:", "`" + prefix + "dadjoke` returns a dad joke.", false);
		return eb;
	}

	@Override
	public Field get_basic_help() {
		return new Field(topname, "`" + prefix + "dj` returns a dad joke.", true, true);
	}

	@Override
	public boolean has_basic_help() {
		return false;
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public void unload() {
		topname = null;
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
	}

	@Override
	public boolean has_reaction() {
		return false;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		short_commands.add("dj");
		return short_commands;
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
		if (event.getName().equals("dadjoke")) {
			event.deferReply();
			try {
				URL url;
				url = new URL("https://icanhazdadjoke.com/");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestProperty("accept", "application/json");
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer webcontent = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					webcontent.append(inputLine);
				}
				in.close();
				JsonObject parser = (JsonObject) Jsoner.deserialize(webcontent.toString());
				String joke = (String) parser.get("joke");
				event.reply(joke).queue();

			} catch (IOException | JsonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public HashMap<String, String> get_slash() {
		HashMap<String, String> slash_commands = new HashMap<>();
		slash_commands.put("dadjoke", "A random dad joke.");
		return slash_commands;
	}

	@Override
	public boolean has_slash() {
		return true;
	}

	@Override
	public void run_button(ButtonClickEvent event) {
	}

	@Override
	public boolean has_button() {
		return false;
	}
}
