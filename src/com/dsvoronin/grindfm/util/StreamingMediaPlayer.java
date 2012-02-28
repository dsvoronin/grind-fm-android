package com.dsvoronin.grindfm.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Vector;

public class StreamingMediaPlayer {

    private static final String TAG = StreamingMediaPlayer.class.getSimpleName();

    private static int INTIAL_KB_BUFFER = 96 * 10 / 8;//assume 96kbps*10secs/8bits per byte

    final private String DOWNFILE = "downloadingMediaFile";


    final static public String AUDIO_MPEG = "audio/ogg";
    final static public String BITERATE_HEADER = "icy-br";
//    final private int BIT = 8;
//    final private int SECONDS = 30;

    private BufferedInputStream stream;

    private URL url;
    private URLConnection urlConn;

    private TextView textStreamed;

    private View playButton;

    private ProgressBar progressBar;

    //  Track for display by progressBar
    private int totalKbRead = 0;

    // Create Handler to call View updates on the main UI thread.
//    private final Handler handler = new Handler();

//    private MediaPlayer mediaPlayer;

    private File downloadingMediaFile;

//    private boolean isInterrupted;

    private Context context;

    private int counter = 0;

    private boolean stopping;
    Thread preparringthread;
    private boolean started;
    private int playedcounter;
    boolean waitingForPlayer;
    private Vector<MediaPlayer> mediaplayers;

    public StreamingMediaPlayer(Context context, TextView textStreamed, View playButton, ProgressBar progressBar) {
        this.context = context;
        this.textStreamed = textStreamed;
        this.playButton = playButton;
        this.progressBar = progressBar;

        downloadingMediaFile = new File(context.getCacheDir(), DOWNFILE + counter);
        downloadingMediaFile.deleteOnExit();

        setupVars();
    }

