package com.eventr.app.events.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eventr.app.events.R;
import com.eventr.app.events.models.EventGroup;
import com.eventr.app.events.GroupDetailActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Suraj on 23/08/16.
 */
public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.ViewHolder> {
    private List<EventGroup> mItems;

    public GroupsRecyclerAdapter(List<EventGroup> items) {
        mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_row, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        EventGroup item = mItems.get(i);
        viewHolder.groupNameView.setText(item.getName());
        viewHolder.groupEventNameView.setText(item.getEventName());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.group_title) public TextView groupNameView;
        @BindView(R.id.group_event_name) public TextView groupEventNameView;
        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setClickable(true);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int groupIndex = getAdapterPosition();
            Intent intent = new Intent(v.getContext(), GroupDetailActivity.class);
            intent.putExtra(v.getContext().getString(R.string.intent_group_detail_key), mItems.get(groupIndex));
            v.getContext().startActivity(intent);
        }
    }
}
