package commands;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
	HashMap<String, AudioManager> connections = new HashMap<>();
	// final static String outputwavpath = "output.wav";
	final static String outputwavpath = bot.Bot.path + "/videos/output.wav";

	@Override
	public void run_message(MessageReceivedEvent event) {
		if (!event.getAuthor().getId().equals(Bot.myID)) {
			return;
		}
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		String content = message.getContentRaw();
		if (content.startsWith(prefix + topname + " ")) {
			content = content.substring(prefix.length() + topname.length() + 1);
			if (content.startsWith("join ")) {
				message.delete().queue();
				content = content.substring("join ".length());
				AudioManager am = event.getGuild().getAudioManager();
				VoiceChannel vc = event.getGuild().getVoiceChannelsByName(content, true).get(0);
				connections.put(event.getGuild().getId(), am);
				channel.sendMessage("joined " + am.getConnectedChannel()).queue();
				am.openAudioConnection(vc);
			}
			if (content.equals("leaveall")) {
				message.delete().queue();
				for (AudioManager am : connections.values()) {
					am.closeAudioConnection();
				}
			}
			if (content.startsWith("video ")) {
				AudioManager am = event.getGuild().getAudioManager();
				VoiceChannel vc = event.getMember().getVoiceState().getChannel();
				connections.put(event.getGuild().getId(), am);
				am.openAudioConnection(vc);
				channel.sendMessage("joined " + am.getConnectedChannel()).queue();
				content = content.substring("video ".length());
				File file = YTDL.getVideo(content);
				char[] buf = new char[(int) file.length()];
				System.out.println("bufferin");
				try {
					System.out.println("in the try");
					java.io.FileReader fr = new java.io.FileReader(file);
					fr.read(buf);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					am.setSendingHandler(new ASH(file));
				} catch (IOException | UnsupportedAudioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("testin");
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
		return short_commands;
	}

	@Override
	public void unload() {
		topname = null;
		for (AudioManager am : connections.values()) {
			am.closeAudioConnection();
		}
		connections = null;
	}

}

class ASH implements AudioSendHandler {
	ByteBuffer bb = ByteBuffer.allocate(30720 * 100);
	byte[] bytes;
	private int framesize = 3840;
	boolean providing = false;
	File path = new File("/home/azureuser/bot/videos/output");
	BufferedInputStream is;

	ASH(File file) throws IOException, UnsupportedAudioFileException {
		String args = "ffmpeg -y -v error -i " + file.getAbsolutePath()
				+ " -strict experimental -vn -sn -ac 2 -ar 48000 -b 1536000 -f s16be " + path.getAbsolutePath();
		ProcessBuilder pb = new ProcessBuilder(args.split(" "))
				.redirectError(new File("/home/azureuser/bot/videos/error.txt"));
		Process p = pb.start();
		System.out.println(p.pid());
		System.out.println(Arrays.toString(args.split(" ")));
		long time = System.currentTimeMillis();
		while (p.isAlive()) {

		}
		System.out.println(System.currentTimeMillis() - time + " for conversa");
		is = new BufferedInputStream(new FileInputStream(path.getAbsolutePath()), framesize * 50 * 10);
		providing = true;
	}

	@Override
	public boolean canProvide() {
		return providing;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		try {
			bytes = is.readNBytes(framesize);
		} catch (IOException e) {
			providing = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ByteBuffer.wrap(bytes);
	}

	@Override
	public boolean isOpus() {
		return false;
	}

}

class ARH implements AudioReceiveHandler {

}

class YTDL {
	// static OutputStream os = new ByteArrayOutputStream();
	static OutputStream os = new PipedOutputStream();

	public static File getVideo(String videoId) { // for url https://www.youtube.com/watch?v=abc12345
		YoutubeDownloader downloader = new YoutubeDownloader();

		// sync parsing
		RequestVideoInfo request = new RequestVideoInfo(videoId);
		Response<VideoInfo> response = downloader.getVideoInfo(request);
		VideoInfo video = response.data();

		// video details
		VideoDetails details = video.details();
		System.out.println(details.title());
		System.out.println(details.viewCount());
		details.thumbnails().forEach(image -> System.out.println("Thumbnail: " + image));

		// HLS url only for live videos and streams
		if (video.details().isLive()) {
			System.out.println("Live Stream HLS URL: " + video.details().liveUrl());
		}

		// get videos formats only with audio
		List<VideoWithAudioFormat> videoWithAudioFormats = video.videoWithAudioFormats();
		videoWithAudioFormats.forEach(it -> {
			System.out.println(it.audioQuality() + ", " + it.videoQuality() + " : " + it.url());
		});

		// get all videos formats (may contain better quality but without audio)
		List<VideoFormat> videoFormats = video.videoFormats();
		videoFormats.forEach(it -> {
			System.out.println(it.videoQuality() + " : " + it.url());
		});

		// get audio formats
		List<AudioFormat> audioFormats = video.audioFormats();
		audioFormats.forEach(it -> {
			System.out.println(it.audioQuality() + " : " + it.url());
		});

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
		if (formatByItag != null) {
			System.out.println(formatByItag.url());
		}

		Format format = video.bestAudioFormat();

		// os = new
		// ByteArrayOutputStream(Long.valueOf(video.bestAudioFormat().contentLength()).intValue());
		File file = new File("audio");
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
		RequestVideoFileDownload vrequest = new RequestVideoFileDownload(format).overwriteIfExists(true).renameTo("vid")
				.callback(new YoutubeProgressCallback<File>() {
					@Override
					public void onDownloading(int progress) {
						System.out.printf("Downloaded %d%%\n", progress);
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
		file = vresponse.data(); // will block current thread
		return file;
	}
}