package com.example.quanlybaitapsinhvien;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderHelper {

    public static void setReminder(Context context, int baiTapId, String tenBai, String thoiGianNhac) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("TEN_BAI", tenBai);
        intent.putExtra("BAI_TAP_ID", baiTapId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                baiTapId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(thoiGianNhac);
            if (date != null && date.getTime() > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
                }
                Log.d("ReminderHelper", "Đã đặt nhắc nhở cho bài tập " + baiTapId + " vào lúc " + thoiGianNhac);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void cancelReminder(Context context, int baiTapId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                baiTapId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("ReminderHelper", "Đã hủy nhắc nhở cho bài tập " + baiTapId);
        }
    }

    public static void cancelAllReminders(Context context) {
        SessionManager session = new SessionManager(context);
        int sinhVienId = session.layUserId();
        
        android.database.sqlite.SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COT_TD_ID_NOI_DUNG + " FROM " + DatabaseHelper.BANG_TIEN_DO + " WHERE " + DatabaseHelper.COT_TD_ID_SINH_VIEN + " = ? AND " + DatabaseHelper.COT_TD_THOI_GIAN_NHAC + " IS NOT NULL", new String[]{String.valueOf(sinhVienId)});
        while (cursor.moveToNext()) {
            cancelReminder(context, cursor.getInt(0));
        }
        cursor.close();
    }
}