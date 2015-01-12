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
public class Platform
{
    private Platform()
    {
    }
    // public methods
    /*
    * This method detects if the plaform is a windows bases OS or not.
    *
    * @return true if the platform is a window based OS.
    */
    public static final boolean IsWindow()
    {
        boolean flag = false;
        String osName = System.getProperty("os.name");
        if(osName.toLowerCase().indexOf("windows") > -1)
        {
            flag = true;
        }
        return flag;
    }
}
