package GloriousKingJulien;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Suggestions {
	char prefix = MyBot.prefix;

	public void run(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;
		Message message = event.getMessage();
		String content = message.getContentRaw();
		if (content.startsWith(this.prefix + "s ")) {
			File suggestions = new File("suggestions.txt");
			content = content.substring(3);
			content = event.getAuthor().getAsTag() + " " + content;
			Message msg = event.getMessage();
			msg.delete().queue();
			try {
				FileWriter fr = new FileWriter(suggestions, true);
				fr.write(content + "\n");
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
