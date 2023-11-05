package com.example.timelockzone;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import org.zone.timelock.*;

public class MainActivity extends AppCompatActivity {

    private static final String Version="v10000001/";
    private static final int length_date=10; // ddMMyyyyhh

    private static final int length_hour=2; // hh

    private static String txtDate = "01092023";
    private static String txtHour = "00";
    private static final String scheme = "secp256k1";
    private static final String tinyUrl = "https://tinyurl.com/api-create.php?url=";
    private static TimePickerDialog timePickerDialog;


    private String CreateTinyUrl(String url) throws IOException {
        String tinyUrlLookup = tinyUrl + url;
        InputStream IS= new URL(tinyUrlLookup).openStream();
        InputStreamReader ISR=new InputStreamReader(IS);
        BufferedReader reader = new BufferedReader(ISR);
        return reader.readLine();

    }

    public void select_date(View view) {

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void select_hour(View view) {

        timePickerDialog.show();
    }

    public void encrypt(View view) {

        String plainText;
        try {

            KeyFactory kf = KeyFactory.getInstance("ECDH"); // by default secp256k1 will be selected by bouncycastle

            Cipher iesCipher = Cipher.getInstance("ECIES"); // you can replace this with more secure instantiations of ECIES like "ECIESwithSHA256" etc. Se also Victor Shoup's paper on ECIES.


            Date date;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHH");
                date = sdf.parse(txtDate+txtHour);

            } catch (Exception e) {
                ShowAlert(getString(R.string.e4), getString(R.string.e5), getString(R.string.ok));
                return;
            }


            long Round = Timelock.DateToRound(date);

            byte[] pk;
            try {
                pk = Timelock.getPublicKeyFromRound(Round, scheme);
            } catch(Exception e) {
                ShowAlert(getString(R.string.e2),getString(R.string.e12),getString(R.string.back));
                return;
            }
// retrieve PK based on the round Round

            PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(pk));


            iesCipher.init(Cipher.ENCRYPT_MODE, pub);


            plainText = ((EditText) findViewById(R.id.input)).getText().toString();
            byte[] cipherText = new byte[iesCipher.getOutputSize(plainText.getBytes().length)];


            int ctlength = iesCipher.update(plainText.getBytes(), 0, plainText.getBytes().length, cipherText, 0);
            iesCipher.doFinal(cipherText, ctlength);
            System.out.println(Base64.getEncoder().encodeToString(cipherText));
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHH");
            sdf.setTimeZone(TimeZone.getTimeZone(Timelock.timezone));
            final String t=sdf.format(date); // format s in timelock.zone timezone
            ((TextView) findViewById(R.id.output)).setText(t+ Base64.getEncoder().encodeToString(cipherText));


        } catch (InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException |
                 ShortBufferException | NoSuchPaddingException | BadPaddingException |
                 IllegalBlockSizeException e) {
            e.printStackTrace();
            Log.e("timelock.zone", "exception", e);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage(getString(R.string.e10));

            builder.setTitle(getString(R.string.e2));

            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.ok),  (dialog, which) -> {

            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }


    }


    private void ShowAlert(String title, String msg, String btn) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage(msg);

        builder.setTitle(title);

        builder.setCancelable(false);
        builder.setPositiveButton(btn, (dialog, which) -> {

        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    public void decrypt(View view) {

        try {

            KeyFactory kf = KeyFactory.getInstance("ECDH");
            String s = (((TextView) findViewById(R.id.input)).getText()).toString();
            if (s.length() <=  length_date) {
                ShowAlert(getString(R.string.e2), getString(R.string.e9), getString(R.string.ok));
                return;
            }
            byte[] cipherText2;
            try {
                cipherText2 = Base64.getDecoder().decode(s.substring(length_date));
            } catch (Exception e) {
                ShowAlert(getString(R.string.e2), getString(R.string.e9), getString(R.string.ok));
                return;
            }
            byte[] sk;
            Date strDate = new Date();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHH");
                sdf.setTimeZone(TimeZone.getTimeZone(Timelock.timezone));
                sdf.setLenient(false);
                ParsePosition p = new ParsePosition( 0 );
                strDate =  sdf.parse(s.substring(0,length_date),p);
                if(p.getIndex() < length_date) {
                    throw new ParseException( "ddMMyyyyHH", p.getIndex() );
                }
                if (new Date().before(strDate)) {
//                    ShowAlert(getString(R.string.e6), getString(R.string.e7) + s.substring(0, 2) + "/" + s.substring(2, 4) + "/" + s.substring(4, 8) + " (DD/MM/YYYY), " + HourParsing(s.substring(8,10))+" "+ getString(R.string.e8), getString(R.string.back));
                    ShowAlert(getString(R.string.e6), getString(R.string.e7) + strDate.toLocaleString()+" "+ getString(R.string.e8), getString(R.string.back));
                    return;
                }


            } catch (ParseException e) {
                ShowAlert(getString(R.string.e4), getString(R.string.e5), getString(R.string.ok));
                return;
            }
            long Round = Timelock.DateToRound(strDate);
            // retrieve SK from round R
            try {
                sk = Timelock.getSecretKeyFromRound(Round, scheme);
            } catch(Exception e) {
                ShowAlert(getString(R.string.e2),getString(R.string.e12),getString(R.string.back));
                return;
            }


            PrivateKey Sk = kf.generatePrivate(new PKCS8EncodedKeySpec(sk));

            Cipher iesCipher2 = Cipher.getInstance("ECIES");
            iesCipher2.init(Cipher.DECRYPT_MODE, Sk);
            byte[] plainText2 = new byte[iesCipher2.getOutputSize(cipherText2.length)];
            int ctlength2 = iesCipher2.update(cipherText2, 0, cipherText2.length - 1, plainText2);
            ctlength2 += iesCipher2.doFinal(plainText2, ctlength2);
            System.out.println("decrypted plaintext: " + ctlength2 + " " + cipherText2.length + " " + toString(plainText2));
            TextView tv = findViewById(R.id.output);

            tv.setText(new String( plainText2, StandardCharsets.UTF_8));


        } catch (InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException |
                 ShortBufferException | NoSuchPaddingException | BadPaddingException |
                 IllegalBlockSizeException e) {
            e.printStackTrace();
            Log.e("timelock.zone", "exception", e);
            ShowAlert(getString(R.string.e2), getString(R.string.e3), getString(R.string.ok));

        }


    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {

                String daystr, monthstr;
                if (dayOfMonth < 10) daystr = "0" + dayOfMonth;
                else daystr = "" + dayOfMonth;
                if (monthOfYear < 9) monthstr = "0" + (monthOfYear + 1);
                else monthstr = "" + (monthOfYear + 1);

                txtDate = daystr + monthstr + year1;

            }, year, month, day);
            datePicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());

            return datePicker;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // TODO: when the selected day is the current one set as minimum hour the current hour.
        }
    }


    private static String toString(
            byte[] bytes,
            int length) {
        char[] chars = new char[length];

        for (int i = 0; i != chars.length; i++) {
            chars[i] = (char) (bytes[i] & 0xff);
        }

        return new String(chars);
    }

    private static String toString(
            byte[] bytes) {
        return toString(bytes, bytes.length);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Timelock.Setup();
        final Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH)+1;
        final int day = c.get(Calendar.DAY_OF_MONTH);
        String daystr;
        String monthstr;
        if (day<10) daystr="0"+day;
        else daystr=day+"";
        if (month<10) monthstr="0"+month;
        else monthstr=month+"";
        final int hour = c.get(Calendar.HOUR_OF_DAY);

        timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        if (hourOfDay<10) txtHour="0"+hourOfDay;
                        else txtHour=""+hourOfDay;
                        if (minute!=0) ShowAlert(getString(R.string.e13), getString(R.string.e14)+" "+HourParsing(""+hourOfDay), getString(R.string.ok));

                    }
                }, hour, 0, true);



        txtDate=daystr+monthstr+year;
        Intent appLinkIntent = getIntent();

        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) {
            String s = appLinkData.toString();
            if (s.length() > getString(R.string.AppLink).length()) {
                String ct = appLinkData.toString().substring(getString(R.string.AppLink).length()+Version.length() );

                ((EditText) findViewById(R.id.input)).setText(ct);
                decrypt(getWindow().getDecorView().getRootView());
            }
        }

    }

    public void finish(View v) {

        //moveTaskToBack(true);
        MainActivity.this.finish();
        System.exit(0);
    }

    public void Instructions(View v) {
//    ShowAlert("Instrutions","How to encrypt.\n1. Choose a date in the future.\n2. Click on Encrypt.\nThe ciphertext will be displayed. Click on  it and the ciphertext will be copied to your clipboard.\n\nHow to decrypt.\n1. Paste the ciphertext in the textbox.\n2. Click on decrypt.\nThe plaintext will be displayed or you will get an error if the time of decryption is not reached yet.\n\nSharing.\nClick on the sharing icon to share the ciphertext along with instructions to social media.\n\n©Timelock.zone, 2023.\nPowered by drand.love.",
        //          "Back");
        ShowAlert(getString(R.string.instructions_tile), getString(R.string.instructions), getString(R.string.back));
    }

    public void copyToClipboard(View v) {
        ShowAlert("", getString(R.string.ctc), getString(R.string.back));
        ClipboardManager clipboardManager = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", ((TextView) findViewById(R.id.output)).getText().toString()));
    }

    private boolean first_usage = true;

    public void textEmpty(View v) {
        if (first_usage)
            ((TextView) findViewById(R.id.input)).setText("");
        first_usage = false;
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        // first parameter is the file for icon and second one is menu
        return super.onCreateOptionsMenu(menu);
    }

    private static String HourParsing(String hour) {
        if (Integer.valueOf(hour)<= 12) return hour+":00 AM, "+Calendar.getInstance().getTimeZone().getDisplayName();
        else return (Integer.valueOf(hour)-12)+":00 PM, "+Calendar.getInstance().getTimeZone().getDisplayName();
    }


    public void share(View v) {

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        // type of the content to be shared
        sharingIntent.setType("text/plain");
        String s = ((TextView) findViewById(R.id.output)).getText().toString();
        try { // we first try with tinyurl, if it fails we try with long url. TODO: less duplicated code
            String day = s.substring(0, 2);
            String month = s.substring(2, 4);
            String year = s.substring(4, 8);
            String hour = s.substring(8, 10);
            // recall: s is the ciphertext;
            // Body of the content

            String shareBody = getString(R.string.b1) + day + "/" + month + "/" + year + " (DD/MM/YYYY), "+HourParsing(hour)+".\n" + getString(R.string.b2) + CreateTinyUrl(getString(R.string.AppLink) + Version+ s);
            // subject of the content. you can share anything
            String shareSubject = getString(R.string.b4);

            // passing body of the content
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

            // passing subject of the content
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.e11)));
        } catch (Exception e) {
            try{
                String day = s.substring(0, 2);
                String month = s.substring(2, 4);
                String year = s.substring(4, 8);
                String hour = s.substring(8, 10);
                // recall: s is the ciphertext
                // Body of the content

                String shareBody = getString(R.string.b1) + day + "/" + month + "/" + year + " (DD/MM/YYYY), "+HourParsing(hour)+".\n" + getString(R.string.b2) + getString(R.string.AppLink) + Version + s;
                // subject of the content. you can share anything
                String shareSubject = getString(R.string.b4);

                // passing body of the content
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                // passing subject of the content
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.e11)));


            } catch (Exception e2) {
                ShowAlert(getString(R.string.e2), getString(R.string.e1), getString(R.string.back));
            }
        }
    }

}