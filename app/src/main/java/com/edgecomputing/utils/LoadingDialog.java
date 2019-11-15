package com.edgecomputing.utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.edgecomputing.R;

public class LoadingDialog extends Dialog {
    private static ImageView ivLoading;
    private static TextView tvLoading;
    private static LoadingDialog dialog;
    Context context;

    public LoadingDialog(Context context) {
        super(context);
        this.context = context;
    }

    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    //显示dialog的方法
    public static LoadingDialog showDialog(Context context) {
        dialog = new LoadingDialog(context, R.style.LoadingDialog);//dialog样式
        dialog.setContentView(R.layout.loading_dialog);//dialog布局文件
        ivLoading = dialog.findViewById(R.id.ivLoading);
        tvLoading = dialog.findViewById(R.id.tvLoading);
        tvLoading.setText("搜索中...");
        dialog.setCanceledOnTouchOutside(false);//点击外部允许关闭dialog
        return dialog;
    }

}
