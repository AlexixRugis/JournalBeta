package com.example.journalbeta;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment timetableFragment = new TimetableFragment();
    private Fragment marksFragment = new MarksFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_menu);

        timetableFragment = new TimetableFragment();
        marksFragment = new MarksFragment();

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomMenuListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                timetableFragment).commit();


    }

    @Override
    public void onBackPressed() { }

    private BottomNavigationView.OnNavigationItemSelectedListener bottomMenuListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.menu_timetable:
                            selectedFragment = timetableFragment;
                            break;
                        case R.id.menu_marks:
                            selectedFragment = marksFragment;
                            break;
                    }

                    if(selectedFragment != null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedFragment).commit();

                    return true;
                }
            };
}