package com.kevin.vension.redenvelopeshelper.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.kevin.vension.redenvelopeshelper.R;
import com.kevin.vension.redenvelopeshelper.utils.Constant;
import com.kevin.vension.redenvelopeshelper.utils.SharedPerferenceUtil;
import com.kevin.vension.redenvelopeshelper.utils.WechatVersionUtils;

import java.util.List;

public class HelpService extends AccessibilityService {

    /**
     * 微信的包名
     */
    public static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /**
     * 红包消息的关键字
     */
    final String WECHAT_HONGBAO_TEXT_KEY = "[微信红包]";
    /**
     * 微信几个页面的包名+地址。用于判断在哪个页面
     */
    private String WX_LauncherUI = "com.tencent.mm.ui.LauncherUI";
    private String WX_LuckyMoneyDetailUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private String WX_LuckyMoneyReceiveUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";

    private String TAG = "Vension";
    private boolean isNotDO = false;    // 是否下一次接收时不处理。
    private Handler mHandler = new MHandler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private Context context;
    private boolean isClickOpenMoney;   // 是否点击了拆红包
    private boolean detailGetMoneyOk;   // 获取钱数量ok
    private boolean detailGetNameOk;    // 获取发钱的人OK
    private boolean clickRedMoney;      // 点击了红包
    private boolean isSelfSendMoney;    // 是否是自己发的红包
    private boolean isWechatAlway;   // 是否是聊天界面或者主界面
    private boolean isOpenNotification; // 打开了通知栏的微信红包
    private SharedPerferenceUtil sharedPerferenceUtil;
    private boolean isLockScreen = false;


    class MHandler extends Handler {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {

            if (context == null) {
                context = getApplicationContext();
            }
            if (sharedPerferenceUtil == null) {
                sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
            }

            boolean isOpen = sharedPerferenceUtil.getBoolean(Constant.IS_RUNNING_SERVICE, true);
            if (!isOpen) {
                return;
            }

            if (event.getPackageName().toString().equals(WECHAT_PACKAGENAME)) {
                if (event.getText() != null && event.getText().size() > 0 &&
                        (event.getText().get(0).toString().contains("请勿使用红包辅助软件"))
                        && "android.widget.Toast$TN".equals(event.getClassName())) {
                    return;
                }

                if (event.getClassName().equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI")) {
                    // 微信红包界面，说明可能是发红包了
                    isNotDO = false;
                    isOpenNotification = true;
                }

                int eventType = event.getEventType();
                if (event.getText() != null && event.getText().size() > 0 &&
                        (event.getText().get(0).equals("请求不成功，请稍后再试") || event.getText().get(0).equals("系统繁忙，请稍后再试"))
                        && "android.widget.Toast$TN".equals(event.getClassName())) {
                    isNotDO = true;
                    return;
                }

                //通知栏事件
                if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                    List<CharSequence> texts = event.getText();
                    if (texts != null && !texts.isEmpty()) {
                        for (CharSequence t : texts) {
                            String text = String.valueOf(t);
                            if (text.contains(WECHAT_HONGBAO_TEXT_KEY)) {
                                isNotDO = false;
                                isOpenNotification = true;
                                isWechatAlway = false;
                                openNotify(event);
                                break;
                            }
                        }
                    }
                } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

                    S("界面改变");
                    try {
                        openHongBao(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                    S("滑动改变11111");
                    CharSequence className = event.getClassName();
                    if (className.equals("android.widget.ListView")) {
                        S("滑动改变22222");
                        robNewRedMoney(event);
                    }
                } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    contentChange(event);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 内容改变
     *
     * @param event
     */

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void contentChange(AccessibilityEvent event) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        if (WechatVersionUtils.canFindViewById(context) && WechatVersionUtils.canRobWechatVersion(context)) {

            List<AccessibilityNodeInfo> exitNodeInfos = root.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getWechatExitId(context));

            if (exitNodeInfos != null && exitNodeInfos.size() > 0) {
                // 是聊天界面
                if (isWechatAlway) {
                }
            } else {
                isWechatAlway = false;
            }
        }
    }

