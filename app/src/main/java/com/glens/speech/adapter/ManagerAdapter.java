package com.glens.speech.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.glens.speech.R;
import com.glens.speech.utils.DialogMessage;

import java.util.List;

/**
 *  区台经理
 */
public class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.ViewHolder> {

    //展示数据源存放
    private List<DialogMessage> mMsgList;

    //内部类 将子项的所有布局进行传入匹配
    static class ViewHolder extends RecyclerView.ViewHolder {
        //       布局元素
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;

        //        内部类的构造函数传入recycler子项的布局
        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_layout_manager);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_layout_manager);
            leftMsg = (TextView) view.findViewById(R.id.left_msg_manager);
            rightMsg = (TextView) view.findViewById(R.id.right_msg_manager);
        }
    }

    //    传入要展示的数据源
    public ManagerAdapter(List<DialogMessage> msgList) {
        mMsgList = msgList;
    }

    //    用于创建内部类viewholder实例 传入msg_item布局，在构造函数加载出所有布局
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_manager, parent, false);
        return new ViewHolder(view);
    }

    //    对recyclerview子项数据进行赋值，在子项滚动到屏幕内时执行。通过position参数得到当前项具体内容
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DialogMessage msg = mMsgList.get(position);
        if (msg.getType() == DialogMessage.TYPE_RECEIVED) {
            //收到消息，用左边的格式 右边格式隐藏
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else if (msg.getType() == DialogMessage.TYPE_SEND) {
            //发送消息，用右边格式 左边格式隐藏
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
        }
    }

    //    返回recyclerview有多少子项
    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

}