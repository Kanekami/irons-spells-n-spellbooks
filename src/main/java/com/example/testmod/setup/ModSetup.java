package com.example.testmod.setup;

import com.example.testmod.capabilities.magic.MagicEvents;
import com.example.testmod.capabilities.scroll.ScrollDataEvents;
import com.example.testmod.capabilities.spellbook.SpellBookDataEvents;
import com.example.testmod.entity.MobSyncedCastingData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static com.example.testmod.capabilities.magic.SyncedSpellData.SYNCED_SPELL_DATA;

public class ModSetup {

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;

        //PLAYER
        //bus.addListener(ClientPlayerEvents::onPlayerTick); Firing for all players
        //bus.addListener(KeyMappings::onRegisterKeybinds);
//        bus.addListener(ClientPlayerEvents::onLivingEquipmentChangeEvent);
        //bus.addListener(ClientPlayerEvents::onPlayerRenderPre);
//        bus.addListener(ClientPlayerEvents::onLivingEntityUseItemEventStart);
//        bus.addListener(ClientPlayerEvents::onLivingEntityUseItemEventTick);
//        bus.addListener(ClientPlayerEvents::onLivingEntityUseItemEventFinish);

        //MANA
        bus.addGenericListener(Entity.class, MagicEvents::onAttachCapabilitiesPlayer);
        //bus.addListener(ManaEvents::onPlayerCloned);
        bus.addListener(MagicEvents::onRegisterCapabilities);
        bus.addListener(MagicEvents::onWorldTick);

        //SPELLBOOKS
        //bus.addGenericListener(ItemStack.class, SpellBookDataEvents::onAttachCapabilities);
        bus.addListener(SpellBookDataEvents::onRegisterCapabilities);

        //SCROLLS
        bus.addListener(ScrollDataEvents::onRegisterCapabilities);
        bus.addGenericListener(ItemStack.class, ScrollDataEvents::onAttachCapabilitiesItemStack);

    }

    public static void init(FMLCommonSetupEvent event) {
        Messages.register();
        EntityDataSerializers.registerSerializer(MobSyncedCastingData.MOB_SYNCED_CASTING_DATA);
        EntityDataSerializers.registerSerializer(SYNCED_SPELL_DATA);
    }

}