package com.glens.speech.utils;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import ecRobot.openinteraction.SemanticUnderstandingInteraction;

public class WordsToSpeech {

    static String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/speech.mp3";

    static String managerPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/manager.mp3";



    public static boolean speech(String words) {
        boolean flag = true;
        try {
            SemanticUnderstandingInteraction.getInstance().speakToTTS(words,filePath);
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean speechManager(String words) {
        boolean flag = true;
        try {
            SemanticUnderstandingInteraction.getInstance().speakToTTS(words,managerPath);
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }


    public static void playMedia (final String path) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("tag", "播放完毕");
                    mp.reset();
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            });
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void playMedia2 (final String path) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d("tag", "播放完毕");
                    mp.reset();
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            });
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


}
