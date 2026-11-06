package com.example.quanlybaitapsinhvien.utils;

import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WindowInsetUtils {

    public static void applyHeaderAndRootInsets(View headerView, View rootView) {
        if (headerView != null) {
            final int initialTop = headerView.getPaddingTop();
            final int initialLeft = headerView.getPaddingLeft();
            final int initialRight = headerView.getPaddingRight();
            final int initialBottom = headerView.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(headerView, (v, insets) -> {
                Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(
                        initialLeft,
                        initialTop + statusBarInsets.top,
                        initialRight,
                        initialBottom
                );
                return insets;
            });
        }

        if (rootView != null) {
            final int initialBottom = rootView.getPaddingBottom();
            final int initialLeft = rootView.getPaddingLeft();
            final int initialRight = rootView.getPaddingRight();
            final int initialTop = rootView.getPaddingTop();

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                v.setPadding(
                        initialLeft,
                        initialTop,
                        initialRight,
                        initialBottom + navBarInsets.bottom
                );
                return insets;
            });
        }
    }
}
