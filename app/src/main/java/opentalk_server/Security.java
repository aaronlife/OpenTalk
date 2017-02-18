package opentalk_server;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Security
{
    //
    // HASH加密
    //
    public final static String HASHTYPE_MD2 = "MD2";
    public final static String HASHTYPE_MD5 = "MD5";
    public final static String HASHTYPE_SHA1 = "SHA1";
    public final static String HASHTYPE_SHA256 = "SHA-256";
    public final static String HASHTYPE_SHA384 = "SHA-384";
    public final static String HASHTYPE_SHA512 = "SHA-512";

    public static String toHash(String s)
    {
        MessageDigest sha = null;

        try
        {
            sha = MessageDigest.getInstance(HASHTYPE_SHA256);
            sha.update(s.getBytes());
        }
        catch(Exception e)
        {
            Globals.err("toHash err: " + e.getMessage());
            Globals.err("toHash err: " + e.toString());

            return "";
        }

        return Globals.bytesToHexString(sha.digest());
    }

    //
    // AES加解密
    //
    public static class AESKeySet
    {
        public SecretKey secretKey;
        public byte[] iv = new byte[16];
    }

    public static AESKeySet generateAESKeySet()
    {
        AESKeySet aesKeySet = new AESKeySet();

        KeyGenerator keyGen = null;

        try
        {
            keyGen = KeyGenerator.getInstance("AES");
        }
        catch(NoSuchAlgorithmException e)
        {
            Globals.err("generateAESKeySet err: " + e.getMessage());
            Globals.err("generateAESKeySet err: " + e.toString());

            return null;
        }

        // 產生256位元的Key
        keyGen.init(256, new SecureRandom());
        aesKeySet.secretKey = keyGen.generateKey();

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(aesKeySet.iv);

        Globals.log("::", "--------",
                    "Key=" + Globals.bytesToHexString(aesKeySet.iv));
        
        return aesKeySet;
    }

    public static String encryptAES(AESKeySet aesKeySet, String msg)
    {
        Cipher cipher = null;
        byte[] byteCipherText = null;

        Globals.log("::", "--------", "加密前：" + msg);

        try
        {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, aesKeySet.secretKey,
                                             new IvParameterSpec(aesKeySet.iv));
            byteCipherText = cipher.doFinal(msg.getBytes("UTF-8"));
        }
        catch(NoSuchAlgorithmException | NoSuchPaddingException |
              InvalidKeyException | InvalidAlgorithmParameterException |
              IllegalBlockSizeException | BadPaddingException |
              UnsupportedEncodingException e)
        {
            Globals.err("encryptAES err: " + e.getMessage());
            Globals.err("encryptAES err: " + e.toString());
        }

        Globals.log("::", "--------", "加密後：" +
                                    Globals.bytesToHexString(byteCipherText));

        return Globals.bytesToHexString(byteCipherText);
    }

    public static String decryptAES(AESKeySet aesKeySet, String data)
    {
        if(aesKeySet == null || data == null)
        {
            Globals.err("解密結果錯誤。");
            return null;
        }

        Globals.log("::", "--------", "解密前：" + data);

        String strDecryptedText = null;
        byte[] dataBytes = Globals.hexStringToBytes(data);

        try
        {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, aesKeySet.secretKey,
                        new IvParameterSpec(aesKeySet.iv));

            byte[] decryptedText = cipher.doFinal(dataBytes);
            strDecryptedText = new String(decryptedText);
        }
        catch(NoSuchAlgorithmException | NoSuchPaddingException |
              InvalidKeyException | InvalidAlgorithmParameterException |
              IllegalBlockSizeException | BadPaddingException e)
        {
            Globals.err("decryptAES err: " + e.getMessage());
            Globals.err("decryptAES err: " + e.toString());
        }

        Globals.log("::", "--------", "解密後：" + strDecryptedText);

        return strDecryptedText;
    }

    //
    // RSA加解密
    //
    static class RSAKeyPair
    {
        PublicKey publicKey;
        PrivateKey privateKey;
    }

    public static RSAKeyPair generateRSAKeyPair()
    {
        RSAKeyPair rsaKeyPair = new RSAKeyPair();
        KeyPairGenerator keygen = null;

        try
        {
            keygen = KeyPairGenerator.getInstance("RSA");
        }
        catch(NoSuchAlgorithmException e)
        {
            Globals.err("generateRSAKeyPair err: " + e.getMessage());
            Globals.err("generateRSAKeyPair err: " + e.toString());
        }

        SecureRandom random = null;

        try
        {
            random = SecureRandom.getInstance("SHA1PRNG");
        }
        catch(NoSuchAlgorithmException e)
        {
            Globals.err("generateRSAKeyPair err: " + e.getMessage());
            Globals.err("generateRSAKeyPair err: " + e.toString());
        }

        keygen.initialize(2048, random);
        KeyPair keyPair = keygen.generateKeyPair();

        rsaKeyPair.publicKey = keyPair.getPublic();
        rsaKeyPair.privateKey = keyPair.getPrivate();

        Globals.log("::", "--------", "Public Key: " +
                Globals.bytesToHexString(rsaKeyPair.publicKey.getEncoded()));

        Globals.log("::", "--------", "Private Key: " +
                Globals.bytesToHexString(rsaKeyPair.privateKey.getEncoded()));

        return rsaKeyPair;
    }

    public static String encryptRSA(PublicKey publicKey, String msg)
    {
        Globals.log("::", "--------", "加密前：" + msg);

        byte[] resultBytes = null;

        try
        {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            resultBytes = cipher.doFinal(msg.getBytes("UTF-8"));
        }
        catch (IllegalBlockSizeException | BadPaddingException |
               InvalidKeyException | NoSuchAlgorithmException |
               NoSuchPaddingException | UnsupportedEncodingException e)
        {
            Globals.err("encryptRSA err: " + e.getMessage());
            Globals.err("encryptRSA err: " + e.toString());

            return null;
        }

        Globals.log("::", "--------", "加密後：" +
                                      Globals.bytesToHexString(resultBytes));

        return Globals.bytesToHexString(resultBytes);
    }

    public static String decryptRSA(PrivateKey privateKey, String data)
    {
        Globals.log("::", "--------", "解密前：" + data);

        byte[] resultBytes = null;

        try
        {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            resultBytes = cipher.doFinal(Globals.hexStringToBytes(data));
        }
        catch (IllegalBlockSizeException | BadPaddingException |
               InvalidKeyException | NoSuchAlgorithmException |
               NoSuchPaddingException e)
        {
            Globals.err("decryptRSA: " + e.getMessage());
            Globals.err("decryptRSA: " + e.toString());

            return null;
        }

        Globals.log("::", "--------", "解密後：" + new String(resultBytes));

        return new String(resultBytes);
    }
}
