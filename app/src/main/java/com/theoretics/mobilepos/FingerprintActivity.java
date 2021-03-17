package com.theoretics.mobilepos;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.imagpay.MessageHandler;
import com.imagpay.fingerprint.FingerprintHandler;
import com.imagpay.fingerprint.FingerprintListener;
import com.imagpay.utils.HCBoolean;
import com.theoretics.ui.BaseActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class FingerprintActivity extends BaseActivity implements
		FingerprintListener, OnClickListener {
	private MessageHandler msghandler;
	private TextView tv_text;
	private ImageView iv_result;
	private String files = "/sdcard/";
	// fingerprint SDK
	private FingerprintHandler fignerprintHandler;
	private long lastClick = 0;
	Handler handleros = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 101:
				iv_result.setVisibility(View.VISIBLE);
				iv_result.setImageBitmap((Bitmap) msg.obj);
				break;
			case 100:
				msghandler.sendMessage((String) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setChildContentView(R.layout.activity_finger);
		setTitle(getResources().getString(R.string.finger_test));
		tv_text = (TextView) findViewById(R.id.f_status);
		msghandler = new MessageHandler(tv_text);
		Button bt_finger = (Button) findViewById(R.id.bt_add);
		bt_finger.setOnClickListener(this);
		Button bt_verify = (Button) findViewById(R.id.bt_verify);
		bt_verify.setOnClickListener(this);
		iv_result = (ImageView) findViewById(R.id.iv_result);
		// only for Z91 at present
		// init fignerprint SDK
		fignerprintHandler = FingerprintHandler.getInstance(this);
		// add linstener for operate fignerprint
		fignerprintHandler.addFignerprintListener(this);
		// show debug info
		fignerprintHandler.setShowLog(true);
	}

	@Override
	public void onAuthenticationFailed(int arg0) {

	}

	@Override
	public void onAuthenticationSucceeded(int arg0, Object arg1) {

	}

	@Override
	public void onEnrollmentProgress(int arg0, int arg1, int arg2) {
		if (arg2 == 0 && arg1 == 0) {
			sendMessage("Fingerprint ID:" + arg0 + "  Enrollment SUCCESSFUL!");
		} else {
			sendMessage("Fingerprint ID:" + arg0);
			sendMessage("remaining times:" + arg1);
			sendMessage("reason:" + arg2);
		}
	}

	private void sendMessage(String msg) {
		msghandler.sendMessage(msg);
	}

	@Override
	protected void onDestroy() {
		fignerprintHandler.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (!clickCheck()) {
			sendMessage(getResources().getString(R.string.button_tip));
			return;
		}
		tv_text.setText("");
		iv_result.setImageDrawable(null);
		switch (v.getId()) {
		case R.id.bt_add:
			fignerprintHandler.getImage();
			sendMessage("Start to enrollment fignerprint...");
			break;
		case R.id.bt_verify:
			fignerprintHandler.config(115200);
			break;
		default:
			break;
		}
	}

	@Override
	public void onGetImageComplete(String arg0, byte[] arg1) {
		final Message msg = new Message();
		if (!HCBoolean.isEmpty(arg0) && "00".equals(arg0)) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String name = sdf.format(new Date()) + ".bmp";
				// convert image data to bmp
				Bitmap bitmap = fignerprintHandler.generateBmp(arg1, files
						+ name);
				msg.what = 101;
				msg.obj = bitmap;
				handleros.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			msg.what = 100;
			if (arg0.equals("01")) {
				msg.obj = getResources().getString(R.string.finger_tip01);
			} else if (arg0.equals("02")) {
				msg.obj = getResources().getString(R.string.finger_tip02);
			} else if (arg0.equals("03")) {
				msg.obj = getResources().getString(R.string.finger_tip03);
			} else if (arg0.equals("FF")) {
				msg.obj = getResources().getString(R.string.finger_tiperr);
			}
			handleros.sendMessage(msg);
		}

	}

	private boolean clickCheck() {
		if (System.currentTimeMillis() - lastClick <= 3000) {
			return false;
		}
		lastClick = System.currentTimeMillis();
		return true;
	}
}
