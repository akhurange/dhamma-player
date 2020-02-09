package org.dhamma.dhammaplayer;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import org.dhamma.dhammaplayer.ui.main.SectionsPagerAdapter;

public class MainActivity extends BaseActivity {

    private TabLayout mTabLayout;
    private int[] tabIcons = {
            R.drawable.ic_alarm,
            R.drawable.ic_library_music,
            R.drawable.ic_event_note
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setupTabIcons();
    }

    private void setupTabIcons() {
        for (int i=0; i<3; i++) {
            mTabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }
        mTabLayout.getTabAt(0).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        for (int i=1; i<3; i++) {
            int tabIconColor = mTabLayout.getTabTextColors().getDefaultColor();
            mTabLayout.getTabAt(i).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
        }
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIconColor = Color.WHITE;
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = mTabLayout.getTabTextColors().getDefaultColor();
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
}