package com.zgy.project.stockServer.core.utils.DateUtil;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static boolean isSkipCurStock(String compareDate,String endDate){
        if (StringUtils.isBlank(endDate)){
            return true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date end_Date = sdf.parse(endDate);
            Date nowDate = sdf.parse(compareDate);
            if (end_Date.before(nowDate)){
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }
}
