/*
 * Copyright (c) 2025. ManasMods
 * GNU General Public License 3
 */

package io.github.manasmods.manascore.inventory.mixin;

import io.github.manasmods.manascore.inventory.InventoryTabRegistry;
import io.github.manasmods.manascore.inventory.api.AbstractInventoryTab;
import io.github.manasmods.manascore.inventory.api.InventoryTabScreen;
import io.github.manasmods.manascore.inventory.client.widget.InventoryTabSwitcherWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen {
    private InventoryTabSwitcherWidget widget = null;

    protected MixinAbstractContainerScreen(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        if (this instanceof InventoryTabScreen) {
            final Map<Integer, AbstractInventoryTab> tabRegistryEntries = InventoryTabRegistry.getEntries();
            this.widget = new InventoryTabSwitcherWidget((AbstractContainerScreen) (Object) this, (int) Math.round(Math.ceil(tabRegistryEntries.size() / 12F)));
            tabRegistryEntries.forEach(widget::addUpdateListener);
            widget.updateTabs();
            addRenderableWidget(widget);
        }
    }

    @Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderTransparentBackground(Lnet/minecraft/client/gui/GuiGraphics;)V", shift = At.Shift.AFTER))
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        if(this.widget != null) {
            this.widget.renderBackground(guiGraphics, i, j, f);
        }
    }
}
