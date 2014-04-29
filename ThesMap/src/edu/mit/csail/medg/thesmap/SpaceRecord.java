package edu.mit.csail.medg.thesmap;


/**
 * A SpaceRecord is a round-robin store for keeping track of the start
 * of words, sequences of which are to be taken as phrases. We make it 
 * long enough that we can go back to previous phrases that can overlap
 * the most recently stored one by at least one word.
 * @author psz
 *
 */
public class SpaceRecord {
	protected int[] wordStarts;
	int l;
	protected int pointer;

	public SpaceRecord(int nWords) {
		l = 2 * nWords;
		wordStarts = new int[l];
		pointer = 0;
		wordStarts[pointer] = 0;
		for (int j = 1; j < l; j++) {
			wordStarts[j] = -1;	// non-existent positions are -1
		}
	}

	public void add(int wordStartPosn) {
		pointer = (pointer + 1) % l;
		wordStarts[pointer] = wordStartPosn;
	}

	/*
	 * Gives the beginning of the n'th word before here, n counting up from 1
	 * Must check for -1 value, which means there was no word n before here.
	 */
	public int getPrevStart(int n) {
		int i = ((pointer - n) + l) % l;
		return wordStarts[i];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("<");
		String sep = "";
		for (int i = 0; i < l; i++) {
			sb.append(sep);
			sb.append(i);
			sb.append(": ");
			sb.append(wordStarts[i]);
			sep = ", ";
		}
		sb.append(";");
		sb.append(pointer);
		sb.append(">");
		return sb.toString();
	}

	public String toShow() {
		StringBuilder sb = new StringBuilder("<");
		String sep = "";
		for (int i = 0; i < l; i++) {
			sb.append(sep);
			sb.append(getPrevStart(i));
			sep = ", ";
		}
		sb.append(">");
		return sb.toString();
	}
}