    /**
     * 抢新发的红包
     *
     * @param event
     */
    private void robNewRedMoney(AccessibilityEvent event) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        if (WechatVersionUtils.canFindViewById(context) && WechatVersionUtils.canRobWechatVersion(context)) {

            List<AccessibilityNodeInfo> exitNodeInfos = root.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getWechatExitId(context));
            if (exitNodeInfos != null && exitNodeInfos.size() > 0) {
                // 是聊天界面
                List<AccessibilityNodeInfo> wechatList = root.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getWechatListId(context));
                if (wechatList != null && wechatList.size() > 0) {
                    AccessibilityNodeInfo accessibilityNodeInfo = wechatList.get(0);

                    int itemCount = event.getItemCount();
                    int oldCount = sharedPerferenceUtil.getInteger(Constant.WECHAT_UI_LIST_COUNT, itemCount);

                    S(itemCount+"      "+oldCount);
                    sharedPerferenceUtil.putInteger(Constant.WECHAT_UI_LIST_COUNT, itemCount);
                    int i = itemCount - oldCount;
                    if (i <= 0) {
                        return;
                    }
                    S("走到这里咯啊吗？11111");
                    AccessibilityNodeInfo nodeInfo = accessibilityNodeInfo.getChild(accessibilityNodeInfo.getChildCount() - 3);
                    if (nodeInfo == null) {
                        return;
                    }
                    S("走到这里咯啊吗？2222");
                    List<AccessibilityNodeInfo> getRedMoneyList = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getWechatGetRedMoneyId(context));
                    if (getRedMoneyList != null && getRedMoneyList.size() > 0) {
                        try {
                            isNotDO = false;
                            S("走到这里咯啊吗？3333");
                            WeChatUIRobRedMoney(root);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 打开通知栏消息
     */
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }

        Notification notification = (Notification) event.getParcelableData();
        if (notification == null) {
            return;
        }
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
            if (mHandler == null) {
                mHandler = new MHandler();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    clickMoneyAndKey();
                }
            }, 200);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

//    private void openLockScreen() {
//        boolean is_lock_screen_rob = sharedPerferenceUtil.getBoolean(Constant.IS_LOCK_SCREEN_ROB, false);
//        if (is_lock_screen_rob) {
//            isLockScreen = true;
//            SleepManager.wakeUpAndUnlock(context);
//            sendBroadcast(new Intent("hello.hongbaoqiangguang_notification_close_window_lock"));
//        }
//    }

    private void closeLockScreen() {
//        boolean is_lock_screen_rob = sharedPerferenceUtil.getBoolean(Constant.IS_LOCK_SCREEN_ROB, false);
//        if (isLockScreen && is_lock_screen_rob) {
//            isLockScreen = false;
//        }
    }

    private void openHongBao(AccessibilityEvent event) {
        String className = event.getClassName().toString();
        if (className.equals(WX_LuckyMoneyReceiveUI)) {
            // 弹框界面
            isNotDO = true;
            isClickOpenMoney = false;
            isWechatAlway = false;
            openMoney();
        } else if (className.equals(WX_LuckyMoneyDetailUI)) {
            // 详情界面
            isNotDO = true;
            isWechatAlway = false;
            getMoneyNumberAndPerson();
//            performGlobalAction(GLOBAL_ACTION_BACK);
        } else if (className.equals(WX_LauncherUI)) {
            // 在聊天界面或主界面
            clickMoneyAndKey();
        }
        // 此处逻辑保留，以后可能会使用，勿删
//        else if ("com.tencent.mm.ui.base.p".equals(event.getClassName())) {
//            isNotDO = true;
//            isWechatAlway = false;
//        }
        else {
            isWechatAlway = false;
        }
    }

