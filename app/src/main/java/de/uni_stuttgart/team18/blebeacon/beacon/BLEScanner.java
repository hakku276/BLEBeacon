package de.uni_stuttgart.team18.blebeacon.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * The BLE Scanner which scans for the respective BLE Devices
 * Created by aanal on 5/24/17.
 */

public class BLEScanner extends ScanCallback {

    /**
     * The BLE Scanner Callback which can be used to get feedback from the scanner
     */
    public interface BLEScannerCallback {
        /**
         * Called Whenever a device with the scan criteria has been found
         *
         * @param result the ScanResult of the scan
         */
        void onScanResult(ScanResult result);

        /**
         * Called when the scan fails
         *
         * @param reason The reason of the failure
         */
        void onScanFailed(ScannerFailureReason reason);
    }

    /**
     * The reasons for Scanner Failure
     */
    public enum ScannerFailureReason {
        SCAN_ALREADY_STARTED,
        APPLICATION_REGISTRATION_FAILED,
        SCAN_NOT_SUPPORTED,
        INTERNAL_ERROR
    }

    private static final String TAG = BLEScanner.class.getName();

    /**
     * defines whether the device is currently BLE Scanning or not
     */
    private boolean mScanning;
    /**
     * The bluetooth adapter, to start start the LE Scans
     */
    private BluetoothAdapter adapter;

    /**
     * Callback that is waiting for scanner results
     */
    private BLEScannerCallback mCallback;

    public BLEScanner(BluetoothAdapter adapter, BLEScannerCallback callback) {
        this.adapter = adapter;
        mScanning = false;
        this.mCallback = callback;
    }

    /**
     * Starts a scan for any possible device
     */
    public void startScan() {
        if (!mScanning) {
            mScanning = true;
            adapter.getBluetoothLeScanner().startScan(this);
        } else {
            Log.d(TAG, "startScan: Scanner already running");
        }
    }

    /**
     * Stops the scan
     */
    void stopScan() {
        if (mScanning) {
            Log.d(TAG, "stopScan: Stopping LE Scanner");
            adapter.getBluetoothLeScanner().stopScan(this);
            mScanning = false;
        } else {
            Log.d(TAG, "stopScan: Scanner not running");
        }
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        Log.d(TAG, "onScanResult: Device Found: " + result.toString());
        if (mCallback != null) {
            mCallback.onScanResult(result);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        //nothing to do here
        Log.d(TAG, "onBatchScanResults: Devices found: " + results.size());
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e(TAG, "onScanFailed: The Scan Failed with error code: " + errorCode);
        ScannerFailureReason reason;
        switch (errorCode) {
            case SCAN_FAILED_ALREADY_STARTED:
                reason = ScannerFailureReason.SCAN_ALREADY_STARTED;
                break;
            case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                reason = ScannerFailureReason.APPLICATION_REGISTRATION_FAILED;
                break;
            case SCAN_FAILED_FEATURE_UNSUPPORTED:
                reason = ScannerFailureReason.SCAN_NOT_SUPPORTED;
                break;
            case SCAN_FAILED_INTERNAL_ERROR:
                reason = ScannerFailureReason.INTERNAL_ERROR;
                break;
            default:
                reason = ScannerFailureReason.INTERNAL_ERROR;
        }
        if (reason == ScannerFailureReason.SCAN_ALREADY_STARTED) {
            // if scan already started, it is not a failure
            mScanning = true;
        } else if (mCallback != null) {
            mCallback.onScanFailed(reason);
        }
    }
}
