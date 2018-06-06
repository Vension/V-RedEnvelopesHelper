package com.kevin.vension.redenvelopeshelper.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.kevin.vension.redenvelopeshelper.service.HelpService;

public class WechatVersionUtils {

    /**
     * 校验版本是否相等
     *
     * @return 传入的版本与当前系统的版本刚好相等则返回true ，否则返回false
     */
    public static boolean versionVerificationOK(int version) {

        if (Build.VERSION.SDK_INT == version) {
            return true;
        }

        return false;
    }


    /**
     * 校验版本是否可用
     *
     * @return 如果当前系统的版本小于或等于传进来的版本，则返回true
     */
    public static boolean versionVerificationLessThanOK(int version) {

        if (Build.VERSION.SDK_INT <= version) {
            return true;
        }

        return false;
    }


    /**
     * 校验版本是否可用
     *
     * @return 如果当前系统的版本大于或等于传进来的版本，则返回true
     */
    public static boolean versionVerificationGreaterThanOK(int version) {

        if (Build.VERSION.SDK_INT >= version) {
            return true;
        }
        return false;
    }

    /**
     * 系统版本是否支持用id寻找控件
     *
     * @param context
     * @return
     */
    public static boolean canFindViewById(Context context) {
        return versionVerificationGreaterThanOK(18);
    }


    static int eightVersionCode = 821;
    static int nineVersionCode = 840;
    static int tenVersionCode = 980;
    static int elevenVersionCode = 1000;


    static String eightVersionName = "6.3.22";
    static String nineVersionName = "6.3.23";
    static String tenVersionName = "6.5.3";
    static String elevenVersionName = "6.5.4";


    /**
     * 微信中所需要的控件id
     */
    enum WechatFunctionId {
        WECHAT_EXIT_ID,                     // 聊天界面 退出id
        WECHAT_GET_RED_PACKET_ID,           // 聊天界面 领取红包id
        WECHAT_LIST_ID,                     // 聊天界面 列表id
        WECHAT_EDIT_TEXT_ID,                // 聊天界面 编辑框id
        WECHAT_EDIT_TEXT_IMAGE_BUTTON_ID,   // 聊天界面 表情按钮id
        WECHAT_SEND_BUTTON_ID,              // 聊天界面 发送按钮id
        DIALOG_EXIT_BUTTON_ID,              // 弹框 退出按钮id
        DIALOG_OPEN_MONEY_ID,               // 弹框 拆红包id
        DETAIL_EXIT_ID,                     // 详情界面 退出id
        DETAIL_MONEY_NUMBER_ID,             // 详情界面 红包钱数id
        DETAIL_SEND_MONEY_PEOPLE_ID,        // 详情界面 发红包的人
    }

    /**
     * 支持的微信版本
     */
    enum WechatVersion {
        /**
         * code=680,name=6.3.8.56_re6b2553
         */
        ONE,

        /**
         * code=700,name=6.3.9.48_refecd3e
         */
        TWO,

        /**
         * code=720,name=6.3.11.49_rc8fa1c5
         */
        THREE,

        /**
         * code=740,name=6.3.13.49_r4080b63
         */
        FOUR,

        /**
         * code=760,name=6.3.15.49_r8aff805
         */
        FIVE,

        /**
         * code=780,name=6.3.16.49_r03ae324
         */
        SIX,

        /**
         * code=800,name=6.3.18
         */
        SEVEN,
        /**
         * code=821,name=6.3.22
         */
        EIGHT,

        /**
         * code=840,name=6.3.23
         */
        NINE,

        /**
         * versionName:6.5.3  versionCode:980
         */
        TEN,

        /**
         * 6.5.4  versionCode:1000
         */
        ELEVEN,
        /**
         * 未知的微信版本
         */
        NO,
    }

    /**
     * 获取聊天界面的返回按钮id
     *
     * @param context
     * @return
     */
    public static String getWechatExitId(Context context) {
        return getId(context, WechatFunctionId.WECHAT_EXIT_ID);
    }


    /**
     * 获取聊天界面的红包id
     *
     * @param context
     * @return
     */
    public static String getWechatGetRedMoneyId(Context context) {
        return getId(context, WechatFunctionId.WECHAT_GET_RED_PACKET_ID);
    }


    /**
     * 获取聊天界面的消息列表id
     *
     * @param context
     * @return
     */
    public static String getWechatListId(Context context) {
        return getId(context, WechatFunctionId.WECHAT_LIST_ID);
    }

