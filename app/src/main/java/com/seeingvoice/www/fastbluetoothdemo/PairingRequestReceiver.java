package com.seeingvoice.www.fastbluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Date:2019/8/5
 * Time:15:11
 * auther:zyy
 */
public class PairingRequestReceiver extends BroadcastReceiver {
    String strPsw = "0000";
    final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
    static BluetoothDevice remoteDevice = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_PAIRING_REQUEST)) {

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {//如果不是已经配对了
                try {
                    ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                    abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                    ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对

                    // ClsUtils.cancelPairingUserInput(device.getClass(),
                    // device); //一般调用不成功，前言里面讲解过了
                    Toast.makeText(context, "配对信息" + device.getName(),Toast.LENGTH_LONG)
                            .show();
                } catch (Exception e) {
                    Toast.makeText(context, "请求连接错误...", Toast.LENGTH_LONG).show();
                }
            }
            // */
            // pair(device.getAddress(),strPsw);

                //1.确认配对
//                try {
//                    ClsUtils.setPairingConfirmation(btDevice.getClass(), btDevice, true);
//                    //2.终止有序广播
//                    Log.i(TAG, "isOrderedBroadcast:"+isOrderedBroadcast()+",isInitialStickyBroadcast:"+isInitialStickyBroadcast());
//                    abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
//                    //3.调用setPin方法进行配对...
////                    boolean ret = ClsUtils.setPin(btDevice.getClass(), btDevice, pin);
//                    ClsUtils.setPin(btDevice.getClass(), btDevice, pin);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
        }
    }

}
