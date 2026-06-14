package org.CreadoresProgram.rpbridge.block.utils;
import org.CreadoresProgram.rpbridge.block.RPBlocks;
public class RPBlocksUtils {
    public static boolean isSolid(byte block){
        return block > RPBlocks.AIR && block < RPBlocks.TRANSPARENT;
    }
    public static boolean isTransparent(byte block){
        return block > RPBlocks.SOLID_BLACK && block < RPBlocks.STEAM_WHITE;
    }
    public static boolean isSteam(byte block){
        return block == RPBlocks.AIR || (block > RPBlocks.TRANSPARENT_BLACK && block < RPBlocks.SLAB);
    }
    public static boolean isMix(byte block){
        return block > RPBlocks.STEAM_BLACK && block <= RPBlocks.WATER;
    }
    public static boolean isHide(){}
    public static byte traduceBlock(int bedrockId){}
}