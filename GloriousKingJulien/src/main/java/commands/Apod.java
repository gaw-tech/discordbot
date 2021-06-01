package commands;

import BetterBot.Module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Map;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Apod extends ListenerAdapter implements Module {
	String topname = "apod";

	@Override
	public void run_message(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;
		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = message.getChannel();
		if (content.equals(prefix + "apod")) {
			try {
				URL url;
				url = new URL("https://api.nasa.gov/planetary/apod?api_key=" + BetterBot.BetterBot.nasaapikey);
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
				String explanation = (String) parser.get("explanation");
				String hdurl = (String) parser.get("hdurl");
				String title = (String) parser.get("title");
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle(title);
				if (explanation.length() > 1000) {
					explanation = explanation.substring(0, 1000) + "...";
				}
				eb.addField("Explanation", explanation, true);
				eb.setImage(hdurl);
				eb.setFooter("NASA APIs", "https://api.nasa.gov/assets/img/favicons/favicon-192.png");
				eb.setAuthor("Astronomy Picture Of the Day", "https://apod.nasa.gov/apod");
				channel.sendMessage(eb.build()).queue();

			} catch (IOException | JsonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help APOD");
		eb.addField("Description:",
				"You are fascinated by space? Then this is the right place. APOD stands for Astronomy Picture Of the Day. If you want to look at the webiste directly: https://apod.nasa.gov/apod",
				true);
		eb.addField("Usage:", "`" + prefix + "apod` returns todays astronomy picture of the day", false);
		return eb;
	}

	@Override
	public Field get_basic_help() {
		return new Field("APOD", "`" + prefix + "apod` returns the astronomy picture of the day", true, true);
	}

	@Override
	public boolean has_basic_help() {
		return true;
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
		return new LinkedList<String>();
	}
}

class ParameterStringBuilder {
	public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			result.append("&");
		}

		String resultString = result.toString();
		return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
	}
}