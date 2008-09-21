package com.ibm.semplore.util;
import java.util.StringTokenizer;


public class TestUnicode {

    public static String parse(String input)
    {
        StringTokenizer st = new StringTokenizer(input, "\\", true);

        StringBuffer sb = new StringBuffer();

        while(st.hasMoreTokens())
        {
            String token = st.nextToken();
            if (token.charAt(0) == '\\' && token.length() == 1)
            {
                if(st.hasMoreTokens())
                {
                    token = st.nextToken();
                }
                if(token.charAt(0) == 'u')
                {
                    String hexnum;
                    if (token.length() > 5)
                    {
                        hexnum = token.substring(1,5);
                        token = token.substring(5);
                    }
                    else
                    {
                        hexnum = token.substring(1);
                        token = "";
                    }
                    sb.append((char)Integer.parseInt(hexnum, 16));
                }
            }
            sb.append(token);
        }
        return sb.toString();
    }

	static String ustr = "\\u0442\\u0440\\u0438 \\u0442\\u043E\\u0447\\u043D\\u044B\\u0445 \\u0443\\u0434\\u0430\\u0440\\u0430";

	public static void main(String[] args) {
		System.out.println(parse(ustr));

	}

}