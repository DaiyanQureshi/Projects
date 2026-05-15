package com.daiyan.accountify.fragment_report;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.daiyan.accountify.R;

public class report_template extends AppCompatActivity {

    FrameLayout container;
    Button addRow;
    View childLayout = null;
    TableLayout tableLayout;
    LinearLayout trader_layout,trader_layout_2;
    TextView title,document_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_template);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        container = findViewById(R.id.invoiceContainer);
        setTemplate();
        findViewById();
        setAsReport();

        addRow.setOnClickListener(view -> {add_new_row();});


    }


    public void findViewById(){
        addRow = findViewById(R.id.addRow);
        tableLayout = childLayout.findViewById(R.id.tableItems);
        trader_layout = childLayout.findViewById(R.id.trader_layout);
//        trader_layout_2 = childLayout.findViewById(R.id.trader_layout_2);
        document_type = childLayout.findViewById(R.id.document_type);
        title = childLayout.findViewById(R.id.title);


    }

    public void setTemplate(){
        LayoutInflater inflater = LayoutInflater.from(this);
        childLayout = inflater.inflate(R.layout.invoice_template_1, container, false);
        container.removeAllViews();
        container.addView(childLayout);
    }

    public void add_new_row(){
        // Row Inflate karein (Design load karein)
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_invoice_row, null);

        // TextViews dhundhein
        TextView txtSl = rowView.findViewById(R.id.txtSl);
        TextView txtDesc = rowView.findViewById(R.id.txtDesc);
        TextView txtQty = rowView.findViewById(R.id.txtQty);
        TextView txtRate = rowView.findViewById(R.id.txtRate);
        TextView txtAmount = rowView.findViewById(R.id.txtAmount);

        // Data set karein
        txtSl.setText(String.valueOf(1));
        txtDesc.setText("item yoyo");
        txtQty.setText("56");
        txtRate.setText("200000");
        txtAmount.setText("11200000");
        tableLayout.addView(rowView);

        // 5. Divider Line add karein (Style maintain karne ke liye)
        View divider = new View(this);
        divider.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 1)); // Height 1px
        divider.setBackgroundColor(Color.parseColor("#CCCCCC")); // Light Gray color
        tableLayout.addView(divider);
    }

    public void setAsReport(){
        trader_layout.setVisibility(View.GONE);
        trader_layout_2.setVisibility(View.GONE);
        title.setText("Person Name");
        document_type.setText("REPORT");
    }
}
