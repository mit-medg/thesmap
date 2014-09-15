package gov.nih.nlm.nls.lvg.Api;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides an API for WordInd.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class FieldsApi
{
    // private constructor
    /**
    * Creates a FieldsApi object and initiate related data (default).
    */
    public FieldsApi()
    {
    }
    // public methods
    /**
    * A method to cut out and/or rearrange fields of an input string
    *
    * @param   inStr  an input String format to be mutated/fields
    * @param   fs  field separator
    * @param   fieldList  the list of field for output
    *
    * @return  String result of rearrange fields
    */
    public static String Mutate(String inStr, String fs, 
        Vector<Integer> fieldList)
    {
        // check if no fieldList specified, return the original string
        if(fieldList.size() == 0)
        {
            return inStr;
        }
        // form the vector for fields
        StringTokenizer buf = new StringTokenizer(inStr, fs, true);
        Vector<String> inStrList = new Vector<String>();
        String curToken = new String();
        String lastToken = fs;
        while(buf.hasMoreTokens() == true)
        {
            curToken = buf.nextToken();
            if((curToken.equals(fs)) == true)
            {
                if((lastToken.equals(fs)) == true)
                {
                    inStrList.addElement("");
                }
            }
            else
            {
                inStrList.addElement(curToken);
            }
            lastToken = curToken;
        }
        // form the output
        String out = new String();
        for(int i = 0; i < fieldList.size(); i++)
        {
            int index = (fieldList.elementAt(i)).intValue()-1;
            if(index < inStrList.size())
            {
                out += (inStrList.elementAt(index)  + fs);
            }
            else
            {
                out += fs;
            }
        }
        // TBD: should common next line out
        int fsLength = fs.length();
        out = out.substring(0, out.length()-fsLength);  // take out the last fs
        return out;
    }
    // private methods
    // data members
}
