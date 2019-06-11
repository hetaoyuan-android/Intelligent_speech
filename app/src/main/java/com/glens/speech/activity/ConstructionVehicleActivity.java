package com.glens.speech.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.glens.speech.R;
import com.glens.speech.adapter.ImageAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 工程车辆
 */
public class ConstructionVehicleActivity extends AppCompatActivity {


    @BindView(R.id.gridview)
    GridView gridview;
    @BindView(R.id.vehicle_back)
    ImageView vehicleBack;

    //图片适配器
    private ImageAdapter adapter;
    //保存联网获取的图片数据
    private List<String> list;
    private boolean isShowCheck;
    // 记录选中的checkbox
    private List<String> checkList;
    String url = "http://juheimg.oss-cn-hangzhou.aliyuncs.com/joke/201503/03/48B47A541B74F574993495EF5734BF82.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_construction_vehicle);
        ButterKnife.bind(this);
        initData();
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new MyOnItemClickListener());
    }

    private void initData() {
        list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(url);
        }
        checkList = new ArrayList<>();
        adapter = new ImageAdapter(ConstructionVehicleActivity.this, list);
    }

    @OnClick(R.id.vehicle_back)
    public void onViewClicked() {
        finish();
    }
}

class MyOnItemClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
