package com.shao.camera.view.listview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * @author Swain
 * @function 自定义adapter recycleview使用
 * @date 2015/9/16
 */
public abstract class UniversalAdapter<T> extends BaseAdapter {
	protected Context context;
	protected List<T> datas;
	private int layoutId;

	public UniversalAdapter(Context context, List<T> datas, int layoutId)
	{
		this.layoutId = layoutId;
		this.context = context;
		this.datas = datas;
	}

	public void addListMore(List<T> list) {
		datas.addAll(list);
		notifyDataSetChanged();
	}

	public void delListItem(int positation) {
		datas.remove(positation);
		notifyDataSetChanged();
	}

	public void delList() {
		datas.clear();
		notifyDataSetChanged();
	}

	public void setList(List<T> list) {
		this.datas = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return null == datas ? 0 : datas.size();
	}

	@Override
	public T getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = ViewHolder.get(context, convertView, parent, layoutId, position);
		setDate(holder, getItem(position), position);
		return holder.getCovertView();
	}

	public abstract void setDate(ViewHolder holder, T t, int position);

}
