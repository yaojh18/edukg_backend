package com.example.edukg_backend.Util;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {

    public static String getCharPinYin(char pinYinStr) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        try {
            //执行转换
            return PinyinHelper.toHanyuPinyinStringArray(pinYinStr, format)[0];

        } catch (Exception e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    public static String getFullSpell(String pinYinStr) {
        StringBuffer sb = new StringBuffer();
        String tempStr = null;
        //循环字符串
        for (int i = 0; i < pinYinStr.length(); i++) {

            tempStr = getCharPinYin(pinYinStr.charAt(i));
            if (tempStr == null) {
                //非汉字直接拼接
                sb.append(pinYinStr.charAt(i));
            } else {
                sb.append(tempStr);
            }
        }
        return sb.toString();
    }
}