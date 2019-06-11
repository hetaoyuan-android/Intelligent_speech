package com.glens.speech.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.glens.speech.R;
import com.glens.speech.adapter.ManagerAdapter;
import com.glens.speech.utils.AuditRecorderConfiguration;
import com.glens.speech.utils.DialogMessage;
import com.glens.speech.utils.ExtAudioRecorder;
import com.glens.speech.utils.FailRecorder;
import com.glens.speech.utils.HttpUtil;
import com.glens.speech.utils.MyGifView;
import com.glens.speech.utils.WordsToSpeech;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ecRobot.AsrSpeechToWords;
import ecRobot.NluInterlocution;
import ecRobot.TtsWordsToSpeech;
import ecRobot.common.AuthService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 *  台区经理
 */
public class DistrictManagerActivity extends AppCompatActivity {

    @BindView(R.id.manager_recycler_view)
    RecyclerView managerRecyclerView;
    @BindView(R.id.iv_manager_back)
    ImageView ivManagerBack;
    @BindView(R.id.iv_manager_sound_recording)
    ImageView ivManagerSoundRecording;
    @BindView(R.id.iv_manager_all_question)
    ImageView ivManagerAllQuestion;
    @BindView(R.id.ll_manager_main)
    LinearLayout llManagerMain;
    @BindView(R.id.iv_manager_gif)
    MyGifView ivManagerGif;
    @BindView(R.id.tv_welcome)
    TextView tvWelcome;

    private MediaPlayer mediaPlayer;

    // 获取类的实例
    ExtAudioRecorder recorder;
    //录音地址
    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "media.wav";
    //语音：正常回复的回答
    String filePathAnswer = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "answer.mp3";
    //语音：我不知道应该怎么答复您
    String filePathAnswerOne = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "answer1.wav";
    //语音：语音识别失败
    String filePathAnswerTwo = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "answer2.wav";
    //响应超时
    String filePathAnswerThree = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "answer_timeout.wav";

    String managerPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/manager.mp3";


    //权限集合
    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean havePermissons = true;

    private ManagerAdapter managerAdapter;
    private List<DialogMessage> msgList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district_manager);
        ButterKnife.bind(this);
        AuditRecorderConfiguration configuration = new AuditRecorderConfiguration.Builder()
                .recorderListener(listener)
                .uncompressed(true)
                .builder();

        if (recorder == null) {
            recorder = new ExtAudioRecorder(configuration);
        }
        int permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(PERMISSIONS, 4);
                havePermissons = false;
            }
        }
        if (havePermissons) onSuccessPermission();
    }

    private void onSuccessPermission() {

        //为滑动框设置布局管理器，设置适配器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        managerRecyclerView.setLayoutManager(layoutManager);
        managerAdapter = new ManagerAdapter(msgList);
        managerRecyclerView.setAdapter(managerAdapter);
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/");
        if (!path.exists()) {
            path.mkdir();
        }
    }

    @OnClick({R.id.iv_manager_back, R.id.iv_manager_sound_recording, R.id.iv_manager_all_question, R.id.iv_manager_gif})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_manager_back:
                finish();
                break;
            case R.id.iv_manager_sound_recording:
                managerRecyclerView.setVisibility(View.VISIBLE);
                llManagerMain.setVisibility(View.INVISIBLE);
                ivManagerGif.setVisibility(View.VISIBLE);
                tvWelcome.setVisibility(View.INVISIBLE);
                //检查sdcard
                if (!isExitsSdcard()) {
                    String needSd = getResources().getString(R.string.send_voice_need_sdcard_support);
                    Toast.makeText(DistrictManagerActivity.this, needSd, Toast.LENGTH_SHORT).show();
                    return;
                }
                stopPlaying();
                // 设置输出文件
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
                recorder.setOutputFile(filePath);
                recorder.prepare();
                recorder.start();
                break;
            case R.id.iv_manager_all_question:
                break;
            case R.id.iv_manager_gif:
                if (llManagerMain.getVisibility() != View.VISIBLE) {
                    llManagerMain.setVisibility(View.VISIBLE);
                }
                ivManagerGif.setVisibility(View.INVISIBLE);
                if (recorder.getState() != ExtAudioRecorder.State.RECORDING) {
                    return;
                }

                //录音成功
                int time = recorder.stop();
                if (time > 0) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            mediaPlayer = new MediaPlayer();
                            //1. 获取token
                            String token = AuthService.getAuth();
                            //2. 语音转文字
                            String speechWords = "";

                            boolean flag = true;
                            try {
                                speechWords = AsrSpeechToWords.run(filePath);
                                Log.d("speechWords", speechWords);
                            } catch (Exception e) {
                                flag = false;
                                e.printStackTrace();
                            }
                            //发送消息
                            DialogMessage msgSend = new DialogMessage(speechWords, DialogMessage.TYPE_SEND);
                            msgList.add(msgSend);
                            //通知列表有新数据插入 这样数据才能在recyclerview中显示
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //通知列表有新数据插入 这样数据才能在recyclerview中显示
                                    managerAdapter.notifyDataSetChanged();
                                    managerAdapter.notifyItemInserted(msgList.size() + 1);
                                    //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
