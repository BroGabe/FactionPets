package com.brogabe.factionpets.utils;

import java.text.DecimalFormat;

public class MoneyUtil {

    public static String intToDollars(int amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }
}
