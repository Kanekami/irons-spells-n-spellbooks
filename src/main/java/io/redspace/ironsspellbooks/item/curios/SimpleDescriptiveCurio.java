package io.redspace.ironsspellbooks.item.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleDescriptiveCurio extends CurioBaseItem {
    final @Nullable String slotIdentifier;
    Style descriptionStyle;
    boolean showHeader;

    public SimpleDescriptiveCurio(Properties properties, String slotIdentifier) {
        super(properties);
        this.slotIdentifier = slotIdentifier;
        this.showHeader = true;
        descriptionStyle = Style.EMPTY.withColor(ChatFormatting.YELLOW);
    }

    public SimpleDescriptiveCurio(Properties properties) {
        this(properties, null);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, TooltipContext tooltipContext, ItemStack stack) {
        int i = tooltips.size();
        var attrTooltip = super.getAttributesTooltip(tooltips, tooltipContext, stack);
        boolean needHeader = attrTooltip.size() == i;
        var descriptionLines = getDescriptionLines(stack);
        if (needHeader && !descriptionLines.isEmpty()) {
            attrTooltip.add(Component.empty());
            attrTooltip.add(Component.translatable("curios.modifiers." + slotIdentifier).withStyle(ChatFormatting.GOLD));
        }
        attrTooltip.addAll(descriptionLines);

        return attrTooltip;
    }

    public List<Component> getDescriptionLines(ItemStack stack) {
        return List.of(getDescription(stack));
    }

    public Component getDescription(ItemStack stack) {
        return Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".desc")).withStyle(descriptionStyle);
    }
}
