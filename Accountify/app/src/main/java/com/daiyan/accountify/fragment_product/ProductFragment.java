package com.daiyan.accountify.fragment_product;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.selection.StableIdKeyProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daiyan.accountify.R;
import ultimate_files.database.database_connectivity;
import ultimate_files.money_formate;
import ultimate_files.RecycleViewHelper;

import com.daiyan.accountify.fragment_bills.Bills_fragment_3;
import com.daiyan.accountify.new_purchase.new_purchase;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;

public class ProductFragment extends Fragment {

    database_connectivity db_connect_obj;
    RecyclerView recyclerView;
    Cursor cursor, cursorNN;
    TextView text;
    money_formate money_formate;
    ImageView back;
    TextView product, person;
    View product_view, person_view;
    String slide_flag = "product";

    // NEW ADDED: FloatingActionButton ko global banaya taaki observer isko change kar sake
    ExtendedFloatingActionButton fab;

    SelectionTracker<Long> tracker;

    BigDecimal displayTotalValue = BigDecimal.ZERO;
    TextView displayTotalValueText;


    public static ProductFragment newInstance() {
        return new ProductFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialization();
        findViewById(view);
        setDisplatTotalValue();
        onClickListener();

        // NEW ADDED: FAB ko initialize kiya
        fab = view.findViewById(R.id.Add);
        fab.setText("Add New Stock");

        // Default click listener (jab normal mode ho)
        fab.setOnClickListener(v -> addStock());


        product();

    }
    public void setDisplatTotalValue(){
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        cursor = db_connect_obj.get_data(db_connect_obj.getTable_name_trade());
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String amt = cursor.getString(cursor.getColumnIndexOrThrow("in_stock_total_amount"));
                displayTotalValue = displayTotalValue.add(new BigDecimal(amt));
            }
        }
        displayTotalValueText.setText("Total Stock Value");

        text.setText(money_formate.set((displayTotalValue.toString())).split("\\.")[0]);
    }

    public void initialization() {
        db_connect_obj = new database_connectivity(getContext());
    }

    public void findViewById(View view) {
        text = view.findViewById(R.id.total_stock_text);
        money_formate = new money_formate();
        back = view.findViewById(R.id.back);
        recyclerView = view.findViewById(R.id.stock_item_recycle_view);
        product = view.findViewById(R.id.product);
        person = view.findViewById(R.id.person);
        product_view = view.findViewById(R.id.product_view);
        person_view = view.findViewById(R.id.person_view);
        displayTotalValueText = view.findViewById(R.id.total_stock_text_text);
    }

    public void onClickListener() {
        product.setOnClickListener(v -> product());
        person.setOnClickListener(v -> person());
    }

    public void person() {
        displayTotalValue = BigDecimal.ZERO;
        slide_flag = "person";
        person.setTextColor(Color.parseColor("#304FFE"));
        person_view.setVisibility(View.VISIBLE);
        product.setTextColor(Color.parseColor("#7F8081"));
        product_view.setVisibility(View.GONE);
        set_item_for_person();

    }

    public void set_item_for_person(){


        if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
//            cursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_parties(),"trade_type","sale");
        cursor = db_connect_obj.get_data(db_connect_obj.getTable_name_parties());



            if (cursor == null || cursor.getCount() == 0) {
                return;
            }

            RecycleViewHelper adapter = new RecycleViewHelper.Builder(getContext())
                    .setData(R.layout.customer_list, cursor)
                    .map(RecycleViewHelper.customerList().getName(), "person_name")
                    .map(RecycleViewHelper.customerList().getPersonType(), "trade_type")
                    .map(RecycleViewHelper.customerList().getDueDate(), "phone_number")
                    .map(RecycleViewHelper.customerList().getAmount(), "total_payment_amount")
                    .map(RecycleViewHelper.customerList().getStatusLabel(), "pending")
                    .map(RecycleViewHelper.customerList().getImage(), "person_name")

                    .setBindListener((view, cursor1, colName, id) -> {
                        int idx = cursor1.getColumnIndex(colName);
                        if (idx == -1) return false;
                        String val = cursor1.getString(idx);

                        if (id == RecycleViewHelper.customerList().getDueDate()) {
                            if (val == null || val.trim().isEmpty()) {
                                view.setVisibility(View.GONE);
                            } else {
                                ((TextView) view).setText(val.trim());
                                view.setVisibility(View.VISIBLE);
                            }
                            return true;
                        }

                        if (id == RecycleViewHelper.customerList().getPersonType()) {
                            String typeText = "";
                            ((TextView) view).setText(typeText);
                            view.setVisibility(typeText.isEmpty() ? View.GONE : View.VISIBLE);
                            return true;
                        }

                        if (id == RecycleViewHelper.customerList().getAmount() || id == RecycleViewHelper.customerList().getStatusLabel()) {
                            double amount = 0;
                            try {
                                if (val != null && !val.trim().isEmpty()) amount = Double.parseDouble(val);
                            } catch (NumberFormatException e) { amount = 0; }

                            int color;
                            String statusText;

                            color = Color.parseColor("#404040");
                            statusText = "Total Amount";

                            if (id == RecycleViewHelper.customerList().getAmount()) {
                                ((TextView) view).setText("₹ " + Math.abs(amount));
                            } else {
                                ((TextView) view).setText(statusText);
                            }
                            ((TextView) view).setTextColor(color);
                            return true;
                        }

                        if (id == RecycleViewHelper.customerList().getImage()) {
                            ((ImageView) view).setImageResource(R.drawable.default_contect_logo);
                            return true;
                        }
                        return false;
                    })
                    .setOnItemClick((cursorClick, position) -> {
                        if (tracker != null && tracker.hasSelection()) {
                            return;
                        }
                        Intent intent = new Intent(getContext(), ProductFragment_Layer_2.class);
                        intent.putExtra("slide", slide_flag);

                        String name = cursorClick.getString(cursorClick.getColumnIndexOrThrow("person_name"));
                        String amount = cursorClick.getString(cursorClick.getColumnIndexOrThrow("total_payment_amount"));

                        intent.putExtra("name", name);
                        intent.putExtra("amount", amount);

                        startActivity(intent);



//                        Intent intent = new Intent(this, Bills_fragment_3.class);
//                        String name = cursorClick.getString(cursorClick.getColumnIndexOrThrow("person_name"));
//                        intent.putExtra("name", name);
//                        intent.putExtra("IS_REPORT_MODE", getIntent().getBooleanExtra("IS_REPORT_MODE", false));
//                        startActivity(intent);

                        Toast.makeText(getContext(), "Person Clicked", Toast.LENGTH_SHORT).show();
                    })
                    .build();

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);

            tracker = new SelectionTracker.Builder<>(
                    "parties_selection_id",
                    recyclerView,
                    new StableIdKeyProvider(recyclerView),
                    new RecycleViewHelper.GenericItemDetailsLookup(recyclerView),
                    StorageStrategy.createLongStorage())
                    .withSelectionPredicate(SelectionPredicates.<Long>createSelectAnything())
                    .build();

            adapter.setTracker(tracker);

            tracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
                @Override
                public void onSelectionChanged() {
                    super.onSelectionChanged();
                    if (tracker.hasSelection()) {
//                        fab.setImageResource(android.R.drawable.ic_menu_delete);
//                        fab.setOnClickListener(v -> processSelectedBills());
                    } else {
//                        fab.setImageResource(R.drawable.add_gray);
//                        fab.setOnClickListener(v -> createTemplate());
                    }
                }
            });

    }

    public void product() {
        displayTotalValue = BigDecimal.ZERO;
        slide_flag = "product";
        product.setTextColor(Color.parseColor("#304FFE"));
        product_view.setVisibility(View.VISIBLE);
        person.setTextColor(Color.parseColor("#7F8081"));
        person_view.setVisibility(View.GONE);
        if (db_connect_obj.is_table_exists(db_connect_obj.getTable_name_trade())) {
            set_item_for_product();
        }
    }

    public void set_item_for_product() {

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        cursor = db_connect_obj.get_data(db_connect_obj.getTable_name_trade());


        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(getContext())
                .setData(R.layout.manage_stock_list_item, cursor)
                .map(RecycleViewHelper.stockList().getName(), "item_name")
                .map(RecycleViewHelper.stockList().getQuantity(), "in_stock_quantity")
                .map(RecycleViewHelper.stockList().getRate(), "in_stock_avg_rate")
                .map(RecycleViewHelper.stockList().getTotalPrice(), "in_stock_total_amount")
                .map(RecycleViewHelper.stockList().getImage(), "item_name")
                .map(RecycleViewHelper.stockList().getPriceLabel(), "item_name")
                .setBindListener((view, cursor1, colName, id) -> {
                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;
                    String val = cursor1.getString(idx);

                    if (id == RecycleViewHelper.stockList().getQuantity()) {
                        // Try to get unit safely
                        int unitIdx = cursor1.getColumnIndex("unit");
                        String unit = (unitIdx != -1) ? cursor1.getString(unitIdx) : "";
                        ((TextView)view).setText("Qty: " + val + " " + unit);
                        return true;
                    }

                    if (id == RecycleViewHelper.stockList().getRate()) {
                        ((TextView) view).setText("Avg Rate: " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getTotalPrice()) {
                        ((TextView) view).setText("₹ " + val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getPriceLabel()) {
                        ((TextView) view).setText("Current Stock");
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getImage()) {
                        ((ImageView) view).setImageResource(R.drawable.product_icon);
                        return true;
                    }
                    return false;
                })
                .setOnItemClick((cursorClick, position) -> {
                    if (tracker != null && tracker.hasSelection()) {
                        return;
                    }

                    Intent intent = new Intent(getContext(), ProductFragment_Layer_2.class);
                    intent.putExtra("slide", slide_flag);

                    String name = cursorClick.getString(cursorClick.getColumnIndexOrThrow("item_name"));
                    String quantity = cursorClick.getString(cursorClick.getColumnIndexOrThrow("in_stock_quantity"));
                    String avg_rate = cursorClick.getString(cursorClick.getColumnIndexOrThrow("in_stock_avg_rate"));
                    String amount = cursorClick.getString(cursorClick.getColumnIndexOrThrow("in_stock_total_amount"));

                    intent.putExtra("name", name);
                    intent.putExtra("quantity", quantity);
                    intent.putExtra("avg_rate", avg_rate);
                    intent.putExtra("amount", amount);

                    startActivity(intent);
                })
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        tracker = new SelectionTracker.Builder<>(
                "product_selection_id",
                recyclerView,
                new StableIdKeyProvider(recyclerView),
                new RecycleViewHelper.GenericItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.<Long>createSelectAnything())
                .build();

        adapter.setTracker(tracker);

        // ==========================================
        // NEW ADDED: STEP 1 - THE OBSERVER
        // ==========================================
        tracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (tracker.hasSelection()) {
                    // Agar ek bhi item select hua, toh Add button ko Action button bana do
                    // (Note: Abhi tere paas delete_icon na ho toh app drawable use kar, ya baad me icon change kar lena)

                    fab.setText("Add New Stock");
                    fab.setOnClickListener(v -> processSelectedItems()); // Click par naya action chalega
                } else {
                    // Selection khatam (sab deselect ho gaya), toh wapas original state me aa jao
                    fab.setText("Add New Stock");
                    // Tera original add icon
                    fab.setOnClickListener(v -> addStock()); // Click par normal add form khulega
                }
            }
        });
    }

    public void addStock() {
        Intent intent = new Intent(getContext(), new_purchase.class);
        startActivity(intent);
    }

    // ==========================================
    // NEW ADDED: STEP 2 - DATA EXTRACTION METHOD
    // ==========================================
    public void processSelectedItems() {
        if (tracker == null || !tracker.hasSelection()) return;

        int selectedCount = tracker.getSelection().size();

        // Loop chala kar selected items ka exact data nikal rahe hain
        for (Long selectionKey : tracker.getSelection()) {
            int position = selectionKey.intValue();

            if (cursor != null && cursor.moveToPosition(position)) {
                String selectedItemName = cursor.getString(cursor.getColumnIndexOrThrow("item_name"));

                // FUTURE ACTION: Yahan tu apna delete logic lagayega
                // db_connect_obj.delete_data(selectedItemName);

                System.out.println("Ready to process: " + selectedItemName);
            }
        }

        Toast.makeText(getContext(), selectedCount + " Items Processed", Toast.LENGTH_SHORT).show();

        // Kaam hone ke baad selection mode clear karna zaroori hai
        tracker.clearSelection();

        // Agar database se item udaya hai, toh list refresh kar dena
        // set_item();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}