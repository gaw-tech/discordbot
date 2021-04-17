package commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import BetterBot.Vorlage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Neo extends ListenerAdapter implements Vorlage {
	String topname = "neo";
	LinkedList<NeoData> data = new LinkedList<>();

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = event.getChannel();
		if (content.equals(prefix + "neo")) {
			if (data.isEmpty()) {
				try {
					URL url;
					url = new URL("https://ssd-api.jpl.nasa.gov/cad.api?diameter=true&dist-max=0.01");
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
					JsonArray data = (JsonArray) parser.get("data");
					data.forEach(dataobj -> {
						JsonArray dc = (JsonArray) dataobj;
						NeoData neo = new NeoData();
						neo.des = (String) dc.get(0);
						neo.orbit_id = Integer.parseInt((String) dc.get(1));
						neo.jd = Double.parseDouble((String) dc.getString(2));
						neo.cd = (String) dc.get(3);
						neo.dist = Double.parseDouble((String) dc.getString(4));
						neo.dist_min = Double.parseDouble((String) dc.getString(5));
						neo.dist_max = Double.parseDouble((String) dc.getString(6));
						neo.v_rel = Double.parseDouble((String) dc.getString(7));
						neo.v_inf = Double.parseDouble((String) dc.getString(8));
						neo.t_sigma_f = (String) dc.get(9);
						neo.h = Double.parseDouble((String) dc.getString(10));
						neo.diameter = (((String) dc.getString(11)) == null)?-1:Double.parseDouble((String) dc.getString(11));
						neo.diameter_sigma = (((String) dc.getString(12)) == null)?-1:Double.parseDouble((String) dc.getString(12));
						this.data.add(neo);
					});
				} catch (IOException | JsonException e) {
					e.printStackTrace();
				}
			}
			channel.sendMessage(this.data.removeFirst().toEmbed().build()).queue();			
		}
	}

	@Override
	public EmbedBuilder help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Todo");
		return eb;
	}

	@Override
	public Field basic_help() {
		return new Field("", "", true, true);
	}

	@Override
	public String gettopname() {
		return topname;
	}

	@Override
	public void unload() {
		topname = null;
	}

}

class NeoData {
	String des;
	int orbit_id;
	double jd;
	String cd;
	double dist;
	double dist_min;
	double dist_max;
	double v_rel;
	double v_inf;
	String t_sigma_f;
	double h;
	double diameter;
	double diameter_sigma;
	
	EmbedBuilder toEmbed() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(des);
		eb.addField("Data","orbit_id: " + orbit_id + "\nClose-approach time: " + cd + "\nDistance: " + (((long)(dist*149_597_870_700L))/1000.0) + " km\nDiameter: " +((diameter==-1)?"unknown":diameter + " km")  , false);
		return eb;
	}
}