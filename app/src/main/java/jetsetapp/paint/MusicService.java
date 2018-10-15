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
    static Foreground.Listener myListener = new Foreground.Listener() {

        public void onBecameForeground() {
            if (player != null) {
                player.start();
            }

        }

        public void onBecameBackground() {
            player.pause();
        }

    };
    int result = 0;
    private static int[] playList = {R.raw.ridehorse, R.raw.oldman_short, R.raw.dadyfinger};

//    AudioManager.OnAudioFocusChangeListener focusChangeListener =
//            new AudioManager.OnAudioFocusChangeListener() {
//                public void onAudioFocusChange(int focusChange) {
//                    AudioManager am =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
//                    switch (focusChange) {
//
//                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) :
//                            // Lower the volume while ducking.
//                            player.setVolume(0.2f, 0.2f);
//                            break;
//
//                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
//                            player.pause();
//                            break;
//
//                        case (AudioManager.AUDIOFOCUS_LOSS) :
//                            player.stop();
//                            player.release();
//                            break;
//
////                        case (AudioManager.AUDIOFOCUS_GAIN) :
////                            // Return the volume to normal and resume if paused.
//////                            player.setVolume(1f, 1f);
////                            player.start();
////                            break;
//                        default: break;
//                    }
//                }
//            };

    public void onCreate() {
        super.onCreate();

        lastSong++;

        currentSong = (lastSong % 3);
        Log.d("currentSong ", String.valueOf(lastSong % 3));
        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

//        result = manager.requestAudioFocus(focusChangeListener,
//// Use the music stream.
//                AudioManager.STREAM_MUSIC,
//// Request permanent focus.
//                AudioManager.AUDIOFOCUS_GAIN
//        );

//        if (!manager.isMusicActive()) {

        player = MediaPlayer.create(this, playList[currentSong]);
        player.setOnCompletionListener(this);
        Foreground.get(getApplication()).addListener(myListener);

//        }

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
        currentSong = (lastSong % 3);

        player = MediaPlayer.create(this, playList[currentSong]);
        player.setOnCompletionListener(this);
        player.start();

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Foreground.get(this).removeListener(myListener);
        player.stop();
        player.release();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
