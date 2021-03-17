package com.theoretics.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 一些常用的公用方法
 * 
 */
public class PublicUtil {
	// EditText获取焦点
	public static void setFocus(EditText et) {
		et.setFocusable(true);
		et.setFocusableInTouchMode(true);
		et.requestFocus();
		et.requestFocusFromTouch();
	}

	// 格式化日期
	@SuppressLint("SimpleDateFormat")
	public static String current() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	// 显示对话框
	public static Dialog showDialog(Context context, View view, String title,
			String content) {
		Dialog alertDialog = new AlertDialog.Builder(context).setTitle(title)
				.setMessage(content).create();
		return alertDialog;
	}

	/**
	 * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
	 * 
	 * @param v
	 * @param event
	 * @return
	 */
	public static boolean isShouldHideInput(View v, MotionEvent event) {
		if (v != null && (v instanceof EditText)) {
			int[] l = { 0, 0 };
			v.getLocationInWindow(l);
			int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
					+ v.getWidth();
			if (event.getX() > left && event.getX() < right
					&& event.getY() > top && event.getY() < bottom) {
				// 点击EditText的事件，忽略它。
				return false;
			} else {
				// hideSoftInput(v.getWindowToken());
				return true;
			}
		}
		// 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
		return false;
	}

	/**
	 * 多种隐藏软件盘方法的其中一种
	 * 
	 * @param token
	 */
	public static void hideSoftInput(IBinder token, InputMethodManager im) {
		if (token != null) {
			im.hideSoftInputFromWindow(token,
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 进度条Dialog
	 * 
	 * @param context
	 * @param title
	 * @param text
	 * @return
	 */
	public static ProgressDialog getDialog(Context context, String title,
			String text) {
		ProgressDialog mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setTitle(title);
		mProgressDialog.setMessage(text);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		return mProgressDialog;
	}

	/**
	 * 密码输入
	 */
	public static void showPassMode(final EditText et) {
		et.setTransformationMethod(PasswordTransformationMethod.getInstance());
	}

	/**
	 * 发送handler消息
	 * 
	 * @param handler
	 * @param msg
	 */
	public static void sendHandler(Handler handler, int msg) {
		handler.sendMessage(handler.obtainMessage(msg));
	}

	/**
	 * 验证手机号码
	 * 
	 * @param mobiles
	 * @return [0-9]{5,9}
	 */
	public static boolean isMobileNO(String mobiles) {
		boolean flag = false;
		try {
			Pattern p = Pattern
					.compile("^((13[0-9])|(15[^4,\\D])|(18[0,2-9]))\\d{8}$");
			Matcher m = p.matcher(mobiles);
			flag = m.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
}
