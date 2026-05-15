package com.daiyan.accountify.receive_payment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daiyan.accountify.R;
import ultimate_files.database.database_connectivity;
import ultimate_files.RecycleViewHelper;
import com.google.android.material.textfield.TextInputEditText;

public class Receive_payment extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView back;
    Button save;
    TextView business_name;
    database_connectivity db_connect_obj;
    TextInputEditText person_name, received_amount;
    Cursor cursorLoad;
    boolean ckicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receive_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById();
        initialization();
        onclickListener();
        item_name_search();

            }
    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            // Step 1: Check karo ki kya tumhara suggestions wala list open hai
            if (recyclerView.getVisibility() == android.view.View.VISIBLE) {

                // Step 2: RecyclerView ka screen par exact area nikalna
                android.graphics.Rect outRect = new android.graphics.Rect();
                recyclerView.getGlobalVisibleRect(outRect);

                // Step 3: person_name view ka area nikalna (Ye bohot zaroori hai!)
                android.graphics.Rect personRect = new android.graphics.Rect();
                person_name.getGlobalVisibleRect(personRect);

                // Step 4: Touch ke coordinates X aur Y lena
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                // Step 5: Agar click recyclerView ke bahar HUA HAI aur person_name ke bhi bahar HUA HAI
                if (!outRect.contains(x, y) && !personRect.contains(x, y)) {
                    // List ko hide kar do
                    recyclerView.setVisibility(android.view.View.GONE);

                    // Bonus Pro Tip: Agar keyboard khula hai to usko bhi automatically hide karna UX ke liye best hota hai
                    android.view.View view = this.getCurrentFocus();
                    if (view != null) {
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void onclickListener(){
        back.setOnClickListener(v -> finish());

        save.setOnClickListener(v -> {
            if(!ckicked){
                Toast.makeText(this, "Please select from given list", Toast.LENGTH_SHORT).show();
                return;
            }
            if(person_name.getText().toString().trim().isEmpty() || received_amount.getText().toString().trim().isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                save_data();
                ckicked = false;
                loadTable();
                person_name.setText("");
                received_amount.setText("");
                recyclerView.setVisibility(View.GONE);
            }
        });

//        person_name.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                loadTable();
//                set_item();
//                recyclerView.setVisibility(View.VISIBLE);
//            }
//        });
//        person_name.setOnClickListener(v->{
//            loadTable();
//            set_item();
//
//            recyclerView.setVisibility(View.VISIBLE);
//
//        });


    }

    public void save_data(){
        String name = person_name.getText().toString().trim();
        String amountStr = received_amount.getText().toString().trim();
        double paymentToApply = 0;

        try {
            paymentToApply = Double.parseDouble(amountStr);
        } catch (Exception e) {
            return;
        }

        if (paymentToApply <= 0) return;

        android.database.sqlite.SQLiteDatabase db = db_connect_obj.getWritableDatabase();

        // Sabse purane Sale bills uthao jinka pending > 0 hai
        Cursor cursor = db.rawQuery("SELECT id, pending, payment_amount FROM " + db_connect_obj.getTable_name_product() + " WHERE person_name = ? AND trade_type = 'sale' AND CAST(pending AS REAL) > 0 ORDER BY id ASC", new String[]{name});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                if (paymentToApply <= 0) break;

                String id = cursor.getString(0);
                double currentPending = cursor.getDouble(1);
                double currentPaid = cursor.getDouble(2);

                double amountToDeduct = Math.min(currentPending, paymentToApply);

                ContentValues cv = new ContentValues();
                cv.put("pending", String.valueOf(currentPending - amountToDeduct));
                cv.put("payment_amount", String.valueOf(currentPaid + amountToDeduct));

                // Ye UPDATE call karte hi database trigger khud payment_logs aur recent_activity set karega
                db.update(db_connect_obj.getTable_name_product(), cv, "id=?", new String[]{id});

                paymentToApply -= amountToDeduct;

            } while (cursor.moveToNext());
            cursor.close();
        }

        // Advance payment handling
        if (paymentToApply > 0) {
            Cursor lastBill = db.rawQuery("SELECT id, pending, payment_amount FROM " + db_connect_obj.getTable_name_product() + " WHERE person_name = ? AND trade_type = 'sale' ORDER BY id DESC LIMIT 1", new String[]{name});
            if (lastBill != null && lastBill.moveToFirst()) {
                String id = lastBill.getString(0);
                double currentPending = lastBill.getDouble(1);
                double currentPaid = lastBill.getDouble(2);

                ContentValues cv = new ContentValues();
                cv.put("pending", String.valueOf(currentPending - paymentToApply));
                cv.put("payment_amount", String.valueOf(currentPaid + paymentToApply));
                db.update(db_connect_obj.getTable_name_product(), cv, "id=?", new String[]{id});
                lastBill.close();
            }
        }

        Toast.makeText(this, "Payment Received & Handled by Database!", Toast.LENGTH_SHORT).show();
    }

    public void findViewById() {
        back = findViewById(R.id.back);
        save = findViewById(R.id.save);
        business_name = findViewById(R.id.business_name);
        recyclerView = findViewById(R.id.stock_item_recycle_view);
        person_name = findViewById(R.id.person_name);
        received_amount = findViewById(R.id.received_amount);
    }

    public void initialization(){
        db_connect_obj = new database_connectivity(this);
        loadTable();
    }

    public void loadTable(){
        // Get all parties properly
        cursorLoad = db_connect_obj.get_data(db_connect_obj.getTable_name_parties());
    }

    public void item_name_search() {
        person_name.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { set_item(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    public void set_item(){
        if (cursorLoad == null || cursorLoad.isClosed()) return;

        String[] columns = new String[]{"_id", "person_name", "phone_number", "pending", "trade_type"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        String searchInput = person_name.getText().toString().toLowerCase();

        if (cursorLoad.moveToFirst()) {
            int idCounter = 0;
            do {
                String dbPersonName = cursorLoad.getString(cursorLoad.getColumnIndexOrThrow("person_name"));
                if (searchInput.isEmpty() || dbPersonName.toLowerCase().contains(searchInput)) {
                    matrixCursor.addRow(new Object[]{
                            idCounter++,
                            dbPersonName,
                            cursorLoad.getString(cursorLoad.getColumnIndexOrThrow("phone_number")),
                            cursorLoad.getString(cursorLoad.getColumnIndexOrThrow("pending")),
                            cursorLoad.getString(cursorLoad.getColumnIndexOrThrow("trade_type"))
                    });
                }
            } while (cursorLoad.moveToNext());
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.customer_list, matrixCursor)
                .map(RecycleViewHelper.customerList().getName(), "person_name")
                .map(RecycleViewHelper.customerList().getDueDate(), "phone_number")
                .map(RecycleViewHelper.customerList().getPersonType(), "trade_type")
                .map(RecycleViewHelper.customerList().getAmount(), "pending")
                .map(RecycleViewHelper.customerList().getStatusLabel(), "pending")
                .setBindListener(RecycleViewHelper.Logic.Customer(true))
                .setOnItemClick((cursor, position) -> {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("person_name"));
                    person_name.setText(name);
                    person_name.setSelection(name.length());
                    recyclerView.setVisibility(View.GONE);
                    ckicked = true;
                })
                .build();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setVisibility(matrixCursor.getCount() > 0 ? View.VISIBLE : View.GONE);
    }
}