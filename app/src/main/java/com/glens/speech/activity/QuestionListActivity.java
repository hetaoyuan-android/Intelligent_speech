package com.glens.speech.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.glens.speech.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *  问题列表
 */
public class QuestionListActivity extends AppCompatActivity {

    @BindView(R.id.lv_question)
    ListView lvQuestion;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    private ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        list = intent.getStringArrayListExtra("list_question");
        Log.i("QuestionListActivity", "lis" + list.size());
        lvQuestion.setAdapter(new MyAdapter());
    }

    @OnClick(R.id.iv_back)
    public void onViewClicked() {
        finish();
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mHolder;
            if (convertView == null) {
                mHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                convertView = inflater.inflate(R.layout.item_question, null, true);
                mHolder.questionTitle = convertView.findViewById(R.id.tv_question_title);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }
            mHolder.questionTitle.setText(list.get(position));
            return convertView;
        }
    }

    class ViewHolder {
        private TextView questionTitle;
    }
}
