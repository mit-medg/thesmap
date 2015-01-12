package edu.mit.csail.medg.thesmap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * An AnnotationSet holds all the annotations on a document, organized by the starting and
 * ending offsets of the phrases that are annotated.
 * 
 * The most important logic is in the procedure integrate, which incorporates a new Annotation
 * into the AnnotationSet, but under the constraints that
 * If the phrase of the new Annotation overlaps any of the existing ones in the set
 * 		and if these are equal Annotations, then either the old or new one is omitted;
 * 		the one we retain is the one that spans the smallest phrase, on the assumption
 * 		that the longer one simply contains additional "noise" words.
 * The AnnotationSet is stored as a TreeMap indexed by starting position, where the content
 * is another TreeMap of Annotations indexed by ending position.  Note that each Annotation also
 * contains its start and end positions, as well as an InterpretationSet.
 * 
 * An AnnotationSet will generally contain annotations of multiple types. The ones it contains
 * are represented by a BitSet whose bits are defined in Annotator.
 * @author psz
 *
 */
public class AnnotationSet implements Iterable<Annotation> {
	
	protected TreeMap<Integer, AS1> treeByStart = new TreeMap<Integer, AS1>();
	int phraseLength = 6;	// Defaults to 6.
	BitSet containsAnnotationTypes = new BitSet(Annotator.annotationTypes.size());

	public AnnotationSet() {
		phraseLength = ThesMap.getInteger("phraseLength");
	}
	
//	public AnnotationSet(int phraseLength) {
//		this.phraseLength = phraseLength;  
//	}
	
	public int size() {
		int sum = 0;
		for (AS1 at: treeByStart.values()) sum += at.size();
		return sum;
	}
	
	public synchronized void add(Annotation ann) {
		AS1 existing = treeByStart.get(ann.begin);
		if (existing == null) {
			existing = new AS1();
			treeByStart.put(ann.begin, existing);
		}
		existing.add(ann);
	}
	
	public synchronized void add(List<Annotation> annotations) {
		for (Annotation a: annotations) add(a);
	}
	
	public AnnotationIterator iterator() {
		return new AnnotationIterator(this);
	}
	
	/**
	 * Integrate adds Interpretations to the AnnotationSet unless they are redundant.
	 * An Interpretation is redundant if it is the same an another that spans a shorter
	 * phrase, assuming that the rest of the longer phrase is inessential.
	 * Thus, we look for phrases that overlap with the proposed Annotation and
	 * find Interpretations that are equal.  If so, we eliminate either the new
	 * Interpretations (part of the Annotation argument) or existing ones, whichever
	 * is longer.
	 * 
	 * A free parameter of this process is the maximum number of words (tokens) in a phrase,
	 * which we keep as phraseLength.
	 * 
	 * We find potentially overlapping Annotations by indexing backward from the start of the
	 * proposed Annotation to phraseLength-1 words, and then considering any phrases that 
	 * begin there or up to one before the end of the proposed phrase. Note that this
	 * over-generates candidates because not all word boundaries will exist in the AnnotationSet.
	 * 
	 * If an Interpretation in the proposed Annotation is redundant, we simply ignore it.
	 * If an Interpretation is an already-present Annotation is redundant, we have to remove it,
	 * which requires playing with Iterators to avoid Java's unfortunate general inability to
	 * modify data structures over with it is iterating. Note that we need to iterate over
	 * all pairs of Interpretations.
	 * @param ann
	 */
	public synchronized void integrate(Annotation ann) {
		//TheMap.log("Integrate: " + ann.toShow());
		// Walk startKey back to earliest possible overlap
		Integer startKey = ann.begin; 
		for (int i = 1; i < phraseLength; i++) {
			Integer earlyKey = treeByStart.lowerKey(startKey);
			if (earlyKey == null) break;
			else startKey = earlyKey;
		}
		// Overlaps can occur until we advance to old annotations that are beyond the
		// end of the proposed one.
		while (startKey != null && startKey < ann.end) {
			AS1 thisAS1 = treeByStart.get(startKey);
			if (thisAS1 != null) {
				Collection<Annotation> oldAnns = thisAS1.treeByEnd.values();
				for (Annotation old: oldAnns) {
					if (old.begin >= ann.end) break;	// no overlap because old start later
					if (old.end > ann.begin) {	// overlap if old ends after ann starts
						// They overlap; check all pairs of Interpretations for redundancy.
						proposition:
							for (Iterator<Interpretation> iti = ann.interpSet.getInterpretations().iterator();
									iti.hasNext(); ) {
								Interpretation proposed = iti.next();
								for (Iterator<Interpretation> ito = old.interpSet.getInterpretations().iterator(); 
										ito.hasNext(); ) {
									Interpretation prev = ito.next();
									if (proposed.equals(prev)) {
										if (old.length() <= ann.length()) {
											// keep the old one, delete proposed
											iti.remove();
											continue proposition;
										} else {
											// keep the new (shorter) one, delete old
											ito.remove();
										}
									}
								}
							}
					}
				}
			}
			startKey = treeByStart.higherKey(startKey);
		}
		if (ann.interpSet.size() > 0) add(ann);
		//TheMap.log(toShow());
	}
	
