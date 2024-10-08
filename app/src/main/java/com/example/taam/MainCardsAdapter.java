package com.example.taam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taam.structures.Item;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainCardsAdapter extends RecyclerView.Adapter<MainCardsAdapter.ViewHolder> {

    private final ArrayList<Item> mDataSet;
    private final ArrayList<Item> checkedItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView descTextView;
        private final ImageView imageView;
        private final CheckBox checkBox;

        public ViewHolder(View v) {
            super(v);

            nameTextView = v.findViewById(R.id.nameTextView);
            descTextView = v.findViewById(R.id.descTextView);
            imageView = v.findViewById(R.id.imageView);
            checkBox = v.findViewById(R.id.checkBox);

            // Define click listener for the ViewHolder's View.
            /*
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.toggle();
                }
            });
             */

        }
        public TextView getNameTextView() { return nameTextView; }
        public TextView getDescTextView() { return descTextView; }
        public ImageView getImageView() { return imageView; }
        public CheckBox getCheckBox() { return checkBox; }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet Item[] containing the data to populate views to be used by RecyclerView.
     */
    public MainCardsAdapter(ArrayList<Item> dataSet) {
        mDataSet = dataSet;
        checkedItems = new ArrayList<>();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getNameTextView().setText(mDataSet.get(position).getName());
        holder.getDescTextView().setText(mDataSet.get(position).getDescription());

        holder.getCheckBox().setOnCheckedChangeListener(null);
        holder.getCheckBox().setChecked(checkedItems.contains( mDataSet.get(position)) );

        holder.getCheckBox().setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) { checkedItems.add(mDataSet.get(holder.getBindingAdapterPosition())); }
            else { checkedItems.remove(mDataSet.get(holder.getBindingAdapterPosition())); }
        });

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

    public ArrayList<Item> getCheckedItems() { return checkedItems; }
    public void clearCheckedItems() { checkedItems.clear(); }

}