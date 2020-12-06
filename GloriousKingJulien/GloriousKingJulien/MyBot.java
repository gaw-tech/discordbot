package GloriousKingJulien;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class MyBot {
	final static char prefix = '?';

	public static void main(String[] args) {
		try {
			JDA jda = JDABuilder.createDefault("Nzc4NzMxNTQwMzU5Njc1OTA0.X7WQQw.NHwewGzfs3dVTLeNX85PD6h_Xj4").build();
			Commands commands = new Commands(jda);
			jda.addEventListener(commands);
			jda.getPresence().setActivity(Activity.playing("with your data"));
			// jda.addEventListener(new MyListener());

			try {
				while (true) {
					Thread.sleep(60 * 1000);
					System.out.println("saving stats for " + commands.stats.size + " servers.");
					commands.stats.write();
					// System.out.println(commands.stats.first.toString());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			System.out.println("what the hek?");
			e.printStackTrace();
		}
	}

}