    /**
     * Progressivly download the media to a temporary location and update the MediaPlayer as new content becomes available.
     */
    public void startStreaming(final String mediaUrl) throws IOException {
        final String METHOD_TAG = TAG + ".StartStreaming";

//        int bitrate = 56;

//        sendMessage( PlayListTab.CHECKRIORITY );

//        sendMessage( PlayListTab.RAISEPRIORITY );

//        sendMessage( PlayListTab.START );

        try {
            url = new URL(mediaUrl);
            urlConn = url.openConnection();
            urlConn.setReadTimeout(1000 * 20);
            urlConn.setConnectTimeout(1000 * 5);

            String ctype = urlConn.getContentType();
            if (ctype == null) {
                ctype = "";
            } else {
                ctype = ctype.toLowerCase();
            }

            //See if we can handle this type
            Log.d(METHOD_TAG, "Content Type: " + ctype);
            if (ctype.contains(AUDIO_MPEG) || ctype.equals("")) {

                String temp = urlConn.getHeaderField(BITERATE_HEADER);
                Log.d(METHOD_TAG, "Bitrate: " + temp);

            } else {
                Log.e(METHOD_TAG, "Does not look like we can play this audio type: " + ctype);
                Log.e(METHOD_TAG, "Or we could not connect to audio");
//                sendMessage (PlayListTab.TROUBLEWITHAUDIO);
                stop();
                return;
            }
        } catch (IOException ioe) {
            Log.e(METHOD_TAG, "Could not connect to " + mediaUrl);
//            sendMessage( PlayListTab.TROUBLEWITHAUDIO);
            stop();
            return;
        }


        Log.d(METHOD_TAG, "Setup incremental stream");
        //Lets Start Streaming by downloading parts of the stream and playing it in pieces
        //Set up buffer size
        //Assume XX kbps * XX seconds / 8 bits per byte
//        INTIAL_KB_BUFFER = bitrate * SECONDS / BIT;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    downloadAudioIncrement(mediaUrl);
                } catch (IOException e) {
                    Log.e(METHOD_TAG, "Unable to initialize the MediaPlayer for Audio Url = " + mediaUrl, e);
//                        sendMessage( PlayListTab.TROUBLEWITHAUDIO);
                    stop();
                    return;
                }
            }
        };
        Thread t = new Thread(r);
        //t.setDaemon(true);
        t.start();

    }


    /**
     * Download the url stream to a temporary location and then call the setDataSource
     * for that local file
     */
    public void downloadAudioIncrement(String mediaUrl) throws IOException {
        String METHOD_TAG = TAG + ".downloadAudioIncrement";

        // start tracing to "/sdcard/mynpr.trace"
        //URLConnection cn = new URL(mediaUrl).openConnection();
        //cn.setConnectTimeout(1000 * 30);
        //cn.setReadTimeout(1000 * 15);
        //cn.connect();
        //int bufsizeForDownload = 8 * 1024;
        int bufsizeForDownload = 1024;
        int bufsizeForfile = 64 * 1024;
        stream = new BufferedInputStream(urlConn.getInputStream(), bufsizeForDownload);
        //stream =  urlConn.getInputStream() ;
        if (stream == null) {
            Log.e(METHOD_TAG, "Unable to create InputStream for mediaUrl: " + mediaUrl);
//            sendMessage( PlayListTab.TROUBLEWITHAUDIO);
            stop();
        }

        Log.d(METHOD_TAG, "File name: " + downloadingMediaFile);
        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(downloadingMediaFile), bufsizeForfile);

        byte buf[] = new byte[bufsizeForDownload];
        int totalBytesRead = 0, incrementalBytesRead = 0, numread = 0;

        if (stopping == true) {
            stream = null;
            Log.d(METHOD_TAG, "null out stream ");
        }
        do {
            if (bout == null) {
                counter++;
                Log.d(METHOD_TAG, "FileOutputStream is null, Create new one: " + DOWNFILE + counter);
                downloadingMediaFile = new File(context.getCacheDir(), DOWNFILE + counter);
                downloadingMediaFile.deleteOnExit();
                bout = new BufferedOutputStream(new FileOutputStream(downloadingMediaFile), bufsizeForfile);
            }

            try {
                //Log.v(TAG, "read stream");
                numread = stream.read(buf);
            } catch (IOException e) {
                Log.e(METHOD_TAG, e.toString());
                Log.d(METHOD_TAG, "Bad read. Let's quit.");
//                sendMessage( PlayListTab.TROUBLEWITHAUDIO);
                stop();
                /*if (stream != null){
                       Log.d(TAG, "Bad read. Let's try to reconnect to source and continue downloading");
                       urlConn = new URL(mediaUrl).openConnection();
                       urlConn.setConnectTimeout(1000 * 30);
                       urlConn.connect();
               stream = urlConn.getInputStream();
               numread = stream.read(buf);
               } */
            } catch (NullPointerException e) {
                //Let's get out of here
                break;
            }

            if (numread < 0) {
                //We got something weird. Let's get out of here.
                Log.e(METHOD_TAG, "Bad read from stream. We got some number less than 0: " + numread + " Let's quit");
//                sendMessage( PlayListTab.TROUBLEWITHAUDIO);
                stop();
                break;

            } else if (numread >= 1) {
                //Log.v(TAG, "write to file");
                bout.write(buf, 0, numread);

                totalBytesRead += numread;
                incrementalBytesRead += numread;
                totalKbRead = totalBytesRead / 1000;
            }

            if (totalKbRead >= INTIAL_KB_BUFFER && stopping != true) {
//                sendMessage( PlayListTab.CHECKRIORITY );
                Log.v(METHOD_TAG, "Reached Buffer amount we want: " + "totalKbRead: " + totalKbRead + " INTIAL_KB_BUFFER: " + INTIAL_KB_BUFFER);
                bout.flush();
                bout.close();

                bout = null;

                setupplayer(downloadingMediaFile);
                totalBytesRead = 0;

            }

        } while (stream != null);
        Log.d(METHOD_TAG, "Done with streaming");
        // stop tracing
    }

    private void setupVars() {
        totalKbRead = 0;
        counter = 0;
        playedcounter = 0;
        mediaplayers = new Vector<MediaPlayer>(3);
        started = false;
        stopping = false;
        preparringthread = null;
        waitingForPlayer = false;
    }

    /**
     * Set Up player(s)
     */
    private void setupplayer(File partofaudio) {
        final String METHOD_TAG = TAG + ".setupplayer";

        final File f = partofaudio;
        Log.d(TAG, "File " + f.getAbsolutePath());

        Runnable r = new Runnable() {
            public void run() {

                MediaPlayer mp = new MediaPlayer();
                try {

                    MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            String LISTENER_TAG = METHOD_TAG + ".MediaPlayer.OnCompletionListener";
                            Log.d(LISTENER_TAG, "Start");
                            waitingForPlayer = false;
                            boolean leave = false;

                            if (stopping) {
                                //we should get out of here since it is time to leave
                                leave = true;
                            }

                            long timeInMilli = Calendar.getInstance().getTime().getTime();
                            long timeToQuit = (1000 * 30) + timeInMilli; //add 30 seconds
                            if (mediaplayers.size() <= 1 && stopping == false) {
                                Log.d(LISTENER_TAG, "waiting for another mediaplayer");
                                waitingForPlayer = true;
                            }
                            while (mediaplayers.size() <= 1 && leave == false) {

                                if (timeInMilli > timeToQuit) {
                                    //time to get out of here
                                    Log.e(LISTENER_TAG, "Timeout occured waiting for another media player");
                                    leave = true;
                                }
                                timeInMilli = Calendar.getInstance().getTime().getTime();
                            }
                            if (waitingForPlayer == true) {
                                //we must have been waiting
                                waitingForPlayer = false;
                            }
                            if (leave == false) {
                                MediaPlayer mp2 = mediaplayers.get(1);
                                mp2.start();
                                Log.d(LISTENER_TAG, "Start another player.");

                                mp.release();
                                mediaplayers.remove(mp);
                                removefile();
                            } else {

                            }

                        }
                    };

                    FileInputStream ins = new FileInputStream(f);
                    Log.d(METHOD_TAG, "File length = " + f.length());

                    mp.setDataSource(ins.getFD());
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    Log.d(METHOD_TAG, "Setup player completion listener");
                    mp.setOnCompletionListener(listener);

                    if (stopping) {
                        //The process is stopping. Let's get out of here
                        return;
                    }

                    Log.d(METHOD_TAG, "Prepare Media Player " + f);
                    if (started == false || waitingForPlayer == true) {
                        Log.d(METHOD_TAG, "Prepare synchronously.");
                        mp.prepare();
                    } else {
                        //This will save us a few more seconds
                        Log.d(METHOD_TAG, "Prepare Asynchronously.");
                        mp.prepareAsync();
                    }

                    mediaplayers.add(mp);

                    if (started == false) {
                        Log.d(METHOD_TAG, "Start Media Player " + f);
                        startMediaPlayer();
                    }

                } catch (FileNotFoundException e) {
                    Log.e(METHOD_TAG, e.toString(), e);
                    Log.e(METHOD_TAG, "Can't find file. Android must have deleted it on a clean up :-(");
                } catch (IllegalStateException e) {
                    Log.e(METHOD_TAG, e.toString(), e);
                } catch (IOException e) {
                    Log.e(METHOD_TAG, e.toString(), e);
                }


            }
        };

        preparringthread = new Thread(r);
        preparringthread.start();

        // Wait indefinitely for the thread to finish
        if (!started) {
            try {
                Log.d(METHOD_TAG, "Start and wait for first audio clip to be prepared.");
                preparringthread.join();
                // Finished
            } catch (InterruptedException e) {
                // Thread was interrupted
            }
        }

    }

    //Stop Audio
    public void stop() {
        final String METHOD_TAG = TAG + ".STOP";
        Log.d(METHOD_TAG, "Entry");

        stopping = true;

        try {

            if (mediaplayers != null) {
                if (!mediaplayers.isEmpty()) {
                    final MediaPlayer mp = mediaplayers.get(0);
                    if (mp.isPlaying()) {
                        Log.d(METHOD_TAG, "Stop Player");
                        Runnable r = new Runnable() {
                            public void run() {
                                mp.stop();
                            }
                        };
                        Thread t = new Thread(r);
                        t.start();
                    }
                }
            }
            Runnable r = new Runnable() {
                public void run() {
                    if (stream != null) {
                        Log.d(METHOD_TAG, "Close stream");
                        try {
                            stream.close();
                            Log.d(METHOD_TAG, "Done Closing stream");
                        } catch (IOException e) {
                            Log.e(METHOD_TAG, "error closing open connection");
//                            sendMessage(PlayListTab.STOP);
                        }
                    }
                    stream = null;
                }
            };
            stream = null;
            //closing the stream may take a few seconds, we will run this in a thread.
            //Thread t = new Thread(r);
            //t.start();


        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(METHOD_TAG, "No items in Media player List");
//            sendMessage(PlayListTab.STOP);
        } /*catch (IOException e) {
                Log.e(TAG, "error closing open connection");
                sendMessage(PlayListTab.STOP);
        } */
    }

    //Removed file from cache
    private void removefile() {
        String METHOD_TAG = TAG + ".removefile";
        File temp = new File(context.getCacheDir(), DOWNFILE + playedcounter);
        Log.d(METHOD_TAG, temp.getAbsolutePath());
        temp.delete();
        playedcounter++;
    }

