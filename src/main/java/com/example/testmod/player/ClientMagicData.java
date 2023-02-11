package com.example.testmod.player;

import com.example.testmod.capabilities.magic.PlayerCooldowns;
import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.capabilities.spellbook.SpellBookData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.TeleportSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;

public class ClientMagicData {
    /**
     * COOLDOWNS
     *************************/
    private static final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    public static PlayerCooldowns getCooldowns() {
        return ClientMagicData.playerCooldowns;
    }

    public static float getCooldownPercent(SpellType spellType) {
        return playerCooldowns.getCooldownPercent(spellType);
    }


    /**
     * MANA
     *************************/
    private static int playerMana;

    public static int getPlayerMana() {
        return playerMana;
    }

    public static void setMana(int playerMana) {
        ClientMagicData.playerMana = playerMana;
    }


    /**
     * CASTING
     *************************/
    public static boolean isCasting = false;
    public static int castDurationRemaining = 0;
    public static int castDuration = 0;
    public static CastType castType = CastType.NONE;
    public static boolean supressRightClicks;

    public static float getCastCompletionPercent() {
        return 1 - (castDurationRemaining / (float) castDuration);
    }

    public static void resetClientCastState(){
        isCasting = false;
        castDurationRemaining = 0;
        castDuration = 0;
        castType = CastType.NONE;

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem()) {
            Minecraft.getInstance().player.stopUsingItem();
        }
    }

    /**
     * SPIN ATTACK
     *************************/
    public static SpinAttackType lastSpinAttack = SpinAttackType.RIPTIDE;

    public enum SpinAttackType {
        RIPTIDE,
        FIRE
    }


    /**
     * SPELL BAR RENDER CACHING
     *************************/
    public static List<Vec2> relativeSpellBarSlotLocations = Lists.newArrayList();

    public static void generateRelativeLocations(SpellBookData spellBookData, int boxSize, int spriteSize) {
        relativeSpellBarSlotLocations.clear();
        if (spellBookData == null)
            return;
        int spellCount = spellBookData.getSpellSlots();

        int[] row1 = new int[SPELL_LAYOUT[spellCount - 1][0]];
        int[] row2 = new int[SPELL_LAYOUT[spellCount - 1][1]];
        int[] row3 = new int[SPELL_LAYOUT[spellCount - 1][2]];

        int[] rowWidth = {
                boxSize * row1.length,
                boxSize * row2.length,
                boxSize * row3.length
        };
        int[] rowHeight = {
                row1.length > 0 ? boxSize : 0,
                row2.length > 0 ? boxSize : 0,
                row3.length > 0 ? boxSize : 0
        };

        int[][] display = {row1, row2, row3};
        int overallHeight = rowHeight[0] + rowHeight[1] + rowHeight[2];
        for (int row = 0; row < display.length; row++) {
            for (int column = 0; column < display[row].length; column++) {
                int offset = -rowWidth[row] / 2;
                Vec2 location = new Vec2(offset + column * boxSize, (row) * boxSize - (overallHeight / 2));
                location.add(-spriteSize / 2);
                relativeSpellBarSlotLocations.add(location);
            }
        }
    }

    /**
     * HELPER
     *************************/
    public static final int[][] SPELL_LAYOUT = {
            {1, 0, 0}, //1
            {2, 0, 0}, //2
            {2, 1, 0}, //3
            {2, 2, 0}, //4
            {3, 2, 0}, //5
            {3, 3, 0}, //6
            {4, 3, 0}, //7
            {4, 4, 0}, //8
            {3, 3, 3}, //9
            {3, 4, 3}, //10
            {4, 4, 3}, //11
            {4, 4, 4}, //12
            {4, 5, 4}, //13
            {5, 5, 4}, //14
            {5, 5, 5}  //15
    };

    /**
     * Player Synced Data
     */
    private static HashMap<Integer, SyncedSpellData> playerSyncedDataLookup = new HashMap<>();

    public static SyncedSpellData getPlayerSyncedData(int serverPlayerId) {
        return playerSyncedDataLookup.getOrDefault(serverPlayerId, new SyncedSpellData(-1));
    }


    /**
     * Network Handling Wrapper
     */
    public static void handleClientboundOnClientCast(int spellId, int level, CastSource castSource) {
        var spell = AbstractSpell.getSpell(spellId, level);
        //var player = Minecraft.getInstance().player;
        spell.onClientCastComplete(Minecraft.getInstance().player.level, Minecraft.getInstance().player, null);
    }

    public static void handlePlayerSyncedData(SyncedSpellData playerSyncedData) {
        playerSyncedDataLookup.put(playerSyncedData.getServerPlayerId(), playerSyncedData);
    }

    public static void handleClientboundTeleport(Vec3 pos1, Vec3 pos2) {
        TeleportSpell.particleCloud(Minecraft.getInstance().player.level, Minecraft.getInstance().player, pos1);
        TeleportSpell.particleCloud(Minecraft.getInstance().player.level, Minecraft.getInstance().player, pos2);
    }

}