package com.daiyan.accountify.new_sale;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import ultimate_files.date_formate_checker;
import ultimate_files.money_formate;
import ultimate_files.RecycleViewHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class add_item extends AppCompatActivity {

    // Layouts
    FrameLayout data_input_panel_1, data_input_panel_2;

    // Inputs
    TextInputEditText item_name, quantity, rate, unit, date, gst_in_percent, gst_in_rupee, discount_in_percent, discount_in_rupee, payment_date;
    TextView subtotal, pending, save, save_and_new, balance_due_text, balance_due_rupee;
    EditText total_amount, payment_amount;
    CheckBox received_checkbox;
    RecyclerView recyclerView;

    // Logic
    private Handler updateHandler = new Handler(Looper.getMainLooper());
    date_formate_checker date_formate_obj;
    database_connectivity db_connect_obj;
    private boolean isUpdatingFields = false;
    private TextInputLayout customerNameInputLayout;
    private TextInputEditText personNameEditText;
    String activityFrom = "";
    String activityFromIdOfTable = "";
    ArrayList<Bundle> bundleList = new ArrayList<>();

    // Calculation Watcher
    private TextWatcher itemCalculationWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        public void afterTextChanged(Editable s) {
            if (!isUpdatingFields) updating_methods();
        }
    };

    private TextWatcher pendingTextChange = new TextWatcher() {
        public void afterTextChanged(Editable editable) {
            calculate_pending();
        }
        public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        status_bar_color();
        findViewById();
        initialization();
        getData();
        onClickListener();
        item_name_search();
    }

    private void calculate_pending() {
        BigDecimal total = safeParseBigDecimal(total_amount.getText().toString());
        BigDecimal paid = safeParseBigDecimal(payment_amount.getText().toString());
        BigDecimal due = total.subtract(paid);
        pending.setText(due.toString());

        int color = (due.compareTo(BigDecimal.ZERO) > 0) ? Color.parseColor("#08BD7C") :
                (due.compareTo(BigDecimal.ZERO) == 0) ? Color.parseColor("#7F8081") : Color.parseColor("#AB0836");

        pending.setTextColor(color);
        if(balance_due_rupee != null) balance_due_rupee.setTextColor(color);
    }

    public void getData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;

        String id = bundle.getString("id");
        String from = bundle.getString("from");
        if (id == null || from == null) return;

        activityFrom = from;
        activityFromIdOfTable = id;

        save_and_new.setText("Delete");
        save_and_new.setBackgroundColor(Color.parseColor("#C1330E"));
        save.setText("Update");

        Cursor cursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_product(), "id", id);
        if (cursor != null && cursor.moveToFirst()) {
            data_input_panel_2.setVisibility(View.VISIBLE);
            customerNameInputLayout.setVisibility(View.VISIBLE);

            // Safe mapping from DB to UI
            personNameEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("person_name")));
            item_name.setText(cursor.getString(cursor.getColumnIndexOrThrow("item_name")));
            quantity.setText(cursor.getString(cursor.getColumnIndexOrThrow("quantity")));
            unit.setText(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
            rate.setText(cursor.getString(cursor.getColumnIndexOrThrow("rate")));
            date.setText(cursor.getString(cursor.getColumnIndexOrThrow("trade_date")));
            subtotal.setText(cursor.getString(cursor.getColumnIndexOrThrow("subtotal")));
            gst_in_percent.setText(cursor.getString(cursor.getColumnIndexOrThrow("gst_in_percent")));
            gst_in_rupee.setText(cursor.getString(cursor.getColumnIndexOrThrow("gst_in_rupee")));
            discount_in_percent.setText(cursor.getString(cursor.getColumnIndexOrThrow("discount_in_percent")));
            discount_in_rupee.setText(cursor.getString(cursor.getColumnIndexOrThrow("discount_in_rupee")));
            total_amount.setText(cursor.getString(cursor.getColumnIndexOrThrow("total_amount")));
            payment_amount.setText(cursor.getString(cursor.getColumnIndexOrThrow("payment_amount")));
            pending.setText(cursor.getString(cursor.getColumnIndexOrThrow("pending")));
            cursor.close();
        }
    }

    public void save(View view) {
        if (!condition_Check()) return;

        if (activityFrom.equals("product_fragment_layer_2")) {
            ContentValues receivedValues = new ContentValues();
            Bundle bundle = createContentBundle();
            for (String key : bundle.keySet()) {
                receivedValues.put(key, bundle.getString(key));
            }
            if (db_connect_obj.update_data(db_connect_obj.getTable_name_product(), receivedValues, "id", activityFromIdOfTable)) {
                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show();
            }
        } else {
            bundleList.add(createContentBundle());
            Intent intent = new Intent();
            intent.putExtra("data", bundleList);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    public void save_and_new(View view) {
        if (activityFrom.equals("product_fragment_layer_2")) {
            if (db_connect_obj.delete_row(db_connect_obj.getTable_name_product(), "id", activityFromIdOfTable)) {
                Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }

        if (!condition_Check()) return;
        bundleList.add(createContentBundle());
        clearInputFields();
        Toast.makeText(this, "Item Added", Toast.LENGTH_SHORT).show();
    }

    public void updating_methods() {
        if (isUpdatingFields) return;
        isUpdatingFields = true;
        try {
            BigDecimal qty = safeParseBigDecimal(quantity.getText().toString());
            BigDecimal price = safeParseBigDecimal(rate.getText().toString());
            BigDecimal sub = qty.multiply(price);

            subtotal.setText(sub.toString());

            BigDecimal discPer = safeParseBigDecimal(discount_in_percent.getText().toString());
            BigDecimal discAmt = sub.multiply(discPer).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            discount_in_rupee.setText(discAmt.toString());

            BigDecimal gstPer = safeParseBigDecimal(gst_in_percent.getText().toString());
            BigDecimal taxableAmt = sub.subtract(discAmt);
            BigDecimal gstAmt = taxableAmt.multiply(gstPer).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            gst_in_rupee.setText(gstAmt.toString());

            BigDecimal finalTotal = taxableAmt.add(gstAmt);
            total_amount.setText(finalTotal.setScale(0, BigDecimal.ROUND_HALF_UP).toString());

            if (qty.compareTo(BigDecimal.ZERO) > 0) data_input_panel_2.setVisibility(View.VISIBLE);
            calculate_pending();
        } finally {
            isUpdatingFields = false;
        }
    }

    public ArrayList<ContentValues> loadData(String filter) {
        ArrayList<ContentValues> data_list = new ArrayList<>();
        // Updated Column Names to match your NEW trade table structure
        Cursor tempCursor = db_connect_obj.get_data(db_connect_obj.getTable_name_trade());
        if (tempCursor != null && tempCursor.moveToFirst()) {
            do {
                String name = tempCursor.getString(tempCursor.getColumnIndexOrThrow("item_name"));
                // Trigger ne in_stock_... columns banaye hain
                String qty = tempCursor.getString(tempCursor.getColumnIndexOrThrow("in_stock_quantity"));
                String rate = tempCursor.getString(tempCursor.getColumnIndexOrThrow("in_stock_avg_rate"));
                String total = tempCursor.getString(tempCursor.getColumnIndexOrThrow("in_stock_total_amount"));

                if (filter.isEmpty() || name.toLowerCase().contains(filter.toLowerCase())) {
                    ContentValues cv = new ContentValues();
                    cv.put("item_name", name);
                    cv.put("quantity", qty);
                    cv.put("avg_rate", rate);
                    cv.put("total_amount", total);
                    data_list.add(cv);
                }
            } while (tempCursor.moveToNext());
            tempCursor.close();
        }
        return data_list;
    }

    public void set_to_list(ArrayList<ContentValues> data_list) {
        if (data_list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            return;
        }

        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"_id", "item_name", "quantity", "avg_rate", "total_amount"});
        for (int i = 0; i < data_list.size(); i++) {
            ContentValues cv = data_list.get(i);
            matrixCursor.addRow(new Object[]{i, cv.get("item_name"), cv.get("quantity"), cv.get("avg_rate"), cv.get("total_amount")});
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.manage_stock_list_item, matrixCursor)
                .map(RecycleViewHelper.stockList().getName(), "item_name")
                .map(RecycleViewHelper.stockList().getQuantity(), "quantity")
                .map(RecycleViewHelper.stockList().getRate(), "avg_rate")
                .setOnItemClick((cursor, position) -> {
                    item_name.setText(cursor.getString(cursor.getColumnIndexOrThrow("item_name")));
                    rate.setText(cursor.getString(cursor.getColumnIndexOrThrow("avg_rate")));
                    recyclerView.setVisibility(View.GONE);
                }).build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void findViewById() {
        data_input_panel_1 = findViewById(R.id.data_input_panel_1);
        data_input_panel_2 = findViewById(R.id.data_input_panel_2);
        item_name = findViewById(R.id.item_name);
        quantity = findViewById(R.id.avg_rate); // Note: Ensure ID in XML is correct
        unit = findViewById(R.id.unit);
        rate = findViewById(R.id.rate);
        date = findViewById(R.id.trade_date);
        subtotal = findViewById(R.id.subtotal);
        gst_in_percent = findViewById(R.id.gst_in_percent);
        gst_in_rupee = findViewById(R.id.gst_in_rupee);
        discount_in_percent = findViewById(R.id.discount_in_percent);
        discount_in_rupee = findViewById(R.id.discount_in_rupee);
        total_amount = findViewById(R.id.total_amount);
        payment_amount = findViewById(R.id.payment_amount);
        pending = findViewById(R.id.pending);
        save = findViewById(R.id.save);
        save_and_new = findViewById(R.id.save_and_new);
        recyclerView = findViewById(R.id.stock_item_recycle_view);
        received_checkbox = findViewById(R.id.payment_checkbox);
        customerNameInputLayout = findViewById(R.id.customer_name_input_layout);
        personNameEditText = findViewById(R.id.person_name);
        balance_due_rupee = findViewById(R.id.balance_due_rupee);
    }

    public void initialization() {
        db_connect_obj = new database_connectivity(this);
        date_formate_obj = new date_formate_checker();
        date.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        data_input_panel_2.setVisibility(View.GONE);
    }

    public void onClickListener() {
        save.setOnClickListener(v -> save(v));
        save_and_new.setOnClickListener(v -> save_and_new(v));
        quantity.addTextChangedListener(itemCalculationWatcher);
        rate.addTextChangedListener(itemCalculationWatcher);
        gst_in_percent.addTextChangedListener(itemCalculationWatcher);
        discount_in_percent.addTextChangedListener(itemCalculationWatcher);
        payment_amount.addTextChangedListener(pendingTextChange);

        received_checkbox.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) payment_amount.setText(total_amount.getText().toString());
        });
    }

    public void item_name_search() {
        item_name.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                set_to_list(loadData(s.toString()));
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    public Bundle createContentBundle() {
        Bundle b = new Bundle();
        b.putString("item_name", item_name.getText().toString());
        b.putString("quantity", quantity.getText().toString());
        b.putString("rate", rate.getText().toString());
        b.putString("unit", unit.getText().toString());
        b.putString("trade_date", date.getText().toString());
        b.putString("subtotal", subtotal.getText().toString());
        b.putString("gst_in_percent", gst_in_percent.getText().toString());
        b.putString("gst_in_rupee", gst_in_rupee.getText().toString());
        b.putString("discount_in_percent", discount_in_percent.getText().toString());
        b.putString("discount_in_rupee", discount_in_rupee.getText().toString());
        b.putString("total_amount", total_amount.getText().toString());
        b.putString("payment_amount", payment_amount.getText().toString());
        b.putString("pending", pending.getText().toString());
        return b;
    }

    public boolean condition_Check() {
        if (item_name.getText().toString().trim().isEmpty()) return false;
        if (quantity.getText().toString().trim().isEmpty()) return false;
        if (rate.getText().toString().trim().isEmpty()) return false;
        return true;
    }

    private BigDecimal safeParseBigDecimal(String text) {
        if (text == null || text.trim().isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(text.trim()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    public void status_bar_color() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    private void clearInputFields() {
        item_name.setText("");
        quantity.setText("");
        rate.setText("");
        data_input_panel_2.setVisibility(View.GONE);
    }
}