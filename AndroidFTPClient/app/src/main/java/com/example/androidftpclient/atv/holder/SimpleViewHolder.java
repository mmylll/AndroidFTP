package com.example.androidftpclient.atv.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.androidftpclient.atv.model.TreeNode;
import com.example.androidftpclient.atv.view.AndroidTreeView;


/**
 * Created by Bogdan Melnychuk on 2/11/15.
 */
public class SimpleViewHolder extends TreeNode.BaseNodeViewHolder<Object> {

    public SimpleViewHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, Object value) {
        final TextView tv = new TextView(context);
        tv.setText(String.valueOf(value));
        return tv;
    }

    @Override
    public void toggle(boolean active) {

    }

    @Override
    public void setTreeViev(AndroidTreeView androidTreeView) {

    }
}
