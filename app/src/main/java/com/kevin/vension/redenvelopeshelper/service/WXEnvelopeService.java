package com.kevin.vension.redenvelopeshelper.service;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * @author ：Created by vension on 2018/1/4.
 * @email：kevin-vension@foxmail.com
 * @desc character determines attitude, attitude determines destiny
 */
public class WXEnvelopeService extends AccessibilityService {

	static final String TAG = "Vension-红包助手";
	static final String ENVELOPE_TEXT_KEY = "[微信红包]";

	/**
	 * 微信几个页面的包名+地址。用于判断在哪个页面
	 * WX_LauncherUI-微信聊天界面
	 * LUCKEY_MONEY_RECEIVER-点击红包弹出的界面
	 * WX_LuckyMoneyDetailUI-红包领取后的详情界面
	 */
	private String WX_LauncherUI = "com.tencent.mm.ui.LauncherUI";
	private String WX_LuckyMoneyReceiveUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
	private String WX_LuckyMoneyDetailUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
	private String LUCK_MONEY_RECEIVE_PREFIX="com.tencent.mm.plugin.luckymoney.ui";

	/**
	 * 是否点击了红包
	 */
	private boolean isOpenRP = false;
	/**
	 * 是否点击了开按钮，打开了详情页面
	 */
	private boolean isOpenDetail = false;

	/**
	 * 获取PowerManager.WakeLock对象
	 */
	private PowerManager.WakeLock wakeLock;

