package edu.mit.csail.medg.thesmap;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;


public class AnnotatorcTakes extends Annotator{

	UmlsWindow myWindow = null;
	int phraseLength;
	ResourceConnectorcTakes cTakes = null;
	ResourceConnectorUmls umls = null;
	AnnotationSet tempAnnSet;
	
	public static final String name = "cTakes";
	
	public AnnotatorcTakes(UmlsWindow w) {
		super(name, w);
		myWindow = w;
		phraseLength = ThesMap.getInteger("phraseLength");
		cTakes = ResourceConnectorcTakes.get();
		umls = ResourceConnectorUmls.get();
		tempAnnSet = new AnnotationSet();
	}
	
	public static AnnotatorcTakes makeAnnotatorInstance(String name, UmlsWindow w) {
		return new AnnotatorcTakes(w);
	}
	
	/**
	 * Checks to see if Annotations of type umls can be made because the right
	 * resources are available.  As a side-effect, it also caches one of each
	 * such resource.
	 * @return null if OK, an error string if not.
	 */
	public static String errInit() {
		ResourceConnectorcTakes cTakes = ResourceConnectorcTakes.get();
		if (cTakes == null || !cTakes.initialized)
			return "Could not initialize cTakes connector.";
		else return null;
	}

	@Override
	protected Void doInBackground() throws Exception {
		U.log("Starting to run AnnotatorCTakes.doInBackground()");
		long startTime = System.nanoTime();
		
		firePropertyChange(name, 0, -1);
        process(myWindow.textArea.getText());
        
        addAnnotationSet();
		
		long diff = System.nanoTime() - startTime;
		U.log("AnnotatorcTakes elapsed time (ms): " + diff/1000000);
		
        return null;
	}
	
    /**
     * Process text using cTakes API and create Annotations.
     *
     * @param text the input text
     */
    void process(String text) throws Exception {
    	cTakes.process(text);
    }
    
    /** 
     * Get the Annotation set
     */
    protected void addAnnotationSet() {
    	ConcurrentHashMap<IdentifiedAnnotation, InterpretationSet> ans = ResourceConnectorcTakes.ctakesInterpretations;
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
