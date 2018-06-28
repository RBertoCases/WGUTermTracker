package com.rcases.android.wgutermtracker;

import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rcases.android.wgutermtracker.database.AppDatabase;
import com.rcases.android.wgutermtracker.database.Term;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTermActivity extends AppCompatActivity implements View.OnClickListener {

    // Extra for the term ID to be received in the intent
    public static final String EXTRA_TERM_ID = "extraTermId";
    // Extra for the term ID to be received after rotation
    public static final String INSTANCE_TERM_ID = "instanceTermId";
    //
    private static final int DEFAULT_TERM_ID = -1;
    private static final String TAG = AddTermActivity.class.getSimpleName();
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private EditText mTitle;
    private EditText mStartDate;
    private EditText mEndDate;
    private int mYear, mMonth, mDay;
    private ImageButton mStartDateButton, mEndDateButton;
    private Button mCourseButton;
    private AppDatabase mDb;
    private int mTermId = DEFAULT_TERM_ID;

    private void initViews() {
        mTitle = findViewById(R.id.termTitleField);
        mStartDate = findViewById(R.id.termStartField);
        mEndDate = findViewById(R.id.termEndField);
        mStartDateButton = findViewById(R.id.startDateButton);
        mEndDateButton = findViewById(R.id.endDateButton);
        mCourseButton = findViewById(R.id.buttonCoursesForTerm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_term);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();

        mStartDateButton.setOnClickListener(this);
        mEndDateButton.setOnClickListener(this);
        mCourseButton.setOnClickListener(this);

        mDb = AppDatabase.getsInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_TERM_ID)) {
            mTermId = savedInstanceState.getInt(INSTANCE_TERM_ID, DEFAULT_TERM_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TERM_ID)) {
            if (mTermId == DEFAULT_TERM_ID) {
                mTermId = intent.getIntExtra(EXTRA_TERM_ID, DEFAULT_TERM_ID);
                final LiveData<Term> term = mDb.termDao().loadTermById(mTermId);
                term.observe(this, new Observer<Term>() {
                    @Override
                    public void onChanged(@Nullable Term termItem) {
                        term.removeObserver(this);
                        populateUI(termItem);
                    }
                });
            }
        }
    }

    private void populateUI(Term term) {
        if (term == null) {
            return;
        }

        String start = dateFormat.format(term.getStartDate());
        String end = dateFormat.format(term.getEndDate());

        mTitle.setText(term.getTitle());
        mStartDate.setText(start);
        mEndDate.setText(end);
    }

    //@Override
    public void onClick(View v) {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        if (v == mStartDateButton) {

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            mStartDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        if (v == mEndDateButton) {

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            mEndDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        if (v == mCourseButton) {
            if (mTermId == DEFAULT_TERM_ID) {
                Toast toast = Toast.makeText(getApplicationContext(), "Please save the Term before adding Courses.", Toast.LENGTH_SHORT);
                toast.show();
            } else {
            Intent intent = new Intent(AddTermActivity.this, ListCourseActivity.class);
            intent.putExtra(EXTRA_TERM_ID, mTermId);
            startActivity(intent);
            }
        }

    }

    public void onSaveButtonClicked() {
        String title = mTitle.getText().toString();
        String start = mStartDate.getText().toString();
        String end = mEndDate.getText().toString();
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = dateFormat.parse(start);
            endDate = dateFormat.parse(end);
        } catch (ParseException e) {
            Toast.makeText(this, "Please enter valid dates", Toast.LENGTH_LONG).show();
            return;
        }

        final Term term = new Term(title, startDate, endDate);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mTermId == DEFAULT_TERM_ID) {
                    mDb.termDao().insert(term);
                } else {
                    term.setId(mTermId);
                    mDb.termDao().updateTerm(term);
                }

                finish();
            }
        });
        Toast.makeText(this, "Term Saved", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.saveItem) {
            onSaveButtonClicked();
        }

        return super.onOptionsItemSelected(item);
    }

    //@Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(AddTermActivity.this, ListCourseActivity.class);
        intent.putExtra(AddTermActivity.EXTRA_TERM_ID, itemId);
        startActivity(intent);
    }
}
