package runningcode.net.epc;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Author： chenchongyu
 * Date: 2020-01-14
 * Description:
 */
public class AccessibilityHelper {
    public static void performClick(AccessibilityNodeInfo nodeInfo) {

        if (nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            AccessibilityNodeInfo parent = nodeInfo.getParent();

            while (parent != null) {
                if (parent.isClickable()) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return;
                }

                parent = parent.getParent();
            }
        }

    }

    public static void performBack(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }
}
