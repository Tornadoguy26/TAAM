package com.example.taam;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taam.structures.Item;

import java.util.ArrayList;

public class ReportDialog {

    private final Dialog reportDialog;
    private final Spinner reportSpinner;
    private final EditText reportSearch;
    private final CheckBox reportCheckBox;

    private final PdfPresenter pdfPresenter;
    private ArrayList<Item> dataSet;

    public ReportDialog(AppCompatActivity activity) {
        pdfPresenter = new PdfPresenter(activity);
        reportDialog = new Dialog(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View reportView = inflater.inflate(R.layout.report_layout, null);
        reportDialog.setContentView(reportView);

        Button reportCancel = reportView.findViewById(R.id.reportCancelButton);
        reportCancel.setOnClickListener(v -> reportDialog.dismiss());

        reportSearch = reportView.findViewById(R.id.reportSearchInput);

        reportCheckBox = reportView.findViewById(R.id.reportCheckBox);

        TextView reportCheckDesc = reportView.findViewById(R.id.reportCheckBoxDesc);

        reportSpinner = reportView.findViewById(R.id.reportSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.report_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportSpinner.setAdapter(adapter);
        reportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (!selectedItem.equals("Select All Items")) {
                    reportSearch.setVisibility(View.VISIBLE);
                    reportSearch.setHint("Enter " + selectedItem.toLowerCase());
                } else {
                    reportSearch.setVisibility(View.GONE);
                }
                if (selectedItem.equals("Lot Number") || selectedItem.equals("Name")) {
                    reportCheckDesc.setVisibility(View.GONE);
                    reportCheckBox.setChecked(false);
                    reportCheckBox.setVisibility(View.GONE);
                } else {
                    reportCheckDesc.setVisibility(View.VISIBLE);
                    reportCheckBox.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        Button reportGenerate = reportView.findViewById(R.id.reportGenerateButton);
        reportGenerate.setOnClickListener(v -> pdfPresenter.generateReport(reportSpinner.getSelectedItem().toString(),
                reportSearch.getText().toString(), reportCheckBox.isChecked(), dataSet));
    }

    public void show(ArrayList<Item> dataSet) {
        reportDialog.show();
        this.dataSet = dataSet;
    }

}
