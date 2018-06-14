package de.uni_stuttgart.team18.blebeacon.beacon;

public class EddystoneBeacon {

    private static final EddystoneBeacon instance = new EddystoneBeacon();
    private String nameSpaceId;
    private String instanceId;
    private String url;
    private short voltage;
    private short temperature;
    private float distance;

    private EddystoneBeacon() {

    }

    public static EddystoneBeacon getInstance() {
        return instance;
    }

    public void update(byte[] data) {
        switch (data[0]) {
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

    private void updateUIDFrame(byte[] data) {
        nameSpaceId = toHex(data, 2, 10);
        instanceId = toHex(data, 12, 6);
    }

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
