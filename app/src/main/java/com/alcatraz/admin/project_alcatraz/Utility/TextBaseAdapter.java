package com.alcatraz.admin.project_alcatraz.Utility;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alcatraz.admin.project_alcatraz.R;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.Arrays;

public class TextBaseAdapter extends RecyclerView.Adapter<TextBaseAdapter.TextViewHolder> {
    private String[] data;
    private DiscreteScrollView recyclerView;
    private int textColor, selectedColor;

    public TextBaseAdapter(int[] data) {
        this.data = Arrays.toString(data).split("[\\[\\]]")[1].split(", ");
    }

    public TextBaseAdapter(int[] data, int textColor, int selectedColor) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            this.data = Arrays.stream(data).mapToObj(String::valueOf).toArray(String[]::new);
//        } else {
            this.data = Arrays.toString(data).split("[\\[\\]]")[1].split(", ");
//        }
        this.textColor = textColor;
        this.selectedColor = selectedColor;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = (DiscreteScrollView) recyclerView;

        this.recyclerView.setItemTransitionTimeMillis(150);
        this.recyclerView.setItemTransformer(new ScaleTransformer() {
            @Override
            public void transformItem(View item, float position) {
                float closenessToCenter = 1f - Math.abs(position);
                super.transformItem(item, position);
                ((TextView) item.findViewById(R.id.item_text)).setTextColor(ColorUtils.blendARGB(textColor, selectedColor, closenessToCenter));
            }
        });
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.list_text_layout;
    }

    @NonNull
    @Override
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
        holder.bindData(data[position], recyclerView.getCurrentItem() == position);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public class TextViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView vText;

        TextViewHolder(View view) {
            super(view);
            vText = view.findViewById(R.id.item_text);
            vText.setOnClickListener(this);
        }

        void bindData(String value, boolean selected) {
            vText.setText(value);
            if (selected)
                vText.setTextColor(selectedColor);
            else
                vText.setTextColor(textColor);
        }

        @Override
        public void onClick(View v) {
            recyclerView.smoothScrollToPosition(getAdapterPosition());
        }
    }
}
