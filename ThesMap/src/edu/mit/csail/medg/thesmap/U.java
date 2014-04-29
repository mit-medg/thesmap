package edu.mit.csail.medg.thesmap;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;

import javax.swing.SwingUtilities;

/**
 * A simple Utility class to allow us to shorten some commands
 * @author psz
 *
 */
public class U {
	
	public static final int NO_LOG = 0;
	public static final int LOG_TO_OUT = 1;
	public static final int LOG_TO_LOG_WINDOW = 2;
	
	public static int logging = LOG_TO_LOG_WINDOW;
	
	public static final int PRINT_TO_OUT = 1;
	public static final int PRINT_TO_LOG = 2;
	
	public static int printing = PRINT_TO_LOG;

	public static final int ERR_TO_OUT = 1;
	public static final int ERR_TO_LOG = 2;
	
	public static int erring = ERR_TO_LOG;
	
	public static LogWindow log = null;
	public static DebugWindow debugW = null;

	public static synchronized void p(String s) {
		if (printing == PRINT_TO_LOG) log(s);
		else System.out.println(s);
	}
	
	public static synchronized void pe(String s) {
		if (erring == ERR_TO_LOG) log("Err: " + s);
		else System.err.println(s);
	}
	
	/**
	 * Log a string. If the log window does not exist, create and run it.
	 * @param s
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public static synchronized void log(String s) {
		if (logging == LOG_TO_OUT) System.out.println(s);
		else if (logging == LOG_TO_LOG_WINDOW) {
			if (log == null) {
				log = new LogWindow("Log Window");
				try {
					SwingUtilities.invokeAndWait(log);
				} catch (InvocationTargetException | InterruptedException e) {
					System.err.println("Unable to initiate LogWindow.");
					e.printStackTrace();
					logging = LOG_TO_OUT;
					log = null;
				}
			}
			if (log != null) log.println(s);
			else System.out.println("Log: " + s);
		}
	}
	
	public static void setLogging(int val) {
		logging = val;
	}
	
	public static void setPrinting(int val) {
		printing = val;
	}
	
	public static void setErring(int val) {
		erring = val;
	}
	
	public static void debug(String s) {
		if (ThesMap.debug) {
			if (debugW == null) {
				debugW = new DebugWindow("Debug");
				try {
					SwingUtilities.invokeAndWait(debugW);
				} catch (InvocationTargetException | InterruptedException e) {
					System.err.println("Unable to initiate Debug Window.");
					e.printStackTrace();
					debugW = null;
				}
			}
			if (debugW != null) debugW.println(s);
		}
	}
	
	public static String showMatcher(Matcher m) {
		int n = m.groupCount();
		StringBuilder sb = new StringBuilder("Match: \"");
		sb.append(m.group()).append("\"");
		for (int i = 1; i <= n; i++) {
			String matched = m.group(i);
			if (matched != null) {
				sb.append(", [").append(i).append(":").append(m.start(i)).append("-");
				sb.append(m.end(i)).append("] \"").append(m.group(i)).append("\"");
			}
		}
		return sb.toString();
	}
	
	public static void debugMatcher(Matcher m) {
		debug(showMatcher(m));
	}
	
	/**
	 * Returns an abbreviated version of its argument, to default length
	 * given by defaultAbbrevLength.  The middle of the string is elided and
	 * substituted by ..., about 2/3 of the way.
	 * @param s The String to abbreviate.
	 * @return The abbreviated String.
	 */
	public static String abbrev(String s) {
		return abbrev(s, defaultAbbrevLength, 0, null);
	}
	
	/**
	 * Gives the default length of abbreviated strings.
	 */
	static final int defaultAbbrevLength = 80;
	
	/**
	 * Returns an abbreviated version of its 1st argument, to a length
	 * given by its 2nd.  The middle of the string is elided and
	 * substituted by ..., about 2/3 of the way.
	 * @param s The String to abbreviate.
	 * @param length The length to which to abbreviate.
	 * @return The abbreviated String.
	 */
	public static String abbrev(String s, Integer length) {
		return abbrev(s, (length == null) ? defaultAbbrevLength : length, 0, null);
	}
	
	/**
	 * Combines substring and abbrev.
	 * @param s The String to abbreviate.
	 * @param length The length to which to abbreviate.
	 * @param beg Starting position within the String.
	 * @return The abbreviated substring.
	 */
	public static String abbrev(String s, int length, int beg) {
		return abbrev(s, length, beg, null);
	}
	
	/**
	 * Combines substring and abbrev.
	 * @param s The String to abbreviate.
	 * @param length The length to which to abbreviate.
	 * @param beg Starting position within the String.
	 * @param end Ending position within the String.
	 * @return The abbreviated substring.
	 */
	public static String abbrev(String s, int length, Integer beg, Integer end) {
		if (end == null) end = s.length();
		int init = (int)Math.round(2.0d * length / 3.0d);
		int fin = (int)Math.max(0, length - init - 3);
		if (end - beg <= length) return s.substring(beg, end);
		return s.substring(beg, init) + "..." + s.substring(end-fin);
	}

}
