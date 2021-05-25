package com.myapplicationdev.android.demoshowsms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Views
    private TextView smsTextView;
    private Button retrieveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsTextView = findViewById(R.id.sms_text_view);
        retrieveButton = findViewById(R.id.retrieve_button);
        retrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(
                        MainActivity.this, Manifest.permission.READ_SMS);

                // Check whether permission is not granted
                if (permissionCheck != PermissionChecker.PERMISSION_GRANTED) {
                    // Ask for permission
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 0);
                    return;
                }

                smsTextView.setText(retrieveMessages());
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission/s granted
                    smsTextView.setText(retrieveMessages());
                } else {
                    // Permission/s not granted
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private String retrieveMessages() {
        Uri uri = Uri.parse("content://sms");
        ContentResolver cr = getContentResolver();

        String[] columns = {"date", "address", "body", "type"};
        Cursor cursor = cr.query(
                uri,
                columns,
                null,
                null);

        String smsBody = "";
        if (cursor.moveToFirst()) {
            do {
                // Date
                long dateInMillis = cursor.getLong(0);
                String date = (String) DateFormat.format("dd MMM yyyy h:mm:ss aa", dateInMillis);
                // Address
                String address = cursor.getString(1);

                // Body
                String body = cursor.getString(2);

                // Type: Recipient Name or Number
                String type = cursor.getString(3);
                type = type.equalsIgnoreCase("1") ? "Inbox:" : "Sent:";

                smsBody += String.format("%s %s \n at %s\n%s\n\n", type, address, date, body);
            } while (cursor.moveToNext());
        }

        return smsBody;

    }


}
