package com.rcases.android.wgutermtracker;

import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.rcases.android.wgutermtracker.database.AppDatabase;
import com.rcases.android.wgutermtracker.database.Assessment;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static com.rcases.android.wgutermtracker.AddCourseActivity.EXTRA_COURSE_ID;

public class AddAssessmentActivity extends AppCompatActivity implements View.OnClickListener {

    // Constants for assessment type
    public static final int TYPE_OBJ = 1;
    public static final int TYPE_PERF = 2;
    // Extra for the assessment ID to be received in the intent
    public static final String EXTRA_ASSESSMENT_ID = "extraAssessmentId";
    // Extra for the assessment ID to be received after rotation
    public static final String INSTANCE_ASSESSMENT_ID = "instanceAssessmentId";

    private static final int DEFAULT_ASSESSMENT_ID = -1;

    private static final int DEFAULT_COURSE_ID = -1;


    private EditText mTitle, mGoalDate;
    private Switch mGoalAlert;
    private int mYear, mMonth, mDay;
    private ImageButton mGoalDateButton;
    private RadioGroup mRadioGroup;
    private AppDatabase mDb;
    private int mAssessmentId = DEFAULT_ASSESSMENT_ID;
    private int mCourseId = DEFAULT_COURSE_ID;

    private void initViews() {
        mTitle = findViewById(R.id.editTextAssessmentTitle);
        mRadioGroup = findViewById(R.id.radioGroup);
        mGoalDate = findViewById(R.id.editTextGoalDate);
        mGoalDateButton = findViewById(R.id.buttonGoalDate);
        mGoalAlert = findViewById(R.id.switchGoalDateAlert);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assessment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();

        mGoalDateButton.setOnClickListener(this);
        mGoalAlert.setOnClickListener(this);

        mDb = AppDatabase.getsInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ASSESSMENT_ID)) {
            mAssessmentId = savedInstanceState.getInt(INSTANCE_ASSESSMENT_ID, DEFAULT_ASSESSMENT_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ASSESSMENT_ID)) {
            if (mAssessmentId == DEFAULT_ASSESSMENT_ID) {
                mAssessmentId = intent.getIntExtra(EXTRA_ASSESSMENT_ID, DEFAULT_ASSESSMENT_ID);
                final LiveData<Assessment> assessment = mDb.assessmentDao()
                        .loadAssessmentById(mAssessmentId);
                assessment.observe(this, new Observer<Assessment>() {
                    @Override
                    public void onChanged(@Nullable Assessment assessmentItem) {
                        mCourseId = assessment.getValue().getCourseId();
                        assessment.removeObserver(this);
                        populateUI(assessmentItem);
                    }
                });
            }
        }

        if (intent != null && intent.hasExtra(EXTRA_COURSE_ID)) {
            if (mCourseId == DEFAULT_COURSE_ID) {
                mCourseId = intent.getIntExtra(EXTRA_COURSE_ID, DEFAULT_COURSE_ID);
            }
        }

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        mGoalAlert.setChecked(sharedPreferences
                .getBoolean("assessmentAlert" + mAssessmentId, false));

    }


    private void populateUI(Assessment assessment) {
        if (assessment == null) {
            return;
        }

        String goal = DateUtil.dateFormat.format(assessment.getGoalDate());

        mTitle.setText(assessment.getTitle());
        setTypeInViews(assessment.getAssessmentType());
        mGoalDate.setText(goal);

    }

    @Override
    public void onClick(View v) {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        if (v == mGoalDateButton) {

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            mGoalDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }


    }

    public void onSaveButtonClicked() {
        String title = mTitle.getText().toString();
        int type = getTypeFromViews();
        String goal = mGoalDate.getText().toString();
        Date goalDate = null;

        //Stops save action if there is a ParseException
        try {
            goalDate = DateUtil.dateFormat.parse(goal);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Please enter a valid date", Toast.LENGTH_LONG).show();
            return;
        }

        final Assessment assessment = new Assessment(title, type, goalDate, mCourseId);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mAssessmentId == DEFAULT_ASSESSMENT_ID) {
                    mDb.assessmentDao().insert(assessment);
                } else {
                    assessment.setId(mAssessmentId);
                    assessment.setCourseId(mCourseId);
                    mDb.assessmentDao().updateAssessment(assessment);

                }
                saveAssessmentAlert();
                finish();
            }
        });
        Toast.makeText(this, "Assessment Saved", Toast.LENGTH_LONG).show();
    }

    private void saveAssessmentAlert() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(new StringBuilder().append("assessmentAlert").append(mAssessmentId).toString(), mGoalAlert.isChecked());
        editor.apply();

        if (mGoalAlert.isChecked()) {
            enableAlert();
        }
    }

    private void enableAlert() {
        long now = DateUtil.todayLong();
        String title = "Assessment: " + mTitle.getText().toString();
        String body = "Your " + getTypeNameFromViews() + " assessment is today!";
        String body3 = "Your " + getTypeNameFromViews() + " assessment is within 3 days!";
        String body21 = "Your " + getTypeNameFromViews() + " assessment is within 3 weeks!";
        String goal = mGoalDate.getText().toString();

        if (now <= DateUtil.getDateTimestamp(goal)) {
            AlertReceiver.scheduleAssessmentAlarm(getApplicationContext(), mAssessmentId,
                    DateUtil.getDateTimestamp(goal), title, body);
        }
        if (now <= DateUtil.getDateTimestamp(goal) - 3 * 24 * 60 * 60 * 1000) {
            AlertReceiver.scheduleAssessmentAlarm(getApplicationContext(), mAssessmentId,
                    DateUtil.getDateTimestamp(goal) - 3 * 24 * 60 * 60 * 1000, title, body3);
        }
        if (now <= DateUtil.getDateTimestamp(goal) - 21 * 24 * 60 * 60 * 1000) {
            AlertReceiver.scheduleAssessmentAlarm(getApplicationContext(), mAssessmentId,
                    DateUtil.getDateTimestamp(goal) - 21 * 24 * 60 * 60 * 1000, title, body21);
        }

    }

    /**
     * getType is called whenever the selected type needs to be retrieved
     */
    public int getTypeFromViews() {
        int type = 1;
        int checkedId = ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radButtonObj:
                type = TYPE_OBJ;
                break;
            case R.id.radButtonPerf:
                type = TYPE_PERF;
        }
        return type;
    }

    public String getTypeNameFromViews() {
        int type = 1;
        String typeName = "";
        int checkedId = ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radButtonObj:
                typeName = "Objective";
                break;
            case R.id.radButtonPerf:
                typeName = "Performance";
        }
        return typeName;
    }

    /**
     * setType is called when we receive a assessment from ListAssessmentActivity
     *
     * @param type the priority value
     */
    public void setTypeInViews(int type) {
        switch (type) {
            case TYPE_OBJ:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButtonObj);
                break;
            case TYPE_PERF:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButtonPerf);
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
        long id = item.getItemId();

        if (id == R.id.saveItem) {
            onSaveButtonClicked();
        }

        return super.onOptionsItemSelected(item);
    }

}
