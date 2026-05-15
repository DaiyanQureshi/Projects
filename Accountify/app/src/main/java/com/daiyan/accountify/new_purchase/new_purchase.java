package com.daiyan.accountify.new_purchase;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daiyan.accountify.R;
import ultimate_files.dashboard;
import ultimate_files.database.database_connectivity;
import ultimate_files.database.sqlite_database;
import ultimate_files.date_formate_checker;
import ultimate_files.money_formate;
import ultimate_files.RecycleViewHelper;

import com.daiyan.accountify.new_sale.add_item;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class new_purchase extends AppCompatActivity {
    // Layouts & Views
    ConstraintLayout main, home;
    FrameLayout header_background, data_input_panel_1, data_input_panel_2, data_input_panel_3;
    ScrollView scrollView2;
    private static final String TABLE_NAME = "Sold_item";

    ImageView back, billed_item_arrow_bacground, billed_item_arrow_icon, add_item_background, add_item_icon;
    TextView save, save_and_new, billed_item_arrow_text, add_item_text, total_amount, balance_due_text, balance_due_rupee, pending, description_text;
    TextInputEditText person_name, phone_number, description;
    EditText payment_amount;
    CheckBox received_checkbox;
    RecyclerView recyclerView;

    // DB & Objects
    database_connectivity db_connect_obj;
    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private final int UPDATE_INTERVAL = 1000; // Increased to 1 sec to prevent lag
    private static final int REQUEST_CODE_ADD_DATA = 101;
    private ArrayList<ContentValues> product_data = new ArrayList<>();
    boolean flag_item_list = true;

    // Logic: Calculate Pending on the fly
    public static String get_table_name() {
        return TABLE_NAME;
    }
    private TextWatcher pendingTextChange = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable editable) {
            calculate_pending();
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_purchase);

        setupSystemUI();
        findViewById();
        initialization();
        onClickListener();
        start_periodic_update(); // Fixed recursion
        billed_item_arrow_icon.setVisibility(View.GONE);
        billed_item_arrow_text.setVisibility(View.GONE);
        billed_item_arrow_bacground.setVisibility(View.GONE);
    }

    private void calculate_pending() {
        BigDecimal total = safeParseBigDecimal(total_amount.getText().toString());
        BigDecimal paid = safeParseBigDecimal(payment_amount.getText().toString());
        BigDecimal due = total.subtract(paid);
        pending.setText(due.toString());

        int color = (due.compareTo(BigDecimal.ZERO) > 0) ? Color.parseColor("#08BD7C") :
                (due.compareTo(BigDecimal.ZERO) == 0) ? Color.parseColor("#7F8081") : Color.parseColor("#AB0836");

        pending.setTextColor(color);
        balance_due_rupee.setTextColor(color);
    }

    public void initialization() {
        db_connect_obj = new database_connectivity(this);
        // Important: Triggers depend on dashboard rows existence
//        db_connect_obj.initialize_dashboard_rows();
    }

    public void onClickListener() {
        View.OnClickListener toggleList = v -> list_item_visibility();
        billed_item_arrow_icon.setOnClickListener(toggleList);
        billed_item_arrow_text.setOnClickListener(toggleList);
        billed_item_arrow_bacground.setOnClickListener(toggleList);

        received_checkbox.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) payment_amount.setText(total_amount.getText().toString());
        });

        save.setOnClickListener(v -> { save_data(); finish(); });
        save_and_new.setOnClickListener(v -> save_and_new());
        back.setOnClickListener(v -> finish());
        payment_amount.addTextChangedListener(pendingTextChange);

        View.OnClickListener addItem = v -> {
            Intent intent = new Intent(this, add_item.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_DATA);
        };
        add_item_background.setOnClickListener(addItem);
        add_item_text.setOnClickListener(addItem);
        add_item_icon.setOnClickListener(addItem);
    }

    public void save_data() {
        if (person_name.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter Person Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (product_data.isEmpty()) {
            Toast.makeText(this, "Add at least one item", Toast.LENGTH_SHORT).show();
            return;
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (ContentValues item : product_data) {
            item.put("trade_type", "purchase");
            item.put("person_name", person_name.getText().toString().trim());
            item.put("phone_number", phone_number.getText().toString().trim());
//            item.put("trade_date", todayDate); // Required for Dashboard Triggers
//            item.put("pending", pending.getText().toString());
//            item.put("payment_amount", payment_amount.getText().toString());

            db_connect_obj.insert_data(db_connect_obj.getTable_name_product(), item);
            // No need for update_trade_table or update_partie_table, Triggers handle it!
        }
        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show();
    }

    public void save_and_new() {
        save_data();
        person_name.setText("");
        phone_number.setText("");
        payment_amount.setText("0");
        product_data.clear();
        refreshActivityUI();
    }

    public void refreshActivityUI() {
        BigDecimal sumTotal = BigDecimal.ZERO;
        for (ContentValues cv : product_data) {
            sumTotal = sumTotal.add(safeParseBigDecimal(cv.getAsString("total_amount")));
        }
        total_amount.setText(sumTotal.toString());
        calculate_pending();
        set_list_item();

        int visibility = product_data.isEmpty() ? View.GONE : View.VISIBLE;
        billed_item_arrow_icon.setVisibility(visibility);
        billed_item_arrow_text.setVisibility(visibility);
        billed_item_arrow_bacground.setVisibility(visibility);
        recyclerView.setVisibility(visibility);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_DATA && resultCode == RESULT_OK && data != null) {
            ArrayList<Bundle> bundles = data.getParcelableArrayListExtra("data");
            if (bundles != null) {
                for (Bundle b : bundles) {
                    ContentValues cv = new ContentValues();
                    for (String key : b.keySet()) cv.put(key, b.getString(key));
                    product_data.add(cv);
                }
                refreshActivityUI();
            }
        }
    }

    public void set_list_item() {
        String[] columns = {"_id", "item_name", "quantity", "rate", "total_amount"};
        MatrixCursor cursor = new MatrixCursor(columns);
        int id = 1;
        for (ContentValues cv : product_data) {
            cursor.addRow(new Object[]{id++, cv.getAsString("item_name"), cv.getAsString("quantity"), cv.getAsString("rate"), cv.getAsString("total_amount")});
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(this)
                .setData(R.layout.trade_item_list, cursor)
                .map(RecycleViewHelper.tradeList().getTitle(), "item_name")
                .map(RecycleViewHelper.tradeList().getTotalAmount(), "total_amount")
                .setBindListener((view, c, col, vid) -> {
                    if (vid == RecycleViewHelper.tradeList().getTotalAmount()) {
                        ((TextView)view).setText("₹ " + c.getString(c.getColumnIndexOrThrow(col)));
                        return true;
                    }
                    return false;
                }).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void start_periodic_update() {
        updateRunnable = new Runnable() {
            @Override public void run() {
                // Perform any UI background updates here if needed
                updateHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        updateHandler.post(updateRunnable);
    }

    private BigDecimal safeParseBigDecimal(String val) {
        if (val == null || val.isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(val.trim()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private void setupSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void findViewById() {
        recyclerView = findViewById(R.id.recycle_view_page);
        person_name = findViewById(R.id.person_name);
        phone_number = findViewById(R.id.phone_number);
        payment_amount = findViewById(R.id.payment_amount);
        total_amount = findViewById(R.id.total_amount);
        pending = findViewById(R.id.pending);
        balance_due_rupee = findViewById(R.id.balance_due_rupee);
        received_checkbox = findViewById(R.id.received_checkbox);
        save = findViewById(R.id.save);
        save_and_new = findViewById(R.id.save_and_new);
        back = findViewById(R.id.back);
        add_item_background = findViewById(R.id.add_item_background);
        add_item_icon = findViewById(R.id.add_item_icon);
        add_item_text = findViewById(R.id.add_item_text);
        billed_item_arrow_icon = findViewById(R.id.billed_item_arrow_icon);
        billed_item_arrow_text = findViewById(R.id.billed_item_arrow_text);
        billed_item_arrow_bacground = findViewById(R.id.billed_item_arrow_bacground);
    }

    public void list_item_visibility() {
        flag_item_list = !flag_item_list;
        recyclerView.setVisibility(flag_item_list ? View.VISIBLE : View.GONE);
    }
}