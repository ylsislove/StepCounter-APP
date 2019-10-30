package com.yaindream.step01;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;

public class StepCounterService extends Service {

    public static Boolean FLAG = false;     // 服务运行标志
    private SensorManager mSensorManager;   // 传感器服务
    private StepDetector detector;          // 传感器监听对象

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        FLAG = true;    // 标记为服务正在运行

        // 创建监听器类，实例化监听对象
        detector = new StepDetector(this);

        // 获取传感器的服务，初始化传感器
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        // 注册传感器，注册监听器
        mSensorManager.registerListener(detector, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        FLAG = false;// 服务停止
        if (detector != null) {
            mSensorManager.unregisterListener(detector);
        }
    }

}
