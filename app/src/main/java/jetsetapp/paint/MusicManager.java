package jetsetapp.paint;

import java.util.Random;


class MusicManager {

    static boolean musicAlreadyPlayedAtBeginning;
    private static Random r = new Random();
    private static int numberOfSongs = MusicService.getPlayListLength();
    static int lastSong = r.nextInt(numberOfSongs);



}
