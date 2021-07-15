package bot;

import java.util.HashMap;
import java.util.LinkedList;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface Module {
	public String prefix = Bot.prefix;
	
	public void run_message(MessageReceivedEvent event);
	
	public void run_reaction(MessageReactionAddEvent event);
	
	public void run_slash(SlashCommandEvent event);
	
	public void run_button(ButtonClickEvent event);
	
	public boolean has_reaction();
	
	public boolean has_slash();
	
	public boolean has_button();

	public boolean has_basic_help();

	public EmbedBuilder get_help();

	public HashMap<String,String> get_slash();

	public Field get_basic_help();

	public String get_topname();

	public LinkedList<String> get_short_commands();

	public void unload();

}