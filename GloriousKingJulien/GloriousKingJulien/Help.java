package GloriousKingJulien;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help {
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
		if (content.equals(this.prefix + "help")) {
			MessageChannel channel = event.getChannel();
			Scanner scanner;
			try {
				scanner = new Scanner(new File("help.txt"));

				String msg = "";
				while (scanner.hasNext()) {
					msg = msg + "`" + this.prefix + scanner.next() + "`" + scanner.nextLine() + "\n";
				}

				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle("Help, powered by Amdy!", null);
				eb.setColor(1);
				eb.addField("Things i can do:", msg, false);
				eb.setFooter("Summoned by: " + event.getMember().getNickname(), event.getAuthor().getAvatarUrl());
				channel.sendMessage(eb.build()).queue();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}