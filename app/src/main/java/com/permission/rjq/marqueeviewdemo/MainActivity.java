package com.permission.rjq.marqueeviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MarqueeView marqueeView = findViewById(R.id.marquee_view);
        List<String> strings = new ArrayList<>();
        strings.add("123456789");
        strings.add("223456789");
        strings.add("323456789");
        strings.add("423456789");
        strings.add("523456789");
        marqueeView.startWithList(strings);
    }
}
