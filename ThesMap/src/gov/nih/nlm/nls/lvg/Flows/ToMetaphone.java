package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class transforms the input to a metaphone code. 
* The maximum lenght for truncate the metaphone code can be specified in the
* configuration file.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/metaPhone.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/metaphone/index.html"> 
* Metaphone Algorithm</a>
*
* @version    V-2013
****************************************************************************/
public class ToMetaphone extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   maxCodeLength   maxinum code length for metaphone code
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - the results from this flow component
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, int maxCodeLength, 
        boolean detailsFlag, boolean mutateFlag)
    {
        // mutate the term: strip diacritics
        String term = GetMetaphone(in.GetSourceTerm(), maxCodeLength);
        // details & mutate
        String details = null;
        String mutate = null;
        if(detailsFlag == true)
        {
            details = INFO;
        }
        if(mutateFlag == true)
        {
            mutate = Transformation.NO_MUTATE_INFO;
        }
        // updatea target 
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.METAPHONE, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "neurologist");
        int maxLength = Integer.parseInt( 
            conf.GetConfiguration(Configuration.MAX_METAPHONE));
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToMetaphone.Mutate(in, maxLength, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private methods
    private static String GetMetaphone(String inStr, int maxCodeLength)
    {
        String outStr = new String();
        String tempStr = new String();
        // check the illegal input
        if((inStr == null) || (inStr.length() == 0))
        {
            return tempStr;
        }
        // drop non-alphabetic character
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < inStr.length(); i++)
        {
            if(Character.isLetter(inStr.charAt(i)) == true)
            {
                buffer.append(inStr.charAt(i));
            }
        }
        tempStr = buffer.toString();
        // return the input if it's length is 1
        if(tempStr.length() == 0)
        {
            return "";
        }
        else if(tempStr.length() == 1)
        {
            return tempStr;
        }
        // convert to uppercase
        tempStr = tempStr.toUpperCase();
        // check the initial letter exception
        // if starts with GN, KN, PN, AE, WR, drop the first chararcter
        if((tempStr.startsWith("GN"))
        || (tempStr.startsWith("KN"))
        || (tempStr.startsWith("PN"))
        || (tempStr.startsWith("AE"))
        || (tempStr.startsWith("WR")))
        {
            tempStr = tempStr.substring(1);
        }
        else if(tempStr.startsWith("X"))    // if X, map to S
        {
            tempStr = "S" + tempStr.substring(1);
        }
        else if(tempStr.startsWith("WH"))    // if WH, map to W
        {
            tempStr = "W" + tempStr.substring(2);
        }
        buffer = new StringBuffer();
        // loop through and convert to metaphone
        int size = tempStr.length(); 
        for(int i = 0; i < size; i++)
        {
            char curChar = tempStr.charAt(i);
            // Drop duplicates except for CC
            if((GetCharAt(tempStr, i-1) == curChar)
            && (curChar != 'C'))
            {
                continue;
            }
            // drop vowel character if not the first letter 
            else if((IsVowel(curChar) == true)
            && (i != 0))
            {
                continue;
            }
            else
            {
                switch(curChar)
                {
                    case 'B':    // drop if mb at the end of the string
                        if((i != (size-1))
                        || (GetCharAt(tempStr, i-1) != 'M'))
                        {
                            buffer.append(curChar);
                        }
                        break;
                    case 'C':
                        // map to X if CIA, or CH (not SCH)
                        if(((GetCharAt(tempStr, i+1) == 'I')
                         && (GetCharAt(tempStr, i+2) == 'A'))
                        || ((GetCharAt(tempStr, i+1) == 'H')
                         && (GetCharAt(tempStr, i-1) != 'S')))
                        {
                            buffer.append('X');
                        }
                        // map to S if CE, CI, CY
                        else if(IsFrontVowel(GetCharAt(tempStr, i+1)) == true)
                        {
                            // drop if SCE, SCI, SCY
                            if(GetCharAt(tempStr, i-1) != 'S')
                            {
                                buffer.append('S');
                            }
                        }
                        else // map to K otherwise
                        {
                            buffer.append('K');
                        }
                        break;
                    case 'D':
                        // map to J if DGE, DGI, DGY
                        if((GetCharAt(tempStr, i+1) == 'G')
                        && (IsFrontVowel(GetCharAt(tempStr, i+2)) == true))
                        {
                            buffer.append('J');
                        }
                        else    // map to T otherwise
                        {
                            buffer.append('T');
                        }
                        break;
                    case 'G':
                        // drop if GN or GNED
                        if((GetCharAt(tempStr, i+1) == 'N')
                        // drop if in GH && at the end or before a consonant
                        || ((GetCharAt(tempStr, i+1) == 'H')
                         && ((i == size-1) 
                          || (IsVowel(GetCharAt(tempStr, i+2)) == false)))
                        // drop if DGE, DGI, DGY
                        || ((GetCharAt(tempStr, i-1) == 'D')
                         && (IsFrontVowel(GetCharAt(tempStr, i+1)) == true)))
                        {
                            continue; // do nothing
                        }
                        else if(IsFrontVowel(GetCharAt(tempStr, i+1)) == true)
                        {
                            buffer.append('J');
                        }
                        else    // map to K otherwise
                        {
                            buffer.append('K');
                        }
                        break;
                    case 'H':
                        // map to H if not after vowel, varson, 
                        // and before a constant 
                        if((IsVowel(GetCharAt(tempStr, i-1)) == false)
                        && (IsVarson(GetCharAt(tempStr, i-1)) == false)
                        && (IsVowel(GetCharAt(tempStr, i+1)) == true))
                        {
                            buffer.append('H');
                        }
                        break;
                    case 'K':
                        // drop after C, map to K otherwise
                        if(GetCharAt(tempStr, i-1) != 'C')
                        {
                            buffer.append('K');
                        }
                        break;
                    case 'P':
                        // map to F if before H
                        if(GetCharAt(tempStr, i+1) == 'H')
                        {
                            buffer.append('F');
                        }
                        else
                        {
                            buffer.append('P');
                        }
                        break;
                    case 'Q':        // map to K
                        buffer.append('K');
                        break;
                    case 'S':
                        // map to X if SH, SIA, SIO
                        if((GetCharAt(tempStr, i+1) == 'H')
                        || ((GetCharAt(tempStr, i+1) == 'I')
                         && ((GetCharAt(tempStr, i+2) == 'A')
                          || (GetCharAt(tempStr, i+2) == 'O'))))
                        {
                            buffer.append('X');
                        }
                        else // otherwise, map to S
                        {
                            buffer.append('S');
                        }
                        break;
                    case 'T':
                        // map to X if TIA, TIO
                        if((GetCharAt(tempStr, i+1) == 'I')
                        && ((GetCharAt(tempStr, i+2) == 'A')
                         || (GetCharAt(tempStr, i+2) == 'O')))
                        {
                            buffer.append('X');
                        }
                        // map to 0 (zero) if TH
                        else if(GetCharAt(tempStr, i+1) == 'H')
                        {
                            buffer.append('0');
                        }
                        // drop if in TCH
                        else if((GetCharAt(tempStr, i+1) == 'C')
                        && (GetCharAt(tempStr, i+2) == 'H'))
                        {
                            continue;  // do nothing
                        }
                        else    // otherwise, map to T
                        {
                            buffer.append('T');
                        }
                        break;
                    case 'V':        // map to F
                        buffer.append('F');
                        break;
                    case 'W':    // drop if before a vowel, otherwise map to W
                        if(IsVowel(GetCharAt(tempStr, i+1)) == true)
                        {
                            buffer.append('W');
                        }
                        break;
                    case 'X':        // map to KS
                        buffer.append("KS");
                        break;
                    case 'Y':    // drop if before a vowel, otherwise map to Y
                        if(IsVowel(GetCharAt(tempStr, i+1)) == true)
                        {
                            buffer.append('Y');
                        }
                        break;
                    case 'Z':        // map to S
                        buffer.append('S');
                        break;
                    default:
                        buffer.append(curChar);
                        break;
                }
            }
            outStr = buffer.toString();
            if(outStr.length() >= maxCodeLength)
            {
                break;
            }
        }
        return outStr;
    }
    private static char GetCharAt(String str, int index)
    {
        try
        {
            return str.charAt(index);
        }
        catch (Exception e)
        {
            return '?';
        }
    }
    private static boolean IsVarson(char in)
    {
        if((in == 'C')
        || (in == 'G')
        || (in == 'P')
        || (in == 'S')
        || (in == 'T'))
        {
            return true;
        }
        return false;
    }
    private static boolean IsFrontVowel(char in)
    {
        if((in == 'E')
        || (in == 'I')
        || (in == 'Y'))
        {
            return true;
        }
        return false;
    }
    private static boolean IsVowel(char in)
    {
        if((in == 'A')
        || (in == 'E')
        || (in == 'I')
        || (in == 'O')
        || (in == 'U'))
        {
            return true;
        }
        return false;
    }
    // data members
    private static final String INFO = "Metaphone";
}
