package com.mipos.app.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    public static String format(long amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }
}
