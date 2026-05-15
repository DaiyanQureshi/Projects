package com.daiyan.accountify.profile;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.daiyan.accountify.R;

import ultimate_files.database.database_connectivity;

public class profile extends AppCompatActivity {

    EditText user_business_name, user_name, user_phone_number, user_emailid, user_gstin;
    ImageView user_profile_save; // The member variable
    ImageView back;
    database_connectivity db_connect_obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById();
        back.setOnClickListener(v ->{finish();});
        if(db_connect_obj.is_table_exists(db_connect_obj.getTable_name_profile())){loadProfileData();}
        user_profile_save.setOnClickListener(v -> {saveOrUpdateProfileData();});
    }

    public void findViewById(){
        db_connect_obj = new database_connectivity(this);
        back = findViewById(R.id.back);
        user_business_name = findViewById(R.id.user_business_name);
        user_name = findViewById(R.id.user_name);
        user_phone_number = findViewById(R.id.user_phone_number);
        user_emailid = findViewById(R.id.user_emailid);
        user_gstin = findViewById(R.id.user_gstin);
        user_profile_save = findViewById(R.id.user_profile_save);

        db_connect_obj.create_table_profile();

    }

    private void loadProfileData() {
        Cursor cursor = db_connect_obj.get_data(db_connect_obj.getTable_name_profile());

        if (cursor != null && cursor.moveToFirst()) {
            user_business_name.setText(cursor.getString(1));
            user_name.setText(cursor.getString(2));
            user_phone_number.setText(cursor.getString(3));
            user_emailid.setText(cursor.getString(4));
            user_gstin.setText(cursor.getString(5));
        }

        if (cursor != null) {
            cursor.close();
        }
    }


    private void saveOrUpdateProfileData() {
        if (user_business_name.getText().toString().replace(" ", "").isEmpty()) {
            Toast.makeText(this, "Business name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues table_data = new ContentValues();
        table_data.put("user_business_name", user_business_name.getText().toString());
        table_data.put("user_name", user_name.getText().toString());
        table_data.put("user_phone_number", user_phone_number.getText().toString());
        table_data.put("user_emailid", user_emailid.getText().toString());
        table_data.put("user_gstin", user_gstin.getText().toString());

        boolean isSuccess;

            // Data exists, so UPDATE it (assuming row id 1)
            if(db_connect_obj.update_data(db_connect_obj.getTable_name_profile(), table_data, "id", "1")){
                Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();

            }
         else {
            // No data, so INSERT it
            if(db_connect_obj.insert_data(db_connect_obj.getTable_name_profile(), table_data)){
                Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();

            }
            else{
                Toast.makeText(this, "Error Saving Profile!", Toast.LENGTH_SHORT).show();

            }
        }

        }
}