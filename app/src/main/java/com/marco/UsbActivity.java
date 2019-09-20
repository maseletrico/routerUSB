package com.marco;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.marco.smartrouterdev.MainActivity;


import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UsbActivity extends AppCompatActivity implements Runnable {

    private static final String TAG = "USB ACTIVITY ";
    private static final String ACTION_USB_PERMISSION = "com.marco.smartrouterdev.USB_PERMISSION";
    private UsbManager usbManager;
    private PendingIntent permissionIntent;
    private boolean mPermissionRequestPending;

    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    //UsbCommunication usbCommunication;
    //USBAccessoryManager accessoryManager;

    public boolean CONNECTED = false;

    MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessoryList = manager.getAccessoryList();

        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(usbReceiver, filter);

        if (getLastNonConfigurationInstance() != null) {
            mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
            openAccessory(mAccessory);
        }

    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Toast.makeText(UsbActivity.this, "ACTION", Toast.LENGTH_SHORT).show();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized (this) {
                    Toast.makeText(UsbActivity.this, "ACTION_USB_PERMISSION ", Toast.LENGTH_SHORT).show();
                    UsbAccessory routerAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (routerAccessory != null) {
                            //call method to set up accessory communication
                            Toast.makeText(UsbActivity.this, "USB REGISTERED", Toast.LENGTH_SHORT).show();
                            openAccessory(routerAccessory);
                            //mainActivity.imageViewAccesspoint.setImageResource(R.mipmap.usb);
                            CONNECTED=true;
                        }
                    } else {
                        Log.d(TAG, "permission denied for accessory " + routerAccessory);
                        Toast.makeText(UsbActivity.this, "permission denied for accessory " + routerAccessory, Toast.LENGTH_SHORT).show();
                    }
                    mPermissionRequestPending = false;
                }
            }else if(UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)){
                mainActivity.imageViewAccesspoint.setVisibility(View.INVISIBLE);
                UsbAccessory routerAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (routerAccessory != null && routerAccessory.equals(mAccessory)) {
                    closeAccessory();
                }
            }

//            openAccessoryThread = new Thread(new openAccessoryThread());
//            openAccessoryThread.start();
        }
    };

    private void openAccessory(UsbAccessory accessory) {
        mFileDescriptor = usbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            //Toast.makeText(MainActivity.this, "openAccessory ok", Toast.LENGTH_SHORT).show();
//            mainActivity.imageViewAccesspoint.setImageResource(R.mipmap.usb);
//            mainActivity.imageViewAccesspoint.setVisibility(View.VISIBLE);
            CONNECTED=true;
        } else {
            //Toast.makeText(MainActivity.this, "openAccessory fail", Toast.LENGTH_SHORT).show();
//            mainActivity.imageViewAccesspoint.setVisibility(View.INVISIBLE);
            CONNECTED=false;
        }
    }

    private void closeAccessory() {
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }


    @Override
    public void run() {
        byte[] buffer = new byte[1];
        buffer[0]=(byte)0;
//        String txString ="USB TX String";
//        byte[] buffer = txString.getBytes(StandardCharsets.UTF_8); // Java 7+ only

        int ret = 0;
        byte[] usbBuffer = new byte[15];
        int i;

        //while (ret >= 0) {
        try {
            ret = mInputStream.read(buffer);
            mainActivity.testTextView2.setText(String.valueOf(ret));
        } catch (IOException e) {
            //break;
            Log.e(TAG, "read failed", e);
        }
        //}

        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
                mOutputStream.flush();
                //Log.e(TAG, "write OK");
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /******************* USB *********************/
        if (mInputStream != null && mOutputStream != null) {
            return;
        }

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (usbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (usbReceiver) {
                    if (!mPermissionRequestPending) {
                        usbManager.requestPermission(accessory,permissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (mAccessory != null) {
            return mAccessory;
        } else {
            return super.onRetainNonConfigurationInstance();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeAccessory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }
}
