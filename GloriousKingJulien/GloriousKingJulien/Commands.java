package GloriousKingJulien;

import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	JDA jda;
	ServerStatsList stats = new ServerStatsList();

	Suggestions suggestions = new Suggestions();
	Ping ping = new Ping();
	VideoFeed videofeed = new VideoFeed();
	Help help = new Help();
	StatsCommand statscommand = new StatsCommand();

	Commands(JDA jda) {
		this.jda = jda;
	}

	Commands(JDA jda, ServerStatsList stats) {
		this.jda = jda;
		this.stats = stats;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		stats.addMsg(event.getGuild().getIdLong());
		if (event.getAuthor().isBot()) {
			if (event.getMessage().getContentRaw().startsWith("" + MyBot.prefix)) {
				event.getChannel().sendMessage("Sorry i could not hear you?").queueAfter(1, TimeUnit.SECONDS);
			}
			return;
		}
		suggestions.run(event);
		ping.run(event);
		videofeed.run(event);
		help.run(event);
		statscommand.run(event);

		// blödsinn

		if (event.getMessage().getContentRaw().equals(",e sad")) {
			event.getChannel().sendMessage(event.getAuthor().getAsMention() + " is sad.").queue();
			event.getChannel().sendMessage("https://tenor.com/view/crying-big-ed-sad-90day-fiance-tears-gif-17532777")
					.queue();
			event.getMessage().delete().queue();
		}
	}
}