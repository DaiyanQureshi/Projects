package com.daiyan.accountify;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.daiyan.accountify.available_balance.available_balance;

import ultimate_files.AppExecutor;
import ultimate_files.database.database_connectivity;
import ultimate_files.database.sqlite_database;
import ultimate_files.money_formate;

import com.daiyan.accountify.customers.customers;
import com.daiyan.accountify.daily_report.daily_report;
import com.daiyan.accountify.fragment_bills.BillsFragment;
import com.daiyan.accountify.fragment_bills.Bills_fragment_2;
import com.daiyan.accountify.fragment_home.dashboard.payable;
import com.daiyan.accountify.fragment_home.dashboard.profit;
import com.daiyan.accountify.fragment_home.dashboard.purchase;
import com.daiyan.accountify.fragment_home.dashboard.receivable;
import com.daiyan.accountify.fragment_home.dashboard.sales;
import com.daiyan.accountify.new_sale.trade_sale;
import com.daiyan.accountify.profile.profile;
import com.daiyan.accountify.fragment_home.HomeFragment;
import com.daiyan.accountify.fragment_more.MoreFragment;
import com.daiyan.accountify.fragment_report.ReportFragment;
import com.daiyan.accountify.new_purchase.new_purchase;
import com.daiyan.accountify.fragment_product.ProductFragment;
import com.daiyan.accountify.receive_payment.Receive_payment;
import com.daiyan.accountify.sent_payment.sent_payment;
import com.daiyan.accountify.suppliers.suppliers;
import com.daiyan.accountify.upi_payment.upi_payment;

import java.math.BigDecimal;


public class MainActivity extends AppCompatActivity {

    Cursor profileData;
    ImageView menuButton;
    PopupMenu popup;
    sqlite_database sqlite_db_obj;

    TextView Current_Balance, today_sales, money_to_pay, money_to_receive, this_month_profit;
    ImageView home_image,bills_image,report_image,products_image,more_image;

    Cursor cursor_profile,cursor_available_balance,cursor_sold_item, cussor_stock_item, cursor_customers, cursor_suppliers;
    static Context context;
    database_connectivity db_connect_obj;
    money_formate moneyformate;

    public static Context get_context(){
        return context;
    }
//    LocalDate parsedDate = LocalDate.parse(safeDateString);
//
//    // 4. Now you can get all the parts safely!
//    int year = parsedDate.getYear();       // This will be 2025
//    int month = parsedDate.getMonthValue(); // This will be 2
//    int day = parsedDate.getDayOfMonth();   // This will be 28

//    LocalDate parsedDate = LocalDate.parse("2025-02-28"); 
//    DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("MMM",, Locale.ENGLISH);//Feb
//    DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("MMMM",, Locale.ENGLISH); February
//    String shortMonthName = parsedDate.format(shortFormatter);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById();
//        Current_Balance.setText("");

        initialization();

        if (savedInstanceState == null) {loadFragment(new HomeFragment(), false);}

