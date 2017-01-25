package com.example.android.bluetoothchat.utils;

import java.util.ArrayList;

/**
 * Created by GyungDal on 2017-01-04.
 */

public class SignalBuilder {
    private static char count;

    static{
        count = 0x00;
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte)(s & 0x00FF),(byte)((s & 0xFF00)>>8)};
    }

    public static byte[] move(byte[] motion){
        ArrayList<Character> bytes = new ArrayList<Character>();
        //STX
        bytes.add((char)0x02);

        //코드
        bytes.add((char)100);

        //카운터
        bytes.add(count++);

        //data
        for (byte m: motion) {
            bytes.add((char)m);
        }

        //CRC
        short c = CRC16.cal(motion);
        byte[] crc = shortToBytes(c);
        bytes.add((char)crc[0]);
        bytes.add((char)crc[1]);

        //Spare
        for(int i = 0;i<8;i++)
            bytes.add((char)0x00);

        //ETX
        bytes.add((char)0x03);
        byte[] result = new byte[bytes.size()];
        for(int i = 0;i<bytes.size();i++){
            char tt = bytes.get(i);
            result[i] = (byte)tt;
        }
        return result;
    }
}
