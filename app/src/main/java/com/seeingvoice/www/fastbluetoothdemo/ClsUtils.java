package com.seeingvoice.www.fastbluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Date:2019/8/2
 * Time:16:02
 * auther:zyy
 */
public class ClsUtils {

    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */

    static public boolean createBond(Class btClass, BluetoothDevice btDevice)throws Exception{
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */

    static public boolean removeBond(Class<?> btClass, BluetoothDevice btDevice)throws Exception{
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }



    static public boolean setPin(Class<? extends BluetoothDevice> btClass, BluetoothDevice btDevice,String str) throws Exception {

        try{
            Method removeBondMethod = btClass.getDeclaredMethod("setPin",
                    new Class[]{byte[].class});

            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice,
                    new Object[]{str.getBytes()});
            Log.e("returnValue", "" + returnValue);
        }catch (SecurityException e){
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    //A2DP 与  HeadSet连接
    static public boolean connect(Class btClass, BluetoothProfile proxy, BluetoothDevice btDevice) throws Exception {
        Method connectMethod = btClass.getDeclaredMethod("connect", BluetoothDevice.class);
        connectMethod.setAccessible(true);
        Boolean returnValue = (Boolean) connectMethod.invoke(proxy,btDevice);
        return returnValue.booleanValue();
    }


    //A2DP 与  HeadSet 断开连接
    static public boolean disconnect(Class btClass,BluetoothProfile proxy,BluetoothDevice btDevice) throws Exception {
        Method disconnectMethod = btClass.getDeclaredMethod("disconnect", BluetoothDevice.class);
        disconnectMethod.setAccessible(true);
        Boolean returnValue = (Boolean) disconnectMethod.invoke(proxy,btDevice);
        return returnValue.booleanValue();
    }

    // 取消用户输入
    static public boolean cancelPairingUserInput(Class<?> btClass,BluetoothDevice device)  throws Exception{
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
//        cancelBondProcess(btClass, device);
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }



    // 取消配对
    static public boolean cancelBondProcess(Class<?> btClass, BluetoothDevice device) throws Exception{
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }



    //确认配对
    static public void setPairingConfirmation(Class<?> btClass,BluetoothDevice device,boolean isConfirm)throws Exception {
        Method setPairingConfirmation = btClass.getDeclaredMethod("setPairingConfirmation",boolean.class);
        setPairingConfirmation.invoke(device,isConfirm);
    }

    /**
     *
     * @param clsShow
     */

    static public void printAllInform(Class clsShow){
        try{
            // 取得所有方法
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            for (; i < hideMethod.length; i++){
                Log.e("method name", hideMethod[i].getName() + ";and the i is:"+ i);
            }

            // 取得所有常量
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++){
                Log.e("Field name", allFields[i].getName());
            }
        }catch (SecurityException e){
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

