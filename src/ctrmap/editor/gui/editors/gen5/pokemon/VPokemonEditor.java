package ctrmap.editor.gui.editors.gen5.pokemon;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.pokemon.gen5.battle.evolution.WBEvolutionData;
import ctrmap.formats.pokemon.gen5.battle.evolution.WBEvolutionData.WBEvolutionEntry;
import ctrmap.formats.pokemon.gen5.battle.learnset.WBLearnsetData;
import ctrmap.formats.pokemon.gen5.battle.learnset.WBLearnsetData.WBLearnsetEntry;
import ctrmap.formats.pokemon.gen5.battle.personal.WBPersonalData;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;

public class VPokemonEditor extends JPanel implements AbstractTabbedEditor {

    private CTRMap Instance;
    private TextFile PkmnNames, ItemNames, MoveNames, AbilNames;
    private List<WBPersonalData> PersonalData;
    private List<WBLearnsetData> Learnsets;
    private List<WBEvolutionData> Evolutions;
    private int currentPokemonIndex = -1;
    private boolean updatingUI = false;

    // Type names (Gen 5 order)
    private static final String[] TYPE_NAMES = {
        "Normal", "Fighting", "Flying", "Poison", "Ground", "Rock",
        "Bug", "Ghost", "Steel", "Fire", "Water", "Grass",
        "Electric", "Psychic", "Ice", "Dragon", "Dark"
    };

    private static final String[] STAT_NAMES = {"HP", "Attack", "Defense", "Speed", "Sp. Attack", "Sp. Defense"};

    private static final String[] EGG_GROUP_NAMES = {
        "---", "Monster", "Water 1", "Bug", "Flying", "Field",
        "Fairy", "Grass", "Human-Like", "Water 3", "Mineral",
        "Amorphous", "Water 2", "Ditto", "Dragon", "Undiscovered"
    };

    private static final String[] EXP_GROWTH_NAMES = {
        "Medium Fast", "Erratic", "Fluctuating", "Medium Slow", "Fast", "Slow"
    };

    // UI components
    private JComboBox<String> pokemonSelector;
    private JButton saveButton;

    // Stats tab
    private JSpinner[] baseStatSpinners = new JSpinner[6];
    private JComboBox<String> type1Combo, type2Combo;
    private JSpinner[] evYieldSpinners = new JSpinner[6];
    private JComboBox<String> ability1Combo, ability2Combo, hiddenAbilityCombо;
    private JSpinner catchRateSpinner, genderRatioSpinner, baseHappinessSpinner;
    private JSpinner hatchStepsSpinner, baseExpSpinner, heightSpinner, weightSpinner;
    private JComboBox<String> eggGroup1Combo, eggGroup2Combo, expGrowthCombo;
    private JComboBox<String> wildItem1Combo, wildItem2Combo, wildItem3Combo;

    // Learnset tab
    private DefaultTableModel learnsetTableModel;
    private JTable learnsetTable;
    private JComboBox<String> learnsetMoveCombo;
    private JSpinner learnsetLevelSpinner;

    // Evolution tab
    private JSpinner[] evoMethodSpinners = new JSpinner[WBEvolutionData.MAX_SLOTS];
    private JSpinner[] evoParamSpinners = new JSpinner[WBEvolutionData.MAX_SLOTS];
    private JComboBox<String>[] evoTargetCombos = new JComboBox[WBEvolutionData.MAX_SLOTS];

    // TM tab
    private JCheckBox[] tmCheckboxes;

    public VPokemonEditor(CTRMap Instance) {
        this.Instance = Instance;
        this.PersonalData = new ArrayList<>();
        this.Learnsets = new ArrayList<>();
        this.Evolutions = new ArrayList<>();

        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // North: selector + save
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        pokemonSelector = new JComboBox<>();
        pokemonSelector.setMaximumRowCount(20);
        pokemonSelector.addActionListener(e -> {
            if (!updatingUI) {
                int sel = pokemonSelector.getSelectedIndex();
                if (sel >= 0 && sel < PersonalData.size()) {
                    currentPokemonIndex = sel;
                    UI_LoadPokemon();
                }
            }
        });

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> SaveCurrentPokemon());

