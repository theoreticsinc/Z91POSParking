package com.theoretics.ui;

import java.util.ArrayList;
import java.util.List;

import com.theoretics.mobilepos.R;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 自定义适配器
 * 
 * @author zhengwei
 *
 */
public class MyPagerAdapter extends PagerAdapter {
	Vibrator vibrator;
	ArrayList<ImageInfo> data;
	Activity activity;
	LayoutParams params;
	List<notify> listeners = new ArrayList<notify>();
	int Cnt;

	public void addNotify(notify listener) {
		listeners.add(listener);
	}

	public MyPagerAdapter(Activity activity, ArrayList<ImageInfo> data) {
		this.activity = activity;
		this.data = data;
		vibrator = (Vibrator) activity
				.getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		// return 2;
		return 1;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int index) {
		View view = activity.getLayoutInflater().inflate(R.layout.grid, null);
		GridView gridView = (GridView) view.findViewById(R.id.gridView1);
		gridView.setNumColumns(2);
		gridView.setVerticalSpacing(5);
		gridView.setHorizontalSpacing(5);
		gridView.setAdapter(new BaseAdapter() {

			@Override
			public int getCount() {
				return 3;
			}

			@Override
			public Object getItem(int position) {
				return position;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View item = LayoutInflater.from(activity).inflate(
						R.layout.grid_item, null);
				ImageView iv = (ImageView) item.findViewById(R.id.imageView1);
				RelativeLayout relativeLayout = (RelativeLayout) item
						.findViewById(R.id.relativeLayout);
				iv.setImageResource((data.get(index * 8 + position)).imageId);
				relativeLayout.setBackgroundResource((data.get(index * 8
						+ position)).bgId);
				relativeLayout.getBackground().setAlpha(225);
				TextView tv = (TextView) item.findViewById(R.id.msg);
				tv.setText((data.get(index * 8 + position)).imageMsg);
				return item;
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				for (notify nf : listeners) {
					nf.onItemClick(position);
				}
			}
		});

		((ViewPager) container).addView(view);

		return view;
	}

	public interface notify {
		public void onItemClick(int position);
	}
}
