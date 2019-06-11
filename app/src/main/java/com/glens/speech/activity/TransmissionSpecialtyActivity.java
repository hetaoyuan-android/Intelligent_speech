package com.glens.speech.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.glens.speech.R;
import com.glens.speech.adapter.TransmissionAdapter;
import com.glens.speech.utils.AuditRecorderConfiguration;
import com.glens.speech.utils.DialogMessage;
import com.glens.speech.utils.ExtAudioRecorder;
import com.glens.speech.utils.FailRecorder;
import com.glens.speech.utils.HttpUtil;
import com.glens.speech.utils.MyGifView;
import com.glens.speech.utils.WordsToSpeech;


import org.json.JSONArray;
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
import ecRobot.openinteraction.IListening;
import ecRobot.openinteraction.SemanticUnderstandingInteraction;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.glens.speech.activity.SafetyEnquiryActivity.isExitsSdcard;

/**
 *  输电专业
 */
public class TransmissionSpecialtyActivity extends AppCompatActivity {

    @BindView(R.id.transmission_recycler_view)
    RecyclerView transmissionRecyclerView;
    @BindView(R.id.ll_transmission_main)

    LinearLayout llTransmissionMain;
    @BindView(R.id.iv_transmission_back)
    ImageView ivTransmissionBack;
    @BindView(R.id.iv_sound_recording)
    ImageView ivSoundRecording;
    @BindView(R.id.iv_transmission_all_question)
    ImageView ivTransmissionAllQuestion;
    @BindView(R.id.ll_transmission_bottom_main)
    LinearLayout llTransmissionBottomMain;
    @BindView(R.id.iv_transmission_gif)
    MyGifView ivTransmissionGif;

    //权限集合
    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean havePermissons = true;
    private TransmissionAdapter transmissionAdapter;
    private List<DialogMessage> msgList = new ArrayList<>();
    //是否显示特征值
    private boolean isShowDisplay = false;


    // 获取类的实例
    ExtAudioRecorder recorder;
    //录音地址
    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "media.wav";
    //语音：正常回复的回答
    String filePathAnswer = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "answer.wav";
    //语音：我不知道应该怎么答复您
    String filePathAnswerOne = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "answer1.wav";
    //语音：语音识别失败
    String filePathAnswerTwo = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "answer2.wav";
    //响应超时
    String filePathAnswerThree = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/" + "answer_timeout.wav";
    //转换得到的语音文件固定存放在手机路径： /sdcard/dw_answer/speech.wav
    String speechPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/speech.mp3";

    private MediaPlayer mediaPlayer;
    private String url = "http://172.16.2.76:8080/olis/eap/demo/getTalkMessage?param=";
    private String url1 = "http://172.16.2.132:8080/pmsWeb/eap/pmsServices/transmission/voice/getVoiceResult?searchName=";

    private String mJsonStr;
    private String intent;
    private String normalized_word;
    private JSONArray slots;
    private IListeningImpl impl;

//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == 666) {
//                loadData();
//            }
//        }
//    };


