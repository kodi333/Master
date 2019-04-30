package jetsetapp.paint;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

import static jetsetapp.paint.MusicManager.musicAlreadyPlayedAtBegining;

public class CatGallery extends AppCompatActivity implements View.OnClickListener {

    private static boolean pictureChosen = false;

    ImageButton dogs;
    ImageButton cats;
    ImageButton other;

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

        Intent musicService = new Intent(this, MusicService.class);

        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            if (!manager.isMusicActive() && !musicAlreadyPlayedAtBegining) {
                Log.d("Music", "started.");
                startService(musicService);
                Foreground.get(getApplication()).addListener(MusicService.myListener);

                musicAlreadyPlayedAtBegining = true;
            }
        }

        //change CatGallery thumbnail to last saved
        //iterate thru all cats
        for (int i = 1; i <= 14; i++) {

            String overwrittenImageName = Save.getNameOfOverwrittenFile() + "cat" + i;
            String imageName = "cat" + i;

            Log.i("imageName", "imageName " + imageName);
            Log.i("overwrittenImageName", "overwrittenImageName " + overwrittenImageName);

            File file = new File(Save.getFile_path() + "/" + overwrittenImageName + ".png");
            if (file.exists()) {

                int imageId = getResources().getIdentifier(imageName, "id", getPackageName());
//                int imageId = R.id.cat2; //2131165306 2131099746
//            String catId = "R.id." + "cat" + i;
//            int catIdInt = R.id + Integer.parseInt(catId);
//            ImageView catPicture = findViewById(catIdInt);
                ImageView thumbPicture = findViewById(imageId);

//                thumbPicture.setImageBitmap(BitmapFactory.decodeFile(Save.getFile_path()+ "/"
//                        + overwrittenImageName + ".png"));
                thumbPicture.setImageBitmap(BitmapFactory.decodeFile(Save.getFile_path() + "/" + overwrittenImageName + ".png"));
//        }

                Log.i("catIdInt", "catIdInt " + imageId);
            }
        }


//        ImageView catPicture = findViewById(R.id.cat2);
//        File file = new File(Save.getFile_path()+ "/" + "Overwrittencat2.png" );
//        if(file.exists()) {
//            catPicture.setImageBitmap(BitmapFactory.decodeFile(Save.getFile_path()+ "/" + "Overwrittencat2.png"));
//        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.dogs:
                Intent intentApp = new Intent(CatGallery.this,
                        DogGallery.class);
                CatGallery.this.startActivity(intentApp);
                Log.v("TAG", "dogsStart");
                break;

            case R.id.other:
                intentApp = new Intent(CatGallery.this,
                        OtherGallery.class);
                CatGallery.this.startActivity(intentApp);
                Log.v("TAG", "otherStart");
                break;
        }

    }

    public void setBackground(View v) {
        pictureChosen = true;
        ImageView x = (ImageView) v;
        String buttonId = String.valueOf(x.getTag());
//        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + Save.getNameOfFolder();

        Intent mainActivity = new Intent(CatGallery.this, MainActivity.class);
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
