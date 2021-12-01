package com.gradle.ide.logger;

import java.io.PrintStream;
import java.io.OutputStream;
import com.gradle.ide.util.FileUtil;
import com.gradle.ide.service.ApplicationLoader;
public class SystemLogPrinter {

    public static void start(Logger logger) {
        //reset
        FileUtil.writeFile(ApplicationLoader.applicationContext.getExternalFilesDir(null) + "/logs.txt", "");

        PrintStream ps = new PrintStream(new OutputStream() {
				private String cache;

				@Override
				public void write(int b) {
					if (cache == null) cache = "";

					if (((char) b) == '\n') {
						//write each line printed to the specified path
						// com.apk.builder.logger.d("System.out", cache);
						FileUtil.writeFile(ApplicationLoader.applicationContext.getExternalFilesDir(null) + "/logs.txt",
						FileUtil.readFile(ApplicationLoader.applicationContext.getExternalFilesDir(null) + "/logs.txt") + "\n" + cache);

						cache = "";
					} else {
						cache += (char) b;
					}
				}
			});

        System.setOut(ps);
        System.setErr(ps);
    }
}
