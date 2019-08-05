package com.seeingvoice.www.fastbluetoothdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnBluetoothListener {

    private static final String TAG = MainActivity.class.getName();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothReceiver mBluetothReceiver;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 230;
    private static final int MY_PERMISSIONS_REQUEST_ENABLE_BLUETOOTH = 231;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private boolean mScanning = false;//蓝牙状态
    private Handler mHandler = new Handler();//线程通信
    private TextView mTvConnedDevice;
    private BluetoothA2dp mBluetoothA2dp;
    private BluetoothHeadset mBluetoothHeadset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvConnedDevice = findViewById(R.id.tv_tips);
        requestPermission();
        //通过获取系统服务得到蓝牙管理者对象
        initBluetoothAdapter();//初始化蓝牙适配器
        isApdaterConned();//判断蓝牙适配器连接的设备
        mBluetothReceiver = new BluetoothReceiver();
        mBluetothReceiver.setOnBluetoothListener(this);
        // 注册Receiver来获取蓝牙设备相关的结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intent.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mBluetothReceiver, intent);
    }

    //判断蓝牙适配器是否连接了 SV-H1耳机
    private void isApdaterConned() {
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        int state = -1;//连接状态
        //得到连接状态的方法
        Method method = null;
        try {
            method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            state = (int) method.invoke(mBluetoothAdapter, (Object[]) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        //蓝牙已经连接上设备,不一定是耳机
        if (state == BluetoothAdapter.STATE_CONNECTED) {
            //获取已经连接的蓝牙设备,有的手机可以同时连接多个设备,有的手机只能连接一个
            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
            boolean isConnected = false;
            for (BluetoothDevice device : devices) {
                Method isConnectedMethod = null;
                try {
                    isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    //获取连接的状态
                    isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                if (isConnected) {
                    if (device != null) {
                        //获取已经连接的设备信息
                        Log.e(TAG, "getConnectInfo" + device.getAddress() + "  " + device.getName());
                        if (device.getName().contains("SV-H1")) {
                            mTvConnedDevice.setText("当前连接的设备：" + device.getName() + ";地址：" + device.getAddress());
                            mDeviceList.add(device);//目标蓝牙耳机添加到设备列表
                        } else {
                            //如果是非见声耳机的逻辑
                        }
                    }
                }
            }
            if (mDeviceList.size() > 0 && mDeviceList != null) {
            } else {
                //去蓝牙设置页面取消掉非见声耳机的 取消配对
                Toast.makeText(this, "请到设置里，取消非蓝牙耳机的配对", Toast.LENGTH_LONG);
            }
        }else {//蓝牙是打开状态,但是未连接任何设备
            //开启搜索,搜索附近的蓝牙设备
            startScanBluth();
        }
    }

    /**
     * 扫描附近的蓝牙耳机
     */
    private void startScanBluth() {
        Log.e(TAG, "startScanBluth: 开始扫描蓝牙设备");
        getBluetoothHeadset(this);
        getBluetoothA2DP(this);
        // 判断是否在搜索,如果在搜索，就取消搜索
        if (mScanning) {
            mBluetoothAdapter.cancelDiscovery();
            mScanning = false;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.cancelDiscovery();
            }
        }, 30000);
        //手机蓝牙扫描30秒结束
        mScanning = true;
//         开始搜索,搜索结果通过广播返回
        mBluetoothAdapter.startDiscovery();
    }

    //初始化蓝牙适配器
    private void initBluetoothAdapter() {
        mBluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        assert mBluetoothManager != null;
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        openBluetoothAdapter();
    }
    //打开蓝牙适配器
    private void openBluetoothAdapter() {
        /* 打开手机蓝牙*/
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {//打开手机蓝牙适配器
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MY_PERMISSIONS_REQUEST_ENABLE_BLUETOOTH);
        }
    }

    /**
     * 协议连接时，优先级设置100，断开时设置为0
     * setPriority(bluetoothDevice,100);
     * setPriority(bluetoothDevice,0);
    * */
    public void setPriority(BluetoothDevice device, int priority) {
        if (mBluetoothA2dp == null || mBluetoothHeadset == null) return;
        //通过反射获取BluetoothA2dp中setPriority方法（hide的），设置优先级
        try {
            Method connectA2dpMethod = BluetoothA2dp.class.getMethod("setPriority",
                    BluetoothDevice.class,int.class);
            connectA2dpMethod.invoke(mBluetoothA2dp, device, priority);

            Method connectHeadsetMethod = BluetoothA2dp.class.getMethod("setPriority",
                    BluetoothDevice.class,int.class);
            connectHeadsetMethod.invoke(mBluetoothHeadset, device, priority);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 连接蓝牙耳机A2DP*/
    public void getBluetoothA2DP(final Context context){
        if (mBluetoothAdapter == null){
            return;
        }

        mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP){
                    mBluetoothA2dp = (BluetoothA2dp)proxy;//获得A2DP，可以通过反射得到连接的method
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.A2DP){
                    mBluetoothA2dp = null;
                }
            }
        }, BluetoothProfile.A2DP);
    }

    /** 连接蓝牙耳机headset协议*/
    public void getBluetoothHeadset(final Context context) {
        if (mBluetoothAdapter == null){
            return;
        }
        mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    mBluetoothHeadset = (BluetoothHeadset) proxy;
                }
            }

            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    mBluetoothHeadset = null;
                }
            }
        }, BluetoothProfile.HEADSET);
    }


    /**
     * 动态申请蓝牙必要权限
     */
    private void requestPermission() {
        Log.i(TAG,"requestPermission");
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG,"checkSelfPermission");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.i(TAG,"shouldShowRequestPermissionRationale");
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
            } else {
                Log.i(TAG,"requestPermissions");
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG,"onRequestPermissionsResult granted");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Log.i(TAG,"onRequestPermissionsResult denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showWaringDialog();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // User chose not to enable Bluetooth.
        if (requestCode == MY_PERMISSIONS_REQUEST_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_CANCELED) {
            finish();
            Log.e(TAG,"不开启蓝牙适配器，该APP无法使用");
            return;
        }
    }

    /**
     * 没有蓝牙粗略定位权限，则提示没有该权限程序不能运行
     */
    private void showWaringDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("警告！")
                .setMessage("请前往设置->应用->PermissionDemo->权限中打开相关权限，否则功能无法正常运行！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 一般情况下如果用户不授权的话，功能是无法运行的，做退出处理
                        finish();
                    }
                }).show();
    }

    //广播监听接口回调


    @Override
    public void deviceFound(BluetoothDevice bluetoothDevice) {
        //通过工具类ClsUtils,调用createBond方法，发现了见声耳机，进行配对
        try {
            Log.e("MainActivity", "MainActivity deviceFound(BluetoothDevice bluetoothDevice)"+"发现了SV-H1开始配对");
            ClsUtils.createBond(bluetoothDevice.getClass(), bluetoothDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deviceBonded(BluetoothDevice bluetoothDevice) {
        if (null != bluetoothDevice){
            //通过工具类ClsUtils,调用createBond方法，发现了见声耳机，进行配对
            try {
                Log.e("MainActivity", "BluetoothDevice.BOND_BONDED"+"SV-H1开始连接");
                setPriority(bluetoothDevice,100);
                ClsUtils.connect(bluetoothDevice.getClass(), mBluetoothA2dp,bluetoothDevice);
                ClsUtils.connect(bluetoothDevice.getClass(), mBluetoothHeadset,bluetoothDevice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deviceA2dpConned(BluetoothDevice bluetoothDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvConnedDevice.setText("A2dp连接");
            }
        });
    }

    @Override
    public void deviceHeadsetConned(BluetoothDevice bluetoothDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvConnedDevice.setText("Headset连接");
            }
        });
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mBluetothReceiver);
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