    private void loadData() throws JSONException {

        if (intent.equals("APPQUERYDWTEAMRATE")) {
            intent = "班组到位率";
        } else if (intent.equals("APPQUERYLINERATE")) {
            intent = "线路到位率";
        } else if (intent.equals("APPQUERYNEARBYDEVICE")) {
            intent = "巡视到位率";
        } else if (intent.equals("APPQUERYTOWERDETAIL")) {
            intent = "杆塔台账";
        } else if (intent.equals("APPVIEWSINGLELINEDRAW")) {
            intent = "单线图";
        } else if (intent.equals("APPVIEWLINELAYOUT")) {
            intent = "沿步图";
        } else if (intent.equals("APPQUERYLINEFAULTHISTORY")) {
            intent = "故障履历";
        } else if (intent.equals("APPQUERYDEFECT")) {
            intent = "缺陷情况";
        } else if (intent.equals("APPQUERYHIDDENTROUBLE")) {
            intent = "隐患情况";
        }
        DialogMessage msgSendIntent = new DialogMessage("特征:" + intent, DialogMessage.TYPE_SEND);
        msgList.add(msgSendIntent);

        for (int i = 0; i < slots.length(); i++) {
            DialogMessage msgSend = new DialogMessage("参数" + (i+1) +":" + slots.getJSONObject(i).getString("normalized_word"), DialogMessage.TYPE_SEND);
            msgList.add(msgSend);
        }

//        DialogMessage msgSend3 = new DialogMessage(intent, DialogMessage.TYPE_SEND);
//        msgList.add(msgSend3);



        Log.i("hetao", "url mJsonStr" + url1 + mJsonStr);
        HttpUtil.sendRequestWithOkhttp(url1 + mJsonStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("hetao", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseContent = response.body().string();

                Log.i("mJsonStr", "mJsonStr" + mJsonStr);
                Log.i("responseContent", "responseContent" + responseContent);
                JSONObject jsonObjectResponse = null;
                JSONObject jsonObject = null;
                try {
                    jsonObjectResponse = new JSONObject(responseContent);
                    jsonObject = new JSONObject(mJsonStr);
                    JSONObject dataObject = jsonObjectResponse.getJSONObject("data");
                    final String nluResult = (String) dataObject.get("Msg");
                    final String errorMsg = (String) dataObject.get("errorMsg");
                    int isSuccess = dataObject.getInt("isSuccess");
                    Log.d("nluResult", nluResult);


                    boolean flag = isSuccess == 1;

                    if (flag) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //文字理解并回复文字
                                //接收消息
                                DialogMessage msgAccept = new DialogMessage(nluResult, DialogMessage.TYPE_RECEIVED);
                                msgList.add(msgAccept);
                                //通知列表有新数据插入 这样数据才能在recyclerview中显示

                                transmissionAdapter.notifyDataSetChanged();
                                transmissionAdapter.notifyItemInserted(msgList.size() + 1);
                                //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
                                //msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                transmissionRecyclerView.smoothScrollToPosition(transmissionAdapter.getItemCount() - 1);
                                Log.d("nluResult", nluResult);
//                                        textToSpeech(nluResult);
                            }

                        });
                        Boolean flag2 = WordsToSpeech.speech(nluResult);
                        // flag == true, 转换成功， 得到的语音文件固定存放在手机路径： /sdcard/dw_answer/speech.wav
                        // flag == false， 转换失败
                        if (flag2) {
                            // 播放语音：
                            // path : 文件路径
                            WordsToSpeech.playMedia(speechPath);
                            Log.d("hetao222", "WordsToSpeech " + speechPath);
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //文字理解并回复文字
                                //接收消息
                                DialogMessage msgAccept = new DialogMessage(errorMsg, DialogMessage.TYPE_RECEIVED);
                                msgList.add(msgAccept);
                                //通知列表有新数据插入 这样数据才能在recyclerview中显示

                                transmissionAdapter.notifyDataSetChanged();
                                transmissionAdapter.notifyItemInserted(msgList.size() + 1);
                                //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
                                //msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                transmissionRecyclerView.smoothScrollToPosition(transmissionAdapter.getItemCount() - 1);
                                Log.d("nluResult", errorMsg);
//                                        textToSpeech(errorMsg);
                            }

                        });

                        Boolean flag1 = WordsToSpeech.speech(errorMsg);
                        // flag == true, 转换成功， 得到的语音文件固定存放在手机路径： /sdcard/dw_answer/speech.wav
                        // flag == false， 转换失败
                        if (flag1) {
                            // 播放语音：
                            // path : 文件路径
                            WordsToSpeech.playMedia(speechPath);
                            Log.d("hetao333:WordsToSpeech", "WordsToSpeech " + errorMsg);
                        }


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission_specialty);
        ButterKnife.bind(this);

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

        AuditRecorderConfiguration configuration = new AuditRecorderConfiguration.Builder()
                .recorderListener(listener)
                .uncompressed(true)
                .builder();

        recorder = new ExtAudioRecorder(configuration);

        //为滑动框设置布局管理器，设置适配器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        transmissionRecyclerView.setLayoutManager(layoutManager);
        transmissionAdapter = new TransmissionAdapter(msgList);
        transmissionRecyclerView.setAdapter(transmissionAdapter);
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/");
        if (!path.exists()) {
            path.mkdir();
        }
        impl = new IListeningImpl();
        Log.i("hetao555","impl" + impl);
        SemanticUnderstandingInteraction.getInstance().registerListening(impl);
    }

    @OnClick({R.id.iv_transmission_back, R.id.iv_sound_recording, R.id.iv_transmission_all_question, R.id.iv_transmission_gif})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_transmission_back:
                finish();
                impl = null;
