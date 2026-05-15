package com.daiyan.accountify.fragment_bills;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.daiyan.accountify.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ultimate_files.database.database_connectivity;

public class bills_template extends AppCompatActivity {

    database_connectivity db;

    FrameLayout container;
    View childLayout = null;
    TableLayout tableLayout;

    TextView bizTitle, bizMobile, bizEmail, bizWebsite, bizAddress, documentType;
    TextView traderName, traderContact, traderAddress;
    TextView documentName, tradeDate, dueDate, poNumber;
    TextView subtotalText, discountPercentText, discountRupeeText, taxPercentText, taxRupeeText, totalAmountText, contect_number, trader_contect_number;
    ImageView btnShareIcon;

    double globalSubtotal = 0.0;
    double totalTax = 0.0;
    double taxInPercent = 0, discountInPercent = 0;
    double totalDiscount = 0.0;
    double globalTotal = 0.0;
    String generatedInvoiceNo = "";
    String currentDateStr = "";
    String dueDateStr = "";
    Cursor profieCursor;

    String gBizName = "Business Name";
    String gBizPhone = "9999988888";
    String gBizEmail = "info@business.com";
    String gClientName = "Unknown";
    String gClientPhone = "";

    boolean isReportMode = false;
    boolean isViewMode = false;
    ExtendedFloatingActionButton addRow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bills_template);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new database_connectivity(this);

        // Pura mode system yahan pehle set hona chahiye
        isViewMode = getIntent().getBooleanExtra("VIEW_MODE", false);
        isReportMode = getIntent().getBooleanExtra("IS_REPORT_MODE", false);

        findViewById();
        setTemplate();
        setupInvoiceHeaders();
        processInvoiceData();

    }

    public void findViewById(){
        container = findViewById(R.id.invoiceContainer);
        addRow = findViewById(R.id.addRow);
        addRow.setText("Save");


        if (isViewMode) {
            addRow.setVisibility(View.VISIBLE);
            addRow.setText(isReportMode ? "Share Report" : "Share Invoice");
            addRow.setOnClickListener(v -> shareInvoiceAsImage());
        } else {
            addRow.setText(isReportMode ? "Save Report" : "Save Invoice");
            addRow.setOnClickListener(v -> saveInvoiceToDatabase());
        }
    }

    public void setTemplate(){
        LayoutInflater inflater = LayoutInflater.from(this);
        childLayout = inflater.inflate(R.layout.invoice_template_1, container, false);
        tableLayout = childLayout.findViewById(R.id.tableItems);

        // UI Mapping
        btnShareIcon = childLayout.findViewById(R.id.btnShareIcon);
        btnShareIcon.setOnClickListener(v -> shareInvoiceAsImage());

        documentType = childLayout.findViewById(R.id.document_type);
        bizTitle = childLayout.findViewById(R.id.title);
        bizMobile = childLayout.findViewById(R.id.mobile_number);
        bizEmail = childLayout.findViewById(R.id.email_id);
        bizWebsite = childLayout.findViewById(R.id.website);
        bizAddress = childLayout.findViewById(R.id.address);

        // Document Details
        documentName = childLayout.findViewById(R.id.document_name);
        tradeDate = childLayout.findViewById(R.id.trade_date);
        dueDate = childLayout.findViewById(R.id.due_date);
        poNumber = childLayout.findViewById(R.id.po_number);

        // Totals Details
        subtotalText = childLayout.findViewById(R.id.subtotal);
        discountPercentText = childLayout.findViewById(R.id.discount_in_percent);
        discountRupeeText = childLayout.findViewById(R.id.discount_in_rupee);
        taxPercentText = childLayout.findViewById(R.id.tax_in_percent);
        taxRupeeText = childLayout.findViewById(R.id.tax_in_rupee);
        totalAmountText = childLayout.findViewById(R.id.total);

        // Client Details
        traderName = childLayout.findViewById(R.id.trader_name);
        contect_number = childLayout.findViewById(R.id.contect_number);
        trader_contect_number = childLayout.findViewById(R.id.trader_contect_number);

        // ==========================================
        // UI TRANSFORMER & CROP ENGINE (REPORT MODE)
        // ==========================================
        if (isReportMode) {
            documentType.setText("REPORT");

            // 1. Hide scribbled details
            bizMobile.setVisibility(View.GONE);
            bizEmail.setVisibility(View.GONE);
            bizWebsite.setVisibility(View.GONE);
            bizAddress.setVisibility(View.GONE);

            // 2. Hide old trader sections completely
            View traderLayout = childLayout.findViewById(R.id.trader_layout);
            if (traderLayout != null) traderLayout.setVisibility(View.GONE);

            // 3. Hide Payment Instructions Box completely
            View paymentLayout = childLayout.findViewById(R.id.footer_payment_layout);
            if (paymentLayout != null) paymentLayout.setVisibility(View.GONE);

            // 4. Compact Header Box Height
            View masterHeader = childLayout.findViewById(R.id.master_header_layout);
            if (masterHeader != null) {
                ViewGroup.LayoutParams params = masterHeader.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                masterHeader.setLayoutParams(params);
            }

            // 5. Table Headings Badlo
            TextView qtyHeader = childLayout.findViewById(R.id.avg_rate);
            TextView rateHeader = childLayout.findViewById(R.id.rate);
            if (qtyHeader != null) qtyHeader.setText("Trade");
            if (rateHeader != null) rateHeader.setText("Qty x Rate");
        }

        container.removeAllViews();
        container.addView(childLayout);
    }

    public void setupInvoiceHeaders() {
        if (!isViewMode) {
            // THE DYNAMIC CALL: Mode ke hisab se INV-001 ya REP-001 milega
            generatedInvoiceNo = db.getNextDocumentNumber(isReportMode);
            documentName.setText(generatedInvoiceNo);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();

            currentDateStr = sdf.format(cal.getTime());
            tradeDate.setText(currentDateStr);

            cal.add(Calendar.DAY_OF_MONTH, 7);
            dueDateStr = sdf.format(cal.getTime());
            dueDate.setText(dueDateStr);
        }
        poNumber.setText("N/A");
    }

    public void processInvoiceData(){
        ArrayList<Bundle> receivedList = getIntent().getParcelableArrayListExtra("com.daiyan.accountify.fragment_bills");
        int i=0;

        if (receivedList != null && !receivedList.isEmpty()) {

            if (isViewMode) {
                // ==========================================
                // READ EXACT SAVED DATA FROM DB (VIEW MODE)
                // ==========================================
                Bundle firstItem = receivedList.get(0);

                documentName.setText(firstItem.getString("invoice_no"));
                tradeDate.setText(firstItem.getString("invoice_date"));
                dueDate.setText(firstItem.getString("due_date"));

                bizTitle.setText(firstItem.getString("business_name"));
                bizMobile.setText("Mobile: +91 " + firstItem.getString("business_phone"));
                bizEmail.setText("Email: " + firstItem.getString("business_email"));

                traderName.setText(firstItem.getString("person_name"));
                String ph = firstItem.getString("phone_number", "");
                if (contect_number != null) contect_number.setText(ph.isEmpty() ? "Contact Number" : ph);
                if (trader_contect_number != null) trader_contect_number.setText(ph.isEmpty() ? "Contact (Opt)" : ph);

                String savedDisPercent = firstItem.getString("invoice_discount_percent", "0");
                String savedTaxPercent = firstItem.getString("invoice_tax_percent", "0");
                discountPercentText.setText("Discount (" + savedDisPercent + "%)");
                taxPercentText.setText("Tax Rate (" + savedTaxPercent + "%)");

                subtotalText.setText(firstItem.getString("invoice_subtotal"));
                discountRupeeText.setText(firstItem.getString("invoice_discount"));
                taxRupeeText.setText(firstItem.getString("invoice_tax"));
                totalAmountText.setText(firstItem.getString("invoice_final_total").split("\\.")[0]);

            } else {
                // ==========================================
                // GENERATE FRESH INVOICE LOGIC (CREATE MODE)
                // ==========================================
                gClientName = receivedList.get(0).getString("person_name", "Client Name");
                traderName.setText(gClientName);

                profieCursor = db.get_data(db.getTable_name_profile());
                if (profieCursor != null && profieCursor.moveToFirst()) {
                    gBizName = profieCursor.getString(profieCursor.getColumnIndexOrThrow("user_business_name"));
                    gBizPhone = profieCursor.getString(profieCursor.getColumnIndexOrThrow("user_phone_number"));
                    gBizEmail = profieCursor.getString(profieCursor.getColumnIndexOrThrow("user_emailid"));

                    if (isReportMode) {
                        bizTitle.setText(gClientName); // Report mein upar person ka naam aayega
                    } else {
                        bizTitle.setText(gBizName != null ? gBizName : "Business Name");
                    }
                    bizMobile.setText(gBizPhone != null ? "Mobile: +91 " + gBizPhone : "Mobile: +91 99999 88888");
                    bizEmail.setText(gBizEmail != null ? "Email: " + gBizEmail : "Email: info@business.com");

                    profieCursor.close();
                } else {
                    if (isReportMode) {
                        bizTitle.setText(gClientName);
                    } else {
                        bizTitle.setText(gBizName);
                    }
                    bizMobile.setText("Mobile: +91 " + gBizPhone);
                    bizEmail.setText("Email: " + gBizEmail);
                    if (profieCursor != null) profieCursor.close();
                }

                gClientPhone = receivedList.get(0).getString("phone_number", "");
                if(gClientPhone.isEmpty() || gClientPhone.trim().equals("")){
                    if (contect_number != null) contect_number.setText("Contact Number");
                    if (trader_contect_number != null) trader_contect_number.setText("Contact (Opt)");
                } else {
                    if (contect_number != null) contect_number.setText(gClientPhone);
                    if (trader_contect_number != null) trader_contect_number.setText(gClientPhone);
                }
            }

            for (Bundle singleBundle : receivedList) {
                i++;
                View currentRowView = LayoutInflater.from(this).inflate(R.layout.item_invoice_row, null);

                TextView txtSl = currentRowView.findViewById(R.id.txtSl);
                TextView txtDesc = currentRowView.findViewById(R.id.txtDesc);
                TextView txtDate = currentRowView.findViewById(R.id.txtDate);
                TextView txtQty = currentRowView.findViewById(R.id.txtQty);
                TextView txtRate = currentRowView.findViewById(R.id.txtRate);
                TextView txtAmount = currentRowView.findViewById(R.id.txtAmount);

                String itemName = singleBundle.getString("item_name", "");
                String itemDate = singleBundle.getString("trade_date", "");
                String qtyStr = singleBundle.getString("quantity", "0");
                String rateStr = singleBundle.getString("rate", "0");

                txtSl.setText(String.valueOf(i));
                txtDesc.setText(itemName);
                txtDate.setText(itemDate);

                // Dynamic Logic for Trade and Rate Display
                if (isReportMode) {
                    String tType = singleBundle.getString("trade_type", "");
                    txtQty.setText(tType.equalsIgnoreCase("purchase") ? "Bought" : "Sold");
                    txtRate.setText(qtyStr + " x " + rateStr);
                } else {
                    txtQty.setText(qtyStr);
                    txtRate.setText(rateStr);
                }

                if (isViewMode) {
                    txtAmount.setText(singleBundle.getString("total_amount", "0"));
                } else {
                    String discountStr = singleBundle.getString("discount_in_rupee", "0");
                    String taxStr = singleBundle.getString("gst_in_rupee", "0");
                    String taxPerStr  = singleBundle.getString("gst_in_percent", "0");
                    String disPerStr  = singleBundle.getString("discount_in_percent", "0");

                    double qty = 0, rate = 0, tax = 0, discount = 0, taxPer = 0, disPer = 0;
                    try {
                        qty = Double.parseDouble(qtyStr);
                        rate = Double.parseDouble(rateStr);
                        tax = Double.parseDouble(taxStr);
                        discount = Double.parseDouble(discountStr);
                        taxPer = Double.parseDouble(taxPerStr);
                        disPer = Double.parseDouble(disPerStr);
                    } catch (Exception e){}

                    double rowAmount = qty * rate;
                    globalSubtotal += rowAmount;
                    totalTax += tax;
                    totalDiscount += discount;

                    discountInPercent += disPer;
                    taxInPercent += taxPer;

                    txtAmount.setText(String.valueOf(rowAmount));
                }

                tableLayout.addView(currentRowView);

                View divider = new View(this);
                divider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
                tableLayout.addView(divider);
            }

            if (!isViewMode) {
                globalTotal = globalSubtotal + totalTax - totalDiscount;

                taxInPercent = taxInPercent / i;
                discountInPercent = discountInPercent / i;

                discountPercentText.setText(String.format(Locale.getDefault(), "Discount (%.1f%%)", discountInPercent));
                taxPercentText.setText(String.format(Locale.getDefault(), "Tax Rate (%.1f%%)", taxInPercent));

                subtotalText.setText(String.format(Locale.getDefault(), "%.2f", globalSubtotal));
                discountRupeeText.setText(String.format(Locale.getDefault(), "%.2f", totalDiscount));
                taxRupeeText.setText(String.format(Locale.getDefault(), "%.2f", totalTax));
                totalAmountText.setText(String.format(Locale.getDefault(), "%.2f", globalTotal));
            }
        }
    }

    public void saveInvoiceToDatabase() {
        ArrayList<Bundle> receivedList = getIntent().getParcelableArrayListExtra("com.daiyan.accountify.fragment_bills");

        if (receivedList == null || receivedList.isEmpty()) {
            Toast.makeText(this, "No data to save", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allSaved = true;

        for (Bundle singleBundle : receivedList) {
            ContentValues values = new ContentValues();

            values.put("invoice_no", generatedInvoiceNo);
            values.put("invoice_date", currentDateStr);
            values.put("due_date", dueDateStr);
            values.put("po_number", "N/A");

            if(isReportMode){
                values.put("business_name", gClientName);
            } else {
                values.put("business_name", gBizName != null ? gBizName : "");
            }

            values.put("business_phone", gBizPhone != null ? gBizPhone : "");
            values.put("business_email", gBizEmail != null ? gBizEmail : "");

            values.put("person_name", gClientName);
            values.put("phone_number", gClientPhone);
            values.put("shipping_name", gClientName);
            values.put("shipping_contact", gClientPhone);

            values.put("invoice_subtotal", String.valueOf(globalSubtotal));
            values.put("invoice_discount", String.valueOf(totalDiscount));
            values.put("invoice_tax", String.valueOf(totalTax));
            values.put("invoice_final_total", String.valueOf(globalTotal));

            values.put("invoice_discount_percent", String.format(Locale.getDefault(), "%.1f", discountInPercent));
            values.put("invoice_tax_percent", String.format(Locale.getDefault(), "%.1f", taxInPercent));

            // THE FIX: Safe parsing for empty/null values to prevent SQLite crash
            String qtyStr = singleBundle.getString("quantity", "0");
            if (qtyStr == null || qtyStr.trim().isEmpty()) qtyStr = "0";

            String rateStr = singleBundle.getString("rate", "0");
            if (rateStr == null || rateStr.trim().isEmpty()) rateStr = "0";

            double rAmount = 0;
            try {
                rAmount = Double.parseDouble(qtyStr) * Double.parseDouble(rateStr);
            } catch (Exception e){
                rAmount = 0.0;
            }

            values.put("item_name", singleBundle.getString("item_name", ""));
            values.put("quantity", qtyStr);
            values.put("rate", rateStr);
            values.put("discount_in_rupee", singleBundle.getString("discount_in_rupee", "0"));
            values.put("gst_in_rupee", singleBundle.getString("gst_in_rupee", "0"));
            values.put("total_amount", String.valueOf(rAmount));
            values.put("trade_date", singleBundle.getString("trade_date", ""));

            // YEH 3 LINES NAYI HAIN (Complete Data ke liye)
            values.put("trade_type", singleBundle.getString("trade_type", ""));
            values.put("unit", singleBundle.getString("unit", ""));
            values.put("description", singleBundle.getString("description", ""));

            // THE ROUTING FIX: Report mode toh alag table
            String targetTable = isReportMode ? db.getTable_name_report() : db.getTable_name_invoice();
            boolean isSaved = db.insert_data(targetTable, values);
            if (!isSaved) {
                allSaved = false;
            }
        }

        if (allSaved) {
            Toast.makeText(this, (isReportMode ? "Report" : "Invoice") + " Saved Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Database Error: Could not save completely", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareInvoiceAsImage() {
        if (childLayout == null) return;

        try {
            android.widget.LinearLayout printableArea = childLayout.findViewById(R.id.invoice_printable_area);
            if (printableArea == null) {
                Toast.makeText(this, "Printable area nahi mila", Toast.LENGTH_SHORT).show();
                return;
            }

            if (btnShareIcon != null) btnShareIcon.setVisibility(View.INVISIBLE);

            printableArea.measure(
                    View.MeasureSpec.makeMeasureSpec(printableArea.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            printableArea.layout(0, 0, printableArea.getMeasuredWidth(), printableArea.getMeasuredHeight());

            int width = printableArea.getMeasuredWidth();
            int height = printableArea.getMeasuredHeight();
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
            canvas.drawColor(android.graphics.Color.WHITE);

            printableArea.draw(canvas);

            if (btnShareIcon != null) btnShareIcon.setVisibility(View.VISIBLE);
            printableArea.requestLayout();

            java.io.File cachePath = new java.io.File(getCacheDir(), "images");
            cachePath.mkdirs();
            String prefix = isReportMode ? "Report_" : "Invoice_";
            java.io.File file = new java.io.File(cachePath, prefix + System.currentTimeMillis() + ".png");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(intent, isReportMode ? "Share Report" : "Share Invoice"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Share fail ho gaya", Toast.LENGTH_SHORT).show();
            if (btnShareIcon != null) btnShareIcon.setVisibility(View.VISIBLE);
        }
    }
}