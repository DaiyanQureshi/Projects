package ultimate_files;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import ultimate_files.database.database_connectivity;

import java.math.BigDecimal;

public class dashboard {

    private database_connectivity db_connect_obj;
    private ContentValues tableData;
    private Cursor cursor;

    public dashboard(Context context){
        db_connect_obj = new database_connectivity(context);
        db_connect_obj.create_table_dashboard();
        tableData = new ContentValues();
        loadExistingData();
    }

    private void loadExistingData() {
        cursor = db_connect_obj.get_data(db_connect_obj.getTable_name_dashboard());

        if (cursor != null && cursor.moveToFirst()) {

            tableData.put("profit_filter", cursor.getString(cursor.getColumnIndexOrThrow("profit_filter")));
            tableData.put("net_profit", cursor.getString(cursor.getColumnIndexOrThrow("net_profit")));

            tableData.put("sales_filter", cursor.getString(cursor.getColumnIndexOrThrow("sales_filter")));
            tableData.put("sales", cursor.getString(cursor.getColumnIndexOrThrow("sales")));

            tableData.put("payable_filter", cursor.getString(cursor.getColumnIndexOrThrow("payable_filter")));
            tableData.put("payable_amount", cursor.getString(cursor.getColumnIndexOrThrow("payable_amount")));

            tableData.put("receivable_filter", cursor.getString(cursor.getColumnIndexOrThrow("receivable_filter")));
            tableData.put("receivable_amount", cursor.getString(cursor.getColumnIndexOrThrow("receivable_amount")));

            tableData.put("purchase_filter", cursor.getString(cursor.getColumnIndexOrThrow("purchase_filter")));
            tableData.put("purchase", cursor.getString(cursor.getColumnIndexOrThrow("purchase")));

            tableData.put("total_purchase_stock_value", cursor.getString(cursor.getColumnIndexOrThrow("total_purchase_stock_value")));
            tableData.put("total_sale_stock_value", cursor.getString(cursor.getColumnIndexOrThrow("total_sale_stock_value")));
        }

        if(cursor != null) cursor.close();
    }




    private String addToValue(String newValue, String oldValue) {
        BigDecimal oldVal = BigDecimal.ZERO;
        BigDecimal newVal = BigDecimal.ZERO;

        try {
            if (oldValue != null && !oldValue.trim().isEmpty()) {
                // Sirf digits aur dot (.) rakhega, baaki sab safai
                String cleanOld = oldValue.toString().replaceAll("[^\\d.]", "");
                if (!cleanOld.isEmpty()) {
                    oldVal = new BigDecimal(cleanOld);
                }
            }
        } catch (Exception e) {
            oldVal = BigDecimal.ZERO;
        }

        try {
            if (newValue != null && !newValue.trim().isEmpty()) {
                String cleanNew = newValue.toString().replaceAll("[^\\d.]", "");
                if (!cleanNew.isEmpty()) {
                    newVal = new BigDecimal(cleanNew);
                }
            }
        } catch (Exception e) {
            newVal = BigDecimal.ZERO;
        }


        BigDecimal sum = oldVal.add(newVal);

        return sum.toPlainString();
    }

    private void update_data(){
        boolean updated = db_connect_obj.update_data(
                db_connect_obj.getTable_name_dashboard(),
                tableData,
                "id",
                "1"
        );

        if (!updated) {
            // insert if not exists
            db_connect_obj.insert_data(db_connect_obj.getTable_name_dashboard(), tableData);
        }
    }



    public void setNet_profit(String net_profit) {
        loadExistingData();
        tableData.put(
                "net_profit",
                addToValue(net_profit, tableData.getAsString("net_profit"))
        );
        update_data();
    }

    public void setProfit_filter(String profit_filter) {
        loadExistingData();
        tableData.put("profit_filter", profit_filter);
        update_data();
    }

    public void setSales(String sales) {
        loadExistingData();
        tableData.put(
                "sales",
                addToValue(sales, tableData.getAsString("sales"))
        );
        update_data();
    }

    public void setSales_filter(String sales_filter) {
        loadExistingData();
        tableData.put("sales_filter", sales_filter);
        update_data();
    }

    public void setPayable_amount(String amount) {
        loadExistingData();
        tableData.put(
                "payable_amount",
                addToValue(amount, tableData.getAsString("payable_amount"))
        );
        update_data();
    }

    public void setPayable_filter(String payable_filter) {
        loadExistingData();
        tableData.put("payable_filter", payable_filter);
        update_data();
    }

    public void setReceivable_amount(String value) {
        loadExistingData();
        tableData.put(
                "receivable_amount",
                addToValue(value, tableData.getAsString("receivable_amount"))
        );
        update_data();
    }

    public void setReceivable_filter(String receivable_filter) {
        loadExistingData();
        tableData.put("receivable_filter", receivable_filter);
        update_data();
    }

    public void setPurchase(String purchase) {
        loadExistingData();
        tableData.put(
                "purchase",
                addToValue(purchase, tableData.getAsString("purchase"))
        );
        update_data();
    }

    public void setPurchase_filter(String purchase_filter) {
        loadExistingData();
        tableData.put("purchase_filter", purchase_filter);
        update_data();
    }

    public void setTotal_purchase_stock_value(String value) {
        loadExistingData();
        tableData.put(
                "total_purchase_stock_value",
                addToValue(value, tableData.getAsString("total_purchase_stock_value"))
        );
        update_data();
    }

    public void setTotal_sale_stock_value(String value) {
        loadExistingData();
        tableData.put(
                "total_sale_stock_value",
                addToValue(value, tableData.getAsString("total_sale_stock_value"))
        );
        update_data();
    }
}