	/**
	 * Find the hitting set of a specific position in the text that has been 
	 * annotated.  This is all those annotations whose span includes the given position.
	 * @return ArrayList<Annotation> the set of spanning Annotations
	 */
	
	public synchronized ArrayList<Annotation> hittingSet(int position) {
		ArrayList<Annotation> ans = new ArrayList<Annotation>();
//		System.out.println("Look for hitting set for position "+ position);
		Integer startKey = position;
		// Find earliest possible overlapping Annotations.
		// We may need to walk back one more than in integrate, because
		// position is not necessarily at the start of a token.
		for (int i = 0; i < phraseLength; i++) {
			Integer earlyKey = treeByStart.lowerKey(startKey);
//			System.out.println("startKey="+startKey+", earlyKey="+earlyKey);
			if (earlyKey == null) break;
			else startKey = earlyKey;
		}
//		System.out.println("Found startKey to be "+ startKey);
		// Now advance through annotations until their start is beyond position.
		while (startKey != null && startKey <= position) {
			AS1 thisAS1 = treeByStart.get(startKey);
			if (thisAS1 != null) {
//				System.out.println("Found AS1 at "+startKey);
				for (Integer endKey = thisAS1.treeByEnd.higherKey(startKey);
						endKey != null; endKey = thisAS1.treeByEnd.higherKey(endKey)) {
					Annotation ann = thisAS1.getByEnd(endKey);
//					System.out.println("Considering ann= " + ann);
					if (ann.begin > position) break;
					if (ann.end > position) {
						ans.add(ann);
					}
				}
			}
			startKey = treeByStart.higherKey(startKey);
		}
//		System.out.println("Found "+ans.size()+" relevant Annotations:");
//		for (Annotation a: ans) {
//			System.out.println(a.toShow(5));
//		}
		return ans;
	}
	
	public synchronized ArrayList<Annotation> hittingSet(int position, ArrayList<String> tuis) {
		ArrayList<Annotation> allHits = hittingSet(position);
		if (tuis == null) return allHits;
		Iterator<Annotation> it = allHits.iterator();
		while (it.hasNext()) {
			if (!it.next().matchesTui(tuis)) it.remove();
		}
		// This leaves only the Annotations in which some Interpretation matches.
		// Now compute only the subset of Interpretations that all match.
		ArrayList<Annotation> ans = new ArrayList<Annotation>();
		for (Annotation a: allHits) {
			ans.add(a.matchingTui(tuis));
		}
		return ans;
	}
	
	public synchronized ArrayList<Annotation> hittingSet(int position, ArrayList<String> tuis, ArrayList<String> types) {
		ArrayList<Annotation> ans = hittingSet(position);
		if (tuis == null && types == null) return ans;
		Iterator<Annotation> it = ans.iterator();
		while (it.hasNext()) {
			Annotation interesting = it.next().matchingTuiAndType(tuis, types);
			if (interesting.isNull()) it.remove();
		}
		return ans;
	}
	
	public synchronized TreeSet<Integer> getAnnotationBreaks() {
		TreeSet<Integer> breaks = new TreeSet<Integer>();
		for (Entry<Integer, AS1> e: treeByStart.entrySet()) {
			breaks.add(e.getKey());
			for (Integer i: e.getValue().treeByEnd.keySet()) {
				breaks.add(i);
			}
		}
		return breaks;
	}
	
	/**
	 * The rules for integrating InterpretationNumeric are different, because we don't insist that
	 * the proposed Interpretation be equal in all aspects to an old one in order to discard it.
	 * We do, however, want to assure that in interpreting something like "2014-02-23", we don't also
	 * interpret "2014-02" or "-02-23" as ranges, or "-23" as a number.  This is easy by just assuring 
	 * that new numeric interpretations that fall completely within an existing one are discarded.
	 * The AnnotationSet here contains only Numeric Interpretations.
	 * @param ann A new proposed Annotation whose Interpretations are all InterpretationNumeric.
	 */
	public void integrateNumeric(Annotation ann) {
		ArrayList<Annotation> hs = hittingSet(ann.begin);
		for (Annotation a: hs) {
			if (ann.end <= a.end) return;	// It's within old one, skip it 
		}
		integrate(ann);
	}

	
	public BitSet typeBits() {
		BitSet ans = new BitSet();
		for (AS1 e: treeByStart.values()) {
			for (Annotation a: e.treeByEnd.values()) {
				ans.or(a.typeBits());
			}
		}
		return ans;
	}
	
