/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.SwingWorker;


/**
 * @author psz
 *
 */
public abstract class Annotator extends SwingWorker<Void, String> {
	
	/*
	 * We index Annotation types by mapping the type to an integer and the integer to a type name,
	 * and the name to a Class.
	 * This index is independent of the color index that gets associated in UmlsWindow with each type.
	 * That is given by getPanelIndex of the annotator name in UmlsWindow.MethodChooser.
	 */
	public static HashMap<String, Integer> annotationTypes = new HashMap<String, Integer>();
	public static ArrayList<String> annotationIndex = new ArrayList<String>();
	public static HashMap<String, String> annotationClasses = new HashMap<String, String>();
	
	public UmlsWindow myWindow = null;
	
	public String name = null;
	
	public Annotator(String name, UmlsWindow w) {
		this.myWindow = w;
		this.name = name;
	}
	
	public void done() {
		if (myWindow != null) myWindow.annotatorDone(name);
	}
	
	/**
	 * We maintain a mapping from Interpretation.type's to successive integers, to support
	 * BitSet operations.
	 */
	
	static void registerType(String type, String className) {
		Integer existing = annotationTypes.get(type);
		U.log("Registering Annotation " + type + " with class " + className);
		if (existing == null) {
			int index = annotationTypes.size();
			annotationTypes.put(type, index);
			annotationIndex.add(type);
			annotationClasses.put(type, className);
			U.log(" ... assigned index " + index);
		}
		else U.log(" ... already at index " + existing);
	}
	
	public BitSet getBitSet() {
		BitSet bs = new BitSet();
		bs.set(annotationTypes.get(this.name));
		return bs;
	}
	
	public static BitSet getBitSet(String name) {
		BitSet bs = new BitSet();
//		U.log("Annotator.getBitSet(\""+name+"\")");
//		Annotator.checkStatic("Checking in Annotator.getBitSet " + name);
		
		HashMap<String, Integer> foo = annotationTypes;
		Integer i = foo.get(name);
		if (i != null) bs.set(i);
		else U.pe("Bug!!!");
		return bs;
	}
	
	public static ArrayList<String> getNames(BitSet bits) {
		ArrayList<String> ans = new ArrayList<String>();
		int i = -1;
		while ((i = bits.nextSetBit(i + 1)) >= 0) {
			ans.add(getName(i));
		}
		return (ans.size() > 0) ? ans : null;
	}
	
	public String getName() {
		return name;
	}
	
	public static String getName(int index) {
		return annotationIndex.get(index);
	}
	
	public static Integer getIndex(String type) {
//		U.log("getIndex of \"" + type + "\"");
//		showStatic();
		Integer ans = annotationTypes.get(type);
		return ans;
	}
	
	/**
	 * We define a generic method that permits invocation of a static
	 * method on an Annotator subclass that is identified by the name 
	 * of the Annotator.
	 * @param annotatorName The name property of the Annotator
	 * @param methodName The name of a static method
	 * @param args Variable argument list of args to pass
	 * @return The Object returned by the method.
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeAnnotatorStatic(String annotatorName, String methodName, Object...args) {
		String className = annotationClasses.get(annotatorName);
		Class<Annotator> ann = null;
		Object ans = null;
		String pkg = Annotator.class.getPackage().getName();
		try {
			ann = (Class<Annotator>)Class.forName(pkg + "." + className);
		} catch (ClassNotFoundException e1) {}
		if (ann != null) {
			Method method = null;
			@SuppressWarnings("rawtypes")
			Class[] argTypes = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
			}
			try {
				method = ann.getMethod(methodName, argTypes);
			} 
			catch (SecurityException e) {}
			catch (NoSuchMethodException e) {}
			if (method != null) {
				try {
					ans = (Object)method.invoke(null, args);
				}
				catch (IllegalArgumentException e) {}
				catch (IllegalAccessException e) {}
				catch (InvocationTargetException e) {}
				return ans;
			}
		}
		U.pe("Unable to invoke static" + methodName + " on Annotator" + annotatorName);
		return null;
	}	
	
	/**
	 * We define a generic method that permits invocation of a 
	 * method on an Annotator subclass that is identified by the name 
	 * of the Annotator.
	 * @param annotatorName The name property of the Annotator
	 * @param methodName The name of a static method of no arguments
	 * @return The Object returned by the method.
	 */
	@SuppressWarnings("unchecked")
	public Object invokeAnnotator(String annotatorName, String methodName, Object...args) {
		String className = annotationClasses.get(annotatorName);
		Class<Annotator> ann = null;
		Object ans = null;
		String pkg = Annotator.class.getPackage().getName();
		try {
			ann = (Class<Annotator>)Class.forName(pkg + "." + className);
		} catch (ClassNotFoundException e1) {}
		if (ann != null) {
			Method method = null;
			@SuppressWarnings("rawtypes")
			Class[] argTypes = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
			}
			try {
				method = ann.getMethod(methodName, argTypes);
			} 
			catch (SecurityException e) {}
			catch (NoSuchMethodException e) {}
			if (method != null) {
				try {
					ans = (Object)method.invoke(this, (Object[])null);
				}
				catch (IllegalArgumentException e) {}
				catch (IllegalAccessException e) {}
				catch (InvocationTargetException e) {}
				return ans;
			}
		}
		U.pe("Unable to invoke " + methodName + " on Annotator" + annotatorName);
		return null;
	}

	
	/**
	 * Dispatches to create an Annotator of the type specified by the argument String. 
	 * @param annotatorName The name associated with this Annotator type.
	 * @return
	 */
	public static String errInit(String annotatorName) {
		return (String)invokeAnnotatorStatic(annotatorName, "errInit");
	}
	
	public static Annotator makeAnnotator(String annotatorName, UmlsWindow w) {
		Annotator ann = (Annotator)invokeAnnotatorStatic(annotatorName, "makeAnnotatorInstance", annotatorName, w);
		return ann;
	}
	
	public static void showStatic() {
		U.log(annotationTypes.size() + "," + annotationIndex.size() + " Annotator types:");
		for (Entry<String, Integer> e: annotationTypes.entrySet()) {
			U.log(e.getKey() + " = " + e.getValue());
		}
		for (int i = 0; i < annotationIndex.size(); i++) {
			U.log(i + " => " + annotationIndex.get(i));
		}
	}
	
	public static boolean checkStatic(String msg) {
		int ti = annotationTypes.size();
		int ii = annotationIndex.size();
		int ci = annotationClasses.size();
		boolean wrong = (ti != ii || ii != ci);
		if (wrong)
			System.err.println("Annotator caches wrong: " + ti + ", " + ii + ", " + ci + "\n  " + msg);
		return wrong;
	}
}
