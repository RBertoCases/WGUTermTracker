package com.rcases.android.wgutermtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MAIN = "mainActivity";

    private Button termListButton;
    private Button courseListButton;
    private Button assessmentListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        termListButton = findViewById(R.id.termListButton);
        courseListButton = findViewById(R.id.courseListButton);
        assessmentListButton = findViewById(R.id.assessmentListButton);

        termListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListTermActivity.class);
                startActivity(intent);
            }
        });

        courseListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListCourseActivity.class);
                intent.putExtra(EXTRA_MAIN, false);
                startActivity(intent);
            }
        });

        assessmentListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListAssessmentActivity.class);
                intent.putExtra(EXTRA_MAIN, false);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menu.findItem(R.id.saveItem).setEnabled(false);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.saveItem) {
            Toast.makeText(this, "Save Button pressed.", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }


}
