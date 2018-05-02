package domain.hackathon.personal_assistant;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

public class weather extends AppCompatActivity {
    ArrayList<HashMap<String, String>> weatherhash;
    private weatherRecAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerView reclist;
    TextView citystate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        weatherhash = new ArrayList<>();
        reclist = (RecyclerView) findViewById(R.id.wReclist);
        citystate = (TextView) findViewById(R.id.scitystate);



        Intent intent = getIntent();
        if (intent.hasExtra("wsearch"))
        {
            weatherhash = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("wsearch");
            citystate.setText(weatherhash.get(0).get("city") + ", " +weatherhash.get(0).get("state"));
        }
        mAdapter = new weatherRecAdapter(weatherhash, Glide.with(this));
        mLayoutManager = new LinearLayoutManager(this);
        reclist.setLayoutManager(mLayoutManager);
        //reclist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        reclist.setItemAnimator(new DefaultItemAnimator());
        reclist.setAdapter(mAdapter);


    }

}
