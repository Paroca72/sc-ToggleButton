package com.sccomponents.togglebutton.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sccomponents.togglebutton.ScToggleButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScToggleButton.addOnGroupChangeListener(new ScToggleButton.OnGroupChangeListener() {
            @Override
            public void onChanged(ScToggleButton source) {
                String i = "";
            }
        });
    }
}
