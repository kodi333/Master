package jetsetapp.paint;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

public class OtherGallery extends AppCompatActivity implements View.OnClickListener {

    private static boolean pictureChosen = false;

    ImageView dogs;
    ImageView cats;
    ImageView other;
    private static String orgImageName = null;
    private Intent mainActivity;

    public static boolean isPictureChosen() {
        return pictureChosen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_gallery);

        dogs = (ImageButton) findViewById(R.id.dogs);
        cats = (ImageButton) findViewById(R.id.cats);
        other = (ImageButton) findViewById(R.id.other);

        dogs.setOnClickListener(this);
        cats.setOnClickListener(this);
        other.setOnClickListener(this);

        for (int i = 1; i <= 14; i++) {

            String overwrittenImageName = Save.getNameOfOverwrittenFile() + "princess" + i;
            orgImageName = "princess" + i;

            Log.i("orgImageName", "orgImageName " + orgImageName);
            Log.i("overwrittenImageName", "overwrittenImageName " + overwrittenImageName);

            File file = new File(Save.getFile_path() + "/" + overwrittenImageName + ".png");
            if (file.exists()) {

                int imageId = getResources().getIdentifier(orgImageName, "id", getPackageName());

                ImageView thumbPicture = findViewById(imageId);

                thumbPicture.setImageBitmap(BitmapFactory.decodeFile(Save.getFile_path() + "/" + overwrittenImageName + ".png"));
//        }

                Log.i("princessIdInt", "princessIdInt " + imageId);
            }
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.dogs:
                Intent intentApp = new Intent(OtherGallery.this,
                        DogGallery.class);
                OtherGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.id.dogs);
                Log.v("TAG", "dogsStart");
                break;

            case R.id.cats:
                intentApp = new Intent(OtherGallery.this,
                        CatGallery.class);
                OtherGallery.this.startActivity(intentApp);
                MainActivity.setCurrentLayout(R.id.cats);
                Log.v("TAG", "catsStart");
                break;

        }

    }

    public void setBackground(View v) {
        pictureChosen = true;
        ImageView x = (ImageView) v;
        String buttonId = String.valueOf(x.getTag());
//        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + Save.getNameOfFolder();

        mainActivity = new Intent(OtherGallery.this, MainActivity.class);
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
// super.onBackPressed();
    }
}
