package com.bittoo.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Utils {

    public static UUID convertBytesToUUID(byte[] bytes) {
        var byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }
}
