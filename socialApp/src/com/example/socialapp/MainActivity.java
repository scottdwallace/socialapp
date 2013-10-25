package com.example.socialapp;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //adds the respective elements to the list
        initList();
        ListView lv = (ListView) findViewById(R.id.listView);
        SimpleAdapter simpleAdpt;
        simpleAdpt = new SimpleAdapter(this,socialList,android.R.layout.simple_list_item_1,new String[]
                {"social"},new int[] {android.R.id.text1});

        lv.setAdapter(simpleAdpt);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    List<Map<String,String>> socialList=new ArrayList<Map<String,String>>();

    private void initList()
    {
        socialList.add(createSocial("social","Facebook"));
        socialList.add(createSocial("social","Twitter"));
        socialList.add(createSocial("social","LinkdIn"));


    }
    private HashMap<String, String> createSocial(String key, String name)
    {
        HashMap<String, String> social= new HashMap<String,String>();
        social.put(key,name);
        return social;
    }
}