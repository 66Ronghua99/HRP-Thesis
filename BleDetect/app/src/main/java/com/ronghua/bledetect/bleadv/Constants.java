package com.ronghua.bledetect.bleadv;

import android.os.ParcelUuid;

import java.util.UUID;

public class Constants {
    private final static ParcelUuid SIMPLE_BROADCAST = ParcelUuid.fromString(
            String.valueOf(UUID.nameUUIDFromBytes("0001".getBytes())));
    private final static ParcelUuid IMPERSONATION_CHALLENGE = ParcelUuid.fromString(
            String.valueOf(UUID.nameUUIDFromBytes("0010".getBytes())));
    private final static ParcelUuid CHALLENGE_REPLY = ParcelUuid.fromString(
            String.valueOf(UUID.nameUUIDFromBytes("0011".getBytes())));
    private final static ParcelUuid CERTIFICATE = ParcelUuid.fromString(
            String.valueOf(UUID.nameUUIDFromBytes("0100".getBytes())));
    private final static ParcelUuid MESSAGE_TYPE = ParcelUuid.fromString(
            String.valueOf(UUID.nameUUIDFromBytes("0000".getBytes())));

    public enum MessageType{
        Simple,
        Challenge,
        Reply,
        Certificate
    }
}
