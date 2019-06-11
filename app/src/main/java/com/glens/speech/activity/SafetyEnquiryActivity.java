package com.glens.speech.activity;

import android.Manifest;
import android.content.Intent;
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
import android.widget.Toast;

import com.glens.speech.R;
import com.glens.speech.adapter.MsgAdapter;
import com.glens.speech.utils.AuditRecorderConfiguration;
import com.glens.speech.utils.DialogMessage;
import com.glens.speech.utils.ExtAudioRecorder;
import com.glens.speech.utils.FailRecorder;
import com.glens.speech.utils.MyGifView;
import com.glens.speech.utils.WordsToSpeech;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ecRobot.AsrSpeechToWords;
import ecRobot.NluInterlocution;
import ecRobot.TtsWordsToSpeech;
import ecRobot.common.AuthService;


/**
 *  安规查询
 */
public class SafetyEnquiryActivity extends AppCompatActivity {

    @BindView(R.id.ll_safety_enquiry_main)
    LinearLayout llSafetyEnquiryMain;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.iv_sound_recording)
    ImageView ivSoundRecording;
    @BindView(R.id.iv_all_question)
    ImageView ivAllQuestion;
    @BindView(R.id.ll_bottom_main)
    LinearLayout llBottomMain;
    @BindView(R.id.msg_recycler_view)
    RecyclerView msgRecyclerView;
    @BindView(R.id.iv_gif)
    MyGifView ivGif;

    //权限集合
    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // 没有答案的问题
    String nluQuery = "";
    ArrayList<String> list = new ArrayList<>();

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


    // 消息
    private List<DialogMessage> msgList = new ArrayList<>();
    private MsgAdapter adapter;
    private boolean havePermissons = true;

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_enquiry);
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
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/dw_answer/");
        if (!path.exists()) {
            path.mkdir();
        }
//        IListeningImpl impl = new IListeningImpl();
//        SemanticUnderstandingInteraction.getInstance().registerListening(impl);
    }


