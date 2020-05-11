package com.example.uidraft;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements ItemAdapter.OnItemListener{

    private  static final String TAG = "MainActivity";

    ItemAdapter itemAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        List<String> itemList = new ArrayList<>();
        for(int i=0; i<3; i++){
            itemList.add("element"+(i+1)+"");
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        final ItemAdapter itemAdapter = new ItemAdapter(itemList,MainActivity.this, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this,3);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        this.itemAdapter = itemAdapter;

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText2);
                String input = editText.getText().toString();
                int index = Integer.parseInt(input); // selection index
//https://androidnoon.com/highlight-selected-item-in-recyclerview-on-click-android-studio/ -> highlighting
                itemAdapter.select(index);

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
        // switch(item.getItemId())
        // case check
    }

    @Override
    public void onItemClick(int position) {
        System.out.println("clicked@@@@");
        Log.d(TAG, "onItemClick: clicked.");

        Intent intent = new Intent(this, itemAdapter.activities[position]);

        startActivity(intent);
    }
}
