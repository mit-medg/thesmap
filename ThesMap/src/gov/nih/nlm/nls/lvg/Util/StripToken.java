package gov.nih.nlm.nls.lvg.Util;
import java.util.*;
/*****************************************************************************
* This class tokenizes a string into Vector<StrTokenObject>.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see    StrTokenObject
* @see    <a href="../../../../../../../designDoc/UDF/token/index.html">Design document</a>
*
* @version    V-2013
***************************************************************************/
public class StripToken
{
    // public methods
    /**
    * Tokenize a string and assigned token type to each token.  This method 
    * uses the default delimiters defined in StrTokenObject.  They are 
    * briefly described as follows:
    * <ul>
    * <li>restoreD: A String contains delimiters that will be restored
    *     after tokenized
    * <li>strippingD: A String contains delimiters that will be restored and
    *     it's previous token will be stripped out if they are in conflict 
    *     list or their type are stripped.
    * <li>strippableD: A String contains delimiters that will be restored 
    *     (stripped) if it's previous token is not stripped out (stripped).
    * <li>confList: char that can not be used before a delim of strippingD
    * <li>spaceD:    space deliminator for nor-restore
    * </ul>
    * No overlap is allowed between restoreD, strippingD, & strippableD.
    *
    * @param   inStr  a String to be tokenized
    *
    * @return  Vector<StrTokenObject> tokenized elements
    */
    public static Vector<StrTokenObject> Tokenize(String inStr)
    {
        return Tokenize(inStr, StrTokenObject.RESTORE_D_STR, 
            StrTokenObject.STRIPPING_D_STR, StrTokenObject.STRIPABLE_D_STR,
            StrTokenObject.SPACE_D_STR);
    }
    /**
    * Tokenize a string and assigned token type to each token.
    *
    * @param   inStr  a String to be tokenized
    * @param   restoreD  a String contains delimiters that will be restored 
    *          after tokenized
    * @param   strippingD  a String contains delimiters that it's previous
    *          token will be stripped out if they are in conflict list or their
    *          type are stripped.
    * @param   strippableD  a String contains delimiters that will be restored
    *          if it's previous token is not stripped out.
    * @param   spaceD  a String contains space delimiters. 
    *
    * @return  Vector<StrTokenObject> - tokenized elements
    */
    public static Vector<StrTokenObject> Tokenize(String inStr, String restoreD,
        String strippingD, String strippableD, String spaceD)
    {
        // use space delim to tokenize the input string
        Vector<StrTokenObject> tokenList = new Vector<StrTokenObject>();
        StringTokenizer buf = new StringTokenizer(inStr, spaceD);
        while(buf.hasMoreTokens() == true)
        {
            String cur = buf.nextToken(); 
            // handle each token in details
            Vector<StrTokenObject> curList 
                = Tokenize(cur, restoreD, strippingD, strippableD);
            
            tokenList.addAll(curList);                // put tokens back 
            tokenList.addElement(SPACE_DELIM);        // add a space between
        }
        // remove the last SPACE_DELIM
        int lastIndex = tokenList.size()-1;
        if(lastIndex >= 0)
        {
            tokenList.remove(lastIndex);
        }
        return tokenList;
    }
    /**
    * Tokenize a string and assigned token type to each token.
    *
    * @param   inStr  a String to be tokenized
    * @param   restoreD  a String contains delimiters that will be restored 
    *          after tokenized
    * @param   strippingD  a String contains delimiters that it's previous
    *          token will be stripped out if they are in conflict list or their
    *          type are stripped.
    * @param   strippableD  a String contains delimiters that will be restored
    *          if it's previous token is not stripped out.
    *
    * @return  Vector<StrTokenObject> - tokenized elements
    */
    public static Vector<StrTokenObject> Tokenize(String inStr, String restoreD,
        String strippingD, String strippableD)
    {
        Vector<StrTokenObject> tokenList = new Vector<StrTokenObject>();
        String allDelim = restoreD + strippingD + strippableD;
        // tokenize the input and assign the type for each token
        StringTokenizer buf = new StringTokenizer(inStr, allDelim, true);
        while(buf.hasMoreTokens() == true)
        {
            String cur = buf.nextToken();
            if(restoreD.indexOf(cur) != -1)
            {
                AddToList(cur, StrTokenObject.RESTORE_D, tokenList);
            }
            else if(strippingD.indexOf(cur) != -1)
            {
                AddToList(cur, StrTokenObject.STRIPPING_D, tokenList);
            }
            else if(strippableD.indexOf(cur) != -1)
            {
                AddToList(cur, StrTokenObject.STRIPABLE_D, tokenList);
            }
            else
            {
                AddToList(cur, StrTokenObject.TOKEN, tokenList);
            }
        }
        return tokenList;
    }
    /**
    * Clean up elements in the tokenized Vector.  This method includes lots of
    * smart operations while restoring tokens into a string.  This is used
    * when strip with punctuations.  In such case, some punctuations need to be
    * stripped automatically.  This method uses the defualt conflict string
    * " -,:;".
    *
    * @param   list  a tokenized vector.
    *
    * @return  a vector with clean-up tokenized elements.
    *
    * @see    StrTokenObject
    */
    public static Vector<StrTokenObject> CleanUpToken(
        Vector<StrTokenObject> list)
    {
        return CleanUpToken(list, StrTokenObject.CONFLICT_STR);
    }
    /**
    * Clean up elements in the tokenized Vector.  This method includes lots of
    * smart operations while restoring tokens into a string.  This is used
    * when strip with punctuations.  In such case, some punctuations need to be
    * stripped automatically.
    *
    * @param   list  a tokenized vector.
    * @param   confList   a string contians all conflict characters.  
    *          A conflict character is a character need to be stripped if
    *          the following token is stripping type.
    *
    * @return  a vector with clean-up tokenized elements.
    */
    public static Vector<StrTokenObject> CleanUpToken(
        Vector<StrTokenObject> list, String confList)
    {
        Vector<StrTokenObject> newList = new Vector<StrTokenObject>();
        int lastDelimType = StrTokenObject.NONE;    // type of last token
        // go through the list
        for(int i = 0; i < list.size(); i++)
        {
            StrTokenObject cur = list.elementAt(i);
            String curToken = cur.GetTokenStr();
            int curDelimType = cur.GetTokenType();
            switch(curDelimType)
            {
                // add the element if it is a token type
                case StrTokenObject.TOKEN:        
                    newList.addElement(cur);
                    lastDelimType = curDelimType;
                    break;
                // add the element if it is a space type and the previous token
                // is a token, restored, stripping, or stripable delimiter
                case StrTokenObject.SPACE_D:
                    if((lastDelimType == StrTokenObject.TOKEN)
                    || (lastDelimType == StrTokenObject.RESTORE_D)
                    || (lastDelimType == StrTokenObject.STRIPPING_D)
                    || (lastDelimType == StrTokenObject.STRIPABLE_D))
                    {
                        newList.addElement(cur);
                        lastDelimType = curDelimType;
                    }
                    break;
                // Don't add (remove) the element if it is a stripped type
                case StrTokenObject.STRIPPED:
                    lastDelimType = curDelimType;
                    break;
                // Add the element if it is a restore type
                case StrTokenObject.RESTORE_D:
                    newList.addElement(cur);
                    lastDelimType = curDelimType;
                    break;
                case StrTokenObject.STRIPPING_D:
                    //remove conflict list
                    if(newList.size() > 0)        // check if any char before it 
                    {
                        int prevIndex = newList.size() - 1;
                        StrTokenObject prev = newList.elementAt(prevIndex);
                        int prevTokenType = prev.GetTokenType();
                        String prevTokenStr = prev.GetTokenStr();
                        // remove previous element if the previous element is 
                        // stripped type or a character in conflict list
                        while((prevTokenType == StrTokenObject.STRIPPED)
                        || ((prevTokenStr.length() == 1)
                        && (confList.indexOf(prevTokenStr.charAt(0)) != -1))) 
                        {
                            newList.remove(prevIndex);
                            prevIndex = newList.size() -1;
                            prev = newList.elementAt(prevIndex);
                            prevTokenStr = prev.GetTokenStr();
                            prevTokenType = prev.GetTokenType();
                        }
                    }
                    // add the element if the newList is empty
                    newList.addElement(cur);
                    lastDelimType = curDelimType;
                    break;
                // Add the element if the previous token is stripped type
                case StrTokenObject.STRIPABLE_D:
                    if(lastDelimType != StrTokenObject.STRIPPED)
                    {
                        newList.addElement(cur);
                        lastDelimType = curDelimType;
                    }
                    break;
            }
        }
        return newList;
    }
    /**
    * Remove the end elements of a tokenized Vector<StrTokenObject> if it is 
    * a character in the bad end list string defined in StrTokenObject.
    *
    * @param   list  a vector of tokenized StrTokenObjects that will be removed
    *          end element (character) if it is in a badEndList.
    *
    * @return  Vector<StrTokenObject> - tokenized StrTokenObject without any 
    *          bad end character at the end.
    *
    * @see    StrTokenObject
    */
    public static Vector<StrTokenObject> CleanUpEnd(Vector<StrTokenObject> list)
    {
        return CleanUpEnd(list, StrTokenObject.BAD_END_STR);
    }
    /**
    * Remove the end elements of a tokenized Vector<StrTokenObject> if it is 
    * a character in the bad end list string.
    *
    * @param   list  a vector of tokenized StrTokenObjects that will be removed
    *          end element (character) if it is in a badEndList.
    * @param   badEndList  a String that contains all bad end characters.
    *          The default value for badEndList is " -,:;".
    *
    * @return  a vector of tokenized StrTokenObject without any bad end 
    *          character at the end.
    */
    public static Vector<StrTokenObject> CleanUpEnd(Vector<StrTokenObject> list,
        String badEndList)
    {
        Vector<StrTokenObject> newList = new Vector<StrTokenObject>(list);
        if(newList.size() > 0)
        {
            int lastIndex = newList.size() - 1;
            StrTokenObject last = newList.elementAt(lastIndex);
            String lastTokenStr = last.GetTokenStr();
            // if the end character is in badEndList, remove it 
            while((lastTokenStr.length() == 1)
            && (badEndList.indexOf(lastTokenStr.charAt(0)) != -1))
            {
                newList.remove(lastIndex);
                lastIndex = newList.size() -1;
                last = newList.elementAt(lastIndex);
                lastTokenStr = last.GetTokenStr();
            }
        }
        return newList;
    }
    /**
    * Compose a string from a list of tokenized StrTokenObjects.
    *
    * @param   list  Vector<StrTokenObject>
    *
    * @return  a string that is composed from the input list.
    */
    public static String ComposeString(Vector<StrTokenObject> list)
    {
        // compose String
        String out = new String();
        for(int i = 0; i < list.size(); i++)
        {
            StrTokenObject temp = list.elementAt(i);
            out += temp.GetTokenStr();
        }
        return out;
    }
    /**
    *    Test driver for this class
    */
    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.out.println("** Usage: java Str <inWord>");
        }
        else
        {
            String inWord = args[0];
            Vector<StrTokenObject> tokenList = StripToken.Tokenize(inWord);
            System.out.println("---------------------------------------------");
            System.out.println("in:  '" + inWord + "'");
            System.out.println("out: '" + StripToken.ComposeString(tokenList) 
                + "'");
            Vector<StrTokenObject> newList = StripToken.CleanUpToken(tokenList);
            System.out.println("new: '" + StripToken.ComposeString(newList) 
                + "'");
        }
    }
    // private methods
    // Add strTokenObj into a Vector<StrTokenObject> (list)
    private static void AddToList(String tokenStr, int tokenType, 
        Vector<StrTokenObject> list)
    {
        if(tokenStr != null)
        {
            StrTokenObject strTokenObj = 
                new StrTokenObject(tokenStr, tokenType);
            list.addElement(strTokenObj);
        }
    }
    // print out all elements from a Vector<StrTokenObject> (list)
    private static void PrintTokenVector(Vector<StrTokenObject> list)
    {
        System.out.println("===============================================");
        for(int i = 0; i < list.size(); i++)
        {
            StrTokenObject temp = list.elementAt(i);
            System.out.println(i + ": [" + temp.GetTokenType() + "]-["
                + temp.GetTokenStr() + "]");
        }
    }
    // data member
    final private static StrTokenObject SPACE_DELIM = 
        new StrTokenObject(" ", StrTokenObject.SPACE_D);    // space delimiter
}
