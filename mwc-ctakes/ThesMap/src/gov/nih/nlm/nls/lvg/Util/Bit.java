package gov.nih.nlm.nls.lvg.Util;
/*****************************************************************************
* This class provides methods of bit operations.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Bit
{
    // public methods
    /**
    * Add two integers bitwise.  This operation is a bitwise exclusive or 
    * operation.
    *
    * @param   b1  the first integer to be added
    * @param   b2  the second integer to be added
    *
    * @return  result of bidwise addition of two integers.
    */
    public static int Add(int b1, int b2)
    {
        int b3 = b1 | b2;
        return b3;
    }
    /**
    * Minus two long bitwise.  This operation is a bitwise operation.
    * It take off bit if both bit are 1.  keep the same as the first input, 
    * otherwise.
    *
    * @param   b1  the first long to be deducted
    * @param   b2  the second long to deduct
    *
    * @return  result of bidwise minus of two longs.
    */
    public static long Minus(long b1, long b2)
    {
        long b3 = b1 ^ (b1 & b2);
        return b3;
    }
    /**
    * Add two long bitwise.  This operation is a bitwise exclusive or 
    * operation.
    *
    * @param   b1  the first long to be added
    * @param   b2  the second long to be added
    *
    * @return  result of bidwise addition of two longs.
    */
    public static long Add(long b1, long b2)
    {
        long b3 = b1 | b2;
        return b3;
    }
    /**
    * Determine if a long (container) contains another long (item).
    * Contains means all bits with value of 1 in item is also 1 in the container
    * Exclusive and is used for this bitwise contain operation.
    *
    * @param   container  the long container
    * @param   item  the long containee
    *
    * @return  true and false if container contains or not contain item.
    */
    public static boolean Contain(long container, long item)
    {
        boolean containFlag = ((container & item) == item);
        return containFlag;
    }
}
