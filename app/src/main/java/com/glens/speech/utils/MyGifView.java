package com.glens.speech.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import com.glens.speech.R;

/**
 * 自定义实现gif动画
 */
public class MyGifView extends View {
    private long movieStart;
    private Movie movie;

    //重写该构造方法
    public MyGifView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        //以文件流（InputStream）读取进gif图片资源
        movie = Movie.decodeStream(getResources().openRawResource(R.drawable.sound_recording_press));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        long curTime = android.os.SystemClock.uptimeMillis();
        //第一次播放
        if (movieStart == 0) {
            movieStart = curTime;
        }
        if (movie != null) {
            int duraction = movie.duration();
            int relTime = (int) ((curTime - movieStart) % duraction);
            movie.setTime(relTime);
            movie.draw(canvas, 0, 0);
            //强制重绘
            invalidate();
        }
        super.onDraw(canvas);
    }
}