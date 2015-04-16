package edu.mit.csail.medg.thesmap;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.UIMAException;
import org.xml.sax.SAXException;


public class AnnotatorcTakes extends Annotator{

	UmlsWindow myWindow = null;
	int phraseLength;
	ResourceConnectorcTakes cTakes = null;
	
	public static final String name = "cTakes";
	
	public AnnotatorcTakes(UmlsWindow w) {
		super(name, w);
		myWindow = w;
		phraseLength = ThesMap.getInteger("phraseLength");
		cTakes = ResourceConnectorcTakes.get();
	}
	
	public static AnnotatorcTakes makeAnnotatorInstance(String name, UmlsWindow w) {
		return new AnnotatorcTakes(w);
	}
	
	/**
	 * Checks to see if Annotations of type ctakes can be made because the right
	 * resources are available.  As a side-effect, it also caches one of each
	 * such resource.
	 * @return null if OK, an error string if not.
	 */
	public static String errInit() {
		ResourceConnectorcTakes cTakes = ResourceConnectorcTakes.get();
		if (cTakes == null || !cTakes.initialized)
			return "Could not initialize cTakes connector.";
		else {
			return null;
		}
	}

	@Override
	protected Void doInBackground() throws Exception {
		/** 
		 * If there is a GUI open, we don't want to interrupt the GUI for cTAKES.
		 * If it is being done in the batch process, we want to make sure that we wait the proper amount of time.
		 */
		if (ThesMap.interactive) {
			runProcess();
		} else {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					runProcess();
				}
			});
		}
        return null;
	}
	
	private void runProcess() {
		U.log("Starting to run AnnotatorCTakes.doInBackground()");
		long startTime = System.nanoTime();
		
		firePropertyChange(name, 0, -1);
		String currentText = myWindow.textArea.getText();
		ConcurrentHashMap<IdentifiedAnnotation, InterpretationSet> result;
		try {
			result = cTakes.process(currentText);
	        addAnnotationSet(result);
		} catch (UIMAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		long diff = System.nanoTime() - startTime;
		U.log("AnnotatorcTakes elapsed time (ms): " + diff/1000000);
	}
	
	@Override
	public void done() {
		if (myWindow != null) {
			myWindow.annotatorDone(name);
		}
	}
    
    /** 
     * Get the Annotation set
     */
    protected void addAnnotationSet(ConcurrentHashMap<IdentifiedAnnotation, InterpretationSet> result) {
    	//System.out.println("adding to the annotation set");
    	ConcurrentHashMap<IdentifiedAnnotation, InterpretationSet> ans = result;
    	Iterator<IdentifiedAnnotation> iter = ans.keySet().iterator();
    	 
        while(iter.hasNext()){
            IdentifiedAnnotation key = iter.next();
            InterpretationSet is = ans.get(key);
			if (is != null && is != InterpretationSet.nullInterpretationSet) {
				myWindow.annSet.integrate(new Annotation(key.getBegin(), key.getEnd(), key.getCoveredText(), is));
			}
        }
    }
    
    static void log(String s) {
        if (doLog) U.log(s);
    }

    static final boolean doLog = false;
	
}
