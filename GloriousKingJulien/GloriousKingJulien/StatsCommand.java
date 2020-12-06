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
		if (content.equals(this.prefix + "stats")) {
			MessageChannel channel = event.getChannel();
			String server = event.getGuild().getId();

			File file = new File(server + ".txt.jpg");

			DrawStats.drawStats(server);
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("Crappy graph!");
			eb.setDescription("Graph of how many messages were sent over time.");
			eb.setImage("attachment://stats.jpg");
			eb.setFooter("Summoned by: " + event.getMember().getNickname(), event.getAuthor().getAvatarUrl());

			channel.sendMessage(eb.build()).addFile(file, "stats.jpg").queue();
		}
	}

}