package com.example.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class AddonTemplate extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Addon Template");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Template");
        Modules.get().add(new AutoGuttaModule());
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }
}

class AutoGuttaModule extends Module {
    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final SettingGroup sgAutoFix = settings.createGroup("Auto Fix Settings");
    private final SettingGroup sgAutoRtp = settings.createGroup("Auto RTP Settings");

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
            .name("Command")
            .defaultValue("/home")
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("Delay")
            .defaultValue(5)
            .min(0)
            .sliderMax(100)
            .build()
    );

    private final Setting<Integer> fullStacksThreshold = sgGeneral.add(new IntSetting.Builder()
            .name("Full Stacks Threshold")
            .defaultValue(27)
            .min(1)
            .sliderMax(36)
            .build()
    );

    private final Setting<List<Item>> itemsToDrop = sgGeneral.add(new ItemListSetting.Builder()
            .name("Items To Drop")
            .build()
    );

    private final Setting<Boolean> enableFix = sgAutoFix.add(new BoolSetting.Builder()
            .name("Enable Fix")
            .defaultValue(true)
            .build()
    );

    private final Setting<String> fixCommand = sgAutoFix.add(new StringSetting.Builder()
            .name("Fix Command")
            .defaultValue("/fix all")
            .build()
    );

    private final Setting<Double> fixThreshold = sgAutoFix.add(new DoubleSetting.Builder()
            .name("Fix Threshold")
            .defaultValue(15.000)
            .min(1.0)
            .sliderMax(100.0)
            .build()
    );

    private final Setting<Boolean> enableRtp = sgAutoRtp.add(new BoolSetting.Builder()
            .name("Enable Rtp")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> rtpSlotId = sgAutoRtp.add(new IntSetting.Builder()
            .name("Rtp Slot Id")
            .defaultValue(15)
            .min(0)
            .sliderMax(44)
            .build()
    );

    private final Setting<Integer> rtpDelayTicks = sgAutoRtp.add(new IntSetting.Builder()
            .name("Rtp Delay Ticks")
            .defaultValue(5)
            .min(0)
            .sliderMax(100)
            .build()
    );

    private int fixTimer = 0;
    private int rtpTimer = 0;

    public AutoGuttaModule() {
        super(Modules.CATEGORY_MISC, "AutoGutta", "Auto warps, fixes tools, drops items, and executes auto-RTP.");
    }

    @Override
    public void onActivate() {
        fixTimer = 0;
        rtpTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null) return;

        int occupiedSlots = 0;
        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                occupiedSlots++;
                if (itemsToDrop.get().contains(stack.getItem())) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.THROW, mc.player);
                }
            }
        }

        if (occupiedSlots >= fullStacksThreshold.get()) {
            ChatUtils.sendPlayerMsg(command.get());
        }

        if (enableFix.get()) {
            ItemStack mainHand = mc.player.getMainHandStack();
            if (mainHand.isDamageable()) {
                double currentDurability = mainHand.getMaxDamage() - mainHand.getDamage();
                if (currentDurability <= fixThreshold.get()) {
                    if (fixTimer >= delay.get()) {
                        ChatUtils.sendPlayerMsg(fixCommand.get());
                        fixTimer = 0;
                    } else {
                        fixTimer++;
                    }
                }
            }
        }

        if (enableRtp.get()) {
            ItemStack rtpStack = mc.player.currentScreenHandler.getSlot(rtpSlotId.get()).getStack();
            if (!rtpStack.isEmpty()) {
                if (rtpTimer >= rtpDelayTicks.get()) {
                    ChatUtils.sendPlayerMsg("/rtp");
                    rtpTimer = 0;
                } else {
                    rtpTimer++;
                }
            }
        }
    }
}
