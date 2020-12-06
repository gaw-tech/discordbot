package GloriousKingJulien;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StatusBar {
	char prefix = MyBot.prefix;

	public void run(MessageReceivedEvent event, JDA jda) {

		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();
		// getContentRaw() is an atomic getter
		// getContentDisplay() is a lazy getter which modifies the content for e.g.
		// console view (strip discord formatting)
		if (content.startsWith(this.prefix + "sbar ")) {
			content = content.substring(6);

			if (content.startsWith("playing ")) {
				jda.getPresence().setActivity(Activity.playing(content.substring(8)));
			}
			if (content.startsWith("listening ")) {
				jda.getPresence().setActivity(Activity.listening(content.substring(10)));
			}
			if (content.startsWith("watching ")) {
				jda.getPresence().setActivity(Activity.watching(content.substring(9)));
			}
			/*if (content.startsWith("streaming ")) {
				jda.getPresence().setActivity(Activity.streaming(content.substring(10), "https://www.youtube.com/watch?v=5qap5aO4i9A"));
			}*/

			event.getMessage().delete().queue(); // Important to call .queue() on the

		}
	}

}