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
public interface AssessmentDao {

    @Query("SELECT * FROM Assessment ORDER BY goalDate")
    LiveData<List<Assessment>> loadAllAssessments();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Assessment assessment);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateAssessment(Assessment assessment);

    @Delete
    void deleteAssessment(Assessment assessment);

    @Query("SELECT * FROM Assessment WHERE assessment_id = :id")
    LiveData<Assessment> loadAssessmentById(int id);

    @Query("SELECT * FROM Assessment WHERE courseId = :courseId")
    LiveData<List<Assessment>> findAssessmentsForCourse(int courseId);

}
