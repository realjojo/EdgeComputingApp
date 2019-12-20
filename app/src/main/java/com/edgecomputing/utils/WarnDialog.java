package com.edgecomputing.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.edgecomputing.R;

/**
 * @Author: jojo
 * @Date: Created on 2019/10/31 14:35
 */
public class WarnDialog extends Dialog {
    private Context context;
    private static WarnDialog dialog;
    private static ImageView ivProgress, ivClose;
    private static TextView tvText;

    public WarnDialog(Context context) {
        super(context);
        this.context = context;
    }

    public WarnDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    //显示dialog的方法
    public static WarnDialog showDialog(Context context, String msg) {
        dialog = new WarnDialog(context, R.style.WarnDialog);//dialog样式
        dialog.setContentView(R.layout.warn_dialog);//dialog布局文件
        tvText = dialog.findViewById(R.id.tvText);
        tvText.setText(msg);
        ivProgress = dialog.findViewById(R.id.ivProgress);
        ivClose = dialog.findViewById(R.id.ivClose);
        dialog.setCanceledOnTouchOutside(false);//点击外部不允许关闭dialog

        return dialog;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        super.setOnDismissListener(listener);
    }

}
