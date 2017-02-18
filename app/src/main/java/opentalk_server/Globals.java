package opentalk_server;


import java.text.SimpleDateFormat;
import java.util.Date;

public class Globals
{
    public static final String SERVER_VERSION = "3";

    public static final String LOGTAG = "aarontest";
    public static final String SERVERIP = "www.aaronlife.com";
    public static final int SERVERPORT = 9999;

    public static void log(String dir, String uid, String msg)
    {
        String dt = new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date());
        System.out.println(dt + " SERVER" + dir + "[" + uid + "]: " + msg);
    }

    public static void err(String msg)
    {
        String dt = new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date());
        System.out.println(dt + " *** " + msg + " ***");
    }

    public static String bytesToHexString(byte[] src)
    {
        StringBuilder stringBuilder = new StringBuilder("");

        if (src == null || src.length <= 0)
        {
            return null;
        }

        for (int i = 0; i < src.length; i++)
        {
            int tmp = src[i] & 0xFF;
            String hv = Integer.toHexString(tmp);
            if (hv.length() < 2)
            {
                stringBuilder.append(0);
            }

            //stringBuilder.append(hv + " ");
            stringBuilder.append(hv);
        }

        return stringBuilder.toString().toUpperCase();
    }

    public static byte[] hexStringToBytes(String hexString)
    {
        char[] hex = hexString.toCharArray();
        int length = hex.length / 2;
        byte[] rawData = new byte[length];

        for(int i = 0; i < length; i++)
        {
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            int value = (high << 4) | low;

            if (value > 127) value -= 256;

            rawData[i] = (byte)value;
        }

        return rawData ;
    }

    private static String byte2hex(byte[] b)
    {
        String hs="";
        String stmp="";
        for (int n=0;n<b.length;n++){
            stmp=(java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length()==1) hs=hs+"0"+stmp;
            else hs=hs+stmp;
        }
        return hs.toUpperCase();
    }
}
