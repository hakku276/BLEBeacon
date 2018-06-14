package de.uni_stuttgart.team18.blebeacon;

import org.junit.Test;

import de.uni_stuttgart.team18.blebeacon.beacon.EddystoneBeacon;

import static org.junit.Assert.assertEquals;

public class EddystoneBeaconTest {

    //TLM only requires frame type, version, batt, batt, temp, temp
    private static final byte[] fakeTLMData = {0x20, 0x00, 0x00, 0x01, 0x01, 0x00};

    private static final byte[] fakeURLData = {
            0x10,  // Frame type: URL
            (byte) 0xF8, // Power
            0x03, // https://
            'g',
            'o',
            'o',
            0x00, // .com/
            'a',
            '0',
            'm',
            'n',
            's',
            'S',
    };

    private static final String urlData = "https://goo.com/a0mnsS";

    @Test
    public void testTLMFrameUpdate_batteryAndTempShouldBothBe1() {
        EddystoneBeacon.getInstance().update(fakeTLMData, 1);
        assertEquals(1, EddystoneBeacon.getInstance().getVoltage());
        assertEquals(1.0, EddystoneBeacon.getInstance().getTemperature(), 0);
    }

    @Test
    public void testURLFrameUpdate_urlShouldbeEqualToURLData(){
        EddystoneBeacon.getInstance().update(fakeURLData, 10);
        assertEquals(urlData, EddystoneBeacon.getInstance().getUrl());
    }

}
