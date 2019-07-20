package jetsetapp.paint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static jetsetapp.paint.MusicManager.musicAlreadyPlayedAtBeginning;

public class CatGallery extends AppCompatActivity implements View.OnClickListener {

    private static boolean pictureChosen = false;
    private static boolean foreground = false;
    private ImageButton dogs;
    private ImageButton cats;
    private ImageButton other;
    private SharedPreferences prefs = null;
    private static String orgImageName = null;
    private Intent mainActivity;

    public static boolean isPictureChosen() {
        return pictureChosen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_gallery);

        dogs = findViewById(R.id.dogs);
        cats = findViewById(R.id.cats);
        other = findViewById(R.id.other);

        dogs.setOnClickListener(this);
        cats.setOnClickListener(this);
        other.setOnClickListener(this);

//        check if the app is just started
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean firstStart = prefs.getBoolean("firstStart", true);
//        firstStart = true;

        if (prefs.getBoolean("firstStart", true)) {
            Intent musicService = new Intent(this, MusicService.class);

            AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            try {
                foreground = new ForegroundCheckTask().execute(this).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (manager != null) {
                if (!manager.isMusicActive() && !musicAlreadyPlayedAtBeginning && foreground) {
                    Log.d("Music", "started.");
                    startService(musicService);

                    Foreground.get(getApplication()).addListener(MusicService.myListener);

                    musicAlreadyPlayedAtBeginning = true;
                }
            }
//            set firstTimeViewLoad to false
            prefs.edit().putBoolean("firstrun", false).apply();
        }

        //change CatGallery thumbnail to last saved
        //iterate thru all images
        for (int i = 1; i <= 15; i++) {

            String overwrittenImageName = Save.getNameOfOverwrittenFile() + "cat" + i;
            orgImageName = "cat" + i;

            Log.i("orgImageName", "orgImageName " + orgImageName);
            Log.i("overwrittenImageName", "overwrittenImageName " + overwrittenImageName);

            File file = new File(Save.getFile_path() + "/" + overwrittenImageName + ".png");
            if (file.exists()) {

                int imageId = getResources().getIdentifier(orgImageName, "id", getPackageName());

                ImageView thumbPicture = findViewById(imageId);

                thumbPicture.setImageBitmap(BitmapFactory.decodeFile(Save.getFile_path() + "/" + overwrittenImageName + ".png"));

                Log.i("catIdInt", "catIdInt " + imageId);
            }
        }


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.dogs:
                Intent intentApp = new Intent(CatGallery.this,
                        DogGallery.class);
                CatGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.id.dogs);
                Log.v("TAG", "dogsStart");
                break;

            case R.id.other:
                intentApp = new Intent(CatGallery.this,
                        OtherGallery.class);
                CatGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.id.other);
                Log.v("TAG", "otherStart");
                break;
        }

    }

    public void setBackground(View v) {
        pictureChosen = true;
        ImageView x = (ImageView) v;
        String buttonId = String.valueOf(x.getTag());

        mainActivity = new Intent(CatGallery.this, MainActivity.class);
//        jesli istnieje OverwrittenKidsPaint + buttonid wtedy putExtra("picture", "Overwritten" + buttoin
        File file = new File(Save.getFile_path(), Save.getNameOfOverwrittenFile() + buttonId + ".png");

        if (file.exists()) {
            mainActivity.putExtra("picture", Save.getNameOfOverwrittenFile() + buttonId);
            Log.i("Found", "File found : Overwritten" + buttonId);
        } else {
            mainActivity.putExtra("picture", buttonId);
            Log.i("Found", "File not found : Overwritten" + buttonId);
        }
        startActivity(mainActivity);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MusicService.class)); // this NEEDS to be here without it when you slide
//        away (destroy) the app the music still plays
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
