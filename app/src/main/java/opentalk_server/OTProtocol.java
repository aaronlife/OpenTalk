package opentalk_server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OTProtocol
{
    public static class Command
    {
        public String name;
        public String data1;
        public String data2;
        public String datetime;
    }

    public static final int READ_TIMEOUT = 15000;
    public static final int HEART_BEAT = 5000;

    public static final String CONNECTOR = ";:";
    public static final String NONE = "NONE";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String OK = "OK";

    public static final String KEEP_ALIVE = "keep_alive";

    public static final String C2S_LOGIN = "login";
    public static final String C2S_REGISTER = "register";
    public static final String C2S_OFFLINE_NOTI = "offline_noti";
    public static final String C2S_MESSAGE = "message";
    public static final String C2S_STICKER = "sticker";
    public static final String C2S_FIND = "find";
    public static final String C2S_INVITATION = "invitation";
    public static final String C2S_ACCEPT_INVITATION = "accept_invitation";

    public static final String S2C_REGISTER = "register";
    public static final String S2C_WELCOME = "welcome";
    public static final String S2C_FIND = "find";
    public static final String S2C_INVITATION = "invitation";
    public static final String S2C_ADD_FRIEND = "add_friend";
    public static final String S2C_MESSAGE = "message";
    public static final String S2C_STICKER = "sticker";

    public static String makeCmd(String name, String data1, String data2,
                                 String datetime, Security.AESKeySet aesKeySet)
    {
        // 1. 組合資料
        String data = name + CONNECTOR + datetime +
                CONNECTOR + data1 + CONNECTOR + data2;

        // 2. 加密資料
        String enData = Security.encryptAES(aesKeySet, data);

        return enData;
    }

    public static Command parseCmd(String data, Security.AESKeySet aesKeySet)
    {
        Command command = new Command();

        // 1. 解密資料
        String deData = Security.decryptAES(aesKeySet, data);

        // 2. 分析資料
        String[] cmdArray = deData.split(OTProtocol.CONNECTOR);

        if(cmdArray.length != 4) return null;

        // 3. 建立Commnad物件存放分析後資料
        command.name = cmdArray[0];
        command.datetime = cmdArray[1];
        command.data1 = cmdArray[2];
        command.data2 = cmdArray[3];

        return command;
    }

    public static String nowStr()
    {
        return new SimpleDateFormat("M/d H:mm").format(new Date());
    }
}