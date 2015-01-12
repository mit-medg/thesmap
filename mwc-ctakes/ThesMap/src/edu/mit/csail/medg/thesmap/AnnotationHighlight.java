package edu.mit.csail.medg.thesmap;

import java.awt.Color;
import java.util.BitSet;

public class AnnotationHighlight {
	/*
	 * We associate a relatively light color with each type of annotation,
	 * and highlight corresponding text with that color.  When multiple
	 * annotations apply to a piece of text, we "add" the colors of their 
	 * component annotations, using an inverted RGB scale so that multiple
	 * annotations make the highlight darker.
	 * 
	 * In an RGB color space, the obvious base colors are light red, green and blue.
	 * But what should we do when we have more than three annotation types?
	 * (This is so far hypothetical, because we don't yet!)
	 * We use colors that are darker but pure for the additional types. This
	 * continues to give distinct colors for all combinations.
	 * 
	 * To moke colors as distinct as possible, we allocate only enough to cover the number
	 * of needed annotation types.  Because base colors can be represented as
	 * (1 0 0), (0 1 0), (0 0 1), (2 0 0), (0 2 0), (0 0 2), (3 0 0), ...
	 * we compute the scale factors to give as many distinct colors as needed,
	 * though always a multiple of 3. 
	 * 
	 * The index of these colors is the panel index in UmlsWindow.MethodChooser.
	 */
	
	AnnotationHighlight() {
	}
	
	static final AnnotationHighlight dummyAnnotationHighlight = new AnnotationHighlight();

	/**
	 * Implements the mapping from a rather abstract color space to actual Colors.
	 * r=Red, g=Green, b=Blue are on a darkness scale ranging from 0 to nLevels,
	 * and higher numbers lead to darker colors.
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	static Color getColor(int r, int g, int b) {
		int rs = scale(r);
		int gs = scale(g);
		int bs = scale(b);
		/* Because these are used as subtractive colors, however, they 
		 * produce Cyan, Magenta and Yellow as the primary colors.  
		 * We perform a very inaccurate cmy->rgb conversion, which is 
		 * suitable to this simple task.		 */
//		System.out.println(r+"->"+rs+","+g+"->"+gs+","+b+"->"+bs);
		return new Color((gs+bs)/2, (rs+bs)/2, (rs+gs)/2);
	}
	
	static Color getColor(Integer[] s) {
		return getColor(s[0], s[1], s[2]);
	}
	
	public static int nLevels = 3;
	public static final int minCol = 96;
	public static final int maxCol = 256;	// 1-offset; adjust to 0-offset later
	public static final int darken = 32;
	public static int incCol = (maxCol-minCol)/(nLevels-1);

	static int scale(int orig) {
		return (int)Math.min(255, maxCol - darken - orig * incCol);
	}
	
	static void setNumberOfBaseColors(int n) {
		nLevels = (int)Math.ceil(n/3.0);
		incCol = (256-minCol-32)/(int)Math.max(1, nLevels-1);
	}
	
	static int getNumberOfBaseColors() {
		return 3 * nLevels;
	}
	
	static int getNumberOfColors() {
		return (int)Math.pow(nLevels, 3);
	}
	
	/**
	 * Maps a color index to a specific color.
	 * The first three are at level 1, the next three at 2, etc.
	 * In each tier, the level is set for r, g or b.
	 * 
	 * @param index 0-based index
	 * @return the Color
	 */
	static Color getColor(int index) {
		return getColor(getColorSpec(index));
	}
	
	static Integer[] getColorSpec(int index) {
		int colorLevel = (int)Math.floor(index/3.0) + 1;
		int colorPosition = index % 3;
		Integer[] ans = {0, 0, 0};
		ans[colorPosition] = colorLevel;
		return ans;
	}
	
	static Color getColor(String type) {
		return getColor(getColorSpec(type));
	}
	
	static Integer[] getColorSpec(String type) {
		return getColorSpec(Annotator.getIndex(type));
	}
	
	static Color getColor(BitSet typeBits) {
		Integer[] c = null;
		int i = -1;
		while ((i = typeBits.nextSetBit(i + 1)) >= 0) {
			c = add(c, getColorSpec(i));
		}
		return getColor(c);
	}
	
	/*
	 * A ColorSpec is a triple of the r, g and b specifications
	 */
	
	static Integer[] add(Integer[] x, Integer[] y) {
		Integer[] ans;
		if (x == null) ans = y;
		else {
			ans = new Integer[3];
			for (int i = 0; i < 3; i++) ans[i] = x[i] + y[i];
		}
		return ans;
	}
	
//	class ColorSpec implements Comparable<ColorSpec>{
//		int r, g, b;
//		
//		ColorSpec(int r, int g, int b) {
//			this.r = r;
//			this.g = g;
//			this.b = b;
//		}
//		
//		ColorSpec add(ColorSpec other) {
//			return new ColorSpec(r + other.r, g + other.g, b + other.b);
//		}
//
//		@Override
//		public int compareTo(ColorSpec o) {
//			if (r < o.r) return -1;
//			else if (r == o.r) {
//				if (g < o.g) return -1;
//				else if (g == o.g) {
//					if (b < o.b) return -1;
//					else if (b == o.b) return 0;
//				}
//			}
//			return 1;
//		}
//		
//		public boolean equals(ColorSpec o) {
//			return r == o.r && g == o.g && b == o.b;
//		}
//	}

}
