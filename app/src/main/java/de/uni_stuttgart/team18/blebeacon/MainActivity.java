package de.uni_stuttgart.team18.blebeacon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;

import de.uni_stuttgart.team18.blebeacon.beacon.BLEScanner;
import de.uni_stuttgart.team18.blebeacon.beacon.EddystoneBeacon;

public class MainActivity extends AppCompatActivity implements BLEScanner.BLEScannerCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    //view elements
    private TextView mTextInstanceId;
    private TextView mTextNameSpaceId;
    private TextView mTextVoltage;
    private TextView mTextTemperature;
    private TextView mTextDistance;
    private TextView mTextURL;
    private TextView mTextLastUpdate;
    private Button mButton;

    private BLEScanner scanner;
    private EddystoneBeacon beacon;
    private boolean mScanRequested;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scanner = new BLEScanner(mBluetoothAdapter, this);
        beacon = EddystoneBeacon.getInstance();

        if (isLocationPermissionGranted()) {
            Log.i(TAG, "onCreate: Permission has been granted");
        }

        //get all the views
        mTextInstanceId = findViewById(R.id.text_instance_id);
        mTextNameSpaceId = findViewById(R.id.text_namespace_id);
        mTextVoltage = findViewById(R.id.text_voltage);
        mTextTemperature = findViewById(R.id.text_temperature);
        mTextDistance = findViewById(R.id.text_distance);
        mTextURL = findViewById(R.id.text_url);
        mTextLastUpdate = findViewById(R.id.text_last_update);
        mButton = findViewById(R.id.button_toggle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mScanRequested && isLocationPermissionGranted()) {
            startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanner.stopScan();
    }

    private void startScan() {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "startScan: Bluetooth not availble");
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            mScanRequested = false;
            mButton.setText("Start");
        } else {
            scanner.startScan();
            mButton.setText("Stop");
        }
    }

    private boolean isLocationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onCreate: Do not have permissions");
            // Permission is not granted ask for permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);

            if (mScanRequested) {
                startScan();
            }

            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length == 2 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission has been granted");
                }
                break;
        }
    }

    @Override
    public void onScanResult(ScanResult result) {
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null && !scanRecord.getServiceData().isEmpty()) {
            byte[] data = scanRecord.getServiceData(scanRecord.getServiceUuids().get(0));
            Date date = new Date();
            mTextLastUpdate.setText(date.toString());

            beacon.update(data, result.getRssi());

            if (beacon.getNameSpaceId() != null) {
                mTextNameSpaceId.setText(beacon.getNameSpaceId());
            } else {
                mTextNameSpaceId.setText("n/a");
            }
            if (beacon.getInstanceId() != null) {
                mTextInstanceId.setText(beacon.getInstanceId());
            } else {
                mTextInstanceId.setText("n/a");
            }
            if (beacon.getUrl() != null) {
                mTextURL.setText(beacon.getUrl());
            } else {
                mTextURL.setText("n/a");
            }
            if (beacon.getVoltage() != 0) {
                mTextVoltage.setText(String.format(Locale.getDefault(), "%d mV", beacon.getVoltage()));
            } else {
                mTextVoltage.setText("n/a");
            }

            mTextTemperature.setText(String.format(Locale.getDefault(), "%.02f °C", beacon.getTemperature()));
            mTextDistance.setText(String.format(Locale.getDefault(), "%.02f °C", beacon.getDistance()));
        }
    }

    @Override
    public void onScanFailed(BLEScanner.ScannerFailureReason reason) {
        Log.e(TAG, "onScanFailed: Could not start scan");
        Toast.makeText(this, "Sorry! The scan failed", Toast.LENGTH_SHORT).show();
    }


    public void toggleScanState(View view) {
        mScanRequested = !mScanRequested;
        if (mScanRequested && isLocationPermissionGranted()) {
            startScan();
        } else if (!mScanRequested) {
            scanner.stopScan();
            mButton.setText("Start");
        }
    }
}
