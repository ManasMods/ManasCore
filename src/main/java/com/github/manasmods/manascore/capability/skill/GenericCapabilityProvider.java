package com.github.manasmods.manascore.capability.skill;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class GenericCapabilityProvider<CAP extends INBTSerializable<CompoundTag>> implements ICapabilitySerializable<CompoundTag> {
    private final CAP defaultInstance;
    private final LazyOptional<CAP> data;
    private final Capability<CAP> capability;

    public GenericCapabilityProvider(Capability<CAP> capability, Supplier<CAP> defaultInstance) {
        this.defaultInstance = defaultInstance.get();
        this.data = LazyOptional.of(() -> this.defaultInstance);
        this.capability = capability;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return this.capability.orEmpty(cap, this.data);
    }


    @Override
    public CompoundTag serializeNBT() {
        return this.defaultInstance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.defaultInstance.deserializeNBT(nbt);
    }
}