//    private boolean validateNotInterrupted() {
//        if (isInterrupted) {
//            if (mediaPlayer != null) {
//                mediaPlayer.pause();
//                //mediaPlayer.release();
//            }
//            return false;
//        } else {
//            return true;
//        }
//    }


//    /**
//     * Test whether we need to transfer buffered data to the MediaPlayer.
//     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
//     */
//    private void testMediaBuffer() {
//        Runnable updater = new Runnable() {
//            public void run() {
//                if (mediaPlayer == null) {
//                    //  Only create the MediaPlayer once we have the minimum buffered data
//                    if (totalKbRead >= INTIAL_KB_BUFFER) {
//                        try {
//                            startMediaPlayer();
//                        } catch (Exception e) {
//                            Log.e(getClass().getName(), "Error copying buffered conent.", e);
//                        }
//                    }
//                } else if (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000) {
//                    //  NOTE:  The media player has stopped at the end so transfer any existing buffered data
//                    //  We test for < 1second of data because the media player can stop when there is still
//                    //  a few milliseconds of data left to play
//                    transferBufferToMediaPlayer();
//                }
//            }
//        };
//        handler.post(updater);
//    }

//    private void startMediaPlayer() {
//        try {
//            File bufferedFile = new File(context.getCacheDir(), "playingMedia" + (counter++) + ".dat");
//
//            // We double buffer the data to avoid potential read/write errors that could happen if the
//            // download thread attempted to write at the same time the MediaPlayer was trying to read.
//            // For example, we can't guarantee that the MediaPlayer won't open a file for playing and leave it locked while
//            // the media is playing.  This would permanently deadlock the file download.  To avoid such a deadloack,
//            // we move the currently loaded data to a temporary buffer file that we start playing while the remaining
//            // data downloads.
//            moveFile(downloadingMediaFile, bufferedFile);
//
//            Log.e(getClass().getName(), "Buffered File path: " + bufferedFile.getAbsolutePath());
//            Log.e(getClass().getName(), "Buffered File length: " + bufferedFile.length() + "");
//
//            mediaPlayer = createMediaPlayer(bufferedFile);
//
//            // We have pre-loaded enough content and started the MediaPlayer so update the buttons & progress meters.
//            mediaPlayer.start();
//            startPlayProgressUpdater();
//        } catch (IOException e) {
//            Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);
//            return;
//        }
//    }

    //Start first audio clip
    private void startMediaPlayer() {
        String METHOD_TAG = TAG + ".startMediaPlayer";

        //Grab out first media player
        started = true;
        MediaPlayer mp = mediaplayers.get(0);
        Log.d(METHOD_TAG, "Start Player");
        mp.start();

//        sendMessage(PlayListTab.STOPSPIN);
    }

