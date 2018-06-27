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
public interface CourseDao {

    @Query("SELECT * FROM Course ORDER BY title")
    LiveData<List<Course>> loadAllCourses();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Course course);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateCourse(Course course);

    @Delete
    void deleteCourse(Course course);

    @Query("SELECT * FROM Course WHERE course_id = :id")
    LiveData<Course> loadCourseById(int id);

    @Query("SELECT * FROM Course WHERE termId = :termId")
    LiveData<List<Course>> findCoursesForTerm(int termId);

    @Query("SELECT * FROM Course WHERE termId = :termId LIMIT 1")
    boolean findAnyCoursesForTerm(int termId);

}