        topPanel.add(new JLabel("Pokemon: "), BorderLayout.WEST);
        topPanel.add(pokemonSelector, BorderLayout.CENTER);
        topPanel.add(saveButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Stats", CreateStatsPanel());
        tabs.addTab("Learnset", CreateLearnsetPanel());
        tabs.addTab("Evolution", CreateEvolutionPanel());
        tabs.addTab("TM Compatibility", CreateTMPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ======================== PANEL CREATION ========================

    private JComponent CreateStatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Base Stats section
        JPanel baseStatsPanel = new JPanel(new GridLayout(6, 2, 4, 2));
        baseStatsPanel.setBorder(BorderFactory.createTitledBorder("Base Stats"));
        for (int i = 0; i < 6; i++) {
            baseStatsPanel.add(new JLabel(STAT_NAMES[i]));
            baseStatSpinners[i] = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
            final int idx = i;
            baseStatSpinners[i].addChangeListener(e -> {
                if (!updatingUI) {
                    WBPersonalData pd = GetCurrentPersonal();
                    if (pd != null) pd.SetBaseStat(idx, (int) baseStatSpinners[idx].getValue());
                }
            });
            baseStatsPanel.add(baseStatSpinners[i]);
        }

        // Types section
        JPanel typesPanel = new JPanel(new GridLayout(2, 2, 4, 2));
        typesPanel.setBorder(BorderFactory.createTitledBorder("Types"));
        type1Combo = new JComboBox<>(TYPE_NAMES);
        type2Combo = new JComboBox<>(TYPE_NAMES);
        type1Combo.addActionListener(e -> {
            if (!updatingUI) {
                WBPersonalData pd = GetCurrentPersonal();
                if (pd != null) pd.SetType1(type1Combo.getSelectedIndex());
            }
        });
        type2Combo.addActionListener(e -> {
            if (!updatingUI) {
                WBPersonalData pd = GetCurrentPersonal();
                if (pd != null) pd.SetType2(type2Combo.getSelectedIndex());
            }
        });
        typesPanel.add(new JLabel("Primary"));
        typesPanel.add(type1Combo);
        typesPanel.add(new JLabel("Secondary"));
        typesPanel.add(type2Combo);

        // EV Yield section
        JPanel evPanel = new JPanel(new GridLayout(6, 2, 4, 2));
        evPanel.setBorder(BorderFactory.createTitledBorder("EV Yield"));
        for (int i = 0; i < 6; i++) {
            evPanel.add(new JLabel(STAT_NAMES[i]));
            evYieldSpinners[i] = new JSpinner(new SpinnerNumberModel(0, 0, 3, 1));
            final int idx = i;
            evYieldSpinners[i].addChangeListener(e -> {
                if (!updatingUI) {
                    WBPersonalData pd = GetCurrentPersonal();
                    if (pd != null) pd.SetEVYield(idx, (int) evYieldSpinners[idx].getValue());
                }
            });
            evPanel.add(evYieldSpinners[i]);
        }

        // Abilities section
        JPanel abilPanel = new JPanel(new GridLayout(3, 2, 4, 2));
        abilPanel.setBorder(BorderFactory.createTitledBorder("Abilities"));
        ability1Combo = new JComboBox<>();
        ability2Combo = new JComboBox<>();
        hiddenAbilityCombо = new JComboBox<>();
        ability1Combo.addActionListener(e -> {
            if (!updatingUI) {
                WBPersonalData pd = GetCurrentPersonal();
                if (pd != null) pd.SetAbility1(ability1Combo.getSelectedIndex());
            }
        });
        ability2Combo.addActionListener(e -> {
            if (!updatingUI) {
                WBPersonalData pd = GetCurrentPersonal();
                if (pd != null) pd.SetAbility2(ability2Combo.getSelectedIndex());
            }
        });
        hiddenAbilityCombо.addActionListener(e -> {
            if (!updatingUI) {
                WBPersonalData pd = GetCurrentPersonal();
                if (pd != null) pd.SetHiddenAbility(hiddenAbilityCombо.getSelectedIndex());
            }
        });
        abilPanel.add(new JLabel("Ability 1"));
        abilPanel.add(ability1Combo);
        abilPanel.add(new JLabel("Ability 2"));
        abilPanel.add(ability2Combo);
        abilPanel.add(new JLabel("Hidden"));
        abilPanel.add(hiddenAbilityCombо);

        // Misc section
        JPanel miscPanel = new JPanel(new GridLayout(7, 2, 4, 2));
        miscPanel.setBorder(BorderFactory.createTitledBorder("Misc"));

        catchRateSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        genderRatioSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        baseHappinessSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        hatchStepsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        baseExpSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        heightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        weightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));

