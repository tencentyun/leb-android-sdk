package com.tencent.xbright.lebwebrtcdemo.main;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.xbright.lebwebrtcdemo.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bugly初始化
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppChannel("myChannel");  //设置渠道
        strategy.setAppVersion("2.0.1");      //App的版本, 这里设置了SDK version
        strategy.setAppPackageName("com.tencent.xbright.lebwebrtcdemo");  //App的包名
        CrashReport.initCrashReport(getApplicationContext(), "e3243444c9", false, strategy);

        setupNavigation();
    }

    private void setupNavigation() {
        BottomNavigationView navigationView = findViewById(R.id.nav_view);
        View publishMenu = navigationView.findViewById(R.id.navigation_publish);

        boolean enablePublish = true;
        try {
            Class.forName("com.tencent.xbright.lebwebrtcdemo.main.PublishTabFragment");
            publishMenu.setVisibility(View.VISIBLE);
        } catch (ClassNotFoundException ex) {
            publishMenu.setVisibility(View.GONE);
            enablePublish = false;
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Set<Integer> navIds = new HashSet<>();
        navIds.add(R.id.navigation_playback);
        if (enablePublish) {
            navIds.add(R.id.navigation_publish);
        };
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navIds).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }
}
