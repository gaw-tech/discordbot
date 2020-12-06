package GloriousKingJulien;

import java.io.File;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StatsCommand {
	char prefix = MyBot.prefix;

	public void run(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;
		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();
		// getContentRaw() is an atomic getter
		// getContentDisplay() is a lazy getter which modifies the content for e.g.
		// console view (strip discord formatting)
		if (content.startsWith(this.prefix + "stats")) {
			MessageChannel channel = event.getChannel();
			String server = event.getGuild().getId();

			File file = new File(server + ".txt.jpg");

			String graphdescription;
			
			if (content.contains("all")) {
				DrawStats.drawStats(server);
				graphdescription = "over time.";
			} else {
				int minutes = Integer.parseInt(content.replaceAll("[\\D]", ""));
				//it looks ugly if it are to few minutes
				if (minutes < 5)
					return;
				DrawStats.drawLastMinutesStats(server, minutes);
				graphdescription = "in the last " + minutes + " minutes.";
			}

			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Crappy graph!");
			eb.setDescription("Graph of how many messages were sent " + graphdescription);
			eb.setImage("attachment://stats.jpg");

			String nickname = (event.getMember().getNickname() != null) ? event.getMember().getNickname()
					: event.getMember().getEffectiveName();
			eb.setFooter("Summoned by: " + nickname, event.getAuthor().getAvatarUrl());
			channel.sendMessage(eb.build()).addFile(file, "stats.jpg").queue();
		}
	}

}