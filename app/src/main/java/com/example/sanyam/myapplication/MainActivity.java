package com.example.sanyam.myapplication;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.parse.Parse;
import com.parse.ParseInstallation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String APP_ID = "RAE5DvwkzwwPrFsvSqVnggmlGTMV9EUFZQBvAm3M";
    public static final String CLIENT_ID = "3S7ykAX5rwWEk1IRkWPbMD4pBgTwOrpGCq71c8uV";
    public static int logout=0;

    String my_num, my_operator;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor1;
    Intent i = null;
    Button btnLogout;

    ListView mListView;
    private List<String> feedList = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlarmManagerBroadcastReceiver.setAlarm(this);

        //login-logout code
        Bundle b = getIntent().getExtras();
        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        my_num = b.getString("my_num");
        //period=b.getInt("period");
        my_operator = b.getString("my_operator");
        btnLogout = (Button) findViewById(R.id.btnLogout);

        //populating listview on first run
        feedList = fetchData();
        ArrayAdapter<String> usageAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.listitemusage,
                R.id.listView_textView,
                feedList
        );
        mListView = (ListView) findViewById(R.id.listView_usage);
        mListView.setAdapter(usageAdapter);

        //setting up refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new DownloadFilesTask().execute();          //creates thread to get data from sql & populate listView
            }
        });

        //Parse Code
        // Enable Local Datastore.{Saving data to parse}
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, APP_ID, CLIENT_ID);

        ParseInstallation.getCurrentInstallation().saveInBackground();
    }


    //method for fetching data from sql in list format
    //Used 2 places MainActivity & Refreshing
    private List<String> fetchData() {
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        Log.e("Reading from DB :", "Reading all usage");
        List<DataUsage> usages = db.getUsageList();

        //Put fetched data from sql in a list
        List<String> usageArray = new ArrayList<>();
        for (DataUsage du : usages) {
            long newtxWifiBytes = du.getTxWifiBytes();
            long newrxWifiBytes = du.getRxWifiBytes();
            long newtxCellBytes = du.getTxCellBytes();
            long newrxCellBytes = du.getRxCellBytes();
            Date date = du.getDate();
            usageArray.add(newtxWifiBytes + " " + newrxWifiBytes + " " + newtxCellBytes + " " + newrxCellBytes + " " + date);
        }
        Collections.reverse(usageArray);
        return usageArray;
    }

    //method for deleting records from sql 'if required'
    public void deleteRecords() {
        DataUsageDatabaseHandler db = new DataUsageDatabaseHandler(this);
        db.deleteRecords();
    }

    //this works on button press
    public void refreshButton(View view) {
//        new DownloadFilesTask().execute();
        getApplicationContext().getSharedPreferences("MYPREF", 0).edit().clear().commit();
    }

    //this works on button press
    public void clearButton(View view) {
        deleteRecords();
    }

    //thread for getting data from sql & populating listview
    private class DownloadFilesTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected Void doInBackground(String... params) {
            feedList = fetchData();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            updateList();
        }
    }

    //puts value in listView for display
    private void updateList() {
        ArrayAdapter mAdapter = new ArrayAdapter(MainActivity.this, R.layout.listitemusage, feedList);
        mListView.setAdapter(mAdapter);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
    public void onLogout(View view) {
        logout = 1;
        Log.e("logout", String.valueOf(logout));
        SharedPreferences settings = getSharedPreferences(Login.MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
        Log.e("logout", "logout1");
        setLoginState(0);
        editor1 = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        editor1.remove("plans");
        editor1.commit();
        Log.e("logout", "logout2");

        // Log.d(TAG, "Now log out and start the activity login");
        i = new Intent(MainActivity.this, Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        }

    private void setLoginState(int status) {
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("isLogged", status);
        ed.commit();
        }
}