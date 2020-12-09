package GloriousKingJulien;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OtherCommands {
	char prefix = MyBot.prefix;

	public void run(MessageReceivedEvent event, JDA jda) {
		if (event.getAuthor().isBot())
			return;
		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();

		if (content.equals(this.prefix + "servers")) {
			MessageChannel channel = event.getChannel();

			List<Guild> guilds = jda.getGuilds();
			String msg = "";
			
			for(Guild guild : guilds) {
				msg = msg + guild.getName() + "\n";
			}

			channel.sendMessage(msg).queue();
		}
	}

}