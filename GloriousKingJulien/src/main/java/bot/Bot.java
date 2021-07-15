package bot;

import java.io.FileNotFoundException;
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

	public static void main(String[] args) {
		try {
			Config.load();
		} catch (FileNotFoundException e1) {
			System.out.println("Config file could not be loaded.");
			e1.printStackTrace();
			return;
		}
		MainListener commands;
		try {
			token = Config.get("token").readString();
			myID = Config.get("myID").readString();
			nasaapikey = Config.get("nasaapikey").readString();

			jda = JDABuilder
					.createDefault(token, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES,
							GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES,
							GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES,
							GatewayIntent.GUILD_MESSAGE_REACTIONS)
					.setChunkingFilter(ChunkingFilter.ALL).setMemberCachePolicy(MemberCachePolicy.ALL)
					.enableCache(CacheFlag.ONLINE_STATUS, CacheFlag.CLIENT_STATUS)
					.setMemberCachePolicy(MemberCachePolicy.ALL).build();

			commands = new MainListener(jda);
			jda.addEventListener(commands);

		} catch (LoginException e) {
			System.out.println("what the hek?");
			e.printStackTrace();
		}

	}
}