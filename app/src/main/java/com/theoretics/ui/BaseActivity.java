package com.theoretics.ui;

import com.theoretics.mobilepos.R;
import com.theoretics.util.PublicUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 
 * Activity界面父类,给其它界面继承(主要是抽出布局设置,标题设置,输入框焦点处理)
 * 
 */
public class BaseActivity extends Activity implements OnClickListener {
	/**
	 * activity主显示区域
	 */
	protected RelativeLayout baseMainFrame;

	protected LayoutInflater inflater;

	protected LinearLayout return_iv;
	protected TextView title_tv;
	protected ImageView btn_right;
	public String TAG = "Devil";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base);
		baseMainFrame = (RelativeLayout) findViewById(R.id.baseMainFrame);
		inflater = LayoutInflater.from(this);

		title_tv = (TextView) findViewById(R.id.base_title);
	}

	/**
	 * 在子类的activity中设置主要的显示区域
	 * 
	 */
	protected void setChildContentView(int layoutId) {
		ViewGroup layout = (ViewGroup) inflater.inflate(layoutId, null);
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
		baseMainFrame.addView(layout, layoutParams);
	}

	/**
	 * 设置标题上的字
	 * 
	 */
	protected void setTitle(String text) {
		title_tv.setText(text);
	}

	/**
	 * 设置返回按钮是否显示
	 */
	protected void setVisibileBackBtn(boolean flag) {
		if (flag)
			return_iv.setVisibility(View.VISIBLE);
		else
			return_iv.setVisibility(View.INVISIBLE);
	}

	/**
	 * 设置返回按钮是否显示
	 */
	protected void setVisibileRightBtn(boolean flag) {
		if (flag)
			btn_right.setVisibility(View.VISIBLE);
		else
			btn_right.setVisibility(View.INVISIBLE);
	}

	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;
		}

	}

	// 隐藏软键盘
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			// 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
			View v = getCurrentFocus();

			if (PublicUtil.isShouldHideInput(v, ev)) {
				InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				PublicUtil.hideSoftInput(v.getWindowToken(), im);
			}
		}
		return super.dispatchTouchEvent(ev);
	}

}
