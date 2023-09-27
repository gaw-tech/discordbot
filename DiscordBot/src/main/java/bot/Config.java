package bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Config {
	static HashMap<String, ConfigObj> configs = new HashMap<>();
	static String path = "config.txt";

	/*
	 * Tries to open the config.txt file which should be in the same folder as the main executable of the bot.
	 * Then fills in the configs map.
	 * Throws a FileNotFoundException if it can't find the config.txt file.
	 */
	public static void load() throws FileNotFoundException {
		path = Bot.path + "/config.txt";
		File file = new File(path);
		System.out.println("confix.txt @ " + file.getAbsolutePath());
		// probably a bad idea
		@SuppressWarnings("resource")
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

	/*
	 * Saves the configs to config.txt.
	 * Throws IOException.
	 */
	public static void save() throws IOException {
		File file = new File(path);
		FileWriter fr = new FileWriter(file);
		fr.write(getAsString());
		fr.close();
	}

	/*
	 * Turns the config into a string. Internally used to save the config.
	 */
	private static String getAsString() {
		String result = "";
		for (String name : configs.keySet()) {
			result += name + ":" + configs.get(name).toString() + "\n";
		}
		return result;
	}

	/*
	 * Returns the config with the given name.
	 * Returns null if the config with the given name does not exist.
	 */
	public static ConfigObj get(String name) {
		if (!configs.containsKey(name))
			throw new NullPointerException("Could not find config \"" + name + "\" in " + path);
		return configs.get(name);
	}

	/*
	 * TODO
	 */
	public static void setLine(ConfigType type, String name, String data) {
		configs.put(name, new ConfigObj(type, data));
	}

	/* 
	 * Starts a short dialog asking if a new config file should be created.
	 * Returns the config file if it was created else it returns null.
	 */
	public static File newFileRoutine() {
		// we leave the scanner open so we still use it later
		@SuppressWarnings("resource")
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
		return file;
	}
}
