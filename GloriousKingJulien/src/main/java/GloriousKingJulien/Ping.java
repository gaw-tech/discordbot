package GloriousKingJulien;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Ping {
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
		if (content.equals(this.prefix + "ping")) {
			MessageChannel channel = event.getChannel();
			long time = System.currentTimeMillis();
			channel.sendMessage("Pong!").queue(response -> {
				response.editMessageFormat("Pong: `%d ms`", System.currentTimeMillis() - time).queue();
			}); // Important to call .queue() on the RestAction returned by
			// sendMessage(...)
		} else if (content.equals(this.prefix + "pong")) {
			MessageChannel channel = event.getChannel();
			long time = System.currentTimeMillis();
			channel.sendMessage("Ping!").queue(response -> {
				response.editMessageFormat("Ping: `%d ms`", System.currentTimeMillis() - time).queue();
			});
		}
	}

}
