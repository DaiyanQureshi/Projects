package com.daiyan.accountify.suppliers;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.daiyan.accountify.R;
import ultimate_files.database.database_connectivity;

public class add_supplier extends AppCompatActivity {


    // Layout
    androidx.constraintlayout.widget.ConstraintLayout main;

    // ImageViews
    ImageView back;
    ImageView save_new_supplier;

    // TextViews
    TextView add_new_supplier_page_title;
    TextView store_name;

    // EditTexts
    EditText supplier_name;
    EditText mobile_number;
    EditText email_id;
    EditText gstin;
    Cursor cursor;

    // Database
    database_connectivity db_connect_obj;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_supplier);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initialization();
        findViewById();
        back.setOnClickListener(v ->{finish();});
        save_new_supplier.setOnClickListener(v -> {
            save_new_supplier_Data();
        });

    }

    public void save_new_supplier_Data() {
        // Validate that supplier name is not empty
        if (supplier_name.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Supplier name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if(db_connect_obj.is_table_exists(db_connect_obj.getTable_name_parties())){
            cursor = db_connect_obj.searchInColumn((db_connect_obj.getTable_name_parties()),"person_name",supplier_name.getText().toString());
        }
        if (cursor.getCount() >0 ) {
            Toast.makeText(this, "This supplier name already exsist. Create new supplier name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cursor != null) {
            cursor.close();
        }

        // Collect data into ContentValues
        ContentValues table_data = new ContentValues();
        table_data.put("trade_type", "purchase");
        table_data.put("person_name", supplier_name.getText().toString());
        table_data.put("phone_number", mobile_number.getText().toString());
        table_data.put("email_id", email_id.getText().toString());
        table_data.put("gst_number", gstin.getText().toString());

        // Insert data into the database
        if (db_connect_obj.insert_data(db_connect_obj.getTable_name_parties(), table_data)) {
            Toast.makeText(this, "New Supplier Saved!", Toast.LENGTH_SHORT).show();
            // Optionally, finish the activity after saving
            // finish();
        } else {
            Toast.makeText(this, "Error to Save New Supplier!", Toast.LENGTH_SHORT).show();
        }
    }
    public void initialization(){
        db_connect_obj = new database_connectivity(this);
    }
    public void findViewById() {
        // Initialize database helpers

        // Layout
        main = findViewById(R.id.main);

        // ImageViews
        back = findViewById(R.id.back);
        save_new_supplier = findViewById(R.id.save_new_supplier);

        // TextViews
        add_new_supplier_page_title = findViewById(R.id.add_new_supplier_page_title);
        store_name = findViewById(R.id.business_name);

        // EditTexts
        supplier_name = findViewById(R.id.supplier_name);
        mobile_number = findViewById(R.id.mobile_number);
        email_id = findViewById(R.id.email_id);
        gstin = findViewById(R.id.gstin);

        // Create the 'suppliers' table if it doesn't exist
db_connect_obj.create_table_parties();
    }
}