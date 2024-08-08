package com.example.taam;

import android.app.AlertDialog;
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
    String fileName = "";
    Uri fileUri;
    Button buttonUpload;
    Button buttonSubmit;
    Button buttonBack;
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

        DatabaseManager databaseManager = DatabaseManager.getInstance();
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
        buttonUpload = findViewById(R.id.addScreen_UploadButton);
        buttonSubmit = findViewById(R.id.addScreen_SubmitButton);
        buttonBack = findViewById(R.id.addScreen_BackButton);
        buttonBack.setOnClickListener(v -> {
            this.finish();
        });
        buttonUpload.setOnClickListener(v -> imageChooser());

        buttonSubmit.setOnClickListener(v -> {
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
                Toast.makeText(this, "Invalid Inputs! Please fill " +
                        "all the fields", Toast.LENGTH_SHORT).show();
            }
            else {
                // make pop up confirmation
                new AlertDialog.Builder(this)
                        .setTitle("Add Confirmation")
                        .setMessage("Are you sure you want to add this item?")
                        // database
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                            Item new_item = new Item(Integer.parseInt(lotNumber),
                                    name,
                                    category,
                                    period,
                                    description);
                            databaseManager.addItem(new_item, fileUri, this);
                        }).setNegativeButton(android.R.string.no, (dialog, which) -> {
                            // User cancelled, do nothing
                            dialog.dismiss();
                        })
                        .setIcon(android.R.drawable.ic_input_add)
                        .show();


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
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        String[] uploadTypes = {"image/*", "video/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, uploadTypes);

        startActivityForResult(Intent.createChooser(intent, "Select Media File"), 200);
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
                    this.fileUri = selectedImageUri;
                    this.fileName = selectedImageUri.toString();
                    this.buttonUpload.setText(fileUri.getPath());
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