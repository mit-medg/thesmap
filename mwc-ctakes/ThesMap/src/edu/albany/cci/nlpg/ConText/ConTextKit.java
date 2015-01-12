
package edu.albany.cci.nlpg.ConText;

import java.io.*;
import java.util.*;

public class ConTextKit {

	/**
	 * Given a set of triggers, phrases, and sentences to process, runs Wendy
	 * Chapman's ConText algorithm over the sentences. Annotations are made for
	 * detected trigger terms and phrases are flagged in the sentence.
	 */
	public static void main(String[] args) {
		
		/* Local variables */
		ConText con = new ConText();
		boolean modeSilent = false;
		String triggerFilename = "";
		String phraseFilename = "";
		String sentenceFilename = "";
		String outputFilename = "";
		String arg = "";
		String param = "";
		String result;
		PrintWriter outputFile = null;
		ArrayList<String> triggers = new ArrayList<String>();
		ArrayList<String> phrases = new ArrayList<String>();
		
		/* Get proper system newline */
		String newline = System.getProperty("line.separator");
		
		/* Help information screen string */
		String helpInfo = "Usage: ConTextKit [OPTION] [TRIGGER_FILE]"
			+ newline + "Run the ConText algorithm by Wendy Chapman over one or more sentences and return the annotated results."
			+ newline
			+ newline + "  -q, --silence            silence the standard out"
			+ newline + "  -s, --sentences=FILE     use a file containing a list of sentences"
			+ newline + "  -p, --phrases=FILE       use a file containing a list of phrases"
			+ newline + "  -o, --output=FILE        save results to a specified file"
			+ newline + "  -v, --verbose            include tags for trigger terms"
			+ newline + "      --help               display this help and exit"
			+ newline + "      --version            output version information and exit"
			+ newline
			+ newline + "Example:  ConTextKit --sentences=sentences.txt -q triggers.txt"
			+ newline
			+ newline + "Note: If no sentence file is provided, ConTextKit expects "
					  + "to receive sentences via stdin, separated by a newline. "
					  + "If there is no newline separating sentences, the scope will be "
					  + "incorrect and unexpected results will occur.";

		String versionInfo = "ConTextKit v0.1" + newline + "by Ken Burford";
		
		/********************************
		 * Command line argument parser *
		 ********************************/
		try {
		
			/* Print the help and version screens */
			if (args[0].equals("--help")) {
				System.out.println(helpInfo);
				System.exit(0);
			}
			else if (args[0].equals("--version")) {
				System.out.println(versionInfo);
				System.exit(0);
			}
			
			/* If no arguments, print usage info */
			if (args.length == 0) printUsageExit(-1);
			
			/* Loop through arguments */
			for (int x = 0; x < args.length; x++) {
				
				/* Check if this is the last arg, which should be the triggers */
				if (x == args.length-1) {
					triggerFilename = args[x];
					break;
				}
				
				/* If the argument is of a proper length.. */
				if (args[x].length() > 1) {
					
					/* For parameters that start with "--" ... */
					if (args[x].substring(0, 2).equals("--")) {
						
						/* Do we want to silence stdout? */
						if (args[x].equals("--silence")) {
							modeSilent = true;
							continue;
						}
						/* Enable verbose mode */
						else if (args[x].equals("--verbose")) {
							con.VERBOSE_MODE = true;
							continue;
						}
						/* Ensure that there is an equals sign for others */
						if (args[x].contains("=")) {
							String[] split = args[x].split("=");
							/* If nothing was found after the equals sign.. */
							if (split.length != 2) printUsageExit(-1);
							/* Remove preceding "--" */
							if (split[0].length() > 2) {
								split[0] = split[0].substring(2);
							}
							if (split[0].equals("sentences")) {
								sentenceFilename = split[1];
							}
							else if (split[0].equals("phrases")) {
								phraseFilename = split[1];
							}
							else if (split[0].equals("output")) {
								outputFilename = split[1];
							}
							else printUsageExit(-1);
						}
						/* Invalid parameters */
						else printUsageExit(-1);
					}
					/* For parameters that start with "-" ... */
					else if (args[x].substring(0, 1).equals("-")) {
						
						/* If this is an invalid switch, die */
						if (args[x].length() < 2) printUsageExit(-1);
						/* Otherwise, save the switch */
						else arg = args[x].substring(0, 2);
						
						/* Silence standard out */
						if (args[x].equals("-q")) {
							modeSilent = true;
							continue;
						}
						/* Enable verbose mode */
						else if (args[x].equals("-v")) {
							con.VERBOSE_MODE = true;
							continue;
						}
						/* If the switch has no space between itself
						 * and the parameter following it */
						if (args[x].length() > 2) {
							param = args[x].substring(2);
						}
						/* Otherwise, it's the next arg passed in */
						else {
							x++;
							param = args[x];
						}
						/* Use the parameter to set attributes */
						if (arg.equals("-s")) {
							sentenceFilename = param;
						}
						else if (arg.equals("-p")) {
							phraseFilename = param;
						}
						else if (arg.equals("-o")) {
							outputFilename = param;
						}
						else printUsageExit(-1);
					}
					
				}
				else printUsageExit(-1);
			}
		} /* try */
		catch (Exception ex) {
			printUsageExit(-1);
		}
		
		/* Read in files and feed them into ConText */
		try {
			
			/* If the user has a phrase file.. */
			if (phraseFilename != "") {
				File phraseFile = new File(phraseFilename);
				Scanner phraseScan = new Scanner(phraseFile);
				while (phraseScan.hasNextLine()) {
					phrases.add(phraseScan.nextLine());
				}
				phraseScan.close();
			}
			
			/* If the user specified an output file.. */
			if (outputFilename != "") {
				outputFile = 
					new PrintWriter(new BufferedWriter(new FileWriter(outputFilename)));
			}
			
			/* Open trigger file */
			File triggerFile = new File(triggerFilename);
			Scanner triggerScan = new Scanner(triggerFile);
			
			/* Read in triggers and add to an array list */
			while (triggerScan.hasNextLine()) {
				triggers.add(triggerScan.nextLine());
			}
			
			try {
				
				/* Pre-sort lists and disable ConText internal sorting */
				triggers = con.sortTerms(triggers);
				phrases = con.sortTerms(phrases);
				con.DISABLE_SORT = true;
				
				/* If we're reading sentences from a file.. */
				if (sentenceFilename != "") {
					
					/* Open the file */
					File sentenceFile = new File(sentenceFilename);
					Scanner sentenceScan = new Scanner(sentenceFile);
					
					while (sentenceScan.hasNextLine()) {
						String sentence = sentenceScan.nextLine().trim();
						result = con.getConText(sentence, triggers, phrases);
						if (modeSilent == false) System.out.println(result);
						if (outputFilename != "") {
							outputFile.write(result + newline);
						}
					}
					sentenceScan.close();
					
				}
				/* Otherwise, look to stdin */
				else {
					
					/* Initialize access to stdin */
					BufferedReader stdin = 
						new BufferedReader (new InputStreamReader(System.in));
					
					while (true) {
						String sentence = stdin.readLine().trim();
						result = con.getConText(sentence, triggers, phrases);
						if (modeSilent == false) System.out.println(result);
						if (outputFilename != "") {
							outputFile.write(result + newline);
						}
					}
					
				}
				
				result = con.getConText(
						"Family \nHisTory; [PHRASE=APR][D]_woot[PHRASE], 'ro::::\" is a cool apart \n froom preVIous \"[PHRASE=APR]pHrase_\n_test[PHRASE] free woooot.", 
						triggers, phrases);
				System.out.println(result);
				
			}
			catch (Exception e) {
				System.out.println(e);
			}

			if (outputFilename != "") outputFile.close();
			
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		
		/* Exit successfully */
		System.exit(0);
		
	} /* main */
	
	public static void printUsageExit(int status) {
		
		/* Get proper newline */
		String newline = System.getProperty("line.separator");
		
		/* Usage info string */
		String usageInfo = "Usage: ConTextKit [OPTION] [TRIGGER_FILE]"
			+ newline +    "Try `ConTextKit --help' for more information.";
		
		System.out.println(usageInfo);
		System.exit(status);
		
	} /* printUsageExit */

} /* ConTextKit */
