package com.glens.speech;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.glens.speech.activity.ConstructionVehicleActivity;
import com.glens.speech.activity.DistrictManagerActivity;
import com.glens.speech.activity.SafetyEnquiryActivity;
import com.glens.speech.activity.TransmissionSpecialtyActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.ll_safety_enquiry)
    LinearLayout llSafetyEnquiry;
    @BindView(R.id.ll_power_transmission_specialty)
    LinearLayout llPowerTransmissionSpecialty;
    @BindView(R.id.ll_district_manager)
    LinearLayout llDistrictManager;
    @BindView(R.id.ll_construction_vehicle)
    LinearLayout llConstructionVehicle;
    @BindView(R.id.ll_face_recognition)
    LinearLayout llFaceRecognition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.ll_safety_enquiry, R.id.ll_power_transmission_specialty, R.id.ll_district_manager, R.id.ll_construction_vehicle, R.id.ll_face_recognition})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_safety_enquiry:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SafetyEnquiryActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_power_transmission_specialty:
                Intent intent1 = new Intent();
                intent1.setClass(MainActivity.this, TransmissionSpecialtyActivity.class);
                startActivity(intent1);
                break;
            case R.id.ll_district_manager:
                Intent intent2 = new Intent();
                intent2.setClass(MainActivity.this, DistrictManagerActivity.class);
                startActivity(intent2);
                break;
            case R.id.ll_construction_vehicle:
                Intent intent3 = new Intent();
                intent3.setClass(MainActivity.this, ConstructionVehicleActivity.class);
                startActivity(intent3);
                break;
            case R.id.ll_face_recognition:
                break;
        }
    }
}
