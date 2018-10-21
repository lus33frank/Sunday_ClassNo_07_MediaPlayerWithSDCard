package tw.com.changchinghsiang.sunday_classno_07_mediaplayerwithsdcard;

import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 0;

    private SeekBar seekBar;
    private TextView tvShow;

    private MediaPlayer mediaPlayer;
    private File dir;
    private String[] musicList;
    private int fileRandom;
    private Timer timer;
    private String strTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findviewer();
    }

    private void findviewer() {
        tvShow = (TextView) findViewById(R.id.textView);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Android 6 以上檢查權限
        if (Build.VERSION.SDK_INT >= 23){
            //外存
            int permission_SDCard = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            if (permission_SDCard != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[] {WRITE_EXTERNAL_STORAGE,
                                READ_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE);
            }
            else {
                //權限相同
            }
        }

        //取得檔案路徑
        dir = new File(Environment.getExternalStorageDirectory(), "Music");
        //取得檔案列表
        musicList = dir.list();
        //亂數取得一個檔案序號
        fileRandom = (int)(Math.random() * musicList.length);
    }

    //權限控管處理
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得權限
                    Toast.makeText(this, "\"讀寫外部儲存媒體權限\"已取得！", Toast.LENGTH_SHORT).show();
                } else {
                    //使用者拒絕權限
                    Toast.makeText(this, "您必需同意本APP取得\"讀寫外部儲存媒體權限\"才能繼續使用。", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    //開始播放
    public void StartOnClick(View view) {
        try {
            if (mediaPlayer == null) {
                //建立物件
                mediaPlayer = new MediaPlayer();
                //設定音頻屬性
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                //設定資料來源
                mediaPlayer.setDataSource(dir + "/" + musicList[fileRandom]);
            }
            //設定循環播放
            mediaPlayer.setLooping(true);
            //設定準備同步播放
            mediaPlayer.prepare();
            //開始播放
            mediaPlayer.start();

            /* === SeekBar === */
            //設定上限值：獲取文件的持續時間
            seekBar.setMax(mediaPlayer.getDuration());
            //計算文件的持續時間：分
            int minutes = mediaPlayer.getDuration() / 1000 / 60;
            //計算文件的持續時間：秒
            int seconds = (int) (((mediaPlayer.getDuration() / 1000 / 60F) - minutes) * 60);

            strTotal = "Music Played.\nTotal Time:" + minutes + "分" + seconds + "秒";
            tvShow.setText(strTotal);

            /* === 設定移動值 === */
            //建立計數器
            timer = new Timer();
            //建立Task物件
            SeekBarTask sbTask = new SeekBarTask();
            //排定行程
            timer.schedule(sbTask, 0, 1000);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //結束播放
    public void StopOnClick(View view) {
        if (mediaPlayer != null){
            //停止播放
            mediaPlayer.stop();
            //設為不存在
            mediaPlayer = null;

            //亂數取得一個檔案序號
            fileRandom = (int)(Math.random() * musicList.length);
            //seekBar歸0
            seekBar.setProgress(0);
            //計數器停止
            timer.cancel();
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && mediaPlayer != null){
                //如果是使用者設定長度位置
                seekBar.setProgress(progress);
            }
            else if (!fromUser){    //非使用者設定
                //計算文件的目前時間：分
                int pminutes = progress / 1000 / 60;
                //計算文件的目前時間：秒
                int pseconds = (int) (((progress / 1000 / 60F) - pminutes) * 60);

                tvShow.setText(strTotal + "\nNow:" + pminutes + "分" + pseconds + "秒");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //沒用到
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //沒用到
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null){
            //停止播放
            mediaPlayer.stop();
            //釋放資源
            mediaPlayer.release();
            //設為不存在
            mediaPlayer = null;
        }
    }

    private class SeekBarTask extends TimerTask{

        @Override
        public void run() {
            //設定移動值
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            //如果非播放中處理
            if (!mediaPlayer.isPlaying()){
                //停止播放
                mediaPlayer.stop();
                //設為不存在
                mediaPlayer = null;
                //計數器停止
                timer.cancel();
            }
        }
    }
}