//    //任务对话的回调类
//    public class IListeningImpl implements IListening {
//        @Override
//        public void onListen(String jsonStr) {
//            System.out.println("MainActivity-------IListeningImpl="+jsonStr);
//            try {
//                JSONObject nluAnswerSlots = new JSONObject(jsonStr).getJSONObject("schema");
//                String intent = nluAnswerSlots.getString("intent");//获取领域，也就是所谓的函数
//                JSONArray slots = nluAnswerSlots.getJSONArray("slots");//获取槽位，也就是所谓的参数
//                if ("INPLACE_RATE".equals(intent))//查询班组到位率的领域
//                {
//                    for (int i = 0; i < slots.length(); i++)
//                    {
//                        JSONObject slot = slots.getJSONObject(i);
//                        //槽位的key
//                        String slotName = slot.getString("name");
//                        String slotValue = slot.getString("normalized_word");
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == 0) {
            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            onSuccessPermission();
        } else {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.iv_back, R.id.iv_sound_recording, R.id.iv_all_question, R.id.iv_gif})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //返回
            case R.id.iv_back:
                finish();
                break;
            //录音
            case R.id.iv_sound_recording:
                llSafetyEnquiryMain.setVisibility(View.INVISIBLE);
                msgRecyclerView.setVisibility(View.VISIBLE);
                llBottomMain.setVisibility(View.INVISIBLE);
                ivGif.setVisibility(View.VISIBLE);
                //检查sdcard
                if (!isExitsSdcard()) {
                    String needSd = getResources().getString(R.string.send_voice_need_sdcard_support);
                    Toast.makeText(SafetyEnquiryActivity.this, needSd, Toast.LENGTH_SHORT).show();
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
            // 问题
            case R.id.iv_all_question:
                Intent intent = new Intent();
                intent.putStringArrayListExtra("list_question", list);
                intent.setClass(SafetyEnquiryActivity.this, QuestionListActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_gif:
                if (llBottomMain.getVisibility() != View.VISIBLE) {
                    llBottomMain.setVisibility(View.VISIBLE);
                }
                ivGif.setVisibility(View.INVISIBLE);
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
                                    adapter.notifyDataSetChanged();
                                    adapter.notifyItemInserted(msgList.size() + 1);
                                    //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
//                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                    msgRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
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
                                        adapter.notifyDataSetChanged();
                                        adapter.notifyItemInserted(msgList.size() + 1);
                                        //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
//                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                        msgRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
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
                            String nluResult = "";
                            String nluType = "";

                            if (flag) {
                                //3. 文字理解并回复文字
                                try {
                                    nluResult = new JSONObject(NluInterlocution.dialog(format(speechWords))).getString("say");
                                    nluType = new JSONObject(NluInterlocution.dialog(format(speechWords))).getString("type");
                                    Log.i("hetao", "type:" + nluType);
                                    if (nluType.equals("failure")) {
                                        nluQuery = new JSONObject(NluInterlocution.dialog(format(speechWords))).getString("query");
                                        list.add(nluQuery);
                                    }
                                    //接收消息
                                    DialogMessage msgAccept = new DialogMessage(nluResult, DialogMessage.TYPE_RECEIVED);
                                    msgList.add(msgAccept);
                                    //通知列表有新数据插入 这样数据才能在recyclerview中显示
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //通知列表有新数据插入 这样数据才能在recyclerview中显示
                                            adapter.notifyDataSetChanged();
                                            adapter.notifyItemInserted(msgList.size() + 1);
                                            //定位将显示的数据定位到最后一行，保证可以看到最后一条消息
//                                                        msgRecyclerView.scrollToPosition(msgList.size() + 1);
                                            msgRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);

                                        }
                                    });
                                    Log.d("nluResult", nluResult);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.i("hetao", "failure:" + nluQuery);
                            }
                            //4. 文字转语音，并播放
                            String exceptionReason = "";
                            if (flag) {
                                try {
                                    TtsWordsToSpeech.runToFile(nluResult, filePathAnswer);
                                } catch (Exception e) {
                                    exceptionReason = e.getMessage();
                                    e.printStackTrace();
                                }

                                WordsToSpeech.playMedia(filePathAnswer);
                                Log.i("hetao111", "filePathAnswer :" + filePathAnswer);

                                try {
                                    if (exceptionReason.toLowerCase().contains("timed out")) {
                                        //响应超时
                                        AssetFileDescriptor afd = getAssets().openFd("answer_timeout.wav");
                                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                    } else {
                                        mediaPlayer.setDataSource(filePathAnswer);
                                    }

                                    Log.d("回答语音文件生成", "语音文件已生成");
                                    /* 准备 */
                                    mediaPlayer.prepare();
                                    mediaPlayer.start();
                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            endPlay(mp);
                                            File fileAns = new File(filePathAnswer);
                                            if (fileAns.exists()) {
                                                fileAns.delete();
                                                Log.d("删除文件", "删除文件");
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    thread.start();
                } else {
                    String st2 = getResources().getString(R.string.the_recording_time_is_too_short);
                    Toast.makeText(SafetyEnquiryActivity.this, st2, Toast.LENGTH_SHORT).show();
                    recorder.reset();
                }
                Log.i("aaaa", "list:00000" + list.size());
                for (int i = 0; i < list.size(); i++) {
                    Log.i("aaaa", "list:00000:get" + list.get(i));
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

    public String format(String s) {
        String str = s.replaceAll("[`!@#~^&*()|{}';',\\[\\]<>?~！@#……& amp;*（)()——|{}【】\"\" ‘；”“’。，、？|]", "");
        return str;
    }

    /**
     * Sdcard是否存在
     */
    public static boolean isExitsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    /** 录音失败的提示 */
    ExtAudioRecorder.RecorderListener listener = new ExtAudioRecorder.RecorderListener() {
        @Override
        public void recordFailed(FailRecorder failRecorder) {
            if (failRecorder.getType() == FailRecorder.FailType.NO_PERMISSION) {
                Toast.makeText(SafetyEnquiryActivity.this, "录音失败，可能是没有给权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SafetyEnquiryActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        stopPlaying();
        super.onDestroy();
    }
}
