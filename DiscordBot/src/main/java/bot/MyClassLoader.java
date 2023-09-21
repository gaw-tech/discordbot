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
	private String javaRoot = path +"/java";

	public MyClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// if(!"reflection.MyObject".equals(name)) //i have no clue what this would be
		// good for but it was in the original code so i'll leave it here for now.
		// return super.loadClass(name);
		//speaking of original code i have no clue where it came from. i should find that out.

		// Prepare source somehow.
		try {
			//File file = new File("C:\\Users\\Georg\\Desktop\\classtest\\" + name + ".java");
			//File file = new File("/home/pi/commands/" + name + ".java");
			File file = new File(path + "/commands/"+ name + ".java");
			System.out.println("the next line is the path as a string\n"+path + "/commands/"+ name + ".java");
			String source = "";
			Scanner scanner;
			scanner = new Scanner(file);
			while (scanner.hasNext()) {
				source = source + scanner.nextLine() + "\n";
			}

			// Save source in .java file.
			//File root = new File("/home/pi/java");
			//File root = new File("C:\\java"); // On Windows running on C:\, this is C:\java.
			File root = new File(javaRoot);
			if(!root.exists()) {
				root.mkdir();
			}
			File sourceFile = new File(root, "commands/" + name + ".java");
			sourceFile.getParentFile().mkdirs();
			Files.write(sourceFile.toPath(), source.getBytes(StandardCharsets.UTF_8));

			// Compile source file.
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			compiler.run(null, null, null, sourceFile.getPath());

			// Load and instantiate compiled class.
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
			Class<?> cls = Class.forName("commands." + name, false, classLoader);

			scanner.close();
			return cls;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("this is from the class loaders catch");
			throw new ClassNotFoundException();
		}
	}

}