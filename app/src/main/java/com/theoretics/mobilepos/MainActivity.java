package com.theoretics.mobilepos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Layout.Alignment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.imagpay.PrnStrFormat;
import com.imagpay.Settings;
import com.imagpay.SwipeEvent;
import com.imagpay.SwipeListener;
import com.imagpay.enums.CardDetected;
import com.imagpay.enums.EmvStatus;
import com.imagpay.enums.PosLED;
import com.imagpay.enums.PrintStatus;
import com.imagpay.enums.PrnTextFont;
import com.imagpay.mpos.MposHandler;
import com.theoretics.ui.ImageInfo;
import com.theoretics.ui.MyPagerAdapter;
import com.theoretics.util.DBHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements
		com.theoretics.ui.MyPagerAdapter.notify, SwipeListener {
	ArrayList<ImageInfo> data;
	private static TextView mynum;
	MyPagerAdapter adapter;
	Button btn;

	/**** SDK ***/
	private static String TAG = "TheoreticsPOS";
	private MposHandler handler;
	private Settings setting;
	private Context context;

	private DBHelper dbh;

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 101:
				Toast.makeText(MainActivity.this,
						"Printing now,pls wait for a moment", Toast.LENGTH_LONG)
						.show();
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mynum = (TextView) findViewById(R.id.mynum);
		initData();
		ViewPager vpager = (ViewPager) findViewById(R.id.vPager);
		adapter = new MyPagerAdapter(MainActivity.this, data);
		adapter.addNotify(this);
		vpager.setAdapter(adapter);
		vpager.setPageMargin(50);
		vpager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				mynum.setText("" + (int) (arg0 + 1));
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		initSDK();
		btn = (Button) findViewById(R.id.set);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String versionname = "";
				PackageManager pm = getPackageManager();
				try {
					PackageInfo packageInfo = pm.getPackageInfo(
							getPackageName(), 0);
					versionname = packageInfo.versionName;
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
				Toast.makeText(getApplicationContext(), versionname,
						Toast.LENGTH_LONG).show();
			}
		});

		dbh = new DBHelper(this);
		//dbh.insertContact("","", "", "", "", 0, 0);