//    private MediaPlayer createMediaPlayer(File mediaFile)
//            throws IOException {
//        MediaPlayer mPlayer = new MediaPlayer();
//        mPlayer.setOnErrorListener(
//                new MediaPlayer.OnErrorListener() {
//                    public boolean onError(MediaPlayer mp, int what, int extra) {
//                        Log.e(getClass().getName(), "Error in MediaPlayer: (" + what + ") with extra (" + extra + ")");
//                        return false;
//                    }
//                });
//
//        //  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
//        //  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
//        //  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.
//        FileInputStream fis = new FileInputStream(mediaFile);
//        mPlayer.setDataSource(fis.getFD());
//        mPlayer.prepare();
//        return mPlayer;
//    }

//    /**
//     * Transfer buffered data to the MediaPlayer.
//     * NOTE: Interacting with a MediaPlayer on a non-main UI thread can cause thread-lock and crashes so
//     * this method should always be called using a Handler.
//     */
//    private void transferBufferToMediaPlayer() {
//        try {
//            // First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause
//            boolean wasPlaying = mediaPlayer.isPlaying();
//            int curPosition = mediaPlayer.getCurrentPosition();
//
//            // Copy the currently downloaded content to a new buffered File.  Store the old File for deleting later.
//            File oldBufferedFile = new File(context.getCacheDir(), "playingMedia" + counter + ".dat");
//            File bufferedFile = new File(context.getCacheDir(), "playingMedia" + (counter++) + ".dat");
//
//            //  This may be the last buffered File so ask that it be delete on exit.  If it's already deleted, then this won't mean anything.  If you want to
//            // keep and track fully downloaded files for later use, write caching code and please send me a copy.
//            bufferedFile.deleteOnExit();
//            moveFile(downloadingMediaFile, bufferedFile);
//
//            // Pause the current player now as we are about to create and start a new one.  So far (Android v1.5),
//            // this always happens so quickly that the user never realized we've stopped the player and started a new one
//            mediaPlayer.pause();
//
//            // Create a new MediaPlayer rather than try to re-prepare the prior one.
//            mediaPlayer = createMediaPlayer(bufferedFile);
//            mediaPlayer.seekTo(curPosition);
//
//            //  Restart if at end of prior buffered content or mediaPlayer was previously playing.
//            //	NOTE:  We test for < 1second of data because the media player can stop when there is still
//            //  a few milliseconds of data left to play
//            boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
//            if (wasPlaying || atEndOfFile) {
//                mediaPlayer.start();
//            }
//
//            // Lastly delete the previously playing buffered File as it's no longer needed.
//            oldBufferedFile.delete();
//
//        } catch (Exception e) {
//            Log.e(getClass().getName(), "Error updating to newly loaded content.", e);
//        }
//    }

