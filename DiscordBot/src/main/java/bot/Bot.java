package bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {
	public static String prefix = "?";
	public static String myID;
	public static JDA jda;
	private static String token;
	public static String path;

	public static void main(String[] args) {
		// get path of the jar, this is where the config file should be
		try {
			path = URLDecoder.decode(Bot.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
			path = path.substring(0, path.lastIndexOf('/'));
		} catch (UnsupportedEncodingException e) {
			System.err.println("This error should never happen. It still did. That's odd.");
			e.printStackTrace();
		}

		// read the config to set up the bot properly
		try {
			Config.load();
		} catch (FileNotFoundException e) {
			// Since no config file was found the user is guided through a simple setup.
			System.out.println("Config file could not be loaded. File not found in " + path + "/config.txt");
			File file = Config.newFileRoutine();
			if (file != null) {
				System.out.println("Botowner id:");
				Scanner scanner = new Scanner(System.in);
				while (!scanner.hasNextLong()) {
					System.out.println("id must be a long");
					scanner.nextLine();
				}
				String id = scanner.nextLong() + "";
				Config.setLine(ConfigType.STRING, "myID", '"' + id + '"');
				System.out.println("Bot token:");
				String token = scanner.next();
				Config.setLine(ConfigType.STRING, "token", '"' + token + '"');
				Config.setLine(ConfigType.ARRAY_STRING, "slash_channels", "{}");
				try {
					Config.save();
				} catch (IOException e1) {
					System.out.println("Couldn't save generated config.");
					e1.printStackTrace();
				}
				scanner.close();
			} else {
				System.out.println("I don't know what to do now. Bye!");
				e.printStackTrace();
				return;
			}
		}
		
		// set up the bots main listener
		MainListener commands;
		try {
			token = Config.get("token").readString();
			myID = Config.get("myID").readString();

			jda = JDABuilder
					.createDefault(token, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES,
							GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES,
							GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES,
							GatewayIntent.GUILD_MESSAGE_REACTIONS)
					.setChunkingFilter(ChunkingFilter.ALL).setMemberCachePolicy(MemberCachePolicy.ALL)
					.enableCache(CacheFlag.ONLINE_STATUS, CacheFlag.CLIENT_STATUS)
					.setMemberCachePolicy(MemberCachePolicy.ALL).build();

			try {
				jda.awaitReady();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			commands = new MainListener(jda);
			jda.addEventListener(commands);
			try {
				ArrayList<String> modules = Config.get("modules").readStringArray();
				for (String module : modules) {
					MainListener.load(module);
				}
			} finally {
				System.out.println("All modules loaded. Bot ready!");
			}

		} catch (LoginException e) {
			System.out.println("Was not able to login! Maybe the token is bad?");
			e.printStackTrace();
		}

	}
}