package com.rcases.android.wgutermtracker;

import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rcases.android.wgutermtracker.database.AppDatabase;
import com.rcases.android.wgutermtracker.database.Term;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static com.rcases.android.wgutermtracker.R.color.colorPrimary;

public class AddTermActivity extends AppCompatActivity implements View.OnClickListener {

    // Extra for the term ID to be received in the intent
    public static final String EXTRA_TERM_ID = "extraTermId";
    // Extra for the term ID to be received after rotation
    public static final String INSTANCE_TERM_ID = "instanceTermId";
    //
    private static final int DEFAULT_TERM_ID = -1;

    private EditText mTitle;
    private EditText mStartDate;
    private EditText mEndDate;
    private int mYear, mMonth, mDay;
    private ImageButton mStartDateButton, mEndDateButton;
    private Button mCourseButton;
    private AppDatabase mDb;
    private int mTermId = DEFAULT_TERM_ID;
    private TextView mCourseList;

    private void initViews() {
        mTitle = findViewById(R.id.termTitleField);
        mStartDate = findViewById(R.id.termStartField);
        mEndDate = findViewById(R.id.termEndField);
        mStartDateButton = findViewById(R.id.startDateButton);
        mEndDateButton = findViewById(R.id.endDateButton);
        mCourseList = findViewById(R.id.tvTermCourseList);
        //mCourseButton = findViewById(R.id.buttonCoursesForTerm);
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
        //mCourseButton.setOnClickListener(this);

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

        if (mTermId != DEFAULT_TERM_ID) {
            coursesButton();
            mCourseList.setText("Course List");
        } else {
            mCourseList.setText("Once you save the term, the COURSES button " +
                    "will appear here to allow you to create asssociated courses");
        }
    }


    private void coursesButton() {
        Button button = new Button(this);

        button.setText("COURSES");
        button.setBackgroundColor(getResources().getColor(colorPrimary));
        button.setTextColor(Color.WHITE);
        LinearLayout linearLayout = findViewById(R.id.linearLayoutTerm);
        linearLayout.addView(button); // this call instantiates the button on screen.

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(AddTermActivity.this, ListCourseActivity.class);
                intent.putExtra(EXTRA_TERM_ID, mTermId);
                startActivity(intent);
            }
        });
    }

    private void populateUI(Term term) {
        if (term == null) {
            return;
        }

        String start = DateUtil.dateFormat.format(term.getStartDate());
        String end = DateUtil.dateFormat.format(term.getEndDate());

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
            startDate = DateUtil.dateFormat.parse(start);
            endDate = DateUtil.dateFormat.parse(end);
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
