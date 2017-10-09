package com.blaire.snakegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

import static android.R.attr.keycode;

public class GameActivity extends Activity {
    Canvas canvas;
    SnakeView snakeView;
    Bitmap headBitmap;
    Bitmap tailBitmap;
    Bitmap bodyBitmap;
    Bitmap appleBitmap;

    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

    //0 = up, 1 = right, 2 = down, 3 = left
    int directionOfTravel = 0;

    int screenWidth;
    int screenHeight;
    int topGap;

    long lastFrameTime;
    int fps;
    int score;
    int high;

    int[] snakeX;
    int [] snakeY;
    int snakeLength;
    int appleX;
    int appleY;

    int blockSize;
    int numBlocksWide;
    int numBlocksHigh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSound();
        configureDisplay();
        snakeView = new SnakeView(this);
        setContentView(snakeView);
    }

    public void onResume() {
        super.onResume();
        snakeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        snakeView.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        snakeView.pause();
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_BACK){
            snakeView.pause();
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
            return true;
        }
        return false;
    }

    public void loadSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sample1 = soundPool.load(this, R.raw.sample1, 0);
        sample2 = soundPool.load(this,R.raw.sample2, 0);
        sample3 = soundPool.load(this, R.raw.sample3, 0);
        sample4 = soundPool.load(this, R.raw.sample4, 0);
    }

    public void configureDisplay(){
        Display display= getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenHeight = size.y;
        screenWidth =size.x;
        topGap =screenHeight/14;
        blockSize = screenWidth / 40;
        numBlocksWide = 40;
        numBlocksHigh = (screenHeight - topGap) / blockSize;

        headBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head);
        tailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tail);
        bodyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.body);
        appleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.apple);

        headBitmap = Bitmap.createScaledBitmap(headBitmap, blockSize, blockSize, false);
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap, blockSize, blockSize, false);
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap, blockSize, blockSize,false);
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap, blockSize, blockSize, false);
    }

    class SnakeView extends SurfaceView implements Runnable{
        Thread gameThread;
        SurfaceHolder ourholder;
        volatile  boolean playingSnake;
        Paint paint;

        public SnakeView(Context context){
            super(context);
            ourholder = getHolder();
            paint = new Paint();
            snakeX = new int[200];
            snakeY = new int [200];
            getSnake();
            getApple();
        }

        public void  getSnake(){
            snakeLength = 3;
            snakeX[0] = numBlocksWide / 2;
            snakeY[0] = numBlocksHigh / 2;

            snakeX[1] = snakeX[0] - 1;
            snakeY[1] = snakeY[0];

            snakeX[2] = snakeX[1] - 1;
            snakeY[2] = snakeY[0];
        }

        public void getApple(){
            Random random = new Random();
            appleX = random.nextInt(numBlocksWide - 1) + 1;
            appleY = random.nextInt(numBlocksHigh - 1) + 1;
        }

        @Override
        public void run(){
            while (playingSnake){
                updateGame();
                drawGame();
                controlFPS();
            }
        }

        public void  updateGame(){
            if(snakeX[0] == appleX && snakeY[0] == appleY){
                snakeLength++;
                getApple();
                score+= snakeLength;
                soundPool.play(sample1, 1, 1 , 0, 0, 1);
            }

            for(int i = snakeLength; i>0; i--){
                snakeX[i] = snakeX[i-1];
                snakeY[i] = snakeY[i-1];
            }

            switch(directionOfTravel){
                case 0: //up
                    snakeY[0]--;
                break;
                case 1: //right
                    snakeX[0]++;
                    break;
                case 2: //down
                    snakeY[0]++;
                    break;
                case 3: // left
                    snakeX[0]--;
                    break;
            }
            boolean isDead = false;
            if(snakeX[0] < 0 ) isDead = true;
            if(snakeX[0] >= numBlocksWide) isDead = true;
            if(snakeY[0] <  0) isDead = true;
            if(snakeY[0] >  numBlocksHigh) isDead = true;
            for(int i = snakeLength - 1; i > 0; i--){
                if(i>4 && (snakeX[0] == snakeX[i]) &&(snakeY[0] == snakeY[i]))
                {
                    isDead = true;
                }
            }
            if(isDead == true){
                soundPool.play(sample4,1, 1,0, 0, 1);
                score = 0;
                getSnake();
            }
        }

        public void drawGame(){
            if(ourholder.getSurface().isValid()){
                canvas = ourholder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(topGap / 2);
                canvas.drawText("Score: " + score + " High Score: " +  high, 10, topGap - 6, paint);
                paint.setStrokeWidth(3);
                canvas.drawLine(1, topGap, screenWidth -1, topGap, paint);
                canvas.drawLine(screenWidth - 1, topGap, screenWidth -1 , topGap + (numBlocksHigh * blockSize), paint );
                canvas.drawLine(screenWidth - 1, topGap + (numBlocksHigh * blockSize),
                        1 ,topGap + (numBlocksHigh * blockSize), paint);
                canvas.drawLine(1, topGap + (numBlocksHigh * blockSize), 1, topGap, paint);

                canvas.drawBitmap(headBitmap, snakeX[0] * blockSize, snakeY[0] * blockSize + topGap, paint);

                for(int i = 1; i < snakeLength - 1; i++){
                    canvas.drawBitmap(bodyBitmap, snakeX[i] * blockSize, snakeY[i] * blockSize + topGap, paint);
                }

                canvas.drawBitmap(tailBitmap, snakeX[snakeLength -1]  * blockSize,
                        snakeY[snakeLength - 1] * blockSize + topGap, paint);

                canvas.drawBitmap(appleBitmap, appleX * blockSize, (appleY * blockSize) + topGap, paint );
                ourholder.unlockCanvasAndPost(canvas);
            }
        }

        public void controlFPS(){
            long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
            long timeToSleep = 100 - timeThisFrame;
            if(timeThisFrame > 0){
                fps = (int)(1000/ timeThisFrame);
            }
            if(timeToSleep > 0){
                try {
                    Thread.sleep(timeToSleep);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            lastFrameTime = System.currentTimeMillis();
        }

        public void pause(){
            playingSnake = false;
            try {
                gameThread.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        public void resume(){
            playingSnake = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event){
            switch (event.getAction()){
                case MotionEvent.ACTION_UP:
                    if (event.getX() >= screenWidth / 2){
                        directionOfTravel++;
                        if(directionOfTravel ==4)
                            directionOfTravel = 0;

                    }else{
                        directionOfTravel--;
                        if(directionOfTravel == -1)
                            directionOfTravel = 3;
                        }
                    }
                    return true;
            }

    }
}