//    private void fireDataLoadUpdate() {
//        Runnable updater = new Runnable() {
//            public void run() {
//                textStreamed.setText((totalKbRead + " Kb read"));
//                float loadProgress = ((float) totalKbRead / (float) mediaLengthInKb);
//                progressBar.setSecondaryProgress((int) (loadProgress * 100));
//            }
//        };
//        handler.post(updater);
//    }
//
//    private void fireDataFullyLoaded() {
//        Runnable updater = new Runnable() {
//            public void run() {
//                transferBufferToMediaPlayer();
//
//                // Delete the downloaded File as it's now been transferred to the currently playing buffer file.
//                downloadingMediaFile.delete();
//                textStreamed.setText(("Audio full loaded: " + totalKbRead + " Kb read"));
//            }
//        };
//        handler.post(updater);
//    }

//    public MediaPlayer getMediaPlayer() {
//        return mediaPlayer;
//    }

//    public void startPlayProgressUpdater() {
//        float progress = (((float) mediaPlayer.getCurrentPosition() / 1000) / mediaLengthInSeconds);
//        progressBar.setProgress((int) (progress * 100));
//
//        if (mediaPlayer.isPlaying()) {
//            Runnable notification = new Runnable() {
//                public void run() {
//                    startPlayProgressUpdater();
//                }
//            };
//            handler.postDelayed(notification, 1000);
//        }
//    }

//    public void interrupt() {
//        playButton.setEnabled(false);
//        isInterrupted = true;
//        validateNotInterrupted();
//    }

//    /**
//     * Move the file in oldLocation to newLocation.
//     */
//    public void moveFile(File oldLocation, File newLocation)
//            throws IOException {
//
//        if (oldLocation.exists()) {
//            BufferedInputStream reader = new BufferedInputStream(new FileInputStream(oldLocation));
//            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newLocation, false));
//            try {
//                byte[] buff = new byte[8192];
//                int numChars;
//                while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
//                    writer.write(buff, 0, numChars);
//                }
//            } catch (IOException ex) {
//                throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
//            } finally {
//                try {
//                    if (reader != null) {
//                        writer.close();
//                        reader.close();
//                    }
//                } catch (IOException ex) {
//                    Log.e(getClass().getName(), "Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
//                }
//            }
//        } else {
//            throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
//        }
//    }
}
