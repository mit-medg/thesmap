package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This class provides a class for all Lvg Flow specific options.  This 
* class is used in the LvgCmdApi.
*
* <p><b>History:</b>
* <ul>
* <li>SCR-15, chlu, 08-01-12, add derivtional type options
* <li>SCR-20, chlu, 08-01-12, add derivtion negation options
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class LvgFlowSpecificOption 
{
    // public constructor
    public LvgFlowSpecificOption()
    {
    }
    /**
    * Set the maximum Permutation number for uninflecting words
    *
    * @param   value  maximum permutation number for uninflecting words
    */
    public void SetMaxPermuteTermNum(int value)
    {
        maxPermuteTermNum_ = value;
    }
    /**
    * Set the maximum Metaphone code length
    *
    * @param   value  the maximum Metaphone code length
    */
    public void SetMaxMetaphoneCodeLength(int value)
    {
        maxMetaphoneCodeLength_ = value;
    }
    /**
    * Set the derivation output filter
    *
    * @param   value  value of the type of derivation output filter
    */
    public void SetDerivationFilter(int value)
    {
        derivationFilter_ = value;
    }
    /**
    * Set the inflection output filter
    *
    * @param   value  value of the type of inflection output filter
    */
    public void SetInflectionFilter(int value)
    {
        inflectionFilter_ = value;
    }
    /**
    * Set the derivation type
    *
    * @param   optionStr  the type of derivation: suffixD, prefixD, zeroD
    */
    public void SetDerivationType(String optionStr)
    {
        derivationType_ = GetDerivationTypeInt(optionStr);
    }
    /**
    * Set the derivation negation
    *
    * @param   optionStr  the negation of derivation: negative or otherwise
    */
    public void SetDerivationNegation(String optionStr)
    {
        derivationNegation_ = GetDerivationNegationInt(optionStr);
    }
    /**
    * Get the maximum permute term number for uninflecting words
    *
    * @return   maximum permute term number for uninflecting words
    */
    public int GetMaxPermuteTermNum()
    {
        return maxPermuteTermNum_;
    }
    /**
    * Get the maximum Metaphone code length
    *
    * @return   the maximum Metaphone code length
    */
    public int GetMaxMetaphoneCodeLength()
    {
        return maxMetaphoneCodeLength_;
    }
    /**
    * Get the derivation output filter
    *
    * @return   value of the type of derivation output filter
    */
    public int GetDerivationFilter()
    {
        return derivationFilter_;
    }
    /**
    * Get the inflection output filter
    *
    * @return   value of the type of inflection output filter
    */
    public int GetInflectionFilter()
    {
        return inflectionFilter_;
    }
    /**
    * Get the derivation type
    *
    * @return   value of the type of derivation: suffixD, prefixD, zeroD
    */
    public int GetDerivationType()
    {
        return derivationType_;
    }
    /**
    * Get the derivation negation
    *
    * @return   value of the negation of derivation: negative or otherwise
    */
    public int GetDerivationNegation()
    {
        return derivationNegation_;
    }

    /**
    * Get the derivation type value from string
    *
    * @param optionStr derivation type in String
    *
    * @return   value of the derivation type in integer
    */
    public static int GetDerivationTypeInt(String optionStr)
    {
        int kdtInt = OutputFilter.D_TYPE_ALL;

        if(optionStr.equals(KDT_Z) == true)
        {
            kdtInt = OutputFilter.D_TYPE_ZERO;
        }
        else if(optionStr.equals(KDT_S) == true)
        {
            kdtInt = OutputFilter.D_TYPE_SUFFIX;
        }
        else if(optionStr.equals(KDT_P) == true)
        {
            kdtInt = OutputFilter.D_TYPE_PREFIX;
        }
        else if(optionStr.equals(KDT_ZS) == true)
        {
            kdtInt = OutputFilter.D_TYPE_ZERO_SUFFIX;
        }
        else if(optionStr.equals(KDT_ZP) == true)
        {
            kdtInt = OutputFilter.D_TYPE_ZERO_PREFIX;
        }
        else if(optionStr.equals(KDT_SP) == true)
        {
            kdtInt = OutputFilter.D_TYPE_SUFFIX_PREFIX;
        }

        return kdtInt;
    }

    /**
    * Get the derivation negation value from string
    *
    * @param optionStr derivation negation in String
    *
    * @return   value of the derivation negation in integer
    */
    public static int GetDerivationNegationInt(String optionStr)
    {
        int kdnInt = OutputFilter.D_NEGATION_OTHERWISE;

        if(optionStr.equals(KDN_N) == true)
        {
            kdnInt = OutputFilter.D_NEGATION_NEGATIVE;
        }
        else if(optionStr.equals(KDN_B) == true)
        {
            kdnInt = OutputFilter.D_NEGATION_BOTH;
        }

        return kdnInt;
    }

    // private method
    // data members
    // from configuration file
    private int maxPermuteTermNum_ = -1;      // Used in uninflected words -f:B
    private int maxMetaphoneCodeLength_ = -1; // Used in Metaphone -f:m
    // from command line option
    private int derivationFilter_ = OutputFilter.LVG_ONLY;        // default
    private int inflectionFilter_ = OutputFilter.LVG_OR_ALL;    // default

    private int derivationType_ = OutputFilter.D_TYPE_ALL;        // default
    private int derivationNegation_ = OutputFilter.D_NEGATION_OTHERWISE;

    final public static String KDT_Z = "Z";
    final public static String KDT_S = "S";
    final public static String KDT_P = "P";
    final public static String KDT_ZS = "ZS";
    final public static String KDT_ZP = "ZP";
    final public static String KDT_SP = "SP";
    final public static String KDT_ZSP = "ZSP";

    final public static String KDN_O = "O";        // otherwise
    final public static String KDN_N = "N";        // negation
    final public static String KDN_B = "B";        // both
}
