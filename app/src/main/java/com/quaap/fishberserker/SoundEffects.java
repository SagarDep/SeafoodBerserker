package com.quaap.fishberserker;

/**
 * Created by tom on 2/5/17.
 * <p>
 * Copyright (C) 2017  tom
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;



import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class SoundEffects {

    private SoundPool mSounds;

    private int[] mBGMSongIds;
    private MediaPlayer mBGMPlayer;

    private float mBGMVol = 1;


    private Map<Integer,Integer> mSoundIds = new HashMap<>();

    private static final int GOODBING = 0;
    private static final int BADBING = 1;
    private static final int HIGHCLICK = 2;
    private static final int LOWCLICK = 3;
    private static final int BABA = 4;
    private static final int DRUMROLLHIT = 5;
    private static final int HIT = 6;

    private final int [] soundFiles = {

    };
    private final float [] soundVolumes = {
            .6f,
            .4f,
            .5f,
    };

    private SharedPreferences appPreferences;

    private volatile boolean mReady = false;

    private volatile boolean mMute = false;

    private Context mContext;

    public SoundEffects(final Context context) {
        mContext = context;
        if (Build.VERSION.SDK_INT>=21) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSounds = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(4)
                    .build();
        } else {
            mSounds = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        }
        appPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());


        final TypedArray songs = context.getResources().obtainTypedArray(R.array.songs);
        mBGMSongIds = new int[songs.length()];
        for (int i=0; i<songs.length(); i++) {
            mBGMSongIds[i] = songs.getResourceId(i, 0);
        }
        songs.recycle();

        Utils.async(new Runnable() {
            @Override
            public void run() {

                for (int i=0; i<soundFiles.length; i++) {
                    mSoundIds.put(i, mSounds.load(context, soundFiles[i],1));
                }
                mReady = true;
            }
        });
    }

    public int getBGMSongs() {
        return mBGMSongIds.length;
    }

    public void playBGMusic(final int which) {
        if (mBGMPlayer!=null) {
            mBGMPlayer.release();
        }

        Utils.async(new Runnable() {
            @Override
            public void run() {
                mBGMPlayer = MediaPlayer.create(mContext, mBGMSongIds[which]);
                mBGMPlayer.start();
            }
        });
    }

    public void pauseBGMusic() {
        if (mBGMPlayer!=null && mBGMPlayer.isPlaying()) {
            mBGMPlayer.pause();
        }
    }

    public void restartBGMusic() {
        pauseBGMusic();
        if (mBGMPlayer!=null) {
            mBGMPlayer.seekTo(0);
            mBGMPlayer.start();
        }
    }

    public void releaseBGM() {
        if (mBGMPlayer!=null) {
            pauseBGMusic();
            mBGMPlayer.release();
        }
    }

    public void setBGMusicVolume(float vol) {
        if (mBGMPlayer!=null) {
            mBGMVol = vol;
            mBGMPlayer.setVolume(mBGMVol, mBGMVol);
        }
    }

    public void setBGMusicVolumeTemp(float vol, long timeoutmillis) {
        setBGMusicVolume(vol);
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                setBGMusicVolume(mBGMVol);
                t.cancel();
            }
        }, timeoutmillis);
    }


    private boolean isReady() {
        return mReady;
    }

    public void setMute(boolean mute) {
        mMute = mute;
        if (mMute) pauseBGMusic();
    }

    public boolean isMuted() {
        return mMute;
    }

    private void play(int soundKey) {
        play(soundKey, 1);
    }
    private void play(int soundKey, float speed) {
        try {
            if (isReady() && !mMute && appPreferences.getBoolean("use_sound_effects", true)) {

                float vol = soundVolumes[soundKey] + getRandHundreth();
                mSounds.play(mSoundIds.get(soundKey), vol, vol, 1, 0, speed + getRandHundreth()/2);
            }
        } catch (Exception e) {
            Log.e("SoundEffects", "Error playing " + soundKey, e);
        }
    }

    public void playGood() {
        play(GOODBING);
    }

    public void playBad() {
        play(BADBING);
    }

    public void playHighClick() {
        play(HIGHCLICK);
    }

    public void playLowClick() {
        play(LOWCLICK);
    }

    public void playBaba() {
        play(BABA);
    }

    private float getRandHundreth() {
        return (float)((Math.random()-.5)/10);
    }
    public void playHit1() {
        play(HIT, 1);
    }
    public void playHit2() {
        play(HIT, 1.2f);
    }
    public void playHit3() {
        play(HIT, 1.5f);
    }


    public void release() {
        mSounds.release();
        releaseBGM();
    }


}