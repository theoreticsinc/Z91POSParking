package com.zxing.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.theoretics.mobilepos.R;
import com.theoretics.ui.BaseActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class QRtestActivity extends BaseActivity implements View.OnClickListener{
    private Button btn_create,btn_scanner;
    private ImageView imageView;
    private EditText et;
    private String time;
    private File file = null;
	private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_qrtest);
        setTitle(getResources().getString(R.string.qr_test));
        btn_create = (Button) findViewById(R.id.btn_create);
        btn_scanner = (Button) findViewById(R.id.btn_scanner);
        imageView = (ImageView) findViewById(R.id.image);
        et = (EditText) findViewById(R.id.editText);
        String res=getIntent().getStringExtra("QRCODE");
        et.setText(res==null?"":res);
        tv = (TextView) findViewById(R.id.tv_tips);
        btn_create.setOnClickListener(this);
        btn_scanner.setOnClickListener(this);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                saveCurrentImage();
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create:
                String msg = et.getText().toString();
                if(TextUtils.isEmpty(msg)){
                    Toast.makeText(QRtestActivity.this, "please input", Toast.LENGTH_LONG).show();
                    return;
                }
                //生成二维码图片，第一个参数是二维码的内容，第二个参数是正方形图片的边长，单位是像素
               // Generate a two-dimensional code picture, the first parameter is the content of two-dimensional code, the second parameter is the square picture side length, the unit is pixels
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapUtil.create2DCoderBitmap(msg, 400,400);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(bitmap);
                tv.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_scanner:
                Intent intent = new Intent(QRtestActivity.this, CaptureActivity.class);
                startActivity(intent);
                finish();
                break;

            default:
                break;
        }
    }
   
    //这种方法状态栏是空白，显示不了状态栏的信息
    //This method status bar is blank, can not display the status bar information
    private void saveCurrentImage()
    {
        //获取当前屏幕的大小
    	//Gets the size of the current screen
      /*  int width = imageView.getWidth();
//        int width = getWindow().getDecorView().getRootView().getWidth();
        int height = imageView.getHeight();
//        int height = getWindow().getDecorView().getRootView().getHeight();
        //生成相同大小的图片
        //Generate the same size of the picture
        Bitmap temBitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        View view =  getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();*/
        
        imageView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));  
        imageView.layout(0, 0, imageView.getMeasuredWidth(), imageView.getMeasuredHeight());  
        imageView.buildDrawingCache();  
        Bitmap bitmap = imageView.getDrawingCache(); 
        
        //从缓存中获取当前屏幕的图片,创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
        //Get the current screen image from the cache and create a copy of the DrawingCache because the bitmap that DrawingCache gets is disabled after being disabled
//        temBitmap = view.getDrawingCache();
        SimpleDateFormat df = new SimpleDateFormat("yyyymmddhhmmss");
        time = df.format(new Date());
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/screen",  "123456.png");
            if(!file.exists()){
                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/screen/" +  "123456.png";
                    final Result result = parseQRcodeBitmap(path);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(QRtestActivity.this, result.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();
            //禁用DrawingCahce否则会影响性能 ,而且不禁止会导致每次截图到保存的是第一次截图缓存的位图
            //Disable DrawingCahce otherwise it will affect the performance, and does not prohibit each capture will lead to the first screenshot to save the cache bitmap
//            view.setDrawingCacheEnabled(false);
        }
    }

    //解析二维码图片,返回结果封装在Result对象中
    //The QRcode is parsed and the result is wrapped in a Result object
    private com.google.zxing.Result  parseQRcodeBitmap(String bitmapPath){
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath,options);
        options.inSampleSize = options.outHeight / 400;
        if(options.inSampleSize <= 0){
            options.inSampleSize = 1; 
        }
        /**
         * 辅助节约内存设置 Auxiliary saves memory settings
         *
         * options.inPreferredConfig = Bitmap.Config.ARGB_4444;    
         * options.inPurgeable = true;
         * options.inInputShareable = true;
         */
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {
        }
        return result;
    }
}
