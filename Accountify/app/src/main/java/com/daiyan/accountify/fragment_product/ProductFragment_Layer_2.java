package com.daiyan.accountify.fragment_product;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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

import com.daiyan.accountify.MainActivity;
import com.daiyan.accountify.R;
import com.daiyan.accountify.new_purchase.add_item_to_purchase;
import com.daiyan.accountify.new_sale.add_item;
import com.google.android.material.imageview.ShapeableImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import ultimate_files.RecycleViewHelper;
import ultimate_files.database.database_connectivity;
import ultimate_files.date_formate_checker;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

public class ProductFragment_Layer_2 extends AppCompatActivity {

    ShapeableImageView dp;
    TextView title, total_quantity, avg_rate, total_amount;
    TextView purchase, sale;
    View purchase_view, sale_view;
    TextView three_dots, total_quantity_text, avg_rate_text, total_amount_text;
    View search_view;
    database_connectivity db_connect_obj;
    String trade_type = "purchase";
    RecyclerView recyclerView;
    String slide;
    date_formate_checker dfc;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_fragment_layer2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById();
        onClickListener();
        db_connect_obj = new database_connectivity(this);
        getData();
        puchaseList();

    }
    public void findViewById(){
        dp = findViewById(R.id.dp);
        title = findViewById(R.id.title);
        total_quantity = findViewById(R.id.total_quantity);
        avg_rate = findViewById(R.id.avg_rate);
        total_amount = findViewById(R.id.total_amount);
        purchase = findViewById(R.id.purchase);
        sale = findViewById(R.id.Sale);
        recyclerView = findViewById(R.id.layer_2_recycle_view);
        purchase_view = findViewById(R.id.purchase_view);
        sale_view = findViewById(R.id.sale_view);
        total_quantity_text = findViewById(R.id.total_quantity_text);
        avg_rate_text = findViewById(R.id.avg_rate_text);
        total_amount_text = findViewById(R.id.total_amount_text);



    }
    public void onClickListener(){
        purchase.setOnClickListener(v->{puchaseList();});
        sale.setOnClickListener(v->{saleList();});

    }

    public void getData(){
        Bundle bundle = getIntent().getExtras();

        // THE ARCHITECT FIX: Adjust icon size programmatically via padding
        int paddingInDp = 25;
        float scale = getResources().getDisplayMetrics().density;
        int paddingInPx = (int) (paddingInDp * scale + 0.5f);
        dp.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

        if (bundle != null) {
            slide = bundle.getString("slide");
            if(slide.equals("person")){
                dp.setImageResource(R.drawable.default_contect_logo);
                title.setText(bundle.getString("name"));

                String contactNumber = "N/A";
                String gstIn = "N/A";
                String pendingAmount = "0";
                Cursor partyCursor = db_connect_obj.searchInColumn(db_connect_obj.getTable_name_parties(), "person_name", bundle.getString("name"));

                if (partyCursor != null && partyCursor.moveToFirst()) {
                    int phoneIdx = partyCursor.getColumnIndex("phone_number");
                    if(phoneIdx != -1) contactNumber = partyCursor.getString(phoneIdx);

                    int gstIdx = partyCursor.getColumnIndex("gst_number");
                    if(gstIdx != -1) gstIn = partyCursor.getString(gstIdx);

                    int pendingIdx = partyCursor.getColumnIndex("pending");
                    if(pendingIdx != -1) pendingAmount = partyCursor.getString(pendingIdx);

                    partyCursor.close();
                }
                // Handling empty or null strings
                if (contactNumber == null || contactNumber.trim().isEmpty()) contactNumber = "N/A";
                if (gstIn == null || gstIn.trim().isEmpty()) gstIn = "N/A";
                if (pendingAmount == null || pendingAmount.trim().isEmpty()) pendingAmount = "0";

                total_quantity_text.setText("Contact Number: ");
                total_quantity.setText(contactNumber);

                avg_rate_text.setText("GSTIN: ");
                avg_rate.setText(gstIn);

                total_amount_text.setText("Pending Amount: ");
                total_amount.setText("₹ " + pendingAmount);
            }
            else {

                dp.setImageResource(R.drawable.product_icon);
                title.setText(bundle.getString("name"));
                total_quantity.setText(bundle.getString("quantity"));
                avg_rate.setText(bundle.getString("avg_rate"));
                total_amount.setText(bundle.getString("amount"));



            }
        }
    }




    public void puchaseList(){
        trade_type = "purchase";
        purchase.setTextColor(Color.parseColor("#304FFE"));
        purchase_view.setVisibility(View.VISIBLE);
        sale.setTextColor(Color.parseColor("#7F8081"));
        sale_view.setVisibility(View.GONE);

        String name = title.getText().toString();
        Cursor cursor;
        if(slide.equals("product")) {
            cursor = db_connect_obj.searchInMultiColumnsMultiCondition(
                    db_connect_obj.getTable_name_product(),
                    new String[][]{
                            {"trade_type", "==", "purchase", "AND", "("}, {"item_name", "==", name, "OR", ")"}
                    }
            );
        }
        else{
            cursor = db_connect_obj.searchInMultiColumnsMultiCondition(
                    db_connect_obj.getTable_name_product(),
                    new String[][]{
                            {"trade_type", "==", "purchase", "AND", "("}, {"person_name", "==", name, "OR", ")"}
                    }
            );
        }
        setList(cursor,name);
    }
    public void saleList(){
        trade_type = "sale";
        sale.setTextColor(Color.parseColor("#304FFE"));
        sale_view.setVisibility(View.VISIBLE);
        purchase.setTextColor(Color.parseColor("#7F8081"));
        purchase_view.setVisibility(View.GONE);

        String name = title.getText().toString();
        Cursor cursor;
        if(slide.equals("product")) {
            cursor = db_connect_obj.searchInMultiColumnsMultiCondition(
                    db_connect_obj.getTable_name_product(),
                    new String[][]{
                            {"trade_type", "==", "sale", "AND", "("}, {"item_name", "==", name, "OR", ")"}
                    }
            );
        }
        else{
            cursor = db_connect_obj.searchInMultiColumnsMultiCondition(
                    db_connect_obj.getTable_name_product(),
                    new String[][]{
                            {"trade_type", "==", "sale", "AND", "("}, {"person_name", "==", name, "OR", ")"}
                    }
            );
        }
        setList(cursor,name);
    }

    public void setList(Cursor data, String name){

        String name2 = "";
        if(slide.equals("product")){name2 = "person_name";}
        else{name2 = "item_name";}

        RecycleViewHelper adapter = new RecycleViewHelper.Builder(MainActivity.get_context())
                .setData(R.layout.manage_stock_list_item, data)

                // --- MAPPINGS (Using Suggestions) ---
                .map(RecycleViewHelper.stockList().getName(), name2)
                .map(RecycleViewHelper.stockList().getQuantity(), "quantity")
                .map(RecycleViewHelper.stockList().getRate(), "rate")
                .map(RecycleViewHelper.stockList().getTotalPrice(), "total_amount")

                // Dummy mapping for static logic (Image & Label) - linking to any valid column
                .map(RecycleViewHelper.stockList().getImage(), "item_name")
                .map(RecycleViewHelper.stockList().getPriceLabel(), "item_name")

                // --- CUSTOM LOGIC ---
                .setBindListener((view, cursor1, colName, id) -> {
                  // Logic: Static Image
                    if (id == RecycleViewHelper.stockList().getImage()) {
                        if(slide.equals("product")) {
                            ((ImageView) view).setImageResource(R.drawable.product_icon);
                        }
                        else{
                            ((ImageView) view).setImageResource(R.drawable.default_contect_logo);
                        }
                        return true;
                    }
                    if(id == RecycleViewHelper.stockList().getName()){

                    }
                    // Safe Column Indexing to prevent crashes
                    int idx = cursor1.getColumnIndex(colName);
                    if (idx == -1) return false;
                    String val = cursor1.getString(idx);

                    // Logic: Quantity "Qty: 50 Pcs"
                    if (id == RecycleViewHelper.stockList().getQuantity()) {
                        // Try to get unit safely
                        int unitIdx = cursor1.getColumnIndex("unit");
                        String unit = (unitIdx != -1) ? cursor1.getString(unitIdx) : "";
                        ((TextView)view).setText("Qty: " + val + " " + unit);
                        return true;
                    }

                    // Logic: Rate "Avg Rate: 500"
                    if (id == RecycleViewHelper.stockList().getRate()) {
                        String trade_date = data.getString(data.getColumnIndexOrThrow("trade_date"));
                        trade_date = dfc.getSmartFormattedDate(trade_date);

                        String mainText = "Rate: " + val;
                        String subText = "\n" + trade_date;
                        String fullText = mainText + subText;

// 2. SpannableString object create karo
                        SpannableString spannableString = new SpannableString(fullText);

// 3. Trade date ka text size chota karne ke liye RelativeSizeSpan use karo
// 0.7f ka matlab hai original size ka 70%
                        spannableString.setSpan(
                                new RelativeSizeSpan(0.8f),
                                mainText.length(),
                                fullText.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );

// Pro Tip: Secondary text ko thoda halka color de do taaki Rate highlight ho
                        spannableString.setSpan(
                                new ForegroundColorSpan(Color.parseColor("#808080")), // Grey color
                                mainText.length(),
                                fullText.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );

// 4. Ab is spannable object ko direct text view me set kar do
                        ((TextView)view).setText(spannableString);
//                        ((TextView)view).setText("Rate: " + val+"\n"+trade_date);
                        return true;
                    }

                    // Logic: Price "₹ 5000"
                    if (id == RecycleViewHelper.stockList().getTotalPrice()) {
                        ((TextView)view).setText("₹ " + val);
                        return true;
                    }

                    // Logic: Static Text "Total Purchase"
                    if (id == RecycleViewHelper.stockList().getPriceLabel()) {
                        ((TextView)view).setText("Purchase Price");
                        return true;
                    }

//                    // Logic: Static Image
//                    if (id == RecycleViewHelper.stockList().getImage()) {
//                        ((ImageView)view).setImageResource(R.drawable.product_icon);
//                        return true;
//                    }

                    return false;
                })
                .setOnItemClick((cursor, position) -> {
                    Intent intent;
                    if(trade_type == "purchase") {
                        intent = new Intent(this, add_item_to_purchase.class);
                    }
                    else{
                        intent = new Intent(this, add_item.class);

                    }
                        String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));

                    intent.putExtra("id", id);
                    intent.putExtra("from", "product_fragment_layer_2");

                    startActivity(intent);

                })
                .build();

        // 3. Set Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

}