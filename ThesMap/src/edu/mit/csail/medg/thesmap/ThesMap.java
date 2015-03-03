package edu.mit.csail.medg.thesmap;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


/**
 * ThesMap (Thesaurus Map) implements a utility to interpret clinical texts
 * according to several possible methods.  It provides either a command-line 
 * invocation that begins processing a collection of files in parallel or
 * an interactive invocation that allows the user to select the type(s)
 * of interpretation to perform and the annotations to display.
 * 
 * The program relies on local and (possibly) remote resources such as a
 * MySQL database holding the UMLS metathesaurus, the UMLS LVG/norm program,
 * NLM's MetaMap server, and cTakes.  Some of these need to be configured on 
 * the machine where ThesMap is being run, but many may be accessed via 
 * internet protocols.
 */

/**
 * @author psz
 *
 */
public class ThesMap {

	// The global Properties are shared by all components of ThesMap.
	public static ThesProps prop = null; 
	public static boolean interactive = true;
	public static boolean debug = true;
	static final String exampleFileName = "Example.txt";
//	static final Pattern basicPattern = Pattern.compile(
//			"(\\b[A-Za-z%]\\w*?)?(\\s*[:<>=~-]\\s*)?"
//					+ "([+-]?\\d+\\.?|[+-]?\\d*\\.\\d+)(([/-])(\\d+\\.?|\\d*\\.\\d+))?"
//					+ "([-/])?([a-zA-Z%]+(/([a-zA-Z%]+))?)?");
	
//	public static Console console = null;
//	public static boolean headless = false;

