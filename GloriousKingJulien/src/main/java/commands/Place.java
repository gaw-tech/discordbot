
package commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import BetterBot.Vorlage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Place extends ListenerAdapter implements Vorlage {
	boolean keeping = false;
	LinkedList<String> ignorelist = new LinkedList<>();
	String multiplepixelmessage = "";
	int mpmcounter = 0;
	File file = null;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentRaw();
		if (keeping) {
			content = content.toLowerCase();
			if (content.startsWith(".place setpixel")) {
				try {
					String[] splitmsg = content.split(" ");
					int x = Integer.parseInt(splitmsg[2]);
					int y = Integer.parseInt(splitmsg[3]);
					if (splitmsg[4].equalsIgnoreCase("#36393f")) {
						return;
					}
					String add = x + " " + y + " #36393f|";
					multiplepixelmessage += add;
					mpmcounter++;
					if (mpmcounter > 3599) {
						file = new File("multipixel.txt");
						FileWriter fr = new FileWriter(file);
						fr.append(multiplepixelmessage.subSequence(0, multiplepixelmessage.length() - 2));
						fr.close();
						MessageChannel cnl = event.getJDA().getGuildById("817850050013036605")
								.getTextChannelById("832585772720062474");
						cnl.sendFile(file).queue();
						multiplepixelmessage = "";
						mpmcounter = 0;
					}
				} catch (IndexOutOfBoundsException e) {
					// TODO
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("filewriter stuff");
					e.printStackTrace();
				}
			}
		}
		// ignore bots and other users
		if (message.getAuthor().isBot() || !message.getAuthor().getId().equals(BetterBot.BetterBot.myID)) {
			return;
		}
		if (content.equals(prefix + "keep force")) {
			try {
				file = new File("multipixel.txt");
				FileWriter fr;
				fr = new FileWriter(file);
				fr.append(multiplepixelmessage.subSequence(0, multiplepixelmessage.length() - 2));
				fr.close();
				MessageChannel cnl = event.getJDA().getGuildById("817850050013036605")
						.getTextChannelById("832585772720062474");
				cnl.sendFile(file).queue();
				multiplepixelmessage = "";
				mpmcounter = 0;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (content.equals(prefix + "keep")) {
			keeping = !keeping;
			message.getChannel().sendMessage("" + keeping).queue();
		}
		if (content.equals(prefix + "keepsize")) {
			message.getChannel().sendMessage("" + mpmcounter).queue();
		}
		if (content.startsWith(prefix + "keep add ignore ")) {
			List<Member> ml = message.getMentionedMembers();
			for (Member m : ml) {
				ignorelist.add(m.getId());
			}

		}
	}

	@Override
	public EmbedBuilder help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field basic_help() {
		return new Field("", "", true, true);
	}

	@Override
	public String gettopname() {
		return "beepbeepboopboop";
	}

	@Override
	public void unload() {
		keeping = false;
		ignorelist = null;
		multiplepixelmessage = null;
		mpmcounter = 0;
		file = null;
	}

}
