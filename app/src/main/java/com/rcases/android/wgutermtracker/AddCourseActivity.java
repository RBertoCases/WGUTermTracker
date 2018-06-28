package com.rcases.android.wgutermtracker;

import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Switch;
import android.widget.Toast;

import com.rcases.android.wgutermtracker.database.AppDatabase;
import com.rcases.android.wgutermtracker.database.Course;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.rcases.android.wgutermtracker.AddTermActivity.EXTRA_TERM_ID;

public class AddCourseActivity extends AppCompatActivity implements View.OnClickListener {

    // Extra for the course ID to be received in the intent
    public static final String EXTRA_COURSE_ID = "extraCourseId";
    // Extra for the course ID to be received after rotation
    public static final String INSTANCE_COURSE_ID = "instanceCourseId";
    //
    private static final int DEFAULT_COURSE_ID = -1;
    private static final String TAG = AddCourseActivity.class.getSimpleName();
    private static final int DEFAULT_TERM_ID = -1;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private EditText mTitle, mStartDate, mEndDate, mStatus, mNote, mName, mPhone, mEmail;
    private Switch mStartAlert, mEndAlert;
    private int mYear, mMonth, mDay;
    private ImageButton mStartDateButton, mEndDateButton;
    private Button mAssessmentsButton;
    private AppDatabase mDb;
    private int mCourseId = DEFAULT_COURSE_ID;
    private int mTermId = DEFAULT_TERM_ID;

