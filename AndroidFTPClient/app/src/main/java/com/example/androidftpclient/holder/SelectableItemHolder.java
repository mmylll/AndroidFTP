package com.example.androidftpclient.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.androidftpclient.R;
import com.unnamed.b.atv.model.TreeNode;


/**
 * Created by Bogdan Melnychuk on 2/15/15.
 */
public class SelectableItemHolder extends TreeNode.BaseNodeViewHolder<SelectableItemHolder.IconTreeItem> {
    private TextView tvValue;
    private CheckBox nodeSelector;

    public SelectableItemHolder(Context context) {
        super(context);
    }


    @Override
    public View createNodeView(final TreeNode node, SelectableItemHolder.IconTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_selectable_item, null, false);

        tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.text);


        nodeSelector = (CheckBox) view.findViewById(R.id.node_selector);
        nodeSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                node.setSelected(isChecked);
            }
        });
        nodeSelector.setChecked(node.isSelected());


        if (node.isLastChild()) {
            view.findViewById(R.id.bot_line).setVisibility(View.INVISIBLE);
        }


        return view;
    }


    @Override
    public void toggleSelectionMode(boolean editModeEnabled) {
        nodeSelector.setVisibility(editModeEnabled ? View.VISIBLE : View.GONE);
        nodeSelector.setChecked(mNode.isSelected());
    }

    public static class IconTreeItem {
        public int icon;
        public String text;

        public IconTreeItem(int icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }
}
