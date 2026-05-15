package com.daiyan.accountify.available_balance;

public class balance_details_template {
    private final String current_balance;

    public balance_details_template(String current_balance) {
        this.current_balance = current_balance;
    }

    public String getCurrent_balance() {
        return current_balance;
    }
}
