package com.example.rafik_000.appfacebook;

// This class Load Facebook album's Images and create a GrideView to show them

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.HashMap;

public class AlbumActivity extends AppCompatActivity {

    String album_id = "";
    GridView galleryGridView;
    static final String KEY_ALBUM_ID = "album_id";  //This will hold the Album ID
    static final String KEY_IMAGE = "image_id"; // This Variable holds the image path
    ArrayList<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>(); // It is holding the list of Images' paths in an Albums
    HashMap<String, String> map = new HashMap<String, String>();


    /////////////////////////////////////////////////////////////////////////////////////
    // Pagination Variables

    public int currentPage = 1;
    public int displayPerPage = 6;  // Display Perpage
    public int indexRowStart = 0;
    public int indexRowEnd = 0;
    public int TotalRows = 0;
    public GridView gridV;

    public Button btnNext;
    public Button btnPre;

/////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Intent intent = getIntent();
        album_id = intent.getStringExtra("name"); // Get the Album ID from FacebookAlbumActivity using Intent
        galleryGridView = (GridView) findViewById(R.id.galleryGridView);

        ///////////////// Preparing The Paginated GridView //////////////////////////

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

        /////////////////////////////////////////////////////////////////////

        ////////////// Setting UP the GridView for the Images/////////////////

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
        GetFacebookImages(album_id);

        /////////////////////////////////////////////////////////////////////////////////

    }


    public void GetFacebookImages(final String albumId) {

        Log.w("myAlb", "The Album id is: " + albumId);
        Bundle parameters = new Bundle();
        parameters.putString("fields", "images");
        parameters.putString("limit", "100");
        /* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + albumId + "/photos",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {



                        Log.w("mTAG", "Facebook Photos response: " + response);

                        try {
                            if (response.getError() == null) {
                                JSONObject joMain = response.getJSONObject();
                                JSONObject joAlbum = new JSONObject();

                                if (joMain.has("data")) {
                                    JSONArray jaData = joMain.optJSONArray("data");

                                    for (int a = 0; a < jaData.length(); a++){//Get no. of images {
                                        joAlbum = jaData.getJSONObject(a);
                                        JSONArray AlbumImages=joAlbum.getJSONArray("images"); //get images Array in JSONArray format
                                        String AlbumImageSurce = AlbumImages.getJSONObject(0).getString("source");
                                        imageList.add(mappingInbox(albumId,AlbumImageSurce));
                                        Log.w("myAlb", "The image sourse is: " + imageList.get(a).get("image_id"));
                                    }
                                    progressShowData();
                                }


                            } else {
                                Log.v("TAG", response.getError().toString());
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                }).executeAsync();
    }

    public static HashMap<String, String> mappingInbox(String album_id, String image_path)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_ALBUM_ID, album_id);
        map.put(KEY_IMAGE, image_path);
        return map;
    }



    public void progressShowData()
    {
        // Total Record
        TotalRows = imageList.size();

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
        SingleAlbumAdapter adapter = new SingleAlbumAdapter(AlbumActivity.this, imageList);
        galleryGridView.setAdapter(adapter);
        galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                Intent intent = new Intent(AlbumActivity.this, GalleryPreview.class);
                intent.putExtra("path", imageList.get(+position+indexRowStart).get("image_id"));
                startActivity(intent);
            }
        });

    }


    class SingleAlbumAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<HashMap< String, String >> data;
        public SingleAlbumAdapter(Activity a, ArrayList < HashMap < String, String >> d) {
            activity = a;
            data = d;
        }
        public int getCount() {
            //return data.size();
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
            SingleAlbumViewHolder holder = null;
            if (convertView == null) {
                holder = new SingleAlbumViewHolder();
                convertView = LayoutInflater.from(activity).inflate(
                        R.layout.single_album_row, parent, false);

                holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);

                convertView.setTag(holder);
            } else {
                holder = (SingleAlbumViewHolder) convertView.getTag();
            }
            holder.galleryImage.setId(position);

            HashMap < String, String > song = new HashMap < String, String > ();
            song = data.get(position+indexRowStart);
            try {

                GlideApp.with(activity)
                        .load(song.get("image_id")) // Uri of the picture
                        .into(holder.galleryImage);


            } catch (Exception e) {}
            return convertView;
        }
    }


    class SingleAlbumViewHolder {
        ImageView galleryImage;
    }
}