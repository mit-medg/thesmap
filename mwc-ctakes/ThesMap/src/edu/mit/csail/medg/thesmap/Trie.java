package edu.mit.csail.medg.thesmap;

/*
 * I am having trouble implementing an iterator over a Trie.  It needs to return a data structure that
 * contains the words down to the Trie level where I am, and the value. I make this a TrieEntry.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * A Trie is a hierarchic data structure that can hold information about arbitrary sequences
 * of elements. At each level, there may be additional levels down, and data about the current level.
 * This type of structure is often used to store items such as phrases consisting of multiple words,
 * where additional information is stored at the end of each phrase.
 * In this implementation, T is the type of the sequence of elements that index the Trie, and U is
 * the type of the information stored at the ends of valid sequences.
 * Unlike phrase dictionaries, lexical order is not maintained.
 * We may iterate over a Trie, but the Iterator is not re-entrant, so only one Iterator may operate
 * on a Trie at a time.
 * @author psz
 *
 */
public class Trie<T, U> implements Iterable<Trie<T, U>.TrieEntry>{
	
	private static final int hashMapInitialSize = 4;
	private static final float hashMapLoadFactor = 0.9f;
	
	HashMap<T, Trie<T, U>> down = null;
	U data = null;
	int maxDepth = 0;
	
	// To support iteration, we need some auxiliary structures
	boolean returnedValue = false;
	Set<T> iterKeys = null;
	
	public Trie() {
	}
	
	public int maxDepth() {
		return maxDepth;
	}
	
	public void add(T[] inputs, U value) {
		add(new TrieEntry(inputs, value));
	}
	
	public void add(ArrayList<T> inputs, U value) {
		add(new TrieEntry(inputs, value));
	}
	
	public void add(TrieEntry entry) {
		maxDepth = Math.max(maxDepth, entry.size());
		addLevel(0, entry);
	}
	
	private void addLevel(int level, TrieEntry entry) {
		if (level >= entry.size()) data = entry.value();
		else {
			Trie<T, U> next = null;
			if (down == null) {
				down = new HashMap<T, Trie<T, U>>(hashMapInitialSize, hashMapLoadFactor);
			} else {
				next = down.get(entry.get(level));
			}
			if (next == null) {
				next = new Trie<T, U>();
				down.put(entry.get(level), next);
			}
			next.addLevel(level + 1, entry);
		}
	}
	
	public Trie<T, U> get(ArrayList<T> path) {
		Trie<T, U> next = this;
		for (int level = 0; level < path.size(); level++) {
			if (next == null) return null;
			if (next.down == null) return null;
			next = next.down.get(path.get(level));
		}
		return next;
	}
	
	public Trie<T, U> get(T[] path) {
		Trie<T, U> next = this;
		for (int level = 0; level < path.length; level++) {
			if (next == null) return null;
			if (next.down == null) return null;
			next = next.down.get(path[level]);
		}
		return next;
	}
	
	public U getValue(ArrayList<T> path) {
		Trie<T, U> trie = get(path);
		return (trie == null) ? null : trie.data;
	}
	
	public U getValue(T[] path) {
		Trie<T, U> trie = get(path);
		return (trie == null) ? null : trie.data;
	}
	
	public void updateValue(ArrayList<T> path, U newValue) {
		Trie<T, U> trie = get(path);
		if (trie != null) trie.data = newValue;
		else add(path, newValue);
	}
	
	public void updateValue(T[] path, U newValue) {
		Trie<T, U> trie = get(path);
		if (trie != null) trie.data = newValue;
		else add(path, newValue);
	}

	/**
	 * This Iterator allows us to traverse a Trie and return 
	 * a TrieEntry that consists of T[] and U for each element in the Trie.
	 */
	public TrieIterator iterator() {
		return new TrieIterator(this);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("<");
		if (data != null) sb.append("(" + data.toString() + ")");
		if (down != null) {
			sb.append("[");
			for (T key: down.keySet()) {
				sb.append(" ");
				sb.append(key.toString());
				sb.append("=");
				sb.append(down.get(key).toString());
			}
			sb.append("]");
		}
		sb.append(">");
			
		return sb.toString();
	}
	
	public String toShow() {
		return toShow("");
	}
	
