package org.enginehub.worldeditcui.fabric.network.mixin;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@SuppressWarnings({"unchecked", "rawtypes"})
@Pseudo
@Mixin(targets = "com.sk89q.worldedit.fabric.net.handler.WECUIPacketHandler")
public class WECUIPacketHandlerMixin {
    @ModifyArg(
            method = "init()V",
            at = @At(
                    value = "INVOKE:FIRST",
                    target = "net/fabricmc/fabric/api/networking/v1/PayloadTypeRegistry.register(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;Lnet/minecraft/network/codec/StreamCodec;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$TypeAndCodec;"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "net/fabricmc/fabric/api/networking/v1/PayloadTypeRegistry.playC2S()Lnet/fabricmc/fabric/api/networking/v1/PayloadTypeRegistry;"
                    )
            )
    )
    private static <T extends CustomPacketPayload> StreamCodec<? super RegistryFriendlyByteBuf, T> worldeditcui_protocol$injectClientsidePacketEncoder(StreamCodec<? super RegistryFriendlyByteBuf, T> original) {
        return StreamCodec.of(
                (StreamEncoder)CUIPacket.CODEC,
                (StreamDecoder)original
        );
    }

    @ModifyArg(
            method = "init()V",
            at = @At(
                    value = "INVOKE:FIRST",
                    target = "net/fabricmc/fabric/api/networking/v1/PayloadTypeRegistry.register(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;Lnet/minecraft/network/codec/StreamCodec;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$TypeAndCodec;"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "net/fabricmc/fabric/api/networking/v1/PayloadTypeRegistry.playS2C()Lnet/fabricmc/fabric/api/networking/v1/PayloadTypeRegistry;"
                    )
            )
    )
    private static <T extends CustomPacketPayload> StreamCodec<? super RegistryFriendlyByteBuf, T> worldeditcui_protocol$injectClientsidePacketDecoder(StreamCodec<? super RegistryFriendlyByteBuf, T> original) {
        return StreamCodec.of(
                (StreamEncoder)original,
                (StreamDecoder)CUIPacket.CODEC
        );
    }
}

