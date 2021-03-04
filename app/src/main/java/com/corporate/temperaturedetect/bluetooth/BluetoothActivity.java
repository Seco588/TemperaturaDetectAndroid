package com.corporate.temperaturedetect.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.corporate.temperaturedetect.MainActivity;
import com.corporate.temperaturedetect.R;
import com.corporate.temperaturedetect.detect.DetectManager;
import com.corporate.temperaturedetect.manager.Opencv;
import com.corporate.temperaturedetect.manager.SystemInfo;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.FpsMeter;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class BluetoothActivity extends AppCompatActivity implements JavaCameraView.CvCameraViewListener2 {

    private final String TAG = "BluetoothActivity";

    private EditText EditTextBtCommTest;
    private TextView TextViewBtCheck, TextViewBtDisplay;
    private ImageView BtImage;
    private ScrollView scrollView;
    private Button ButtonBtOn, ButtonBtOff, ButtonBtDiscovery, ButtonBtConnect, ButtonBtConnectLastDevice, ButtonBtDisconnect, ButtonTestBt, ButtonBtNext, ButtonBtReturn;
    private ArrayList<BluetoothDevice> arrayListDeviceDiscoveryFound;
    private ArrayAdapter<BluetoothDevice> arrayAdapterDeviceDiscoveryFound;
    private ListView listViewBt;
    private SharedPreferences sp;
    private BluetoothManager bluetoothManager;
    private CascadeClassifier faceDetector;
    private MatOfRect faceDetections;
    private TextView viewTemperature, viewInfo, viewDebug, viewDirections;
    private ProgressBar progressBar;
    private SystemInfo systemInfo;
    private Mat mGray, mRgba;
    private FileWriter fileWriterLogDebug, fileWriterLogTemp;
    private Scalar color_for_loop;
    private Float[] list_temperature;
    private String NoFaceDetect, RoiWidth, RoiHeight, RoiOffsetWidth, RoiOffsetHeight, ThicknessRoi, ChecksToBeCarriedOut,
            textViewMain, textViewDebug, textViewDirection, textViewFinalTemperature, TemperatureSentenceDetect, BluetoothCommand,
            UnitTemperature, Progress, NoBluetoothFound, Complete, TemperatureBelowLimit, TemperatureMaxExceeded, temperatureStringComplete,
            TempMin, ThresholdTemp, TempMax, TemperatureOk, Threshold, ErrorTemperature, DetectionCompleted, OvalFaceOffsetX, OvalFaceOffsetY;
    private boolean checkBoxDebugChecked, checkRadioButton, freeze_loop, checkBoxLogChecked, connectionState;
    private int checks_carried_out = 0;
    private int iThicknessRoi, iChecksToBeCarriedOut, colorDetectForLoop;
    private float temperatureConvert, fRoiWidth, fRoiHeight, fRoiOffsetWidth, fRoiOffsetHeight,
            fTempMax, fTempMin, fThresholdTemp, fOvalFaceOffsetX, fOvalFaceOffsetY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_bluetooth);

        //Share preference
        sp = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        //get user preference
        RoiWidth = sp.getString("RoiWidth", "null");
        RoiHeight = sp.getString("RoiHeight", "null");
        RoiOffsetWidth = sp.getString("RoiOffsetWidth", "null");
        RoiOffsetHeight = sp.getString("RoiOffsetHeight", "null");
        TempMax = sp.getString("TempMax", "null");
        TempMin = sp.getString("TempMin", "null");
        ThresholdTemp = sp.getString("ThresholdTemp", "null");
        ThicknessRoi = sp.getString("ThicknessRoi", "null");
        OvalFaceOffsetY = sp.getString("OvalFaceOffsetY", "null");
        OvalFaceOffsetX = sp.getString("OvalFaceOffsetX", "null");
        ChecksToBeCarriedOut = sp.getString("ChecksToBeCarriedOut", "null");
        BluetoothCommand = sp.getString("BluetoothCommand", "null");
        TemperatureOk = sp.getString("TemperatureOk", "null");//inside range
        NoFaceDetect = sp.getString("NoFaceDetect", "null");
        TemperatureMaxExceeded = sp.getString("TemperatureMaxExceeded", "null");
        TemperatureBelowLimit = sp.getString("TemperatureBelowLimit", "null");
        ErrorTemperature = sp.getString("ErrorTemperature", "null");
        UnitTemperature = sp.getString("UnitTemperature", "null");
        Threshold = sp.getString("Threshold", "null");
        TemperatureSentenceDetect = sp.getString("TemperatureSentenceDetect", "null");
        Progress = sp.getString("Progress", "null");
        DetectionCompleted = sp.getString("DetectionCompleted", "null");
        NoBluetoothFound = sp.getString("NoBluetoothFound", "null");
        Complete = sp.getString("Complete", "null");
        checkBoxDebugChecked = sp.getBoolean("CheckBoxDebugChecked", false);//view debug
        checkBoxLogChecked = sp.getBoolean("CheckBoxLogChecked", false); //save log
        checkRadioButton = sp.getBoolean("CheckRadioButton", true);// front camera

        //get object graphic view activity_bluetooth
        //Image
        BtImage = findViewById(R.id.imageBt);
        //Button
        ButtonBtOn = findViewById(R.id.buttonBtOn);
        ButtonBtOff = findViewById(R.id.buttonBtOff);
        ButtonBtDiscovery = findViewById(R.id.buttonBtDiscovery);
        ButtonBtConnect = findViewById(R.id.buttonBtConnect);
        ButtonBtConnectLastDevice = findViewById(R.id.buttonBtConnectLastDevice);
        ButtonBtDisconnect = findViewById(R.id.buttonBtDisconnect);
        ButtonTestBt = findViewById(R.id.buttonTestBt);
        ButtonBtReturn = findViewById(R.id.buttonBtReturn);
        ButtonBtNext = findViewById(R.id.buttonBtNext);
        TextViewBtCheck = findViewById(R.id.textViewBluetoothCheck); //bar between next and return -TextView
        TextViewBtDisplay = findViewById(R.id.textViewDisplayBluetooth); // ui speak -TextView
        EditTextBtCommTest = findViewById(R.id.editTextTextBluetoothTestCommand);// command to send wend is connected -EditText
        listViewBt = findViewById(R.id.listViewDevice); //ListView // List ui graphic device found
        arrayListDeviceDiscoveryFound = new ArrayList<>(); //List device found
        scrollView = findViewById(R.id.scrollView2); //Scollview

        if (checkBoxDebugChecked) {
            //Log File Debug
            String nameFileDebug = "Debug_rBluetooth";
            String extensionDebug = ".txt";
            Log.i(TAG, "Create log file -> " + nameFileDebug + extensionDebug);
            //Create dir log file Debug
            String dirFileLogDebug = getExternalFilesDir(null) + "/" + nameFileDebug + extensionDebug;
            File fileLogDebug = new File(dirFileLogDebug);
            //Debug
            try {
                fileWriterLogDebug = new FileWriter(fileLogDebug);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (checkBoxLogChecked) {
            //Log File Temp
            String nameFileDebugTemp = "Debug_Temperature";
            String extensionDebugTemp = ".txt";
            Log.i(TAG, "Create log file -> " + nameFileDebugTemp + extensionDebugTemp);
            //Create dir log file Temperature
            String dirFileLogTemp = getExternalFilesDir(null) + "/" + nameFileDebugTemp + extensionDebugTemp;
            File fileLogTemp = new File(dirFileLogTemp);
            //Temp
            try {
                if (fileLogTemp.exists()) {
                    FileReader fileReaderLogTemp = new FileReader(fileLogTemp);
                    BufferedReader bufferedReader = new BufferedReader(fileReaderLogTemp);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        stringBuilder.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
                    String response = stringBuilder.toString();
                    fileReaderLogTemp.close();
                    fileWriterLogTemp = new FileWriter(fileLogTemp);
                    fileWriterLogTemp.write(response);
                } else {
                    fileWriterLogTemp = new FileWriter(fileLogTemp);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (BluetoothAdapter.getDefaultAdapter() == null) {
            //If adapter is null disable all and set text
            TextViewBtCheck.setText(getString(R.string.sBtNotSupported));
            TextViewBtDisplay.append(getString(R.string.sBtNotSupported));
            ButtonBtOn.setEnabled(false);
            ButtonBtOff.setEnabled(false);
            ButtonBtDiscovery.setEnabled(false);
            ButtonBtConnect.setEnabled(false);
            ButtonBtConnectLastDevice.setEnabled(false);
            ButtonBtDisconnect.setEnabled(false);
            ButtonTestBt.setEnabled(false);
            ButtonBtReturn.setEnabled(true);
            ButtonBtNext.setEnabled(true);
            EditTextBtCommTest.setEnabled(false);

        } else {

            //instructions
            TextViewBtDisplay.append(getString(R.string.sInstructions) + "\n\n");
            bluetoothManager = new BluetoothManager(BluetoothAdapter.getDefaultAdapter());

            if (bluetoothManager.getAdapter().isEnabled()) {
                ButtonBtOn.setEnabled(false);
                ButtonBtOff.setEnabled(true);
                ButtonBtConnect.setEnabled(false);
                ButtonBtConnectLastDevice.setEnabled(true);
                ButtonBtDisconnect.setEnabled(false);
                ButtonTestBt.setEnabled(false);
                ButtonBtReturn.setEnabled(true);
                ButtonBtNext.setEnabled(true);
                EditTextBtCommTest.setEnabled(false);
                ButtonBtDisconnect.setEnabled(false);
                ButtonBtDiscovery.setEnabled(true);
                BtImage.setImageResource(R.drawable.ic_bt_action_on);
            } else {
                ButtonBtOn.setEnabled(true);
                ButtonBtOff.setEnabled(false);
                ButtonBtDiscovery.setEnabled(false);
                ButtonBtConnect.setEnabled(false);
                ButtonBtConnectLastDevice.setEnabled(false);
                ButtonBtDisconnect.setEnabled(false);
                ButtonTestBt.setEnabled(false);
                ButtonBtReturn.setEnabled(true);
                ButtonBtNext.setEnabled(true);
                EditTextBtCommTest.setEnabled(false);
                ButtonBtDisconnect.setEnabled(false);
                BtImage.setImageResource(R.drawable.ic_bt_action_off);
            }
        }
        load();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        if (connectionState) {
            ButtonBtDisconnect.performClick();
        }
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    private void load() {

        //On bt click
        ButtonBtOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothManager.getAdapter().isEnabled()) {
                    Log.d(TAG, "mBroadcastReceiverEnable ON");

                    IntentFilter intentFilterEnable = new IntentFilter();
                    intentFilterEnable.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiverEnable, intentFilterEnable);

                    if (bluetoothManager.getAdapter().enable()) {
                        //Work in broadcast mBroadcastReceiverEnable
                        TextViewBtDisplay.append(getString(R.string.sBtTurnOn) + "\n\n");
                        scrollView.scrollTo(0, scrollView.getBottom());
                    }
                } else {
                    TextViewBtDisplay.append(getString(R.string.sBtAlreadyOn) + "\n\n");
                    scrollView.scrollTo(0, scrollView.getBottom());
                }
            }
        });

        //Off Bt click
        ButtonBtOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothManager.getAdapter().isEnabled()) {
                    Log.d(TAG, "mBroadcastReceiverEnable ON");

                    IntentFilter intentFilterEnable = new IntentFilter();
                    intentFilterEnable.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiverEnable, intentFilterEnable);

                    assert bluetoothManager != null;
                    if (bluetoothManager.getAdapter().disable()) {
                        //Work in broadcast mBroadcastReceiverEnable
                        TextViewBtDisplay.append(getString(R.string.sBtTurnOff) + "\n\n");
                        scrollView.scrollTo(0, scrollView.getBottom());
                    }
                } else {
                    TextViewBtDisplay.append(getString(R.string.sBtAlreadyOff) + "\n\n");
                    scrollView.scrollTo(0, scrollView.getBottom());
                }
            }
        });

        //Discovery Bt click
        ButtonBtDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothManager.getAdapter().isDiscovering()) {
                    TextViewBtDisplay.append(getString(R.string.sBtAlreadyDiscovery) + "\n\n");
                } else {
                    Log.d(TAG, "mBroadcastReceiverDiscovery ON");
                    IntentFilter intentFilterDiscovery = new IntentFilter();
                    intentFilterDiscovery.addAction(BluetoothDevice.ACTION_FOUND);
                    intentFilterDiscovery.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    intentFilterDiscovery.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    intentFilterDiscovery.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                    registerReceiver(mBroadcastReceiverDiscovery, intentFilterDiscovery);

                    if (bluetoothManager.getAdapter().startDiscovery()) {
                        //Work in broadcast mBroadcastReceiverDiscovery
                        arrayAdapterDeviceDiscoveryFound = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayListDeviceDiscoveryFound);
                        listViewBt.setAdapter(arrayAdapterDeviceDiscoveryFound);
                        scrollView.scrollTo(0, scrollView.getBottom());
                    }


                }
            }
        });

        listViewBt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Callback method to be invoked when an item in this AdapterView has
             * been clicked.
             * <p>
             * Implementers can call getItemAtPosition(position) if they need
             * to access the data associated with the selected item.
             *
             * @param parent   The AdapterView where the click happened.
             * @param view     The view within the AdapterView that was clicked (this
             *                 will be a view provided by the adapter)
             * @param position The position of the view in the adapter.
             * @param id       The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                bluetoothManager.setRemoteDevice(arrayListDeviceDiscoveryFound.get(position));
                TextViewBtCheck.setText(bluetoothManager.getRemoteDevice().getName());
                TextViewBtDisplay.append(getString(R.string.sBtSelectDevice)
                        + "\n" + getString(R.string.sBtName) + bluetoothManager.getRemoteDevice().getName()
                        + "\n" + getString(R.string.sBtMacAddress) + bluetoothManager.getRemoteDevice().getAddress()
                        + "\n" + getString(R.string.sBtBondState) + bluetoothManager.getRemoteDevice().getBondState()
                        + "\n\n");
                scrollView.scrollTo(0, scrollView.getBottom());

                //when click on device
                ButtonBtConnect.setEnabled(true);
            }
        });

        //Connect Bt click
        ButtonBtConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Before connecting the device, check the binding status
                if (bluetoothManager.createBondState()) {
                    //State bonding is BONDED
                    Log.d(TAG, " mBroadcastReceiverConnect ON");
                    IntentFilter intentFilterConnection = new IntentFilter();
                    // intentFilterConnection.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
                    intentFilterConnection.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                    intentFilterConnection.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                    intentFilterConnection.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                    registerReceiver(mBroadcastReceiverConnect, intentFilterConnection);

                    //Now i can try create socket and connect it
                    if (bluetoothManager.createSocket()) {
                        //socket is already
                        Log.d(TAG, " The socket is created successfully");
                        if (bluetoothManager.connectSocket()) {
                            //socket is connected
                            Log.d(TAG, " The socket is connected");
                            if (bluetoothManager.connectedStream()) {
                                //stream is connect
                                Log.d(TAG, "The stream are connected");
                                TextViewBtDisplay.append(getString(R.string.sBtConnected) + bluetoothManager.getRemoteDevice().getName() + "\n\n");
                                scrollView.scrollTo(0, scrollView.getBottom());
                                //Work in mBroadcastReceiverConnect

                            } else {
                                TextViewBtDisplay.append(getString(R.string.sBtErrorCreateConnectionStream) + "\n\n");
                                scrollView.scrollTo(0, scrollView.getBottom());
                                unregisterReceiver(mBroadcastReceiverConnect);
                            }
                        } else {
                            TextViewBtDisplay.append(getString(R.string.sBtErrorCreateConnectionSocket) + "\n\n");
                            scrollView.scrollTo(0, scrollView.getBottom());
                            unregisterReceiver(mBroadcastReceiverConnect);
                        }
                    } else {
                        TextViewBtDisplay.append(getString(R.string.sBtErrorSocket) + "\n\n");
                        scrollView.scrollTo(0, scrollView.getBottom());
                        unregisterReceiver(mBroadcastReceiverConnect);
                    }

                } else {
                    //Manage state bonded error
                    TextViewBtDisplay.append(getString(R.string.sBtErrorPaired) + bluetoothManager.getRemoteDevice().getName() + "\n\n");
                    scrollView.scrollTo(0, scrollView.getBottom());
                }
            }
        });

        //Last Connect Bt click
        ButtonBtConnectLastDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastConnectionMacAddress = sp.getString("LastConnectionMacAddress", "+");
                Log.d(TAG, "Last device : " + lastConnectionMacAddress);
                assert lastConnectionMacAddress != null;
                if (lastConnectionMacAddress.equals("+")) {
                    TextViewBtDisplay.append(getString(R.string.sBtNoLastConnect) + "\n" + getString(R.string.sBtErrorConnectLastDevie) + "\n\n");
                    scrollView.scrollTo(0, scrollView.getBottom());
                    ButtonBtDiscovery.performClick();
                } else {
                    for (BluetoothDevice device : bluetoothManager.getAdapter().getBondedDevices()) {
                        if (device.getAddress().equals(lastConnectionMacAddress)) {
                            bluetoothManager.setRemoteDevice(device);
                            ButtonBtConnect.performClick();
                        }
                    }
                }
            }
        });

        //Disconnect Bt click
        ButtonBtDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothManager.disconnectSteam()) {
                    //stream is disconnected
                    Log.d(TAG, "The stream is disconnected");
                    if (bluetoothManager.disconnectSocket()) {
                        //socket is disconnected
                        Log.d(TAG, "The socket is disconnected");
                        TextViewBtDisplay.append(getString(R.string.sBtDisconnect) + "\n\n");
                        scrollView.scrollTo(0, scrollView.getBottom());
                    } else {
                        TextViewBtDisplay.append(getString(R.string.sBtErrorDisconnectSocket));
                        scrollView.scrollTo(0, scrollView.getBottom());
                    }
                } else {
                    TextViewBtDisplay.append(getString(R.string.sBtErrorDisconnectStream));
                    scrollView.scrollTo(0, scrollView.getBottom());
                }
            }
        });

        //Test Bt click
        ButtonTestBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send command write in to edit ext to bluetooth devices and output response only if is connect
                String command = EditTextBtCommTest.getText().toString();
                if (command.equals("")) {
                    TextViewBtDisplay.append(getString(R.string.sBtErrorResp) + "\n\n");
                    scrollView.scrollTo(0, scrollView.getBottom());
                } else {
                    String resp = " - ";
                    if (bluetoothManager.sendData(command)) {
                        resp = bluetoothManager.receveData();
                    }
                    TextViewBtDisplay.append(getString(R.string.sBtResponse) + resp + "\n\n");
                    scrollView.scrollTo(0, scrollView.getBottom());
                }

            }
        });

        ButtonBtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionState) {
                    Log.d(TAG, " unregisterReceiver(mBroadcastReceiverConnect)");
                    unregisterReceiver(mBroadcastReceiverConnect);
                }
                //Jump to detector
                jumpToDetector();
            }
        });

        ButtonBtReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionState) {
                    ButtonBtDisconnect.performClick();
                }
                //Go to Main activity
                Intent myIntent = new Intent(BluetoothActivity.this, MainActivity.class);
                BluetoothActivity.this.startActivity(myIntent);
            }
        });

    }

    private final BroadcastReceiver mBroadcastReceiverDiscovery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if (!arrayListDeviceDiscoveryFound.contains(device)) {
                    arrayListDeviceDiscoveryFound.add(device);
                    arrayAdapterDeviceDiscoveryFound.notifyDataSetChanged();
                    TextViewBtDisplay.append(getString(R.string.sBtFoundNewDevice) + "\n" + getString(R.string.sBtName) + device.getName() + "\n" + getString(R.string.sBtMacAddress) + device.getAddress() + "\n\n");
                    scrollView.scrollTo(0, scrollView.getBottom());
                }
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                //When call request discovery set image and set visibility to listViewBt for select device
                BtImage.setImageResource(R.drawable.ic_bt_search);
                ButtonBtOn.setEnabled(false);
                ButtonBtOff.setEnabled(false);
                ButtonBtDiscovery.setEnabled(false);
                ButtonBtConnect.setEnabled(false);
                ButtonBtConnectLastDevice.setEnabled(false);
                ButtonBtDisconnect.setEnabled(false);
                ButtonTestBt.setEnabled(false);
                ButtonBtReturn.setEnabled(false);
                ButtonBtNext.setEnabled(false);
                EditTextBtCommTest.setEnabled(false);
                ButtonBtDisconnect.setEnabled(false);
                ButtonBtDiscovery.setEnabled(false);
                listViewBt.setEnabled(false);
                TextViewBtCheck.setText(getString(R.string.sBtDiscoveryStart));
                TextViewBtDisplay.append(getString(R.string.sBtDiscoveryStart) + "\n\n");
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                //Stop thread discovery when finish the time of search
                bluetoothManager.getAdapter().cancelDiscovery();
                BtImage.setImageResource(R.drawable.ic_bt_setting);
                ButtonBtOn.setEnabled(false);
                ButtonBtOff.setEnabled(true);
                ButtonBtDiscovery.setEnabled(true);
                ButtonBtConnect.setEnabled(false);
                ButtonBtConnectLastDevice.setEnabled(true);
                ButtonBtDisconnect.setEnabled(false);
                ButtonTestBt.setEnabled(false);
                ButtonBtReturn.setEnabled(true);
                ButtonBtNext.setEnabled(true);
                EditTextBtCommTest.setEnabled(false);
                ButtonBtDisconnect.setEnabled(false);
                ButtonBtDiscovery.setEnabled(true);
                listViewBt.setEnabled(true);
                TextViewBtDisplay.append(getString(R.string.sBtDiscoveryFinished) + "\n\n");
                TextViewBtCheck.setText(getString((R.string.sBtDiscoveryFinished)));
                Log.d(TAG, "mBroadcastReceiverDiscovery OFF");
                unregisterReceiver(mBroadcastReceiverDiscovery);
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiverConnect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;

            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "BluetoothAdapter.ACTION_ACL_CONNECTED");
                    BtImage.setImageResource(R.drawable.ic_bt_connect);
                    ButtonBtConnect.setEnabled(false);
                    ButtonBtConnectLastDevice.setEnabled(false);
                    ButtonTestBt.setEnabled(true);
                    ButtonBtDisconnect.setEnabled(true);
                    EditTextBtCommTest.setEnabled(true);
                    listViewBt.setEnabled(false);
                    ButtonBtOff.setEnabled(false);
                    ButtonBtDiscovery.setEnabled(false);
                    //Save device
                    sp.edit().putString("LastConnectionMacAddress", bluetoothManager.getRemoteDevice().getAddress()).apply();
                    //set Connection state true
                    connectionState = true;
                    TextViewBtDisplay.append(getString((R.string.sBtOkConnection)) + "\n\n");
                    TextViewBtCheck.setText(getString((R.string.sBtOkConnection)));
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.d(TAG, "BluetoothAdapter.ACTION_ACL_DISCONNECT_REQUESTED");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "BluetoothAdapter.ACTION_ACL_DISCONNECTED");
                    BtImage.setImageResource(R.drawable.ic_bt_setting);
                    ButtonBtConnect.setEnabled(false);
                    ButtonBtConnectLastDevice.setEnabled(true);
                    ButtonTestBt.setEnabled(false);
                    ButtonBtDisconnect.setEnabled(false);
                    EditTextBtCommTest.setEnabled(false);
                    listViewBt.setEnabled(true);
                    ButtonBtOff.setEnabled(true);
                    ButtonBtDiscovery.setEnabled(true);
                    //set Connection state true
                    connectionState = false;
                    TextViewBtDisplay.append(getString((R.string.sBtKoConnection)) + "\n\n");
                    TextViewBtCheck.setText(getString((R.string.sBtKoConnection)));
                    break;
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiverEnable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (extra) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, " mBroadcastReceiverEnable : STATE_OFF.");
                        BtImage.setImageResource(R.drawable.ic_bt_action_off);
                        ButtonBtOn.setEnabled(true);
                        ButtonBtOff.setEnabled(false);
                        ButtonBtConnect.setEnabled(false);
                        ButtonBtConnectLastDevice.setEnabled(false);
                        ButtonBtDisconnect.setEnabled(false);
                        ButtonTestBt.setEnabled(false);
                        ButtonBtDiscovery.setEnabled(false);
                        ButtonBtReturn.setEnabled(true);
                        ButtonBtNext.setEnabled(true);
                        EditTextBtCommTest.setEnabled(false);
                        ButtonBtDisconnect.setEnabled(false);
                        Log.d(TAG, "BT : mBroadcastReceiverEnable OFF");
                        unregisterReceiver(mBroadcastReceiverEnable);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiverEnable : STATE_TURNING_OFF.");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiverEnable : STATE_ON.");
                        BtImage.setImageResource(R.drawable.ic_bt_action_on);
                        Log.d(TAG, "mBroadcastReceiverEnable OFF");
                        unregisterReceiver(mBroadcastReceiverEnable);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiverEnable : STATE_TURNING_ON.");
                        BtImage.setImageResource(R.drawable.ic_bt_action_on);
                        ButtonBtOn.setEnabled(false);
                        ButtonBtOff.setEnabled(true);
                        ButtonBtDiscovery.setEnabled(true);
                        ButtonBtConnect.setEnabled(false);
                        ButtonBtConnectLastDevice.setEnabled(true);
                        ButtonBtDisconnect.setEnabled(false);
                        ButtonTestBt.setEnabled(false);
                        ButtonBtReturn.setEnabled(true);
                        ButtonBtNext.setEnabled(true);
                        EditTextBtCommTest.setEnabled(false);
                        ButtonBtDisconnect.setEnabled(false);
                        break;
                }
            }
        }
    };


    private void jumpToDetector() {
        Log.d(TAG, "jumpToDetector");

        if (connectionState) {
            IntentFilter intentFilterConnectionView = new IntentFilter();
            intentFilterConnectionView.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intentFilterConnectionView.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilterConnectionView.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            registerReceiver(mBroadcastReceiverConnectView, intentFilterConnectionView);
        }

        //load view detect
        setContentView(R.layout.activity_detect);

        //get object view
        viewDirections = findViewById(R.id.textViewDirection);
        viewDebug = findViewById(R.id.textViewDebug);
        viewTemperature = findViewById(R.id.textViewTemperature);
        viewInfo = findViewById(R.id.textViewInfo);
        progressBar = findViewById(R.id.progressBar);
        //after check get javaCamera2View

        //load opencv
        Opencv opencv = new Opencv(this);

        //if opencv is load
        if (opencv.isStatusEnableLibrary()) {

            //get camera
            JavaCamera2View javaCamera2View = findViewById(R.id.javaCamera2View);
            //Set listener
            javaCamera2View.setCvCameraViewListener(this);
            //set camera
            //true = front - false = retro
            int camera;
            if (checkRadioButton) {
                camera = CameraBridgeViewBase.CAMERA_ID_FRONT;
            } else {
                camera = CameraBridgeViewBase.CAMERA_ID_BACK;
            }
            javaCamera2View.setCameraIndex(camera);
            javaCamera2View.enableView();//Enable view
            javaCamera2View.enableFpsMeter();//Show fps

            //get classifier
            faceDetector = opencv.getFaceDetector();//Classifier
            faceDetections = new MatOfRect();//Container faces detect

        } else {
            Toast.makeText(this, "Error to load openCV library contact developer", Toast.LENGTH_LONG).show();
        }
    }

    private final BroadcastReceiver mBroadcastReceiverConnectView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;

            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "ACTION_ACL_CONNECTED");
                    connectionState = true;
                    Log.d(TAG, "connectionState = true");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.d(TAG, "DISCONNECT_REQUESTED");
                    connectionState = false;
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "DISCONNECTED");
                    connectionState = false;
                    Log.d(TAG, "connectionState = false");
                    //start discovery
                    if (bluetoothManager.disconnectSteam()) {
                        Log.d(TAG, "disconnectSteam");
                        if (bluetoothManager.disconnectSocket()) {
                            Log.d(TAG, "disconnectSocket");
                            IntentFilter intentFilterDiscovery = new IntentFilter();
                            intentFilterDiscovery.addAction(BluetoothDevice.ACTION_FOUND);
                            intentFilterDiscovery.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                            intentFilterDiscovery.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            intentFilterDiscovery.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                            registerReceiver(mBroadcastReceiverDiscoveryView, intentFilterDiscovery);
                            if (bluetoothManager.getAdapter().startDiscovery()) {
                                //Work in broadcast mBroadcastReceiverDiscoveryView
                                Log.d(TAG, "Start discovery ");
                            }
                        }
                    }
                    break;
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiverDiscoveryView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.d(TAG, "ACTION_FOUND");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                String lastConnectionMacAddress = sp.getString("LastConnectionMacAddress", "+");
                assert lastConnectionMacAddress != null;
                if (device.getAddress().equals(lastConnectionMacAddress)) {
                    Log.d(TAG, "equals : " + lastConnectionMacAddress + " -> " + device.getAddress());
                    bluetoothManager.setRemoteDevice(device);
                    Log.d(TAG, "Set device : " + device.getName());
                    bluetoothManager.getAdapter().cancelDiscovery();
                    Log.d(TAG, "Cancel discovery");

                    if (bluetoothManager.createSocket()) {
                        //socket is already
                        Log.d(TAG, "The socket is created successfully");
                        if (bluetoothManager.connectSocket()) {
                            //socket is connected
                            Log.d(TAG, "The socket is connected");
                            if (bluetoothManager.connectedStream()) {
                                //stream is connect
                                connectionState = true;
                                Log.d(TAG, "The stream are connected");
                            }
                        }
                    }
                }
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                //
                Log.d(TAG, "ACTION_DISCOVERY_STARTED");
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.d(TAG, "Connect stsate ACTION_DISCOVERY_FINISHED = " + connectionState);
                if (!connectionState) {
                    Log.d(TAG, "[+][+][+] \nNo device found re-search \n[+][+][+]");
                    bluetoothManager.getAdapter().startDiscovery();
                } else {
                    unregisterReceiver(mBroadcastReceiverDiscoveryView);
                }
            }
        }
    };


    /**
     * This method is invoked when camera preview has started. After this method is invoked
     * the frames will start to be delivered to client via the onCameraFrame() callback.
     *
     * @param width  -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted");

        systemInfo = new SystemInfo();
        //  DetectManager detectManager = new DetectManager();

        fRoiWidth = Float.parseFloat(RoiWidth);
        fRoiHeight = Float.parseFloat(RoiHeight);
        fRoiOffsetWidth = Float.parseFloat(RoiOffsetWidth);
        fRoiOffsetHeight = Float.parseFloat(RoiOffsetHeight);
        iThicknessRoi = Integer.parseInt(ThicknessRoi);
        iChecksToBeCarriedOut = Integer.parseInt(ChecksToBeCarriedOut);
        fTempMin = Float.parseFloat(TempMin);
        fThresholdTemp = Float.parseFloat(ThresholdTemp);
        fTempMax = Float.parseFloat(TempMax);
        fOvalFaceOffsetY = Float.parseFloat(OvalFaceOffsetY);
        fOvalFaceOffsetX = Float.parseFloat(OvalFaceOffsetX);

        Log.d(TAG, "\nfRoiWidth : " + fRoiWidth +
                "\nfRoiHeight : " + fRoiHeight +
                "\nfRoiOffsetWidth : " + fRoiOffsetWidth +
                "\nfRoiOffsetHeight : " + fRoiOffsetHeight +
                "\niThicknessRoi : " + iThicknessRoi +
                "\niChecksToBeCarriedOut : " + iChecksToBeCarriedOut +
                "\nfTempMin : " + fTempMin +
                "\nfThresholdTemp : " + fThresholdTemp +
                "\nfTempMax : " + fTempMax +
                "\nfOvalFaceOffsetY : " + fOvalFaceOffsetY +
                "\nfOvalFaceOffsetX : " + fOvalFaceOffsetX);

        list_temperature = new Float[iChecksToBeCarriedOut];

        freeze_loop = false;

        //Set auto size text
        viewDirections.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        viewDebug.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        viewTemperature.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        viewInfo.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);


        viewInfo.setBackgroundColor(Color.TRANSPARENT);
        viewDirections.setBackgroundColor(Color.TRANSPARENT);
        viewTemperature.setBackgroundColor(Color.TRANSPARENT);


        //Progressbar
        progressBar.setMax(iChecksToBeCarriedOut); //Max graphic load bar
        progressBar.setMin(0); //Min graphic load bar
    }

    /**
     * This method is invoked when camera preview has been stopped for some reason.
     * No frames will be delivered via onCameraFrame() callback after this method is called.
     */
    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped");
        if (checkBoxLogChecked) {
            try {
                fileWriterLogTemp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (checkBoxDebugChecked) {
            try {
                fileWriterLogDebug.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mRgba.release();
        mGray.release();
        unregisterReceiver(mBroadcastReceiverConnectView);
    }


    /**
     * This method is invoked when delivery of the frame needs to be done.
     * The returned values - is a modified frame which needs to be displayed on the screen.
     * TODO: pass the parameters specifying the format of the frame (BPP, YUV or RGB and etc)
     *
     * @param inputFrame
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "onCameraFrame");

        //Frame
        mRgba = inputFrame.rgba();//Color
        mGray = inputFrame.gray();//Gray

        //Ram
        long totalRamValue = systemInfo.totalRamMemorySizeDevice(this);
        long usedRamValue = systemInfo.usedRamMemoryDevice(this);

        //Set state logic
        boolean face_detect_frame = false;
        Scalar color_rect = new Scalar(225, 225, 225, 40);

        textViewMain = NoFaceDetect;

        Rect roi = DetectManager.createRoi(mGray, fRoiWidth, fRoiHeight);
        //Create sub roi rect in mRgba
        Rect subRoiMGray = DetectManager.createSubroiFromRoi(roi, fRoiOffsetWidth, fRoiOffsetHeight);
        //Create ROI in Mat mRoi from mRgba
        Mat mRoi = mGray.submat(roi);
        Mat mRoi_rotate;
        //Graphic debug
        if (checkBoxDebugChecked) {
            viewDebug.setVisibility(View.VISIBLE);
            String FPS_FORMAT = new DecimalFormat("0.00").format(FpsMeter.getFps());//Fps
            textViewDebug = getResources().getString(R.string.app_name)
                    + "\nFps : " + FPS_FORMAT
                    + "\nDisplay : " + mRgba.width() + "x" + mRgba.height()
                    + "\nRAM"
                    + "\n Device : "
                    + "\n  Total : " + totalRamValue + " MB"
                    + "\n  Used : " + usedRamValue + " MB"
                    + "\n App :"
                    + "\n  Total : " + systemInfo.maxMemory() + " MB"
                    + "\n  Used :" + systemInfo.usedMem() + " MB";
            viewDebug.setText(textViewDebug);
        } else {
            viewDebug.setVisibility(View.INVISIBLE);
        }

        int angle = checkRadioButton ? 90 : -90;
        mRoi_rotate = Imgproc.getRotationMatrix2D(new Point(mRoi.cols() / 2f, mRoi.rows() / 2f), angle, 1.0);
        Imgproc.warpAffine(mRoi, mRoi, mRoi_rotate, mRoi.size());

        //Detect face
        faceDetector.detectMultiScale(mGray, faceDetections, 1.1, 4, 2, subRoiMGray.size(), roi.size());
        angle = checkRadioButton ? -90 : 90;
        mRoi_rotate = Imgproc.getRotationMatrix2D(new Point(mRoi.cols() / 2f, mRoi.rows() / 2f), angle, 1.0);
        Imgproc.warpAffine(mRoi, mRoi, mRoi_rotate, mRoi.size());


        for (Rect rect : faceDetections.toArray()) {

            //Detect face
            face_detect_frame = true;

            //Set one face
            rect = faceDetections.toArray()[0];
            if (checkBoxDebugChecked) {
                //Graphic rect red on face detect
                Imgproc.rectangle(mRgba, rect.tl(), rect.br(), new Scalar(255, 0, 0), iThicknessRoi);
                //Direction for center point
                textViewDirection = DetectManager.arrowsDirections(roi, subRoiMGray, rect);
            }
            if (DetectManager.checkSquares(roi, subRoiMGray, rect)) {

                //if you are not in freeze not
                if (!freeze_loop) {
                    textViewFinalTemperature = "";
                    //Color for identification detect face inside roi
                    color_rect = new Scalar(0, 255, 255, 30);

                    //Check temperature how to specific in configuration
                    if (checks_carried_out <= (iChecksToBeCarriedOut - 1)) {

                        //Progress
                        progressBar.setProgress(checks_carried_out, true);

                        int k;
                        if (progressBar.getProgress() != 0) {
                            k = ((100 / progressBar.getProgress()) - 100) * -1;
                        } else {
                            k = progressBar.getProgress();
                        }

                        //If bluetooth is connect
                        if (connectionState) {
                            //Send command temperature
                            String resp = " - ";
                            Log.d(TAG, "Command -> " + BluetoothCommand);
                            if (bluetoothManager.sendData(BluetoothCommand)) {
                                //Get temperature
                                resp = bluetoothManager.receveData();
                            }

                            //save resp in file debug txt
                            Log.d(TAG, "Response -> " + resp);
                            if (resp.contains(" - ")) {
                                temperatureConvert = 0f;
                                Log.d(TAG, "(resp.equals(\" - \")");
                            } else {
                                temperatureConvert = DetectManager.convertTemperature(resp);
                            }

                            Log.d(TAG, "list_temperature.length : " + list_temperature.length +
                                    " \n checks_carried_out :" + checks_carried_out +
                                    " \n temperatureConvert :" + temperatureConvert);

                            //Save in float list
                            list_temperature[checks_carried_out] = temperatureConvert;
                            if (checkBoxDebugChecked) {
                                //Graphic text
                                textViewMain = TemperatureSentenceDetect + temperatureConvert + UnitTemperature + "\n" + Progress + k + "%";
                                try {
                                    fileWriterLogDebug.write(TimeZone.getDefault().toZoneId().toString() + "\n" + Calendar.getInstance().getTime().toString() +
                                            "\nCommand : " + BluetoothCommand + "\nConnectionState :" + connectionState + "\nCheck ID : " + checks_carried_out + "\nResponse : " + resp + "\n temperatureConvert : " + temperatureConvert + "\n");
                                    fileWriterLogDebug.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                textViewMain = "";
                            }
                        } else {
                            temperatureConvert = 0f;
                        }

                        //if check are end
                        if (checks_carried_out == (iChecksToBeCarriedOut - 1)) {

                            freeze_loop = true;//Active loop for block view text temperature detect
                            checks_carried_out = 0;//Reset control
                            temperatureStringComplete = NoBluetoothFound;

                            if (connectionState) {
                                //Temperature != 0
                                //Calculate threshold
                                Float[][] temperature_thresholds = DetectManager.temperatureThresholds(list_temperature);
                                if (checkBoxDebugChecked) {
                                    try {
                                        StringBuilder tempSave = new StringBuilder();
                                        for (int i = 3; i < list_temperature.length; i++) {
                                            tempSave.append(temperature_thresholds[i][0].toString()).append(" ");
                                        }
                                        fileWriterLogDebug.write("\nTemperature \nMin :" + temperature_thresholds[0][0]
                                                + " Med :" + temperature_thresholds[1][0]
                                                + " Max :" + temperature_thresholds[2][0]
                                                + "\n Complete list :" + tempSave.toString());
                                        fileWriterLogDebug.flush();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                //Get higher temperature
                                temperatureConvert = temperature_thresholds[2][0];

                                //Get sentence
                                temperatureStringComplete = DetectManager.temperatureResponse(temperature_thresholds[2][0],
                                        fTempMin, fThresholdTemp, fTempMax, TemperatureOk, Threshold, TemperatureMaxExceeded, TemperatureBelowLimit, ErrorTemperature);
                                if (checkBoxDebugChecked) {
                                    temperatureStringComplete = temperatureStringComplete + "\n" + Complete;
                                }

                                //Set color from temperature
                                color_for_loop = DetectManager.getColoFromTemperature(temperature_thresholds[2][0], fTempMin, fTempMax);
                                int i = DetectManager.levelTemp(temperature_thresholds[2][0],
                                        fTempMin, fThresholdTemp, fTempMax);
                                switch (i) {
                                    case 0:
                                    case 1:
                                        colorDetectForLoop = Color.GREEN;
                                        break;
                                    case 2:
                                        colorDetectForLoop = Color.RED;
                                        break;
                                    case 3:
                                        colorDetectForLoop = Color.BLUE;
                                        break;
                                    case 4:
                                        colorDetectForLoop = Color.GRAY;
                                        break;
                                }
                                if (checkBoxLogChecked) {
                                    try {
                                        if (i <= 1) {
                                            fileWriterLogTemp.write("Temperature : " + temperatureConvert + " " + Calendar.getInstance().getTime().toString() + "\n");
                                            fileWriterLogTemp.flush();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            checks_carried_out++;//Increment value
                        }
                    }
                }
            } else {
                checks_carried_out = 0;//Reset check_value
                Arrays.fill(list_temperature, 0f);//Reset list
                freeze_loop = false;
                progressBar.setProgress(progressBar.getMin(), true);//Progress
                textViewFinalTemperature = "";
            }
        }


        //Nothing face detect
        if (!face_detect_frame) {
            checks_carried_out = 0;//Reset check_value
            Arrays.fill(list_temperature, 0f);//Reset list
            freeze_loop = false;
            progressBar.setProgress(progressBar.getMin(), true);//Progress
            textViewDirection = "";
            textViewFinalTemperature = "";
            color_rect = new Scalar(225, 225, 225, 40);
        }

        //Graphic debug
        if (freeze_loop) {
            progressBar.setProgress(progressBar.getMax(), true);//Progress

            //Graphic text
            textViewMain = temperatureStringComplete;
            textViewDirection = DetectionCompleted;
            textViewFinalTemperature = temperatureConvert + UnitTemperature;
            if (connectionState) {
                color_rect = color_for_loop;
                //Background text
                viewInfo.setBackgroundColor(getColor(R.color.colorTextBackground));
                viewDirections.setBackgroundColor(getColor(R.color.colorTextBackground));
                viewTemperature.setBackgroundColor(getColor(R.color.colorTextBackground));
                //Color text
                viewTemperature.setTextColor(colorDetectForLoop);
                viewInfo.setTextColor(colorDetectForLoop);
                viewDirections.setTextColor(getColor(R.color.colorTextDebug));
            }
        } else {
            //Background text
            viewInfo.setBackgroundColor(Color.TRANSPARENT);
            viewDirections.setBackgroundColor(Color.TRANSPARENT);
            viewTemperature.setBackgroundColor(Color.TRANSPARENT);
            //Color text
            viewInfo.setTextColor(Color.WHITE);
            viewDirections.setTextColor(Color.WHITE);
            viewTemperature.setTextColor(Color.WHITE);
        }
        if (checkBoxDebugChecked) {
            //Graphic rect
            Imgproc.rectangle(mRgba, roi.tl(), roi.br(), color_rect, iThicknessRoi); //Graphic rect blue in mRgba
            Imgproc.rectangle(mRgba, subRoiMGray.tl(), subRoiMGray.br(), color_rect, iThicknessRoi); //Graphic rect green in mRgba
        }

        viewInfo.setText(textViewMain);
        viewDirections.setText(textViewDirection);
        viewTemperature.setText(textViewFinalTemperature);

        RotatedRect box = new RotatedRect(new Point(mRgba.width() * 0.5, (mRgba.height() * 0.5)), new Size(fOvalFaceOffsetX, fOvalFaceOffsetY), 90);
        Imgproc.ellipse(mRgba, box, color_rect, iThicknessRoi);
        return mRgba;
    }
}