	/**
	 * KeyguardManager.KeyguardLock对象
	 */
	private KeyguardManager.KeyguardLock _KeyguardLock;


	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int eventType = event.getEventType();
		switch (eventType) {
			//通知栏来信息，判断是否含有微信红包字样，是的话跳转
			case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
				Log.e(TAG,"通知栏状态改变");
				changeNotificationState(event);//通知栏状态改变
				break;
			//界面跳转的监听
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
				Log.e(TAG,"界面跳转的监听");
				changeWindowState(event);//界面跳转的监听
				break;
		}

	}

	/**通知栏状态改变*/
	private void changeNotificationState(AccessibilityEvent event) {
		List<CharSequence> texts = event.getText();
		if (texts.isEmpty())
			return;
		for (CharSequence text : texts) {
			String content = text.toString();
			if (!TextUtils.isEmpty(content)) {
				//判断是否含有[微信红包]字样
				if (content.contains(ENVELOPE_TEXT_KEY)) {
					Log.e(TAG, content);
					if (!isScreenOn()) {
						wakeUpScreen();
					}
					//如果有则打开微信红包页面
					openWeChatPage(event);
					isOpenRP = false;
				}
			}
		}
	}


	/**界面跳转的监听*/
	private void changeWindowState(AccessibilityEvent event) {
		String className = event.getClassName().toString();

		//判断是否是微信聊天界面
		if (WX_LauncherUI.equals(className)) {
			Log.e(TAG,"进入列表页 找红包\n"+ className);
			showToast("进入列表页 找红包");
			//获取当前聊天页面的根布局
			AccessibilityNodeInfo rootNode = getRootInActiveWindow();
			//开始找红包
			findRedPacket(rootNode);
		}


		//判断是否是显示‘开’的那个红包界面
//                Log.e("spire==测试 哪个是开的ui名称：", className);  由于微信经常 变换开页面的ui名称，此处直接找到微信头部，进入
		if(className.indexOf(LUCK_MONEY_RECEIVE_PREFIX)== 0 && (!className.equals(WX_LuckyMoneyDetailUI))){
//                if (LUCKEY_MONEY_RECEIVER.equals(className)) {
			AccessibilityNodeInfo rootNode = getRootInActiveWindow();
			//开始抢红包
			Log.e(TAG,"spire点击了 之后红包进入 开的界面\n"+ className);
			showToast("开始抢红包");
			Log.e("", className);
			openRedPacket(rootNode);
		}

		//判断是否是红包领取后的详情界面
		if (isOpenDetail && WX_LuckyMoneyDetailUI.equals(className)) {
			Log.e(TAG,"spire领取之后的详细界面\n"+ className);
			showToast("领取后的详情界面");
			isOpenDetail = false;
			//返回桌面
			back2Home();
			//如果之前是锁着屏幕的则重新锁回去
			release();//释放一下资源。
		}
	}


	/**
	 * 开启红包所在的聊天页面
	 */
	private void openWeChatPage(AccessibilityEvent event) {
		//A instanceof B 用来判断内存中实际对象A是不是B类型，常用于强制转换前的判断
		if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
			Notification notification = (Notification) event.getParcelableData();
			//打开对应的聊天界面
			PendingIntent pendingIntent = notification.contentIntent;
			try {
				pendingIntent.send();
			} catch (PendingIntent.CanceledException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 遍历查找红包
	 */
	private void findRedPacket(AccessibilityNodeInfo rootNode) {
		if (rootNode != null) {
			//从最后一行开始找起
			for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
				AccessibilityNodeInfo node = rootNode.getChild(i);
				//如果node为空则跳过该节点
				if (node == null) {
					continue;
				}
				CharSequence text = node.getText();
				if (text != null && text.toString().equals("领取红包")) {
					AccessibilityNodeInfo parent = node.getParent();
					//while循环,遍历"领取红包"的各个父布局，直至找到可点击的为止
					while (parent != null) {
						if (parent.isClickable()) {
							//模拟点击
							Log.e(TAG,"spire发现了列表页的红包 执行点击");
							showToast("领取红包 执行点击");
							parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
							//isOpenRP用于判断该红包是否点击过
							isOpenRP = true;
							break;
						}
						parent = parent.getParent();
					}
				}
				//判断是否已经打开过那个最新的红包了，是的话就跳出for循环，不是的话继续遍历
				if (isOpenRP) {
					break;
				} else {
					findRedPacket(node);
				}

			}
		}
	}


	/**
	 * 开始打开红包
	 */
	private void openRedPacket(AccessibilityNodeInfo rootNode) {
		if (rootNode != null) {
			List<AccessibilityNodeInfo> openList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c2i");
			if (openList != null && openList.size() > 0){
				showToast("findAccessibilityNodeInfosByViewId点击拆红包");
				for (AccessibilityNodeInfo n : openList) {
					// 点击拆红包
					Log.e(TAG,"点击拆红包");
					showToast("点击拆红包");
					n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					isOpenDetail = true;
				}
			}else{
				showToast("Button点击拆红包");
				for (int i = 0; i < rootNode.getChildCount(); i++) {
					AccessibilityNodeInfo node = rootNode.getChild(i);
					Log.e(TAG,"spire openRedPacket==\n"+ node.getClassName().toString());
					showToast("点击拆红包");
					if ("android.widget.Button".equals(node.getClassName())) {
						Log.e(TAG,"spire 成功点开了\n" + node.getClassName().toString());
						showToast("成功点开了");
						node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
						isOpenDetail = true;
					}
					openRedPacket(node);
				}
			}
		}
	}


	/**
	 * 服务连接
	 */
	@Override
	protected void onServiceConnected() {
		Toast.makeText(this, "微信抢红包服务已开启", Toast.LENGTH_SHORT).show();
		super.onServiceConnected();
	}

	/**
	 * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
	 */
	@Override
	public void onInterrupt() {
		Toast.makeText(this, "微信抢红包服务已中断", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 服务断开
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		Toast.makeText(this, "微信抢红包服务已关闭", Toast.LENGTH_SHORT).show();
		return super.onUnbind(intent);
	}

	/**
	 * 返回桌面
	 */
	private void back2Home() {
		Intent home = new Intent(Intent.ACTION_MAIN);
		home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		home.addCategory(Intent.CATEGORY_HOME);
		startActivity(home);
	}

	/**
	 * 判断是否处于亮屏状态
	 *
	 * @return true-亮屏，false-暗屏
	 */
	private boolean isScreenOn() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
		Log.e(TAG, isScreenOn + "");
		return isScreenOn;
	}

	/**
	 * 解锁屏幕
	 */
	private void wakeUpScreen() {
		//获取电源管理器对象
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
		wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "bright");

		//点亮屏幕
		wakeLock.acquire(1000);
		//得到键盘锁管理器
		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		_KeyguardLock = km.newKeyguardLock("unlock");

		//解锁
		_KeyguardLock.disableKeyguard();
	}

	/**
	 * 释放keyguardLock和wakeLock
	 */
	public void release() {
		if (_KeyguardLock != null) {
			_KeyguardLock.reenableKeyguard();
			_KeyguardLock = null;
		}
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	private void showToast(String text){
//		Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
	}

}

