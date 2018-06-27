/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rcases.android.wgutermtracker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rcases.android.wgutermtracker.database.Assessment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This TaskAdapter creates and binds ViewHolders, that hold the description and priority of a task,
 * to a RecyclerView to efficiently display data.
 */
public class AssessmentAdapter extends RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder> {

    private static final String TAG = AssessmentAdapter.class.getSimpleName();

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    // Date formatter
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    // Class variables for the List that holds task data and the Context
    private List<Assessment> mAssessments;
    private Context mContext;


    /**
     * Constructor for the TaskAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public AssessmentAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public AssessmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.assessment_list_item, parent, false);

        return new AssessmentViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(AssessmentViewHolder holder, int position) {
        // Determine the values of the wanted data
        Assessment assessment = mAssessments.get(position);
        String title = assessment.getTitle();
        String type = getTypeText(assessment.getAssessmentType());
        //TODO BREAKS APP IF SAVED WITHOUT DATE
        String goalDate = dateFormat.format(assessment.getGoalDate());

        //Set values
        holder.assessmentTitleView.setText(title);
        holder.assessmentGoalDate.setText("Goal Date: " + goalDate);
        holder.assessmentTypeView.setText(type);

    }

    private String getTypeText(int type) {
        String typeString = "";

        switch (type) {
            case 1:
                typeString = "O";
                break;
            case 2:
                typeString = "P";
                break;
            default:
                break;
        }
        return typeString;
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mAssessments == null) {
            return 0;
        }
        return mAssessments.size();
    }

    public List<Assessment> getAssessments() {
        return mAssessments;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setAssessments(List<Assessment> assessments) {
        mAssessments = assessments;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    class AssessmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the task description and priority TextViews
        TextView assessmentTitleView;
        TextView assessmentGoalDate;
        TextView assessmentTypeView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public AssessmentViewHolder(View itemView) {
            super(itemView);

            assessmentTitleView = itemView.findViewById(R.id.assessmentTitle);
            assessmentTypeView = itemView.findViewById(R.id.textViewAssessmentType);
            assessmentGoalDate = itemView.findViewById(R.id.assessmentGoalDate);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mAssessments.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}