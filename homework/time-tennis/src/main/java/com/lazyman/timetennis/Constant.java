package com.lazyman.timetennis;

import org.apache.commons.lang3.time.FastDateFormat;

public interface Constant {

    String SK_USER = "user";

    FastDateFormat FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");

    FastDateFormat FORMAT_WEEK = FastDateFormat.getInstance("E");

    FastDateFormat FORMAT_MONTH = FastDateFormat.getInstance("yyyy-MM");

    FastDateFormat FORMAT_COMPACT = FastDateFormat.getInstance("yyyyMMddHHmmss");

    String PRODUCT_BOOKING = "BO";

    String PRODUCT_CARD = "CA";

    String PRODUCT_RECHARGE = "RC";

    String PRODUCT_REFUND = "RFD";
}
