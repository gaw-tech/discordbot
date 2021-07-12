package commands;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import BetterBot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class QRInvite implements Module {
	String topname = "QRInvite";

	void get_qr(String url_in) throws IOException {
		URL url = new URL(url_in);
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf))) {
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();
		FileOutputStream fos = new FileOutputStream("invite_qr.png");
		fos.write(response);
		fos.close();
	}

	@Override
	public void run_message(MessageReceivedEvent event) {
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		String content = message.getContentRaw();
		if (content.startsWith(prefix + "qrinvite ") && !message.getMentionedChannels().isEmpty()) {
			Invite invite = message.getMentionedChannels().get(0).createInvite().complete();
			try {
				get_qr("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + invite.getUrl());
				channel.sendFile(new File("invite_qr.png")).queue();
				new File("invite_qr.png").delete();
			} catch (Exception | Error e) {
				channel.sendMessage("Sorry, but i can't fulfill your request.");
				e.printStackTrace();
			}

		}
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
	}

	@Override
	public void run_button(ButtonClickEvent event) {
	}

	@Override
	public boolean has_reaction() {
		return false;
	}

	@Override
	public boolean has_slash() {
		return false;
	}

	@Override
	public boolean has_button() {
		return false;
	}

	@Override
	public boolean has_basic_help() {
		return true;
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(topname);
		eb.setDescription(
				"`" + prefix + "qrinvite #<channel>` returns an invitiation for the mentioned channel (if possible)");
		return eb;
	}

	@Override
	public HashMap<String, String> get_slash() {
		return null;
	}

	@Override
	public Field get_basic_help() {
		return new Field(topname,
				"`" + prefix + "qrinvite #<channel>` returns an invitiation for the mentioned channel (if possible)",
				true, true);
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		return short_commands;
	}

	@Override
	public void unload() {
		topname = null;
	}

}
