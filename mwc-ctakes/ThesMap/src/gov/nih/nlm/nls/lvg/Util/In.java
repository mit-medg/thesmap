package gov.nih.nlm.nls.lvg.Util;
import java.util.*;
/*****************************************************************************
* This class provides inputs related methods.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class In
{
    // Public Methods
    /**
    * Get the field string from the input line
    *
    * @param   line  the input line String
    * @param   delimiter  the field delimiter
    * @param   fieldNum  the number of field to be retreived
    *
    * @return  result of bidwise addition of two longs.
    */
    public static String GetField(String line, String delimiter, int fieldNum)
    {
        int delimiterSize = delimiter.length();
        int currentField = 0;
        int beginIndex = 0;
        int endIndex = 0-delimiterSize;
        String field = line;
        // set min. field num
        if(fieldNum < 1)
        {
            fieldNum = 1;
        }
        // fidn the begin and end index of the field
        while(currentField < fieldNum)
        {
            currentField++;
            beginIndex = endIndex+delimiterSize;
            endIndex = line.indexOf(delimiter, beginIndex);
            // reach the last field, exit out, use last field for the result
            if(endIndex == -1)
            {
                endIndex = line.length();
                break;
            }
        }
        field = line.substring(beginIndex, endIndex);
        return field;
    }
    /**
    * Get the output string from the input line
    *
    * @param   line  the input line String
    * @param   delimiter  the field delimiter
    * @param   outputFieldList  a vector contains field numbers of the input for
    *          output
    *
    * @return  result of bidwise addition of two longs.
    */
    public static String GetOutTerm(String line, String delimiter, 
        Vector<Integer> outputFieldList)
    {
        String outString = new String();
        if(outputFieldList.size() == 0)
        {
            return outString;
        }
        StringTokenizer buf = new StringTokenizer(line, delimiter);
        // get the field string
        Vector<String> outStringList = new Vector<String>();
        while(buf.hasMoreTokens() == true)
        {
            outStringList.addElement(buf.nextToken());
        }
        // form the output string
        for(int i = 0; i < outputFieldList.size(); i++)
        {
            int index = outputFieldList.elementAt(i).intValue()-1;
            if((index >= 0) && (index < outStringList.size()))
            {
                outString += outStringList.elementAt(index) + delimiter;
            }
            else
            {
                outString += "null" + delimiter;
            }
        }
        return outString;
    }
}
