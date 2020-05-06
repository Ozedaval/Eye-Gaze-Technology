package com.example.uidraft;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

        ItemAdapter itemAdapter = new ItemAdapter(itemList,MainActivity.this, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this,3);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        this.itemAdapter = itemAdapter;

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
