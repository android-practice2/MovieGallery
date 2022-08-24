package com.bignerdranch.android.moviegallery.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteBufferUti {
    public static ByteBuffer str_to_bb(String msg) {
        Charset charset = StandardCharsets.UTF_8;
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

    public static String bb_to_str(ByteBuffer buffer) {
        Charset charset = StandardCharsets.UTF_8;
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        return new String(bytes, charset);
    }

}
