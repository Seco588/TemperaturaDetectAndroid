package com.corporate.temperaturedetect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.corporate.temperaturedetect.bluetooth.BluetoothActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    boolean checkRadioButton;

    //Permission list
    private final String[] PERMISSION = {
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        //Set Layout
        setContentView(R.layout.activity_main);

        //Permission
        checkPermission();

        //Get SharePreferences
        final SharedPreferences sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        //Get View
        final EditText editTextBluetoothCommand = findViewById(R.id.editTextBluetoothCommandToSend);
        final EditText editTextNumberChecksToBeCarriedOut = findViewById(R.id.editTextNumberChecksToBeCarriedOut);
        final EditText editTextNumberDecimalMaxTemp = findViewById(R.id.editTextNumberDecimalTempMax);
        final EditText editTextNumberDecimalMinTemp = findViewById(R.id.editTextNumberDecimalTempMin);
        final EditText editTextNumberDecimalOvalFaceOffsetX = findViewById(R.id.editTextNumberDecimalOvalFaceOffsetX);
        final EditText editTextNumberDecimalOvalFaceOffsetY = findViewById(R.id.editTextNumberDecimalOvalFaceOffsetY);
        final EditText editTextNumberDecimalRoiHeight = findViewById(R.id.editTextNumberDecimalRoiH);
        final EditText editTextNumberDecimalRoiWidth = findViewById(R.id.editTextNumberDecimalRoiW);
        final EditText editTextNumberDecimalRoiOffsetHeight = findViewById(R.id.editTextNumberDecimalRoiOffsetH);
        final EditText editTextNumberDecimalRoiOffsetWidth = findViewById(R.id.editTextNumberDecimalRoiOffsetW);
        final EditText editTextNumberDecimalThresholdTemp = findViewById(R.id.editTextNumberDecimalTempThreshold);
        final EditText editTextNumberThicknessRoi = findViewById(R.id.editTextNumberRoiThickness);
        final EditText editTextTextDetectionCompleted = findViewById(R.id.editTextTextDetectionCompleted);
        final EditText editTextTextErrorTemperature = findViewById(R.id.editTextTextErrorTemperature);
        final EditText editTextTextNoBluetoothFound = findViewById(R.id.editTextTextNoBluetoothFound);
        final EditText editTextTextNoFaceDetect = findViewById(R.id.editTextTextNoFaceDetect);
        final EditText editTextTextComplete = findViewById(R.id.editTextTextTemperatureInsideRange);
        final EditText editTextTextProgress = findViewById(R.id.editTextTextProgress);
        final EditText editTextTextTemperatureBelowLimit = findViewById(R.id.editTextTextTemperatureBelowLimit);
        final EditText editTextTextTemperatureMaxExceeded = findViewById(R.id.editTextTextTemperatureMaxExceeded);
        final EditText editTextTextTemperatureOk = findViewById(R.id.editTextTextTemperatureOk);
        final EditText editTextTextTemperatureSentenceDetect = findViewById(R.id.editTextTextTemperatureSentenceDetect);
        final EditText editTextTextThreshold = findViewById(R.id.editTextTextThreshold);
        final EditText editTextTextUnitTemperature = findViewById(R.id.editTextTextUnitTemperature);
        final CheckBox checkBoxDebug = findViewById(R.id.checkBoxDebug); //Enable debugcheckBoxLog
        final CheckBox checkBoxLog = findViewById(R.id.checkBoxLog); //Enable checkBoxLog
        final RadioButton radioButtonFront = findViewById(R.id.radioButtonMainFront); //Camera
        final RadioButton radioButtonRetro = findViewById(R.id.radioButtonMainRetro); //Camera
        final Button buttonResetLog = findViewById(R.id.buttonResetLog); //Button for reset log
        final Button button = findViewById(R.id.buttonMainNext); //Button for save SharedPreferences and change view

        //Set Hint
        editTextBluetoothCommand.setHint(sp.getString("BluetoothConnectionAttempts", getResources().getString(R.string.hBluetoothCommandToSend)));
        editTextNumberChecksToBeCarriedOut.setHint(sp.getString("ChecksToBeCarriedOut", getResources().getString(R.string.hChecksToBeCarriedOut)));
        editTextNumberDecimalMaxTemp.setHint(sp.getString("TempMax", getResources().getString(R.string.hTempMax)));
        editTextNumberDecimalMinTemp.setHint(sp.getString("TempMin", getResources().getString(R.string.hTempMin)));
        editTextNumberDecimalOvalFaceOffsetX.setHint(sp.getString("OvalFaceOffsetX", getResources().getString(R.string.hOvalFaceOffsetX)));
        editTextNumberDecimalOvalFaceOffsetY.setHint(sp.getString("OvalFaceOffsetY", getResources().getString(R.string.hOvalFaceOffsetY)));
        editTextNumberDecimalRoiHeight.setHint(sp.getString("RoiHeight", getResources().getString(R.string.hRoiH)));
        editTextNumberDecimalRoiWidth.setHint(sp.getString("RoiWidth", getResources().getString(R.string.hRoiW)));
        editTextNumberDecimalRoiOffsetHeight.setHint(sp.getString("RoiOffsetHeight", getResources().getString(R.string.hRoiOffsetH)));
        editTextNumberDecimalRoiOffsetWidth.setHint(sp.getString("RoiOffsetWidth", getResources().getString(R.string.hRoiOffsetW)));
        editTextNumberDecimalThresholdTemp.setHint(sp.getString("ThresholdTemp", getResources().getString(R.string.hTempThreshold)));
        editTextNumberThicknessRoi.setHint(sp.getString("ThicknessRoi", getResources().getString(R.string.hRoiThickness)));
        editTextTextDetectionCompleted.setHint(sp.getString("DetectionCompleted", getResources().getString(R.string.hDetectionCompleted)));
        editTextTextErrorTemperature.setHint(sp.getString("ErrorTemperature", getResources().getString(R.string.hErrorTemperature)));
        editTextTextNoBluetoothFound.setHint(sp.getString("NoBluetoothFound", getResources().getString(R.string.hNoBluetoothFound)));
        editTextTextNoFaceDetect.setHint(sp.getString("NoFaceDetect", getResources().getString(R.string.hNoFaceDetect)));
        editTextTextComplete.setHint(sp.getString("Complete", getResources().getString(R.string.hTemperatureInsideRange)));
        editTextTextProgress.setHint(sp.getString("Progress", getResources().getString(R.string.hTemperatureInProgress)));
        editTextTextTemperatureBelowLimit.setHint(sp.getString("TemperatureBelowLimit", getResources().getString(R.string.hTemperatureBelowLimit)));
        editTextTextTemperatureMaxExceeded.setHint(sp.getString("TemperatureMaxExceeded", getResources().getString(R.string.hTemperatureMaxExceeded)));
        editTextTextTemperatureOk.setHint(sp.getString("TemperatureOk", getResources().getString(R.string.hTemperatureOk)));
        editTextTextTemperatureSentenceDetect.setHint(sp.getString("TemperatureSentenceDetect", getResources().getString(R.string.hTemperatureSentenceDetect)));
        editTextTextThreshold.setHint(sp.getString("Threshold", getResources().getString(R.string.hTemperatureInsideThreshold)));
        editTextTextUnitTemperature.setHint(sp.getString("UnitTemperature", getResources().getString(R.string.hUnitTemperature)));

        //Set "Hint" checkbox
        checkBoxDebug.setChecked(sp.getBoolean("CheckBoxDebugChecked", getResources().getBoolean(R.bool.Debug)));//fasle
        checkBoxLog.setChecked(sp.getBoolean("CheckBoxLogChecked", getResources().getBoolean(R.bool.Log)));//false
        checkRadioButton = sp.getBoolean("CheckRadioButton", getResources().getBoolean(R.bool.Camera));//true = front
        if (checkRadioButton) {
            radioButtonFront.setChecked(true);
            radioButtonRetro.setChecked(false);
        } else {
            radioButtonFront.setChecked(false);
            radioButtonRetro.setChecked(true);
        }


        radioButtonFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRadioButton = true;
                radioButtonRetro.setChecked(false);
            }
        });

        radioButtonRetro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRadioButton = false;
                radioButtonFront.setChecked(false);
            }
        });

        buttonResetLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log File Temp
                String nameFileDebugTemp = "Debug_Temperature";
                String extensionDebugTemp = ".txt";
                String dirFileLogTemp = getExternalFilesDir(null) + "/" + nameFileDebugTemp + extensionDebugTemp;
                Log.i(TAG, "Check log file -> " + nameFileDebugTemp + extensionDebugTemp + "\n dir -> " + dirFileLogTemp);
                File fileLogTemp = new File(dirFileLogTemp);
                if (fileLogTemp.exists()) {
                    if (fileLogTemp.delete()) {
                        Toast.makeText(MainActivity.this, "Reset ...", Toast.LENGTH_SHORT).show();
                        try {
                            if (fileLogTemp.createNewFile()) {
                                Toast.makeText(MainActivity.this, "Reset Log file Temperature complete!", Toast.LENGTH_SHORT).show();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "File Temperature not exist", Toast.LENGTH_SHORT).show();

                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean Debug = checkBoxDebug.isChecked();
                boolean Log = checkBoxLog.isChecked();
                String BluetoothCommand = editTextBluetoothCommand.getText().toString();
                String UnitTemperature = editTextTextUnitTemperature.getText().toString();
                String Threshold = editTextTextThreshold.getText().toString();
                String TemperatureSentenceDetect = editTextTextTemperatureSentenceDetect.getText().toString();
                String TemperatureOk = editTextTextTemperatureOk.getText().toString();
                String TemperatureMaxExceeded = editTextTextTemperatureMaxExceeded.getText().toString();
                String TemperatureBelowLimit = editTextTextTemperatureBelowLimit.getText().toString();
                String Progress = editTextTextProgress.getText().toString();
                String Complete = editTextTextComplete.getText().toString();
                String NoFaceDetect = editTextTextNoFaceDetect.getText().toString();
                String NoBluetoothFound = editTextTextNoBluetoothFound.getText().toString();
                String ErrorTemperature = editTextTextErrorTemperature.getText().toString();
                String DetectionCompleted = editTextTextDetectionCompleted.getText().toString();
                String ThicknessRoi = editTextNumberThicknessRoi.getText().toString();
                String ThresholdTemp = editTextNumberDecimalThresholdTemp.getText().toString();
                String RoiOffsetWidth = editTextNumberDecimalRoiOffsetWidth.getText().toString();
                String RoiOffsetHeight = editTextNumberDecimalRoiOffsetHeight.getText().toString();
                String RoiWidth = editTextNumberDecimalRoiWidth.getText().toString();
                String RoiHeight = editTextNumberDecimalRoiHeight.getText().toString();
                String OvalFaceOffsetY = editTextNumberDecimalOvalFaceOffsetY.getText().toString();
                String OvalFaceOffsetX = editTextNumberDecimalOvalFaceOffsetX.getText().toString();
                String TempMin = editTextNumberDecimalMinTemp.getText().toString();
                String TempMax = editTextNumberDecimalMaxTemp.getText().toString();
                String ChecksToBeCarriedOut = editTextNumberChecksToBeCarriedOut.getText().toString();

                //save in SharedPreferences
                SharedPreferences.Editor editor = sp.edit();

                editor.putBoolean("CheckBoxDebugChecked", Debug);
                editor.putBoolean("CheckBoxLogChecked", Log);
                editor.putBoolean("CheckRadioButton", checkRadioButton);

                if (!BluetoothCommand.isEmpty()) {
                    editor.putString("BluetoothCommand", BluetoothCommand);
                } else {
                    editor.putString("BluetoothCommand", String.valueOf(editTextBluetoothCommand.getHint()));
                }

                if (!UnitTemperature.isEmpty()) {
                    editor.putString("UnitTemperature", UnitTemperature);
                } else {
                    editor.putString("UnitTemperature", String.valueOf(editTextTextUnitTemperature.getHint()));
                }

                if (!Threshold.isEmpty()) {
                    editor.putString("Threshold", Threshold);
                } else {
                    editor.putString("Threshold", String.valueOf(editTextTextThreshold.getHint()));
                }

                if (!TemperatureSentenceDetect.isEmpty()) {
                    editor.putString("TemperatureSentenceDetect", TemperatureSentenceDetect);
                } else {
                    editor.putString("TemperatureSentenceDetect", String.valueOf(editTextTextTemperatureSentenceDetect.getHint()));
                }

                if (!TemperatureOk.isEmpty()) {
                    editor.putString("TemperatureOk", TemperatureOk);
                } else {
                    editor.putString("TemperatureOk", String.valueOf(editTextTextTemperatureOk.getHint()));
                }

                if (!TemperatureMaxExceeded.isEmpty()) {
                    editor.putString("TemperatureMaxExceeded", TemperatureMaxExceeded);
                } else {
                    editor.putString("TemperatureMaxExceeded", String.valueOf(editTextTextTemperatureMaxExceeded.getHint()));
                }

                if (!TemperatureBelowLimit.isEmpty()) {
                    editor.putString("TemperatureBelowLimit", TemperatureBelowLimit);
                } else {
                    editor.putString("TemperatureBelowLimit", String.valueOf(editTextTextTemperatureBelowLimit.getHint()));
                }

                if (!Progress.isEmpty()) {
                    editor.putString("Progress", Progress);
                } else {
                    editor.putString("Progress", String.valueOf(editTextTextProgress.getHint()));
                }

                if (!Complete.isEmpty()) {
                    editor.putString("Complete", Complete);
                } else {
                    editor.putString("Complete", String.valueOf(editTextTextComplete.getHint()));
                }

                if (!NoFaceDetect.isEmpty()) {
                    editor.putString("NoFaceDetect", NoFaceDetect);
                } else {
                    editor.putString("NoFaceDetect", String.valueOf(editTextTextNoFaceDetect.getHint()));
                }

                if (!NoBluetoothFound.isEmpty()) {
                    editor.putString("NoBluetoothFound", NoBluetoothFound);
                } else {
                    editor.putString("NoBluetoothFound", String.valueOf(editTextTextNoBluetoothFound.getHint()));
                }

                if (!ErrorTemperature.isEmpty()) {
                    editor.putString("ErrorTemperature", ErrorTemperature);
                } else {
                    editor.putString("ErrorTemperature", String.valueOf(editTextTextErrorTemperature.getHint()));
                }

                if (!DetectionCompleted.isEmpty()) {
                    editor.putString("DetectionCompleted", DetectionCompleted);
                } else {
                    editor.putString("DetectionCompleted", String.valueOf(editTextTextDetectionCompleted.getHint()));
                }

                if (!ThicknessRoi.isEmpty()) {
                    editor.putString("ThicknessRoi", ThicknessRoi);
                } else {
                    editor.putString("ThicknessRoi", String.valueOf(editTextNumberThicknessRoi.getHint()));
                }

                if (!ThresholdTemp.isEmpty()) {
                    editor.putString("ThresholdTemp", ThresholdTemp);
                } else {
                    editor.putString("ThresholdTemp", String.valueOf(editTextNumberDecimalThresholdTemp.getHint()));
                }

                if (!RoiOffsetWidth.isEmpty()) {
                    editor.putString("RoiOffsetWidth", RoiOffsetWidth);
                } else {
                    editor.putString("RoiOffsetWidth", String.valueOf(editTextNumberDecimalRoiOffsetWidth.getHint()));
                }

                if (!RoiOffsetHeight.isEmpty()) {
                    editor.putString("RoiOffsetHeight", RoiOffsetHeight);
                } else {
                    editor.putString("RoiOffsetHeight", String.valueOf(editTextNumberDecimalRoiOffsetHeight.getHint()));
                }

                if (!RoiWidth.isEmpty()) {
                    editor.putString("RoiWidth", RoiWidth);
                } else {
                    editor.putString("RoiWidth", String.valueOf(editTextNumberDecimalRoiWidth.getHint()));
                }

                if (!RoiHeight.isEmpty()) {
                    editor.putString("RoiHeight", RoiHeight);
                } else {
                    editor.putString("RoiHeight", String.valueOf(editTextNumberDecimalRoiHeight.getHint()));
                }

                if (!OvalFaceOffsetY.isEmpty()) {
                    editor.putString("OvalFaceOffsetY", OvalFaceOffsetY);
                } else {
                    editor.putString("OvalFaceOffsetY", String.valueOf(editTextNumberDecimalOvalFaceOffsetY.getHint()));
                }

                if (!OvalFaceOffsetX.isEmpty()) {
                    editor.putString("OvalFaceOffsetX", OvalFaceOffsetX);
                } else {
                    editor.putString("OvalFaceOffsetX", String.valueOf(editTextNumberDecimalOvalFaceOffsetX.getHint()));
                }

                if (!TempMin.isEmpty()) {
                    editor.putString("TempMin", TempMin);
                } else {
                    editor.putString("TempMin", String.valueOf(editTextNumberDecimalMinTemp.getHint()));
                }

                if (!TempMax.isEmpty()) {
                    editor.putString("TempMax", TempMax);
                } else {
                    editor.putString("TempMax", String.valueOf(editTextNumberDecimalMaxTemp.getHint()));
                }

                if (!ChecksToBeCarriedOut.isEmpty()) {
                    editor.putString("ChecksToBeCarriedOut", ChecksToBeCarriedOut);
                } else {
                    editor.putString("ChecksToBeCarriedOut", String.valueOf(editTextNumberChecksToBeCarriedOut.getHint()));
                }

                editor.apply();

                //Show message for user
                Toast.makeText(MainActivity.this, getString(R.string.sSavePreference), Toast.LENGTH_SHORT).show();

                //Go to Bluetooth activity
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
                finish();
            }
        });


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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
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

    private void checkPermission() {
        //Permission
        requestPermissions(PERMISSION, PackageManager.PERMISSION_GRANTED);
        for (String permission : PERMISSION) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, getString(R.string.sMainPermissionNotOk) + permission);
                // Toast.makeText(this, getString(R.string.sMainPermissionNotOk) + permission, Toast.LENGTH_LONG).show();
            }
        }
    }
}