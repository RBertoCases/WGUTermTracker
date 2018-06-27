package com.rcases.android.wgutermtracker.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {@ForeignKey(entity = Course.class,
        parentColumns = "course_id",
        childColumns = "courseId",
        onDelete = CASCADE,
        onUpdate = CASCADE)},
        indices = {@Index(value = "courseId")})
public class Assessment {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "assessment_id")
    private int id;
    private String title;
    private int assessmentType;
    private Date goalDate;
    private int courseId;

    public Assessment(String title, int assessmentType, Date goalDate, int courseId) {
        this.title = title;
        this.assessmentType = assessmentType;
        this.goalDate = goalDate;
        this.courseId = courseId;
    }

    @NonNull
    public int getId() {
        return id;
    }

    public void setId(@NonNull int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(int assessmentType) {
        this.assessmentType = assessmentType;
    }

    public Date getGoalDate() {
        return goalDate;
    }

    public void setGoalDate(Date goalDate) {
        this.goalDate = goalDate;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = this.courseId;
    }
}