        status_bar_color();

    }

    public void initialization(){
        sqlite_db_obj = new sqlite_database(this);
//        if(sqlite_db_obj.is_table_exists(profile.get_table_name())){
//            cursor_profile = sqlite_db_obj.get_data(profile.get_table_name());}
        if(sqlite_db_obj.is_table_exists(new_purchase.get_table_name())){
            cussor_stock_item = sqlite_db_obj.get_data(new_purchase.get_table_name());}
        if(sqlite_db_obj.is_table_exists(new_purchase.get_table_name())){
            cussor_stock_item = sqlite_db_obj.get_data(new_purchase.get_table_name());}
        if(sqlite_db_obj.is_table_exists(new_purchase.get_table_name())){
            cussor_stock_item = sqlite_db_obj.get_data(new_purchase.get_table_name());}
        if(sqlite_db_obj.is_table_exists(new_purchase.get_table_name())){
            cussor_stock_item = sqlite_db_obj.get_data(new_purchase.get_table_name());}

        db_connect_obj = new database_connectivity(this);
        moneyformate = new money_formate();
    }
    public void status_bar_color(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE); // Set status bar color to white
        }

        // For changing icons to dark (so they are visible on white background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
    public void findViewById(){
        context = this;
        menuButton = findViewById(R.id.three_dots);
        today_sales = findViewById(R.id.today_sales);
        money_to_pay = findViewById(R.id.money_to_pay);
        money_to_receive = findViewById(R.id.money_to_receive);
        this_month_profit = findViewById(R.id.this_month_profit);
        Current_Balance = findViewById(R.id.Current_Balance);
        home_image = findViewById(R.id.home_image);
        bills_image = findViewById(R.id.bills_image);
        report_image = findViewById(R.id.report_image);
        products_image = findViewById(R.id.products_image);
        more_image = findViewById(R.id.more_image);

    }
    public void profit(View view){
        Intent intent = new Intent(this, profit.class);
        startActivity(intent);
    }

    public void sales(View view){
        Intent intent = new Intent(this, sales.class);
        startActivity(intent);

    }
    public void payable(View view){
        Intent intent = new Intent(this, payable.class);
        startActivity(intent);
    }
    public void receivable(View view){
        Intent intent = new Intent(this, receivable.class);
        startActivity(intent);
    }
    public void purchase(View view){
        Intent intent = new Intent(this, purchase.class);
        startActivity(intent);
    }
    public void threedots(View v){

        // 1. Create a PopupMenu
        popup = new PopupMenu(MainActivity.this, v); // 'v' is the view that was clicked (the icon)

        // 2. Inflate your menu.xml
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        // 3. Set a listener for menu item clicks
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_settings) {
                    Toast.makeText(MainActivity.this, "Settings clicked!", Toast.LENGTH_SHORT).show();

                    return true;
                } else if (itemId == R.id.action_profile) {
                    profile();
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        // 4. Show the menu
        popup.show();
    }
    public void profile(){
        Intent intent = new Intent(MainActivity.this, profile.class);
        startActivity(intent);
    }

    public void changeFragmentIcons(){
        home_image.setImageResource(R.drawable.home);
        bills_image.setImageResource(R.drawable.bills);
        report_image.setImageResource(R.drawable.report);
        products_image.setImageResource(R.drawable.products);
        more_image.setImageResource(R.drawable.more);
    }
    public void home(View view){
        changeFragmentIcons();
        home_image.setImageResource(R.drawable.click_home);;
        loadFragment(new HomeFragment(), false);
    }
    public void bills(View view){
        changeFragmentIcons();
        bills_image.setImageResource(R.drawable.clicked_bills);
        loadFragment(new BillsFragment(), false);
    }
    public void report(View view){
        changeFragmentIcons();
        report_image.setImageResource(R.drawable.clicked_report);
        loadFragment(new ReportFragment(), false);
    }
    public void product(View view){
        changeFragmentIcons();
        products_image.setImageResource(R.drawable.clicked_products);
        loadFragment(new ProductFragment(), false);
    }

    public void more(View view){
        changeFragmentIcons();
        more_image.setImageResource(R.drawable.click_more);
        loadFragment(new MoreFragment(), false);
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // This clears the back stack when navigating between main tabs (recommended for bottom nav)
        if (!addToBackStack) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Use replace() to swap the old fragment for the new one
        transaction.replace(R.id.homeFragment, fragment);

        if (addToBackStack) {
            // Allows the user to press the back button to navigate previous fragments in this tab
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    //Quick Action Button Calls
    public void invoice(View view){
        Intent intent = new Intent(MainActivity.this, trade_sale.class);

        startActivity(intent);
    }
    public void record_payment(View view){
        Intent intent = new Intent(MainActivity.this, Receive_payment.class);
        startActivity(intent);
    }
    public void add_expenses(View view){
        Intent intent = new Intent(MainActivity.this, sent_payment.class);
        startActivity(intent);
    }

    public void new_purchase(View view){
        Intent intent = new Intent(MainActivity.this, new_purchase.class);
        startActivity(intent);
    }
    public void add_customer(View view){
        Intent intent = new Intent(MainActivity.this, customers.class);
        startActivity(intent);
    }
    public void view_udhar(View view){
        Intent intent = new Intent(MainActivity.this, suppliers.class);
        startActivity(intent);
    }
    public void upi_payment(View view) {
        Intent intent = new Intent(MainActivity.this, Bills_fragment_2.class);
        startActivity(intent);
    }
    public void daily_report(View view){
        Intent intent = new Intent(MainActivity.this, com.daiyan.accountify.fragment_bills.Bills_fragment_2.class);
        intent.putExtra("IS_REPORT_MODE", true);
        startActivity(intent);
    }


}