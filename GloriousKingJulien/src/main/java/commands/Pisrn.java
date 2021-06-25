package commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import BetterBot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Pisrn implements Module {
	String topname = "PISRN";

	@Override
	public void run_message(MessageReceivedEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		String content = message.getContentRaw();
		if (content.equals(prefix + "pisrn")) {
			try {
				URL url;
				url = new URL("http://api.open-notify.org/astros.json");
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
				String number = parser.get("number").toString();
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle("People in Space Right Now");
				eb.setDescription("There are " + number + " people in space right now.");
				String nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
						: event.getMember().getEffectiveName();
				eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
				channel.sendMessage(eb.build()).queue();
			} catch (Exception | Error e) {
				// TODO
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
		InteractionHook ih = event.deferReply().complete();
		try {
			URL url;
			url = new URL("http://api.open-notify.org/astros.json");
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
			String number = parser.get("number").toString();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("People in Space Right Now");
			eb.setDescription("There are " + number + " people in space right now.");
			String nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			eb.setFooter("Summoned by: " + nickname, event.getUser().getAvatarUrl());
			ih.editOriginalEmbeds(eb.build()).queue();
		} catch (Exception | Error e) {
			ih.editOriginal("Sorry, but i failed.").queue(msg -> {
				msg.delete().queueAfter(15, TimeUnit.SECONDS);
			});
			e.printStackTrace();
		}

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
		return true;
	}

	@Override
	public boolean has_button() {
		return false;
	}

	@Override
	public boolean has_basic_help() {
		return true;
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help PISRN");
		eb.addField("Description:", "PISRN is the acronym for \"People in space right now\".", true);
		eb.addField("Usage:", "`" + prefix + "pisrn` returns the number of people who are in space right now.", true);
		return eb;
	}

	@Override
	public HashMap<String, String> get_slash() {
		HashMap<String, String> slash_commands = new HashMap<>();
		slash_commands.put("pisrn", "Tells you how many people are in space right now.");
		return slash_commands;
	}

	@Override
	public Field get_basic_help() {
		return new Field(topname, "`" + prefix + "pisrn` returns the number of people who are in space right now.", true,
				true);
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		return new LinkedList<String>();
	}

	@Override
	public void unload() {
		topname = null;
	}

}
