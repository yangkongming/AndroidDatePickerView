package com.example.datepickerview;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


/**
 * author : Yangkongming
 * date   : 2020/4/20
 * desc   :
 */
public class CustomDatePicker extends View {

    /**
     * 定义结果回调接口
     */
    public interface ResultHandler {
        void handle(String time);
    }

    private ResultHandler handler;
    private boolean canAccess = false;
    private DatePickerView year_pv, month_pv, day_pv;
    private static final int MAX_MONTH = 12;
    private ArrayList<String> year, month, day;
    private int startYear, startMonth, startDay, endYear, endMonth, endDay;
    private boolean spanYear, spanMon, spanDay;
    private Calendar selectedCalender, startCalendar, endCalendar;
    private ImageView tv_cancel;
    private TextView tv_select;
    private String selectedMonth, selectedDay;

    private Dialog datePickerDialog;

    private Context context;
    public static final String ALL = "ALL";
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm";

    public CustomDatePicker(Context context, ResultHandler resultHandler, String startDate, String endDate) {
        super(context);
        if (isValidDate(startDate, TIME_FORMAT) && isValidDate(endDate, TIME_FORMAT)) {
            this.context = context;
            canAccess = true;
            this.handler = resultHandler;
            selectedCalender = Calendar.getInstance();
            startCalendar = Calendar.getInstance();
            endCalendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.CHINA);
            try {
                startCalendar.setTime(sdf.parse(startDate));
                endCalendar.setTime(sdf.parse(endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            initDialog();
            initView();
        }
    }


    private void initDialog() {
        if (datePickerDialog == null) {
            datePickerDialog = new Dialog(context, R.style.dialog);
            datePickerDialog.setCancelable(false);
            datePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            datePickerDialog.setContentView(R.layout.custom_date_picker);
            Window window = datePickerDialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                datePickerDialog.setCanceledOnTouchOutside(true);
                WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics dm = new DisplayMetrics();
                if (manager != null) {
                    manager.getDefaultDisplay().getMetrics(dm);

                    window.setWindowAnimations(R.style.dialog);
                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.width = dm.widthPixels;
                    window.setAttributes(lp);
                }
            }
        }
    }


    private void initView() {
        year_pv = datePickerDialog.findViewById(R.id.year_pv);
        month_pv = datePickerDialog.findViewById(R.id.month_pv);
        day_pv = datePickerDialog.findViewById(R.id.day_pv);
        tv_cancel = datePickerDialog.findViewById(R.id.tv_cancle);
        tv_select = datePickerDialog.findViewById(R.id.tv_select);
        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.dismiss();
            }
        });
        tv_select.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMonth.equals(ALL)) {
                    handler.handle(selectedCalender.get(Calendar.YEAR) + "");
                } else if (selectedDay.equals(ALL)) {
                    handler.handle(selectedCalender.get(Calendar.YEAR) + "-" + (selectedCalender.get(Calendar.MONTH) + 1));
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.CHINA);
                    handler.handle(sdf.format(selectedCalender.getTime()));
                }
                datePickerDialog.dismiss();
            }
        });
    }

    private void initParameter() {
        startYear = startCalendar.get(Calendar.YEAR);
        startMonth = startCalendar.get(Calendar.MONTH) + 1;
        startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        endYear = endCalendar.get(Calendar.YEAR);
        endMonth = endCalendar.get(Calendar.MONTH) + 1;
        endDay = getLastDayOfMonth(endCalendar.get(Calendar.MONTH) + 1);
//    endCalendar.get(Calendar.DAY_OF_MONTH);
        spanYear = startYear != endYear;
        spanMon = (!spanYear) && (startMonth != endMonth);
        spanDay = (!spanMon) && (startDay != endDay);
        selectedCalender.setTime(startCalendar.getTime());//当前选择时间第一次是开始时间，第二次是第一次的选择时间
    }


    /**
     * 获得该月最后一天
     *
     * @param month
     * @return
     */
    public int getLastDayOfMonth(int month) {
        Calendar cal = Calendar.getInstance();
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最大天数
        int lastDay;
        //2月的平年瑞年天数
        if (month == 2) {
            lastDay = cal.getLeastMaximum(Calendar.DAY_OF_MONTH);
        } else {
            lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        return lastDay;
    }


    /**
     * 初始化时间集合数据
     */
    private void initTimer() {
        initArrayList();
        if (spanYear) {
            for (int i = startYear; i <= endYear; i++) {
                year.add(String.valueOf(i));
            }
            for (int i = startMonth; i <= MAX_MONTH; i++) {
                month.add(formatTimeUnit(i));
            }
            for (int i = startDay; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(formatTimeUnit(i));
            }
        } else if (spanMon) {
            year.add(String.valueOf(startYear));
            for (int i = startMonth; i <= endMonth; i++) {
                month.add(formatTimeUnit(i));
            }
            for (int i = startDay; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(formatTimeUnit(i));
            }
        } else if (spanDay) {
            year.add(String.valueOf(startYear));
            month.add(formatTimeUnit(startMonth));
            for (int i = startDay; i <= endDay; i++) {
                day.add(formatTimeUnit(i));
            }
        }
        loadComponent();
    }

    /**
     * 将“0-9”转换为“00-09”
     */
    private String formatTimeUnit(int unit) {
        return unit < 10 ? "0" + unit : String.valueOf(unit);
    }

    /**
     * 初始化集合
     */
    private void initArrayList() {
        if (year == null) year = new ArrayList<>();
        if (month == null) month = new ArrayList<>();
        if (day == null) day = new ArrayList<>();
        year.clear();
        month.clear();
        day.clear();
        selectedMonth = "";
        selectedDay = "";
    }

    /**
     * 初始化时间显示控件
     */
    private void loadComponent() {
        year_pv.setData(year);
        month_pv.setData(month);
        day_pv.setData(day);
        year_pv.setSelected(0);
        month_pv.setSelected(0);
        day_pv.setSelected(0);
        executeScroll();
    }

    /**
     * 给时间控件添加点击事件
     */
    private void addListener() {
        year_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
            @Override
            public void onSelect(String year) {
                selectedCalender.set(Calendar.YEAR, Integer.parseInt(year));
                monthChange();
            }
        });
        month_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
            @Override
            public void onSelect(String month) {
                Log.e("月份", month);
                if (month.equals(ALL)) {
                    selectedMonth = month;
                } else {
                    selectedCalender.set(Calendar.MONTH, Integer.parseInt(month) - 1);
                }
                dayChange();
            }
        });
        day_pv.setOnSelectListener(new DatePickerView.onSelectListener() {
            @Override
            public void onSelect(String day) {
                Log.e("日份", day);
                if (day.equals(ALL)) {
                    selectedDay = day;
                } else {
                    selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                }
            }
        });
    }

    /**
     * 月份改变
     */
    private void monthChange() {
        month.clear();
        month.add(ALL);
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        if (selectedYear == startYear) {
            for (int i = startMonth; i <= MAX_MONTH; i++) {
                month.add(formatTimeUnit(i));
            }
        } else if (selectedYear == endYear) {
            for (int i = 1; i <= endMonth; i++) {
                month.add(formatTimeUnit(i));
            }
        } else {
            for (int i = 1; i <= MAX_MONTH; i++) {
                month.add(formatTimeUnit(i));
            }
        }
//        selectedCalender.set(Calendar.MONTH, Integer.parseInt(month.get(1)) - 1);
        month_pv.setData(month);
        selectedMonth = ALL;
        month_pv.setSelected(0);
        month_pv.postDelayed(new Runnable() {
            @Override
            public void run() {
                dayChange();
            }
        }, 100);
    }

    /**
     * 日期改变
     */
    private void dayChange() {
        day.clear();
        day.add(ALL);
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
        if (selectedYear == startYear && selectedMonth == startMonth) {
            for (int i = startDay; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(formatTimeUnit(i));
            }
        } else if (selectedYear == endYear && selectedMonth == endMonth) {
            for (int i = 1; i <= endDay; i++) {
                day.add(formatTimeUnit(i));
            }
        } else {
            for (int i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(formatTimeUnit(i));
            }
        }

        selectedDay = ALL;
        day_pv.setData(day);
        day_pv.setSelected(0);
    }

    /**
     * 滑动
     */
    private void executeScroll() {
        year_pv.setCanScroll(year.size() > 1);
        month_pv.setCanScroll(month.size() > 1);
        day_pv.setCanScroll(day.size() > 1);
    }

    /**
     * 显示时间
     *
     * @param time
     */
    public void show(String time) {

        if (canAccess) {
            if (isValidDate(time, "yyyy-MM-dd")) {
                if (startCalendar.getTime().getTime() < endCalendar.getTime().getTime()) {
                    canAccess = true;
                    initParameter();
                    initTimer();
                    addListener();
                    setSelectedTime(time);
                    datePickerDialog.show();
                }
            } else {
                canAccess = false;
            }
        }
    }

    /**
     * 设置日期控件是否可以循环滚动
     */
    public void setIsLoop(boolean isLoop) {
        if (canAccess) {
            this.year_pv.setIsLoop(isLoop);
            this.month_pv.setIsLoop(isLoop);
            this.day_pv.setIsLoop(isLoop);
        }
    }

    /**
     * 设置日期控件默认选中的时间
     */
    public void setSelectedTime(String time) {
        if (canAccess) {
            String[] str = time.split(" ");
            String data = str[0];
            year_pv.setSelected(data.substring(0, 4));
            selectedCalender.set(Calendar.YEAR, Integer.parseInt(data.substring(0, 4)));
            month.clear();
            month.add(ALL);
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            if (selectedYear == startYear) {
                for (int i = startMonth; i <= MAX_MONTH; i++) {
                    month.add(formatTimeUnit(i));
                }
            } else if (selectedYear == endYear) {
                for (int i = 1; i <= endMonth; i++) {
                    month.add(formatTimeUnit(i));
                }
            } else {
                for (int i = 1; i <= MAX_MONTH; i++) {
                    month.add(formatTimeUnit(i));
                }
            }
            month_pv.setData(month);
            month_pv.setSelected(data.substring(5, 7));
            selectedCalender.set(Calendar.MONTH, Integer.parseInt(data.substring(5, 7)) - 1);
            day.clear();
            day.add(ALL);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            if (selectedYear == startYear && selectedMonth == startMonth) {
                for (int i = startDay; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                    day.add(formatTimeUnit(i));
                }
            } else if (selectedYear == endYear && selectedMonth == endMonth) {
                for (int i = 1; i <= endDay; i++) {
                    day.add(formatTimeUnit(i));
                }
            } else {
                for (int i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                    day.add(formatTimeUnit(i));
                }
            }
            day_pv.setData(day);
            day_pv.setSelected(data.substring(8));
            selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data.substring(8)));
            executeScroll();
        }
    }

    /**
     * 验证字符串是否是一个合法的日期格式
     */
    private boolean isValidDate(String date, String template) {
        boolean convertSuccess = true;
        SimpleDateFormat format = new SimpleDateFormat(template, Locale.CHINA);
        try {
            format.setLenient(false);
            format.parse(date);
        } catch (Exception e) {
            convertSuccess = false;
        }
        return convertSuccess;
    }

}


