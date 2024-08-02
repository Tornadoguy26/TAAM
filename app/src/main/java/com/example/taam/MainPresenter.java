package com.example.taam;

import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.example.taam.structures.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;

public class MainPresenter {
    private final MainActivity mainActivity;
    private final FirebaseAuth auth;
    private final StorageReference storageReference;

    public MainPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void login(User user) {
        auth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mainActivity.onLoginSuccess();
                        mainActivity.switchAdminStatus(true);
                    } else {
                        mainActivity.onLoginFailure();
                    }
                });
    }

    public void generateReport(String searchBy, String searchValue, boolean descPic) {
        Toast.makeText(mainActivity, "not implemented yet", Toast.LENGTH_SHORT).show();
        /*
        int pageHeight = 1120;
        int pageWidth = 792;
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(pdfPath, "TAAM_Report.pdf");

        // Check if file already exists and create a unique filename
        int counter = 0;
        while (file.exists()) {
            counter++;
            file = new File(pdfPath, "TAAM(" + counter + ").pdf");
        }
        Toast.makeText(mainActivity, "Generating report", Toast.LENGTH_SHORT).show();
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint title = new Paint();

        // creates page metadata
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

         */
    }
}