package gov.nih.nlm.nls.lvg.Util;
/*****************************************************************************
* This class represents character related methods.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Char
{
    // public methods
    /**
    * Check if a char is a puntuation.  Punctuations include:
    * <li>DASH_PUNCTUATION: -
    * <li>START_PUNCTUATION: ( { [
    * <li>END_PUNCTUATION: ) } ]
    * <li>CONNECTOR_PUNCTUATION: _
    * <li>OTHER_PUNCTUATION: !@#%&*\:;"',.?/
    * <li>MATH_SYMBOL: ~ + = | < >
    * <li>CURRENCY_SYMBOL: $
    * <li>MODIFIER_SYMBOL: ` ^
    *
    * @param   inChar  input chararter for checking if it is a punctuation
    *
    * @return  true or false if the input character is or is not a punctuation
    */
    public static boolean IsPunctuation(char inChar)
    {
        boolean isPunctuation = false;
        int type = Character.getType(inChar);
        // check if the input is a punctuation
        if((type == Character.DASH_PUNCTUATION)         // -
        || (type == Character.START_PUNCTUATION)        // ( { [
        || (type == Character.END_PUNCTUATION)          // ) } ]
        || (type == Character.CONNECTOR_PUNCTUATION)    // _
        || (type == Character.OTHER_PUNCTUATION)        // !@#%&*\:;"',.?/ 
        || (type == Character.MATH_SYMBOL)              // ~ + = | < >
        || (type == Character.CURRENCY_SYMBOL)          // $
        || (type == Character.MODIFIER_SYMBOL))         // ` ^
        {
            isPunctuation = true;
        }
        return isPunctuation;
    }
}
