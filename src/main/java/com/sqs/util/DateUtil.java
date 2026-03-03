package com.sqs.util;

import java.text.SimpleDateFormat;

public class DateUtil {
    public static String getCurrentDateTime() {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(System.currentTimeMillis());
    }
}