//                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                    managerRecyclerView.smoothScrollToPosition(managerAdapter.getItemCount() - 1);
                                }
                            });
                            File f = new File(filePath);
                            if (f.exists()) {
                                f.delete();
                            }
                            if (!flag) {
                                //接收消息
                                DialogMessage msgAccept = new DialogMessage("语音识别失败！", DialogMessage.TYPE_RECEIVED);
                                msgList.add(msgAccept);
                                //通知列表有新数据插入 这样数据才能在recyclerview中显示
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //通知列表有新数据插入 这样数据才能在recyclerview中显示
                                        managerAdapter.notifyDataSetChanged();
                                        managerAdapter.notifyItemInserted(msgList.size() + 1);
                                        //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
//                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                        managerRecyclerView.smoothScrollToPosition(managerAdapter.getItemCount() - 1);
                                    }
                                });
                                try {
                                    AssetFileDescriptor afd = getAssets().openFd("answer2.wav");
                                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                    mediaPlayer.prepare();
                                    mediaPlayer.start();
                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            endPlay(mp);
                                        }
                                    });
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }

                            String url = "http://172.16.2.132:8080/pmsWeb/eap/pmsServices/transmission/voice/getVoiceResult?searchName=";
                            Log.i("url + speechWords", "url + speechWords" + url + speechWords);
                            // 网络请求数据
                            HttpUtil.sendRequestWithOkhttp(url + speechWords, new Callback() {

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.i("hetao", e.getMessage());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String responseContent = response.body().string();
                                        Log.i("hetaoresponse00", "responseContent" + responseContent);
                                        try {
                                            JSONObject jsonObject = new JSONObject(responseContent);
                                            final String message = (String) jsonObject.get("Msg");
                                            int isSuccess = jsonObject.getInt("isSuccess");
                                            final String errMessage = (String) jsonObject.get("errorMsg");
                                            Log.i("message000", "message" + message);
                                            boolean flag = isSuccess == 1;
                                            if (flag) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //文字理解并回复文字
                                                        //接收消息
                                                        DialogMessage msgAccept = new DialogMessage(message, DialogMessage.TYPE_RECEIVED);
                                                        msgList.add(msgAccept);
                                                        //通知列表有新数据插入 这样数据才能在recyclerview中显示

                                                        managerAdapter.notifyDataSetChanged();
                                                        managerAdapter.notifyItemInserted(msgList.size() + 1);
                                                        //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
                                                        //msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                                        managerRecyclerView.smoothScrollToPosition(managerAdapter.getItemCount() - 1);
                                                        Log.d("nluResult", message);
                                                    }

                                                });
                                                Boolean flag2 = WordsToSpeech.speech(message);
                                                if (flag2) {
                                                    // 播放语音：
                                                    // path : 文件路径
                                                    WordsToSpeech.playMedia(managerPath);
                                                    Log.d("hetao:managerPath", "managerPath " + managerPath);
                                                }
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //文字理解并回复文字
                                                        //接收消息
                                                        DialogMessage msgAccept = new DialogMessage(errMessage, DialogMessage.TYPE_RECEIVED);
                                                        msgList.add(msgAccept);
                                                        //通知列表有新数据插入 这样数据才能在recyclerview中显示

                                                        managerAdapter.notifyDataSetChanged();
                                                        managerAdapter.notifyItemInserted(msgList.size() + 1);
                                                        //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
                                                        //msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                                        managerRecyclerView.smoothScrollToPosition(managerAdapter.getItemCount() - 1);
                                                        Log.d("nluResult", errMessage);
