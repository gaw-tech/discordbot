package GloriousKingJulien;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VideoFeed {
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
		if (content.equals(prefix + "v")) {
			try {
				Scanner scanner = new Scanner(new File("links.txt"));
				int lines = scanner.nextInt();
				String link = scanner.next();
				Random rdm = new Random();
				for (int i = 1; i <= rdm.nextInt(lines); i++) {
					link = scanner.next();
				}
				MessageChannel channel = event.getChannel();
				channel.sendMessage(link).queue();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