    private void getMoneyNumberAndPerson() {

        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            S(TAG + "rootWindow为空");
            return;
        }

        if (WechatVersionUtils.canRobWechatVersion(context) && WechatVersionUtils.canFindViewById(context)) {
            // 可控的两个微信版本和4.3以上系统
            if (isClickOpenMoney) {
                // 如果是我点击过拆红包，获取钱
                isClickOpenMoney = false;
                clickRedMoney = false;
                List<AccessibilityNodeInfo> moneyNumbers = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getDetailMoneyNumberId(context));
                // 找到领取了多少钱
                float money = 0f;
                String sendMoneyName = "";
                for (AccessibilityNodeInfo n : moneyNumbers) {
                    try {
                        String str = n.getText().toString();
                        money = Float.parseFloat(str);
                        if (money != 0) {
                            isClickOpenMoney = false;
                            detailGetMoneyOk = true;

                            List<AccessibilityNodeInfo> sendMoneyPerson = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getDetailSendMoneyPeopleId(context));
                            // 找到发红包人的名字
                            for (AccessibilityNodeInfo n1 : sendMoneyPerson) {
                                try {
                                    String str2 = n1.getText().toString();
                                    sendMoneyName = getSendMoneyName(str2);
                                    if (!TextUtils.isEmpty(sendMoneyName)) {
                                        detailGetNameOk = true;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 点击返回
                List<AccessibilityNodeInfo> detailExit = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getDetailExitId(context));
                for (AccessibilityNodeInfo n : detailExit) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

                if (money != 0 && !TextUtils.isEmpty(sendMoneyName)) {
                    // 将数据存入数据库
                    robRedMoneyOk();
                }

            } else if (clickRedMoney) {
                clickRedMoney = false;
                // 不是刚才点击过的，但是是点击过红包。返回
                List<AccessibilityNodeInfo> detailExit = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getDetailExitId(context));
                for (AccessibilityNodeInfo n : detailExit) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

            }

            // 如果不自动回复，就在这里息屏吧
            boolean isAutoSendMessage = false;
            if (!isAutoSendMessage) {
                closeLockScreen();
            }

        } else {
            if (isClickOpenMoney) {
                // 如果是我点击过拆红包，获取钱
                isClickOpenMoney = false;
                clickRedMoney = false;
                List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(".");
                for (AccessibilityNodeInfo n : list) {
                    try {
                        String m = n.getText().toString();
                        try {
                            float money = Float.parseFloat(m);
                            String s = n.getParent().getChild(0).getText().toString();
                            String sendMoney = "";

                            String string_en = "Lucky Money from ";
                            String string_zh = "的红包";
                            if (s.lastIndexOf(string_zh) == (s.length() - string_zh.length()) && s.indexOf(string_en) != 0) {
                                sendMoney = getZhSendMoneyName(s);
                            } else if (s.indexOf(string_en) == 0 && s.lastIndexOf(string_zh) != 0) {
                                sendMoney = getEnSendMoneyName(s, string_en);
                            } else if (s.indexOf(string_en) == 0 && s.lastIndexOf(string_zh) == 0) {
                                // 中文
                                sendMoney = getZhSendMoneyName(s);
                            } else {
                                // 不是抢到红包的
                            }
//                        S(TAG + "   获取到的钱：" + money + "   发这钱的人：" + sendMoney);
                            if (money != 0 && !TextUtils.isEmpty(sendMoney)) {
                                // 将数据存入数据库
                                robRedMoneyOk();
                            }
                        } catch (Exception e) {
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else if (clickRedMoney) {
                clickRedMoney = false;
            }

        }
    }

    /**
     * 抢红包成功的回调
     */
    private void robRedMoneyOk() {
        // TODO 播放提示音
        boolean isPlayMusic = true;
        if (isPlayMusic && !isSelfSendMoney) {
            playMusic();
        }
    }

    /**
     * 播放音乐
     */
    private void playMusic() {
//        S(" 播放成功音乐了~");
        MediaPlayer mp = MediaPlayer.create(context, R.raw.rob_success);
        mp.start();
        isOpenNotification = true;
    }

    private String getSendMoneyName(String s) {
        String sendMoney;
        sendMoney = getZhSendMoneyName(s);
        return sendMoney;
    }

    private String getZhSendMoneyName(String s) {
        String sendMoney;
        sendMoney = s.substring(0, s.length() - 3);
        return sendMoney;
    }

    private String getEnSendMoneyName(String s, String string_en) {
        String sendMoney;
        sendMoney = s.substring(string_en.length());
        return sendMoney;
    }

    private void openMoney() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            S(TAG + "rootWindow为空");
            return;
        }

        if (WechatVersionUtils.canFindViewById(context) && WechatVersionUtils.canRobWechatVersion(context)) {
            // 系统和微信都是支持抢红包的时候运行此段代码

            List<AccessibilityNodeInfo> openList = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getDialogOpenMoneyId(context));
            if (openList == null || openList.isEmpty() && clickRedMoney) {
                // 找不到拆红包和開
                // 帮助点击关闭按钮
                List<AccessibilityNodeInfo> closeList = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getDialogExitButtonId(context));
                for (AccessibilityNodeInfo n : closeList) {
                    isClickOpenMoney = false;
                    clickRedMoney = false;
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                isNotDO = true;
                closeLockScreen();
                return;
            }

            for (AccessibilityNodeInfo n : openList) {
                // 点击拆红包
                T("点击拆红包");
                isClickOpenMoney = true;
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else {
            T("拆红包");
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
            for (AccessibilityNodeInfo n : list) {
                isClickOpenMoney = true;
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            try {
                List<AccessibilityNodeInfo> openList = nodeInfo.findAccessibilityNodeInfosByText("发了一个红包，金额随机");
//                S(" 正在找開字");
                for (AccessibilityNodeInfo n : openList) {
                    AccessibilityNodeInfo parent = n.getParent();
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        CharSequence text = parent.getChild(i).getText();
                        S(" 找到：" + text);
                    }
                    parent.getChild(parent.getChildCount() - 2).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isClickOpenMoney = true;
                    S(" 有找到開字；非常OK");
//            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                List<AccessibilityNodeInfo> openList = nodeInfo.findAccessibilityNodeInfosByText("给你发了一个红包");
                for (AccessibilityNodeInfo n : openList) {
                    AccessibilityNodeInfo parent = n.getParent();
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        CharSequence text = parent.getChild(i).getText();
                    }

                    parent.getChild(parent.getChildCount() - 2).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isClickOpenMoney = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<AccessibilityNodeInfo> list2 = nodeInfo.findAccessibilityNodeInfosByText("手慢了，红包派完了");
            for (AccessibilityNodeInfo n : list2) {
                isClickOpenMoney = false;
                clickRedMoney = false;
                isNotDO = true;
                closeLockScreen();
            }

            List<AccessibilityNodeInfo> list3 = nodeInfo.findAccessibilityNodeInfosByText("该红包已被别人领取");
            for (AccessibilityNodeInfo n : list3) {
                isClickOpenMoney = false;
                clickRedMoney = false;
                isNotDO = true;
                closeLockScreen();
            }

            List<AccessibilityNodeInfo> list4 = nodeInfo.findAccessibilityNodeInfosByText("超过1天未领取，红包已失效");
            for (AccessibilityNodeInfo n : list4) {
                isClickOpenMoney = false;
                clickRedMoney = false;
                isNotDO = true;
                closeLockScreen();
            }
        }
    }

    private void clickMoneyAndKey() {
        try {
            final AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
            if (nodeInfo == null) {
                S(" rootWindow为空");
                return;
            }

            if (WechatVersionUtils.canFindViewById(context) && WechatVersionUtils.canRobWechatVersion(context)) {
                boolean isWechatUI;
                List<AccessibilityNodeInfo> exitNodeInfos = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getWechatExitId(context));
                if (exitNodeInfos != null && exitNodeInfos.size() > 0) {
                    isWechatUI = true;
                    if (isWechatAlway && !isOpenNotification) {
                        closeLockScreen();
                        return;
                    }

                    if (!isOpenNotification) {

                        closeLockScreen();
                        return;
                    }
                    isOpenNotification = false;
                    isWechatAlway = true;
                } else {
                    isWechatAlway = false;
                    isWechatUI = false;
                }
                S("isWechatUI:" + isWechatUI + " isNotDO:" + isNotDO + " detailGetMoneyOk:" + detailGetMoneyOk + " detailGetNameOk:" + detailGetNameOk + " clickRedMoney:" + clickRedMoney);
                if (isWechatUI) {   // 是聊天界面
                    WeChatUIRobRedMoney(nodeInfo);

                } else {
                    WeChatUIRobRedMoney(nodeInfo);
                }

            } else {
                // 普通方式抢红包
                List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
                if (list.isEmpty()) {
                    // 没有领取红包，就找到微信红包，并且点击
                    list = nodeInfo.findAccessibilityNodeInfosByText(WECHAT_HONGBAO_TEXT_KEY);
                    for (AccessibilityNodeInfo n : list) {
                        CharSequence contentDescription = n.getContentDescription();
                        if (contentDescription != null && !contentDescription.equals("") && !contentDescription.equals("null") && contentDescription.toString().contains(WECHAT_HONGBAO_TEXT_KEY)) {
                            String content = contentDescription.toString();
                            String[] split = content.split(",");
                            if (!TextUtils.isEmpty(split[2]) && split[2].contains(WECHAT_HONGBAO_TEXT_KEY)) {
                                // 关键词需要是在主屏，并且关键词不是好友姓名
                                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                mHandler = new Handler();
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
                                        if (rootInActiveWindow == null) {
                                            return;
                                        }
                                        List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByText("领取红包");

                                        List<AccessibilityNodeInfo> list2 = rootInActiveWindow.findAccessibilityNodeInfosByText("查看红包");
                                        Rect outBounds = new Rect();
                                        if (list2 != null && list2.size() > 0) {
                                            list2.get(0).getBoundsInScreen(outBounds);
                                            for (AccessibilityNodeInfo l : list2) {
                                                Rect rect = new Rect();
                                                l.getBoundsInScreen(rect);
                                                S(TAG + " rect:" + rect + " rect.centerY():" + rect.centerY());
                                            }
                                        } else {
                                            outBounds = null;
                                        }

                                        // 最新的红包领取
                                        for (int i = list.size() - 1; i >= 0; i--) {
                                            AccessibilityNodeInfo accessibilityNodeInfo = list.get(i);
                                            if (accessibilityNodeInfo == null) {
                                                return;
                                            }
                                            AccessibilityNodeInfo parent = accessibilityNodeInfo.getParent();
                                            if (parent != null) {
                                                S(TAG + "-->领取红包:" + parent);
                                                try {
                                                    Rect rect = new Rect();
                                                    parent.getBoundsInScreen(rect);
                                                    boolean robSelfMoney = true;
//                                                S(TAG + " rect.bottom：" + rect + " " + rect.centerY() + "  outBounds：" + outBounds + "  outBounds:" + outBounds.centerY());
                                                    if (robSelfMoney && outBounds != null && rect != null && rect.centerY() < outBounds.centerY() && list2 != null) {
                                                        // 查看红包比领取红包更新，就点击领取红包
//                                                    S(TAG + " rect.bottom：" + rect.bottom + "  outBounds.bottom：" + outBounds.bottom);
//                                                list2.get(list.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);

                                                        delayRob(list2.get(list.size() - 1));

                                                    } else {
//                                                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                        delayRob(parent);
                                                        isNotDO = true;
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }, 100);

                            }
                        }
                        break;
                    }

                    boolean robSelfMoney = true;
                    if (robSelfMoney) {
                        list = nodeInfo.findAccessibilityNodeInfosByText("查看红包");
                        for (AccessibilityNodeInfo n : list) {
//                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            delayRob(n);
                            isNotDO = true;
                            break;
                        }
                    }
                } else {
                    if (isNotDO) {
                        isNotDO = false;
                        return;
                    }

                    S(" 到这里了");
                    List<AccessibilityNodeInfo> list2 = nodeInfo.findAccessibilityNodeInfosByText("查看红包");
                    Rect outBounds = new Rect();
                    if (list2 != null && list2.size() > 0) {
                        list2.get(0).getBoundsInScreen(outBounds);
                    } else {
                        outBounds = null;
                    }
                    // 最新的红包领起
                    for (int i = list.size() - 1; i >= 0; i--) {
                        AccessibilityNodeInfo parent = list.get(i).getParent();
                        if (parent != null) {

                            boolean robSelfMoney = true;
                            if (robSelfMoney) {
                                Rect rect = new Rect();
                                list.get(list.size() - 1).getBoundsInScreen(rect);
                                if (outBounds != null && rect != null && rect.bottom < outBounds.bottom) {
                                    // 查看红包比领取红包更新，就点击领取红包
//                                list2.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    delayRob(list2.get(0).getParent());
                                } else {
//                                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    delayRob(parent);
                                }
                            } else {
//                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                delayRob(parent);
                            }
                            isNotDO = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SharedPerferenceUtil getSharedPerferenceUtil() {
        if (context == null) {
            context = getApplicationContext();
        }
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }
        return sharedPerferenceUtil;
    }

    private void delayRob(final AccessibilityNodeInfo nodeInfo1) {
        if (nodeInfo1 == null) {
            return;
        }
        if (context == null) {
            context = getApplicationContext();
        }
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }


        if (mHandler == null) {
            mHandler = new MHandler();
        }
        S("  延时设置完毕~");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    S("  点击~");
                    nodeInfo1.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    S("  点击完毕~");
                    clickRedMoney = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0);
    }

    private void WeChatUIRobRedMoney(AccessibilityNodeInfo nodeInfo) {
        if (isSelfSendMoney) {
            // 自己发的，回复一下数据
            detailGetMoneyOk = false;
            detailGetNameOk = false;
        }



        if (!isNotDO) {
            S(" 准备点击红包--");
            // 红包
            List<AccessibilityNodeInfo> redMoneyItems = nodeInfo.findAccessibilityNodeInfosByViewId(WechatVersionUtils.getWechatGetRedMoneyId(context));
            S("  redMoneyItems:" + redMoneyItems);
            if (redMoneyItems != null && redMoneyItems.size() > 0) {
                AccessibilityNodeInfo nodeInfo1 = redMoneyItems.get(redMoneyItems.size() - 1);
                S(" 要开始点击红包了:" + nodeInfo1.getText().toString());
                if (nodeInfo1.getText().toString().equals("查看红包")||nodeInfo1.getText().toString().equals("领取红包")) {
                    // TODO 是否抢自己的红包
                    boolean isRobSelf = true;
                    if (!isRobSelf) {
                        return;
                    }
                    isSelfSendMoney = true;
                } else {
                    isSelfSendMoney = false;
                }
                // TODO 随机延时 固定延时
                delayRob(nodeInfo1.getParent());
            }
        }
        isNotDO = false;
    }


    @Override
    public void onInterrupt() {
        if (context == null) {
            context = getApplicationContext();
        }
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        if (context == null) {
            context = getApplicationContext();
        }
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (context == null) {
            context = getApplicationContext();
        }
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }

    }

    private void S(Object s) {
        System.out.println(TAG + s);
    }

    private void T(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

}
