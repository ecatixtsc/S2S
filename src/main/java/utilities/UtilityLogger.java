/**
 * 
 */
package utilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

/**
 * Utility class to provide static methods related to logger.
 * 
 *
 */
public final class UtilityLogger {

	/**
	 * Default directory to store logs.
	 */
	private static final String DEFAULT_LOG_LOCATION = "logs";

	/**
	 * @param nameOfFolderForLogs full name of the path to keep logs
	 * @throws Exception error message to inform the user
	 */
	private static void checkForDirectories(final String nameOfFolderForLogs) {
		Path path = Paths.get(nameOfFolderForLogs);
		if (!Files.exists(path)) {
			File newDirectory = (new File(path.toString()));
			newDirectory.mkdirs();
		}
	}

	/**
	 * Enhances the standard process to create logger by creating any required
	 * directories.
	 * 
	 * @param clazz               class to log
	 * @param nameOfFolderForLogs full name of the path to keep logs
	 * @return Logger a new loger
	 */
	public static Logger getLogger(final Class<?> clazz, final String nameOfFolderForLogs) {
		checkForDirectories(nameOfFolderForLogs);
		return Logger.getLogger(clazz);
	}

	/**
	 * Create a logger with the default directory.
	 * 
	 * @param clazz class to log
	 * @return Logger a new logger
	 */
	public static Logger getLogger(final Class<?> clazz) {
		return getLogger(clazz, DEFAULT_LOG_LOCATION);
	}

	/** Private constructor for utility class. */
	private UtilityLogger() {
	}

}
