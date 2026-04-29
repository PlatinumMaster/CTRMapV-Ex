package ctrmap.editor.gui.editors.gen5.battle.encounters;
<<<<<<< HEAD
import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.gui.editors.gen5.battle.trainer.VTrainerComponent;
import ctrmap.editor.gui.editors.gen5.battle.trainer.VTrainerDataBinding;
import ctrmap.editor.gui.editors.gen5.battle.trainer.VTrainerEditor;
import ctrmap.editor.gui.editors.util.SystemTextLUT;
import ctrmap.editor.gui.editors.util.TextSingleton;
import ctrmap.editor.gui.editors.util.UserEnumHandler;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.UserData;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.common.GameInfoListener;
import ctrmap.formats.pokemon.gen5.battle.encounters.WBEnc;
import ctrmap.formats.pokemon.gen5.battle.encounters.WBEnc.WBEncType;
import ctrmap.formats.pokemon.gen5.battle.encounters.WBEncEntry;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerData;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerPoke;
=======

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.pokemon.gen5.battle.encounters.WBEnc;
import ctrmap.formats.pokemon.gen5.battle.encounters.WBEncEntry;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.field.debug.VZoneDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
<<<<<<< HEAD
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlNode;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;

public class VWildEditor extends javax.swing.JPanel implements VZoneDebugger, AbstractTabbedEditor {
    private CTRMap Instance;
    private TextFile PkmnNames;
    private ArrayList<WBEnc> CurrentEncounters;
    
    public VWildEditor() {
        initComponents();
    }
    
    public VWildEditor(CTRMap Instance) {
        initComponents();
        this.Instance = Instance;

    }
    
    NTRGameFS FS() {
        return this.Instance.getMissionControl(VLaunchpad.class).fs;
    }
    
=======
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

>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
    @Override
    public String getTabName() {
        return "Wild Encounter Editor";
    }

    @Override
    public boolean isGameSupported(GameInfo game) {
        return game.isGenV();
    }
<<<<<<< HEAD
    
    private void LoadEncountersFromFile(int Index) throws IOException {
        if (Index >= 0 && Index < FS().NARCGetDataMax(NARCRef.FIELD_ZONE_ENCOUNTERS)) {
            FSFile Encs = FS().NARCGet(NARCRef.FIELD_ZONE_ENCOUNTERS, Index);
            CurrentEncounters = new ArrayList<>(); 
            for (int EncIdx = 0; EncIdx < Encs.length() / 0xE8; ++EncIdx) {
                CurrentEncounters.add(new WBEnc(Encs.getDataIOStream()));
            }
        }
    }
    


    @Override
    public void onProjectLoaded(CTRMapProject proj) {
        this.PkmnNames = TextSingleton.Load(FS(), NARCRef.MSGDATA_SYSTEM, SystemTextLUT.get("Pokemon"));
    }

    @Override
    public void onProjectUnloaded(CTRMapProject proj) {
        
    } 
    
    public ArrayList<WBEnc> LoadEntryViaYml(Yaml yml) throws Exception {
       
        ArrayList<WBEnc> encs = new ArrayList<WBEnc>();
        
        for (YamlNode encEntryRoot : yml.root.children) {
            // Deserialize the YML into some bindings.
            VEncounterContainerBinding encYml = YamlReflectUtil.deserialize(encEntryRoot, VEncounterContainerBinding.class);
            
            // Build a WBEnc object.
            WBEnc enc = new WBEnc();

            // Grass
            for (int i = 0; i < Math.min(WBEnc.GRASS_ENC_SLOTS, (encYml.grass != null && encYml.grass.singles != null) ? encYml.grass.singles.length : 0); ++i) {
                enc.SetGrassEncounter(WBEncType.GRASS, i, MakeEntry(encYml.grass.singles[i]));
            }
            for (int i = 0; i < Math.min(WBEnc.GRASS_ENC_SLOTS, (encYml.grass != null && encYml.grass.doubles != null) ? encYml.grass.doubles.length : 0); ++i) {
                enc.SetGrassEncounter(WBEncType.GRASS_DOUBLE,  i, MakeEntry(encYml.grass.doubles[i]));
            }
            for (int i = 0; i < Math.min(WBEnc.GRASS_ENC_SLOTS, (encYml.grass != null && encYml.grass.special != null) ? encYml.grass.special.length : 0); ++i) {
                enc.SetGrassEncounter(WBEncType.GRASS_RARE,  i, MakeEntry(encYml.grass.special[i]));
            }

            // Surf
            for (int i = 0; i < Math.min(WBEnc.SURF_ENC_SLOTS, (encYml.surf != null && encYml.surf.singles != null) ? encYml.surf.singles.length : 0); ++i) {
                enc.SetSurfEncounter(WBEncType.SURF,  i, MakeEntry(encYml.surf.singles[i]));
            }
            for (int i = 0; i < Math.min(WBEnc.SURF_ENC_SLOTS, (encYml.surf != null && encYml.surf.special != null) ? encYml.surf.special.length : 0); ++i) {
                enc.SetSurfEncounter(WBEncType.SURF_RARE,  i, MakeEntry(encYml.surf.special[i]));
            }

            // Fish
            for (int i = 0; i < Math.min(WBEnc.FISH_ENC_SLOTS, (encYml.fish != null && encYml.fish.singles != null) ? encYml.fish.singles.length : 0); ++i) {
                enc.SetSurfEncounter(WBEncType.FISH, i,  MakeEntry(encYml.surf.singles[i]));
            }
            for (int i = 0; i < Math.min(WBEnc.FISH_ENC_SLOTS, (encYml.fish != null && encYml.fish.special != null) ? encYml.fish.special.length : 0); ++i) {
                enc.SetSurfEncounter(WBEncType.FISH_RARE,  i, MakeEntry(encYml.surf.special[i]));
            }
            
            encs.add(enc);
        }

        return encs;
    }

