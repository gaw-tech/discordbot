package BetterBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

public interface Vorlage {
	public String prefix = BetterBot.prefix;

	public EmbedBuilder help();
	
	public Field basic_help();
	
	public String gettopname();

	public void unload();
	
}	