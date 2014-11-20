package edu.mit.csail.medg.thesmap;


public class AnnotatorcTakes extends Annotator{

	UmlsWindow myWindow = null;
	int phraseLength;
	ResourceConnectorcTakes cTakes = null;
	ResourceConnectorUmls umls = null;
	
	public static final String name = "cTakes";
	
	public AnnotatorcTakes(UmlsWindow w) {
		super(name, w);
		myWindow = w;
		phraseLength = ThesMap.getInteger("phraseLength");
		cTakes = ResourceConnectorcTakes.get();
		umls = ResourceConnectorUmls.get();
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
		firePropertyChange(name, 0, -1);
        process(myWindow.textArea.getText());
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
    
    static void log(String s) {
        if (doLog) U.log(s);
    }

    static final boolean doLog = false;
	
}
