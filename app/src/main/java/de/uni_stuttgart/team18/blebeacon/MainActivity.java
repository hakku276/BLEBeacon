package de.uni_stuttgart.team18.blebeacon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

import de.uni_stuttgart.team18.blebeacon.beacon.BLEScanner;
import de.uni_stuttgart.team18.blebeacon.beacon.EddystoneBeacon;

public class MainActivity extends AppCompatActivity implements BLEScanner.BLEScannerCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private BLEScanner scanner;

    private EddystoneBeacon beacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = new BLEScanner(adapter, this);
        beacon = EddystoneBeacon.getInstance();

        if(isLocationPermissionGranted()){
            Log.i(TAG, "onCreate: Permission has been granted");
            scanner.startScan();
        }
    }

    private boolean isLocationPermissionGranted(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onCreate: Do not have permissions");
            // Permission is not granted ask for permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
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
        if(scanRecord != null && !scanRecord.getServiceData().isEmpty()){
            byte[] data = scanRecord.getServiceData(scanRecord.getServiceUuids().get(0));
            Log.d(TAG, "onScanResult: " + Arrays.toString(data));
            beacon.update(data, result.getRssi());
            Log.d(TAG, "onScanResult: namespace id: " + beacon.getNameSpaceId());
            Log.d(TAG, "onScanResult: instance id: " + beacon.getInstanceId());
        }
    }

    @Override
    public void onScanFailed(BLEScanner.ScannerFailureReason reason) {
        Log.e(TAG, "onScanFailed: Could not start scan");
    }
}
