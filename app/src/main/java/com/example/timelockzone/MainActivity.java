package com.example.timelockzone;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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

import java.io.IOException;
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
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;


public class MainActivity extends AppCompatActivity {
    private static String txtDate = "01092023";
    private static final String scheme = "secp256k1";


    public void select_date(View view) {

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    public void encrypt(View view) {

        String plainText;
        try {

            KeyFactory kf = KeyFactory.getInstance("ECDH");

            Cipher iesCipher = Cipher.getInstance("ECIES");

            Date date;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                date = sdf.parse(txtDate);

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

            ((TextView) findViewById(R.id.output)).setText(txtDate + Base64.getEncoder().encodeToString(cipherText));


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
            if (s.length() < 9) return;
            byte[] cipherText2;
            try {
                cipherText2 = Base64.getDecoder().decode(s.substring(8));
            } catch (Exception e) {
                ShowAlert(getString(R.string.e2), getString(R.string.e9), getString(R.string.ok));
                return;
            }
            byte[] sk;
            Date strDate = new Date();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                strDate = sdf.parse(s.substring(0, 8));
                if (new Date().before(strDate)) {
                    ShowAlert(getString(R.string.e6), getString(R.string.e7) + s.substring(0, 2) + "/" + s.substring(2, 4) + "/" + s.substring(4, 8) + " (DD/MM/YYYY) " + getString(R.string.e8), getString(R.string.back));
                    return;
                }


            } catch (ParseException e) {
                ShowAlert(getString(R.string.e4), getString(R.string.e5), getString(R.string.ok));
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
            tv.setText(toString(plainText2));


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

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {

                    String daystr, monthstr;
                    if (dayOfMonth < 10) daystr = "0" + dayOfMonth;
                    else daystr = "" + dayOfMonth;
                    if (monthOfYear < 10) monthstr = "0" + (monthOfYear + 1);
                    else monthstr = "" + (monthOfYear + 1);

                    txtDate = daystr + monthstr + year;

                }
            }, year, month, day);
            datePicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            return datePicker;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
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
        Timelock.Setup();
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String daystr;
        String monthstr;
        if (day<10) daystr="0"+day;
        else daystr=day+"";
        if (month<10) monthstr="0"+month;
        else monthstr=month+"";

        txtDate=daystr+monthstr+year;
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();

      //  String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        if (appLinkData != null) {
            String s = appLinkData.toString();
            if (s.length() > getString(R.string.AppLink).length()) {
                String ct = appLinkData.toString().substring(getString(R.string.AppLink).length() );

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

    public void share(View v) {

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        // type of the content to be shared
        sharingIntent.setType("text/plain");
        String s = ((TextView) findViewById(R.id.output)).getText().toString();
        try {
            String day = s.substring(0, 2);
            String month = s.substring(2, 4);
            String year = s.substring(4, 8);
            String ct = s;
            // Body of the content
            String shareBody = getString(R.string.b1) + day + "/" + month + "/" + year + " (DD/MM/YYYY).\n" + getString(R.string.b2) + getString(R.string.AppLink) + ct;
            // subject of the content. you can share anything
            String shareSubject = getString(R.string.b4);

            // passing body of the content
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

            // passing subject of the content
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.e11)));
        } catch (Exception e) {
            ShowAlert(getString(R.string.e2), getString(R.string.e1), getString(R.string.back));

        }
    }

}