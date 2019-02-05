package com.priyesh.usergpstracking;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setDefaults();
        setEventHandler();
    }

    private void init() {
        context = MainActivity.this;
        db = new DatabaseHelper(context);
    }

    private void setDefaults() {
        startService(new Intent(context, LocationUpdateService.class));
    }

    private void setEventHandler() {
        findViewById(R.id.btn_databasebackup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (db.copyChecklistDbToFolder()) {
                        Toast.makeText(context, "Backup saved sucessfully to folder.", Toast.LENGTH_SHORT).show();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
