package com.nauka2000.nauka2000.Activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nauka2000.nauka2000.Adapter.PostAdapter;
import com.nauka2000.nauka2000.Classes.RecyclerItemClickListener;
import com.nauka2000.nauka2000.Json.JSONParser;
import com.nauka2000.nauka2000.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private SearchView searchView;
    private ArrayList PostList = new ArrayList<HashMap<String, String>>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PostAdapter adapter;
    private LinearLayout loader;
    private String query;
    private TextView textQuery;

    private String url;
    private JSONObject obj = null;
    private int loadStart = 0;
    private int loadCount;
    private boolean endLoad = false;

    private SharedPreferences setting;
    private Boolean use_connect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setting = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        loader = (LinearLayout) findViewById(R.id.loader);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if(isOnline()){
                            if (!endLoad) {
                                mSwipeRefreshLayout.setRefreshing(true);
                                new SearchActivity.JSONParsePost().execute();
                            } else {
                                mSwipeRefreshLayout.setRefreshing(false);
                                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.nothing_more_download, Snackbar.LENGTH_LONG);
                                View view = snack.getView();
                                view.setBackgroundColor(Color.rgb(48, 63, 159));
                                TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.WHITE);
                                snack.show();
                            }
                        }
                    }
                });
            }
        });

        textQuery = (TextView) findViewById(R.id.textQuery);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.post_contentView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = (RecyclerView) findViewById(R.id.post_scrollableview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if(savedInstanceState != null){
            PostList = savedInstanceState.getStringArrayList("PostList");
        }

        adapter = new PostAdapter(PostList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Map<String, String> hashmap;
                        hashmap = (Map<String, String>) PostList.get(position);

                        Intent intent = new Intent(SearchActivity.this, PostFullActivity.class);
                        intent.putExtra("post_id", hashmap.get("id"));
                        startActivity(intent);
                    }
                })
        );

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        if(isOnline()){
            textQuery.setText(getString(R.string.string_search) + " " + query);
            mSwipeRefreshLayout.setRefreshing(true);
            new JSONParsePost().execute();
        }
        else{
            loader.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        if(isOnline()){
            if(!endLoad) {
                new SearchActivity.JSONParsePost().execute();
            }
            else{
                mSwipeRefreshLayout.setRefreshing(false);

                if(PostList.size() > 0) {
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.nothing_more_download, Snackbar.LENGTH_LONG);
                    View view = snack.getView();
                    view.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snack.show();
                }
            }
        }
    }

    private class JSONParsePost extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            loadCount = Integer.parseInt(setting.getString("count_load", "10"));
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();

            try {
                url = "http://nauka2000.com/?nauka_api=1&get_post_search="+ URLEncoder.encode(query, "UTF-8")+"&start="+loadStart+"&end="+loadCount;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Getting JSON from URL
            JSONObject json = null;
            try {
                json = jParser.getJSONFromUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if(json != null) {
                    // Getting JSON Array
                    obj = json.getJSONObject("data");
                    JSONArray arr = obj.getJSONArray("arr");

                    if(arr.length() < loadCount){
                        endLoad = true;
                    }

                    HashMap<String, String> hm;
                    ArrayList<HashMap<String, String>> NewPostList = new ArrayList<HashMap<String, String>>();

                    for (int i = 0, max = arr.length(); i < max; i++) {
                        hm = new HashMap<>();

                        JSONObject item = (JSONObject) arr.get(i);

                        hm.put("id", item.getString("id"));
                        hm.put("title", item.getString("title"));
                        hm.put("date", item.getString("date"));
                        hm.put("excerpt", item.getString("excerpt"));
                        //hm.put("content", item.getString("content"));
                        hm.put("url", item.getString("url"));
                        hm.put("thumbnail", item.getString("thumbnail"));
                        hm.put("image", item.getString("image"));

                        NewPostList.add(hm);
                    }

                    for (int i = 0, max = PostList.size(); i < max; i++) {
                        NewPostList.add((HashMap<String, String>) PostList.get(i));
                    }

                    PostList = NewPostList;

                    adapter = new PostAdapter(PostList);
                    recyclerView.setAdapter(adapter);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(SearchActivity.this);
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    layoutManager.scrollToPosition(arr.length());
                    recyclerView.setLayoutManager(layoutManager);

                    loadStart = PostList.size();
                }
                else{
                    endLoad = true;

                    if(PostList.size() == 0){
                        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.no_search, Snackbar.LENGTH_LONG);
                        View view = snack.getView();
                        view.setBackgroundColor(Color.rgb(48, 63, 159));
                        TextView textView = (TextView)view .findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        snack.show();
                    }
                }

                loader.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isOnline() {
        use_connect = setting.getBoolean("use_connect", true);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
            else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && !use_connect) {
                return true;
            }
            else{
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_wifi, Snackbar.LENGTH_LONG);
                View view = snack.getView();
                view.setBackgroundColor(Color.rgb(48, 63, 159));
                TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snack.show();
                return false;
            }
        }
        else {
            Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_connect, Snackbar.LENGTH_LONG);
            View view = snack.getView();
            view.setBackgroundColor(Color.rgb(48, 63, 159));
            TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snack.show();
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("PostList", PostList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        PostList = savedInstanceState.getStringArrayList("PostList");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.action_search));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(SearchActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cat) {
            Intent intent = new Intent(SearchActivity.this, CatActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_tag) {
            Intent intent = new Intent(SearchActivity.this, TagActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_stat) {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_add) {
            Intent intent = new Intent(SearchActivity.this, AddActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(SearchActivity.this, InfoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
