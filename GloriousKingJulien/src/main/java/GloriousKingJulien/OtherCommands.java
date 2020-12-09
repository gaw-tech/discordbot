package GloriousKingJulien;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OtherCommands {
	char prefix = MyBot.prefix;
	String spammsg;
	boolean spamming = false;

	public void run(MessageReceivedEvent event, JDA jda) {
		if (event.getAuthor().isBot())
			return;
		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = event.getChannel();

		if (content.equals(this.prefix + "servers")) {

			List<Guild> guilds = jda.getGuilds();
			String msg = "";

			for (Guild guild : guilds) {
				msg = msg + guild.getName() + "\n";
			}

			channel.sendMessage(msg).queue();
		}

		// spam command
		if (content.startsWith(prefix + "spam ")) {
			spammsg = content.substring(6);
			spamming = true;
			spam(spammsg, channel);
		}
	}

	void spam(String spammsg, MessageChannel channel) {
		if (spamming) {
			channel.sendMessage(spammsg).queue(response -> {
				spam(this.spammsg, response.getChannel());
			});
		}
	}
}
