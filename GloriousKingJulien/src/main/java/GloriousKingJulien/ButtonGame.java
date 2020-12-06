package GloriousKingJulien;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ButtonGame {
	private long value = 10;
	private boolean started = false;
	long msgId;
	long channelId;

	public void run(JDA jda) {

		if (!started)
			return;

		this.value++;

		MessageChannel channel = jda.getTextChannelById(channelId);
		channel.editMessageById(msgId, "Current value: `" + value + "`\nAdd a reaction to this message and claim your points!").queue();
	}

	public long getValue() {
		return this.value;
	}
	
	public void setValue(long value) {
		this.value = value;
	}

	void startGame(long msgId, long channelId) {
		this.msgId = msgId;
		this.channelId = channelId;
		this.started = true;
	}

	public void reStartGame(long messageId, long channelId) {
		this.msgId = messageId;
		this.channelId = channelId;
		this.started = true;
		this.value = 10;
	}
}
