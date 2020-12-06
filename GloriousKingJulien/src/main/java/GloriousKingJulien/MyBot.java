package GloriousKingJulien;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class MyBot {
	final static char prefix = '?';

	public static void main(String[] args) {
		Scanner scanner;
		try {
			scanner = new Scanner(new File("info.txt"));
			String token = scanner.next();
			String myID = scanner.next();
			try {
				JDA jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS)
						.enableIntents(GatewayIntent.GUILD_PRESENCES).setChunkingFilter(ChunkingFilter.ALL)
						.setMemberCachePolicy(MemberCachePolicy.ALL).build();
				Commands commands = new Commands(jda, myID);
				jda.addEventListener(commands);
				// jda.addEventListener(new MyListener());

				try {
					while (true) {
						Thread.sleep(60 * 1000);
						// System.out.println("saving stats for " + commands.stats.size + " servers.");
						commands.stats.write();
						commands.buttoncommand.bgame.run(jda);
						commands.statsUserOnline.run(jda);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (LoginException e) {
				// TODO Auto-generated catch block
				System.out.println("what the hek?");
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
