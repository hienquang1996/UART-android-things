package com.example.quang.myapplication;

import android.app.Activity;
import android.os.Bundle;
import integration.android.IntentIntegrator;
import integration.android.IntentResult;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */

public class MainActivity extends Activity {

    private static final int CHUNK_SIZE = 512;

    private HandlerThread mInputThread;
    private Handler mInputHandler;

    // UART Device Name
    private static final String UART_DEVICE_NAME = "UART0";
    private UartDevice mSPIDevice;



    private Runnable mTransferUartRunnable = new Runnable() {
        @Override
        public void run() {
            recdata();
            mInputHandler.postDelayed(mTransferUartRunnable, 5000);
        }
    };


    /////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInputThread = new HandlerThread("InputThread");
        mInputThread.start();
        mInputHandler = new Handler(mInputThread.getLooper());

        initSPI();
        mInputHandler.post(mTransferUartRunnable);

    }

    /////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            closeSPI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSPI()
    {
        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> deviceList = manager.getUartDeviceList();
        if(deviceList.isEmpty())
        {
            Log.d(TAG,"No SPI bus is not available");
        }
        else
        {
            Log.d(TAG,"SPI bus available: " + deviceList);
            //check if SPI_DEVICE_NAME is in list
            try {
                mSPIDevice = manager.openUartDevice(UART_DEVICE_NAME);

                mSPIDevice.setBaudrate(115200);
                mSPIDevice.setDataSize(8);
                mSPIDevice.setStopBits(1);
                mSPIDevice.setParity(UartDevice.PARITY_NONE);

                mSPIDevice.registerUartDeviceCallback(mInputHandler, mCallback);


                /*mSPIDevice.setMode(SpiDevice.MODE1);
                mSPIDevice.setFrequency(1000000);
                mSPIDevice.setBitsPerWord(8);
                mSPIDevice.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST);*/

                Log.d(TAG,"SPI: OK... ");

            }catch (IOException e)
            {
                Log.d(TAG,"Open SPI bus fail... ");
            }
        }
    }

    private void closeSPI() throws IOException {
        if(mSPIDevice != null)
        {
            try {
                mSPIDevice.close();
            }finally {
                mSPIDevice = null;
            }

        }
    }


    private void recdata(){
        if (mSPIDevice != null){
            try {
                System.out.println("111111111111");
                final int maxCount = CHUNK_SIZE;
                byte[] buffer = new byte[maxCount];

                System.out.println("buffer: " + buffer);
                System.out.println("mSPIDevice: " + mSPIDevice);

                int count;
                System.out.println("2222222222");
                while ((count = mSPIDevice.read(buffer, buffer.length)) > 0) {
                    System.out.println("333333333333");
                    Log.d(TAG, "Read " + count + " bytes from peripheral");
                    System.out.println("Read " + count + " bytes from peripheral");
                }
                System.out.println("count: " + count);
            }catch (IOException e) {
                Log.w(TAG, "Unable to receive data over UART", e);
                System.out.println("Unable to receive data over UART");
            }
        }
    }


    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            // Queue up a data transfer
            recdata();
            //Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };





    public void readUartBuffer(UartDevice uart) throws IOException {
        // Maximum amount of data to read at one time
        final int maxCount = CHUNK_SIZE;
        byte[] buffer = new byte[maxCount];

        int count;
        while ((count = uart.read(buffer, buffer.length)) > 0) {
            Log.d(TAG, "Read " + count + " bytes from peripheral");
        }
    }


}