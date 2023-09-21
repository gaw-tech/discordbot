package commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bot.Bot;
import bot.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

public class ButtonGame implements Module {
	public static Emoji emoji = Emoji.fromEmote("sipspin", 831867874506178660L, true);
	public static String button_message = "Press the button to claim the points.";
	private String topname = "Button";
	private HashMap<Long, ButtonGameInstance> servers = new HashMap<>();
	Runnable run = new Runnable() {
		@Override
		public void run() {
			for (Long l : servers.keySet()) {
				servers.get(l).increment();
			}
			try {
				save_button_values();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	public ButtonGame() {
		// TODO: load old stuff saved with reflection
		for (Guild g : Bot.jda.getGuilds()) {
			ButtonGameInstance bg = new ButtonGameInstance(g.getIdLong());
			try {
				bg.load();
				servers.put(g.getIdLong(), bg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				load_button_values();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		exec.scheduleAtFixedRate(run, 0, 1, TimeUnit.MINUTES);
	}

	@Override
	public void run_message(MessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentRaw();
		MessageChannel channel = event.getChannel();
		long server_id = event.getGuild().getIdLong();

		// call the button
		if (content.equals(prefix + "b")) {
			call_button(channel, server_id);
			message.delete().queue();
		}

		// scoreboard
		if (content.equals(prefix + "buttonboard") || content.equals(prefix + "bb")) {
			print_top_10(channel, server_id);
			message.delete().queue();
		}

		// check scores
		if (content.equals(prefix + "bs")) {
			if (servers.containsKey(server_id)) {
				servers.get(server_id).print_user_score(message.getAuthor().getIdLong(), channel);
			}
			message.delete().queue();
		}
		if (content.startsWith(prefix + "bs ")) {
			content = content.substring(prefix.length() + "bs ".length());
			if (servers.containsKey(server_id)) {
				servers.get(server_id).print_user_score(Long.parseLong(content), channel);
			}
			message.delete().queue();
		}

		// scoreboard from the second run of the game
		if (content.equals(prefix + "button s2")) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("ButtonBoard", null);
			eb.setColor(1);
			eb.setDescription("Top 10 Players of the button game s2.\n");
			eb.addField("Rank:", "**1.\n2.\n3.\n4.\n5.\n6.\n7.\n8.\n9.\n10.**", true);
			eb.addField("Player:",
					"<@124603627833786370>\n<@807040441605161020>\n<@155419933998579713>\n<@344065593483460618>\n<@882291499914653768>\n<@223932775474921472>\n<@532270249697869835>\n<@182849528515395584>\n<@276462585690193921>\n<@420625924074110976>",
					true);
			eb.addField("Score:", "3955\n3847\n3800\n3627\n3107\n3040\n2934\n2832\n2748\n2743", true);
			eb.setFooter("", Bot.jda.getSelfUser().getAvatarUrl());
			channel.sendMessageEmbeds(eb.build()).queue();
		}

		// georg region
		if (event.getAuthor().getId().equals(Bot.myID)) {
		}
	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
	}

	@Override
	public void run_slash(SlashCommandEvent event) {
		String name = event.getName();
		MessageChannel channel = event.getChannel();
		long server_id = event.getGuild().getIdLong();
		// call the button
		if (name.equals("button")) {
			call_button(channel, server_id);
			event.deferReply().queue(ih -> {
				ih.deleteOriginal().queue();
			});
		}

		// scoreboard
		if (name.equals("buttonboard")) {
			InteractionHook ih = event.deferReply().complete();
			print_top_10(ih, server_id);
		}

		// check scores
		if (name.equals("buttonscore")) {
			if (servers.containsKey(server_id)) {
				InteractionHook ih = event.deferReply().complete();
				servers.get(server_id).print_user_score(event.getMember().getIdLong(), ih);
			} else {
				event.deferReply().queue(ih -> {
					ih.deleteOriginal().queue();
				});
			}
		}
	}

	@Override
	public void run_button(ButtonClickEvent event) {
		if (!event.getButton().getId().startsWith("bgb")) {
			return;
		}
		// if its an old button (button game not started or not equal to the newest
		// button message) delete it
		if (!servers.containsKey(event.getGuild().getIdLong())) {
			// || servers.get(event.getGuild().getIdLong()).message.getIdLong() !=
			// event.getMessageIdLong()) {
			event.getMessage().delete().queue();
			return;
		}
		InteractionHook ih = event.deferReply().setEphemeral(true).complete();
		claim(event.getGuild().getIdLong(), event.getUser().getIdLong(), ih);

	}

	@Override
	public boolean has_reaction() {
		return false;
	}

	@Override
	public boolean has_slash() {
		return true;
	}

	@Override
	public boolean has_button() {
		return true;
	}

	@Override
	public boolean has_basic_help() {
		return true;
	}

	@Override
	public EmbedBuilder get_help() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Help Button");
		eb.addField("Description:",
				"Welcome to the button game. The goal of the button game is to claim as much points as possible. This is done by pressing the \"button\". Note that your score is the highest score you claimed the button at, so if you claim the button your new score is the value of the button.",
				true);
		eb.addField("Usage:",
				"`" + prefix + "b` get the button message\n`" + prefix
						+ "bb` get a list with the top 10 players of the button game\n`" + prefix
						+ "bs` tells you your current score and your rank in the button game\n`" + prefix
						+ "bs <user id>` returns the score and rank of the mentioned user",
				false);
		return eb;
	}

	@Override
	public HashMap<String, String> get_slash() {
		HashMap<String, String> slash_commands = new HashMap<>();
		slash_commands.put("button", "Sends the button from the ButtonGame.");
		slash_commands.put("buttonscore", "Tells you your rank on the ButtonGame scoreboard.");
		slash_commands.put("buttonboard", "Lists the top 10 players of the ButtonGame.");
		return slash_commands;
	}

	@Override
	public Field get_basic_help() {
		return new Field(topname, "`" + prefix + "b` get the button\n`" + prefix + "bb` shows the ButtonBoard\n`"
				+ prefix + "bs` your rank in the button game", true, true);
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		short_commands.add("b");
		short_commands.add("bb");
		short_commands.add("buttonboard");
		short_commands.add("bs");
		return short_commands;
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub
		exec.shutdownNow();
		try {
			save_button_values();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * method to send the button message. sets up a buttongame if it does not exist
	 * for the server.
	 */
	private void call_button(MessageChannel channel, long server_id) {
		ButtonGameInstance bg;
		if (!servers.containsKey(server_id)) {
			bg = new ButtonGameInstance(server_id);
			servers.put(server_id, bg);
		} else {
			bg = servers.get(server_id);
		}
		bg.call_button(channel);
	}

	/*
	 * method to check if a user can claim the button and if it can change the score
	 */
	private void claim(long server_id, long user_id, InteractionHook ih) {
		if (!servers.containsKey(server_id)) {
			return;
		}
		ButtonGameInstance bg = servers.get(server_id);
		bg.claim(user_id, ih);
	}

	/*
	 * method to print the top10 leaderboard
	 */
	private void print_top_10(MessageChannel channel, long server_id) {
		ButtonGameInstance bg = servers.get(server_id);
		if (bg != null) {
			bg.print_top_10(channel);
		}
	}

	private void print_top_10(InteractionHook ih, long server_id) {
		ButtonGameInstance bg = servers.get(server_id);
		if (bg != null) {
			bg.print_top_10(ih);
		}
	}

	private void save_button_values() throws IOException {
		File file = new File("buttons.value");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fr = new FileWriter(file, false);
		for (Long l : servers.keySet()) {
			fr.append(l + " " + servers.get(l).get_value() + "\n");
		}
		fr.close();
	}

	private void load_button_values() throws FileNotFoundException {
		File file = new File("buttons.value");
		if (!file.exists()) {
			return;
		}
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split(" ");
			long server_id = Long.parseLong(line[0]);
			if (servers.containsKey(server_id)) {
				servers.get(server_id).set_value(Integer.parseInt(line[1]));
			}
		}
		scanner.close();
	}

	class ButtonGameInstance {
		private int value = 0;
		private Message message;
		private long server_id;
		private Button button;
		private HashMap<Long, Integer> scores = new HashMap<>();

		ButtonGameInstance(long server_id) {
			this.server_id = server_id;
			button = Button.of(ButtonStyle.PRIMARY, "bgb" + server_id, "" + value, ButtonGame.emoji);
		}

		void call_button(MessageChannel channel) {
			Message new_message = channel.sendMessage(ButtonGame.button_message).setActionRow(button).complete();
			if (new_message != null) {
				if (message != null) {
					message.delete().queue();
				}
				message = new_message;
				try {
					save();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		protected void claim(long user_id, InteractionHook ih) {
			if (scores.containsKey(user_id)) {
				int score = scores.get(user_id);
				if (score >= value) {
					ih.editOriginal(
							"The score of the button needs to be greater than yours, you can't claim it now. Try it in "
									+ (score - value) + " minutes.")
							.queue();
					return;
				}
			}
			scores.put(user_id, value);
			ih.editOriginal("You claimed the button!").queue();
			// TODO: add new rank to the claim message
			message.getChannel().sendMessage("<@" + user_id + "> has claimed `" + value + "` points.").queue();
			value = 0;
			button = Button.of(ButtonStyle.PRIMARY, "bgb" + server_id, "" + value, ButtonGame.emoji);
			call_button(message.getChannel());
		}

		public void increment() {
			value++;
			button = Button.of(ButtonStyle.PRIMARY, "bgb" + server_id, "" + value, ButtonGame.emoji);
			if (message != null) {
				try {
					message.editMessage(ButtonGame.button_message).setActionRow(button).queue();
				} catch (ErrorResponseException e) {
					// TODO: some appropriate error message
				}
			}
		}

		protected void load() throws IOException {
			File file = new File(server_id + ".scores");
			if (!file.exists()) {
				return;
			}
			Scanner scanner = new Scanner(file);
			String[] first_line = scanner.nextLine().split(" ");
			try {
				Message message = Bot.jda.getTextChannelById(Long.parseLong(first_line[1]))
						.retrieveMessageById(Long.parseLong(first_line[0])).complete();
				this.message = message;
			} catch (Exception | Error e) {
				e.printStackTrace();
			}
			while (scanner.hasNext()) {
				String[] line = scanner.nextLine().split(" ");
				long user_id = Long.parseLong(line[0]);
				int user_score = Integer.parseInt(line[1]);
				scores.put(user_id, user_score);
			}
			scanner.close();
		}

		protected void save() throws IOException {
			File file = new File(server_id + ".scores");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fr = new FileWriter(file, false);
			fr.append(message.getId() + " " + message.getChannel().getId() + "\n");
			for (Long user_id : scores.keySet()) {
				fr.append(user_id + " " + scores.get(user_id) + "\n");
			}
			fr.close();
		}

		public int get_value() {
			return value;
		}

		protected void set_value(int value) {
			this.value = value;
		}

		protected void print_top_10(MessageChannel channel) {
			long[] sorted_users = sorted_users();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("ButtonBoard");
			eb.setColor(1); // maybe change that to a fun color?
			eb.setDescription("Top " + ((sorted_users.length < 10) ? sorted_users.length : 10)
					+ " Players of the button game.\n");

			String users = "";
			String scores = "";
			String rank = "";

			for (int i = 0; i < 10 && i < sorted_users.length; i++) {
				users = users + "<@" + sorted_users[i] + ">:\n";
				scores = scores + this.scores.get(sorted_users[i]) + "\n";
				rank = rank + "**" + (i + 1) + ".**\n";
			}
			eb.addField("", "**Rank:**\n" + rank, true);
			eb.addField("", "**Player:** \n" + users, true);
			eb.addField("", "**Score:** \n" + scores, true);
			eb.setFooter("", Bot.jda.getSelfUser().getAvatarUrl());
			channel.sendMessageEmbeds(eb.build()).queue();
		}

		protected void print_top_10(InteractionHook ih) {
			long[] sorted_users = sorted_users();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("ButtonBoard");
			eb.setColor(1); // maybe change that to a fun color?
			eb.setDescription("Top " + ((sorted_users.length < 10) ? sorted_users.length : 10)
					+ " Players of the button game.\n");

			String users = "";
			String scores = "";
			String rank = "";

			for (int i = 0; i < 10 && i < sorted_users.length; i++) {
				users = users + "<@" + sorted_users[i] + ">:\n";
				scores = scores + this.scores.get(sorted_users[i]) + "\n";
				rank = rank + "**" + (i + 1) + ".**\n";
			}
			eb.addField("", "**Rank:**\n" + rank, true);
			eb.addField("", "**Player:** \n" + users, true);
			eb.addField("", "**Score:** \n" + scores, true);
			eb.setFooter("", Bot.jda.getSelfUser().getAvatarUrl());
			ih.editOriginalEmbeds(eb.build()).queue();
		}

		protected long[] sorted_users() {
			long[] sorted_users = new long[scores.size()];
			int[] sorted_scores = new int[scores.size()];
			int i = 0;
			for (Entry<Long, Integer> e : scores.entrySet()) {
				sorted_users[i] = e.getKey();
				sorted_scores[i] = e.getValue();
				i++;
			}
			// TODO: definitely could be improved
			for (int j = 0; j < sorted_scores.length; j++) {
				for (i = 0; i < sorted_scores.length - 1; i++) {
					if (sorted_scores[i] < sorted_scores[i + 1]) {
						int tmp_s = sorted_scores[i + 1];
						long tmp_u = sorted_users[i + 1];
						sorted_scores[i + 1] = sorted_scores[i];
						sorted_users[i + 1] = sorted_users[i];
						sorted_scores[i] = tmp_s;
						sorted_users[i] = tmp_u;
					}
				}
			}
			return sorted_users;
		}

		protected void print_user_score(long user_id, MessageChannel channel) {
			long[] sorted_users = sorted_users();
			int rank = 0;
			EmbedBuilder eb = new EmbedBuilder();
			for (; rank < sorted_users.length; rank++) {
				if (sorted_users[rank] == user_id) {
					rank++;
					eb.appendDescription(
							"<@" + user_id + "> has `" + scores.get(user_id) + "` points and is nr. " + rank);
					channel.sendMessageEmbeds(eb.build()).queue();
					return;
				}
			}
			eb.appendDescription("Found no score for the id: " + user_id);
			channel.sendMessageEmbeds(eb.build()).queue();
		}

		protected void print_user_score(long user_id, InteractionHook ih) {
			long[] sorted_users = sorted_users();
			int rank = 0;
			EmbedBuilder eb = new EmbedBuilder();
			for (; rank < sorted_users.length; rank++) {
				if (sorted_users[rank] == user_id) {
					rank++;
					eb.appendDescription(
							"<@" + user_id + "> has `" + scores.get(user_id) + "` points and is nr. " + rank);
					ih.editOriginalEmbeds(eb.build()).queue();
					return;
				}
			}
			eb.appendDescription("Found no score for the id: " + user_id);
			ih.editOriginalEmbeds(eb.build()).queue();
		}
	}
}