    private WBEncEntry MakeEntry(VEncounterSlotBinding slot) throws Exception {
        WBEncEntry entry = new WBEncEntry();
        if (slot != null) {
            entry.SetSpecies(UserEnumHandler.Parse(slot.species).shortValue());
            entry.SetForm((short) slot.form);
            entry.SetMinLevel((byte) slot.minLevel);
            entry.SetMaxLevel((byte) slot.maxLevel);
        }
        return entry;
    }

        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileLoadPanel3 = new javax.swing.JPanel();
        btnOpenScrInIDE25 = new javax.swing.JButton();
        btnAddZoneData3 = new javax.swing.JButton();
        jEncounterIndexSpinner = new javax.swing.JSpinner();
        jEncounterTabs = new javax.swing.JTabbedPane();

        fileLoadPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Encounters"));

        btnOpenScrInIDE25.setText("Open Encounter");
        btnOpenScrInIDE25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenScrInIDE25ActionPerformed(evt);
            }
        });

        btnAddZoneData3.setText("+");
        btnAddZoneData3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddZoneData3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fileLoadPanel3Layout = new javax.swing.GroupLayout(fileLoadPanel3);
        fileLoadPanel3.setLayout(fileLoadPanel3Layout);
        fileLoadPanel3Layout.setHorizontalGroup(
            fileLoadPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileLoadPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jEncounterIndexSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAddZoneData3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOpenScrInIDE25, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(903, Short.MAX_VALUE))
        );
        fileLoadPanel3Layout.setVerticalGroup(
            fileLoadPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileLoadPanel3Layout.createSequentialGroup()
                .addGroup(fileLoadPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOpenScrInIDE25)
                    .addComponent(btnAddZoneData3)
                    .addComponent(jEncounterIndexSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jEncounterTabs)
                    .addComponent(fileLoadPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fileLoadPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jEncounterTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenScrInIDE25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenScrInIDE25ActionPerformed
        // Check if the user directory exists.
        FSFile wildUserDirectory = Instance.getProject().userData.getUserDataDir(UserData.UsrDirectory.ENCOUNTERS);
        int encounterIndex = (int) jEncounterIndexSpinner.getValue();
        String yaml_name = String.format("%d.yml", encounterIndex);
        if (wildUserDirectory.exists() && wildUserDirectory.getChildCount() > 0) {
            FSFile wildEncYaml = wildUserDirectory.getChild(yaml_name);
            if (wildEncYaml.exists()) {
                ArrayList<WBEnc> encs;
                try {
                    encs = this.LoadEntryViaYml(new Yaml(wildEncYaml));
                    this.jEncounterTabs.add(String.format("Encounter %d", encounterIndex), new VWildComponent(Instance, encs));
                } catch (Exception ex) {
                    Logger.getLogger(VTrainerEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("Did not find YAML");
            } 
        }        
    }//GEN-LAST:event_btnOpenScrInIDE25ActionPerformed

    private void btnAddZoneData3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddZoneData3ActionPerformed

    }//GEN-LAST:event_btnAddZoneData3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddZoneData;
    private javax.swing.JButton btnAddZoneData1;
    private javax.swing.JButton btnAddZoneData2;
    private javax.swing.JButton btnAddZoneData3;
    private javax.swing.JButton btnOpenScrInIDE22;
    private javax.swing.JButton btnOpenScrInIDE23;
    private javax.swing.JButton btnOpenScrInIDE24;
    private javax.swing.JButton btnOpenScrInIDE25;
    private javax.swing.JPanel fileLoadPanel;
    private javax.swing.JPanel fileLoadPanel1;
    private javax.swing.JPanel fileLoadPanel2;
    private javax.swing.JPanel fileLoadPanel3;
    private javax.swing.JSpinner jEncounterIndexSpinner;
    private javax.swing.JTabbedPane jEncounterTabs;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner trainerSelector;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner trainerSelector1;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner trainerSelector2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void loadZone(VZone z) {
    }

=======
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
}
