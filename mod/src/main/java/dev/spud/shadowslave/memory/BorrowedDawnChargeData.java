package dev.spud.shadowslave.memory;

import com.mojang.serialization.Codec;

/** Persistent Java-owned payload for the authored Borrowed Dawn first_light enchantment. */
public record BorrowedDawnChargeData(boolean charged) {
    public static final Codec<BorrowedDawnChargeData> CODEC = Codec.BOOL.xmap(BorrowedDawnChargeData::new, BorrowedDawnChargeData::charged);

    public static BorrowedDawnChargeData empty() {
        return new BorrowedDawnChargeData(false);
    }

    public static BorrowedDawnChargeData storedLight() {
        return new BorrowedDawnChargeData(true);
    }
}
