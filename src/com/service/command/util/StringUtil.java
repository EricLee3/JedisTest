
package com.service.command.util;

import java.sql.Clob;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 * @author sylyu
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 * */
public class StringUtil {
	

	/**
	 *	스트링배열을 (,)로 구분된 하나의 스트링으로 변환시킴
	 *
	 *	@param	String[] s	변환할 스트링배열
	 *	@return String		(,)구분자를 가지는 문자열
	 *
	 */
	public static String arrayToString(String[] s) {
		String str = new String();

		if (s != null) {
			for (int i=0; i < s.length; i++) {
				if (i != s.length - 1 )
					str += s[i] + ",";
				else
					str += s[i];
			}
		}
		return str;
	}

	public static String arrayToStringWithAmp(String[] s) {
		String str = new String();

		if (s != null) {
			for (int i=0; i < s.length; i++) {
				if (i != s.length - 1 )
					str += "'"+s[i] + "',";
				else
					str += "'"+s[i]+"'";
			}
		}
		return str;
	}

	public static String arrayListToStringWithAmp(ArrayList<Object> a) {
		String[] s = new String[a.size()];
		if (a != null) {
			for( int i = 0; i < a.size(); i++) {
				s[i] = (String) a.get(i);
			}
		}

		String str = new String();

		if (s != null) {
			for (int i=0; i < s.length; i++) {
				if (i != s.length - 1 )
					str += "'"+s[i] + "',";
				else
					str += "'"+s[i]+"'";
			}
		}
		return str;
	}


	public static String arrayListToString(ArrayList<Object> a) {
		String[] s = new String[a.size()];
		if (a != null) {
			for( int i = 0; i < a.size(); i++) {
				s[i] = (String) a.get(i);
			}
		}

		String str = new String();

		if (s != null) {
			for (int i=0; i < s.length; i++) {
				if (i != s.length - 1 )
					str += s[i] + ",";
				else
					str += s[i];
			}
		}
		return str;
	}

	/**
	 *	토큰으로 연결된 스트링을 스트링 배열로 변환
	 *
	 *	@param	String s	변환할 스트링
	 *	@param	String t	토큰
	 *	@return String[]	배열
	 *
	 */
	public static String[] stringToArray(String s, String t){
		if(isNull(s)) return null;

		StringTokenizer st = new StringTokenizer(s, t);
		int size = st.countTokens() ;
		if(size <= 0) return null;

		String[] result = new String[size];
		for(int i=0; i<size && st.hasMoreTokens(); i++){
			result[i] = st.nextToken();
		}
		return result;
	}

	/**
	 *	토큰으로 연결된 스트링을 스트링으로 변환
	 *
	 *	@param	String s	변환할 스트링
	 *	@param	String t	토큰
	 *	@return String  	배열
	 *
	 */
	public static String   stringToString(String s, String t){
		if(isNull(s)) return null;

		StringTokenizer st = new StringTokenizer(s, t);
		int size = st.countTokens() ;
		if(size <= 0) return null;

		String result = "";
		for(int i=0; i<size && st.hasMoreTokens(); i++){
			result += st.nextToken();
		}
		return result;
	}

	public static int ArrayLength(String[][] a){
		if(a == null) return 0;

		int aLen = a.length;

		if(aLen <= 0) aLen=0;

		return aLen;
	}

	public static String nullTo(String source, String replace)
	{
		try {
			if(source == null || source.equals(""))
			{
				return replace;
			}
			else
			{
				return source;
			}
		} catch (NullPointerException e) {
			return replace;
		}
		
	}

	/**
	 * String.substring(int start, int end) 대체
	 * NullPointException 방지
	 */
	public static String substring(String src, int start, int end){
		if(src == null || "".equals(src) || start > src.length() || start > end || start < 0) return "";
		if(end > src.length()) end = src.length();

		return src.substring(start, end);
	}
	/**
	 * oracle lpad함수 구현
	 */
	 public static String lpad(String source, int n, String pad){
		if(source == null)
		{
			return null;
		}
		if(source.length() >= n){
			return source;
		}
		return pad.substring(0, n - source.length()) + source;
	}


	/**
	 * money format convert.
	 */
	public static String getPriceFormat(int source) {
		String sPattern = "###,###,###,###,##0";
		DecimalFormat decimalformat = new DecimalFormat(sPattern);
		return decimalformat.format(source);
	}
	
	public static String getPriceFormat(long source)
	{
		String sPattern = "###,###,###,##0";
		DecimalFormat decimalformat = new DecimalFormat(sPattern);
		return decimalformat.format(source);
	}

	public static String getPriceFormat(double source)
	{
		String sPattern = "###,###,###,##0.##";
		DecimalFormat decimalformat = new DecimalFormat(sPattern);
		return decimalformat.format(source);
	}
	public static String getPriceFormat(Double source)
	{
		return getPriceFormat(source.doubleValue());
	}

	/**
	 *	"." 이후 문자열 리턴 ("." 포함
	 *
	 */
	public static String getExt(String szTemp)
	{
		if(szTemp == null) return "";

		String fname = "";
		if (szTemp.indexOf(".") != -1) {
			fname = szTemp.substring(szTemp.lastIndexOf("."));
			return fname;
		} else {
			return "";
		}
	}