	/** The main program.  
	 * @param args
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	public static void main(String[] args) throws InvocationTargetException, InterruptedException {


		// Set up for OS X interface if we are running on such a system
		setUpOSX();		
		U.setLogging(U.LOG_TO_LOG_WINDOW);

		U.log("Starting ThesMap");
		
		// Find and load the ThesMap.properties file.
		prop = new ThesProps();
		U.log(prop.toShow());
		U.log("WJL:" + prop.getProperty("WJLUrl") + " " + prop.getProperty(ThesProps.wjlUrl));
		
		// Determine if we are interactive or batch, depending on whether we have a Console.
//		console = System.console();
//		headless = GraphicsEnvironment.isHeadless();
//		System.out.println(console);
//		if (console != null) console.format("Message on console " + console + "\n");
//		else if (!headless) JOptionPane.showMessageDialog(null, "Foo!");
		// Determine if we are running in batch or interactive
		// If command line argument filenames were given, then batch. 
//		System.exit(0);
		for (int i = 0; i < args.length; i++) {
			if (!args[i].startsWith("-")) {
				interactive = false;
				break;
			}
			U.log("Program arg " + i + ": " + args[i]);
		}
		
		// Check to see which resource connectors are available, 
		// and create connections to one of each.
		setUpResourceConnectors();	
//		String type = AnnotatorUmls.name;
//		Integer indx = Annotator.getIndex(type);
//		System.out.println(type + "-->" + indx);
//		System.out.println(Annotator.getBitSet(type));
		
		if (interactive) {
//			U.debug("Starting Interactive Session");
			try { 
				URL inJar = ClassLoader.getSystemClassLoader().getResource(exampleFileName);
				URI inJarURI = inJar.toURI();
				SwingUtilities.invokeLater(new UmlsWindow(inJarURI));
			} catch (URISyntaxException e) {
				U.pe("Unable to load example file " + exampleFileName);
				SwingUtilities.invokeLater(new UmlsWindow());
			}
		}
		else {
			SwingUtilities.invokeLater(new BatchWindow());
		}
	}

	static void setUpOSX() {
		try {

			if (System.getProperty("os.name").contains("OS X")) {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TheMap");
			} 
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} 
		catch(UnsupportedLookAndFeelException e) {
			System.err.println("UnsupportedLookAndFeelException: " + e.getMessage());
		}
		catch(ClassNotFoundException e) {
			System.err.println("ClassNotFoundException: " + e.getMessage());
		}
		catch(InstantiationException e) {
			System.err.println("InstantiationException: " + e.getMessage());
		}
		catch(IllegalAccessException e) {
			System.err.println("IllegalAccessException: " + e.getMessage());
		}
	}
	
	/**
	 * Set up and register each different type of annotator that we include.
	 * Every annotator has a short display name, which is in the static name
	 * variable of its class.  It also has a class name, which we register
	 * along with the short name.  This is used for generic annotator dispatch.
	 * 
	 */
	private static void setUpResourceConnectors() {
		String errUmls = AnnotatorUmls.errInit();
		if (errUmls == null) {
			U.log("Created UMLS and LVG/norm connectors. UMLS Annotations are feasible.");
			Annotator.registerType(AnnotatorUmls.name, "AnnotatorUmls");
		} else {
			U.pe(errUmls);
		}
		String errUmlsBase = AnnotatorUmlsBase.errInit();
		if (errUmlsBase == null) {
			U.log("Created UMLS Base connectors. UMLS Base Annotations are feasible.");
			Annotator.registerType(AnnotatorUmlsBase.name, "AnnotatorUmlsBase");
		} else {
			U.pe(errUmlsBase);
		}
		String errMM = AnnotatorMetaMap.errInit();
		if (errMM == null) {
			U.log("Created MetaMap connector. MetaMap Annotations are feasible.");
			Annotator.registerType(AnnotatorMetaMap.name, "AnnotatorMetaMap");
		} else {
			U.pe(errMM);
		}
		String errcTakes = AnnotatorcTakes.errInit();
		if (errcTakes == null) {
			U.log("Created cTakes connector. cTakes Annotations are feasible.");
			Annotator.registerType(AnnotatorcTakes.name, "AnnotatorcTakes");
		} else {
			U.pe(errcTakes);
		}
		String errWjl = AnnotatorWjl.errInit();
		if (errWjl == null) {
			U.log("Created WJL connector. WJL Annotations are feasible.");
			Annotator.registerType(AnnotatorWjl.name, "AnnotatorWjl");
		}
		String errNumeric = AnnotatorNumeric.errInit();
		if (errNumeric == null) {
			U.log("Created Numeric connector. Numeric Annotations are feasible.");
			Annotator.registerType(AnnotatorNumeric.name, "AnnotatorNumeric");
		}
		
		// This would be the place to create additional ResourceConnectors if desired.
		/*
		if (errUmls == null) {
			ResourceConnectorUmls.assurePoolSize(1);
			ResourceConnectorNorm.assurePoolSize(1);
		}
		if (errMM == null) {
			ResourceConnectorNorm.assurePoolSize(1);
		}
		if (errcTakes == null) {
			ResourceConnectorcTakes.assurePoolSize(1);
		}
		if (errWjl == null) {
			ResourceConnectorWjl.assurePoolSize(1);
		}
		*/
		
		// Set up for number of required Highlighter colors
		AnnotationHighlight.setNumberOfBaseColors(Annotator.annotationTypes.size());
	}
	
	public static void close() {
		ResourceConnectorUmls.pool.closeAll();
		ResourceConnectorNorm.pool.closeAll();
		ResourceConnectorMetaMap.pool.closeAll();
		ResourceConnectorcTakes.pool.closeAll();
	}
	
	/** 
	 * Convenience methods to get/set ThesMap global properties
	 * These simply dispatch to the singleton instance of ThesProps that is created
	 * in main.
	 */
	public static String getProperty(String name) {
		return prop.getProperty(name);
	}
	
	public static String getProperty(String name, String defaultValue) {
		return prop.getProperty(name, defaultValue);
	}
	
	public void setProperty(String name, String value) {
		prop.setProperty(name, value);
	}
	
	public static Integer getInteger(String name) {
		return prop.getInteger(name);
	}
	
	public static Integer getInteger(String name, String defaultName) {
		return prop.getInteger(name,  defaultName);
	}
	
	public static Integer getInteger(String name, Integer defaultInt) {
		return prop.getInteger(name,  defaultInt);
	}
	

}
