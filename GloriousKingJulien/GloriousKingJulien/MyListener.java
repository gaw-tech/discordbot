package GloriousKingJulien;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MyListener extends ListenerAdapter {
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;
		// We don't want to respond to other bot accounts, including ourself
		Message message = event.getMessage();
		String content = message.getContentRaw();
		// getContentRaw() is an atomic getter
		// getContentDisplay() is a lazy getter which modifies the content for e.g.
		// console view (strip discord formatting)
		if (content.equals("#ping")) {
			MessageChannel channel = event.getChannel();
			channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by
													// sendMessage(...)
		} else if (content.equals("#pong")) {
			MessageChannel channel = event.getChannel();
			channel.sendMessage("Ping!").queue();
		} else if (content.equals("#v")) {
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

		} else if (content.startsWith("#help")) {
			if (content.equals("#help")) {
				MessageChannel channel = event.getChannel();
				String help_msg = "#v for a video\n#ping for pong\n#s to suggest any suggestions and\n#help for this <:watthis:773315266019852299>";
				channel.sendMessage(help_msg).queue();
			}
		} else if (content.startsWith("#s ")) {
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
		} else if (content.startsWith("#shout ")) {
			MessageChannel channel = event.getChannel();
			content = content.substring(7);
			int number = Integer.parseInt(content);
			for (int i = 0; i < number; i++) {
				String text = event.getAuthor().getAsMention();
				channel.sendMessage(text).queue();
			}
		} else if (content.startsWith("#repeat ")) {
			MessageChannel channel = event.getChannel();
			content = content.substring(8);
			for (int i = 0; i < 10; i++) {
				channel.sendMessage(content).queue();
			}
		}
	}
}
