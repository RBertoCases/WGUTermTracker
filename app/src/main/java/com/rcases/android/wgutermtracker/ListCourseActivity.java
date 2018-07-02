package com.rcases.android.wgutermtracker;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rcases.android.wgutermtracker.database.AppDatabase;
import com.rcases.android.wgutermtracker.database.Course;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static com.rcases.android.wgutermtracker.AddTermActivity.EXTRA_TERM_ID;
import static com.rcases.android.wgutermtracker.MainActivity.EXTRA_MAIN;

public class ListCourseActivity extends AppCompatActivity implements CourseAdapter.ItemClickListener {

    private static final String TAG = ListCourseActivity.class.getSimpleName();
    private static final int DEFAULT_TERM_ID = -1;
    private CourseAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private AppDatabase mDb;
    private int mTermId = DEFAULT_TERM_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_course);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab_add_course);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_MAIN)) {
            fab.setEnabled(false);
            fab.setVisibility(View.INVISIBLE);
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent addIntent = new Intent(ListCourseActivity.this, AddCourseActivity.class);
                    addIntent.putExtra(EXTRA_TERM_ID, mTermId);
                    startActivity(addIntent);
                }
            });
        }
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.rv_courses);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new CourseAdapter(this, this);

        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                new AlertDialog.Builder(ListCourseActivity.this)
                        .setTitle("CONFIRM")
                        .setIcon(R.drawable.ic_delete_black_24dp)
                        .setMessage("Deleting this course will delete any associated assessments. \n" +
                                "Are you sure?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        int position = viewHolder.getAdapterPosition();
                                        List<Course> courses = mAdapter.getCourses();
                                        mDb.courseDao().deleteCourse(courses.get(position));

                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                retrieveCourses();
                            }
                        })
                        .show();

            }
        }).attachToRecyclerView(mRecyclerView);


    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveCourses();
    }

    private void retrieveCourses() {
        mDb = AppDatabase.getsInstance(getApplicationContext());
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TERM_ID)) {
            if (mTermId == DEFAULT_TERM_ID) {
                mTermId = intent.getIntExtra(EXTRA_TERM_ID, DEFAULT_TERM_ID);
                final LiveData<List<Course>> courses = mDb.courseDao().findCoursesForTerm(mTermId);
                courses.observe(this, new Observer<List<Course>>() {
                    @Override
                    public void onChanged(@Nullable List<Course> courseItems) {
                        mAdapter.setCourses(courseItems);
                    }
                });
            }
        } else {
            final LiveData<List<Course>> courses = mDb.courseDao().loadAllCourses();
            courses.observe(this, new Observer<List<Course>>() {
                @Override
                public void onChanged(@Nullable List<Course> courseItems) {
                    mAdapter.setCourses(courseItems);
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.saveItem).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    //@Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(ListCourseActivity.this, AddCourseActivity.class);
        intent.putExtra(AddCourseActivity.EXTRA_COURSE_ID, itemId);
        intent.putExtra(EXTRA_TERM_ID, mTermId);
        startActivity(intent);
    }

}