	/**
	 *	주어진 파일명을 이용, 확장자가 .gif, .jpg, .png, .bmp 인 경우 true 리턴
	 *
	 */
	public static boolean isImageFile(String fileName)
	{
		String ext = getExt(fileName);

		if(ext.equals(".gif") || ext.equals(".jpg") || ext.equals(".png") || ext.equals(".bmp"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 데이타를 구분자로 나누어 배열로 리턴
	 *
	 *
	 */
	public static String[] getSplit(String str, String delimiter){
		int	cnt	= 0;
		String[] arrayData = null;

		if (str == null){
			return null;
		}

		if ( str != null && !str.equals("")){
			StringTokenizer	token =	new	StringTokenizer(str,delimiter);
			arrayData = new	String[token.countTokens()];
			while(token.hasMoreTokens())
			{
				arrayData[cnt++] = token.nextToken();
			}
		}
		return arrayData ;
	}



	/**
	 *	파라미터 스트링이 null 이 아니고, "" 이 아니면 true, 아니면 false
	 *
	 *	@param param		검사 문자열
	 *
	 *	@return 검사결과
	 */
	public static boolean isNotNull(String param){
		if(param != null && "".equals(param) == false) return true;
		else return false;
	}


	/**
	 *	파라미터 스트링이 null or "" 이면 true, 아니면 false
	 *
	 *	@param param		검사 문자열
	 *
	 *	@return 검사결과
	 */
	public static boolean isNull(String param){
		if(param == null || "".equals(param) ) return true;
		else return false;
	}

	/**
	 *	파라미터 String 배열이 null or length = 0 or isNull(elementAt(i))
	 *
	 *	@param param		검사 문자열 배열
	 *
	 *	@return 검사결과
	 */
	public static boolean isNull(String[] param){
		if(param == null || param.length == 0) return true;
		for(int i=0; i < param.length; i++){
			if(isNotNull(param[i])) return false;
		}
		return true;
	}


	public static boolean isNotNull(String[] param){
		return !isNull(param);
	}

	public static boolean isNull(Object[] param){
		if(param == null || param.length == 0) return true;
		for(int i=0; i < param.length; i ++){
			if(param[i] != null) return false;
		}

		return true;
	}

	public static boolean isNotNull(Object[] param){
		return !isNull(param);
	}

        public static String checkNullStr(String str){
          if(isNull(str)){
              str = "";
          }
          return str;
        }
        
	/*
		고정길이 len = 3
		현재 값  str = 1 일 경우 001을 반환해 준다.
	 */
	public static String zeroPutStr(int len, String str){
	  for(int i = 0; i < len; i++)
		if(i >= str.length()) str = "0" + str;

	  return str;
	}


	/**
	 *	자바스크립트의 alert('') 함수에서 출력할 문자열에 포함되어 있는 "\n", """ 문자 제거
	 *
	 */
	public static String getAlertMsg(String src){
		src = ReplaceAll(src, "\n", "\\n");
		src = ReplaceAll(src, "\r", "");
		src = ReplaceAll(src, "\"", "'");
		
		return src;
	}

	/**
	 *	스트링 치환 함수
	 *	
	 *	주어진 문자열(buffer)에서 특정문자열('src')를 찾아 특정문자열('dst')로 치환
	 *
	 */
	public static String ReplaceAll(String buffer, String src, String dst){
		if(buffer == null) return null;
		if(buffer.indexOf(src) < 0) return buffer;
		
		int bufLen = buffer.length();
		int srcLen = src.length();
		StringBuffer result = new StringBuffer();

		int i = 0; 
		int j = 0;
		for(; i < bufLen; ){
			j = buffer.indexOf(src, j);
			if(j >= 0) {
				result.append(buffer.substring(i, j));
				result.append(dst);
				
				j += srcLen;
				i = j;
			}else break;
		}
		result.append(buffer.substring(i));
		return result.toString();
	}
	/**
	 String의 & 비트 연산을 한다.
	 00000001 & 00001001 => 00000001로 출력이 된다.
	 **/
	public static String calcStrBit(String root, String comp){
	  String retVal = "";
	  char first = '0';
      
	  for(int i =0;i < 8;i++){
		 if((first = root.charAt(i)) == '1'){
			if(first == comp.charAt(i)) retVal += "1";
			else retVal += "0";          
		 }else{
			retVal += "0";
		 }     
	  }
      
	  return retVal;	
	}	
	
	@SuppressWarnings("deprecation")
	public static String encode(String str){
		if((str != null) && (str.equals(""))) str = ""; 
		else str = URLEncoder.encode(str);
		
		return str;
	}
	
	@SuppressWarnings("deprecation")
	public static String decode(String str){
		if((str != null) && (str.equals(""))) str = ""; 
		else str = URLDecoder.decode(str);
		
		return str;
	}
	
	public static String Uni2Ksc(String uniStr)
	{
		String kscStr = uniStr;
		try
		{
			kscStr = new String(uniStr.getBytes("euc-kr"), "utf-8"); 	
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return kscStr;
	}

	public static String Ksc2Uni(String kscStr)
	{
		String uniStr = kscStr;
		try
		{
			uniStr = new String(kscStr.getBytes("KSC5601"), "8859_1"); 	
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return uniStr;
	}
	
	public static String stringToClob(Clob clob) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
	       br = new BufferedReader(clob.getCharacterStream());
	       char [] buf    = new char[1024];
	       int length = 0;	  
	       while ((length = br.read(buf, 0, 1024)) != -1 ) {
	    	   sb.append(buf, 0, length);
	       }
	       if (br != null) br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString(); 
	}

	public static String stringSlice(String s, int l) {
		StringBuffer sb = new StringBuffer();
		sb.append("");
		if (s != null && !s.equals("")) {
			int le = s.length();
			if (le > l) {
				sb.append(s.substring(0, l)).append("..");
			} else {
				sb.append(s);
			}
		}
		return sb.toString();
	}

	
	
	/**
	 * 파라미터 문자열이 null 이면  문자열 "" 으로, 문자열을 문자형으로 변환 
	 * @param str
	 * @return
	 */
    public static String checkNull(String str) {
        String strTmp;
        if (str == null)
            strTmp = "";
        else
            strTmp = str;
        return strTmp;
    }
    
    
    /**
     * 파라미터 문자열이 null이면 숫자형 0으로, 문자열을 숫자형으로 변환 
     * @param str
     * @return
     */
    public static int checkNumberNull(String str) {
        int strTmp;
        if (str == null || str.equals(""))
            strTmp = 0;
        else
            strTmp = Integer.parseInt(str);
        return strTmp;
    }
    
    
    /**
     * 파라미터 문자열이 null이면 문자형 0으로, 문자열을 문자형으로 변환 
     * @param str
     * @return
     */
    public static String checkNull2(String str) {
        String strTmp;
        if (str == null)
            strTmp = "0";
        else
            strTmp = str;
        return strTmp;
    }
    
    
    /**
     * 파라미터 문자열이 null이면 문자형 0으로, 문자열을 문자형으로 변환 
     * @param str
     * @return
     */
    public static Double checkNull3(Double str) {
    	Double strTmp;
        if (str == null)
            strTmp = 0.0;
        else
            strTmp = str;
        return strTmp;
    }
    
    
    /**
     * 파라미터 문자열이 null이면 숫자형 0.0으로, 문자열을 더블형으로 변환 
     * @param str
     * @return
     */
    public static double checkNullDouble(String str) {
        double strTmp;
        if (str == null)
            strTmp = 0.0;
        else
            strTmp = Double.parseDouble(str);
        return strTmp;
    }
    
    
	
	/***************************************************************************************
     * 분자열의 특정 문자를 다른 문자로 변경
     * @param        String          Original String
     *               String          Source String
     *               String          Target String
     * @return       String          Converted String
     ****************************************************************************************/
    public static String Replace( String oldString, String from, String to )
    {
        String newString = oldString;
        int offset = 0;
        while ((offset = newString.indexOf( from, offset )) > -1)
        {
            StringBuffer temp = new StringBuffer( newString.substring( 0, offset ) );
            temp.append( to );
            temp.append( newString.substring( offset+from.length() ) );
            newString = temp.toString();
            offset++;
        }
        return newString;

    } // End of Replace
    
    
    /**
	 * 대상문자열(strTarget)에서 특정문자열(strSearch)을 찾아 지정문자열(strReplace)로
	 * 변경한 문자열을 반환한다.
	 *
	 * @param strTarget 대상문자열
	 * @param strSearch 변경대상의 특정문자열
	 * @param strReplace 변경 시키는 지정문자열
	 * @return 변경완료된 문자열
	 */
	public static String replaceString(String content, String delimToken, String replaceToken)
	{
		if(content == null) {
			return null;
		}
		if((delimToken == null) || (delimToken.equals(""))) {
			return content;
		}
		if(replaceToken == null) {
			return content;
		}

		String contentTemp = content;
		String contentTemp2 = "";

		try {
			int i = contentTemp.indexOf(delimToken, 0);
			int lengthcontentTemp = contentTemp.length();
			int lengthDelimToken = delimToken.length();
			while (i>=0) {
				contentTemp2 = contentTemp2 + contentTemp.substring(0, i) + replaceToken;
				contentTemp = contentTemp.substring(i+lengthDelimToken, lengthcontentTemp);
				lengthcontentTemp = contentTemp.length();
				i = contentTemp.indexOf(delimToken);
			}
			contentTemp2 = contentTemp2 + contentTemp;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return contentTemp2;
	}
	
	

	public static String stringEndDot(String s, int l) {
		StringBuffer sb = new StringBuffer();
		sb.append("");
		if (s != null && !s.equals("")) {
			int le = s.length();
			if (le > l) {
				sb.append(s.substring(0, l)).append("..");
			} else {
				sb.append(s);
			}
		}
		return sb.toString();
	}
	
	
	
	
}
