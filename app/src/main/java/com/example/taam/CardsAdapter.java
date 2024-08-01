package com.example.taam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taam.structures.Item;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {

    private final ArrayList<Item> mDataSet;
    private int maxLine;

    public void setMaxLine(int maxLine){
        this.maxLine = maxLine;
    }
    private final ArrayList<Item> checkedItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final StorageReference storageReference;
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

            storageReference = FirebaseStorage.getInstance().getReference();
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
    public CardsAdapter(ArrayList<Item> dataSet) {
        mDataSet = dataSet;
        checkedItems = new ArrayList<>();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("[TAAM]", "Element " + position + " set");
        holder.getNameTextView().setText(mDataSet.get(position).getName());
        holder.getDescTextView().setText(mDataSet.get(position).getDescription());

        holder.getCheckBox().setOnCheckedChangeListener(null);
        holder.getCheckBox().setChecked(checkedItems.contains( mDataSet.get(position)) );

        holder.getCheckBox().setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) { checkedItems.add(mDataSet.get(holder.getAdapterPosition())); }
            else { checkedItems.remove(mDataSet.get(holder.getAdapterPosition())); }
        });

        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference photoReference = holder.storageReference.child(
                mDataSet.get(holder.getAdapterPosition()).getLotNumber() + ".png"
        );

        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.getImageView().setImageBitmap(bmp);

        }).addOnFailureListener(e -> {
            // Handle potential failures
        });
        if(this.maxLine != 0) {
            holder.descTextView.setMaxLines(this.maxLine);
        }
        holder.checkBox.setVisibility(View.VISIBLE);
        holder.checkBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (holder.checkBox.isChecked()) {
                    if(MainActivity.posNumberChecked.isEmpty()){
                        MainActivity.clickViewButton.setVisibility(View.VISIBLE);
                    }
                    MainActivity.posNumberChecked.add(mDataSet.get(holder.getAdapterPosition()));
                } else {
                    MainActivity.posNumberChecked.remove(mDataSet.get(holder.getAdapterPosition()));
                    if(MainActivity.posNumberChecked.isEmpty()){
                        MainActivity.clickViewButton.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}