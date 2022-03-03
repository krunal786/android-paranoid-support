package com.codebase.paranoidsupport.extras;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codebase.paranoidsupport.R;
import com.codebase.paranoidsupport.service.BaseActivity;
import com.codebase.paranoidsupport.service.NativeAdsAdapter;
import com.codebase.paranoidsupport.service.api.LocalAdModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LocalAdsAdapter extends NativeAdsAdapter<LocalAdModel> {

    private List<LocalAdModel> items;
    private final BaseActivity activity;
    public static final int CONTENT = 0;

    public LocalAdsAdapter(BaseActivity activity, int listAdDelta) {
        super(activity, listAdDelta);
        items = new ArrayList<>();
        this.activity = activity;
    }

    @Override
    public int itemCount() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder createView(@NonNull @NotNull ViewGroup viewGroup, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ad_item_local_ad, viewGroup, false));
    }

    @Override
    public void bindView(@NonNull @NotNull RecyclerView.ViewHolder baseHolder, int position) {
        onBindViewHolder((RecyclerViewHolder) baseHolder, position);
    }

    @Override
    public LocalAdModel itemAt(int position) {
        return items.get(position);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView ImgPopularTheme;
        TextView txtAppName;
        ConstraintLayout container;

        RecyclerViewHolder(View view) {
            super(view);
            this.ImgPopularTheme = view.findViewById(R.id.img_logo);
            this.txtAppName = view.findViewById(R.id.txt_app_name);
            this.container = view.findViewById(R.id.container);
        }
    }

    @Override
    public int viewType(int position) {
        return CONTENT;
    }

    public void onBindViewHolder(RecyclerViewHolder recyclerViewHolder, final int position) {
        recyclerViewHolder.txtAppName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        recyclerViewHolder.txtAppName.setSelected(true);
        recyclerViewHolder.txtAppName.setText(this.items.get(position).appName);

        try {
            Glide.with(recyclerViewHolder.ImgPopularTheme.getContext())
                    .load(this.items.get(position).appLogo)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(recyclerViewHolder.ImgPopularTheme);
        } catch (Exception e) {
            e.printStackTrace();
        }

        recyclerViewHolder.container.setOnClickListener(view -> {
            String packageName = items.get(position).appPackage;
            Uri uri = Uri.parse("market://details?id=" + packageName);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            } else {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
            try {
                activity.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
            }
        });
    }

    public void notify(List<LocalAdModel> newItems) {
        items = new ArrayList<>(newItems);
        notifyItemRangeInserted(0, newItems.size());
    }
}

