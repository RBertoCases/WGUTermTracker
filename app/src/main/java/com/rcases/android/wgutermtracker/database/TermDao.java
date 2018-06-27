package com.rcases.android.wgutermtracker.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TermDao {

    @Query("SELECT * FROM Term ORDER BY title")
    LiveData<List<Term>> loadAllTerms();

    @Insert
    void insert(Term term);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTerm(Term term);

    @Delete
    void deleteTerm(Term term);

    @Query("SELECT * FROM Term WHERE term_id = :id")
    LiveData<Term> loadTermById(int id);

}