    private void initViews() {
        mTitle = findViewById(R.id.editTextCourseTitle);
        mStartDate = findViewById(R.id.editTextCourseStartDate);
        mEndDate = findViewById(R.id.editTextCourseEndDate);
        mStartDateButton = findViewById(R.id.buttonCourseStart);
        mEndDateButton = findViewById(R.id.buttonCourseEnd);
        mStartAlert = findViewById(R.id.switchCourseStartAlert);
        mEndAlert = findViewById(R.id.switchCourseEndAlert);
        mStatus = findViewById(R.id.editTextStatus);
        mNote = findViewById(R.id.editTextNotes);
        mName = findViewById(R.id.editCourseMentor);
        mPhone = findViewById(R.id.editTextPhone);
        mEmail = findViewById(R.id.editTextEmail);
        mAssessmentsButton = findViewById(R.id.buttonAssessments);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noteToShare = mNote.getText().toString();
                shareNote(noteToShare);
            }
        });
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();

        mStartDateButton.setOnClickListener(this);
        mEndDateButton.setOnClickListener(this);
        mAssessmentsButton.setOnClickListener(this);
        mStartAlert.setOnClickListener(this);
        mEndAlert.setOnClickListener(this);


        mDb = AppDatabase.getsInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_COURSE_ID)) {
            mCourseId = savedInstanceState.getInt(INSTANCE_COURSE_ID, DEFAULT_COURSE_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_COURSE_ID)) {
            if (mCourseId == DEFAULT_COURSE_ID) {
                mCourseId = intent.getIntExtra(EXTRA_COURSE_ID, DEFAULT_COURSE_ID);
                final LiveData<Course> course = mDb.courseDao().loadCourseById(mCourseId);
                course.observe(this, new Observer<Course>() {
                    @Override
                    public void onChanged(@Nullable Course courseItem) {
                        mTermId = course.getValue().getTermId();
                        course.removeObserver(this);
                        populateUI(courseItem);
                    }
                });
            }
        }

        if (intent != null && intent.hasExtra(EXTRA_TERM_ID)) {
            if (mTermId == DEFAULT_TERM_ID) {
                mTermId = intent.getIntExtra(EXTRA_TERM_ID, DEFAULT_TERM_ID);
            }
        }


    }

    private void shareNote(String noteToShare) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, noteToShare);
        if (sharingIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        }
    }

    private void populateUI(Course course) {
        if (course == null) {
            return;
        }

        String start = dateFormat.format(course.getStartDate());
        String end = dateFormat.format(course.getEndDate());

        mTitle.setText(course.getTitle());
        mStartDate.setText(start);
        mEndDate.setText(end);
        mStatus.setText(course.getStatus());
        mNote.setText(course.getNote());
        mName.setText(course.getMentorName());
        mPhone.setText(course.getPhoneNumber());
        mEmail.setText(course.getEmail());

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        mStartAlert.setChecked(sharedPreferences
                .getBoolean("courseStartAlert" + mCourseId, false));
        mEndAlert.setChecked(sharedPreferences
                .getBoolean("courseEndAlert" + mCourseId, false));
    }

    @Override
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

        if (v == mAssessmentsButton) {
            if (mCourseId == DEFAULT_COURSE_ID) {
                Toast toast = Toast.makeText(getApplicationContext(), "Please save the Course before adding Assessments.", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Intent intent = new Intent(AddCourseActivity.this, ListAssessmentActivity.class);
                intent.putExtra(EXTRA_COURSE_ID, mCourseId);
                startActivity(intent);
            }
        }

    }

    public void onSaveButtonClicked() {
        String title = mTitle.getText().toString();
        String start = mStartDate.getText().toString();
        String end = mEndDate.getText().toString();
        String status = mStatus.getText().toString();
        String note = mNote.getText().toString();
        Date startDate = null;
        Date endDate = null;
        String name = mName.getText().toString();
        String phone = mPhone.getText().toString();
        String email = mEmail.getText().toString();

        try {
            startDate = dateFormat.parse(start);
            endDate = dateFormat.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final Course course = new Course(title, startDate, endDate, status, note, name, phone, email, mTermId);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mCourseId == DEFAULT_COURSE_ID) {
                    mDb.courseDao().insert(course);
                } else {
                    course.setId(mCourseId);
                    mDb.courseDao().updateCourse(course);
                }
                saveCourseAlerts();
                finish();
            }
        });

    }

    private void saveCourseAlerts() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("courseStartAlert" + mCourseId, mStartAlert.isChecked());
        editor.putBoolean("courseEndAlert" + mCourseId, mEndAlert.isChecked());
        editor.apply();

        if (mStartAlert.isChecked()) {
            enableStartAlert();
        }

        if (mEndAlert.isChecked()) {
            enableEndAlert();
        }
    }

    private void enableStartAlert() {
        long now = DateUtil.todayLong();
        String title = "Course: " + mTitle.getText().toString();
        String body = "Your " + mTitle.getText().toString() + " course begins today!";
        String body3 = "Your " + mTitle.getText().toString() + " course starts within 3 days!";
        String body21 = "Your " + mTitle.getText().toString() + " course starts within 3 weeks!";
        String date = mStartDate.getText().toString();

        if (now <= DateUtil.getDateTimestamp(date)) {
            AlertReceiver.scheduleCourseStartAlarm(getApplicationContext(),
                    mCourseId, DateUtil.getDateTimestamp(date), title, body);
        }
        if (now <= DateUtil.getDateTimestamp(date) - 3 * 24 * 60 * 60 * 1000) {
            AlertReceiver.scheduleCourseStartAlarm(getApplicationContext(), mCourseId,
                    DateUtil.getDateTimestamp(date) - 3 * 24 * 60 * 60 * 1000, title, body3);
        }
        if (now <= DateUtil.getDateTimestamp(date) - 21 * 24 * 60 * 60 * 1000) {
            AlertReceiver.scheduleCourseStartAlarm(getApplicationContext(), mCourseId,
                    DateUtil.getDateTimestamp(date) - 21 * 24 * 60 * 60 * 1000, title, body21);
        }
    }

    private void enableEndAlert() {
        long now = DateUtil.todayLong();
        String title = "Course: " + mTitle.getText().toString();
        String body = "Your " + mTitle.getText().toString() + " course ends today!";
        String body3 = "Your " + mTitle.getText().toString() + " course ends within 3 days!";
        String body21 = "Your " + mTitle.getText().toString() + " course ends within 3 weeks!";
        String date = mEndDate.getText().toString();

        if (now <= DateUtil.getDateTimestamp(date)) {
            AlertReceiver.scheduleCourseEndAlarm(getApplicationContext(),
                    mCourseId, DateUtil.getDateTimestamp(date), title, body);
        }
        if (now <= DateUtil.getDateTimestamp(date) - 3 * 24 * 60 * 60 * 1000) {
            AlertReceiver.scheduleCourseEndAlarm(getApplicationContext(), mCourseId,
                    DateUtil.getDateTimestamp(date) - 3 * 24 * 60 * 60 * 1000, title, body3);
        }
        if (now <= DateUtil.getDateTimestamp(date) - 21 * 24 * 60 * 60 * 1000) {
            AlertReceiver.scheduleCourseEndAlarm(getApplicationContext(), mCourseId,
                    DateUtil.getDateTimestamp(date) - 21 * 24 * 60 * 60 * 1000, title, body21);
        }
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
            Toast.makeText(this, "Course Saved", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
