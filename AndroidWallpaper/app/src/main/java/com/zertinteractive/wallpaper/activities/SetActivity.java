package com.zertinteractive.wallpaper.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.zertinteractive.wallpaper.R;

public class SetActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE_BIG = "com.zertinteractive.wallpaper.extraImageBig";
    private PublisherAdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

//        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_SEND);
//                Uri uri = Uri.parse(getIntent().getStringExtra(EXTRA_IMAGE_BIG));
//                intent.setDataAndType(uri, "image/*");
//                startActivity(intent);
//
////                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
////                intent.addCategory(Intent.CATEGORY_DEFAULT);
////                intent.setDataAndType(uri.setreso, "image/jpeg");
////                intent.putExtra("mimeType", "image/jpeg");
////                startActivity(Intent.createChooser(intent, "Set as:"));
//
////                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
////                startActivity(intent);
//
////                WallpaperManager myWallpaperManager = WallpaperManager
////                        .getInstance(getApplicationContext());
////                try {
//////            myWallpaperManager.setResource(R.mipmap.ic_launcher);
////                    URL url = new URL(tmp);
////                    Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
////                    myWallpaperManager.setBitmap(bitmap);
////                } catch (IOException e) {
////                    // TODO Auto-generated catch block
////                    e.printStackTrace();
////                }
//            }
//        });

        mAdView = (PublisherAdView) findViewById(R.id.ad_view_set);
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
