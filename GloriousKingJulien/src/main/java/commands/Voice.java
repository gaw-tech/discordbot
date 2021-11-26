package commands;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.Extension;
import com.github.kiulian.downloader.model.Filter;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;
import bot.Bot;
import bot.Module;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class Voice implements Module {
	String topname = "voice";
	static HashMap<String, AudioManager> connections = new HashMap<>();
	static HashMap<String, ASH> handlers = new HashMap<>();
	static HashMap<String, LinkedList<String>> queues = new HashMap<>();

	@Override
	public void run_message(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		String content = message.getContentRaw();
		Member member = event.getMember();
		String guildid = event.getGuild().getId();
		// join VC
		if (content.equals(prefix + "join")) {
			if (connections.containsKey(guildid)) {
				if (connections.get(guildid).isConnected()) {
					channel.sendMessage("I already am in a voice channel. Come to "
							+ connections.get(guildid).getConnectedChannel().getAsMention() + " to join me.").queue();
					return;
				}
			}
			if (!member.getVoiceState().inVoiceChannel()) {
				channel.sendMessage("You have to be in a voice channel for 	me to join you.").queue();
				return;
			}

			VoiceChannel vc = member.getVoiceState().getChannel();
			AudioManager am = (connections.containsKey(guildid)) ? connections.get(guildid)
					: event.getGuild().getAudioManager();
			connections.put(event.getGuild().getId(), am);
			am.openAudioConnection(vc);
			channel.sendMessage("Joining " + vc.getAsMention()).queue();
			ASH ash = new ASH(guildid);
			handlers.put(guildid, ash);
			am.setSendingHandler(ash);
			// TODO: catch permission error
		}
		// play
		if (content.startsWith(prefix + "play ")) {
			// very first lets check if the bot is connected to a vc TODO: maybe change it
			// that the bot joins automatically a vc
			AudioManager am = connections.get(guildid);
			if (am == null || !am.isConnected()) {
				channel.sendMessage(
						"To play a song i must be in a voice chat. To connect me join a voice chat and type `" + prefix
								+ "join`.")
						.queue();
				return;
			}
			// lets also check if the user making the call is in a voice chat
			if (!member.getVoiceState().inVoiceChannel()) {
				channel.sendMessage("If you want me to play something for you, join me in "
						+ am.getConnectedChannel().getAsMention() + ".").queue();
			}
			// first lets get the video id from the input
			content = content.substring(prefix.length() + "play ".length());
			boolean validlink = false;
			int i = content.indexOf("v=");
			if (i != -1) {
				validlink = true;
				content = content.substring(i + 2);
				i = content.indexOf('&');
				if (i != -1) {
					content = content.substring(0, i);
				}
			} else {
				i = content.lastIndexOf('/');
				if (i != -1) {
					validlink = true;
					content = content.substring(i + 1);
				}
			}
			if (!validlink) {
				message.addReaction(event.getJDA().getEmoteById("779883525800722432")).queue();
			} else {
				ASH ash = handlers.get(guildid);
				LinkedList<String> queue = queues.get(guildid);
				// now lets check if we already have it downloaded
				if (!Voice.inqueue(content)) {
					try {
						YTDL.getVideo(content);
					} catch (IOException e) {
						message.addReaction(event.getJDA().getEmoteById("799214599500726302")).queue();
						e.printStackTrace();
					}
				}
				// add the video to the queue
				if (queue == null) {
					queue = new LinkedList<>();
				}
				queue.add(content);
				// play it right now if nothing is playing
				if (!ash.isProviding())
					try {
						ash.play(content);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		// owner
		if (!member.getId().equals(Bot.myID))
			return;
		if (content.equals(prefix + "leave")) {
			message.delete().queue();
			if (connections.containsKey(guildid)) {
				connections.get(guildid).closeAudioConnection();
			}
		}

	}

	@Override
	public void run_reaction(MessageReactionAddEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run_slash(SlashCommandEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run_button(ButtonClickEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean has_reaction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has_slash() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has_button() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has_basic_help() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EmbedBuilder get_help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> get_slash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field get_basic_help() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get_topname() {
		return topname;
	}

	@Override
	public LinkedList<String> get_short_commands() {
		LinkedList<String> short_commands = new LinkedList<>();
		short_commands.add("join");
		short_commands.add("leave");
		short_commands.add("play");
		return short_commands;
	}

	@Override
	public void unload() {
		topname = null;
		for (AudioManager am : connections.values()) {
			am.closeAudioConnection();
		}
		for (ASH ash : handlers.values()) {
			ash.stop();
		}
		connections = null;
	}

	public static boolean inqueue(String videoId) { // probably not thread safe
		for (LinkedList<String> queue : queues.values()) {
			for (String id : queue) {
				if (id.equals(videoId))
					return true;
			}
		}
		return false;
	}
}

class ASH implements AudioSendHandler {
	private ByteBuffer bb = ByteBuffer.allocate(30720 * 100);
	private int framesize = 3840;
	private byte[] bytes = new byte[framesize];
	private boolean providing = false;
	private String path;
	private BufferedInputStream is;
	private String guildid;
	private String videoId = "";

	public ASH(String guildid) {
		this.guildid = guildid;
		path = "/home/azureuser/bot/videos/";
	}

	@Override
	public boolean canProvide() {
		return providing;
	}

	public void play(String videoId) throws IOException {
		this.videoId = videoId;
		is = new BufferedInputStream(new FileInputStream(path + videoId), framesize * 50 * 10);
		providing = true;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		try {
			bytes = is.readNBytes(framesize);
		} catch (IOException e) {
			// we assume we are at the end of the file
			LinkedList<String> queue = Voice.queues.get(guildid);
			// remove the played video from the queue
			queue.removeFirst();
			// delete the file if its not in any queue
			if (!Voice.inqueue(videoId)) {
				new File(videoId).delete();
			}
			// closing the old stream
			try {
				is.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// stop playing if the queu is empty
			if (queue.isEmpty()) {
				providing = false;
			} else {
				// playing the next song from the queue
				try {
					videoId = queue.getFirst();
					is = new BufferedInputStream(new FileInputStream(new File(videoId)));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ByteBuffer.wrap(bytes);
	}

	@Override
	public boolean isOpus() {
		return false;
	}

	public boolean isProviding() {
		return providing;
	}

	public void stop() {
		providing = false;
		try {
			is.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

class ARH implements AudioReceiveHandler {

}

class YTDL {
	// static OutputStream os = new ByteArrayOutputStream();
	static OutputStream os = new PipedOutputStream();

	public static void getVideo(String videoId) throws IOException { // for url https://www.youtube.com/watch?v=abc12345
		YoutubeDownloader downloader = new YoutubeDownloader();

		// sync parsing
		RequestVideoInfo request = new RequestVideoInfo(videoId);
		Response<VideoInfo> response = downloader.getVideoInfo(request);
		VideoInfo video = response.data();

		// video details
		VideoDetails details = video.details();

		// get videos formats only with audio
		List<VideoWithAudioFormat> videoWithAudioFormats = video.videoWithAudioFormats();

		// get all videos formats (may contain better quality but without audio)
		List<VideoFormat> videoFormats = video.videoFormats();

		// get audio formats
		List<AudioFormat> audioFormats = video.audioFormats();

		// get best format
		video.bestVideoWithAudioFormat();
		video.bestVideoFormat();
		video.bestAudioFormat();

		// filtering formats
		List<Format> formats = video.findFormats(new Filter<Format>() {
			@Override
			public boolean test(Format format) {
				return format.extension() == Extension.WEBM;
			}
		});

		// itags can be found here -
		// https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2
		Format formatByItag = video.findFormatByItag(18); // return null if not found

		Format format = video.bestAudioFormat();

		// os = new
		// ByteArrayOutputStream(Long.valueOf(video.bestAudioFormat().contentLength()).intValue());

		/*
		 * // download in-memory to OutputStream RequestVideoStreamDownload vrequest =
		 * new RequestVideoStreamDownload(format, os).maxRetries(100); Response<Void>
		 * vresponse = downloader .downloadVideoStream(vrequest.callback(neaw
		 * YoutubeProgressCallback<Void>() {
		 * 
		 * @Override public void onFinished(Void data) { // TODO Auto-generated method
		 * stub System.out.println("Finished stream"); }
		 * 
		 * @Override public void onError(Throwable throwable) { // TODO Auto-generated
		 * method stub System.out.println("Error: " + throwable.getLocalizedMessage());
		 * }
		 * 
		 * @Override public void onDownloading(int progress) { // TODO Auto-generated
		 * method stub System.out.printf("Downloaded %d%%\n", progress); }
		 * 
		 * }).async()); System.out.println(os); System.out.println(vresponse.error());
		 */
		RequestVideoFileDownload vrequest = new RequestVideoFileDownload(format).overwriteIfExists(true)
				.renameTo(videoId).callback(new YoutubeProgressCallback<File>() {
					@Override
					public void onDownloading(int progress) {
					}

					@Override
					public void onFinished(File videoInfo) {
						System.out.println("Finished file: " + videoInfo);
					}

					@Override
					public void onError(Throwable throwable) {
						System.out.println("Error: " + throwable.getLocalizedMessage());
					}
				}).async();
		Response<File> vresponse = downloader.downloadVideoFile(vrequest);
		File file = vresponse.data();
		String args = "ffmpeg -y -v error -i " + file.getAbsolutePath()
				+ " -strict experimental -vn -sn -ac 2 -ar 48000 -b 1536000 -f s16be videos/" + videoId;
		ProcessBuilder pb = new ProcessBuilder(args.split(" "))
				.redirectError(new File("/home/azureuser/bot/videos/error.txt"));
		Process p = pb.start();
		System.out.println(p.pid());
		System.out.println(Arrays.toString(args.split(" ")));
		long time = System.currentTimeMillis();
		while (p.isAlive()) {

		}
		System.out.println(System.currentTimeMillis() - time + " for conversation");
		file.delete();
	}
}