package com.daiyan.accountify.fragment_home;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daiyan.accountify.MainActivity;
import com.daiyan.accountify.R;
import com.daiyan.accountify.available_balance.available_balance;

import ultimate_files.AppExecutor;
import ultimate_files.RecycleViewHelper;
import ultimate_files.database.database_connectivity;
import ultimate_files.database.sqlite_database;

import com.daiyan.accountify.fragment_product.ProductFragment_Layer_2;
import com.daiyan.accountify.new_sale.trade_sale;
import com.daiyan.accountify.profile.profile;
import ultimate_files.money_formate;

import com.daiyan.accountify.new_purchase.new_purchase;

import java.math.BigDecimal;
import java.time.LocalDate;


public class HomeFragment extends Fragment {
    profile profile_obj;
    private HomeViewModel mViewModel;
    TextView bottomTemplate;
    sqlite_database sqlite_db_obj;

    TextView Current_Balance, business_name,money_to_pay, money_to_receive,today_sales,this_month_profit;
    money_formate money_formate;

    Cursor cursor_profile,cursor_available_balance,cursor_sold_item, cursor_stock_item,cursor_this_month,cursor_today_sales, cursor_customers, cursor_suppliers;

    TextView home_text,bills_text,report_text,products_text,more_text;
    ImageView home,bills,report,products,more;
    database_connectivity db_connect_obj;
    SelectionTracker<Long> tracker;
    RecyclerView recyclerView;



    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // This is where you initialize your ViewModel
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // And this is where you access your "front end" (the views)
        // Now 'view.findViewById' works because 'view' is given to you as a parameter.
        bottomTemplate = view.findViewById(R.id.BottomTemplate);
        business_name = view.findViewById(R.id.business_name);

//        bottomTemplate.setText(text);
//        String name = profile_obj.get_business_name();
//        business_name.setText("name");
        findViewById(view);
        set_dashboard();
        recent_activity_load();

    }

    public void set_dashboard() {
        AppExecutor.getInstance().diskIO().execute(() -> {
            Cursor cursor = null;
            try {
                cursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_dashboard(), "filter", "All time");

                if (cursor != null && cursor.moveToFirst()) {
                    int salesIndex = cursor.getColumnIndex("sales");
                    int purchaseIndex = cursor.getColumnIndex("purchase");
                    int payableIndex = cursor.getColumnIndex("payable");
                    int receivableIndex = cursor.getColumnIndex("receivable");
                    int profitIndex = cursor.getColumnIndex("profit");
                    int inStockValueIndex = cursor.getColumnIndex("in_stock_value");
                    int soldValueIndex = cursor.getColumnIndex("sold_value");

                    // Data ko safely nikalna
                    String sales = (salesIndex != -1) ? cursor.getString(salesIndex) : "0";
                    String purchase = (purchaseIndex != -1) ? cursor.getString(purchaseIndex) : "0";
                    String payable = (payableIndex != -1) ? cursor.getString(payableIndex) : "0";
                    String receivable = (receivableIndex != -1) ? cursor.getString(receivableIndex) : "0";
                    String profit = (profitIndex != -1) ? cursor.getString(profitIndex) : "0";
                    String in_stock_value = (inStockValueIndex != -1) ? cursor.getString(inStockValueIndex) : "0";
                    String sold_value = (soldValueIndex != -1) ? cursor.getString(soldValueIndex) : "0";

                    // Main UI thread par data bhejna
                    AppExecutor.getInstance().mainThread().post(() -> {
                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {

//                            android.util.Log.d("DEBUG_SALES", "Sales ki exact length: " + sales.length() + " aur value: [" + sales + "]");
//
//                            String str = "sales: "+ sales+"\npurchase: "+purchase+"\npayable: "+payable+"\nreceivable: "+receivable+"\nprofit: "+profit;
//                            Toast.makeText(this,str , Toast.LENGTH_SHORT).show();

                            today_sales.setText(money_formate.set(sales).split("\\.")[0]);
                            this_month_profit.setText(money_formate.set(purchase).split("\\.")[0]);
                            money_to_pay.setText(money_formate.set(payable).split("\\.")[0]);
                            money_to_receive.setText(money_formate.set(receivable).split("\\.")[0]);
                            Current_Balance.setText(money_formate.set(profit).split("\\.")[0]);
                        }
                    });
                }
            } finally {
                // Ye line ensure karegi ki chahe error aaye ya nahi, cursor hamesha close hoga
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }
    private BigDecimal safeParseBigDecimal(String text) {
        try {
            String cleanText = text.trim();
            if (cleanText.isEmpty()) {
                return BigDecimal.ZERO;
            }
            // This is where you create the big number from a String
            return new BigDecimal(cleanText);
        } catch (NumberFormatException e) {
            // Catches any invalid numbers (like "abc" or "--")
            return BigDecimal.ZERO;
        }
    }

    public void findViewById(View view) {
        db_connect_obj = new database_connectivity(MainActivity.get_context());

//         home_text,bills_text,report_text,products_text,more_text;
//         home,bills,report,products,more;
//        home_text = view.findViewById(R.id.home_text);
//        bills_text = view.findViewById(R.id.bills_text);
//        report_text = view.findViewById(R.id.report_text);
//        products_text = view.findViewById(R.id.products_text);
//        more_text = view.findViewById(R.id.more_text);
//#989898
//@drawable/click_home
//        #000000
//        home = view.findViewById(R.id.home);
//        bills = view.findViewById(R.id.bills_image);
//        report = view.findViewById(R.id.report_image);
//        products = view.findViewById(R.id.products_image);
//        more = view.findViewById(R.id.more_image);

        money_formate = new money_formate();
        sqlite_db_obj = new sqlite_database(MainActivity.get_context());
        Current_Balance = view.findViewById(R.id.Current_Balance);
        money_to_pay = view.findViewById(R.id.money_to_pay);
        money_to_receive = view.findViewById(R.id.money_to_receive);
        today_sales = view.findViewById(R.id.today_sales);
        this_month_profit = view.findViewById(R.id.this_month_profit);
        recyclerView = view.findViewById(R.id.recent_activity_recycler);


    }

    public void recent_activity_load(){
        // THE FIX 1: Table ka naam 'recent_activity' kar diya hai
        Cursor cursor_recent = db_connect_obj.get_data("recent_activity");

        // Crash Proofing: Agar DB khali hai toh adapter null set karo
        if (cursor_recent == null || cursor_recent.getCount() == 0) {
            recyclerView.setAdapter(null);
            return;
        }

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(getContext())
                .setData(R.layout.manage_stock_list_item, cursor_recent)
                // THE FIX 2: Naye columns ko views ke sath map karo
                .map(RecycleViewHelper.stockList().getName(), "entity_name")
                .map(RecycleViewHelper.stockList().getQuantity(), "activity_type")
                .map(RecycleViewHelper.stockList().getRate(), "timestamp")
                .map(RecycleViewHelper.stockList().getTotalPrice(), "amount")
                .map(RecycleViewHelper.stockList().getImage(), "icon_type")
                .map(RecycleViewHelper.stockList().getPriceLabel(), "activity_type") // Dummy map to avoid null
                .setBindListener((view, cursor1, colName, id) -> {
                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;
                    String val = cursor1.getString(idx);

                    // Null safety
                    val = (val == null || val.equalsIgnoreCase("null")) ? "" : val;

                    if (id == RecycleViewHelper.stockList().getName()) {
                        ((TextView)view).setText(val);
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getQuantity()) {
                        ((TextView)view).setText(val); // Activity Type (Eg. "Sold", "Payment Received")
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getRate()) {
                        ((TextView) view).setText(val); // Timestamp
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getTotalPrice()) {
                        ((TextView) view).setText("₹ " + safeParseBigDecimal(val).toString());
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getPriceLabel()) {
                        ((TextView) view).setText("Activity");
                        return true;
                    }
                    if (id == RecycleViewHelper.stockList().getImage()) {
                        ImageView iconView = (ImageView) view;
                        float scale = view.getResources().getDisplayMetrics().density;
                        int paddingInPx = 0;
                        // THE ARCHITECT FIX 3: Dynamic Icon Logic
                        String iconType = cursor1.getString(cursor1.getColumnIndexOrThrow("icon_type"));
                        String activity_type = cursor1.getString(cursor1.getColumnIndexOrThrow("activity_type"));
                        if ("Sold".equals(activity_type)) {
                            iconView.setImageResource(R.drawable.sales); // Ya jo bhi product logo ho
                            paddingInPx = (int) (7 * scale + 0.5f);

                        } else if ("Bought".equals(activity_type)) {
                            iconView.setImageResource(R.drawable.purchase); // Ya jo bhi product logo ho
                            paddingInPx = (int) (4 * scale + 0.5f);

                        } else if ("invoice".equals(iconType)) {
                            iconView.setImageResource(R.drawable.invoice_icon);
                            paddingInPx = (int) (12 * scale + 0.5f);

                        } else if ("report".equals(iconType)) {
                            iconView.setImageResource(R.drawable.report_icon);
                            paddingInPx = (int) (9 * scale + 0.5f);

                        } else {
                            iconView.setImageResource(R.drawable.customers_icon); // Person logo
                            paddingInPx = (int) (12 * scale + 0.5f);

                        }
                        // Icon ke chaaro taraf padding set kar di
                        iconView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

                        // ScaleType FIT_CENTER kar diya taaki icon stretch ya crop na ho, center me perfectly fit ho
                        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        return true;
                    }
                    return false;
                })
                .setOnItemClick((cursorClick, position) -> {
                    // Recent Activity pe click karne par kuch mat kholo, UI clean rahega
                    if (tracker != null && tracker.hasSelection()) {
                        return;
                    }
                })
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
//
        tracker = new SelectionTracker.Builder<>(
                "activity_selection_id",
                recyclerView,
                new StableIdKeyProvider(recyclerView),
                new RecycleViewHelper.GenericItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.<Long>createSelectAnything())
                .build();

        adapter.setTracker(tracker);
    }





}