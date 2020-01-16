package runningcode.net.epc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;

import com.sixth.adwoad.AdwoAdView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String AD_ID = "4397d89153044c8aa5bd36faf6332d0b";
    private AdwoAdView adView;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int request = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int request2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

            if ((request + request2) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE}, 123);
            } else {
                initView();
            }
        } else {
            initView();
        }
    }

    private void initView() {
        View openView = findViewById(R.id.open);
        openView.setVisibility(View.VISIBLE);
        openView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });
        container = findViewById(R.id.ad_container);
        adView = new AdwoAdView(this, AD_ID, false, 20);
        adView.setBannerMatchScreenWidth(true);
        container.addView(adView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            initView();
        }
    }
}