	public String toShow(String prefix) {
		StringBuilder sb = new StringBuilder(prefix);
		if (down != null) {
			String nextPrefix = "   " + prefix;
			for (T key: down.keySet()) {
				sb.append(key.toString());
				U val = down.get(key).data;
				if (data != null) sb.append(" = ").append(val.toString());
				sb.append("\n");
				sb.append(down.get(key).toShow(nextPrefix));
			}
		}
		return sb.toString();
	}
	
	/**
	 * The main() program is simply for debugging the code.
	 * @param args -- ignored
	 */
	public static void main(String[] args) {
		System.out.println("Starting Trie main.");
		final String[] e1 = {"Now",  "is", "the", "time"};
		final String[] e2 = {"Now",  "was", "the", "time"};
		final String[] e3 = {"for",  "all", "men"};
		Trie<String, Integer> foo = new Trie<String, Integer>();

		foo.add(e1, 44);
		foo.add(e2, 55);
		foo.add(e3, 66);
		System.out.println(foo);
		for (Trie<String, Integer>.TrieEntry e: foo) {
			System.out.println(e.toString());
		}
		
		ArrayList<String> test = new ArrayList<String>(4);
		test.add("Now"); test.add("was"); test.add("the"); test.add("time");
		System.out.println("Retrieved for " + test + "=>" + foo.getValue(test));
		String[] test2 = {"for", "all", "men"};
		System.out.println("Retrieved for " + test2 + "=>" + foo.getValue(test2));
		System.out.println("Ending Trie main.");
		Trie<String, Integer> x = foo.get(e2);
		System.out.println("Incrementing " + x.data);
		x.data++;
		for (Trie<String, Integer>.TrieEntry e: foo) {
			System.out.println(e.toString());
		}

	}
	
	/**
	 * A TrieEntry contains an Array of T, representing the elements of the Trie down to 
	 * this point, and a value U.
	 * @author psz
	 *
	 * @param <T>
	 * @param <U>
	 */
	public class TrieEntry {
		private ArrayList<T> keys;
		private U data;
		
		public TrieEntry(ArrayList<T> keys, U val) {
			this.keys = keys; //new ArrayList<T>(keys);
			data = val;
		}
		
		public TrieEntry(T[] keys, U val) {
			this.keys = new ArrayList<T>();
			for (int i = 0; i < keys.length; i++) {
				this.keys.add(keys[i]);
			}
			data = val;
		}
		
		public int size() {
			return keys.size();
		}
		
		public U value() {
			return data;
		}
		
		public T get(int index) {
			return keys.get(index);
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			String sep = "[\"";
			for (T item: keys) {
				sb.append(sep);
				sb.append(item.toString().replaceAll("\"", "\\\""));
				sep = "\", \"";
			}
			sb.append("\"] -> ");
			sb.append(data.toString());
			return sb.toString();
		}
	}

	/**
	 * This allows one to iterate over the contents of a Trie.
	 * In a recursive descent through the trie, any time we encounter
	 * a non-null data element, the path to that element and its value
	 * are returned as a TrieEntry.
	 * 
	 * The state of the recursive descent is kept in several variables:
	 * trieToPush is the next Trie to be investigated; it is virtually at the top of trieStack, but not yet.
	 * trieStack is the recursion stack of Tries being investigated.
	 * iterStack is the recursion stack of Trie key iterators.
	 * keys is the list of keys encountered so far in traversal.
	 * 
	 * This complexity comes from not having a yield operation in Java, so we need to explicitly
	 * keep track of the state of the recursion in an Iterator. The state of the search is intricate,
	 * and debugging was slow and tedious.
	 * 
	 * Unlike the first (buggy) implementation, here we pre-compute the next element, if any, and save it
	 * in nextResult, when hasNext() is called.  This makes the implementation of next() and hasNext()
	 * simple, delegating the work to search().
	 * 
	 * @author psz
	 *
	 */
	public class TrieIterator implements Iterator<Trie<T, U>.TrieEntry> {
		// trieStack holds the stack of Tries at different levels
		ArrayList<Trie<T, U>> trieStack = new ArrayList<Trie<T, U>>();

		// trieToPush is the next Trie to pursue; it it has data, it will be yielded first.
		Trie<T, U> trieToPush = null;
		
		// iterStack holds the stack of iterators over the different levels of the Trie.
		ArrayList<Iterator<T>> iterStack = new ArrayList<Iterator<T>>();
		
		// keys holds the list of keys that lead us to the current Trie; to return in a TrieEntry.
		// This will have one more element than trieStack or iterStack because we need to
		// collect this information one level before pushing the new Trie.
		ArrayList<T> keys = new ArrayList<T>();
		
