package com.edgecomputing.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edgecomputing.R;
import com.edgecomputing.bean.BlueDevice;

import java.util.ArrayList;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/12 15:35
 */
public class BlueListAdapter extends RecyclerView.Adapter<BlueListAdapter.MyViewHolder> {
    private static final String TAG = "BlueListAdapter";
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<BlueDevice> mBlueList;
    private String[] mStateArray = {"未配对", "配对中", "已配对"};
    public static int CONNECTED = 3;
    private onRecycleItemClickListener mClickListener;

    public BlueListAdapter(Context context, ArrayList<BlueDevice> blue_list) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mBlueList = blue_list;
    }

    public void setOnItemClickListener(onRecycleItemClickListener listener) {
        this.mClickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_bluetooth, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view, mClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        BlueDevice device = mBlueList.get(i);
        myViewHolder.tv_blue_name.setText(device.getName());
        myViewHolder.tv_blue_address.setText(device.getAddress());
        if(device.getState() == 10) {
            myViewHolder.tv_blue_state.setText(mStateArray[0]);
        }else if(device.getState() == 12){
            myViewHolder.tv_blue_state.setText(mStateArray[2]);
        }else {
            myViewHolder.tv_blue_state.setText(mStateArray[1]);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mBlueList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout ll_blue_list;
        public TextView tv_blue_name;
        public TextView tv_blue_address;
        public TextView tv_blue_state;
        private onRecycleItemClickListener mListener;

        public MyViewHolder(View itemView, onRecycleItemClickListener listener) {
            super(itemView);
            ll_blue_list = (LinearLayout) itemView.findViewById(R.id.oneDevice);
            tv_blue_name = (TextView) itemView.findViewById(R.id.tv_blue_name);
            tv_blue_address = (TextView) itemView.findViewById(R.id.tv_blue_address);
            tv_blue_state = (TextView) itemView.findViewById(R.id.tv_blue_state);
            mListener = listener;
            ll_blue_list.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface onRecycleItemClickListener {
        void onItemClick(View v, int position);
    }
}