package GloriousKingJulien;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
	JDA jda;
	String myID;

	// Stats
	ServerStatsList stats = new ServerStatsList();
	StatsUserOnline statsUserOnline = new StatsUserOnline();

	Suggestions suggestions = new Suggestions();
	Ping ping = new Ping();
	VideoFeed videofeed = new VideoFeed();
	Help help = new Help();
	StatsCommand statscommand = new StatsCommand();
	StatusBar statusbar = new StatusBar();
	StatsUserOnlineCommand statsUserOnlineCommand = new StatsUserOnlineCommand();
	OtherCommands otherCommands = new OtherCommands();

	// ButtonGame
	ButtonCommand buttoncommand = new ButtonCommand();

	Commands(JDA jda, String myID) {
		this.jda = jda;
		this.myID = myID;
	}

	Commands(JDA jda, String myID, ServerStatsList stats) {
		this.jda = jda;
		this.myID = myID;
		this.stats = stats;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		stats.addMsg(event.getGuild().getIdLong());
		// prevents from responding to bot and writing in other channels than #spam and
		// #botfun
		if (event.getAuthor().isBot() || (event.getGuild().getIdLong() == 747752542741725244l && event.getChannel().getIdLong() != 768600365602963496l
				&& event.getChannel().getIdLong() != 747776646551175217l)) {
			return;
		}
		statsUserOnlineCommand.run(event);
		suggestions.run(event);
		ping.run(event);
		videofeed.run(event);
		help.run(event);
		statscommand.run(event);
		buttoncommand.run(event, jda);

		// owner commands
		if (event.getAuthor().getAsTag().equals(myID)) {
			statusbar.run(event, jda);
			otherCommands.run(event, jda);
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		buttoncommand.claim(event, jda);
	}
}