        catchRateSpinner.addChangeListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetCatchRate((int) catchRateSpinner.getValue()); } });
        genderRatioSpinner.addChangeListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetGenderRatio((int) genderRatioSpinner.getValue()); } });
        baseHappinessSpinner.addChangeListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetBaseHappiness((int) baseHappinessSpinner.getValue()); } });
        hatchStepsSpinner.addChangeListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetHatchSteps((int) hatchStepsSpinner.getValue()); } });
        baseExpSpinner.addChangeListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetBaseExp((int) baseExpSpinner.getValue()); } });
        heightSpinner.addChangeListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetHeight((int) heightSpinner.getValue()); } });
        weightSpinner.addChangeListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetWeight((int) weightSpinner.getValue()); } });

        miscPanel.add(new JLabel("Catch Rate"));
        miscPanel.add(catchRateSpinner);
        miscPanel.add(new JLabel("Gender Ratio"));
        miscPanel.add(genderRatioSpinner);
        miscPanel.add(new JLabel("Base Happiness"));
        miscPanel.add(baseHappinessSpinner);
        miscPanel.add(new JLabel("Hatch Steps"));
        miscPanel.add(hatchStepsSpinner);
        miscPanel.add(new JLabel("Base Exp"));
        miscPanel.add(baseExpSpinner);
        miscPanel.add(new JLabel("Height"));
        miscPanel.add(heightSpinner);
        miscPanel.add(new JLabel("Weight"));
        miscPanel.add(weightSpinner);

        // Egg Groups + Growth
        JPanel eggPanel = new JPanel(new GridLayout(3, 2, 4, 2));
        eggPanel.setBorder(BorderFactory.createTitledBorder("Egg Groups / Growth"));
        eggGroup1Combo = new JComboBox<>(EGG_GROUP_NAMES);
        eggGroup2Combo = new JComboBox<>(EGG_GROUP_NAMES);
        expGrowthCombo = new JComboBox<>(EXP_GROWTH_NAMES);
        eggGroup1Combo.addActionListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetEggGroup1(eggGroup1Combo.getSelectedIndex()); } });
        eggGroup2Combo.addActionListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetEggGroup2(eggGroup2Combo.getSelectedIndex()); } });
        expGrowthCombo.addActionListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetExpGrowthRate(expGrowthCombo.getSelectedIndex()); } });
        eggPanel.add(new JLabel("Egg Group 1"));
        eggPanel.add(eggGroup1Combo);
        eggPanel.add(new JLabel("Egg Group 2"));
        eggPanel.add(eggGroup2Combo);
        eggPanel.add(new JLabel("Exp Growth"));
        eggPanel.add(expGrowthCombo);

        // Wild Items
        JPanel wildItemPanel = new JPanel(new GridLayout(3, 2, 4, 2));
        wildItemPanel.setBorder(BorderFactory.createTitledBorder("Wild Held Items"));
        wildItem1Combo = new JComboBox<>();
        wildItem2Combo = new JComboBox<>();
        wildItem3Combo = new JComboBox<>();
        wildItem1Combo.setMaximumRowCount(20);
        wildItem2Combo.setMaximumRowCount(20);
        wildItem3Combo.setMaximumRowCount(20);
        wildItem1Combo.addActionListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetWildItem1(wildItem1Combo.getSelectedIndex()); } });
        wildItem2Combo.addActionListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetWildItem2(wildItem2Combo.getSelectedIndex()); } });
        wildItem3Combo.addActionListener(e -> { if (!updatingUI) { WBPersonalData pd = GetCurrentPersonal(); if (pd != null) pd.SetWildItem3(wildItem3Combo.getSelectedIndex()); } });
        wildItemPanel.add(new JLabel("Item 1 (50%)"));
        wildItemPanel.add(wildItem1Combo);
        wildItemPanel.add(new JLabel("Item 2 (5%)"));
        wildItemPanel.add(wildItem2Combo);
        wildItemPanel.add(new JLabel("Item 3 (1%)"));
        wildItemPanel.add(wildItem3Combo);

        // Layout all sections
        // Left column: Base Stats, Types, EV Yield
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.add(baseStatsPanel);
        leftCol.add(typesPanel);
        leftCol.add(evPanel);

        // Middle column: Abilities, Egg Groups, Wild Items
        JPanel midCol = new JPanel();
        midCol.setLayout(new BoxLayout(midCol, BoxLayout.Y_AXIS));
        midCol.add(abilPanel);
        midCol.add(eggPanel);
        midCol.add(wildItemPanel);

        // Right column: Misc
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.add(miscPanel);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(leftCol, gbc);
        gbc.gridx = 1;
        panel.add(midCol, gbc);
        gbc.gridx = 2;
        panel.add(rightCol, gbc);

        return new JScrollPane(panel);
    }

    private JPanel CreateLearnsetPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Table
        learnsetTableModel = new DefaultTableModel(new String[]{"Level", "Move"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        learnsetTable = new JTable(learnsetTableModel);
        learnsetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        learnsetTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        learnsetTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        panel.add(new JScrollPane(learnsetTable), BorderLayout.CENTER);

        // Edit controls
        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        editPanel.add(new JLabel("Move:"));
        learnsetMoveCombo = new JComboBox<>();
        learnsetMoveCombo.setMaximumRowCount(20);
        learnsetMoveCombo.setPreferredSize(new Dimension(180, 24));
        editPanel.add(learnsetMoveCombo);

        editPanel.add(new JLabel("Level:"));
        learnsetLevelSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        editPanel.add(learnsetLevelSpinner);

        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> {
            WBLearnsetData ls = GetCurrentLearnset();
            if (ls != null && learnsetMoveCombo.getSelectedIndex() >= 0) {
                int moveID = learnsetMoveCombo.getSelectedIndex();
                int level = (int) learnsetLevelSpinner.getValue();
                ls.AddEntry(moveID, level);
                UI_LoadLearnset();
            }
        });
        editPanel.add(addBtn);

        JButton removeBtn = new JButton("Remove");
        removeBtn.addActionListener(e -> {
            WBLearnsetData ls = GetCurrentLearnset();
            int sel = learnsetTable.getSelectedRow();
            if (ls != null && sel >= 0) {
                ls.RemoveEntry(sel);
                UI_LoadLearnset();
            }
        });
        editPanel.add(removeBtn);

        JButton updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> {
            WBLearnsetData ls = GetCurrentLearnset();
            int sel = learnsetTable.getSelectedRow();
            if (ls != null && sel >= 0 && learnsetMoveCombo.getSelectedIndex() >= 0) {
                int moveID = learnsetMoveCombo.getSelectedIndex();
                int level = (int) learnsetLevelSpinner.getValue();
                ls.SetEntry(sel, moveID, level);
                UI_LoadLearnset();
            }
        });
        editPanel.add(updateBtn);

        panel.add(editPanel, BorderLayout.SOUTH);

        // Selection listener to populate edit controls
        learnsetTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                WBLearnsetData ls = GetCurrentLearnset();
                int sel = learnsetTable.getSelectedRow();
                if (ls != null && sel >= 0 && sel < ls.GetEntryCount()) {
                    WBLearnsetEntry entry = ls.GetEntry(sel);
                    updatingUI = true;
                    learnsetMoveCombo.setSelectedIndex(Math.min(entry.GetMoveID(), learnsetMoveCombo.getItemCount() - 1));
                    learnsetLevelSpinner.setValue(entry.GetLevel());
                    updatingUI = false;
                }
            }
        });

        return panel;
    }

    private JPanel CreateEvolutionPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel gridPanel = new JPanel(new GridLayout(WBEvolutionData.MAX_SLOTS, 1, 0, 2));
        gridPanel.setBorder(BorderFactory.createTitledBorder("Evolution Slots"));

        for (int i = 0; i < WBEvolutionData.MAX_SLOTS; i++) {
            JPanel slotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
            slotPanel.add(new JLabel("Slot " + (i + 1) + ":"));

            slotPanel.add(new JLabel("Method"));
            evoMethodSpinners[i] = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
            final int idx = i;
            evoMethodSpinners[i].addChangeListener(e -> {
                if (!updatingUI) {
                    WBEvolutionData evo = GetCurrentEvolution();
                    if (evo != null) {
                        WBEvolutionEntry entry = evo.GetEntry(idx);
                        if (entry != null) entry.SetMethod((int) evoMethodSpinners[idx].getValue());
                    }
                }
            });
            slotPanel.add(evoMethodSpinners[i]);

            slotPanel.add(new JLabel("Param"));
            evoParamSpinners[i] = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
            evoParamSpinners[i].addChangeListener(e -> {
                if (!updatingUI) {
                    WBEvolutionData evo = GetCurrentEvolution();
                    if (evo != null) {
                        WBEvolutionEntry entry = evo.GetEntry(idx);
                        if (entry != null) entry.SetParameter((int) evoParamSpinners[idx].getValue());
                    }
                }
            });
            slotPanel.add(evoParamSpinners[i]);

            slotPanel.add(new JLabel("Target"));
            evoTargetCombos[i] = new JComboBox<>();
            evoTargetCombos[i].setMaximumRowCount(20);
            evoTargetCombos[i].setPreferredSize(new Dimension(160, 24));
            evoTargetCombos[i].addActionListener(e -> {
                if (!updatingUI) {
                    WBEvolutionData evo = GetCurrentEvolution();
                    if (evo != null) {
                        WBEvolutionEntry entry = evo.GetEntry(idx);
                        if (entry != null && evoTargetCombos[idx].getSelectedIndex() >= 0) {
                            entry.SetTargetSpecies(evoTargetCombos[idx].getSelectedIndex());
                        }
                    }
                }
            });
            slotPanel.add(evoTargetCombos[i]);

            gridPanel.add(slotPanel);
        }

        panel.add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel CreateTMPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // 128 TM/HM checkboxes in a scrollable grid
        int totalTMs = WBPersonalData.TM_FLAG_BYTES * 8;
        JPanel gridPanel = new JPanel(new GridLayout(0, 8, 2, 2));
        tmCheckboxes = new JCheckBox[totalTMs];

        for (int i = 0; i < totalTMs; i++) {
            String label;
            if (i < 95) {
                label = "TM" + String.format("%02d", i + 1);
            } else if (i < 101) {
                label = "HM" + String.format("%02d", i - 94);
            } else {
                label = "TM" + String.format("%03d", i + 1);
            }
            tmCheckboxes[i] = new JCheckBox(label);
            final int idx = i;
            tmCheckboxes[i].addActionListener(e -> {
                if (!updatingUI) {
                    WBPersonalData pd = GetCurrentPersonal();
                    if (pd != null) pd.SetTMFlag(idx, tmCheckboxes[idx].isSelected());
                }
            });
            gridPanel.add(tmCheckboxes[i]);
        }

        panel.add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        return panel;
    }

    // ======================== DATA ACCESS ========================

    NTRGameFS FS() {
        return Instance.getMissionControl(VLaunchpad.class).fs;
    }

    TextFile LoadSystemTextArchive(int index) {
        return new TextFile(FS().NARCGet(NARCRef.MSGDATA_SYSTEM, index), GenVMessageHandler.INSTANCE);
    }

    private WBPersonalData GetCurrentPersonal() {
        return currentPokemonIndex >= 0 && currentPokemonIndex < PersonalData.size()
            ? PersonalData.get(currentPokemonIndex) : null;
    }

    private WBLearnsetData GetCurrentLearnset() {
        return currentPokemonIndex >= 0 && currentPokemonIndex < Learnsets.size()
            ? Learnsets.get(currentPokemonIndex) : null;
    }

    private WBEvolutionData GetCurrentEvolution() {
        return currentPokemonIndex >= 0 && currentPokemonIndex < Evolutions.size()
            ? Evolutions.get(currentPokemonIndex) : null;
    }

    // ======================== DATA LOADING ========================

    private void LoadAllData() throws IOException {
        PersonalData.clear();
        Learnsets.clear();
        Evolutions.clear();

        int personalMax = FS().NARCGetDataMax(NARCRef.PML_PERSONAL);
        for (int i = 0; i < personalMax; i++) {
            FSFile f = FS().NARCGet(NARCRef.PML_PERSONAL, i);
            PersonalData.add(new WBPersonalData(f.getDataIOStream()));
        }

        int learnsetMax = FS().NARCGetDataMax(NARCRef.PML_LEARNSETS);
        for (int i = 0; i < learnsetMax; i++) {
            FSFile f = FS().NARCGet(NARCRef.PML_LEARNSETS, i);
            Learnsets.add(new WBLearnsetData(f.getDataIOStream()));
        }

        int evoMax = FS().NARCGetDataMax(NARCRef.PML_EVOLUTION);
        for (int i = 0; i < evoMax; i++) {
            FSFile f = FS().NARCGet(NARCRef.PML_EVOLUTION, i);
            Evolutions.add(new WBEvolutionData(f.getDataIOStream()));
        }
    }

    private void LoadTextArchives() {
        PkmnNames = LoadSystemTextArchive(90);
        ItemNames = LoadSystemTextArchive(64);
        MoveNames = LoadSystemTextArchive(403);
        AbilNames = LoadSystemTextArchive(374);
    }

    // ======================== UI UPDATE ========================

    private void UI_LoadPokemon() {
        WBPersonalData pd = GetCurrentPersonal();
        if (pd == null) return;

        updatingUI = true;

        // Base stats
        for (int i = 0; i < 6; i++) {
            baseStatSpinners[i].setValue(pd.GetBaseStat(i));
        }

        // Types
        if (pd.GetType1() < TYPE_NAMES.length) type1Combo.setSelectedIndex(pd.GetType1());
        if (pd.GetType2() < TYPE_NAMES.length) type2Combo.setSelectedIndex(pd.GetType2());

        // EV yields
        for (int i = 0; i < 6; i++) {
            evYieldSpinners[i].setValue(pd.GetEVYield(i));
        }

        // Abilities
        if (pd.GetAbility1() < ability1Combo.getItemCount()) ability1Combo.setSelectedIndex(pd.GetAbility1());
        if (pd.GetAbility2() < ability2Combo.getItemCount()) ability2Combo.setSelectedIndex(pd.GetAbility2());
        if (pd.GetHiddenAbility() < hiddenAbilityCombо.getItemCount()) hiddenAbilityCombо.setSelectedIndex(pd.GetHiddenAbility());

        // Misc
        catchRateSpinner.setValue(pd.GetCatchRate());
        genderRatioSpinner.setValue(pd.GetGenderRatio());
        baseHappinessSpinner.setValue(pd.GetBaseHappiness());
        hatchStepsSpinner.setValue(pd.GetHatchSteps());
        baseExpSpinner.setValue(pd.GetBaseExp());
        heightSpinner.setValue(pd.GetHeight());
        weightSpinner.setValue(pd.GetWeight());

        // Egg groups / growth
        if (pd.GetEggGroup1() < EGG_GROUP_NAMES.length) eggGroup1Combo.setSelectedIndex(pd.GetEggGroup1());
        if (pd.GetEggGroup2() < EGG_GROUP_NAMES.length) eggGroup2Combo.setSelectedIndex(pd.GetEggGroup2());
        if (pd.GetExpGrowthRate() < EXP_GROWTH_NAMES.length) expGrowthCombo.setSelectedIndex(pd.GetExpGrowthRate());

        // Wild items
        if (pd.GetWildItem1() < wildItem1Combo.getItemCount()) wildItem1Combo.setSelectedIndex(pd.GetWildItem1());
        if (pd.GetWildItem2() < wildItem2Combo.getItemCount()) wildItem2Combo.setSelectedIndex(pd.GetWildItem2());
        if (pd.GetWildItem3() < wildItem3Combo.getItemCount()) wildItem3Combo.setSelectedIndex(pd.GetWildItem3());

        // TM flags
        for (int i = 0; i < tmCheckboxes.length; i++) {
            tmCheckboxes[i].setSelected(pd.GetTMFlag(i));
        }

        updatingUI = false;

        // Learnset + Evolution (separate methods since they use different data lists)
        UI_LoadLearnset();
        UI_LoadEvolution();
    }

    private void UI_LoadLearnset() {
        learnsetTableModel.setRowCount(0);
        WBLearnsetData ls = GetCurrentLearnset();
        if (ls == null) return;

        for (int i = 0; i < ls.GetEntryCount(); i++) {
            WBLearnsetEntry entry = ls.GetEntry(i);
            String moveName = entry.GetMoveID() < MoveNames.getLineCount()
                ? MoveNames.getLine(entry.GetMoveID()) : "Move " + entry.GetMoveID();
            learnsetTableModel.addRow(new Object[]{entry.GetLevel(), moveName});
        }
    }

    private void UI_LoadEvolution() {
        WBEvolutionData evo = GetCurrentEvolution();
        updatingUI = true;
        for (int i = 0; i < WBEvolutionData.MAX_SLOTS; i++) {
            if (evo != null && i < evo.GetSlotCount()) {
                WBEvolutionEntry entry = evo.GetEntry(i);
                evoMethodSpinners[i].setValue(entry.GetMethod());
                evoParamSpinners[i].setValue(entry.GetParameter());
                if (entry.GetTargetSpecies() < evoTargetCombos[i].getItemCount()) {
                    evoTargetCombos[i].setSelectedIndex(entry.GetTargetSpecies());
                }
            } else {
                evoMethodSpinners[i].setValue(0);
                evoParamSpinners[i].setValue(0);
                evoTargetCombos[i].setSelectedIndex(0);
            }
        }
        updatingUI = false;
    }

    // ======================== SAVING ========================

    private void SaveCurrentPokemon() {
        if (currentPokemonIndex < 0) return;

        try {
            // Save personal data
            if (currentPokemonIndex < PersonalData.size()) {
                FSFile f = FS().NARCGet(NARCRef.PML_PERSONAL, currentPokemonIndex);
                DataIOStream out = f.getDataIOStream();
                PersonalData.get(currentPokemonIndex).Serialize(out);
                out.close();
            }

            // Save learnset
            if (currentPokemonIndex < Learnsets.size()) {
                FSFile f = FS().NARCGet(NARCRef.PML_LEARNSETS, currentPokemonIndex);
                DataIOStream out = f.getDataIOStream();
                Learnsets.get(currentPokemonIndex).Serialize(out);
                out.close();
            }

            // Save evolution
            if (currentPokemonIndex < Evolutions.size()) {
                FSFile f = FS().NARCGet(NARCRef.PML_EVOLUTION, currentPokemonIndex);
                DataIOStream out = f.getDataIOStream();
                Evolutions.get(currentPokemonIndex).Serialize(out);
                out.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(VPokemonEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ======================== INTERFACE ========================

    @Override
    public void onProjectLoaded(CTRMapProject proj) {
        try {
            LoadTextArchives();
            LoadAllData();
        } catch (IOException ex) {
            Logger.getLogger(VPokemonEditor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        updatingUI = true;

        // Populate pokemon selector
        pokemonSelector.removeAllItems();
        int maxItems = Math.min(PkmnNames.getLineCount(), PersonalData.size());
        for (int i = 0; i < maxItems; i++) {
            pokemonSelector.addItem(String.format("%03d - %s", i, PkmnNames.getLine(i)));
        }

        // Populate ability combos
        String[] abilArr = AbilNames.getFriendlyLinesArray();
        ability1Combo.setModel(new DefaultComboBoxModel<>(abilArr));
        ability2Combo.setModel(new DefaultComboBoxModel<>(abilArr));
        hiddenAbilityCombо.setModel(new DefaultComboBoxModel<>(abilArr));

        // Populate item combos
        String[] itemArr = ItemNames.getFriendlyLinesArray();
        wildItem1Combo.setModel(new DefaultComboBoxModel<>(itemArr));
        wildItem2Combo.setModel(new DefaultComboBoxModel<>(itemArr));
        wildItem3Combo.setModel(new DefaultComboBoxModel<>(itemArr));

        // Populate move combo for learnset editor
        learnsetMoveCombo.setModel(new DefaultComboBoxModel<>(MoveNames.getFriendlyLinesArray()));

        // Populate evolution target combos
        String[] pkmnArr = new String[maxItems];
        for (int i = 0; i < maxItems; i++) {
            pkmnArr[i] = String.format("%03d - %s", i, PkmnNames.getLine(i));
        }
        for (int i = 0; i < WBEvolutionData.MAX_SLOTS; i++) {
            evoTargetCombos[i].setModel(new DefaultComboBoxModel<>(pkmnArr));
        }

        updatingUI = false;

        // Load first pokemon
        if (!PersonalData.isEmpty()) {
            currentPokemonIndex = 0;
            pokemonSelector.setSelectedIndex(0);
            UI_LoadPokemon();
        }
    }

    @Override
    public String getTabName() {
        return "Pokemon Editor";
    }

    @Override
    public boolean isGameSupported(GameInfo game) {
        return game.isGenV();
    }
}
