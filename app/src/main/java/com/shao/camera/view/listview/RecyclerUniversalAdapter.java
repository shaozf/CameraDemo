package com.shao.camera.view.listview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author Swain
 * @function 自定义adapter recycleview使用
 * @date 2015/9/16
 */
public abstract class RecyclerUniversalAdapter<T> extends RecyclerView.Adapter<RecycleViewHolder> {
	protected Context context;
	protected List<T> list;
	private int layoutId;

	public RecyclerUniversalAdapter(Context context, List<T> list, int layoutId)
	{
		this.context = context;
		this.list = list;
		this.layoutId = layoutId;
	}

	public void addListMore(List<T> list) {
		this.list.addAll(list);
		notifyDataSetChanged();
	}

	public void addListItem(T list) {
		this.list.add(list);
		notifyItemInserted(getItemCount());
	}

	public void delListItem(int positation) {
		this.list.remove(positation);
		notifyDataSetChanged();
	}

	public void delList() {
		this.list.clear();
		notifyDataSetChanged();
	}

	public void setList(List<T> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public List<T> getList() {
		return this.list;
	}

	@Override
	public int getItemCount() {
		return null == list ? 0 : list.size();
	}

	@Override
	public void onBindViewHolder(RecycleViewHolder holder, int position) {
		setDate(holder, list.get(position), position);
	}

	@Override
	public RecycleViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
		RecycleViewHolder mHolder = new RecycleViewHolder(view);
		return mHolder;
	}


	public abstract void setDate(RecycleViewHolder holder, T t, int position);
}