/*
        SQLiteDatabase mydatabase = openOrCreateDatabase("theoretics.db",MODE_PRIVATE,null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS TutorialsPoint(Username VARCHAR,Password VARCHAR);");
        mydatabase.execSQL("INSERT INTO TutorialsPoint VALUES('admin','admin');");
        Cursor resultSet = mydatabase.rawQuery("Select * from TutorialsPoint",null);
        resultSet.moveToFirst();
        String username = resultSet.getString(0);
        String password = resultSet.getString(1);
*/
	}

	/**** SDK ***/
	private void initSDK() {
		// Init SDK,call singleton function,so that you can keeping on the
		// connect in the whole life cycle
		handler = MposHandler.getInstance(this);
		setting = Settings.getInstance(handler);
		// power on the device when you need to read card or print
		setting.mPosPowerOn();
        setting.mposLedSwitch(PosLED.LED_BLUE, true);
		try {
			// for 90,delay 1S and then connect
			// Thread.sleep(1000);
			// connect device via serial port
			if (!handler.isConnected()) {
				sendMessage("Connect Res:" + handler.connect());
			} else {
				handler.close();
				sendMessage("ReConnect Res:" + handler.connect());
			}
		} catch (Exception e) {
			sendMessage(e.getMessage());

		}

		// add linstener for connection
		handler.addSwipeListener(this);
		// add linstener for read IC chip card
		// handler.addEMVListener(this);
 		handler.setShowLog(true);
	}

	@Override
	public void onItemClick(int position) {

		switch (position) {
		case 0:
			setting.mposLedSwitch(PosLED.LED_RED, true);
			setting.mposLedSwitch(PosLED.LED_GREEN, false);
			setting.mposLedSwitch(PosLED.LED_BLUE, false);
			//AsyncDataInit runDB = new AsyncDataInit();
			//String sleepTime = "60";
			//runDB.execute(sleepTime);

			//printTicket();
            printOut();
			// printTicket();
			// printtext();
			// printGeorgianText();
			break;
		case 1:
			setting.mposLedSwitch(PosLED.LED_RED, false);
			setting.mposLedSwitch(PosLED.LED_GREEN, true);
			setting.mposLedSwitch(PosLED.LED_BLUE, false);
			startActivity(new Intent(MainActivity.this, CardtestActivity.class));
			break;
		case 2:
			setting.mposLedSwitch(PosLED.LED_RED, false);
			setting.mposLedSwitch(PosLED.LED_GREEN, false);
			setting.mposLedSwitch(PosLED.LED_BLUE, true);
			//startActivity(new Intent(MainActivity.this, QRtestActivity.class));
			//AsyncTaskRunner runner = new AsyncTaskRunner();
			//String sleepTime2 = "60";
			//runner.execute(sleepTime2);
			/*
			ArrayList array_list = dbh.getAllContacts();
			System.out.println("Iterator");
			Iterator iter = array_list.iterator();
			while (iter.hasNext()) {
				System.out.println(iter.next());
			}
			*/

			break;
		default:
			break;
		}
	}

	protected void sendMessage(String string) {
		Log.i(TAG, "==>:" + string);
	}

	/**** SDK start ****/
	private void printGeorgianText() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (setting.isPrinting()) {// check print status
					Message msg = new Message();
					msg.what = 101;
					mHandler.sendMessage(msg);
					Log.d(TAG, "setting.isPrinting():" + setting.isPrinting());
					return;
				}
				StringBuffer receipts = new StringBuffer();
				receipts.append("In Shenzhen კომპიუტერული ტექნიკა Co, შპს მაგნიტური, ბეჭდვის ფუნქცია ტესტები\n");
				setting.prnStr(receipts.toString());
				setting.prnStart();
			}
		}).start();
	}

    private void printOut() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (setting.isPrinting()) {// check print status
                    Message msg = new Message();
                    msg.what = 101;
                    mHandler.sendMessage(msg);
                    Log.d(TAG, "setting.isPrinting():" + setting.isPrinting());
                    return;
                }
                StringBuffer receipts = new StringBuffer();
                receipts.append("This stub is NOT BIR Registered  \nPlease properly dispose\n");
                // 1. print text with default style and font
                setting.prnStr(receipts.toString());
                receipts.setLength(0);
                receipts.append("TESTING PRINTOUT ONLY\n");
                PrnStrFormat psf = new PrnStrFormat();
                psf.setFont(PrnTextFont.MONOSPACE);// specified font
                // // 2. print text with specified style and font
                // setting.prnStr(receipts.toString(), psf);
                receipts.setLength(0);
                receipts.append("print with DejaVuSansMono.ttf\n");
                // // 3. print text with custom font
                psf.setFont(PrnTextFont.CUSTOM);
                psf.setAm(getAssets());
                psf.setPath("fonts/DejaVuSansMono.ttf");
                setting.prnStr(receipts.toString(), psf);

                receipts.setLength(0);
                receipts.append("print with DejaVuSansMono-Oblique.ttf\n");
                // 3. print text with custom font
                psf.setFont(PrnTextFont.CUSTOM);
                psf.setAm(getAssets());
                psf.setPath("fonts/DejaVuSansMono-Oblique.ttf");
                setting.prnStr(receipts.toString(), psf);

                receipts.setLength(0);
                receipts.append("print with DejaVuSansMono-BoldOblique.ttf\n");

                // 3. print text with custom font
                psf.setFont(PrnTextFont.CUSTOM);
                psf.setAm(getAssets());
                psf.setPath("fonts/DejaVuSansMono-BoldOblique.ttf");
                setting.prnStr(receipts.toString(), psf);

                receipts.setLength(0);
                receipts.append("print with DejaVuSansMono-Bold.ttf\n");

                // 3. print text with custom font
                psf.setFont(PrnTextFont.CUSTOM);
                psf.setAm(getAssets());
                psf.setPath("fonts/DejaVuSansMono-Bold.ttf");
                setting.prnStr(receipts.toString(), psf);

                receipts.setLength(0);
                receipts.append("Theoretics Mobile POS \nPlease properly dispose\n\n");
                // 1. print text with default style and font
                setting.prnStr(receipts.toString());
                receipts.setLength(0);
                // 4. start to print
                setting.prnStart();
            }
        }).start();
    }

	private void printtest2() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (setting.isPrinting()) {// check print status
					Message msg = new Message();
					msg.what = 101;
					mHandler.sendMessage(msg);
					Log.d(TAG, "setting.isPrinting():" + setting.isPrinting());
					return;
				}
				StringBuffer receipts = new StringBuffer();
				receipts.append("The cardholder stub   \nPlease properly keep\n");
				// 1. print text with default style and font
				setting.prnStr(receipts.toString());
				receipts.setLength(0);
				receipts.append("Merchant Name:ABC\n");
				receipts.append("Merchant No.:846584000103052\n");
				receipts.append("Terminal No.:12345678\n");
				PrnStrFormat psf = new PrnStrFormat();
				psf.setFont(PrnTextFont.MONOSPACE);// specified font
				// // 2. print text with specified style and font
				// setting.prnStr(receipts.toString(), psf);
				receipts.setLength(0);
				receipts.append("print with DejaVuSansMono.ttf\n");
				receipts.append("Trade Type:consumption\n");
				receipts.append("Serial No.:000024  \nAuthenticode:096706\n");
				receipts.append("Date/Time:2016/09/01 11:27:12\n");
				receipts.append("Ref.No.:123456789012345\n");
				receipts.append("Amount:$ 100.00\n");
				// // 3. print text with custom font
				psf.setFont(PrnTextFont.CUSTOM);
				psf.setAm(getAssets());
				psf.setPath("fonts/DejaVuSansMono.ttf");
				setting.prnStr(receipts.toString(), psf);

				receipts.setLength(0);
				receipts.append("print with DejaVuSansMono-Oblique.ttf\n");
				receipts.append("Trade Type:consumption\n");
				receipts.append("Serial No.:000024  \nAuthenticode:096706\n");
				receipts.append("Date/Time:2016/09/01 11:27:12\n");
				receipts.append("Ref.No.:123456789012345\n");
				receipts.append("Amount:$ 100.00\n");
				// 3. print text with custom font
				psf.setFont(PrnTextFont.CUSTOM);
				psf.setAm(getAssets());
				psf.setPath("fonts/DejaVuSansMono-Oblique.ttf");
				setting.prnStr(receipts.toString(), psf);

				receipts.setLength(0);
				receipts.append("print with DejaVuSansMono-BoldOblique.ttf\n");
				receipts.append("Trade Type:consumption\n");
				receipts.append("Serial No.:000024  \nAuthenticode:096706\n");
				receipts.append("Date/Time:2016/09/01 11:27:12\n");
				receipts.append("Ref.No.:123456789012345\n");
				receipts.append("Amount:$ 100.00\n");
				// 3. print text with custom font
				psf.setFont(PrnTextFont.CUSTOM);
				psf.setAm(getAssets());
				psf.setPath("fonts/DejaVuSansMono-BoldOblique.ttf");
				setting.prnStr(receipts.toString(), psf);

				receipts.setLength(0);
				receipts.append("print with DejaVuSansMono-Bold.ttf\n");
				receipts.append("Trade Type:consumption\n");
				receipts.append("Serial No.:000024  \nAuthenticode:096706\n");
				receipts.append("Date/Time:2016/09/01 11:27:12\n");
				receipts.append("Ref.No.:123456789012345\n");
				receipts.append("Amount:$ 100.00\n");
				// 3. print text with custom font
				psf.setFont(PrnTextFont.CUSTOM);
				psf.setAm(getAssets());
				psf.setPath("fonts/DejaVuSansMono-Bold.ttf");
				setting.prnStr(receipts.toString(), psf);
				// 4. start to print
				setting.prnStart();
			}
		}).start();
	}

	private void printTicket() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (setting.isPrinting()) {// check print status
					Message msg = new Message();
					msg.what = 101;
					mHandler.sendMessage(msg);
					Log.d(TAG, "setting.isPrinting():" + setting.isPrinting());
					return;
				}
				StringBuffer receipts = new StringBuffer();
				receipts.append("POS Signed Order\n");
				PrnStrFormat psf = new PrnStrFormat();
				psf.setTextSize(34);
				psf.setAli(Alignment.ALIGN_CENTER);
				setting.prnStr(receipts.toString(), psf);
				receipts.setLength(0);
				receipts.append("The cardholder stub   \nPlease properly keep\n");
				receipts.append("-----------------------------------------------\n");
				receipts.append("Merchant Name:ABC\n");
				receipts.append("Merchant No.:846584000103052\n");
				receipts.append("Terminal No.:12345678\n");
				receipts.append("categories:visa card\n");
				receipts.append("Period of Validity:2018/04\n");
				receipts.append("Batch no:000101\n");
				receipts.append("Card Number:\n");
				receipts.append("622202400******0269\n");
				receipts.append("Trade Type:consumption\n");
				receipts.append("Serial No.:000024  \nAuthenticode:096706\n");
				receipts.append("Date/Time:2018/04/28 11:27:12\n");
				receipts.append("Ref.No.:123456789012345\n");
				receipts.append("Amount:$ 100.00\n");
				receipts.append("-----------------------------------------------\n");
				setting.prnStr(receipts.toString());
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
				opt.inPurgeable = true;
				opt.inInputShareable = true;
				@SuppressLint("ResourceType") InputStream is = getResources().openRawResource(R.drawable.icon16);
				Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);
				setting.prnBitmap(bitmap);
				setting.mPosPrintAlign(Settings.MPOS_PRINT_ALIGN_CENTER);
				setting.prnStart();
			}
		}).start();
	}

	private void printTextLine(final String text2Print) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (setting.isPrinting()) {// check print status
					Message msg = new Message();
					msg.what = 101;
					mHandler.sendMessage(msg);
					Log.d(TAG, "setting.isPrinting():" + setting.isPrinting());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Log.e(TAG, "printTextLine Error:" + e);
					}
				}
				StringBuffer receipts = new StringBuffer();
				receipts.append(text2Print + "\n");
				setting.prnStr(receipts.toString());
				setting.prnStart();
			}
		}).start();
	}

	private void printtext() {
		new Thread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				// if you need to print a big bmp,advice you to convert it to
				// printing data at first
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
				opt.inPurgeable = true;
				opt.inInputShareable = true;
				@SuppressLint("ResourceType") InputStream is = getResources().openRawResource(R.drawable.icon3);
				Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);
				List<byte[]> data = setting.mPosPrnConvertBmp(bitmap);
				// call mPosEnterPrint open print
				boolean bb = setting.mPosEnterPrint();
				sendMessage("mPosEnterPrint:" + bb);
				if (bb) {
					setting.mPosPrintFontSwitch(Settings.MPOS_PRINT_FONT_NEW);
					// show print receipts
					setting.mPosPrintAlign(Settings.MPOS_PRINT_ALIGN_CENTER);
					setting.mPosPrintTextSize(Settings.MPOS_PRINT_TEXT_DOUBLE_HEIGHT);
					setting.mPosPrnStr("POS Signed Order");
					setting.mPosPrintLn();
					setting.mPosPrintTextSize(Settings.MPOS_PRINT_TEXT_NORMAL);
					setting.mPosPrintAlign(Settings.MPOS_PRINT_ALIGN_LEFT);
					setting.mPosPrnStr("The cardholder stub   \nPlease properly keep");
					setting.mPosPrnStr("--------------------------");
					setting.mPosPrnStr("Merchant Name:ABC");
					setting.mPosPrnStr("Merchant No.:846584000103052");
					setting.mPosPrnStr("Terminal No.:12345678");
					setting.mPosPrnStr("categories: visa card");
					setting.mPosPrnStr("Period of Validity:2016/07");
					setting.mPosPrnStr("Batch no.:000101");
					setting.mPosPrnStr("Card Number:");
					setting.mPosPrnStr("622202400******0269");
					setting.mPosPrnStr("Trade Type:consumption");
					setting.mPosPrnStr("Serial No.:000024  \nAuthenticode:096706");
					setting.mPosPrnStr("Date/Time:2016/09/01 11:27:12");
					setting.mPosPrnStr("Ref.No.:123456789012345");
					setting.mPosPrnStr("Amount:$ 100.00");
					setting.mPosPrnStr("--------------------------");
					setting.mPosPrintAlign(Settings.MPOS_PRINT_ALIGN_CENTER);
					setting.mPosPrnImg(data);
					setting.mPosPrintLn();
					if (!bitmap.isRecycled()) {
						bitmap.recycle();
					}
					bitmap = null;
				} else {
					showToast("" + getResources().getText(R.string.check_paper));
				}
			}
		}).start();
	}

	@Override
	public void onCardDetect(CardDetected arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(SwipeEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnected(SwipeEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEmvStatus(EmvStatus arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onParseData(SwipeEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPrintStatus(PrintStatus arg0) {
		Log.d(TAG, "printStatus:" + arg0.toString());

	}

	@Override
	protected void onDestroy() {
		// power off the device when you do not need to read card or print for a
		// long time
		setting.mPosPowerOff();
		// ondestroy the sdk when you exit the app
		handler.onDestroy();
		setting.onDestroy();
		super.onDestroy();
	}

	/**** SDK end ****/

	private void showToast(String mesg) {
		Message mssg = new Message();
		mssg.what = 10;
		mssg.obj = "" + mesg;
		handleros.sendMessage(mssg);
	}

	Handler handleros = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 10:
				Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT)
						.show();
				break;

			default:
				break;
			}
		}
	};

	private String hexToSting(String ver) {
		if (ver == null)
			return "";
		String[] tmps = ver.trim().replaceAll("..", "$0 ").split(" ");
		StringBuffer sbf = new StringBuffer();
		for (String str : tmps) {
			sbf.append((char) Integer.parseInt(str, 16));
		}
		ver = sbf.toString();
		return ver;
	}

	private void showInfor(String ver, String sn) {
		final AlertDialog.Builder normalDialog = new AlertDialog.Builder(
				MainActivity.this);
		normalDialog.setTitle(""
				+ getResources().getString(R.string.device_information));

		normalDialog.setMessage("" + getResources().getString(R.string.version)
				+ ":" + hexToSting(ver) + "\n" + "sn:" + sn);
		normalDialog.show();
	}

	private void initData() {
		data = new ArrayList<ImageInfo>();
		mynum.setText("1");
		data.add(new ImageInfo(getResources().getString(R.string.printtest),
				R.drawable.icon1, R.drawable.icon_bg01));
		data.add(new ImageInfo(getResources().getString(
				R.string.contactless_card_test), R.drawable.icon2,
				R.drawable.icon_bg02));
		data.add(new ImageInfo(getResources().getString(R.string.test_sim), R.drawable.icon3, R.drawable.icon_bg01));
		//data.add(new ImageInfo(getResources().getString(R.string.qr_test),R.drawable.icon4, R.drawable.icon_bg02));
		//data.add(new ImageInfo(getResources().getString(R.string.finger_test),R.drawable.icon5, R.drawable.icon_bg02));
		//data.add(new ImageInfo(getResources().getString(R.string.device_information), R.drawable.icon7, R.drawable.icon_bg02));


	}

	private class AsyncDataInit extends AsyncTask<String, String, String> {

		private String resp;
		ProgressDialog progressDialog;

		@Override
		protected String doInBackground(String... params) {
			publishProgress("initializing DB..."); // Calls onProgressUpdate()
			dbh.initCardDatabase();
			return "Done";
		}


		@Override
		protected void onPostExecute(String result) {
			// execution of result of Long time consuming operation
			progressDialog.dismiss();
			mynum.setText(result);
		}


		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this,
					"Init Database...",
					"Wait for "+mynum.getText().toString()+ " seconds");
		}


		@Override
		protected void onProgressUpdate(String... text) {
			mynum.setText(text[0]);

		}
	}


	private class AsyncTaskRunner extends AsyncTask<String, String, String> {

		private String resp;
		private String hr, min, sec;
		ProgressDialog progressDialog;
        String apiUrl = "http://192.168.1.80/timecheck.php";

        @Override
		protected String doInBackground(String... params) {
			publishProgress("retrieving..."); // Calls onProgressUpdate()
			/*
			try {
				int time = Integer.parseInt(params[0])*1000;

				Thread.sleep(time);
				resp = "Slept for " + params[0] + " seconds";
			} catch (InterruptedException e) {
				e.printStackTrace();
				resp = e.getMessage();
			} catch (Exception e) {
				e.printStackTrace();
				resp = e.getMessage();
			}
			*/

            // implement API in background and store the response in current variable
            String current = "";
            try {
                URL url;
                HttpURLConnection urlConnection = null;
                try {
					printTextLine("Retrieving Data from Server...");
                    url = new URL(apiUrl);

                    urlConnection = (HttpURLConnection) url
                            .openConnection();

                    InputStream in = urlConnection.getInputStream();

                    InputStreamReader isw = new InputStreamReader(in);

                    int data = isw.read();
                    while (data != -1) {
                        current += (char) data;
                        data = isw.read();

                    }
                    System.out.print(current);
                    // return the data to onPostExecute method
                    //printTextLine(current);
                    //return "2";

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
			try {
				JSONObject jsonObj = new JSONObject(current);
				hr = jsonObj.getString("hr");
				min = jsonObj.getString("min");
				sec = jsonObj.getString("sec");

				printTextLine("JSON The Hour: " + hr + " Min: " + min + " Sec: " +sec);
				System.out.println("JSON The Hour: " + hr + " Min: " + min + " Sec: " +sec);

                printTextLine(current);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "3";
		}


		@Override
		protected void onPostExecute(String result) {
			// execution of result of Long time consuming operation
			progressDialog.dismiss();
			mynum.setText(result);
		}


		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this,
					"Checking Card...",
					"Wait for "+mynum.getText().toString()+ " seconds");
		}


		@Override
		protected void onProgressUpdate(String... text) {
			mynum.setText(text[0]);

		}
	}

}