//                mHandler.removeCallbacksAndMessages(null);
//                mHandler = null;
//                mHandler.removeMessages(SET_VIDEO_PLAYING_CALLBACK)
                break;
            case R.id.iv_sound_recording:
                llTransmissionMain.setVisibility(View.INVISIBLE);
                transmissionRecyclerView.setVisibility(View.VISIBLE);
                llTransmissionBottomMain.setVisibility(View.INVISIBLE);
                ivTransmissionGif.setVisibility(View.VISIBLE);
                //检查sdcard
                if (!isExitsSdcard()) {
                    String needSd = getResources().getString(R.string.send_voice_need_sdcard_support);
                    Toast.makeText(TransmissionSpecialtyActivity.this, needSd, Toast.LENGTH_SHORT).show();
                    return;
                }
                stopPlaying();
                // 设置输出文件
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
                if (filePath == null) {
                    return;
                }
                recorder.setOutputFile(filePath);
                recorder.prepare();
                recorder.start();
                break;
            case R.id.iv_transmission_all_question:
                break;
            case R.id.iv_transmission_gif:
                if (llTransmissionBottomMain.getVisibility() != View.VISIBLE) {
                    llTransmissionBottomMain.setVisibility(View.VISIBLE);
                }
                ivTransmissionGif.setVisibility(View.INVISIBLE);
                if (recorder.getState() != ExtAudioRecorder.State.RECORDING) {
                    return;
                }
                //录音成功
                int time = recorder.stop();
                if (time > 0) {
                    //成功的处理
                    Thread thread = new Thread() {
                        @Override
                        public void run() {

                            //文字转语音并播放
//                                        SemanticUnderstandingInteraction.getInstance().speakToTTS("今天天气怎么样",Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/test.wav");

//                                        super.run();
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
//                                        speechWords="我听不懂你说的鸟语";
                            //发送消息
                            DialogMessage msgSend = new DialogMessage(speechWords, DialogMessage.TYPE_SEND);
                            msgList.add(msgSend);



                            // 解析mJsonStr数据特征值

                            //通知列表有新数据插入 这样数据才能在recyclerview中显示
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //通知列表有新数据插入 这样数据才能在recyclerview中显示
                                    transmissionAdapter.notifyDataSetChanged();
                                    transmissionAdapter.notifyItemInserted(msgList.size() + 1);
                                    //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
//                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                    transmissionRecyclerView.smoothScrollToPosition(transmissionAdapter.getItemCount() - 1);
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
                                        transmissionAdapter.notifyDataSetChanged();
                                        transmissionAdapter.notifyItemInserted(msgList.size() + 1);
                                        //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
//                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                        transmissionRecyclerView.smoothScrollToPosition(transmissionAdapter.getItemCount() - 1);
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

                            if (flag) {
                                try {
                                    new JSONObject(NluInterlocution.dialog(format(speechWords))).getString("say");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            String nluResult = "";
//                                        if (flag) {
//                                            //3. 文字理解并回复文字
//                                            try {
//                                                nluResult = new JSONObject(NluInterlocution.dialog(format(speechWords))).getString("say");
////                                                nluResult = "哈哈哈。。。";
//                                                //接收消息
//                                                DialogMessage msgAccept = new DialogMessage(nluResult, DialogMessage.TYPE_RECEIVED);
//                                                msgList.add(msgAccept);
//                                                //通知列表有新数据插入 这样数据才能在recyclerview中显示
//                                                runOnUiThread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        //通知列表有新数据插入 这样数据才能在recyclerview中显示
//                                                        transmissionAdapter.notifyDataSetChanged();
//                                                        transmissionAdapter.notifyItemInserted(msgList.size() + 1);
//                                                        //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
////                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
//                                                        transmissionRecyclerView.smoothScrollToPosition(transmissionAdapter.getItemCount() - 1);
//
//                                                    }
//                                                });
//                                                Log.d("nluResult", nluResult);
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//
//                                        //4. 文字转语音，并播放
//                                        String exceptionReason = "";
//                                        if (flag) {
//                                            try {
//                                                TtsWordsToSpeech.runToFile(nluResult, filePathAnswer);
//                                            } catch (Exception e) {
//                                                exceptionReason = e.getMessage();
//                                                e.printStackTrace();
//                                            }
//
//                                            try {
//                                                if (exceptionReason.toLowerCase().contains("timed out")) {
//                                                    //响应超时
//                                                    AssetFileDescriptor afd = getAssets().openFd("answer_timeout.wav");
//                                                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//                                                } else {
//                                                    mediaPlayer.setDataSource(filePathAnswer);
//                                                }
//
//                                                Log.d("回答语音文件生成", "语音文件已生成");
//                                                /* 准备 */
//                                                mediaPlayer.prepare();
//                                                mediaPlayer.start();
//                                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                                                    @Override
//                                                    public void onCompletion(MediaPlayer mp) {
//                                                        endPlay(mp);
//                                                    }
//                                                });
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }

                        }
                    };
                    thread.start();
                } else {
                    String st2 = getResources().getString(R.string.the_recording_time_is_too_short);
                    Toast.makeText(TransmissionSpecialtyActivity.this, st2, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults[0] == 0) {
            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            onSuccessPermission();
        } else {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
        }
    }

    //播放语音结束
    private void endPlay(MediaPlayer mp) {
        Log.d("tag", "播放完毕");
        if (recorder != null) recorder.reset();
        if (mp != null) mp.reset();
    }

    public String format(String s) {
        String str = s.replaceAll("[`!@#~^&*()|{}';',\\[\\]<>?~！@#……& amp;*（)()——|{}【】\"\" ‘；”“’。，、？|]", "");
        return str;
    }

    /**
     * 录音失败的提示
     */
    ExtAudioRecorder.RecorderListener listener = new ExtAudioRecorder.RecorderListener() {
        @Override
        public void recordFailed(FailRecorder failRecorder) {
            if (failRecorder.getType() == FailRecorder.FailType.NO_PERMISSION) {
                Toast.makeText(TransmissionSpecialtyActivity.this, "录音失败，可能是没有给权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TransmissionSpecialtyActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
            }
        }
    };


    //任务对话的回调类
    public class IListeningImpl implements IListening {
        @Override
        public void onListen(String jsonStr) {
            System.out.println("MainActivity-------IListeningImpl=" + jsonStr);
            try {
                JSONObject jo = new JSONObject(jsonStr);
                String type = jo.getString("type");
                switch (type) {
                    case "clarify":
                        //需要二次对话
                        textToSpeech(jo.getString("say"));
                        break;
                    case "failure":
                        //查询失败
                        textToSpeech(jo.getString("say"));
                        break;
                    case "satisfy":
                        //查询成功
                        if (!TextUtils.isEmpty(jsonStr)) {
                            mJsonStr = jsonStr;
//                            mHandler.sendEmptyMessage(666);
                            // mJsonStr的解析
                            JSONObject mJsonContent = new JSONObject(mJsonStr);
                            JSONObject schema = mJsonContent.getJSONObject("schema");
                            intent = schema.getString("intent");
                            slots = schema.getJSONArray("slots");
                            for (int i = 0; i <slots.length(); i++) {
                                JSONObject jsonObject1 = slots.getJSONObject(i);
                                normalized_word = jsonObject1.getString("normalized_word");
                                Log.i("hetao00", normalized_word);

                            }
                            Log.i("hetao00", intent);
                            loadData();
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    //文字转语音
    private void textToSpeech(final String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                Boolean flag = WordsToSpeech.speech(text);
                // flag == true, 转换成功， 得到的语音文件固定存放在手机路径： /sdcard/dw_answer/speech.wav
                // flag == false， 转换失败
                if (flag) {
                    // 播放语音：
                    // path : 文件路径
                    WordsToSpeech.playMedia(speechPath);
                    Log.d("hetao444:WordsToSpeech", "WordsToSpeech " + speechPath);
                }
            }
        }.start();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //3. 文字理解并回复文字
                //接收消息
                DialogMessage msgAccept = new DialogMessage(text, DialogMessage.TYPE_RECEIVED);
                msgList.add(msgAccept);
                //通知列表有新数据插入 这样数据才能在recyclerview中显示

                transmissionAdapter.notifyDataSetChanged();
                transmissionAdapter.notifyItemInserted(msgList.size() + 1);
                //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
                //msgRecyclerView.scrollToPosition(msgList.size() + 1);
                transmissionRecyclerView.smoothScrollToPosition(transmissionAdapter.getItemCount() - 1);
                Log.d("text", text);

            }
        });
    }

}
