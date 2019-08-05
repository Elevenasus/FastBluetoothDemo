package com.seeingvoice.www.fastbluetoothdemo;

import android.bluetooth.BluetoothDevice;

/**
 * Date:2018/12/19
 * Time:15:01
 * auther:zyy  监听蓝牙广播接收者
 */
public interface OnBluetoothListener {
//    void bluetoothStateOn();
//    void bluetoothStateOff();
    void deviceFound(BluetoothDevice bluetoothDevice);
//    void deviceBonding(BluetoothDevice bluetoothDevice);
    void deviceBonded(BluetoothDevice bluetoothDevice);
    void deviceA2dpConned(BluetoothDevice bluetoothDevice);
    void deviceHeadsetConned(BluetoothDevice bluetoothDevice);
//    void deviceBondNone(BluetoothDevice bluetoothDevice);
//    void deviceConnected(BluetoothDevice bluetoothDevice);
//    void deviceDisConnected(BluetoothDevice bluetoothDevice);
//    void noDeviceDiscovered();
}
