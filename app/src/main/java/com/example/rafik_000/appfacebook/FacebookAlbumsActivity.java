package com.example.rafik_000.appfacebook;

// This class Load Facebook albums and create a GrideView to show them


import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.BaseAdapter;
import com.facebook.AccessToken;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.ImageView;
import com.facebook.login.LoginManager;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.GraphRequest;
import com.facebook.FacebookGraphResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;
import android.widget.GridView;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;



public class FacebookAlbumsActivity extends AppCompatActivity {

    GridView galleryGridView;
    static final String KEY_ALBUM = "album_name";
    static final String KEY_ID = "album_id";
    static final String KEY_PATH = "path";
    static final String KEY_COUNT = "number_photo";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map = new HashMap<String, String>();

    /////////////////////////////////////////////////////////////////////////////////////
    // Pagination Variables

    public int currentPage = 1;
    public int displayPerPage = 6;  // Number of Albums displayed per Page
    public int indexRowStart = 0;
    public int TotalRows = 0;
    public Button btnNext;
    public Button btnPre;

/////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_albums);


        ///////////////// Preparing The Paginated GridView //////////////////////////

        galleryGridView = (GridView) findViewById(R.id.galleryGridView);
        // Next
        btnNext = (Button) findViewById(R.id.btnNext);
        // Perform action on click
        btnNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentPage = currentPage + 1;
                progressShowData();
            }
        });

        // Previous
        btnPre = (Button) findViewById(R.id.btnPre);
        // Perform action on click
        btnPre.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentPage = currentPage - 1;
                progressShowData();
            }
        });

        ///////////////// /////////////////////////////////////////////////

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (metrics.densityDpi / 160f);

        if(dp < 360)
        {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }


        Bundle parameters = new Bundle();
        parameters.putString("fields", "link,name,count");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),  //your fb AccessToken
                "/" + AccessToken.getCurrentAccessToken().getUserId() + "/albums" ,//user id of login user
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        try {


                            if (response.getError() == null) {

                                JSONObject joMain = response.getJSONObject();
                                Log.w("myTag", "convert GraphResponse response to JSONObject");
                                JSONArray jaData = null;
                                try {
                                    jaData = joMain.getJSONArray("data");
                                    Log.w("myTag", "find JSONArray from JSONObject");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (joMain.has("data")) {
                                    int j=jaData.length();


                                    for (int i = 0; i < jaData.length(); i++) {//find no. of album using jaData.length()
                                        JSONObject joAlbum = jaData.getJSONObject(i); //convert perticular album into JSONObject
                                        albumList.add(mappingInbox(joAlbum.getString("id"),joAlbum.getString("name"), joAlbum.getString("link"), joAlbum.getString("count")));
                                        Log.w("myTag", "The Album id is: " + joAlbum.getString("id"));
                                        Log.w("myTag", "The Album link is: " + joAlbum.getString("link"));
                                        Log.w("myTag", "The Album name is: " + joAlbum.getString("name"));
//
                                    }

                                    progressShowData();


                                }
                            } else {
                                Log.d("Test", response.getError().toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();}



    public static HashMap<String, String> mappingInbox(String album_id, String album_name, String album_path, String pic_count)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_ID, album_id);
        map.put(KEY_ALBUM, album_name);
        map.put(KEY_PATH, album_path);
        map.put(KEY_COUNT, pic_count);
        return map;
    }


    public void progressShowData() //Show the Albums stored inside albumList Array List
    {
        // Total Record
        TotalRows = albumList.size();

        // Start Index
        indexRowStart = ((displayPerPage*currentPage)-displayPerPage);

        int TotalPage = 0;
        if(TotalRows<=displayPerPage)
        {
            TotalPage =1;
        }
        else if((TotalRows % displayPerPage)==0)
        {
            TotalPage =(TotalRows/displayPerPage) ;
        }
        else
        {
            TotalPage =(TotalRows/displayPerPage)+1;
            TotalPage = (int)TotalPage;
        }

        // Disabled Button Next
        if(currentPage >= TotalPage)
        {
            btnNext.setEnabled(false);
        }
        else
        {
            btnNext.setEnabled(true);
        }

        // Disabled Button Previos
        if(currentPage <= 1)
        {
            btnPre.setEnabled(false);
        }
        else
        {
            btnPre.setEnabled(true);
        }
        AlbumAdapter adapter = new AlbumAdapter(FacebookAlbumsActivity.this, albumList);
        galleryGridView.setAdapter(adapter);
        galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                Intent intent = new Intent(FacebookAlbumsActivity.this, AlbumActivity.class);
                intent.putExtra("name", albumList.get(+position+indexRowStart).get("album_id"));
                startActivity(intent);
            }
        });

    }

    class AlbumAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<HashMap< String, String >> data;
        public AlbumAdapter(Activity a, ArrayList < HashMap < String, String >> d) {
            activity = a;
            data = d;
        }
        public int getCount() {//
            //  return data.size();
            if(displayPerPage > TotalRows - indexRowStart)
            {
                return TotalRows - indexRowStart;
            }
            else
            {
                return displayPerPage;
            }

        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumViewHolder holder = null;
            if (convertView == null) {
                holder = new AlbumViewHolder();
                convertView = LayoutInflater.from(activity).inflate(
                        R.layout.album_row, parent, false);

                holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);
                holder.gallery_count = (TextView) convertView.findViewById(R.id.gallery_count);
                holder.gallery_title = (TextView) convertView.findViewById(R.id.gallery_title);

                convertView.setTag(holder);
            } else {
                holder = (AlbumViewHolder) convertView.getTag();
            }
            holder.galleryImage.setId(position);
            holder.gallery_count.setId(position);
            holder.gallery_title.setId(position);

            HashMap < String, String > albm = new HashMap < String, String > ();
            albm = data.get(position+indexRowStart);

            try {
                holder.gallery_title.setText(albm.get(Function.KEY_ALBUM));
                holder.gallery_count.setText(albm.get(Function.KEY_COUNT));
                ImageView galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);


                GlideApp.with(activity)

                        .load(new File(albm.get("path")))
                        .into(galleryImage);


            } catch (Exception e) {}
            return convertView;
        }
    }


    class AlbumViewHolder {
        ImageView galleryImage;
        TextView gallery_count, gallery_title;
    }


}