    /**
     * 获取输入框的id
     *
     * @param context
     * @return
     */
    public static String getWechatEditTextId(Context context) {
        return getId(context, WechatFunctionId.WECHAT_EDIT_TEXT_ID);
    }

    /**
     * 获取输入框旁表情按钮的id
     *
     * @param context
     * @return
     */
    public static String getWechatEditTextImageButtonId(Context context) {
        return getId(context, WechatFunctionId.WECHAT_EDIT_TEXT_IMAGE_BUTTON_ID);
    }

    /**
     * 获取发送按钮的id
     *
     * @param context
     * @return
     */
    public static String getWechatSendButtonId(Context context) {
        return getId(context, WechatFunctionId.WECHAT_SEND_BUTTON_ID);
    }


    /**
     * 获取弹框退出按钮的id
     *
     * @param context
     * @return
     */
    public static String getDialogExitButtonId(Context context) {
        return getId(context, WechatFunctionId.DIALOG_EXIT_BUTTON_ID);
    }


    /**
     * 获取弹框拆红包的id
     *
     * @param context
     * @return
     */
    public static String getDialogOpenMoneyId(Context context) {
        return getId(context, WechatFunctionId.DIALOG_OPEN_MONEY_ID);
    }


    /**
     * 获取详情界面退出的id
     *
     * @param context
     * @return
     */
    public static String getDetailExitId(Context context) {
        return getId(context, WechatFunctionId.DETAIL_EXIT_ID);
    }


    /**
     * 获取详情界面领取到红包数量金额的id
     *
     * @param context
     * @return
     */
    public static String getDetailMoneyNumberId(Context context) {
        return getId(context, WechatFunctionId.DETAIL_MONEY_NUMBER_ID);
    }


    /**
     * 获取详情界面谁发的红包的id
     *
     * @param context
     * @return
     */
    public static String getDetailSendMoneyPeopleId(Context context) {
        return getId(context, WechatFunctionId.DETAIL_SEND_MONEY_PEOPLE_ID);
    }


    private static String getId(Context context, WechatFunctionId functionId) {

        String wechat_exit_id = "";
        String wechat_get_red_packet_id = "";
        String wechat_list_id = "";
        String wechat_edit_text_id = "";
        String wechat_edit_text_image_button_id = "";
        String wechat_send_button_id = "";
        String dialog_exit_button_id = "";
        String dialog_open_money_id = "";
        String detail_exit_id = "";
        String detail_money_number_id = "";
        String detail_send_money_people_id = "";

        WechatVersion wechatVersion = getWechatVersion(context);
        if (wechatVersion == WechatVersion.NINE) {
            // code=840,name=6.3.23

                       /*
            点击领取红包后弹框
            */
            // 弹框退出按钮
            dialog_exit_button_id = "com.tencent.mm:id/gs";
            // 拆红包
            dialog_open_money_id = "com.tencent.mm:id/ba_";

            /*
            聊天界面
            */
            // 聊天界面退出(取按钮的上一级)
            wechat_exit_id = "com.tencent.mm:id/ew";
            // 领取红包按钮
            wechat_get_red_packet_id = "com.tencent.mm:id/a1n";
            // 聊天列表
            wechat_list_id = "com.tencent.mm:id/ye";
            // 编辑框
            wechat_edit_text_id = "com.tencent.mm:id/z4";
            // 表情
            wechat_edit_text_image_button_id = "com.tencent.mm:id/z5";
            // 发送按钮
            wechat_send_button_id = "com.tencent.mm:id/z_";

            /*
            详情界面的
            */
            // 详情界面退出按钮(取按钮的上一级)
            detail_exit_id = "com.tencent.mm:id/fa";
            // 金额
            detail_money_number_id = "com.tencent.mm:id/b8t";
            // 发红包的人
            detail_send_money_people_id = "com.tencent.mm:id/b8p";
        } else if (wechatVersion == WechatVersion.TEN) {
            // code=980,name=6.5.3

                       /*
            点击领取红包后弹框
            */
            // 弹框退出按钮
            dialog_exit_button_id = "com.tencent.mm:id/bed";
            // 拆红包
            dialog_open_money_id = "com.tencent.mm:id/be_";

            /*
            聊天界面
            */
            // 聊天界面退出(取按钮的上一级)
            wechat_exit_id = "com.tencent.mm:id/ga";
            // 领取红包按钮
            wechat_get_red_packet_id = "com.tencent.mm:id/a56";
            // 聊天列表
            wechat_list_id = "com.tencent.mm:id/a1d";

            /*
            详情界面的
            */
            // 详情界面退出按钮(取按钮的上一级)
            detail_exit_id = "com.tencent.mm:id/gr";
        } else if (wechatVersion == WechatVersion.ELEVEN) {
            // 弹框退出按钮
            dialog_exit_button_id = "com.tencent.mm:id/bed";
            // 开
//            dialog_open_money_id = "com.tencent.mm:id/bi3";
            dialog_open_money_id = "com.tencent.mm:id/c2i";

            /*
            聊天界面
            */
            // 聊天界面退出(取按钮的上一级)
            wechat_exit_id = "com.tencent.mm:id/gf";
            // 领取红包按钮
            wechat_get_red_packet_id = "com.tencent.mm:id/a5u";
            // 聊天列表
            wechat_list_id = "com.tencent.mm:id/a22";

            /*
            详情界面的
                    */
            // 详情界面退出按钮
            detail_exit_id = "com.tencent.mm:id/gv";
        }

        String id = "";
        if (functionId == WechatFunctionId.WECHAT_EXIT_ID) {
            id = wechat_exit_id;
        } else if (functionId == WechatFunctionId.WECHAT_GET_RED_PACKET_ID) {
            id = wechat_get_red_packet_id;
        } else if (functionId == WechatFunctionId.WECHAT_LIST_ID) {
            id = wechat_list_id;
        } else if (functionId == WechatFunctionId.WECHAT_EDIT_TEXT_ID) {
            id = wechat_edit_text_id;
        } else if (functionId == WechatFunctionId.WECHAT_EDIT_TEXT_IMAGE_BUTTON_ID) {
            id = wechat_edit_text_image_button_id;
        } else if (functionId == WechatFunctionId.WECHAT_SEND_BUTTON_ID) {
            id = wechat_send_button_id;
        } else if (functionId == WechatFunctionId.DIALOG_EXIT_BUTTON_ID) {
            id = dialog_exit_button_id;
        } else if (functionId == WechatFunctionId.DIALOG_OPEN_MONEY_ID) {
            id = dialog_open_money_id;
        } else if (functionId == WechatFunctionId.DETAIL_EXIT_ID) {
            id = detail_exit_id;
        } else if (functionId == WechatFunctionId.DETAIL_MONEY_NUMBER_ID) {
            id = detail_money_number_id;
        } else if (functionId == WechatFunctionId.DETAIL_SEND_MONEY_PEOPLE_ID) {
            id = detail_send_money_people_id;
        }
        return id;
    }

