package GloriousKingJulien;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ButtonGame {
	private long value = 10;
	long msgId;
	long channelId;
	long serverId;
	boolean initiated = false;

	// Constructor
	ButtonGame(long msgId, long channelId, long serverId) {
		this.msgId = msgId;
		this.channelId = channelId;
		this.serverId = serverId;
	}

	public void run(JDA jda) {

		this.value++;

		if (initiated) {
			MessageChannel channel = jda.getTextChannelById(channelId);
			channel.editMessageById(msgId,
					"Current value: `" + value + "`\nAdd a reaction to this message and claim your points!").queue();
		}
	}

	public long getValue() {
		return this.value;
	}

	public void setValue(long value) {
		this.value = value;
	}

}
