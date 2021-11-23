package bot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class WaveDecoder {
	final static char[] RIFF = { 'R', 'I', 'F', 'F' };
	final static char[] WAVE = { 'W', 'A', 'V', 'E' };
	final static char[] fmt_ = { 'f', 'm', 't', ' ' };
	final static char[] data = { 'd', 'a', 't', 'a' };
	final static char[] LIST = { 'L', 'I', 'S', 'T' };
	public final FileReader fr;
	public 	File file;
	public 	int ChunkSize;
	public 	int Subchunk1Size;
	public 	int AudioFormat;
	public 	int NumChannels;
	public 	int SampleRate;
	public 	int ByteRate;
	public 	int BlockAlign;
	public 	int BitsPerSample;
	public 	int Subchunk2Size;

	public WaveDecoder(File file) throws IOException {
		fr = new FileReader(file);
		char[] header = new char[4];
		fr.read(header, 0, 4);
		for (int i = 0; i < 4; i++) {
			if (header[i] != WaveDecoder.RIFF[i]) {
				System.out.println(Arrays.toString(header));
				//throw new Error("RIFF[" + i + "] was " + header[i]);
				break;
			}
		}
		fr.read(header, 0, 4);
		ChunkSize = littleEndianToInt(header);
		fr.read(header, 0, 4);
		for (int i = 0; i < 4; i++) {
			if (header[i] != WaveDecoder.WAVE[i]) {
				System.out.println(Arrays.toString(header));
				//throw new Error("WAVE[" + i + "] was " + header[i]);
				break;
			}
		}
		fr.read(header, 0, 4);
		for (int i = 0; i < 4; i++) {
			if (header[i] != WaveDecoder.fmt_[i]) {
				System.out.println(Arrays.toString(header));
				//throw new Error("fmt [" + i + "] was " + header[i]);.
				break;
			}
		}
		fr.read(header, 0, 4);
		Subchunk1Size = littleEndianToInt(header);
		fr.read(header, 0, 2);
		AudioFormat = littleEndianToInt(header);
		fr.read(header, 0, 2);
		NumChannels = littleEndianToInt(header);
		fr.read(header, 0, 4);
		SampleRate = littleEndianToInt(header);
		fr.read(header, 0, 4);
		ByteRate = littleEndianToInt(header);
		fr.read(header, 0, 2);
		BlockAlign = littleEndianToInt(header);
		fr.read(header, 0, 2);
		BitsPerSample = littleEndianToInt(header);
		fr.read(header, 0, 4);
		for (int i = 0; i < 4; i++) {
			System.out.println(Arrays.toString(header));
			if (header[i] != WaveDecoder.data[i]) {
				if (header[i] != WaveDecoder.LIST[i]) {
					//throw new Error("data[" + i + "] was " + header[i]);
				}
				fr.read(header, 0, 4);
				int ListHeaderSize = littleEndianToInt(header);
				System.out.println(ListHeaderSize+" removing list chunk");
				fr.read(new char[ListHeaderSize],0,ListHeaderSize);
				break;
			}
		}
		fr.read(header, 0, 4);
		Subchunk2Size = littleEndianToInt(header);

		System.out.println(ChunkSize + "\n" + Subchunk1Size + "\n" + AudioFormat + "\n" + NumChannels + "\n"
				+ SampleRate + "\n" + ByteRate + "\n" + BlockAlign + "\n" + BitsPerSample + "\n" + Subchunk2Size);

	}

	private int littleEndianToInt(char[] in) {
		int i3 = in[0];
		int i2 = in[1];
		int i1 = in[2];
		int i0 = in[3];
		i0 = i0 << 24;
		i1 = i1 << 16;
		i2 = i2 << 8;
		return i0 + i1 + i2 + i3;
	}
}
