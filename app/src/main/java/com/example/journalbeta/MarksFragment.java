package com.example.journalbeta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class MarksFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static MarksFragment instance;
    View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        instance = this;
        fragmentView = inflater.inflate(R.layout.fragment_marks, container, false);
        updateList();

        final SwipeRefreshLayout refreshLayout = fragmentView.findViewById(R.id.marks_refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(JournalData.isAuthorized)
                    JournalAPI.getMarks(fragmentView.getContext());
                else
                    refreshLayout.setRefreshing(false);
            }
        });

        return fragmentView;
    }

    public void updateList() {
        ListView listView = fragmentView.findViewById(R.id.marks_listview);
        SubjectRowAdapter adapter = new SubjectRowAdapter(fragmentView.getContext(), JournalData.subjects, JournalData.marks);
        listView.setAdapter(adapter);

        SwipeRefreshLayout refreshLayout = fragmentView.findViewById(R.id.marks_refresh);
        refreshLayout.setRefreshing(false);
    }

    static class SubjectRowAdapter extends ArrayAdapter<String> {
        Context context;
        String[] mSubjects;
        String[] mMarks;

        public SubjectRowAdapter(Context c, String[] subjects, String[] marks) {
            super(c, R.layout.subject_marks_row, R.id.marks_row_subject, subjects);
            context = c;
            mSubjects = subjects;
            mMarks = marks;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.subject_marks_row, parent, false);
            TextView title = view.findViewById(R.id.marks_row_subject);
            TextView marks = view.findViewById(R.id.marks_row_marks);

            title.setText(mSubjects[position]);
            marks.setText(mMarks[position]);

            return view;
        }
    }
}