package com.example.taam;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taam.structures.Item;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseManager {

    static DatabaseManager databaseManager;
    private static DatabaseReference dbRef;
    private static StorageReference storageRef;
    private static FirebaseAuth auth;

    public final static String[] categories, periods;
    static {
        categories = new String[]{"Clear", "Jade", "Paintings", "Calligraphy", "Rubbings", "Bronze",
                "Brass and Copper", "Gold and Silvers", "Lacquer", "Enamels"};
        periods = new String[]{"Clear", "Xia", "Shang", "Zhou", "Chuanqiu", "Zhanggou", "Qin", "Han",
                "Shangou", "Ji", "South and North", "Shui", "Tang", "Liao", "Song", "Jin",
                "Yuan", "Ming", "Qing", "Modern"};
    }

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
            FirebaseDatabase db = FirebaseDatabase.getInstance("https://taam-1c732-default-rtdb.firebaseio.com/");
            dbRef = db.getReference();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            auth = FirebaseAuth.getInstance();
        }
        return databaseManager;
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
                                        if (fileUri == null) {
                                            Toast.makeText(activity, "Item added", Toast.LENGTH_SHORT).show();
                                            activity.finish();
                                            return;
                                        }
                                        Toast.makeText(activity, "Item added. Now uploading image ...!", Toast.LENGTH_SHORT).show();
                                        StorageReference imageRef = storageRef.child(Integer.toString(item.getLotNumber()));
                                        // UNCOMMENT THIS IF NEED TYPE OF THE DATA UPLOADED
                                        // StorageReference imageRef = storageRef.child(lotNumber + "." + type);
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

    // public ArrayList<Item> readItems();
    public Task<Void> deleteItemInfo(Item item) {
        return dbRef.child("Items").child(Integer.toString(item.getLotNumber())).removeValue();
    }

    public Task<Void> deleteItemImage(Item item) {
        StorageReference itemRef = storageRef.child(item.getLotNumber() + item.getImageExtension());
        return itemRef.delete();
    }
    public void deleteItems(ArrayList<Item> items, Context applicationContext) {
        List<Task <Void>> tasks = new ArrayList<Task<Void>>();

        for (int i = 0; i < items.size(); i++) {
            Task<Void> deleteItemTask = deleteItemInfo(items.get(i));
            deleteItemTask.addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(applicationContext, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            tasks.add(deleteItemTask);
            deleteItemImage(items.get(i));
        }

        items.clear();

        Tasks.whenAll(tasks).addOnCompleteListener(task -> {
            Toast.makeText(applicationContext, "Selected items have been removed", Toast.LENGTH_SHORT).show();
        });
    }

    public Task<AuthResult> loginQuery(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public DatabaseReference getDbRef() { return dbRef; }

}






