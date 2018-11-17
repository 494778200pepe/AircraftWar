package com.jcoder.aircraftwar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wang
 * @date 2018/11/17.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable,View.OnTouchListener {

    private int mWidth, mHeight;
    private Bitmap bg, aircraft, enemy, bullet, explode;
    private ArrayList<GameImage> gameImages = new ArrayList<>();

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }

    private boolean runState = false;
    private SurfaceHolder holder = null;
    private Bitmap gameBitmap = null;
    private Thread thread;

    @Override
    public void run() {
        Paint paint = new Paint();
        try {
            while (runState) {
                Canvas canvas = holder.lockCanvas();
                // 也可以锁住一定的区域：holder.lockCanvas(new Rect(0,0,100,100));
                Canvas canvas1 = new Canvas(gameBitmap);
                for (int i = 0; i < gameImages.size(); i++) {
                    GameImage gameImage = gameImages.get(i);
                    canvas1.drawBitmap(gameImage.getBitmap(), gameImage.getX(), gameImage.getY(), paint);
                }
                canvas.drawBitmap(gameBitmap, 0, 0, paint);
                holder.unlockCanvasAndPost(canvas);
                Thread.sleep(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean aircraftSelected = false;
    private int downX, downY;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("pepe", "ACTION_DOWN");
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                Log.d("pepe", "x = " + x);
                Log.d("pepe", "mAircraft.getX() = " + mAircraft.getX());
                Log.d("pepe", "mAircraft.getX() + aircraft.getWidth() / 4 = " + (mAircraft.getX() + aircraft.getWidth() / 4));

                Log.d("pepe", "y = " + y);
                Log.d("pepe", "mAircraft.getY() = " + mAircraft.getY());
                Log.d("pepe", "mAircraft.getY() + aircraft.getHeight() = " + (mAircraft.getY() + aircraft.getHeight()));
                if (x >= mAircraft.getX() && x < mAircraft.getX() + aircraft.getWidth() / 4 && y >= mAircraft.getY() && y < mAircraft.getY() + aircraft.getHeight()) {
                    aircraftSelected = true;
                    downX = x;
                    downY = y;
                    Log.d("pepe", "选中");
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(aircraftSelected){
                    int moveX = (int) motionEvent.getX();
                    int moveY = (int) motionEvent.getY();

                    mAircraft.setX(mAircraft.getX() + (moveX - downX));
                    mAircraft.setY(mAircraft.getY() + (moveY - downY));
                    downX = moveX;
                    downY = moveY;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                aircraftSelected = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
        init();
        this.holder = surfaceHolder;
        runState = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        runState = false;
    }

    private Aircraft mAircraft;

    private void init() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        gameBitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
        bg = BitmapFactory.decodeResource(getResources(), R.mipmap.bg);
        aircraft = BitmapFactory.decodeResource(getResources(), R.mipmap.aircraft);
        enemy = BitmapFactory.decodeResource(getResources(), R.mipmap.enemy);
        bullet = BitmapFactory.decodeResource(getResources(), R.mipmap.bullet);
        explode = BitmapFactory.decodeResource(getResources(), R.mipmap.explode);
        gameImages.add(new Beijing(bg));
        mAircraft = new Aircraft(aircraft);
        gameImages.add(mAircraft);
    }

    private interface GameImage {
        Bitmap getBitmap();

        int getX();

        int getY();
    }

    private class Aircraft implements GameImage {
        private Bitmap aircraft;
        private int x, y;
        private int index = 0;
        private int num;

        List<Bitmap> list = new ArrayList<>();

        private Aircraft(Bitmap aircraft) {
            this.aircraft = aircraft;
            int width = this.aircraft.getWidth();
            int height = this.aircraft.getHeight();
            for (int i = 0; i < 4; i++) {
                list.add(Bitmap.createBitmap(this.aircraft, i * (width / 4), 0, width / 4, height));
            }
            this.x = (mWidth - aircraft.getWidth() / 4) / 2;
            this.y =  mHeight - aircraft.getHeight() -10;
        }

        @Override
        public Bitmap getBitmap() {
            Bitmap bitmap = list.get(index % list.size());
            num++;
            if (num % 5 == 0) {
                index++;
            }
            return bitmap;
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }
    }


    private class Beijing implements GameImage {
        private Bitmap bg;
        private int h;

        private Beijing(Bitmap bg) {
            this.bg = bg;
            newBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        }

        private Bitmap newBitmap = null;

        @Override
        public Bitmap getBitmap() {
            Paint paint = new Paint();
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(bg, new Rect(0, 0, bg.getWidth(), bg.getHeight()),
                    new RectF(0, h, mWidth, mHeight + h), paint);

            canvas.drawBitmap(bg, new Rect(0, 0, bg.getWidth(), bg.getHeight()),
                    new RectF(0, -mHeight + h, mWidth, h), paint);
            h += 3;
            if (h >= mHeight) {
                h = 0;
            }
            return newBitmap;
        }

        @Override
        public int getX() {
            return 0;
        }

        @Override
        public int getY() {
            return 0;
        }
    }

}
