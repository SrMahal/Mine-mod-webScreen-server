package com.example;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.CreativeModeTab.Row;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class WebScreenBlocks {
   public static Block WEB_SCREEN_BLOCK;
   public static BlockItem WEB_SCREEN_ITEM;
   public static WebRemoteItem WEB_REMOTE_ITEM;
   public static BlockEntityType<WebScreenBlockEntity> WEB_SCREEN_BE;

   private WebScreenBlocks() {
   }

   public static void register() {
      Identifier identifier = Identifier.fromNamespaceAndPath("webscreen", "web_screen");
      ResourceKey<Block> resourcekey = ResourceKey.create(Registries.BLOCK, identifier);
      WEB_SCREEN_BLOCK = new WebScreenBlock(Properties.of().strength(1.0F).noOcclusion().setId(resourcekey));
      Registry.register(BuiltInRegistries.BLOCK, identifier, WEB_SCREEN_BLOCK);
      ResourceKey<Item> resourcekey1 = ResourceKey.create(Registries.ITEM, identifier);
      WEB_SCREEN_ITEM = new BlockItem(WEB_SCREEN_BLOCK, new net.minecraft.world.item.Item.Properties().useBlockDescriptionPrefix().setId(resourcekey1));
      Registry.register(BuiltInRegistries.ITEM, identifier, WEB_SCREEN_ITEM);
      Identifier identifier1 = Identifier.fromNamespaceAndPath("webscreen", "web_remote");
      ResourceKey<Item> resourcekey2 = ResourceKey.create(Registries.ITEM, identifier1);
      WEB_REMOTE_ITEM = new WebRemoteItem(new net.minecraft.world.item.Item.Properties().setId(resourcekey2).stacksTo(1));
      Registry.register(BuiltInRegistries.ITEM, identifier1, WEB_REMOTE_ITEM);
      WEB_SCREEN_BE = Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier, new BlockEntityType<>(WebScreenBlockEntity::new, Set.of(WEB_SCREEN_BLOCK))
      );
      CreativeModeTab creativemodetab = CreativeModeTab.builder(Row.TOP, 0)
         .title(Component.translatable("itemGroup.webscreen.main"))
         .icon(() -> new ItemStack(WEB_SCREEN_ITEM))
         .displayItems((params, output) -> {
            output.accept(WEB_SCREEN_ITEM);
            output.accept(WEB_REMOTE_ITEM);
         })
         .build();
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("webscreen", "main"), creativemodetab);
   }
}
