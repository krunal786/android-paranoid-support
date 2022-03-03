package com.codebase.paranoidsupport.service;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.codebase.paranoidsupport.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public abstract class NativeAdsAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final BaseActivity context;
    private final int listAdDelta;
    public static final int AD = 1;

    public NativeAdsAdapter(BaseActivity context, int listAdDelta) {
        this.context = context;
        this.listAdDelta = listAdDelta;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == AD) {
            AdRecyclerHolder holder = new AdRecyclerHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.ad_item_native_container, viewGroup, false));
            context.nativeAd(holder.native_ad_container);
            return holder;
        }
        return createView(viewGroup, viewType);
    }

    static class AdRecyclerHolder extends RecyclerView.ViewHolder {
        public CardView native_ad_container;

        AdRecyclerHolder(View view) {
            super(view);
            this.native_ad_container = view.findViewById(R.id.native_ad_container);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int position) {
        if (getItemViewType(position) != AD) {
            bindView(baseHolder, getRealPosition(position));
        }
    }

    public abstract void bindView(@NonNull RecyclerView.ViewHolder baseHolder, int position);

    public abstract RecyclerView.ViewHolder createView(@NonNull ViewGroup viewGroup, int viewType);

    public abstract T itemAt(int position);

    public abstract int viewType(int position);

    public abstract int itemCount();

    private int getRealPosition(int position) {
        if (listAdDelta == 0) return position;

        int additionalContent = 0;
        int counter = 0;
        for (int i = 0; i < position; i++) {
            if (counter == listAdDelta) {
                counter = 0;
                additionalContent++;
            } else {
                counter++;
            }
        }
        return position - additionalContent;
    }

    @Override
    public int getItemCount() {
        int viewItems = itemCount();
        if (listAdDelta == 0) return viewItems;

        int additionalContent = 0;
        int counter = 0;
        for (int i = 0; i < viewItems; i++) {
            if (counter == listAdDelta) {
                counter = 0;
                additionalContent++;
            }
            counter++;
        }
        if (counter == listAdDelta) {
            additionalContent++;
        }
        return viewItems + additionalContent;
    }

    @Override
    public int getItemViewType(int position) {
        if (listAdDelta == 0) return viewType(position);

        List<Integer> adsIndexes = new ArrayList<>();
        int counter = 0;
        for (int i = 0; i < itemCount(); i++) {
            if (counter == listAdDelta) {
                counter = 0;
                adsIndexes.add(i + adsIndexes.size());
            }
            counter++;
        }
        if (counter == listAdDelta) {
            adsIndexes.add(itemCount() + adsIndexes.size());
        }
        if (adsIndexes.contains(position)) return AD;
        return viewType(getRealPosition(position));
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface NativeSize {
        int BIG = 0;
        int SMALL = 1;
    }
}
