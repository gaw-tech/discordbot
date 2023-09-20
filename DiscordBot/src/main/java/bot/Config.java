package bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import kotlin.reflect.jvm.internal.impl.types.checker.NewCapturedType;

public class Config {
	static HashMap<String, ConfigObj> configs = new HashMap<>();
	static String path = "config.txt";

	public static void load() throws FileNotFoundException {
		path = Bot.path + "/config.txt";
		File file = new File(path);
		System.out.println("confix.txt @ " + file.getAbsolutePath());
		Scanner scanner = new Scanner(file);
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
		if (!configs.containsKey(name))
			throw new NullPointerException("Could not find config \"" + name + "\" in " + path);
		return configs.get(name);
	}

	public static void setLine(ConfigType type, String name, String data) {
		configs.put(name, new ConfigObj(type, data));
	}

	public static File newFileRoutine() {
		Scanner scanner = new Scanner(System.in);
		boolean validAnswer = false;
		boolean createNewFile = false;
		while (!validAnswer) {
			System.out.println("Create new file? [y/n]");
			String token = scanner.next();
			if (token.equalsIgnoreCase("y")) {
				createNewFile = true;
				validAnswer = true;
			} else if (token.equalsIgnoreCase("n")) {
				validAnswer = true;
			}
		}
		File file = null;
		if (!createNewFile) {
			System.out.println("Not creating a new file.");
		} else {
			file = new File(path);
			System.out.println("Created a new file in " + file.getAbsolutePath() + ".");
		}
		// scanner.close();
		return file;
	}
}
