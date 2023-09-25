package com.example.timelockzone;

import android.app.AlertDialog;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class Timelock {

    public static void Setup()
    {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }
    private static String getUrlContents(String theUrl) throws IOException
    {
        StringBuilder content = new StringBuilder();
        try
        {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
        throw new IOException();
        }
        return content.toString();
    }


    private static long DRAND_GENESIS_TIME=1677685200;
    private static int DRAND_FREQUENCY=3;
    private static byte[] stripPEM(String pem) throws IOException {

        Pattern parse = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
        String encoded = parse.matcher(pem).replaceFirst("$1");
        return Base64.getMimeDecoder().decode(encoded);
    }


    public static long DateToRound(Date date){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        date = cal.getTime();
        long t= ((date.getTime())/1000);
        return (t-Timelock.DRAND_GENESIS_TIME)/Timelock.DRAND_FREQUENCY;
    }
    public static byte[] getPublicKeyFromRound(long Round, String Scheme) throws IOException {

        // TODO: retrieve pk for given round Round and scheme Scheme
        // for the moment the pk is embedded
     /*
        try{
        getUrlContents("https://api.timelock.zone/chain1/"+ Scheme+"/publickey/pem/" + Round);
} catch(IOException e) {
throw  new IOException();
}
*/

        String pkpem="-----BEGIN PUBLIC KEY-----\n" +
                "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEOErqrRCc3yBTCdQNfUQNM85JJHXOqYSH\n" +
                "ibnuF1AtHTgc1iOxS/OlGyVctEF+wJMLrvc/nrd2GhRYcqtsJu9Gfw==\n" +
                "-----END PUBLIC KEY-----\n";
        return stripPEM(pkpem);

    }
    public static byte[] getSecretKeyFromRound(long Round, String Scheme) throws IOException {

        // TODO: retrieve sk for given round Round and given scheme Scheme
        // for the moment the sk is embedded
    /*
        try {
         getUrlContents("https://api.timelock.zone/chain1/" + Scheme + "/secretkey/pkcs8/" + Round);
        } catch(IOException e){
            throw new IOException();
        }

     */
        String skpem="-----BEGIN PRIVATE KEY-----\n" +
                "MIGEAgEAMBAGByqGSM49AgEGBSuBBAAKBG0wawIBAQQg+uEhcA+bG/44RS/COUJa\n" +
                "bjwVrYcMKN8zby1LowdBvnihRANCAAQ4SuqtEJzfIFMJ1A19RA0zzkkkdc6phIeJ\n" +
                "ue4XUC0dOBzWI7FL86UbJVy0QX7Akwuu9z+et3YaFFhyq2wm70Z/\n" +
                "-----END PRIVATE KEY-----\n";
        return stripPEM(skpem);

    }
}
