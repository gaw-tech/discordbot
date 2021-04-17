package BetterBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class BetterBot {
	public static String prefix = "?";
	public static String myID = "381154302720213002";
	static JDA jda;
	public static String nasaapikey = "";

	public static void main(String[] args) {
		Scanner scanner;
		BetterCommands commands;
		try {

			scanner = new Scanner(new File("info.txt"));
			String token = scanner.next();
			myID = scanner.next();

			jda = JDABuilder
					.createDefault(token, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES,
							GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES,
							GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS)
					.setChunkingFilter(ChunkingFilter.ALL).setMemberCachePolicy(MemberCachePolicy.ALL).build();
			;

			commands = new BetterCommands();
			jda.addEventListener(commands);

		} catch (LoginException e) {
			System.out.println("what the hek?");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}