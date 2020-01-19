package runningcode.net.epc;

import android.app.Application;

import com.miui.zeus.mimo.sdk.MimoSdk;
import com.miui.zeus.mimo.sdk.api.IMimoSdkListener;

/**
 * 您可以参考本类中的代码来接入小米游戏广告SDK。在接入过程中，有如下事项需要注意：
 * 1.请将 APP_ID 值替换成您在小米开发者网站上申请的 AppID。
 */
public class AdApplication extends Application {
    // 请注意，千万要把以下的 APP_ID 替换成您在小米开发者网站上申请的 AppID。否则，可能会影响你的应用广告收益。
    private static final String APP_ID = "2882303761517411490";
    // 以下两个没有的话就按照以下传入
    private static final String APP_KEY = "fake_app_key";
    private static final String APP_TOKEN = "fake_app_token";

    @Override
    public void onCreate() {
        super.onCreate();

        // 如果担心sdk自升级会影响开发者自身app的稳定性可以关闭，
        // 但是这也意味着您必须得重新发版才能使用最新版本的sdk, 建议开启自升级
        //MimoSdk.setEnableUpdate(false);

        MimoSdk.setDebug(true); // 正式上线时候务必关闭debug模式
        // MimoSdk.setStaging(true); // 正式上线时候务必关闭stage模式

        // 如需要在本地预置插件,请在assets目录下添加mimo_asset.apk;
        MimoSdk.init(this, APP_ID, APP_KEY, APP_TOKEN, new IMimoSdkListener() {
            @Override
            public void onSdkInitSuccess() {
            }

            @Override
            public void onSdkInitFailed() {
            }
        });
    }
}
