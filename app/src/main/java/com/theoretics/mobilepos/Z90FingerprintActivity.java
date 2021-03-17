package com.theoretics.mobilepos;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.imagpay.MessageHandler;
import com.imagpay.fingerprint.FingerprintHandler;
import com.imagpay.fingerprint.FingerprintListener;
import com.imagpay.mpos.MposHandler;
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
public class Z90FingerprintActivity extends BaseActivity implements
		FingerprintListener, OnClickListener {
	private MessageHandler msghandler;
	private TextView tv_text;
	private ImageView iv_result;
	private String files = "/sdcard/";
	// fingerprint SDK
	private FingerprintHandler fignerprintHandler;

	Handler handleros = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			iv_result.setVisibility(View.VISIBLE);
			iv_result.setImageBitmap((Bitmap) msg.obj);
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
		// only for Z90 at present
		// init fignerprint SDK
		MposHandler _mpos = MposHandler.getInstance(this);
		fignerprintHandler = FingerprintHandler.getInstance(this);
		// add linstener for operate fignerprint
		fignerprintHandler.addFignerprintListener(this);

	}

	@Override
	public void onAuthenticationFailed(int arg0) {

	}

	@Override
	public void onAuthenticationSucceeded(int arg0, Object arg1) {

	}

	@Override
	public void onEnrollmentProgress(int arg0, int arg1, int arg2) {

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
		tv_text.setText("");
		iv_result.setImageDrawable(null);
		switch (v.getId()) {
		case R.id.bt_add:
			sendMessage("start to get Image,pls wait for result...");
			new Thread(new Runnable() {
				@Override
				public void run() {
					// run in a thread
					fignerprintHandler.getImage();
					// wait for some seconds,SDK will call back
					// "onGetImageComplete" function
				}
			}).start();
			break;
		default:
			break;
		}
	}

	@Override
	public void onGetImageComplete(String arg0, byte[] arg1) {
		if (!HCBoolean.isEmpty(arg0) && "00".equals(arg0)) {
			final Message msg = new Message();
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
		}
	}
}
