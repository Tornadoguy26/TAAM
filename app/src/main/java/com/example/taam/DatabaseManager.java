package com.example.taam;


import static com.google.android.material.internal.ContextUtils.getActivity;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taam.structures.Item;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    static DatabaseManager databaseManager;
    private static FirebaseDatabase db;
    private static DatabaseReference dbRef;
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private Context context;
    private DatabaseManager() {

    }

    public static DatabaseManager getInstance() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
            db = FirebaseDatabase.getInstance("https://taam-1c732-default-rtdb.firebaseio.com/");
            dbRef = db.getReference();
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
        }
        return databaseManager;
    }



    // This return a Task<Item>. To get the item, add onCompleteListener and do
    // task.getResult() in Activity to get Item.
    public Task<Item> getItemByLotNumber(int lotNumber){
        final TaskCompletionSource<Item> tcs = new TaskCompletionSource<>();
        dbRef.child("Items").child(Integer.toString(lotNumber)).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d("Error", "Failed to get Item from database");
                    }
                    Item item = task.getResult().getValue(Item.class);
                    tcs.setResult(item);
                });
        return tcs.getTask();
    }

    // Currently this function close the activity given after adding.
    // Further modifications is needed if we need to addItem without leabing the AddItemActivity
    public void addItem(Item item, Uri fileUri, AppCompatActivity activity){
        // Read from the database
        dbRef.child("Items").child(Integer.toString(item.getLotNumber())).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(activity, "An error has occurred when adding the object", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Item itemFound = task.getResult().getValue(Item.class);
                        if(task.getResult().exists()) {
                            Toast.makeText(activity, "Item" +
                                    "with the same lot number already exists", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            dbRef.child("Items").child(Integer.toString(item.getLotNumber())).setValue(item)
                                    .addOnCompleteListener(task2 -> {
                                        if(!task2.isSuccessful()) {
                                            Toast.makeText(activity, "Failed to add image!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Toast.makeText(activity, "Item added. Now uploading image ...!", Toast.LENGTH_SHORT).show();
//                                       // StorageReference imageRef = storageRef.child(Integer.toString(item.getLotNumber()));
                                        // UNCOMMENT THIS IF NEED TYPE OF THE DATA UPLOADED
//                                         StorageReference imageRef = storageRef.child(lotNumber + "." + type);
                                        StorageReference imageRef = storageRef.child(Integer.toString(item.getLotNumber()) + ".png" );


                                        UploadTask uploadTask = imageRef.putFile(fileUri);

                                        // If fail then ...
                                        uploadTask
                                                .addOnCompleteListener(task3 -> {
                                                    if(!task3.isSuccessful()){
                                                        Toast.makeText(activity, "Media failed to upload", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(activity, "Media has been uploaded", Toast.LENGTH_SHORT).show();
                                                    }
                                                    activity.finish();
                                                });
                                    });

                        }
                    }
                });
    }


    public Task<Void> deleteItemInfoByLotNumber(int lotNumber) {
        return dbRef.child("Items").child(Integer.toString(lotNumber)).removeValue();
    }

    public Task<Void> deleteItemImageByLotNumber(int lotNumber) {
        StorageReference itemRef = storageRef.child(Integer.toString(lotNumber)
                + ".png");
        return itemRef.delete();
    }


    public void deleteItems(ArrayList<Item> items, AppCompatActivity activity) {

        List<Task <Void>> tasks = new ArrayList<Task<Void>>();

        for (int i = 0; i < items.size(); i++) {
            Task<Void> deleteItemTask = deleteItemInfoByLotNumber(items.get(i).getLotNumber());
            deleteItemTask.addOnCompleteListener(task -> {
                if(!task.isSuccessful())
                    Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            });
            tasks.add(deleteItemTask);
            deleteItemImageByLotNumber(items.get(i).getLotNumber());

        }
        items.clear();
        Tasks.whenAll(tasks).addOnCompleteListener(task -> {
            Toast.makeText(activity, "Selected items have been removed", Toast.LENGTH_SHORT).show();
        });
    }

    public void deleteItems(ArrayList<Item> items) {
        for (int i = 0; i < items.size(); i++) {
            deleteItemInfoByLotNumber(items.get(i).getLotNumber());
            deleteItemImageByLotNumber(items.get(i).getLotNumber());
        }
    }



}






