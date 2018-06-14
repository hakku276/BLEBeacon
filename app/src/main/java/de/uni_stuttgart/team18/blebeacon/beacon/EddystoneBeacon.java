package de.uni_stuttgart.team18.blebeacon.beacon;

import android.bluetooth.le.ScanResult;

public class EddystoneBeacon {
    private String nameSpaceId;
    private String instanceId;
    private String url;
    private short voltage;
    private short temperature;
    private float distance;

    private static final EddystoneBeacon instance = new EddystoneBeacon();

    public static EddystoneBeacon getInstance(){
        return instance;
    }

    private EddystoneBeacon(){

    }

    public void update(byte[] data){
        switch (data[0]){
            case 0:
                updateUIDFrame(data);
                break;
            case 16:
                updateURLFrame(data);
                break;
            case 32:
                break;
        }
    }

    private void updateUIDFrame(byte[] data){
        nameSpaceId = toHex(data, 2, 10);
        instanceId = toHex(data, 12, 6);
    }

    private void updateURLFrame(byte[] data){

    }

    private String toHex(byte[] data, int offset, int length){
        if (offset + length > data.length) {
            throw new IndexOutOfBoundsException("Array length out of bound");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(String.format("%02X",data[offset + i]));
        }
        return builder.toString();
    }

    public String getNameSpaceId() {
        return nameSpaceId;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
