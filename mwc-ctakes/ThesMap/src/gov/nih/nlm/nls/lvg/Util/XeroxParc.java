package gov.nih.nlm.nls.lvg.Util;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class represents an object of an Xerox Parc stochastic tagger.  The 
* format of Xerox Parc stocastic tagger is ['term', 'category'].
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
***************************************************************************/
public class XeroxParc
{
    //public constructor
    /**
    * Create an Xerox Parc stocastic tagger, using a string format
    */
    public XeroxParc(String tagger)
    {
        tagger_ = tagger;
        DecomposeTagger(tagger_);
    }
    // public methods
    /**
    * Check if the format is legal
    *
    * @return  true or false if the format of input is a legal or illegal format
    * of an Xerox Parc stocastic tagger.
    */
    public boolean IsLegal()
    {
        if((term_ == null) || (category_ == null))
        {
            return false;
        }
        return legal_;
    }
    /**
    * Get the term in the tagger
    *
    * @return  Term in the Xerox Parc stocastic tagger
    */
    public String GetTerm()
    {
        return term_;
    }
    /**
    * Get the category in a string format.
    *
    * @return  a string representation of the category in the tagger
    */
    public String GetCategory()
    {
        return category_;
    }
    /**
    * Get the category in a long integer format.
    *
    * @return  a long integer value of the category in the tagger
    */
    public long GetCategoryValue()
    {
        return categoryValue_;
    }
    /**
    * Test dirver for this class
    */
    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.out.println("** Usage: java XeroxParc <tag>");
        }
        else
        {
            String inTag = args[0];
            System.out.println("-- tagger: '" + inTag + "'");
            XeroxParc foo = new XeroxParc(inTag);
            System.out.println("-- term: [" + foo.GetTerm() + "]");
            System.out.println("-- category: [" + foo.GetCategory() + "]");
            System.out.println("-- categoryValue: [" + foo.GetCategoryValue() 
                + "]");
        }
    }
    //private methods
    private void DecomposeTagger(String tagger)
    {
        // check format
        legal_ = IsLegalXeroxParcFormat(tagger);
        if(legal_ == false)
        {
            return;
        }
        // get term & category
        String delim = "[\',]";
        StringTokenizer buf = new StringTokenizer(tagger, delim);
        term_ = null;
        category_ = null;
        // tokenize tagger using delimiter of "[\',]"
        while(buf.hasMoreTokens() == true)
        {
            String temp = buf.nextToken();
            if(temp != null) 
            {
                if(term_ == null)        // assign term
                {
                    term_ = temp;
                }
                else if((category_ == null) 
                && (Category.ToValue(temp) != 0))
                {
                    category_ = temp;
                    categoryValue_ = Category.ToValue(category_);
                    break;
                }
            }
        }
    }
    // check if the tagger starts with "[" and ends with "]"
    private boolean IsLegalXeroxParcFormat(String tagger)
    {
        boolean legalFormat = true;
        if((tagger.startsWith("[") == false)
        || (tagger.endsWith("]") == false))
        {
            return false;
        }
        return legalFormat;
    }
    // data member
    private String tagger_ = null;
    private String term_ = null;
    private String category_ = null;
    private long categoryValue_ = 0;
    private boolean legal_ = true;
}
