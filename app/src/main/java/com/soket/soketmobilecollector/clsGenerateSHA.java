package com.soket.soketmobilecollector;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static java.security.MessageDigest.getInstance;

public class clsGenerateSHA {

    public clsGenerateSHA(){
    }

    public static String hex256(String input, boolean InUppercase  )
    {
        String hexStr = "";
        try {
            MessageDigest md = getInstance("SHA256");
            md.reset();
            byte[] buffer = input.getBytes(StandardCharsets.UTF_8);
            md.update(buffer);
            byte[] digest = md.digest();

            for (byte b : digest) {
                hexStr = hexStr.concat( Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

        } catch (Exception e) {
            hexStr="";
        }
        if (InUppercase) {
            return hexStr.toUpperCase();
        } else {
            return hexStr;
        }
    }
}
