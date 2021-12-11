package dev.tomat.constellar.content.gui.resourcepack.buttons;

import dev.tomat.constellar.content.gui.GuiUtils;
import dev.tomat.constellar.content.gui.resourcepack.ResourcePackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class RefreshButton extends GuiButton {
    public RefreshButton(int buttonID, int xPos, int yPos)
    {
        super(buttonID, xPos, yPos, GuiUtils.DefaultButtonHeight, GuiUtils.DefaultButtonHeight, "");
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (visible) {
            mc.getTextureManager().bindTexture(ResourcePackUtils.ResourcePackTextures);
            GuiUtils.resetColor();


            int spritesheetColumn = 5;
            boolean hovering = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

            if (hovering)
                spritesheetColumn++;

            drawTexturedModalRect(xPosition, yPosition, 0, ResourcePackUtils.OpenFolderIconSize * spritesheetColumn, width, height);
        }
    }
}
