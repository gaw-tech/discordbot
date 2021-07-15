package bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Config {
	static HashMap<String, ConfigObj> configs = new HashMap<>();
	static String path = "config.txt";

	public static void load() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(path));
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String name = line.substring(0, line.indexOf(':'));
			line = line.substring(line.indexOf(':') + 1, line.length());
			switch (line.charAt(0)) {
			case '{': {
				configs.put(name, new ConfigObj(ConfigType.ARRAY_STRING, line));
				break;
			}
			case '"': {
				configs.put(name, new ConfigObj(ConfigType.STRING, line));
				break;
			}
			case '[': {
				configs.put(name, new ConfigObj(ConfigType.ARRAY_LONG, line));
				break;
			}
			default: {
				configs.put(name, new ConfigObj(ConfigType.LONG, line));
				break;
			}
			}
		}
	}

	public static void save() throws IOException {
		File file = new File(path);
		FileWriter fr = new FileWriter(file);
		fr.write(getAsString());
		fr.close();
	}

	public static String getAsString() {
		String result = "";
		for (String name : configs.keySet()) {
			result += name + ":" + configs.get(name).toString() + "\n";
		}
		return result;
	}

	public static void main(String[] args) {
		try {
			load();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(Config.getAsString());
		try {
			save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ConfigObj get(String name) {
		return configs.get(name);
	}

	public static void setLine(ConfigType type, String name, String data) {
		configs.put(name, new ConfigObj(type, data));
	}
}

class ConfigObj {
	ConfigType type;
	String data;

	ConfigObj(ConfigType type, String data) {
		this.type = type;
		switch (type) {
		case ARRAY_STRING: {
			data = data.substring(data.indexOf('{') + 1, data.lastIndexOf('}'));
			break;
		}
		case ARRAY_LONG: {
			data = data.substring(data.indexOf('[') + 1, data.lastIndexOf(']'));
			break;
		}
		case STRING:
			data = data.substring(data.indexOf('"') + 1, data.lastIndexOf('"'));
			break;
		default:
			data = data.replace(" ", "");
			break;
		}
		this.data = data;
	}

	ArrayList<String> readStringArray() {
		if (!type.equals(ConfigType.ARRAY_STRING)) {
			throw new IllegalArgumentException(
					"The type was " + type + " but it should have been " + ConfigType.ARRAY_STRING);
		}
		ArrayList<String> result = new ArrayList<>();
		int current_pointer = 0;
		int start_pointer = 0;
		boolean first = false;

		while (current_pointer < data.length()) {
			if (data.charAt(current_pointer) == '"') {
				if (!first) {
					first = true;
					start_pointer = current_pointer + 1;
				} else {
					first = false;
					result.add(data.substring(start_pointer, current_pointer));
				}
			}
			current_pointer++;
		}
		return result;
	}

	long[] readLongArray() {
		if (!type.equals(ConfigType.ARRAY_LONG)) {
			throw new IllegalArgumentException(
					"The type was " + type + " but it should have been " + ConfigType.ARRAY_LONG);
		}
		String[] number_array = data.split(",");
		long[] result = new long[number_array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Long.parseLong(number_array[i].replace(" ", ""));
		}
		return result;
	}

	String readString() {
		if (!type.equals(ConfigType.STRING)) {
			throw new IllegalArgumentException(
					"The type was " + type + " but it should have been " + ConfigType.STRING);
		}
		return data;
	}

	long readLong() {
		if (!type.equals(ConfigType.LONG)) {
			throw new IllegalArgumentException("The type was " + type + " but it should have been " + ConfigType.LONG);
		}
		return Long.parseLong(data);
	}

	@Override
	public String toString() {
		switch (type) {
		case ARRAY_LONG: {
			return Arrays.toString(readLongArray());
		}
		case ARRAY_STRING: {
			String result = "{";
			for (String s : readStringArray()) {
				result += "\"" + s + "\",";
			}
			return result + "}";
		}
		case LONG:
			return "" + readLong();
		case STRING:
			return '"' + readString() + '"';
		}
		// TODO Auto-generated method stub
		return super.toString();
	}
}
