package com.kevin.vension.redenvelopeshelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;


/**
 * @author ：Created by vension on 2018/1/4.
 * @email：kevin-vension@foxmail.com
 * @desc character determines attitude, attitude determines destiny
 *
 * @教程1 http://blog.csdn.net/zx_android/article/details/78706615
 * @教程2 https://www.jianshu.com/p/cd1cd53909d7
 */
public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//打开系统设置中辅助功能
		Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
		startActivity(intent);
		Toast.makeText(MainActivity.this, "找到V-微信/V-QQ自动抢红包服务，开启即可", Toast.LENGTH_LONG).show();
	}
}
