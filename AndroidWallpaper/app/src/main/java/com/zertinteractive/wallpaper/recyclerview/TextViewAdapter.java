package com.zertinteractive.wallpaper.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zertinteractive.wallpaper.R;
import com.zertinteractive.wallpaper.viewmodels.ViewTextModel;

import java.util.List;
import java.util.Random;

/**
 * Created by Dell on 11/16/2015.
 */

public class TextViewAdapter extends RecyclerView.Adapter<TextViewAdapter.ViewHolder> implements View.OnClickListener {
    private Context context;
    private List<ViewTextModel> viewTextModels;
    private OnItemClickListener onItemClickListener;

    public TextViewAdapter(Context context, List<ViewTextModel> mViewTextModels) {
        this.context = context;
        this.viewTextModels = mViewTextModels;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        v.setOnClickListener(this);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ViewTextModel item = viewTextModels.get(position);
        int sizeOfText = Math.abs(new Random().nextInt(15)) % 15 + 11;
        int colorR = Math.abs(new Random().nextInt(136)) % 136 + 50; // range must be 50 to 185
        int colorG = Math.abs(new Random().nextInt(136)) % 136 + 50; // range must be 50 to 185
        int colorB = Math.abs(new Random().nextInt(136)) % 136 + 50; // range must be 50 to 185

        holder.textView.setText(item.getTite());
        holder.textView.setTextSize(sizeOfText);
        holder.textView.setTextColor(Color.rgb(colorR, colorR, colorR));
    }

    @Override
    public int getItemCount() {
        return viewTextModels.size();
    }

    @Override
    public void onClick(final View v) {
        if (onItemClickListener != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ViewTextModel mViewTextModels = (ViewTextModel) v.getTag();
                    onItemClickListener.onItemTextClick(v, mViewTextModels);
                }
            }, 100);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.grid_item_label);
        }
    }

    public interface OnItemClickListener {
        void onItemTextClick(View view, ViewTextModel mViewTextModel);
    }
}
