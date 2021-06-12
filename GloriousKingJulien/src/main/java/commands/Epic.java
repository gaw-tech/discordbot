package commands;

import BetterBot.Module;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.joda.time.DateTime;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Epic extends ListenerAdapter implements Module {
	EmbedBuilder embed;
	int hourofday = 0;
	ArrayList<EPICdata> datalist = new ArrayList<>();
	String topname = "EPIC";

	@Override
	public void run_message(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = message.getChannel();
		if (content.equals(prefix + "epic")) {
			DateTime dt = new DateTime();
			if (embed == null || dt.getHourOfDay() != hourofday) {
				try {
					URL url;
					url = new URL("https://epic.gsfc.nasa.gov/api/natural");
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestProperty("accept", "application/json");
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer webcontent = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						webcontent.append(inputLine);
					}
					in.close();
					JsonArray parser = (JsonArray) Jsoner.deserialize(webcontent.toString());
					datalist = new ArrayList<>();
					parser.forEach(entity -> {
						datalist.add(new EPICdata((JsonObject) entity));
					});
					EPICdata latest = getByHour(dt.getHourOfDay());
					EmbedBuilder eb = latest.makeEmbed();
					channel.sendMessage(eb.build()).queue();
					embed = eb;
					hourofday = dt.getHourOfDay();

				} catch (IOException | JsonException e) {
					e.printStackTrace();
				}
			} else {
				channel.sendMessage(embed.build()).queue();
			}
		}
	}

	private EPICdata getByHour(int h) {
		if (datalist.isEmpty()) {
			return null;
		}
		EPICdata best = datalist.get(0);
		DateTime dt = new DateTime();
		int mindist = Integer.MAX_VALUE;
		for (EPICdata d : datalist) {
			if (d.hour == dt.getHourOfDay()) {
				return d;
			}
			if (Math.abs(dt.getHourOfDay() - d.hour) < mindist) {
				mindist = Math.abs(dt.getHourOfDay() - d.hour);
				best = d;
			}
		}
		return best;
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help EPIC");
		eb.addField("Description:",
				"EPIC is the acronym for \"Earth Polychromatic Imaging Camera\", which is onboard of the NOAA's DSCOVR spacecraft. You can find more"
						+ " info under: https://epic.gsfc.nasa.gov/about/epic",
				true);
		eb.addField("Usage:",
				"`" + prefix
						+ "epic` returns the latest avaliable picture at ahout the same time of the day from the EPIC",
				true);
		return eb;
	}

	@Override
	public Field get_basic_help() {
		return new Field(topname, "`" + prefix + "epic` returns an image of the EPIC from NASA", true, true);
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
		embed = null;
		hourofday = 0;
		datalist = null;
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

	@Override
	public void run_slash(SlashCommandEvent event) {
		if (event.getName().equals("epic")) {
			DateTime dt = new DateTime();
			if (embed == null || dt.getHourOfDay() != hourofday) {
				InteractionHook interactionHook = event.deferReply().complete();
				try {
					URL url;
					url = new URL("https://epic.gsfc.nasa.gov/api/natural");
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestProperty("accept", "application/json");
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer webcontent = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						webcontent.append(inputLine);
					}
					in.close();
					JsonArray parser = (JsonArray) Jsoner.deserialize(webcontent.toString());
					datalist = new ArrayList<>();
					parser.forEach(entity -> {
						datalist.add(new EPICdata((JsonObject) entity));
					});
					EPICdata latest = getByHour(dt.getHourOfDay());
					EmbedBuilder eb = latest.makeEmbed();
					event.replyEmbeds(eb.build()).queue();
					embed = eb;
					hourofday = dt.getHourOfDay();
					interactionHook.editOriginalEmbeds(eb.build()).queue();
				} catch (IOException | JsonException e) {
					e.printStackTrace();
				}
			} else {
				event.replyEmbeds(embed.build()).queue();
			}
		}
	}

	@Override
	public HashMap<String, String> get_slash() {
		HashMap<String, String> slash_commands = new HashMap<>();
		slash_commands.put("epic", "Get an image from the NASA EPIC.");
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

class EPICdata {

	String hdurl;
	String caption;
	String time;
	String[] year;
	String image;
	public int hour;

	public EPICdata() {
	}

	public EPICdata(JsonObject latest) {
		this.caption = (String) latest.get("caption");
		String[] date = ((String) latest.get("date")).split(" ");
		this.time = (String) latest.get("date");
		this.year = date[0].split("-");
		String[] time = date[1].split(":");
		this.hour = Integer.parseInt(time[0]);
		this.image = (String) latest.get("image");
		this.hdurl = "https://epic.gsfc.nasa.gov/archive/natural/" + year[0] + "/" + year[1] + "/" + year[2] + "/png/"
				+ image + ".png";
	}

	public EmbedBuilder makeEmbed() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor("Earth Polychromatic Imaging Camera", "https://epic.gsfc.nasa.gov/",
				"https://epic.gsfc.nasa.gov/contents/assets/logo.png");
		eb.addField("Caption", caption + "\n" + time, true);
		eb.setImage(hdurl);
		eb.setFooter("NASA APIs", "https://api.nasa.gov/assets/img/favicons/favicon-192.png");
		return eb;
	}

}
