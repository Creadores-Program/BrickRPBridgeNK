package org.CreadoresProgram.rpbridge.network.protocol;

public interface RPprotocolInfo{
    byte VERSION = 0x01;// Version BrickRPBridge Protocol

    byte LOGIN_SERVER = 0x01;
    byte CHAT = 0x02;
    byte MOVE = 0x03;
    byte FORM = 0x04;
    byte TITLE = 0x05;
    byte PING = 0x06;
    byte INVENTORY = 0x07; //unused
    byte INTERACT = 0x08; //Interact, Dramage
    byte BLOCK_UPDATE = 0x09;
    byte SPAWN_ENTITY = 0x10;
    byte UNSPAWN_ENTITY = 0x11;
    byte RESPAWN = 0x12;
    byte UPDATE_HEARTS = 0x13;
    byte UPDATE_NAME = 0x14;
    byte TRANSACTION_MANAGER = 0x15;
    byte SET_PLAYER_ID = 0x16;
    byte DISCONNECT_PLAYER = 0x17;
    byte TRANSFER_WORLD = 0x18;
}