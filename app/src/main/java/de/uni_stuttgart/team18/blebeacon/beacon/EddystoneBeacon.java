package de.uni_stuttgart.team18.blebeacon.beacon;

public class EddystoneBeacon {

    private static final EddystoneBeacon instance = new EddystoneBeacon();
    private String nameSpaceId;
    private String instanceId;
    private String url;
    private short voltage;
    private float temperature;
    private float distance;

    private EddystoneBeacon() {
    }

    public static EddystoneBeacon getInstance() {
        return instance;
    }

    public void update(byte[] data, int rssi) {
        switch (data[0]) {
            case 0:
                updateUIDFrame(data);
                updateDistance(rssi, data[1]);
                break;
            case 16:
                updateURLFrame(data);
                updateDistance(rssi, data[1]);
                break;
            case 32:
                updateTLMFrame(data);
                break;
        }
    }

    /**
     * Updates the beacon information with the UID frame
     *
     * @param data the uid frame raw data
     */
    private void updateUIDFrame(byte[] data) {
        nameSpaceId = toHex(data, 2, 10);
        instanceId = toHex(data, 12, 6);
    }

    /**
     * Updates the beacon information with the URL frame
     *
     * @param data the url frame raw data
     */
    private void updateURLFrame(byte[] data) {
        StringBuilder urlBuilder = new StringBuilder();
        URLScheme scheme = URLScheme.from(data[2]);
        if (scheme != null) {
            urlBuilder.append(scheme.getExpansion());
            for (int i = 3; i < data.length; i++) {
                if (data[i] < 14) {
                    //byte is a coded byte
                    URLCode code = URLCode.from(data[i]);
                    if (code != null) {
                        urlBuilder.append(code.getExpansion());
                    }
                } else {
                    urlBuilder.append((char) data[i]);
                }
            }
        }
        this.url = urlBuilder.toString();
    }

    /**
     * Updates the Distance information using the RSSI of the signal
     *
     * @param rssi    the signal rssi
     * @param txPower the transmitted signal power
     */
    private void updateDistance(int rssi, byte txPower) {
        distance = 0;
    }

    /**
     * Updates the beacon information with the telemetry frame
     *
     * @param data the raw telemetry frame data
     */
    private void updateTLMFrame(byte[] data) {
        voltage = (short) (((data[2] & 0xff) << 8) | (data[3] & 0xff));

        temperature = (float) ((int) data[4]);
        temperature += (float) (data[5] / 100);
    }

    /**
     * Converts the provided byte array into hex string
     *
     * @param data   the data to be converted
     * @param offset the start offset for the conversion begin
     * @param length the total number of bytes to convert
     * @return the converted hex string
     */
    private String toHex(byte[] data, int offset, int length) {
        if (offset + length > data.length) {
            throw new IndexOutOfBoundsException("Array length out of bound");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(String.format("%02X", data[offset + i]));
        }
        return builder.toString();
    }

    public String getNameSpaceId() {
        return nameSpaceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getDistance() {
        return distance;
    }

    public String getUrl() {
        return url;
    }

    public short getVoltage() {
        return voltage;
    }

    /**
     * The URL Scheme specific to the Eddystone Standards
     */
    private enum URLScheme {
        HTTP_WWW((byte) 0, "http://www."),
        HTTPS_WWW((byte) 1, "https://www."),
        HTTP((byte) 2, "http://"),
        HTTPS((byte) 3, "https://");

        private byte code;
        private String expansion;

        URLScheme(byte code, String expansion) {
            this.code = code;
            this.expansion = expansion;
        }

        /**
         * Convert the eddystone byte code into a URL scheme
         *
         * @param code the eddy stone byte code
         * @return the URL scheme, null if not available
         */
        static URLScheme from(byte code) {
            for (URLScheme scheme :
                    URLScheme.values()) {
                if (scheme.getCode() == code) {
                    return scheme;
                }
            }
            return null;
        }

        public byte getCode() {
            return code;
        }

        public String getExpansion() {
            return expansion;
        }
    }

    /**
     * The URL Codes specific to the Eddystone Standards
     */
    private enum URLCode {
        COM_((byte) 0, ".com/"), ORG_((byte) 1, ".org/"), EDU_((byte) 2, ".edu/"),
        NET_((byte) 3, ".net/"), INFO_((byte) 4, ".info/"), BIZ_((byte) 5, ".biz/"),
        GOV_((byte) 6, ".gov/"), COM((byte) 7, ".com"), ORG((byte) 8, ".org"),
        EDU((byte) 9, ".edu"), NET((byte) 10, ".net"), INFO((byte) 11, ".info"),
        BIZ((byte) 12, ".biz"), GOV((byte) 13, ".gov");

        private byte code;
        private String expansion;

        URLCode(byte code, String expansion) {
            this.code = code;
            this.expansion = expansion;
        }

        /**
         * Convert the byte code into the URLCode respresentation
         *
         * @param code the byte code to be converted
         * @return the URLCode, null if not available
         */
        static URLCode from(byte code) {
            for (URLCode urlCode :
                    URLCode.values()) {
                if (urlCode.code == code) {
                    return urlCode;
                }
            }
            return null;
        }

        byte getCode() {
            return code;
        }

        String getExpansion() {
            return expansion;
        }
    }
}