		// holder for the next value to return, or null
		TrieEntry nextResult = null;
		
		TrieIterator(Trie<T, U> trie) {
			trieToPush = trie;
		}
		
		/**
		 * The Iterator hasNext() if at the current positon there is data to return
		 * or if there are additional iterations over any of its key sets
		 */
		@Override
		public boolean hasNext() {
//			System.out.println("hasNext() called.");
			nextResult = search();
//			System.out.println("hasNext() sees: " + nextResult);
			return nextResult != null;
		}
		
		@Override
		public TrieEntry next() {
			return nextResult;
		}

		/**
		 * search() performs a tree walk over the Trie.
		 * If the top of the stack has data and we have not yet returned it, do so.
		 * If there are subordinate tries, add them to the stack
		 */
		public TrieEntry search() {
			TrieEntry answer = null;
			topSearch:
				while (trieToPush != null || !trieStack.isEmpty()) {
					if (trieToPush != null) {
						// Pushing a new entry
//						System.out.println("Push " + trieToPush);
						trieStack.add(trieToPush);
						HashMap<T, Trie<T, U>> down = trieToPush.down;
						U val = trieToPush.data;
						Iterator<T> nextIterator = (down != null) ? down.keySet().iterator() : null;
						iterStack.add(nextIterator);
						// set up to push its first child next, if there is one
						if (nextIterator != null && nextIterator.hasNext()) {
							T nextKey = nextIterator.next();
							keys.add(nextKey);
							trieToPush = trieToPush.down.get(nextKey);
						} else { // otherwise, be sure we don't push this one again.
							trieToPush = null;
						}
						if (val != null) {
							answer = new TrieEntry(keys, val);
//							System.out.println("Yield: " + answer);
							break topSearch;
						}
						// else continue topSearch;
					} else if (!trieStack.isEmpty()) {			
						// See if the top iterator has any more elements.
						Trie<T, U> topTrie = trieStack.get(trieStack.size() - 1);
						Iterator<T> topIter = iterStack.get(iterStack.size() - 1);
						if (topIter == null || !topIter.hasNext()) {
							// If not, then we pop stacks and try again.
							trieStack.remove(trieStack.size() - 1);
							iterStack.remove(iterStack.size() - 1);
							// prevents underflow in keys at end; ugly!
							if (keys.size() > 0) keys.remove(keys.size() - 1);
							// continue topSearch;
						} else {
							// Move on to next element of topTrie
							T nextKey = topIter.next();
							keys.add(nextKey);
							trieToPush = topTrie.down.get(nextKey);
							// continue topSearch;
						}
					} else break topSearch;
				}
			return answer;
		}
		
		@Override
		public void remove() {
			// For now, don't allow removal of TrieIter iterations.
		}
		
		public void printState(String leader) {
			System.out.print(leader);
//			System.out.println("toPush: " + trieToPush + "\nkeys: " + keys + "\nstack: ");
//				for (Trie<T, U> t: trieStack) System.out.println("   " + t); 
			System.out.println("keys: " + keys);
		}
		
		/* Utilities to manipulate the state of TrieIter consistently.
		 * The operations involved in this are:
		 * 1. Push a new Trie onto the trieStack and set a flag so that it will be yielded. At the
		 * same time, we get an iterator over the keys of its down table (possibly sorted) and save 
		 * those on the iterStack.
		 * 2. Advance moves to the next Trie to yield.  This means one of the following:
		 *   a. The Trie at the top of trieStack is newly pushed, so if it has a data element, we know
		 *   that it is a valid entry and yield it, unsetting the flag so that it will not be yielded again.
		 *   b. The iterator on top of the iterStack has a next element.  In that case, we pop the top of
		 *   the trieStack and push the one corresponding to that next key, as extracted from the down table
		 *   of the (temporarily) top element of trieStack. We then return to Advance, which will yield 
		 *   that newly-pushed Trie.
		 *   c. There is no next element. Then, we Pop the tops of both the trieStack and iterStack and
		 *   Advance again, at one level higher in the Trie.
		 *   d. If the stacks are empty, then we are completely done.  In that case, hasNext() should have
		 *   been false, so next() should not have been called; this is an internal error. 
		 * 3. Pop deletes the tops of both stacks and returns false if there is nothing left.
		 */
		
	}

}
