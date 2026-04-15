package ctrmap.editor.gui.editors.gen5.battle.encounters;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.pokemon.gen5.battle.encounters.WBEnc;
import ctrmap.formats.pokemon.gen5.battle.encounters.WBEncEntry;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.field.debug.VZoneDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;

public class VWildEditor extends JPanel implements VZoneDebugger, AbstractTabbedEditor {

    private CTRMap Instance;
    private TextFile PkmnNames;
    private ArrayList<WBEnc> CurrentEncounters;
    private int currentEncID = -1;

    private JSpinner encSelector;
    private JButton saveButton;
    private JTabbedPane tabbedPane;

    private JPanel regGrassPanel, dblGrassPanel, rareGrassPanel;
    private JPanel regSurfPanel, rareSurfPanel;
    private JPanel regFishPanel, rareFishPanel;

    public VWildEditor() {
        initUI();
    }

    public VWildEditor(CTRMap Instance) {
        this.Instance = Instance;
        initUI();
    }

    NTRGameFS FS() {
        return this.Instance.getMissionControl(VLaunchpad.class).fs;
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Top toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolbar.add(new JLabel("Encounter File:"));
        encSelector = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        encSelector.setPreferredSize(new Dimension(80, 24));
        encSelector.addChangeListener(e -> {
            int idx = ((Number) encSelector.getValue()).intValue();
            try {
                LoadEncountersFromFile(idx);
                if (CurrentEncounters != null && !CurrentEncounters.isEmpty()) {
                    LoadEncounterByIndex(0);
                }
            } catch (IOException ex) {
                Logger.getLogger(VWildEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        toolbar.add(encSelector);

        saveButton = new JButton("Save Encounters");
        saveButton.addActionListener(e -> {
            if (currentEncID >= 0 && CurrentEncounters != null) {
                try {
                    FSFile encFile = FS().NARCGet(NARCRef.FIELD_ZONE_ENCOUNTERS, currentEncID);
                    DataIOStream out = encFile.getDataIOStream();
                    for (WBEnc enc : CurrentEncounters) {
                        enc.Serialize(out);
                    }
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(VWildEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        toolbar.add(saveButton);
        add(toolbar, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane();

        // Grass/Ground tab
        JPanel grassTab = new JPanel();
        grassTab.setLayout(new BoxLayout(grassTab, BoxLayout.Y_AXIS));

        regGrassPanel = new JPanel();
        regGrassPanel.setLayout(new BoxLayout(regGrassPanel, BoxLayout.Y_AXIS));
        grassTab.add(createEncounterSection("Regular Grass", regGrassPanel));

        dblGrassPanel = new JPanel();
        dblGrassPanel.setLayout(new BoxLayout(dblGrassPanel, BoxLayout.Y_AXIS));
        grassTab.add(createEncounterSection("Double Grass", dblGrassPanel));

        rareGrassPanel = new JPanel();
        rareGrassPanel.setLayout(new BoxLayout(rareGrassPanel, BoxLayout.Y_AXIS));
        grassTab.add(createEncounterSection("Special Grass", rareGrassPanel));

        JScrollPane grassScroll = new JScrollPane(grassTab);
        grassScroll.getVerticalScrollBar().setUnitIncrement(16);
        tabbedPane.addTab("Grass/Ground", grassScroll);

        // Surf tab
        JPanel surfTab = new JPanel();
        surfTab.setLayout(new BoxLayout(surfTab, BoxLayout.Y_AXIS));

        regSurfPanel = new JPanel();
        regSurfPanel.setLayout(new BoxLayout(regSurfPanel, BoxLayout.Y_AXIS));
        surfTab.add(createEncounterSection("Regular Surf", regSurfPanel));

        rareSurfPanel = new JPanel();
        rareSurfPanel.setLayout(new BoxLayout(rareSurfPanel, BoxLayout.Y_AXIS));
        surfTab.add(createEncounterSection("Rare Surf", rareSurfPanel));

        JScrollPane surfScroll = new JScrollPane(surfTab);
        surfScroll.getVerticalScrollBar().setUnitIncrement(16);
        tabbedPane.addTab("Surf", surfScroll);

        // Fishing tab
        JPanel fishTab = new JPanel();
        fishTab.setLayout(new BoxLayout(fishTab, BoxLayout.Y_AXIS));

        regFishPanel = new JPanel();
        regFishPanel.setLayout(new BoxLayout(regFishPanel, BoxLayout.Y_AXIS));
        fishTab.add(createEncounterSection("Regular Fishing", regFishPanel));

        rareFishPanel = new JPanel();
        rareFishPanel.setLayout(new BoxLayout(rareFishPanel, BoxLayout.Y_AXIS));
        fishTab.add(createEncounterSection("Rare Fishing", rareFishPanel));

        JScrollPane fishScroll = new JScrollPane(fishTab);
        fishScroll.getVerticalScrollBar().setUnitIncrement(16);
        tabbedPane.addTab("Fishing", fishScroll);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createEncounterSection(String title, JPanel slotsPanel) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBorder(BorderFactory.createTitledBorder(title));

        // Column header row
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        JLabel rateHdr = new JLabel("Rate");
        rateHdr.setPreferredSize(new Dimension(40, 18));
        rateHdr.setFont(rateHdr.getFont().deriveFont(Font.BOLD));
        header.add(rateHdr);

        JLabel pkmnHdr = new JLabel("Pokemon");
        pkmnHdr.setPreferredSize(new Dimension(180, 18));
        pkmnHdr.setFont(pkmnHdr.getFont().deriveFont(Font.BOLD));
        header.add(pkmnHdr);

        JLabel formHdr = new JLabel("Form");
        formHdr.setPreferredSize(new Dimension(70, 18));
        formHdr.setFont(formHdr.getFont().deriveFont(Font.BOLD));
        header.add(formHdr);

        JLabel minHdr = new JLabel("Min Lv");
        minHdr.setPreferredSize(new Dimension(55, 18));
        minHdr.setFont(minHdr.getFont().deriveFont(Font.BOLD));
        header.add(minHdr);

        JLabel maxHdr = new JLabel("Max Lv");
        maxHdr.setPreferredSize(new Dimension(55, 18));
        maxHdr.setFont(maxHdr.getFont().deriveFont(Font.BOLD));
        header.add(maxHdr);

        section.add(header, BorderLayout.NORTH);
        section.add(slotsPanel, BorderLayout.CENTER);
        return section;
    }

    private void LoadEncountersFromFile(int Index) throws IOException {
        if (Index >= 0 && Index < FS().NARCGetDataMax(NARCRef.FIELD_ZONE_ENCOUNTERS)) {
            FSFile Encs = FS().NARCGet(NARCRef.FIELD_ZONE_ENCOUNTERS, Index);
            CurrentEncounters = new ArrayList<>();
            currentEncID = Index;
            DataIOStream stream = Encs.getDataIOStream();
            for (int EncIdx = 0; EncIdx < Encs.length() / 0xE8; ++EncIdx) {
                CurrentEncounters.add(new WBEnc(stream));
            }
            stream.close();
        }
    }

    private void LoadEncounterByIndex(int Index) {
        regGrassPanel.removeAll();
        dblGrassPanel.removeAll();
        rareGrassPanel.removeAll();
        regSurfPanel.removeAll();
        rareSurfPanel.removeAll();
        regFishPanel.removeAll();
        rareFishPanel.removeAll();

        if (CurrentEncounters != null && Index < CurrentEncounters.size()) {
            WBEnc Enc = CurrentEncounters.get(Index);

            for (int i = 0; i < WBEnc.GRASS_ENC_SLOTS; ++i) {
                regGrassPanel.add(new VWildEncounterSlot(
                    Enc.GetGrassEncounter(WBEnc.WBEncType.GRASS, i), PkmnNames, i, WBEnc.WBEncType.GRASS));
            }
            for (int i = 0; i < WBEnc.GRASS_ENC_SLOTS; ++i) {
                dblGrassPanel.add(new VWildEncounterSlot(
                    Enc.GetGrassEncounter(WBEnc.WBEncType.GRASS_DOUBLE, i), PkmnNames, i, WBEnc.WBEncType.GRASS_DOUBLE));
            }
            for (int i = 0; i < WBEnc.GRASS_ENC_SLOTS; ++i) {
                rareGrassPanel.add(new VWildEncounterSlot(
                    Enc.GetGrassEncounter(WBEnc.WBEncType.GRASS_RARE, i), PkmnNames, i, WBEnc.WBEncType.GRASS_RARE));
            }
            for (int i = 0; i < WBEnc.SURF_ENC_SLOTS; ++i) {
                regSurfPanel.add(new VWildEncounterSlot(
                    Enc.GetSurfEncounter(WBEnc.WBEncType.SURF, i), PkmnNames, i, WBEnc.WBEncType.SURF));
            }
            for (int i = 0; i < WBEnc.SURF_ENC_SLOTS; ++i) {
                rareSurfPanel.add(new VWildEncounterSlot(
                    Enc.GetSurfEncounter(WBEnc.WBEncType.SURF_RARE, i), PkmnNames, i, WBEnc.WBEncType.SURF_RARE));
            }
            for (int i = 0; i < WBEnc.FISH_ENC_SLOTS; ++i) {
                regFishPanel.add(new VWildEncounterSlot(
                    Enc.GetFishEncounter(WBEnc.WBEncType.FISH, i), PkmnNames, i, WBEnc.WBEncType.FISH));
            }
            for (int i = 0; i < WBEnc.FISH_ENC_SLOTS; ++i) {
                rareFishPanel.add(new VWildEncounterSlot(
                    Enc.GetFishEncounter(WBEnc.WBEncType.FISH_RARE, i), PkmnNames, i, WBEnc.WBEncType.FISH_RARE));
            }
        }

        regGrassPanel.revalidate();
        regGrassPanel.repaint();
        dblGrassPanel.revalidate();
        dblGrassPanel.repaint();
        rareGrassPanel.revalidate();
        rareGrassPanel.repaint();
        regSurfPanel.revalidate();
        regSurfPanel.repaint();
        rareSurfPanel.revalidate();
        rareSurfPanel.repaint();
        regFishPanel.revalidate();
        regFishPanel.repaint();
        rareFishPanel.revalidate();
        rareFishPanel.repaint();
    }

    @Override
    public void loadZone(VZone z) {
        if (z != null && z.header != null) {
            int encID = z.header.encID;
            try {
                LoadEncountersFromFile(encID);
                if (CurrentEncounters != null && !CurrentEncounters.isEmpty()) {
                    LoadEncounterByIndex(0);
                }
            } catch (IOException ex) {
                Logger.getLogger(VWildEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void onProjectLoaded(CTRMapProject proj) {
        PkmnNames = new TextFile(FS().NARCGet(NARCRef.MSGDATA_SYSTEM, 90), GenVMessageHandler.INSTANCE);
    }

    @Override
    public void onProjectUnloaded(CTRMapProject proj) {
    }

    @Override
    public String getTabName() {
        return "Wild Encounter Editor";
    }

    @Override
    public boolean isGameSupported(GameInfo game) {
        return game.isGenV();
    }
}