    /**
     * 获取微信的版本
     *
     * @param context
     * @return
     */
    private static WechatVersion getWechatVersion(Context context) {
        WechatVersion wechatVersion = WechatVersion.NO;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(HelpService.WECHAT_PACKAGENAME, 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            System.out.println("微信的 versionName:" + versionName + "  versionCode:" + versionCode);
            if (versionCode == eightVersionCode && versionName.contains(eightVersionName)) {
                wechatVersion = WechatVersion.EIGHT;
            } else if (versionCode == nineVersionCode && versionName.contains(nineVersionName)) {
                wechatVersion = WechatVersion.NINE;
            } else if (versionCode == tenVersionCode && versionName.contains(tenVersionName)) {
                wechatVersion = WechatVersion.TEN;
            } else if (versionCode == elevenVersionCode && versionName.contains(elevenVersionName)) {
                wechatVersion = WechatVersion.ELEVEN;
            }
            System.out.println("最终的结果是" + wechatVersion);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return wechatVersion;
    }


    /**
     * 获取微信的版本名称
     *
     * @param context
     * @return
     */
    public static String getWechatVersionCode(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(HelpService.WECHAT_PACKAGENAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //        int versionCode = packageInfo.versionCode;
        if (packageInfo != null) {
            return packageInfo.versionName + "_" + packageInfo.versionCode;
        } else {
            return "0.0";
        }
    }

    /**
     * 此版本的微信我们是否完美支持
     *
     * @param context
     * @return
     */
    public static boolean canRobWechatVersion(Context context) {
        boolean canRob = false;
        WechatVersion wechatVersion = getWechatVersion(context);
        if (wechatVersion == WechatVersion.ONE) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.TWO) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.THREE) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.FOUR) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.FIVE) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.SIX) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.SEVEN) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.EIGHT) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.NINE) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.TEN) {
            canRob = true;
        } else if (wechatVersion == WechatVersion.ELEVEN) {
            canRob = true;
        }
        return canRob;
    }

    /**
     * 主页面判断抢红包功能是否可用
     *
     * @param context
     * @return
     */
    public static boolean canRob(Context context) {
        return canRobWechatVersion(context) && canFindViewById(context);
    }

}
