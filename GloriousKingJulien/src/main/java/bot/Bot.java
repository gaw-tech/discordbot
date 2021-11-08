package bot;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

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
	public static String nasaapikey;
	private static String token;
	public static String path;
	public static String javaRoot;

	public static void main(String[] args) {
		// get path of the jar
		try {
			path = URLDecoder.decode(Bot.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
			path = path.substring(0, path.lastIndexOf('/'));
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			Config.load();
		} catch (FileNotFoundException e1) {
			System.out.println("Config file could not be loaded. File not found.");
			e1.printStackTrace();
			return;
		}
		MainListener commands;
		try {
			token = Config.get("token").readString();
			myID = Config.get("myID").readString();
			nasaapikey = Config.get("nasaapikey").readString();
			javaRoot = Config.get("javaRoot").readString();

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
			/*try {
				ArrayList<String> modules = Config.get("modules").readStringArray();
				for(String module : modules) {
					MainListener.load(module);
				}
			} finally {
				
			}*/

		} catch (LoginException e) {
			System.out.println("what the hek?");
			e.printStackTrace();
		}

	}
}