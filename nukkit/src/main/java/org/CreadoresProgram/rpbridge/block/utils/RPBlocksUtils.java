package org.CreadoresProgram.rpbridge.block.utils;

import org.CreadoresProgram.rpbridge.block.RPBlocks;

import cn.nukkit.level.Level;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.Block;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

public class RPBlocksUtils {
    public static boolean isSolid(byte block){
        return (block > RPBlocks.AIR && block < RPBlocks.TRANSPARENT) || isLigth(block);
    }
    public static boolean isTransparent(byte block){
        return block > RPBlocks.SOLID_BLACK && block < RPBlocks.STEAM_WHITE;
    }
    public static boolean isSteam(byte block){
        return block == RPBlocks.AIR || (block > RPBlocks.TRANSPARENT_BLACK && block < RPBlocks.SLAB);
    }
    public static boolean isMix(byte block){
        return block > RPBlocks.STEAM_BLACK && block < RPBlocks.LIGTH_WHITE;
    }
    public static boolean isLigth(byte block){
        return block > 59 && block <= RPBlocks.LIGTH_BLACK;
    }
    public static boolean isHide(Level level, int x, int y, int z){
        if (y <= 0 || y >= 250) return false;
        return isSolid(traduceBlock(level.getBlock(x + 1, y, z)))
            && isSolid(traduceBlock(level.getBlock(x - 1, y, z)))
            && isSolid(traduceBlock(level.getBlock(x, y + 1, z)))
            && isSolid(traduceBlock(level.getBlock(x, y - 1, z)))
            && isSolid(traduceBlock(level.getBlock(x, y, z + 1)))
            && isSolid(traduceBlock(level.getBlock(x, y, z - 1)));
    }
    public static byte traduceBlock(Block block){
        int bedrockId = block.getId();
        switch(bedrockId){
            case BlockID.STAINED_GLASS:
            case BlockID.STAINED_GLASS_PANE:
                return getColorByMetaTransparent(block.getDamage());

            case BlockID.SHULKER_BOX:
            case BlockID.WOOL:
            case BlockID.CONCRETE:
            case BlockID.CONCRETE_POWDER:
            case BlockID.CARPET:
                return getColorByMetaSolid(block.getDamage());
            case BlockID.AIR:
                return RPBlocks.AIR;
            case BlockID.WHITE_GLAZED_TERRACOTTA:
            case BlockID.QUARTZ_BLOCK:
            case BlockID.IRON_BLOCK:
                return RPBlocks.SOLID_WHITE;
            case BlockID.ORANGE_GLAZED_TERRACOTTA:
            case BlockID.PUMPKIN:
            case BlockID.CARVED_PUMPKIN:
                return RPBlocks.SOLID_ORANGE;

            case BlockID.MAGENTA_GLAZED_TERRACOTTA:
                return RPBlocks.SOLID_MAGENTA;

            case BlockID.LIGHT_BLUE_GLAZED_TERRACOTTA:
                return RPBlocks.SOLID_LIGHT_BLUE;

            case BlockID.YELLOW_GLAZED_TERRACOTTA:
            case BlockID.GOLD_BLOCK:
            case BlockID.SPONGE:
                return RPBlocks.SOLID_YELLOW;

            case BlockID.LIME_GLAZED_TERRACOTTA:
                return RPBlocks.SOLID_LIME;

            case BlockID.PINK_GLAZED_TERRACOTTA:
            case BlockID.CHERRY_PLANKS:
            case BlockID.CHERRY_LOG:
            case BlockID.CHERRY_WOOD:
                return RPBlocks.SOLID_PINK;
            case BlockID.SILVER_GLAZED_TERRACOTTA:
            case BlockID.ANVIL:
                return RPBlocks.SOLID_LIGHT_GRAY;

            case BlockID.CYAN_GLAZED_TERRACOTTA:
            case BlockID.PRISMARINE:
                return RPBlocks.SOLID_CYAN;

            case BlockID.PURPLE_GLAZED_TERRACOTTA:
            case BlockID.PURPUR_BLOCK:
            case BlockID.MYCELIUM:
                return RPBlocks.SOLID_DWELLING;

            case BlockID.BLUE_GLAZED_TERRACOTTA:
            case BlockID.LAPIS_BLOCK:
            case BlockID.DIAMOND_BLOCK:
            case BlockID.BLUE_ICE:
                return RPBlocks.SOLID_BLUE;

            case BlockID.DIRT:
            case BlockID.GRASS_BLOCK:
            case BlockID.PLANK:
            case BlockID.LOG:
            case BlockID.BROWN_GLAZED_TERRACOTTA:
            case BlockID.BOOKSHELF:
            case BlockID.MUD:
            case BlockID.MUD_BRICKS:
                return RPBlocks.SOLID_BROWN;

            case BlockID.GREEN_GLAZED_TERRACOTTA:
            case BlockID.EMERALD_BLOCK:
            case BlockID.MOSS_BLOCK:
                return RPBlocks.SOLID_GREEN;

            case BlockID.RED_GLAZED_TERRACOTTA:
            case BlockID.REDSTONE_BLOCK:
            case BlockID.TNT:
            case BlockID.BRICKS:
            case BlockID.NETHERRACK:
                return RPBlocks.SOLID_RED;

            case BlockID.BLACK_GLAZED_TERRACOTTA:
            case BlockID.OBSIDIAN:
            case BlockID.GLOWING_OBSIDIAN:
            case BlockID.COAL_BLOCK:
            case BlockID.BLACKSTONE:
            case BlockID.NETHERITE_BLOCK:
                return RPBlocks.SOLID_BLACK;
            case BlockID.GLASS:
            case BlockID.BARRIER:
            case BlockID.INVISIBLE_BEDROCK:
                return RPBlocks.TRANSPARENT;
            case BlockID.GLASS_PANE:
                return RPBlocks.TRANSPARENT_WHITE;

            case BlockID.LEAVES:
            case BlockID.LEAVES2:
            case BlockID.MANGROVE_LEAVES:
            case BlockID.AZALEA_LEAVES:
            case BlockID.AZALEA_LEAVES_FLOWERED:
                return RPBlocks.TRANSPARENT_GREEN;

            case BlockID.CHERRY_LEAVES:
                return RPBlocks.TRANSPARENT_PINK;

            case BlockID.ICE:
            case BlockID.PACKED_ICE:
            case BlockID.FROSTED_ICE:
                return RPBlocks.TRANSPARENT_LIGHT_BLUE;

            case BlockID.SEA_LANTERN:
            case BlockID.BEACON:
                return RPBlocks.LIGTH_WHITE;

            case BlockID.LIT_PUMPKIN:
            case BlockID.MAGMA:
                return RPBlocks.LIGTH_ORANGE;

            case BlockID.GLOWSTONE:
            case BlockID.SHROOMLIGHT:
            case BlockID.OCHRE_FROGLIGHT:
            case BlockID.TORCH:
            case BlockID.LANTERN:
                return RPBlocks.LIGTH_YELLOW;

            case BlockID.VERDANT_FROGLIGHT:
                return RPBlocks.LIGTH_LIME;

            case BlockID.PEARLESCENT_FROGLIGHT:
                return RPBlocks.LIGTH_PINK;

            case BlockID.BLUE_ICE:
            case BlockID.SOUL_TORCH:
            case BlockID.SOUL_LANTERN:
                return RPBlocks.LIGTH_BLUE;

            case BlockID.LIT_REDSTONE_LAMP:
            case BlockID.REDSTONE_LAMP:
                return RPBlocks.LIGTH_RED;

            case BlockID.CRYING_OBSIDIAN:
                return RPBlocks.LIGTH_DWELLING;
            case BlockID.FIRE:
            case BlockID.SOUL_FIRE:
            case BlockID.CAMPFIRE_BLOCK:
            case BlockID.SOUL_CAMPFIRE_BLOCK:
                return RPBlocks.STEAM_ORANGE;
            case BlockID.DANDELION:
                return RPBlocks.STEAM_YELLOW;
            case BlockID.POPPY:
            case BlockID.WITHER_ROSE:
                return RPBlocks.STEAM_RED;
            case BlockID.TALL_GRASS:
            case BlockID.DEAD_BUSH:
            case BlockID.VINE:
            case BlockID.SUGARCANE_BLOCK:
            case BlockID.SEAGRASS:
                return RPBlocks.STEAM_GREEN;
            case BlockID.PINK_PETALS:
                return RPBlocks.STEAM_PINK;
            case BlockID.SLAB:
            case BlockID.WOOD_SLAB:
            case BlockID.RED_SANDSTONE_SLAB:
            case BlockID.MUD_BRICK_SLAB:
            case BlockID.CRIMSON_SLAB:
            case BlockID.WARPED_SLAB:
            case BlockID.TUFF_SLAB:
                return RPBlocks.SLAB;

            case BlockID.LAVA:
            case BlockID.STILL_LAVA:
            case BlockID.LAVA_CAULDRON:
                return RPBlocks.LAVA;

            case BlockID.WATER:
            case BlockID.STILL_WATER:
            case BlockID.BUBBLE_COLUMN:
                return RPBlocks.WATER;
            /*case BlockID.STONE:
            case BlockID.COBBLESTONE:
            case BlockID.BEDROCK:
            case BlockID.STONE_BRICKS:
            case BlockID.ANDESITE_STAIRS:
            case BlockID.GRAY_GLAZED_TERRACOTTA:
            case BlockID.DEEPSLATE:
            case BlockID.COBBLED_DEEPSLATE:*/
            default:
                return RPBlocks.SOLID_GREY;
                
        }
    }
    public static byte getColorByMetaSolid(int meta){
        switch (meta) {
            case 0:  return RPBlocks.SOLID_WHITE;
            case 1:  return RPBlocks.SOLID_ORANGE;
            case 2:  return RPBlocks.SOLID_MAGENTA;
            case 3:  return RPBlocks.SOLID_LIGHT_BLUE;
            case 4:  return RPBlocks.SOLID_YELLOW;
            case 5:  return RPBlocks.SOLID_LIME;
            case 6:  return RPBlocks.SOLID_PINK;
            case 7:  return RPBlocks.SOLID_GREY;
            case 8:  return RPBlocks.SOLID_LIGHT_GRAY;
            case 9:  return RPBlocks.SOLID_CYAN;
            case 10: return RPBlocks.SOLID_DWELLING;
            case 11: return RPBlocks.SOLID_BLUE;
            case 12: return RPBlocks.SOLID_BROWN;
            case 13: return RPBlocks.SOLID_GREEN;
            case 14: return RPBlocks.SOLID_RED;
            case 15: return RPBlocks.SOLID_BLACK;
            default: return RPBlocks.SOLID_WHITE;
        }
    }
    public static byte getColorByMetaTransparent(int meta){
        switch (meta) {
            case 0:  return RPBlocks.TRANSPARENT_WHITE;
            case 1:  return RPBlocks.TRANSPARENT_ORANGE;
            case 2:  return RPBlocks.TRANSPARENT_MAGENTA;
            case 3:  return RPBlocks.TRANSPARENT_LIGHT_BLUE;
            case 4:  return RPBlocks.TRANSPARENT_YELLOW;
            case 5:  return RPBlocks.TRANSPARENT_LIME;
            case 6:  return RPBlocks.TRANSPARENT_PINK;
            case 7:  return RPBlocks.TRANSPARENT_GREY;
            case 8:  return RPBlocks.TRANSPARENT_LIGHT_GRAY;
            case 9:  return RPBlocks.TRANSPARENT_CYAN;
            case 10: return RPBlocks.TRANSPARENT_DWELLING;
            case 11: return RPBlocks.TRANSPARENT_BLUE;
            case 12: return RPBlocks.TRANSPARENT_BROWN;
            case 13: return RPBlocks.TRANSPARENT_GREEN;
            case 14: return RPBlocks.TRANSPARENT_RED;
            case 15: return RPBlocks.TRANSPARENT_BLACK;
            default: return RPBlocks.TRANSPARENT_WHITE;
        }
    }
    public static byte[] getWorldData(Level level, int minPosX, int maxPosX, int minPosZ, int maxPosZ){
        int minChunkX = minPosX >> 4;
        int maxChunkX = maxPosX >> 4;
        int minChunkZ = minPosZ >> 4;
        int maxChunkZ = maxPosZ >> 4;
        int chunksLoaded = 0;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!level.isChunkLoaded(cx, cz)) {
                    level.loadChunk(cx, cz, true); 
                    chunksLoaded++;
                }
            }
        }
        ByteBuf buffer = Unpooled.buffer();
        try{
            int indexLen = buffer.writerIndex();
            buffer.writeInt(0);
            int blockCount = 0;
            for(int currentX = minPosX; currentX <= maxPosX; currentX++){
                for(int currentY = 0; currentY <= 250; currentY++){
                    for(int currentZ = minPosZ; currentZ <= maxPosZ; currentZ++){
                        if(RPBlocksUtils.isHide(level, currentX, currentY, currentZ)){
                            continue;
                        }
                        Block blockB = level.getBlock(currentX, currentY, currentZ);
                        byte blockRP = RPBlocksUtils.traduceBlock(blockB);
                        buffer.writeShort(currentX);
                        buffer.writeByte(currentY);
                        buffer.writeShort(currentZ);
                        buffer.writeByte(blockRP);
                        blockCount++;
                    }
                }
            }
            buffer.setInt(indexLen, blockCount);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gos = new GZIPOutputStream(baos)) {
                byte[] rawBytes = new byte[buffer.readableBytes()];
                buffer.readBytes(rawBytes);
                gos.write(rawBytes);
                gos.finish();
            }
            return baos.toByteArray();
        }catch(Exception er){
            er.printStackTrace();
            return new byte[0];
        }finally{
            if (buffer.refCnt() > 0) buffer.release();
        }
    }
}