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

import com.rcases.android.wgutermtracker.database.Term;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This TaskAdapter creates and binds ViewHolders, that hold the description and priority of a task,
 * to a RecyclerView to efficiently display data.
 */
public class TermAdapter extends RecyclerView.Adapter<TermAdapter.TermViewHolder> {

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    // Date formatter
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    // Class variables for the List that holds task data and the Context
    private List<Term> mterms;
    private Context mContext;


    /**
     * Constructor for the TaskAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public TermAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public TermViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.term_list_item, parent, false);

        return new TermViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(TermViewHolder holder, int position) {
        // Determine the values of the wanted data
        Term term = mterms.get(position);
        String title = term.getTitle();
        String startDate = dateFormat.format(term.getStartDate());
        String endDate = dateFormat.format(term.getEndDate());

        //Set values
        holder.termTitleView.setText(title);
        holder.termStartDateView.setText("Start Date: " + startDate);
        holder.termEndDateView.setText("End Date: " + endDate);

    }

    /*
    Helper method for selecting the correct priority circle color.
    P1 = red, P2 = orange, P3 = yellow
    */

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mterms == null) {
            return 0;
        }
        return mterms.size();
    }

    public List<Term> getTerms() {
        return mterms;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setTerms(List<Term> terms) {
        mterms = terms;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    class TermViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables for the task description and priority TextViews
        TextView termTitleView;
        TextView termStartDateView;
        TextView termEndDateView;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public TermViewHolder(View itemView) {
            super(itemView);

            termTitleView = itemView.findViewById(R.id.termTitle);
            termStartDateView = itemView.findViewById(R.id.termStartDate);
            termEndDateView = itemView.findViewById(R.id.termEndDate);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mterms.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}