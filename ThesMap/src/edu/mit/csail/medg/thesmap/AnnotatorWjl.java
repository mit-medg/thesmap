package edu.mit.csail.medg.thesmap;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AnnotatorWjl extends Annotator{

//	UmlsWindow myWindow = null;
	int phraseLength;
	ResourceConnectorWjl wjl = null;
	
	public static final String name = "WJL";
	
	public AnnotatorWjl(UmlsWindow w) {
		super(name, w);
		myWindow = w;
		phraseLength = ThesMap.getInteger("phraseLength");
		wjl = ResourceConnectorWjl.get();
	}
	
	public static AnnotatorWjl makeAnnotatorInstance(String name, UmlsWindow w) {
		return new AnnotatorWjl(w);
	}
	
	/**
	 * Checks to see if Annotations of type umls can be made because the right
	 * resources are available.  As a side-effect, it also caches one of each
	 * such resource.
	 * @return null if OK, an error string if not.
	 */
	public static String errInit() {
		ResourceConnectorWjl wjl = ResourceConnectorWjl.get();
		if (wjl == null || !wjl.initialized)
			return "Could not initialize William J. Long annotator connector.";
		else return null;
	}

	@Override
	protected Void doInBackground() throws Exception {
		if (!wjl.initialized) return null;
		int progress = 0;
		int oldProgress = 0;
		U.log("Starting to run AnnotatorUmls.doInBackground()");
		firePropertyChange(name, 0, -1);
		String text = myWindow.textArea.getText();
		double textl = text.length();
		try {
			org.w3c.dom.Document doc = wjl.lookup(text);
			if (doc == null) {
				firePropertyChange(name,  -1, 100);
				return null;
			}
			doc.getDocumentElement().normalize();
//			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			/* The XML format returned by Bill's parser contains <p>, <br/> and text elements, plus
			 * annotations of concepts it has extracted.  These can be one of the following:
			 * 1. <concept title=STR cui=CUI type=TYPE tui=TUI from=xxx to=yyy text=ORIGINAL_TEXT>STR</concept>
			 * 2. <ambig><concept.../>...<concept.../></ambig>
			 * 3. <clause><concept.../>...<concept.../></clause> or any of the clauses can also be <ambig/>s
			 * A clause represents a head concept and modifiers.
			 * 
			 * This means we must look for concept, ambig or clause forms in the document.
			 * Concept or top-level ambigs just create interpretations
			 * Clauses are more complex because they may contain ambigs
			 */
			NodeList nListConcepts = doc.getElementsByTagName("concept");
			for (int temp = 0; temp < nListConcepts.getLength(); temp++) {
				Node nNode = nListConcepts.item(temp);
				U.log("\nCurr Elt " + temp + ": " + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) nNode;
					// <B class=BLUE title="_FINDING:C0020649:Hypotension" onClick=doRcd(this)>hypotension</B>
//					System.out.println("class="+e.getAttribute("class"));
//					System.out.println("title="+e.getElementsByTagName("title"));
// 				The representation returned by ParseMed has changed, so we need to rewrite this code according to the structures
//				listed above. First, we just look at what is there:
					
//					String title = e.getAttribute("title");
//					String[] parts = title.split(":");
//					if (parts.length < 3) U.log("Badly formed Wjl response: " + title);
//					String type = parts[0];
//					String cui = parts[1];
//					String str = parts[2];
//					String tui = e.getAttribute("tui");
//					String item = e.getTextContent();
//					Integer from = new Integer(e.getAttribute("from"));
//					Integer to = 1 + new Integer(e.getAttribute("to")); // returns last char, not next!
//					String itemFromText = text.substring(from, to);
//					if (!itemFromText.equals(item)) {
//						U.log("WJL text mismatch between \"" + item + "\" and \"" + itemFromText + "\"");
//					}
					oldProgress = progress;
//					progress = (int)Math.round((new Double(from))/textl*100.0);
					firePropertyChange(name, oldProgress, progress);
//					myWindow.annSet.add(
//							new Annotation(from, to, item,
//									new InterpretationWjl(cui, item, tui, str, type.equals("FALSE"), type)));
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void testDOM() {
		URL inJar = ClassLoader.getSystemClassLoader().getResource("wjl-example.html");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(inJar.openStream());
			doc.getDocumentElement().normalize();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("B");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
//				System.out.println("\nCurr Elt: " + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) nNode;
					// <B class=BLUE title="_FINDING:C0020649:Hypotension" onClick=doRcd(this)>hypotension</B>
//					System.out.println("class="+e.getAttribute("class"));
//					System.out.println("title="+e.getElementsByTagName("title"));
					System.out.println(//e.getAttribute("class") + 
							e.getAttribute("title") 
							+ " [" + e.getAttribute("from") + "-" + e.getAttribute("to") 
							+ "], " + e.getAttribute("tui")
							+ ": " + e.getTextContent());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
