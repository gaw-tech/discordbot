package GloriousKingJulien;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;

public class StatsUserOnline {
	int counter = 0;
	
	
	void run(JDA jda) {
		List<Guild> guilds = jda.getGuilds();
		guilds.forEach(guildItem ->{
			this.counter =0;
			Guild guild = guildItem;
			guild.getMembers().stream().forEach(member ->{
				if(member.getOnlineStatus() != OnlineStatus.OFFLINE && !member.getOnlineStatus().toString().equals("")) {
					this.counter++;
				}
			});
			write(guild.getId(), counter);
		});
	}
	
	void write(String serverId, int counter) {
		double time = System.currentTimeMillis();
		String msg ="";

		File file = new File(serverId + "online.txt");
		if (!file.exists()) {
			msg = time + "\n";
		}
		try {
			FileWriter fr = new FileWriter(serverId + "online.txt", true);
			msg = msg + counter;
			fr.write(msg + "\n");
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("fail stats in StatsUserOnline");
			e.printStackTrace();
		}
	}

}
