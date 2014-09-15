package gov.nih.nlm.nls.lvg.Lib;
/*****************************************************************************
* This class provides an object contains category and inflection.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class CatInfl
{
    // public constructor
    public CatInfl(long cat, long infl)
    {
        cat_ = cat;
        infl_ = infl;
    }
    // public method
    /**
    * Get a long integer for the categories.
    *
    * @return  category in long
    */
    public long GetCategory()
    {
        return cat_;
    }
    /**
    * Get a long integer for the inflections.
    *
    * @return  inflection in long
    */
    public long GetInflection()
    {
        return infl_;
    }
    /**
    * Get the string format of categories.
    *
    * @return  string format of category
    */
    public String GetCategoryStr()
    {
        String categoryStr = Category.ToName(cat_);
        return categoryStr;
    }
    /**
    * Get the string format of inflections.
    *
    * @return  string format of inflection
    */
    public String GetInflectionStr()
    {
        String inflectionStr = Inflection.ToName(infl_);
        return inflectionStr;
    }
    /**
    * Convert inflection of positive in adj & adv, singular in noun, 
    * infinitive in verb to base.
    *
    * @param  in    the inflection & category object to be converted
    *
    * @return  a converted inflection & category object
    */
    public static CatInfl ConvertToBase(CatInfl in)
    {
        long cat = in.GetCategory();
        long infl = in.GetInflection();
        if((infl == Inflection.ToValue("singular")) 
        && (cat == Category.ToValue("noun")))
        {
            infl = Inflection.ToValue("base");
        }
        else if((infl == Inflection.ToValue("infinitive")) 
        && (cat == Category.ToValue("verb")))
        {
            infl = Inflection.ToValue("base");
        }
        else if((infl == Inflection.ToValue("positive")) 
        && (cat == Category.ToValue("adj")))
        {
            infl = Inflection.ToValue("base");
        }
        else if((infl == Inflection.ToValue("positive")) 
        && (cat == Category.ToValue("adv")))
        {
            infl = Inflection.ToValue("base");
        }
        return new CatInfl(cat, infl);
    }
    /**
    * Check if a category and a inflection are (legally) related
    *
    * @param  cat    the value of category 
    * @param  infl   the value of inflection 
    *
    * @return  a boolean value, true for related and false for not related
    */
    public static boolean IsRelated(long cat, long infl)
    {
        int catIndex = Category.GetBitIndex(cat);    
        int inflIndex = Inflection.GetBitIndex(infl);    
        return catInflRelation_[catIndex][inflIndex];
    }
    // data member
    private long cat_ = 0;
    private long infl_ = 0;
    private static boolean[][] catInflRelation_ = 
        new boolean[Category.TOTAL_BITS][Inflection.TOTAL_BITS];
    static
    {
        for(int i = 0; i < Category.TOTAL_BITS; i++)
        {
            catInflRelation_[i][Inflection.BASE_BIT] = true;
            for(int j = 1; j < Inflection.TOTAL_BITS; j++)
            {
                catInflRelation_[i][j] = false;
            }
        }
        catInflRelation_[Category.ADJ_BIT][Inflection.COMPARATIVE_BIT] = true;
        catInflRelation_[Category.ADJ_BIT][Inflection.SUPERLATIVE_BIT] = true;
        catInflRelation_[Category.ADJ_BIT][Inflection.POSITIVE_BIT] = true;
        catInflRelation_[Category.ADV_BIT][Inflection.COMPARATIVE_BIT] = true;
        catInflRelation_[Category.ADV_BIT][Inflection.SUPERLATIVE_BIT] = true;
        catInflRelation_[Category.ADV_BIT][Inflection.POSITIVE_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PRES_PART_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PAST_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PAST_PART_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PRES_3S_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.INFINITIVE_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PRES_1_2_3P_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PAST_NEG_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PRES_1_2_3P_NEG_BIT] 
            = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PRES_1S_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PAST_1P_2_3P_NEG_BIT] 
            = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PAST_1P_2_3P_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PAST_1S_3S_NEG_BIT] 
            = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PRES_1P_2_3P_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PRES_1P_2_3P_NEG_BIT] 
            = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PAST_1S_3S_BIT] = true;
        catInflRelation_[Category.AUX_BIT][Inflection.PRES_3S_NEG_BIT] = true;
        catInflRelation_[Category.MODAL_BIT][Inflection.PAST_BIT] = true;
        catInflRelation_[Category.MODAL_BIT][Inflection.PAST_NEG_BIT] = true;
        catInflRelation_[Category.MODAL_BIT][Inflection.PRES_BIT] = true;
        catInflRelation_[Category.MODAL_BIT][Inflection.PRES_NEG_BIT] = true;
        catInflRelation_[Category.NOUN_BIT][Inflection.PLURAL_BIT] = true;
        catInflRelation_[Category.NOUN_BIT][Inflection.SINGULAR_BIT] = true;
        catInflRelation_[Category.VERB_BIT][Inflection.PRES_PART_BIT] = true;
        catInflRelation_[Category.VERB_BIT][Inflection.PAST_BIT] = true;
        catInflRelation_[Category.VERB_BIT][Inflection.PAST_PART_BIT] = true;
        catInflRelation_[Category.VERB_BIT][Inflection.PRES_3S_BIT] = true;
        catInflRelation_[Category.VERB_BIT][Inflection.INFINITIVE_BIT] = true;
        catInflRelation_[Category.VERB_BIT][Inflection.PRES_1_2_3P_BIT] = true;
    }
}
