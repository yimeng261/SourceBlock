package com.sourceblock.compat;

import com.sourceblock.SourceBlockMod;
import com.sourceblock.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(SourceBlockMod.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 为牛奶源方块添加信息提示
        ItemStack milkSourceBlock = new ItemStack(ModItems.MILK_SOURCE_BLOCK.get());
        registration.addIngredientInfo(
            milkSourceBlock,
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.sourceblock.milk_source_block.info")
        );
        
        // 为水源方块添加信息提示
        ItemStack waterSourceBlock = new ItemStack(ModItems.WATER_SOURCE_BLOCK.get());
        registration.addIngredientInfo(
            waterSourceBlock,
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.sourceblock.water_source_block.info")
        );
        
        // 为岩浆源方块添加信息提示
        ItemStack lavaSourceBlock = new ItemStack(ModItems.LAVA_SOURCE_BLOCK.get());
        registration.addIngredientInfo(
            lavaSourceBlock,
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.sourceblock.lava_source_block.info")
        );
    }
}

