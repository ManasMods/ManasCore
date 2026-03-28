package io.github.manasmods.manascore.architectury.mixin.architectury.impl;

import dev.architectury.impl.NetworkAggregator;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.PacketTransformer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(NetworkAggregator.class)
public class MixinNetworkAggregator {
    @Shadow @Final @Mutable
    public static Map<ResourceLocation, CustomPacketPayload.Type<NetworkAggregator.BufCustomPacketPayload>> C2S_TYPE;
    @Shadow @Final @Mutable
    public static Map<ResourceLocation, CustomPacketPayload.Type<NetworkAggregator.BufCustomPacketPayload>> S2C_TYPE;
    @Shadow @Final @Mutable
    public static Map<ResourceLocation, NetworkManager.NetworkReceiver<?>> C2S_RECEIVER;
    @Shadow @Final @Mutable
    public static Map<ResourceLocation, NetworkManager.NetworkReceiver<?>> S2C_RECEIVER;
    @Shadow @Final @Mutable
    public static Map<ResourceLocation, StreamCodec<ByteBuf, ?>> C2S_CODECS;
    @Shadow @Final @Mutable
    public static Map<ResourceLocation, StreamCodec<ByteBuf, ?>> S2C_CODECS;
    @Shadow @Final @Mutable
    public static Map<ResourceLocation, PacketTransformer> C2S_TRANSFORMERS;
    @Shadow @Final @Mutable
    public static Map<ResourceLocation, PacketTransformer> S2C_TRANSFORMERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClassInit(CallbackInfo ci) {
        C2S_TYPE = new ConcurrentHashMap<>();
        S2C_TYPE = new ConcurrentHashMap<>();
        C2S_RECEIVER = new ConcurrentHashMap<>();
        S2C_RECEIVER = new ConcurrentHashMap<>();
        C2S_CODECS = new ConcurrentHashMap<>();
        S2C_CODECS = new ConcurrentHashMap<>();
        C2S_TRANSFORMERS = new ConcurrentHashMap<>();
        S2C_TRANSFORMERS = new ConcurrentHashMap<>();
    }
}