//                                        textToSpeech(errorMsg);
                                                    }

                                                });

                                                Boolean flag1 = WordsToSpeech.speech(errMessage);
                                                // flag == true, 转换成功， 得到的语音文件固定存放在手机路径： /sdcard/dw_answer/speech.wav
                                                // flag == false， 转换失败
                                                if (flag1) {
                                                    // 播放语音：
                                                    // path : 文件路径
                                                    WordsToSpeech.playMedia(managerPath);
                                                    Log.d("hetao:WordsToSpeech", "WordsToSpeech " + managerPath);
                                                }
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            });


//                            String nluResult = "";
//                            if (flag) {
//                                //3. 文字理解并回复文字
//                                try {
//                                    nluResult = new JSONObject(NluInterlocution.dialog(format(speechWords))).getString("say");
//                                    //接收消息
//                                    DialogMessage msgAccept = new DialogMessage(nluResult, DialogMessage.TYPE_RECEIVED);
//                                    msgList.add(msgAccept);
//                                    //通知列表有新数据插入 这样数据才能在recyclerview中显示
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            //通知列表有新数据插入 这样数据才能在recyclerview中显示
//                                            managerAdapter.notifyDataSetChanged();
//                                            managerAdapter.notifyItemInserted(msgList.size() + 1);
//                                            //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
////                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
//                                            managerRecyclerView.smoothScrollToPosition(managerAdapter.getItemCount() - 1);
//
//                                        }
//                                    });
//                                    Log.d("nluResult", nluResult);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            //4. 文字转语音，并播放
//                            String exceptionReason = "";
//                            if (flag) {
//                                try {
//                                    TtsWordsToSpeech.runToFile(nluResult, filePathAnswer);
//                                } catch (Exception e) {
//                                    exceptionReason = e.getMessage();
//                                    e.printStackTrace();
//                                }
//
////                                WordsToSpeech.playMedia(filePathAnswer);
//                                Log.i("hetao", "filePathAnswer :" + filePathAnswer);
//
//                                try {
//                                    if (exceptionReason.toLowerCase().contains("timed out")) {
//                                        //响应超时
//                                        AssetFileDescriptor afd = getAssets().openFd("answer_timeout.wav");
//                                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//                                    } else {
//                                        mediaPlayer.setDataSource(filePathAnswer);
//                                    }
//
//                                    Log.d("回答语音文件生成", "语音文件已生成");
//                                    /* 准备 */
//                                    mediaPlayer.prepare();
//                                    mediaPlayer.start();
//                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                                        @Override
//                                        public void onCompletion(MediaPlayer mp) {
//                                            endPlay(mp);
//                                        }
//                                    });
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
                        }
                    };
                    thread.start();
                } else {
                    String st2 = getResources().getString(R.string.the_recording_time_is_too_short);
                    Toast.makeText(DistrictManagerActivity.this, st2, Toast.LENGTH_SHORT).show();
                    recorder.reset();

                }
                break;
        }
    }

    private void stopPlaying() {
        endPlay(mediaPlayer);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    //播放语音结束
    private void endPlay(MediaPlayer mp) {
        Log.d("tag", "播放完毕");
        if (recorder != null) recorder.reset();
        if (mp != null) mp.reset();
    }

    /**
     * Sdcard是否存在
     */
    public static boolean isExitsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 录音失败的提示
     */
    ExtAudioRecorder.RecorderListener listener = new ExtAudioRecorder.RecorderListener() {
        @Override
        public void recordFailed(FailRecorder failRecorder) {
            if (failRecorder.getType() == FailRecorder.FailType.NO_PERMISSION) {
                Toast.makeText(DistrictManagerActivity.this, "录音失败，可能是没有给权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DistrictManagerActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == 0) {
            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            onSuccessPermission();
        } else {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
        }
    }

    public String format(String s) {
        String str = s.replaceAll("[`!@#~^&*()|{}';',\\[\\]<>?~！@#……& amp;*（)()——|{}【】\"\" ‘；”“’。，、？|]", "");
        return str;
    }

}
