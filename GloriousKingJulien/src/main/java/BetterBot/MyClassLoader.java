package BetterBot;

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

	public MyClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// if(!"reflection.MyObject".equals(name))
		// return super.loadClass(name);

		// Prepare source somehow.
		try {
			//Path path = Paths.get("C:\\Users\\Georg\\Desktop\\classtest\\" + name + ".java");
			/*Path path = Paths.get("/home/pi/commands/" + name + ".java");

			StringBuilder sb = new StringBuilder();

			Stream<String> stream = Files.lines(path);
			stream.forEach(s -> sb.append(s).append("\n"));

			String source = sb.toString();*/

			//File file = new File("C:\\Users\\Georg\\Desktop\\classtest\\" + name + ".java");
			File file = new File("/home/pi/commands/" + name + ".java");
			String source = "";
			Scanner scanner;
			scanner = new Scanner(file);
			while (scanner.hasNext()) {
				source = source + scanner.nextLine() + "\n";
			}
			System.out.println(source);

			// Save source in .java file.
			File root = new File("/home/pi/java");
			//File root = new File("C:\\java"); // On Windows running on C:\, this is C:\java.
			File sourceFile = new File(root, "commands/" + name + ".java");
			sourceFile.getParentFile().mkdirs();
			Files.write(sourceFile.toPath(), source.getBytes(StandardCharsets.UTF_8));

			// Compile source file.
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			compiler.run(null, null, null, sourceFile.getPath());

			// Load and instantiate compiled class.
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
			Class<?> cls = Class.forName("commands." + name, false, classLoader); // Should print "hello".

			return cls;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("this is from the class loaders catch");
			throw new ClassNotFoundException();
		}
	}

}