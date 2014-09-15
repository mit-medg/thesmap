package gov.nih.nlm.nls.lvg.Util;
import java.io.*;
/*****************************************************************************
* This class is a sample prolog for LVG Java codes.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Out
{
    // public methods
    /**
    * Print message to a designated output buffer
    *
    * @param   outWriter  the designated outputs 
    * @param   msg  massage for printing out
    * @param   toStringFlag  a boolean flag for sending output to a String
    */
    public static void Print(BufferedWriter outWriter, String msg,
        boolean fileOutput, boolean toStringFlag) throws IOException
    {
        if(toStringFlag == true)    // output to a String
        {
            if(outString_ == null)
            {
                outString_ = msg; 
            }
            else
            {
                outString_ += msg;
            }
        }
        else if(fileOutput == false)  // std output
        {
            outWriter.write(msg);
            outWriter.flush();
        }
        else
        {
            outWriter.write(msg);
        }
    }
    /**
    * Print message and append a linebreaker to a designated output buffer
    *
    * @param   outWriter  the designated outputs 
    * @param   msg  massage for printing out
    * @param   toStringFlag  a boolean flag for sending output to a String
    */
    public static void Println(BufferedWriter outWriter, String msg, 
        boolean fileOutput, boolean toStringFlag) throws IOException
    {
        if(toStringFlag == true)    // output to a String
        {
            StringBuffer buffer = new StringBuffer();
            if(outString_ == null)
            {
                buffer.append(msg);
                buffer.append(System.getProperty("line.separator").toString());
                outString_ = buffer.toString();
            }
            else
            {
                buffer.append(outString_);
                buffer.append(msg);
                buffer.append(System.getProperty("line.separator").toString());
                outString_ = buffer.toString();
            }
        }
        else if(fileOutput == false)  // std output
        {
            outWriter.write(msg);
            outWriter.newLine();
            outWriter.flush();
        }
        else // file output
        {
            outWriter.write(msg);
            outWriter.newLine();
        }
    }
    /**
    * Reset the output string
    */
    public static void ResetOutString()
    {
        outString_ = null;
    }
    /**
    * Get the output String
    */
    public static String GetOutString()
    {
        return outString_;
    }
    // data member
    private static String outString_ = null;
}
