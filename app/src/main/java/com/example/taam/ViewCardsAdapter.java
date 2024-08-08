package com.example.taam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taam.structures.Item;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ViewCardsAdapter extends RecyclerView.Adapter<ViewCardsAdapter.ViewHolder> {

    private final ArrayList<Item> mDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView lotNumTextView;
        private final TextView nameTextView;
        private final TextView categoryTextView;
        private final TextView periodTextView;
        private final TextView descTextView;
        private final ImageView imageView;

        public ViewHolder(View v) {
            super(v);

            lotNumTextView = v.findViewById(R.id.lotNumTextView);
            nameTextView = v.findViewById(R.id.nameTextView);
            categoryTextView = v.findViewById(R.id.categoryTextView);
            periodTextView = v.findViewById(R.id.periodTextView);
            descTextView = v.findViewById(R.id.descTextView);
            imageView = v.findViewById(R.id.imageView);
        }
        public TextView getLotNumTextView() { return lotNumTextView; }
        public TextView getNameTextView() { return nameTextView; }
        public TextView getCategoryTextView() { return categoryTextView; }
        public TextView getPeriodTextView() { return periodTextView; }
        public TextView getDescTextView() { return descTextView; }
        public ImageView getImageView() { return imageView; }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet Item[] containing the data to populate views to be used by RecyclerView.
     */
    public ViewCardsAdapter(ArrayList<Item> dataSet) {
        mDataSet = dataSet;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getLotNumTextView().setText(String.format("#%s", mDataSet.get(position).getLotNumber()));
        holder.getNameTextView().setText(mDataSet.get(position).getName());
        holder.getCategoryTextView().setText(mDataSet.get(position).getCategory());
        holder.getPeriodTextView().setText(mDataSet.get(position).getPeriod());
        holder.getDescTextView().setText(mDataSet.get(position).getDescription());

        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference photoReference = DatabaseManager.getInstance().getPhotoReference(
                mDataSet.get(holder.getBindingAdapterPosition())
        );

        photoReference.getBytes(3 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.getImageView().setImageBitmap(bmp);

        }).addOnFailureListener(e -> {
            // Handle potential failures
        });

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}