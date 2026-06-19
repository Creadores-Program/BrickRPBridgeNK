package org.CreadoresProgram.rpbridge.utils;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ByteBufProvider{
    private static String Void_Str = "";
    public static void writeString(ByteBuf buffer, String s) throws IOException{
        if(s == null) {
            throw new IllegalArgumentException("String cannot be null!");
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if(bytes.length == 0){
            buffer.writeInt(0);
            return;
        }
        if(bytes.length > 32767) {
            throw new IOException("String too big (was " + s.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            buffer.writeInt(bytes.length);
            buffer.writeBytes(bytes);
        }
    }
    public static String readString(ByteBuf buffer) throws IOException{
        int len = buffer.readInt();
        if(len == 0){
            return Void_Str;
        }
        byte[] bytes = this.readBytes(buffer, len);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    public static byte[] readBytes(ByteBuf buffer, int length) throws IOException {
        if(length < 0) {
            throw new IllegalArgumentException("Array cannot have length less than 0.");
        }

        byte b[] = new byte[length];
        buffer.readBytes(b);
        return b;
    }
    public static int[] readInts(ByteBuf buffer, int length) throws IOException {
        if(length < 0) {
            throw new IllegalArgumentException("Array cannot have length less than 0.");
        }

        int i[] = new int[length];
        for(int index = 0; index < length; index++) {
            i[index] = buffer.readInt();
        }

        return i;
    }
}