package com.kevin.vension.redenvelopeshelper.service;

/**
 * @author ：Created by vension on 2018/1/4.
 * @email：kevin-vension@foxmail.com
 * @desc character determines attitude, attitude determines destiny
 */

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

import static android.content.ContentValues.TAG;

//文本类型：
// 监控QQ消息==童鞋: 恭喜发财

//红包类型：
// 监控QQ消息==童鞋: [QQ红包]恭喜发财

public class QQEnvelopeService extends AccessibilityService {

	static final String ENVELOPE_TEXT_KEY = "[QQ红包]";


	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Log.d(TAG, "QQ事件--------------------------" + event.getEventType());

		final int eventType = event.getEventType();

		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				for (CharSequence t : texts) {
					String text = String.valueOf(t);
					Log.v("tag", "监控QQ消息==" + text);
					if (text.contains(ENVELOPE_TEXT_KEY)) {
						openNotification(event);
						break;
					}
				}
			}
		} else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			openEnvelope(event);
		} else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			openEnvelopeContent(event);
		}
	}

	/**
	 * 打开通知栏消息并跳转到红包页面
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void openNotification(AccessibilityEvent event) {
		if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
			return;
		}
		//以下是精华，将微信的通知栏消息打开
		Notification notification = (Notification) event.getParcelableData();
		PendingIntent pendingIntent = notification.contentIntent;
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
	}




	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void openEnvelope(AccessibilityEvent event) {
		if ("com.tencent.mobileqq.activity.SplashActivity".equals(event.getClassName())) {
			checkKey();
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void openEnvelopeContent(AccessibilityEvent event) {
        /*if ("android.widget.RelativeLayout".equals(event.getClassName())) {
            checkKey();
        } else if("android.widget.TextView".equals(event.getClassName())) {
            checkKey();
        }*/
		checkKey();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void checkKey() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			return;
		}
		List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("点击拆开");
		if (list.isEmpty()) {
			return;
		}
		for (AccessibilityNodeInfo info : list) {
			AccessibilityNodeInfo parent = info.getParent();
			if (parent != null) {
				parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				break;
			}
		}
	}

	@Override
	public void onInterrupt() {
		Toast.makeText(this, "QQ抢红包服务已关闭", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Toast.makeText(this, "QQ抢红包服务已开启", Toast.LENGTH_SHORT).show();
	}

}

