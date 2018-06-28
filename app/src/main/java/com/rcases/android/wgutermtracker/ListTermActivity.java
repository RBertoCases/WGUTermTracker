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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rcases.android.wgutermtracker.database.AppDatabase;
import com.rcases.android.wgutermtracker.database.Term;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static com.rcases.android.wgutermtracker.AddTermActivity.EXTRA_TERM_ID;

public class ListTermActivity extends AppCompatActivity implements TermAdapter.ItemClickListener {

    private static final String TAG = ListTermActivity.class.getSimpleName();
    private TermAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private AppDatabase mDb;
    private int mTermId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_term);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(ListTermActivity.this, AddTermActivity.class);
                startActivity(addIntent);
            }
        });

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.rv_terms);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new TermAdapter(this, this);

        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        mDb = AppDatabase.getsInstance(getApplicationContext());
        retrieveTerms();

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
                try {
                    int position = viewHolder.getAdapterPosition();
                    List<Term> terms = mAdapter.getTerms();
                    mTermId = terms.get(position).getId();
                    if (mDb.courseDao().findAnyCoursesForTerm(mTermId)) {
                        new AlertDialog.Builder(ListTermActivity.this)
                                .setTitle("Unable to Delete")
                                .setIcon(R.drawable.ic_delete_forever_black_24dp)
                                .setMessage("Term has associated courses. \n " +
                                        "Delete associated courses before deleting term.")
                                .show();
                        Log.d(TAG, "Cancelling term deletion");

                        retrieveTerms();
                        Log.d(TAG, "retrieveTerms()");
                    } else {
                        new AlertDialog.Builder(ListTermActivity.this)
                                .setTitle("CONFIRM")
                                .setIcon(R.drawable.ic_delete_black_24dp)
                                .setMessage("Delete Term?")
                                .setPositiveButton("Delete",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        int position = viewHolder.getAdapterPosition();
                                                        List<Term> terms = mAdapter.getTerms();
                                                        mDb.termDao().deleteTerm(terms.get(position));

                                                    }
                                                });
                                                Log.d(TAG, "Deleted Term");
                                            }
                                        })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.d(TAG, "Cancelling term deletion");
                                                retrieveTerms();
                                                Log.d(TAG, "retrieveTerms()");
                                            }
                                        })
                                .show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).attachToRecyclerView(mRecyclerView);


    }


    @Override
    protected void onResume() {
        super.onResume();

        retrieveTerms();

    }

    private void retrieveTerms() {
        mDb = AppDatabase.getsInstance(getApplicationContext());
        final LiveData<List<Term>> terms = mDb.termDao().loadAllTerms();
        terms.observe(this, new Observer<List<Term>>() {
            @Override
            public void onChanged(@Nullable List<Term> termItems) {
                mAdapter.setTerms(termItems);
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
        Intent intent = new Intent(ListTermActivity.this, AddTermActivity.class);
        intent.putExtra(EXTRA_TERM_ID, itemId);
        Log.d(TAG, "Outgoing EXTRA TERM ID = " + itemId);
        startActivity(intent);
    }

}
