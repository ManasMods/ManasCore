package com.github.manasmods.manascore.tab.annotation;

import com.github.manasmods.manascore.tab.AbstractInventoryTab;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScreenForTab {
    Class<? extends AbstractInventoryTab> value();
}
