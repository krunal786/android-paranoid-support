package com.codebase.paranoidsupport.extras;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.service.BaseActivity;
import com.codebase.paranoidsupport.service.NativeAdsAdapter;

import static com.codebase.paranoidsupport.service.AdsUtility.config;
import static com.codebase.paranoidsupport.service.AdsUtility.localAds;

public class LocalAdsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_activity_local_ads);

        RecyclerView rvLocalAds = findViewById(R.id.rvLocalAds);
        LocalAdsAdapter adapter = new LocalAdsAdapter(this, config.listNativeCount);
        rvLocalAds.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int itemViewType = adapter.getItemViewType(position);
                if (itemViewType == NativeAdsAdapter.AD) {
                    return 3;   //should be same as no of columns
                }
                return 1;   //one grid space
            }
        });
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvLocalAds.setLayoutManager(layoutManager);
        localAds.observe(this, adapter::notify);
    }
}