package jetsetapp.paint;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import static jetsetapp.paint.MusicManager.lastSong;


public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    protected static MediaPlayer player;
    public static final String TAG = CanvasView.class.getName();
    static AudioManager manager;
    private static boolean musicPaused = false;
    static Foreground.Listener myListener = new Foreground.Listener() {

        public void onBecameForeground() {
            if (player != null && musicPaused) {
                player.start();
                musicPaused = false;
            }

        }

        public void onBecameBackground() {

            if (manager.isMusicActive()) {
                try {
                    player.pause();
//                    player.release();
                    musicPaused = true;
//                    player.release();
                } catch (Exception exc) {
                    Log.e(TAG, "Music Listener threw exception!", exc);

                }
            }

        }

    };
    private static int[] playList = {R.raw.ridehorse, R.raw.oldman_short, R.raw.dadyfinger};

    public void onCreate() {
        super.onCreate();

        lastSong++;

        currentSong = (lastSong % playList.length);
        Log.d("currentSong ", String.valueOf(lastSong % playList.length));
        manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        player = MediaPlayer.create(this, playList[currentSong]);
        player.setOnCompletionListener(this);
        Foreground.get(getApplication()).addListener(myListener);

    }

    private int currentSong;

    public static int getPlayListLength() {
        return playList.length;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //to play all songs in order
        player.start(); // this starts the music when it appears on screen

        return START_STICKY;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        lastSong++;
        currentSong = (lastSong % playList.length);
        if (player != null) {
            player.reset();
            player.release();
        }
        player = MediaPlayer.create(this, playList[currentSong]);
        player.setOnCompletionListener(this);
        player.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Foreground.get(this).removeListener(myListener);
        player.reset();

//        player.stop();
        player.release();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
