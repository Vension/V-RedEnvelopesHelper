package com.kevin.vension.redenvelopeshelper.service;

/**
 * @author ：Created by vension on 2018/1/5.
 * @email：kevin-vension@foxmail.com
 * @desc character determines attitude, attitude determines destiny
 */

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

//com.tencent.mm:id/c2i
public class RobMoney extends AccessibilityService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int eventType = event.getEventType();
		switch (eventType) {
			//第一步：监听通知栏消息
			case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
				List<CharSequence> texts = event.getText();
				if (!texts.isEmpty()) {
					for (CharSequence text : texts) {
						String content = text.toString();
						Log.i("demo", "text:"+content);
						if (content.contains("[微信红包]")) {
							//模拟打开通知栏消息
							if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
								Notification notification = (Notification) event.getParcelableData();
								PendingIntent pendingIntent = notification.contentIntent;
								try {
									pendingIntent.send();
								} catch (CanceledException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				break;
			//第二步：监听是否进入微信红包消息界面
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
				String className = event.getClassName().toString();
				if (className.equals("com.tencent.mm.ui.LauncherUI")) {
					//开始抢红包
					getPacket();
				} else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
					//开始打开红包
					openPacket();
				}
				break;
		}
	}

	/**
	 * 查找到
	 */
	@SuppressLint("NewApi")
	private void openPacket() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo != null) {
			findOpenBtn(nodeInfo);//点击开红包操作
//			List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("開");
//			for (AccessibilityNodeInfo n : list) {
//				n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//			}
		}
	}


	//
	private boolean findOpenBtn(AccessibilityNodeInfo rootNode) {
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
			Log.d("mylog", "--------RP node className = " + nodeInfo.getClassName() + " cd:" + nodeInfo.getContentDescription());
			if ("android.widget.Button".equals(nodeInfo.getClassName())) {
				Log.d("mylog", "----------RPbutton");
				nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				return true;
			}
			findOpenBtn(nodeInfo);
		}
		return false;
	}


	@SuppressLint("NewApi")
	private void getPacket() {
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		recycle(rootNode);
	}

	/**
	 * 打印一个节点的结构
	 * @param info
	 */
	@SuppressLint("NewApi")
	public void recycle(AccessibilityNodeInfo info) {
		if (info.getChildCount() == 0) {
			if(info.getText() != null){
				if("领取红包".equals(info.getText().toString())){
					//这里有一个问题需要注意，就是需要找到一个可以点击的View
					Log.i("demo", "Click"+",isClick:"+info.isClickable());
					info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					AccessibilityNodeInfo parent = info.getParent();
					while(parent != null){
						Log.i("demo", "parent isClick:"+parent.isClickable());
						if(parent.isClickable()){
							parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
							break;
						}
						parent = parent.getParent();
					}

				}
			}

		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if(info.getChild(i)!=null){
					recycle(info.getChild(i));
				}
			}
		}
	}

	/**
	 * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
	 */
	@Override
	public void onInterrupt() {
		Toast.makeText(this, "微信抢红包服务已关闭", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 当系统连接上你的服务时被调用
	 */
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		Toast.makeText(this, "微信抢红包服务已开启", Toast.LENGTH_SHORT).show();
	}


}
