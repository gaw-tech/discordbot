package bot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class MyClassLoader extends ClassLoader {
	private String path = Bot.path;
	private String root = path + "/java";

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// if(!"reflection.MyObject".equals(name)) //i have no clue what this would be
		// good for but it was in the original code so i'll leave it here for now.
		// return super.loadClass(name);
		// speaking of original code i have no clue where it came from. i should find
		// that out.

		try {
			// Prepare source somehow.
			File file = new File(path + "/commands/" + name + ".java");
			String source = "";
			Scanner scanner;
			scanner = new Scanner(file);
			while (scanner.hasNext()) {
				source = source + scanner.nextLine() + "\n";
			}
			scanner.close();

			// Create subfolder <bot executable>/java if it does not exist. Maybe i could
			// this when creating this class so it only has do be done once.
			File root = new File(this.root);
			if (!root.exists()) {
				root.mkdir();
			}
			// Save source in the subfolder
			File sourceFile = new File(root, "commands/" + name + ".java");
			sourceFile.getParentFile().mkdirs();
			Files.write(sourceFile.toPath(), source.getBytes(StandardCharsets.UTF_8));

			// Compile source file.
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			compiler.run(null, null, null, sourceFile.getPath());

			// Load and instantiate compiled class.
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
			Class<?> cls = Class.forName("commands." + name, false, classLoader);

			return cls;

		} catch (IOException e) {
			try { // This should allow to load classes which where present when the bot was compiled.
				File root = new File(this.root);
				if (!root.exists()) {
					root.mkdir();
				}
				// Load and instantiate compiled class.
				URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
				Class<?> cls = Class.forName("commands." + name, false, classLoader);

				return cls;
			} catch (IOException e1) {
				System.out
						.println("Could not load the provided class. Maybe the java file is not in the required path.");
				e.printStackTrace();
				throw new ClassNotFoundException();
			}
		}
	}

}