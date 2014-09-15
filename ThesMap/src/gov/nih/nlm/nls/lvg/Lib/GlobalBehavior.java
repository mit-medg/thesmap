package gov.nih.nlm.nls.lvg.Lib;
import java.io.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides LVg Global Behavior related definition and methods.
*
* <p><b>History:</b>
* <ul>
* <li>SCR-14, chlu, 06-08-12, fix version information
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class GlobalBehavior
{
    // private constructor
    private GlobalBehavior()
    {
    }
    public static void SetFieldSeparator(String value)
    {
        fieldSeparator_ = value;
    }
    public static String GetFieldSeparator()
    {
        return fieldSeparator_;
    }
    public static String GetDefaultFieldSeparator()
    {
        return FS_STR;
    }
    // public method
    // data member
    /** LVG default separator: "|" */
    final public static String LS_STR 
        = System.getProperty("line.separator").toString();    // line sep string
    // private data
    private final static String FS_STR = "|";    // field seperator string
    private static String fieldSeparator_ = FS_STR;
    /** LVG version */
    public final static String YEAR = "2013";    // year of release
    /** LVG jar string */
    public final static String LVG_JAR_FILE = "lvg" + YEAR + "dist.jar";
}
