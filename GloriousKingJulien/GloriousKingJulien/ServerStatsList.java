package GloriousKingJulien;

public class ServerStatsList {
	ServerStats first;
	ServerStats last;
	int size = 0;

	void addLast(long id) {
		if (this.size == 0) {
			addFirstElement(id);
		} else {
			ServerStats server = new ServerStats(id);
			this.last.next = server;
			this.last = server;
			this.size += 1;
		}
	}

	void addMsg(long id) {
		if (this.size == 0) {
			this.addLast(id);
			this.last.count++;
		}
		boolean nomatch = true;
		int i = 0;
		ServerStats pointer = this.first;
		while (nomatch && i < this.size) {
			if (pointer.id == id) {
				pointer.count++;
				nomatch = false;
			}
			i++;
			pointer = pointer.next;
		}
		if (nomatch) {
			this.addLast(id);
			this.last.count++;
		}
	}

	void write() {
		int i = 0;
		ServerStats pointer = this.first;
		while (i < this.size) {
			pointer.write();
			i++;
			pointer = pointer.next;
		}
	}

	@Override
	public String toString() {
		String out = "";
		int i = 0;
		ServerStats pointer = this.first;
		while (i<this.size) {
			out = out + pointer.id;
			pointer = pointer.next;
			i++;
		}
		return out;
	}

	private void addFirstElement(Long id) {
		ServerStats server = new ServerStats(id);
		this.first = server;
		this.last = server;
		this.size = 1;

	}

}
