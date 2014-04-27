package com.cb.test.settings;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.cb.test.R;

import java.io.File;

/**
 * Customer UI to play video
 *
 * @author Justin Chen
 *
 * question:
 * 1. SeekBar and VideoView cannot return to beginning at the state of stop.
 * 2.
 */

public class CustomerActivity extends Activity
{

    private final static String TAG = "CustomerActivity";

    private Button mPlayBtn, mPauseBtn, mStopBtn, mReplayBtn;

    private SeekBar mSeekBar;

    private VideoView mVideoView;

    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_layout);

        initViews();
    }

    private void initViews()
    {
        initVideo();

        mPlayBtn = (Button) findViewById(R.id.play);
        mPauseBtn = (Button) findViewById(R.id.pause);
        mStopBtn = (Button) findViewById(R.id.stop);
        mReplayBtn = (Button) findViewById(R.id.replay);

        mPlayBtn.setOnClickListener(listener);
        mPauseBtn.setOnClickListener(listener);
        mStopBtn.setOnClickListener(listener);
        mReplayBtn.setOnClickListener(listener);
    }

    private void initVideo()
    {
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);

        mVideoView = (VideoView) findViewById(R.id.video_view);

    }

    private View.OnClickListener listener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.play:
                    play();
                    break;
                case R.id.pause:
                    pause();
                    break;
                case R.id.stop:
                    stop();
                    break;
                case R.id.replay:
                    replay();
                    break;
            }
        }

    };

    private void play()
    {
        File file = new File("/sdcard/s.3gp");
        if (!file.exists())
        {
            Toast.makeText(this, "File is not existed!", Toast.LENGTH_LONG).show();
            return;
        }

        mPlayBtn.setEnabled(false);
        mPauseBtn.setEnabled(true);
        mStopBtn.setEnabled(true);
        mReplayBtn.setEnabled(true);

        mVideoView.setVideoPath(file.getAbsolutePath());

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer)
            {
                mVideoView.start();
                // getDuration returns right while calling VideoView.start()
                // firstly, or it will return -1
                int duration = mVideoView.getDuration();

                asyncSeekBar(duration);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                mSeekBar.setProgress(0);
                mPlayBtn.setEnabled(true);
                mPauseBtn.setEnabled(false);
                mStopBtn.setEnabled(false);
                mReplayBtn.setEnabled(false);
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2)
            {
                mSeekBar.setProgress(0);
                replay();
                return false;
            }
        });
    }

    public void asyncSeekBar(final int duration)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                isPlaying = true;

                while (isPlaying)
                {
                    if (mVideoView.isPlaying())
                    {
                        mSeekBar.setProgress(mVideoView.getCurrentPosition() * 100 / duration);
                    }

                    try
                    {
                        sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void pause()
    {
        if (mPauseBtn.getText().equals("pause"))
        {
            mVideoView.pause();
            mPauseBtn.setText(getResources().getString(R.string._continue));
        }
        else
        {
            mVideoView.start();
            mPauseBtn.setText(getResources().getString(R.string.pause));
        }
    }

    private void stop()
    {
        if (mVideoView != null && mVideoView.isPlaying())
        {
            mVideoView.stopPlayback();
            mVideoView.seekTo(0); // doesn't work, any method that can make
                                  // VideoView return to beginning?

            mPlayBtn.setEnabled(true);
            mPauseBtn.setEnabled(false);
            mPauseBtn.setText(getResources().getString(R.string.pause));
            mStopBtn.setEnabled(false);
            mReplayBtn.setEnabled(false);

            isPlaying = false;
        }
    }

    private void replay()
    {
        if (mPauseBtn.getText().equals("continue"))
            mPauseBtn.setText(getResources().getString(R.string.pause));

        play();
    }
}
