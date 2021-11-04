package com.example.journalbeta;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimetableFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public static TimetableFragment instance;

    final Calendar calendar = Calendar.getInstance();
    View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_timetable, container, false);

        instance = this;


        final SwipeRefreshLayout refreshLayout = fragmentView.findViewById(R.id.timetable_refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(JournalData.isAuthorized) {
                    updateLessons();
                }
                else
                    refreshLayout.setRefreshing(false);
            }
        });

        updateList();

        final ImageButton dateSelectButton = fragmentView.findViewById(R.id.timetable_date_button);
        updateDateText();
        dateSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateText();
                        updateLessons();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Inflate the layout for this fragment
        return fragmentView;
    }

    private void updateDateText() {
        final TextView dateText = fragmentView.findViewById(R.id.timetable_date_text);
        dateText.setText(new SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(calendar.getTime()));

    }

    public void updateList() {
        ListView listView = fragmentView.findViewById(R.id.timetable_listview);
        LessonAdapter adapter = new LessonAdapter(fragmentView.getContext(), JournalData.daySubjects, JournalData.descriptions, JournalData.homework, JournalData.dairyMarks);
        listView.setAdapter(adapter);

        final SwipeRefreshLayout refreshLayout = fragmentView.findViewById(R.id.timetable_refresh);
        refreshLayout.setRefreshing(false);
    }

    private void updateLessons() {
        JournalAPI.getLessons(fragmentView.getContext(), new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.getTime()));
    }

    static class LessonAdapter extends ArrayAdapter<String> {
        Context context;
        String[] mSubjects;
        String[] mDescriptions;
        String[] mHomework;
        String[] mMarks;

        public LessonAdapter(Context c, String[] subjects, String[] descriptions, String[] homework, String[] marks) {
            super(c, R.layout.subject_marks_row, R.id.marks_row_subject, subjects);
            context = c;
            mSubjects = subjects;
            mDescriptions = descriptions;
            mHomework = homework;
            mMarks = marks;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.lesson_row, parent, false);
            TextView title = view.findViewById(R.id.lesson_row_subject);
            TextView description = view.findViewById(R.id.lesson_row_description);
            TextView homework = view.findViewById(R.id.lesson_row_homework);
            TextView mark = view.findViewById(R.id.lesson_row_mark);

            title.setText(mSubjects[position]);
            description.setText(mDescriptions[position]);
            homework.setText(mHomework[position]);
            mark.setText(mMarks[position]);

            return view;
        }
    }
}