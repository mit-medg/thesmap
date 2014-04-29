package edu.mit.csail.medg.thesmap;


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
		// TODO Auto-generated method stub
		return null;
	}
}
