package com.daiyan.accountify.fragment_more;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
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
import com.daiyan.accountify.profile.profile;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;

public class MoreFragment extends Fragment {

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

    SelectionTracker<Long> tracker;

    BigDecimal displayTotalValue = BigDecimal.ZERO;
    TextView displayTotalValueText;


    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialization();
        findViewById(view);
        set_item_for_person();
    }


    public void initialization() {
        db_connect_obj = new database_connectivity(getContext());
    }

    public void findViewById(View view) {
        recyclerView = view.findViewById(R.id.stock_item_recycle_view);

    }


    public void set_item_for_person(){

        MatrixCursor cursor = new MatrixCursor(db_connect_obj.getColumn(db_connect_obj.getTable_name_parties()));
        cursor.addRow(new Object[] {
                "",          // 1. trade_type
                "\tProfile",        // 2. person_name
                "",              // 3. phone_number (Khali chhod diya)
                "",            // 4. gst_number (Khali chhod diya)
                "",              // 5. email_id
                "",   // 6. user_business_address
                "",               // 7. pending
                "",               // 8. opening_pending
                ""             // 9. total_payment_amount
        });

        cursor.addRow(new Object[] {
                "",          // 1. trade_type
                "\tFeedback",        // 2. person_name
                "",              // 3. phone_number (Khali chhod diya)
                "",            // 4. gst_number (Khali chhod diya)
                "",              // 5. email_id
                "",   // 6. user_business_address
                "",               // 7. pending
                "",               // 8. opening_pending
                ""             // 9. total_payment_amount
        });

        cursor.addRow(new Object[] {
                "",          // 1. trade_type
                "\tHelp",        // 2. person_name
                "",              // 3. phone_number (Khali chhod diya)
                "",            // 4. gst_number (Khali chhod diya)
                "",              // 5. email_id
                "",   // 6. user_business_address
                "",               // 7. pending
                "",               // 8. opening_pending
                ""             // 9. total_payment_amount
        });

        cursor.addRow(new Object[] {
                "",          // 1. trade_type
                "\tSetting",        // 2. person_name
                "",              // 3. phone_number (Khali chhod diya)
                "",            // 4. gst_number (Khali chhod diya)
                "",              // 5. email_id
                "",   // 6. user_business_address
                "",               // 7. pending
                "",               // 8. opening_pending
                ""             // 9. total_payment_amount
        });



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

                    if (id == RecycleViewHelper.customerList().getImage()) {
                        // FIX 1: 'cursor' ki jagah 'cursor1' use kiya gaya hai taaki current row read ho
                        String name = cursor1.getString(cursor1.getColumnIndexOrThrow("person_name"));
                        ImageView iconView = (ImageView) view;

                        // FIX 2: String ko trim kiya gaya hai taaki aage peeche ke \t aur spaces hat jayein
                        String cleanName = name.trim();

                        if (cleanName.equals("Profile")) {
                            iconView.setImageResource(R.drawable.dp);
                        } else if (cleanName.equals("Feedback")) {
                            iconView.setImageResource(R.drawable.feedback_icon);
                        } else if (cleanName.equals("Help")) {
                            iconView.setImageResource(R.drawable.help_icon);
                        } else if (cleanName.equals("Setting")) {
                            iconView.setImageResource(R.drawable.setting_icon);
                        }

                        // Icon ko theek se center karne ke liye
                        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        return true;
                    }

                    return false;
                })
                .setOnItemClick((cursorClick, position) -> {
                    if (tracker != null && tracker.hasSelection()) {
                        return;
                    }
                    Intent intent;
                    String name = cursorClick.getString(cursorClick.getColumnIndexOrThrow("person_name"));
                    name = name.trim();

                    if("Profile".equals(name)){
                        intent = new Intent(getContext(), profile.class);
                        startActivity(intent);

                    }
                    else{
                        Toast.makeText(getContext(), name+" Clicked", Toast.LENGTH_SHORT).show();

                    }



//                        Intent intent = new Intent(this, Bills_fragment_3.class);
//                        String name = cursorClick.getString(cursorClick.getColumnIndexOrThrow("person_name"));
//                        intent.putExtra("name", name);
//                        intent.putExtra("IS_REPORT_MODE", getIntent().getBooleanExtra("IS_REPORT_MODE", false));
//                        startActivity(intent);

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


//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (cursor != null && !cursor.isClosed()) {
//            cursor.close();
//        }
//    }
}