package dev.tomat.constellar.content.gui.resourcepack;

import com.google.common.collect.Lists;
import dev.tomat.common.utils.ColorUtils;
import dev.tomat.constellar.content.gui.GuiUtils;
import dev.tomat.constellar.content.gui.resourcepack.buttons.OpenFolderButton;
import dev.tomat.constellar.content.gui.resourcepack.buttons.RefreshButton;
import dev.tomat.constellar.content.gui.resourcepack.buttons.ResourcePackButtonId;
import dev.tomat.constellar.content.gui.resourcepack.entries.ResourcePackEntryConstellar;
import dev.tomat.constellar.content.gui.resourcepack.entries.ResourcePackEntryDefault;
import dev.tomat.constellar.content.gui.resourcepack.entries.ResourcePackEntryFound;
import dev.tomat.constellar.content.gui.resourcepack.panels.AvailableResourcePackPanel;
import dev.tomat.constellar.content.gui.resourcepack.panels.SelectedResourcePackPanel;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ResourcePackRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ResourcePackScreen extends GuiScreen {

    protected final GuiScreen parentScreen;

    protected List<ResourcePackEntry> availableResourcePacks;
    protected List<ResourcePackEntry> selectedResourcePacks;

    protected AvailableResourcePackPanel availableResourcePackPanel;
    protected SelectedResourcePackPanel selectedResourcePackPanel;

    protected boolean changed = false;

    public ResourcePackScreen(GuiScreen parentScreenIn)
    {
        parentScreen = parentScreenIn;
    }

    public void initGui()
    {
        int buttonYPos = height - ResourcePackUtils.ResourcePackButtonsBottomPadding - GuiUtils.DefaultButtonHeight;

        // button id, x, y, text
        buttonList.add(new GuiOptionButton(ResourcePackButtonId.Done,
                (width / 2) - (GuiUtils.DefaultGuiOptionsButtonWidth / 2),
                buttonYPos,
                I18n.format("gui.done")
        ));

        buttonList.add(new OpenFolderButton(ResourcePackButtonId.OpenFolder,
                (width / 2) - (GuiUtils.DefaultGuiOptionsButtonWidth / 2) - ResourcePackUtils.ResourcePackDoneButtonSidePadding,
                buttonYPos
        ));

        buttonList.add(new RefreshButton(ResourcePackButtonId.OpenFolder,
                (width / 2) + (GuiUtils.DefaultGuiOptionsButtonWidth / 2) + ResourcePackUtils.ResourcePackDoneButtonSidePadding,
                buttonYPos
        ));

        if (!changed)
        {
            availableResourcePacks = Lists.newArrayList();
            selectedResourcePacks = Lists.newArrayList();

            ResourcePackRepository resourcePackRepository = mc.getResourcePackRepository();
            resourcePackRepository.updateRepositoryEntriesAll();

            List<ResourcePackRepository.Entry> resourcePacks = Lists.newArrayList(resourcePackRepository.getRepositoryEntriesAll());
            resourcePacks.removeAll(resourcePackRepository.getRepositoryEntries());

            for (ResourcePackRepository.Entry resourcePack : resourcePacks)
            {
                availableResourcePacks.add(new ResourcePackEntryFound(this, resourcePack));
            }

            for (ResourcePackRepository.Entry resourcePack : Lists.reverse(resourcePackRepository.getRepositoryEntries()))
            {
                selectedResourcePacks.add(new ResourcePackEntryFound(this, resourcePack));
            }

            selectedResourcePacks.add(new ResourcePackEntryConstellar(this));
            selectedResourcePacks.add(new ResourcePackEntryDefault(this));
        }

        // pack coords based on top left of panel, without width padding
        availableResourcePackPanel = new AvailableResourcePackPanel(mc,
                ResourcePackUtils.ResourcePackEntryWidth,
                // use epic helper method
                ResourcePackUtils.getResourcePackPanelHeight(height),
                // same xPos as the xPos of the icon. left.
                (width / 2) - (ResourcePackUtils.PaddingBetweenResourcePackPanels / 2) - (ResourcePackUtils.ResourcePackPanelPadding / 2) - ResourcePackUtils.ResourcePackEntryWidth,
                availableResourcePacks
        );
        // basically x with padding
        availableResourcePackPanel.setSlotXBoundsFromLeft((width / 2) - (ResourcePackUtils.PaddingBetweenResourcePackPanels / 2) - (ResourcePackUtils.ResourcePackPanelPadding) - ResourcePackUtils.ResourcePackEntryWidth);

        selectedResourcePackPanel = new SelectedResourcePackPanel(mc,
                ResourcePackUtils.ResourcePackEntryWidth,
                // use epic helper method
                ResourcePackUtils.getResourcePackPanelHeight(height),
                // same xPos as the xPos of the icon. left.
                // whole screen, half screen, then add half the padding between the sides, then add the panel padding (4) to get the icon's X
                (width / 2) + (ResourcePackUtils.PaddingBetweenResourcePackPanels / 2) + (ResourcePackUtils.ResourcePackPanelPadding / 2),
                selectedResourcePacks
        );
        // basically just x with padding lol
        selectedResourcePackPanel.setSlotXBoundsFromLeft((width / 2) + (ResourcePackUtils.PaddingBetweenResourcePackPanels / 2));
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        selectedResourcePackPanel.handleMouseInput();
        availableResourcePackPanel.handleMouseInput();
    }

    public boolean hasResourcePackEntry(ResourcePackEntry resourcePack)
    {
        return selectedResourcePacks.contains(resourcePack);
    }

    public List<ResourcePackEntry> getListContaining(ResourcePackEntry resourcePack)
    {
        return hasResourcePackEntry(resourcePack) ? selectedResourcePacks : availableResourcePacks;
    }

    public List<ResourcePackEntry> getAvailableResourcePackPanel()
    {
        return availableResourcePacks;
    }

    public List<ResourcePackEntry> getSelectedResourcePackPanel()
    {
        return selectedResourcePacks;
    }

    protected void actionPerformed(GuiButton button) {
        if (!button.enabled)
            return;

        if (button.id == ResourcePackButtonId.Done) {
            if (changed) {
                List<ResourcePackRepository.Entry> packs = Lists.newArrayList();

                for (ResourcePackEntry selectedPack : selectedResourcePacks) {
                    if (selectedPack instanceof ResourcePackEntryFound) {
                        packs.add(((ResourcePackEntryFound)selectedPack).getResourcePack());
                    }
                }

                Collections.reverse(packs);

                mc.getResourcePackRepository().setRepositories(packs);
                mc.gameSettings.resourcePacks.clear();
                mc.gameSettings.incompatibleResourcePacks.clear();

                for (ResourcePackRepository.Entry pack : packs)
                {
                    mc.gameSettings.resourcePacks.add(pack.getResourcePackName());

                    // compatibility
                    if (pack.func_183027_f() != 1)
                    {
                        mc.gameSettings.incompatibleResourcePacks.add(pack.getResourcePackName());
                    }
                }

                mc.gameSettings.saveOptions();
                mc.refreshResources();
            }

            // return to screen beforehand todo: make sure to make this configurable
            mc.displayGuiScreen(parentScreen);
            /*
            if (button.id == 2)
            {
                File file1 = this.mc.getResourcePackRepository().getDirResourcepacks();
                String s = file1.getAbsolutePath();

                if (Util.getOSType() == Util.EnumOS.OSX)
                {
                    try
                    {
                        logger.info(s);
                        Runtime.getRuntime().exec(new String[] {"/usr/bin/open", s});
                        return;
                    }
                    catch (IOException ioexception1)
                    {
                        logger.error("Couldn't open file", ioexception1);
                    }
                }
                else if (Util.getOSType() == Util.EnumOS.WINDOWS)
                {
                    String s1 = String.format("cmd.exe /C start \"Open file\" \"%s\"", s);

                    try
                    {
                        Runtime.getRuntime().exec(s1);
                        return;
                    }
                    catch (IOException ioexception)
                    {
                        logger.error("Couldn't open file", ioexception);
                    }
                }

                boolean flag = false;

                try
                {
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
                    oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, file1.toURI());
                }
                catch (Throwable throwable)
                {
                    logger.error("Couldn't open link", throwable);
                    flag = true;
                }

                if (flag)
                {
                    logger.info("Opening via system class!");
                    Sys.openURL("file://" + s);
                }
            }
            else if (button.id == 1)
            {
                if (this.changed)
                {
                    List<ResourcePackRepository.Entry> list = Lists.newArrayList();

                    for (ResourcePackEntry resourcepacklistentry : this.selectedResourcePackPanel)
                    {
                        if (resourcepacklistentry instanceof ResourcePackEntryFound)
                        {
                            list.add(((ResourcePackEntryFound)resourcepacklistentry).getResourcePack());
                        }
                    }

                    Collections.reverse(list);
                    this.mc.getResourcePackRepository().setRepositories(list);
                    this.mc.gameSettings.resourcePacks.clear();
                    this.mc.gameSettings.incompatibleResourcePacks.clear();

                    for (ResourcePackRepository.Entry resourcepackrepository$entry : list)
                    {
                        this.mc.gameSettings.resourcePacks.add(resourcepackrepository$entry.getResourcePackName());

                        if (resourcepackrepository$entry.func_183027_f() != 1)
                        {
                            this.mc.gameSettings.incompatibleResourcePacks.add(resourcepackrepository$entry.getResourcePackName());
                        }
                    }

                    this.mc.gameSettings.saveOptions();
                    this.mc.refreshResources();
                }

                this.mc.displayGuiScreen(this.parentScreen);
            }*/
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        availableResourcePackPanel.mouseClicked(mouseX, mouseY, mouseButton);
        selectedResourcePackPanel.mouseClicked(mouseX, mouseY, mouseButton);
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        // draw pano in main menu and normal bg in-game
        drawDefaultBackground();

        // draw both panels
        availableResourcePackPanel.drawScreen(mouseX, mouseY, partialTicks);
        selectedResourcePackPanel.drawScreen(mouseX, mouseY, partialTicks);

        // draw title
        drawCenteredString(fontRendererObj, I18n.format("resourcePack.title"), width / 2, ResourcePackUtils.ResourcePacksScreenTitleTopPadding, ColorUtils.White);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void markChanged()
    {
        changed = true;
    }
}
