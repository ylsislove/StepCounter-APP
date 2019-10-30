package com.yaindream.step01;

import java.text.DecimalFormat;

import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class StepCounterActivity extends Activity {

    //定义文本框控件
    private TextView tv_show_step;  // 步数
    private TextView tv_timer;      // 运行时间
    private TextView tv_distance;   // 行程
    private TextView tv_calories;   // 卡路里
    private TextView tv_velocity;   // 速度
    private Button btn_start;       // 开始按钮
    private Button btn_stop;        // 停止按钮
    private GifView gifView;

    private long timer = 0;         // 运动时间
    private long startTimer = 0;    // 开始时间
    private long tempTime = 0;
    private Double distance = 0.0;  // 路程：米
    private Double calories = 0.0;  // 热量：卡路里
    private Double velocity = 0.0;  // 速度：米每秒
    private int step_length = 0;    // 步长
    private int weight = 0;         // 体重

    private Thread thread;  //定义线程对象

    // Handler对象用于更新当前步数,定时发送消息，调用方法查询数据用于显示
    // 主要接受子线程发送的数据, 并用此数据配合主线程更新UI
    // Handler运行在主线程中(UI线程中), 它与子线程可以通过Message对象来传递数据,
    // Handler就承担着接受子线程传过来的(子线程用sendMessage()方法传递Message对象，(里面包含数据)
    // 把这些消息放入主线程队列中，配合主线程进行更新UI。
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);           // 此处可以更新UI
            distance = StepDetector.CURRENT_STEP  * step_length * 0.01;
            if (timer != 0 && distance != 0.0) {
                // 体重、距离
                // 跑步热量（kcal）＝体重（kg）×距离（公里）×1.036
                calories = weight * distance * 0.001;
                // 速度velocity
                velocity = distance * 1000 / timer;
            } else {
                calories = 0.0;
                velocity = 0.0;
            }
            tv_show_step.setText(StepDetector.CURRENT_STEP + "");   // 显示当前步数
            tv_distance.setText(formatDouble(distance));            // 显示路程
            tv_calories.setText(formatDouble(calories));            // 显示卡路里
            tv_velocity.setText(formatDouble(velocity));            // 显示速度
            tv_timer.setText(getFormatTime(timer));                 // 显示当前运行时间
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.main);  //设置当前屏幕

        // 获取界面控件
        addView();
        // 初始化控件
        init();

        if (thread == null) {
            thread = new Thread() {     // 子线程用于监听当前步数的变化
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    super.run();
                    int temp = 0;
                    while (true) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (StepCounterService.FLAG) {
                            Message msg = new Message();
                            if (temp != StepDetector.CURRENT_STEP) {
                                temp = StepDetector.CURRENT_STEP;
                            }
                            if (startTimer != System.currentTimeMillis()) {
                                timer = tempTime + System.currentTimeMillis() - startTimer;
                            }
                            handler.sendMessage(msg);// 通知主线程
                        }
                    }
                }
            };
            thread.start();
        }
    }

    /**
     * 获取Activity相关控件
     */
    private void addView() {
        tv_show_step = this.findViewById(R.id.show_step);
        tv_timer = this.findViewById(R.id.timer);
        tv_distance = this.findViewById(R.id.distance);
        tv_calories = this.findViewById(R.id.calories);
        tv_velocity = this.findViewById(R.id.velocity);
        btn_start = this.findViewById(R.id.start);
        btn_stop = this.findViewById(R.id.stop);

        //健身
        gifView = findViewById(R.id.gif_view);
        gifView.setGifImageType(GifImageType.COVER);
        gifView.setShowDimension(100, 100);
        gifView.setGifImage(R.drawable.walk_gif);
        gifView.showCover();

        Intent service = new Intent(this, StepCounterService.class);
        stopService(service);
        StepDetector.CURRENT_STEP = 0;
        tempTime = timer = 0;
        tv_timer.setText(getFormatTime(timer));      // 如果关闭之后，格式化时间
        tv_show_step.setText("0");
        tv_distance.setText(formatDouble(0.0));
        tv_calories.setText(formatDouble(0.0));
        tv_velocity.setText(formatDouble(0.0));
        handler.removeCallbacks(thread);
    }

    /**
     * 初始化界面
     */
    private void init() {
        step_length = 70;   // 步长 70cm
        weight = 50;        // 途中 50KG
        distance = StepDetector.CURRENT_STEP  * step_length * 0.01;
        if ((timer += tempTime) != 0 && distance != 0.0) {
            // tempTime记录运动的总时间，timer记录每次运动时间
            // 体重、距离
            // 跑步热量（kcal）＝体重（kg）×距离（公里）×1.036，换算一下
            calories = weight * distance * 0.001;
            velocity = distance * 1000 / timer;
        } else {
            calories = 0.0;
            velocity = 0.0;
        }
        tv_timer.setText(getFormatTime(timer + tempTime));
        tv_distance.setText(formatDouble(distance));
        tv_calories.setText(formatDouble(calories));
        tv_velocity.setText(formatDouble(velocity));
        tv_show_step.setText(StepDetector.CURRENT_STEP + "");
        btn_start.setEnabled(!StepCounterService.FLAG);
        btn_stop.setEnabled(StepCounterService.FLAG);

        if (StepCounterService.FLAG) {
            btn_stop.setText(getString(R.string.pause));

        } else if (StepDetector.CURRENT_STEP > 0) {
            btn_stop.setEnabled(true);
            btn_stop.setText(getString(R.string.cancel));
        }
    }


    public void onClick(View view) {
        Intent service = new Intent(this, StepCounterService.class);
        switch (view.getId()) {
            case R.id.start:
                startService(service);
                gifView.showAnimation();
                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);
                btn_stop.setText(getString(R.string.pause));
                startTimer = System.currentTimeMillis();
                tempTime = timer;
                break;

            case R.id.stop:
                stopService(service);
                gifView.showCover();
                if (StepCounterService.FLAG && StepDetector.CURRENT_STEP > 0) {
                    btn_stop.setText(getString(R.string.cancel));

                } else {
                    StepDetector.CURRENT_STEP = 0;
                    tempTime = timer = 0;
                    btn_stop.setText(getString(R.string.pause));
                    btn_stop.setEnabled(false);
                    tv_timer.setText(getFormatTime(timer));      // 如果关闭之后，格式化时间
                    tv_show_step.setText("0");
                    tv_distance.setText(formatDouble(0.0));
                    tv_calories.setText(formatDouble(0.0));
                    tv_velocity.setText(formatDouble(0.0));
                    handler.removeCallbacks(thread);
                }
                btn_start.setEnabled(true);
                break;
        }
    }

    /**
     * 计算并格式化doubles数值，保留两位有效数字
     * @param doubles doubles数值
     * @return 返回当前路程
     */
    private String formatDouble(Double doubles) {
        DecimalFormat format = new DecimalFormat("####.##");
        String distanceStr = format.format(doubles);
        return distanceStr.equals(getString(R.string.zero)) ? getString(R.string.double_zero)
                : distanceStr;
    }

    /**
     * 得到一个格式化的时间
     * @param time 时间 毫秒
     * @return 时：分：秒
     */
    private String getFormatTime(long time) {
        time = time / 1000;
        long second = time % 60;
        long minute = (time % 3600) / 60;
        long hour = time / 3600;
        // 秒显示两位
        String strSecond = ("00" + second).substring(("00" + second).length() - 2);
        // 分显示两位
        String strMinute = ("00" + minute).substring(("00" + minute).length() - 2);
        // 时显示两位
        String strHour = ("00" + hour).substring(("00" + hour).length() - 2);

        return strHour + ":" + strMinute + ":" + strSecond;
    }

}
