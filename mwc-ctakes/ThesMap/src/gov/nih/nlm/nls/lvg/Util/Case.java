package gov.nih.nlm.nls.lvg.Util;
/*****************************************************************************
* This class provides methods of preserving case.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Case
{
    // public methods
    /**
    * Preserve cases
    *
    * @param   inStr  the String to be preserved cases
    * @param   caseStr  the String which preserving cases will be based on
    *
    * @return  the case preserved string
    */
    public static String PreserveCase(String inStr, String caseStr)
    {
        int inLength = inStr.length();
        int caseLength = caseStr.length();
        int fixLength = ((inLength > caseLength) ? caseLength : inLength);
        char[] fixStr = inStr.toCharArray();
        // go through the String and fix case one character by one character
        for(int i = 0; i < fixLength; i++)
        {
            if(Character.isUpperCase(caseStr.charAt(i)) == true)
            {
                fixStr[i] = Character.toUpperCase(fixStr[i]);
            }
        }
        // reform the String
        String outStr = new String(fixStr);
        return outStr;
    }
    /**
    * A test driver for this class
    */
    public static void main(String[] args)
    {
        if(args.length != 2)
        {
            System.out.println("** Usage: java Case <inStr> <caseStr>");
        }
        else
        {
            String inStr = args[0];
            String caseStr = args[1];
            System.out.println("-- inStr: '" + inStr + "'");
            System.out.println("-- caseStr: '" + caseStr + "'");
            String outStr = Case.PreserveCase(inStr, caseStr);
            System.out.println("=> outStr: '" + outStr + "'");
        }
    }
}
