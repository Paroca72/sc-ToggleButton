package com.sccomponents.buttons.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sccomponents.buttons.ScSwitch;
import com.sccomponents.buttons.ScToggleButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScSwitch button = (ScSwitch) this.findViewById(R.id.button);
        assert button != null;
    }
}
