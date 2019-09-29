package jetsetapp.paint;

import android.content.ComponentCallbacks2;
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

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
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
    private ImageButton playMusicCatGalleryButton;

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

        playMusicCatGalleryButton = findViewById(R.id.playMusicCatGallery);
        playMusicCatGalleryButton.setOnClickListener(this);


//        check if the app is just started
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean firstStart = prefs.getBoolean("firstStart", true);
//        firstStart = true;
        if (prefs.getBoolean("firstStart", true)) {
            Intent musicService = new Intent(this, MusicService.class);
            musicService.setFlags(musicService.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);

            AudioManager manager = (AudioManager) this.getSystemService(getApplicationContext().AUDIO_SERVICE);
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
                Intent intentApp = new Intent(CatGallery.this, DogGallery.class);
//                Runtime.getRuntime().gc();
//                finishAffinity();
//                intentApp.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
//                intentApp .setFlags(intentApp .getFlags() | Intent.FLAG_ACTIVITY_SINGLE_TOP |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                CatGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.layout.activity_dog_gallery);
                Log.v("TAG", "dogsStart");
                break;

            case R.id.other:
                intentApp = new Intent(CatGallery.this, OtherGallery.class);
//                Runtime.getRuntime().gc();
//                finishAffinity();
                intentApp.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
//                intentApp .setFlags(intentApp .getFlags() | Intent.FLAG_ACTIVITY_SINGLE_TOP |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                CatGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.layout.activity_other_gallery);
                Log.v("TAG", "otherStart");
                break;

            case R.id.playMusicCatGallery:
                //check if music runs
                AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                if (manager != null) {
                    if (manager.isMusicActive()) {
                        Log.d("Music", "stop.");
                        stopService(new Intent(this, MusicService.class));
                        playMusicCatGalleryButton.setBackgroundResource(R.drawable.no_music);
                    } else {
                        Log.d("Music", "started.");
                        startService(new Intent(this, MusicService.class));
                        playMusicCatGalleryButton.setBackgroundResource(R.drawable.music);
                    }
                }

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

    public void onTrimMemory(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }
}


