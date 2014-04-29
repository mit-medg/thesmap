package edu.mit.csail.medg.thesmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class is a non-interactive version of UmlsWindow, for batch processing.
 * It opens a given file, annotates it, and writes the results to a csv file with
 * the same name.csv. 
 * @author psz
 *
 */


public class UmlsDocument implements Runnable {
	
	String text = null;
	File inFile;
	AnnotationSet annSet = null;

//	private static final Pattern spaces = Pattern.compile("\\s+|$");
	
	UmlsDocument(File inFile) {
		this.inFile = inFile;
	}	
	
	public void run() {

		FileInputStream is = null;
		try {
			is = new FileInputStream(inFile);
			text = getContent(is);
//			annotate(ThesMap.getInteger("phraseLength"));
			UmlsWindow.saveAnnotations(csvFile(inFile), annSet);
		} catch (FileNotFoundException e) {
			System.err.println("File " + inFile + " not found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading " + is + ": " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	
	private static File csvFile(File inFile) {
		String name = inFile.getName();
		int dot = name.lastIndexOf('.');
		String newName = (dot < 1) ? name + ".csv" : name.substring(0, dot) + ".csv";
		return new File(inFile.getParentFile(), newName);
	}
	
	private String getContent(InputStream is) throws IOException {
		// This works for either File or Uri input
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = in.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Creates annotations on text.
	 * This is a copy of annotate in UmlsWindow, but is not synchronized because
	 * we expect multiple runs at the same time (on different documents).
	 * Creates an AnnotationSet and saves it as the instance variable annSet.
	 * @param phraseLength Maximum number of tokens in a phrase
	 */
	/*
	public void annotate(int phraseLength) {
		annSet = new AnnotationSet(phraseLength);
		SpaceRecord sr = new SpaceRecord(phraseLength);
		// Tokenize by spaces
		Matcher m = spaces.matcher(text);
		while (m.find()) {
			int here = m.start();	// next found space beginning
			sr.add(m.end());		// end of space = beginning of next word
			//TheMap.log(sr.toShow());
			// Consider all phrases starting phraseLength words back from here
			for (int back = 1; back <= phraseLength; back++) {
				int start = sr.getPrevStart(back);
				if (start >= 0) {
					String phrase = text.substring(start, here);
					InterpretationSet i = InterpretationSet.lookup(phrase);
					if (i != null && i != InterpretationSet.nullInterpretationSet) {
						// Add these interpretations to the annotations unless they duplicate
						// or cover the same interpretations that are already present.
						// See note in documentation. 
						annSet.integrate(new Annotation(start, here, phrase, i));
					}
				}
			}
		}
		*/
	
	
//	protected void saveAnnotations(File chosenFile) {
//		FileOutputStream fos = null;
//		OutputStreamWriter osw = null;
//		try {
//			fos = new FileOutputStream(chosenFile);
//			try {
//				osw = new OutputStreamWriter(fos, "UTF-8");
//				if (osw != null) {
//					for (Annotation ann: annSet) {
//						for (Interpretation i: ann.getInterpretationSet().getInterpretations()) {
//							osw.write(ann.begin + "," + ann.end + "," + i.cui + "," + i.tui 
//									+ ",\"" + UmlsWindow.fixq(i.str) + "\"\n");
//						}
//					}
//				}
//				osw.close();
//				fos.close();
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}


}
