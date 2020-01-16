package runningcode.net.epc;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

import static android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION;

/**
 * Author： chenchongyu
 * Date: 2020-01-13
 * Description:
 * 1.不能在xml里配置packageNames=com.tencent.mm,
 * 因为在Android7.0以上会收不不到TYPE_NOTIFICATION_STATE_CHANGED事件；
 */
public class EveryPennyCountService extends AccessibilityService {
    private static final String TAG = "EPC_TAG";

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (!TextUtils.equals("com.tencent.mm", event.getPackageName())) {
            return;
        }

        Log.d(TAG, event.toString());
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            //通知栏事件
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence text : texts) {
                    String content = text.toString();
                    Log.i(TAG, "text:" + content);
                    if (content.contains("[微信红包]")) {
                        //模拟打开通知栏消息
                        if (event.getParcelableData() != null
                                &&
                                event.getParcelableData() instanceof Notification) {
                            Notification notification = (Notification) event.getParcelableData();
                            PendingIntent pendingIntent = notification.contentIntent;
                            try {
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            //非通知栏事件    处理其他事件
            //获取整个窗口根节点
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
            if (nodeInfo == null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    List<AccessibilityWindowInfo> windows = getWindows();
                    for (AccessibilityWindowInfo window : windows) {
                        if (window.getType() == TYPE_APPLICATION) {
                            nodeInfo = window.getRoot();
                            break;
                        }
                    }

                }
            }

            if (nodeInfo == null) {
                return;
            }
            Log.i(TAG, "进入页面" + nodeInfo);

            //首页listview的带红包的item列表项的id
            List<AccessibilityNodeInfo> listItemNodes =
                    nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bah");
            Log.i(TAG, "listview:" + listItemNodes);
            if (listItemNodes != null && !listItemNodes.isEmpty()) {
                justToChat(listItemNodes);
            } else {
                //非列表页不关注
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    return;
                }
                //聊天页
                Log.i(TAG, "进入非列表页面");
                //聊天右下角的+号
                List<AccessibilityNodeInfo> chatNodes =
                        nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aqk");
                if (!chatNodes.isEmpty()) {
                    Log.i(TAG, "进入聊天页面");
                    //标题
                    List<AccessibilityNodeInfo> titleNodes =
                            nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/lt");
                    if (!titleNodes.isEmpty()) {
                        //在聊天页面
                        //判断标题最后是否是一个括号，括号中是数字，当然最好是用正则
                        String title = titleNodes.get(0).getText().toString();
                        if (!TextUtils.isEmpty(title)) {
                            if (title.contains("(")) {
                                int indexLeft = title.lastIndexOf("(");
                                String end = title.substring(indexLeft);
                                end = end.substring(1, end.length() - 1);
                                try {
                                    Integer.parseInt(end);
                                    //群聊

                                    open(event, nodeInfo);

                                } catch (Exception e) {
                                    //私聊
                                    open(event, nodeInfo);
                                }
                            } else {
                                //私聊 默认私聊
                                open(event, nodeInfo);
                            }
                        }
                    }
                } else {
                    //不知为何，在华为p30上，strike里根据nodeInfo获取元素获取不到，停顿200ms后可以
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    strike(event, nodeInfo);

                }
            }
        }
    }

    private void justToChat(List<AccessibilityNodeInfo> listItemNodes) {
        Log.i(TAG, "进入列表页面");
        //列表页（首页）
        for (AccessibilityNodeInfo itemNode : listItemNodes) {
//                    List<AccessibilityNodeInfo> nodes = itemNode.findAccessibilityNodeInfosByText("[微信红包]");
            List<AccessibilityNodeInfo> nodes = itemNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bal");
            if (nodes == null || nodes.isEmpty()) {
                continue;
            }
            AccessibilityNodeInfo node = nodes.get(0);
            Log.i(TAG, "node text:" + node.getText());

            if (!TextUtils.isEmpty(node.getText()) && TextUtils.indexOf(node.getText(), "[微信红包]") > -1) {
                //还要判断是否有未读消息
                List<AccessibilityNodeInfo> numsNodes =
                        itemNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/op");

                List<AccessibilityNodeInfo> redPointNodes =
                        itemNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bai");
                if (!numsNodes.isEmpty() || !redPointNodes.isEmpty()) {
                    //跳转到聊天页面
                    AccessibilityHelper.performClick(itemNode);
                    break;
                       /* CharSequence text = numsNodes.get(0).getText();
                        if (text != null) {
                            if (Integer.parseInt(text.toString()) != 0) {

                            }
                        }*/
                }
            }
        }
    }

    private void open(AccessibilityEvent event, AccessibilityNodeInfo nodeInfo) {

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("微信红包");
        if (list == null) {
            return;
        }

        if (list.isEmpty()) {
            //没有 直接返回
//            List<AccessibilityNodeInfo> backNodes =
//                    nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ls");
//            if (!backNodes.isEmpty()) {
//                AccessibilityHelper.performClick(backNodes.get(0));
//            }
        } else {
            //有 但是要检查是不是红包
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = list.get(i);
                AccessibilityNodeInfo parent = node.getParent().getParent();
                if (parent != null) {
                    //红包icon
                    List<AccessibilityNodeInfo> wxhbNodes =
                            parent.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/auj");

                    List<AccessibilityNodeInfo> hasList = parent.findAccessibilityNodeInfosByText("已领取");
                    List<AccessibilityNodeInfo> hasList2 = parent.findAccessibilityNodeInfosByText("已被领完");
                    hasList.addAll(hasList2);
                    if (hasList.size() > 0) {
                        Log.i(TAG, "已领取");
                        continue;
                    }

                    if (!wxhbNodes.isEmpty()) {
                        //是的 没错  领取红包
                        AccessibilityHelper.performClick(parent);
                        return;
                    }
                }
            }
        }
    }

    private void strike(AccessibilityEvent event, AccessibilityNodeInfo nodeInfo) {
        if (TextUtils.isEmpty(event.getClassName())) {
            return;
        }
        String className = event.getClassName().toString();
        Log.i(TAG, "enter strike." + className);
        if (TextUtils.indexOf(className, "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI") > -1) {
            //点中了红包 有两种操作 一种是点开红包  一种是手慢了
            Log.i(TAG, "start strike." + nodeInfo);
            //获取开按钮
            List<AccessibilityNodeInfo> kaiNodes =
                    getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/dan");
            //获取 手慢了 提示语句的控件
            List<AccessibilityNodeInfo> slowNodes =
                    nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/dam");
            //获取关闭按钮
            List<AccessibilityNodeInfo> closeNodes =
                    nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d84");
            Log.i(TAG, "start strike." + kaiNodes.size());
            if (!kaiNodes.isEmpty()) {
                //获取到开按钮 点击此按钮
//                NotifyHelper.playEffect(getContext(), getConfig());
                AccessibilityHelper.performClick(kaiNodes.get(0));
            } else {
                if (!slowNodes.isEmpty() && !closeNodes.isEmpty()) {
                    //手慢了 提示语句的控件 关闭对话框
                    AccessibilityHelper.performClick(closeNodes.get(0));
                }
            }
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyBeforeDetailUI".equals(className)) {
            //拆完红包后看详细的纪录界面 这里退出就好
            AccessibilityHelper.performBack(this);
        }
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "EPC is Interrupt!!");
    }
}
