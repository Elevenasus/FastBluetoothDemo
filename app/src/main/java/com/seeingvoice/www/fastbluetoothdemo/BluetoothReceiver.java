package com.seeingvoice.www.fastbluetoothdemo;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import static android.bluetooth.BluetoothDevice.ACTION_PAIRING_REQUEST;

/**
 *
 *蓝牙广播接收者
 * Date:2019/8/2
 * Time:15:59
 * auther:zyy
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = BluetoothReceiver.class.getName();
    String pin = "0000";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000,我们是0000
    private OnBluetoothListener mOnBluetoothListener;

    public void setOnBluetoothListener(OnBluetoothListener onBluetoothListener){
        this.mOnBluetoothListener = onBluetoothListener;
    }

    public BluetoothReceiver() {//空的构造器
    }

    //广播接收器，当远程蓝牙设备被发现时，回调函数onReceiver()会被执行
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction(); //得到action
        BluetoothDevice btDevice = null;  //创建一个蓝牙device对象
        // 从Intent中获取设备对象
        btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String deviceName = btDevice.getName();
        String deviceAddress = btDevice.getAddress();
        int deviceClassType = btDevice.getBluetoothClass().getDeviceClass();//发现的设备类型

        int deviceState = btDevice.getBondState();

        if (btDevice == null){
            return;
        }

        if (TextUtils.isEmpty(deviceName)){
            return;
        }

        if (btDevice != null && !TextUtils.isEmpty(deviceName)&& deviceName.contains("SV-H1")
                && deviceClassType == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ||
                deviceClassType == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET ||
                deviceClassType == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE){
            switch (action){
                case BluetoothDevice.ACTION_FOUND:
                    if (deviceState == BluetoothDevice.BOND_NONE){//没有配对先进行配对
                        Log.e("MainActivity", "onReceive: BluetoothDevice.BOND_NONE"+"发现了SV-H1");
                        mOnBluetoothListener.deviceFound(btDevice);
                    }else if (deviceState == BluetoothDevice.BOND_BONDED){//如果已经配对了，则直接连接
                        mOnBluetoothListener.deviceBonded(btDevice);
                    }
                    break;
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    switch (intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1)) {
                        case BluetoothA2dp.STATE_CONNECTING:
                            break;
                        case BluetoothA2dp.STATE_CONNECTED:
                            Log.e(TAG, "onReceive: A2DP连接");
                            mOnBluetoothListener.deviceA2dpConned(btDevice);
                            break;
                        case BluetoothA2dp.STATE_DISCONNECTED:
//                            mOnBluetoothListener.deviceDisConnected(device);
                            break;
                        default:
                            break;
                    }
                    break;
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    switch (intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1)) {
                        case BluetoothHeadset.STATE_CONNECTING:
                            break;
                        case BluetoothHeadset.STATE_CONNECTED:
                            Log.e(TAG, "onReceive: HEADSET连接");
                            mOnBluetoothListener.deviceHeadsetConned(btDevice);
                            break;
                        case BluetoothHeadset.STATE_DISCONNECTED:
//                            mOnBluetoothListener.deviceDisConnected(device);
                            break;
                        default:
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
//                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (bondState) {
                        case BluetoothDevice.BOND_BONDED:
                            //配对成功
                            Log.e("MainActivity", "BluetoothDevice.BOND_BONDED"+"SV-H1开始成功");
                            mOnBluetoothListener.deviceBonded(btDevice);
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            //正在配对中
                            break;
                        case BluetoothDevice.BOND_NONE:
                            //配对不成功的话，重新尝试配对
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
    }


}
