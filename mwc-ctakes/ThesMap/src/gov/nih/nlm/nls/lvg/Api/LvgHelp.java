package gov.nih.nlm.nls.lvg.Api;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class prints out all help menu for LVG command line syntax.
* <p> This class should be modified to become file driven in the future (TBD).
*
* <p><b>History:</b>
* <ul>
* <li>SCR-15, chlu, 07-23-12, add derivation type options
* <li>SCR-20, chlu, 07-23-12, add derivtion negation options
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class LvgHelp
{
    // public methods
    /**
    * Print out Lvg system help menu.  
    *
    * <p> The option flags and command executions
    * are defined in the classes of Flow and LvgCmdApi, respectively.
    *
    * @param bw    buffer writer for Stdout or file out
    * @param fileOutput  a flag to indicate if the bw is a file output
    *
    * @see gov.nih.nlm.nls.lvg.Lib.Flow 
    * @see LvgCmdApi 
    * @see <a href="../../../../../../../designDoc/LifeCycle/requirement/LvgOption.html"> 
    * Design Document</a>
    */
    public static void LvgHelp(BufferedWriter bw, boolean fileOutput)
    {
        bw_ = bw;
        fileOutput_ = fileOutput;
        // print out the usage
        MenuPrint("");
        MenuPrint("Synopsis:");
        MenuPrint("  lvg [options]");
        MenuPrint("");
        MenuPrint("Description:");
        MenuPrint("  Lexical Variant Generator.");
        MenuPrint("");
        MenuPrint("Options:");
        MenuPrint("  -ccgi    Mark the end of the set of variants returned.");
        MenuPrint("  -cf:INT  Input category field.");
        MenuPrint("  -ci      Show configuration information.");
        MenuPrint("  -C:INT   Case setting.");
        MenuPrint("  -CR:o    Combine records by output terms.");
        MenuPrint("  -CR:oc   Combine records by categories.");
        MenuPrint("  -CR:oe   Combine records by EUI (used in flow s with -m option on).");
        MenuPrint("  -CR:oi   Combine records by inflections.");
        MenuPrint("  -d       Displays details status for each transformation.");
        MenuPrint("  -DC:LONG Display variants contain categories specified.");
        MenuPrint("  -DI:LONG Display variants contain inflections specified.");
        MenuPrint("  -EC:LONG Display variants exclude categories specified.");
        MenuPrint("  -EI:LONG Display variants exclude inflections specified.");
        MenuPrint("  -f:h     Help information for flow components.");
        MenuPrint("  -F:INT   Specified the field for output to display.");
        MenuPrint("  -F:h     Help information for specifying output fields.");
        MenuPrint("  -h       Display program help information (this is it).");
        MenuPrint("  -hs      Display option's hierarchy structure."); 
        MenuPrint("  -i:STR   Define input file name.  The default is screen input");
        MenuPrint("  -if:INT  Input inflection field");
        MenuPrint("  -kd:INT  Restricts the output generated from the derivation morphology (1,2,3)."); 
        MenuPrint("  -kdn:STR  derivation negations (O|N|B)."); 
        MenuPrint("  -kdt:STR  derivation types (Z|S|P|ZS|ZP|SP|ZSP)."); 
        MenuPrint("  -ki:INT  Restricts the output generated from the inflection morphology (1,2,3)."); 
        MenuPrint("  -m       Displays extra information for mutation.");
        MenuPrint("  -n       Return a \"-No Output-\" message when an input produces no output.");
        MenuPrint("  -o:STR   Define output file name.  The default is screen output");
        MenuPrint("  -p       Show the prompt. The default is no prompt.");
        MenuPrint("  -R:INT   Restrict the number of variants for one flow.");
        MenuPrint("  -s:STR   Defines a field separator.");
        MenuPrint("  -SC      Show category in name. The default is in number.");
        MenuPrint("  -SI      Show inflection in name. The default is in number.");
        MenuPrint("  -St:o    Sort outputs by output terms in an alphabetical order.");
        MenuPrint("  -St:oc   Sort outputs by output terms and category.");
        MenuPrint("  -St:oci  Sort outputs by output terms, category, and inflection.");
        MenuPrint("  -t:INT   Define the field to use as the term field.  The default is 1.");
        MenuPrint("  -ti      Display the filtered input term in the output"); 
        MenuPrint("  -v       Returns the current version identification of lvg.");
        MenuPrint("  -x:STR   Loading an alternative configuration file.");
    }
    /**
    * Print out the Lvg output fields help menu.  The format of Lvg outputs are:
    * in term | out term | categories | inflections | flow history | flow number
    *  | additional mutation information |
    */
    public static void OutputFieldHelp(BufferedWriter bw, boolean fileOutput)
    {
        bw_ = bw;
        fileOutput_ = fileOutput;
        MenuPrint("  -F:1     Print output field 1 - input term");
        MenuPrint("  -F:2     Print output field 2 - output term");
        MenuPrint("  -F:3     Print output field 3 - categories");
        MenuPrint("  -F:4     Print output field 4 - inflections");
        MenuPrint("  -F:5     Print output field 5 - flow history");
        MenuPrint("  -F:6     Print output field 6 - flow number");
        MenuPrint("  -F:7+    Print output field above 7 - mutate information");
        MenuPrint("  -F:1:2:5 Print output fields 1, 2, and 5");
    }
    /**
    * Print out the help menu of Lvg flow components.
    *
    * @see 
    * <a href="../../../../../../../designDoc/UDF/flow/index.html">
    * Design Document</a>
    */
    public static void FlowHelp(BufferedWriter bw, boolean fileOutput)
    {
        bw_ = bw;
        fileOutput_ = fileOutput;
        MenuPrint("  -f:0       Strip NEC and NOS.");
        MenuPrint("  -f:a       Generate known acronym expansions.");
        MenuPrint("  -f:A       Generate known acronyms.");
        MenuPrint("  -f:An      Generate antiNorm.");
        MenuPrint("  -f:b       Uninflect the input term.");
        MenuPrint("  -f:B       Uninflect words.");
        MenuPrint("  -f:Bn      Normalized Uninflect words.");
        MenuPrint("  -f:c       Tokenize.");
        MenuPrint("  -f:ca      Tokenize keep all.");
        MenuPrint("  -f:ch      Tokenize no hyphens.");
        MenuPrint("  -f:C       Canonicalize.");
        MenuPrint("  -f:Ct      Lexical name.");
        MenuPrint("  -f:d       Generate derivational variants.");
        MenuPrint("  -f:dc~LONG Generate derivational variants, specifying output categories");
        MenuPrint("  -f:e       Retrieve uninflected spelling variants.");
        MenuPrint("  -f:E       Retrieve Eui.");
        MenuPrint("  -f:f       Filter output.");
        MenuPrint("  -f:fa      Filter out acronyms and abbreviations.");
        MenuPrint("  -f:fp      Filter out proper nouns.");
        MenuPrint("  -f:g       Remove Genitive.");
        MenuPrint("  -f:G       Generate fruitful variants.");
        MenuPrint("  -f:Ge      Fruitful variants, enhanced.");
        MenuPrint("  -f:Gn      Generate known fruitful variants.");
        MenuPrint("  -f:h       Help menu for flow components (this is it).");
        MenuPrint("  -f:i       Generate inflectional variants.");
        MenuPrint("  -f:ici~LONG+LONG  Generate inflectional variants, specifying output categories and inflections");
        MenuPrint("  -f:is      Generate inflectional variants (simple infl).");
        MenuPrint("  -f:l       Lowercase the input.");
        MenuPrint("  -f:L       Retrieve category and inflection.");
        MenuPrint("  -f:Ln      Retrieve category and inflection from database.");
        MenuPrint("  -f:Lp      Retrieve category and inflection for all terms begins with the given word.");
        MenuPrint("  -f:m       Metaphone.");
        MenuPrint("  -f:n       No operation.");
        MenuPrint("  -f:nom     Retrieve nominalizations.");
        MenuPrint("  -f:N       Normalize.");
        MenuPrint("  -f:N3      LuiNormalize.");
        MenuPrint("  -f:o       Replace punctuation with space.");
        MenuPrint("  -f:p       Strip punctuation.");
        MenuPrint("  -f:P       Strip punctuation, enhanced.");
        MenuPrint("  -f:q       Strip diacritics.");
        MenuPrint("  -f:q0      Map symbols to ASCII.");
        MenuPrint("  -f:q1      Map Unicode to ASCII.");
        MenuPrint("  -f:q2      Split ligatures.");
        MenuPrint("  -f:q3      Get Unicode names.");
        MenuPrint("  -f:q4      Get Unicode synonyms.");
        MenuPrint("  -f:q5      Norm Unicode to ASCII.");
        MenuPrint("  -f:q6      Norm Unicode to ASCII with synonym option.");
        MenuPrint("  -f:q7      Unicode core norm.");
        MenuPrint("  -f:q8      Strip or map Unicode.");
        MenuPrint("  -f:r       Recursive synonyms.");
        MenuPrint("  -f:rs      Remove (s), (es), (ies).");
        MenuPrint("  -f:R       Recursive derivations.");
        MenuPrint("  -f:s       Generate spelling variants.");
        MenuPrint("  -f:S       Syntactic uninvert.");
        MenuPrint("  -f:Si      Simple inflections.");
        MenuPrint("  -f:t       Strip stop words.");
        MenuPrint("  -f:T       Strip ambiguity tags.");
        MenuPrint("  -f:u       Uninvert phrase around commas.");
        MenuPrint("  -f:U       Convert output.");
        MenuPrint("  -f:v       Generate fruitful variants from database.");
        MenuPrint("  -f:w       Sort by word order.");
        MenuPrint("  -f:ws~INT  Word size filter.");
        MenuPrint("  -f:y       Generate synonyms.");
    }
    // private methods
    private static void MenuPrint(String text)
    {
        try
        {
            Out.Println(bw_, text, fileOutput_, false);
        }
        catch (IOException e)
        {
        }
    }
    private static BufferedWriter bw_ = null;
    private static boolean fileOutput_ = false;
}