	/**
	 * This provides a relatively expensive way to cache all the
	 * Annotation types that have been recorded in this AnnotationSet.
	 */
	public void cacheContainsAnnotationTypes() {
		containsAnnotationTypes = typeBits();
	}
	
	public void recordAnnotationType(String type) {
		Integer i = Annotator.annotationTypes.get(type);
		if (i != null) containsAnnotationTypes.set(i);
	}
	
	public int getPhraseLength() {
		return phraseLength;
	}

	public void setPhraseLength(int phraseLength) {
		this.phraseLength = phraseLength;
	}
	
	public String toShow(int indent) {
		StringBuilder sbs = new StringBuilder();
		for (int i = 0; i < indent; i++) sbs.append(" ");
		String leadingSpaces = sbs.toString();
		StringBuilder sb = new StringBuilder(leadingSpaces + "AnnotationSet [");
		sb.append(size()); sb.append("]");
		for (Entry<Integer, AS1> e: treeByStart.entrySet()) {
			sb.append("\n");
			sb.append(leadingSpaces);
			sb.append(e.getKey());
			sb.append(":\n");
			sb.append(e.getValue().toShow(indent + 5));
		}
		return sb.toString();
	}
	
	public String toShow() {
		return toShow(0);
	}
	
	protected class AS1 {
		
		protected TreeMap<Integer, Annotation> treeByEnd = 
				new TreeMap<Integer, Annotation>();
		
		protected AS1() {
			
		}
		
		protected Annotation getByEnd(int end) {
			return treeByEnd.get(end);
		}
		
		protected void add(Annotation ann) {
			Annotation existing = treeByEnd.get(ann.end);
			if (existing == null) {
				existing = ann;
				treeByEnd.put(ann.end, existing);
			}
			existing.add(ann);
		}
		
		protected Annotation get(int end) {
			return treeByEnd.get(end);
		}
		
		protected Collection<Annotation> getAll() {
			return treeByEnd.values();
		}
		
		protected int size() {
			return treeByEnd.size();
		}
		
		protected String toShow(int indent) {
			StringBuilder sbs = new StringBuilder();
			for (int i = 0; i < indent; i++) sbs.append(" ");
			String leadingSpaces = sbs.toString();
			StringBuilder sb = new StringBuilder(leadingSpaces + "[");
			sb.append(size()); sb.append("]");
			for (Entry<Integer, Annotation> e: treeByEnd.entrySet()) {
				sb.append("\n");
				sb.append(leadingSpaces);
				sb.append(e.getKey());
				sb.append(":\n");
				sb.append(e.getValue().toShow(indent+3));
			}
			return sb.toString();
		}
	}
	
	public class AnnotationIterator implements Iterator<Annotation> {
		
		Iterator<AS1> nextStart;
		AS1 currStart;
		Iterator<Annotation> nextEnd;
		Annotation currEnd;
		
		
		public AnnotationIterator(AnnotationSet as) {
			// Find the next (first) annotation in currEnd
			nextStart = treeByStart.values().iterator();
			if (nextStart.hasNext()) {
				currStart = nextStart.next();
				nextEnd = currStart.treeByEnd.values().iterator();
				if (nextEnd.hasNext()) {
					currEnd = nextEnd.next();
				} else currEnd = null;
			} else currStart = null;
		}

		@Override
		public boolean hasNext() {
			return currEnd != null;
		}

		@Override
		public Annotation next() {
			Annotation ans = currEnd;
			if (nextEnd.hasNext()) {
				currEnd = nextEnd.next();
			} else if (nextStart.hasNext()) {
				currStart = nextStart.next();
				nextEnd = currStart.treeByEnd.values().iterator();
				if (nextEnd.hasNext()) {
					currEnd = nextEnd.next();
				} else currEnd = null;
			} else currEnd = null;
			return ans;
		}

		@Override
		public void remove() {
			// We do not support removing elements from the Iterator
			throw new UnsupportedOperationException("AnnotationIterator does not support remove.");
		}
		
	}

}
