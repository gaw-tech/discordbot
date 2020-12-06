package GloriousKingJulien;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ServerStats {
	long id;
	ServerStats next;
	int count = 0;
	double time = System.currentTimeMillis();

	ServerStats(long id, ServerStats next) {
		this.id = id;
		this.next = next;
	}

	public ServerStats(long id) {
		this.id = id;
	}

	void write() {
		String msg = "" + this.count;
		this.time = System.currentTimeMillis();
		this.count = 0;
		File file = new File(this.id + ".txt");
		if (!file.exists()) {
			msg = this.time + "\n" + msg;
		}
		try {
			FileWriter fr = new FileWriter(this.id + ".txt", true);
			fr.write(msg + "\n");
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("fail stats in ServerStats");
			e.printStackTrace();
		}
	}

	void run() {
		try {
			while (true) {
				Thread.sleep(60 * 1000);
				String msg = this.time + " " + this.count;
				this.time = System.currentTimeMillis();
				this.count = 0;
				try {
					FileWriter fr = new FileWriter(this.id + ".txt", true);
					fr.write(msg + "\n");
					fr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "" + this.id;
	}
}
