package BetterBot;

import java.util.LinkedList;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface Module {
	public String prefix = BetterBot.prefix;

	public void run_message(MessageReceivedEvent event);
	
	public void run_reaction(MessageReactionAddEvent event);
	
	public boolean has_reaction();
	
	public EmbedBuilder get_help();

	public boolean has_basic_help();

	public Field get_basic_help();

	public String get_topname();

	public LinkedList<String> get_short_commands();

	public void unload();

}