package com.example.taam;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.webkit.MimeTypeMap;
import android.widget.*;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.taam.structures.Item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddItemActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener{
    String[] categories = {"Jade", "Paintings", "Calligraphy", "Rubbings", "Bronze",
            "Brass and Copper", "Gold and Silvers", "Lacquer", "Enamels"};
    String[] periods = {"Xia", "Shang", "Zhou", "Chuanqiu", "Zhanggou", "Qin", "Han",
            "Shangou", "Ji", "South and North", "Shui", "Tang", "Liao", "Song", "Jin",
            "Yuan", "Ming", "Qing", "Modern"};
    String filename;
    Uri filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_additem);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Spinner spinnerCategory = findViewById(R.id.addScreen_SpinnerCategory);
        spinnerCategory.setOnItemSelectedListener(this);
        ArrayAdapter adCategory = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, categories);
        adCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adCategory);

        Spinner spinnerPeriod = findViewById(R.id.addScreen_SpinnerPeriod);
        spinnerPeriod.setOnItemSelectedListener(this);
        ArrayAdapter adPeriod = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, periods);
        adPeriod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adPeriod);

        // get the items
        EditText ET_lotNumber = findViewById(R.id.addScreen_LotNumber);
        EditText ET_name = findViewById(R.id.addScreen_Name);
        Spinner S_category = findViewById(R.id.addScreen_SpinnerCategory);
        Spinner S_period = findViewById(R.id.addScreen_SpinnerPeriod);
        EditText ET_description = findViewById(R.id.addScreen_Description);
        Button B_upload = findViewById(R.id.addScreen_UploadButton);
        Button B_submit = findViewById(R.id.addScreen_SubmitButton);

        B_upload.setOnClickListener(v -> imageChooser());

        B_submit.setOnClickListener(v -> {
//                Take all input texts
//                Upload it to firebase database (realtime)
//                Upload the image to firebase cloud storage wit hthe name <id>.<ext>


            String lotNumber = ET_lotNumber.getText().toString();
            String name = ET_name.getText().toString();
            String category = S_category.getSelectedItem().toString();
            String period = S_period.getSelectedItem().toString();
            String description = ET_description.getText().toString();
            if(name.isEmpty() || category.isEmpty() || period.isEmpty() ||
                    description.isEmpty() || lotNumber.isEmpty()) {
                Toast.makeText(AddItemActivity.this, "Invalid Inputs! Please fill " +
                        "all the fields", Toast.LENGTH_SHORT).show();
            }
            else {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();

                // Read from the database
                myRef.child("Items").child(lotNumber).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(AddItemActivity.this, "An error has occurred when adding the object", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Item itemFound = task.getResult().getValue(Item.class);
                        if(task.getResult().exists()) {
                            Toast.makeText(AddItemActivity.this, "Item" +
                                    "with the same lot number already exists", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Item new_item = new Item(Integer.parseInt(lotNumber), name, category, period,
                                    description);
                            myRef.child("Items").child(lotNumber).setValue(new_item);
                            FirebaseStorage storage = FirebaseStorage.getInstance();


                            StorageReference storageRef = storage.getReference();
                            String type = getFileExtension(AddItemActivity.this, filepath);
                            StorageReference imageRef = storageRef.child(lotNumber + "." + type);

                            UploadTask uploadTask = imageRef.putFile(filepath);

// Register observers to listen for when the download is done or if it fails
                            uploadTask.addOnFailureListener(exception -> {
                                // Handle unsuccessful uploads
                                Toast.makeText(AddItemActivity.this, "Failed to upload image!", Toast.LENGTH_SHORT).show();
                            }).addOnSuccessListener(taskSnapshot -> Toast.makeText(AddItemActivity.this, "Image has been uploaded!", Toast.LENGTH_SHORT).show());
                        }
                    }
                });


//
            }

        });
    }
    public static String getFileExtension(Context context, Uri uri) {
        String extension = null;

        // First try to get extension from MimeTypeMap
        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(uri);
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }

        // If the extension is still null, get it from the URI itself
        if (extension == null) {
            String path = uri.getPath();
            if (path != null) {
                int lastDot = path.lastIndexOf('.');
                if (lastDot != -1) {
                    extension = path.substring(lastDot + 1);
                }
            }
        }

        return extension;
    }
    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 200);
    }



    public void onActivityResult ( int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    ImageView preview = findViewById(R.id.addScreen_PreviewImage);
                    preview.setVisibility(View.VISIBLE);
                    // update the preview image in the layout
                    preview.setImageURI(selectedImageUri);
                    this.filepath = selectedImageUri;
                    this.filename = selectedImageUri.toString();
                }
            }
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> arg0,
                               View arg1,
                               int position,
                               long id)
    {

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // Auto-generated method stub
    }


}