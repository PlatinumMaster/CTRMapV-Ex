package ctrmap.editor.gui.editors.gen5.battle.moves;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.pokemon.gen5.battle.moves.WBMoveData;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;

public class VMoveEditor extends JPanel implements AbstractTabbedEditor {

    private CTRMap Instance;
    private TextFile MoveNames, MoveDescs;
    private List<WBMoveData> Moves;
    private int currentMoveIndex = -1;
    private boolean updatingUI = false;

    // Type names
    private static final String[] TYPE_NAMES = {
        "Normal", "Fighting", "Flying", "Poison", "Ground", "Rock",
        "Bug", "Ghost", "Steel", "Fire", "Water", "Grass",
        "Electric", "Psychic", "Ice", "Dragon", "Dark"
    };

    private static final String[] CATEGORY_NAMES = {"Status", "Physical", "Special"};

    private static final String[] FLAG_NAMES = {
        "Contact", "Charge", "Recharge", "Protect",
        "Magic Coat", "Snatch", "Mirror Move", "King's Rock"
    };

    // UI Components
    private JComboBox<String> moveSelector;
    private JButton saveButton;
    private JComboBox<String> typeCombо, categoryCombо;
    private JSpinner powerSpinner, accuracySpinner, ppSpinner;
    private JSpinner prioritySpinner, effectChanceSpinner, critStageSpinner;
    private JSpinner hitMinSpinner, hitMaxSpinner;
    private JSpinner drainSpinner, healSpinner;
    private JSpinner statusInflictSpinner, statusDurationSpinner;
    private JCheckBox[] flagCheckboxes = new JCheckBox[8];
    private JTextArea descArea;

    public VMoveEditor(CTRMap Instance) {
        this.Instance = Instance;
        this.Moves = new ArrayList<>();

        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // North: selector + save
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        moveSelector = new JComboBox<>();
        moveSelector.setMaximumRowCount(25);
        moveSelector.addActionListener(e -> {
            if (!updatingUI) {
                int sel = moveSelector.getSelectedIndex();
                if (sel >= 0 && sel < Moves.size()) {
                    currentMoveIndex = sel;
                    UI_LoadMove();
                }
            }
        });

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> SaveCurrentMove());

        topPanel.add(new JLabel("Move: "), BorderLayout.WEST);
        topPanel.add(moveSelector, BorderLayout.CENTER);
        topPanel.add(saveButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center: properties
        add(CreatePropertiesPanel(), BorderLayout.CENTER);
    }

    private JPanel CreatePropertiesPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        // Basic properties
        JPanel basicPanel = new JPanel(new GridLayout(7, 2, 4, 2));
        basicPanel.setBorder(BorderFactory.createTitledBorder("Basic Properties"));

        typeCombо = new JComboBox<>(TYPE_NAMES);
        categoryCombо = new JComboBox<>(CATEGORY_NAMES);
        powerSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        accuracySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        ppSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        prioritySpinner = new JSpinner(new SpinnerNumberModel(0, -7, 7, 1));
        effectChanceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));

        typeCombо.addActionListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetType(typeCombо.getSelectedIndex()); } });
        categoryCombо.addActionListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetCategory(categoryCombо.getSelectedIndex()); } });
        powerSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetPower((int) powerSpinner.getValue()); } });
        accuracySpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetAccuracy((int) accuracySpinner.getValue()); } });
        ppSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetPP((int) ppSpinner.getValue()); } });
        prioritySpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetPriority((int) prioritySpinner.getValue()); } });
        effectChanceSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetEffectChance((int) effectChanceSpinner.getValue()); } });

        basicPanel.add(new JLabel("Type"));
        basicPanel.add(typeCombо);
        basicPanel.add(new JLabel("Category"));
        basicPanel.add(categoryCombо);
        basicPanel.add(new JLabel("Power"));
        basicPanel.add(powerSpinner);
        basicPanel.add(new JLabel("Accuracy"));
        basicPanel.add(accuracySpinner);
        basicPanel.add(new JLabel("PP"));
        basicPanel.add(ppSpinner);
        basicPanel.add(new JLabel("Priority"));
        basicPanel.add(prioritySpinner);
        basicPanel.add(new JLabel("Effect Chance"));
        basicPanel.add(effectChanceSpinner);

        // Advanced properties
        JPanel advPanel = new JPanel(new GridLayout(6, 2, 4, 2));
        advPanel.setBorder(BorderFactory.createTitledBorder("Advanced Properties"));

        critStageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        hitMinSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));
        hitMaxSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));
        drainSpinner = new JSpinner(new SpinnerNumberModel(0, -128, 127, 1));
        healSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        statusInflictSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        statusDurationSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));

        critStageSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetCritStage((int) critStageSpinner.getValue()); } });
        hitMinSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetHitCountMin((int) hitMinSpinner.getValue()); } });
        hitMaxSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetHitCountMax((int) hitMaxSpinner.getValue()); } });
        drainSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetDrainPercent((int) drainSpinner.getValue()); } });
        healSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetHealPercent((int) healSpinner.getValue()); } });
        statusInflictSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetStatusInflict((int) statusInflictSpinner.getValue()); } });
        statusDurationSpinner.addChangeListener(e -> { if (!updatingUI) { WBMoveData m = GetCurrentMove(); if (m != null) m.SetStatusDuration((int) statusDurationSpinner.getValue()); } });

        advPanel.add(new JLabel("Crit Stage"));
        advPanel.add(critStageSpinner);
        advPanel.add(new JLabel("Hit Count Min"));
        advPanel.add(hitMinSpinner);
        advPanel.add(new JLabel("Hit Count Max"));
        advPanel.add(hitMaxSpinner);
        advPanel.add(new JLabel("Drain % (neg=recoil)"));
        advPanel.add(drainSpinner);
        advPanel.add(new JLabel("Heal %"));
        advPanel.add(healSpinner);
        advPanel.add(new JLabel("Status Inflict"));
        advPanel.add(statusInflictSpinner);

        // Status duration (extra row at bottom of advanced)
        // Flags
        JPanel flagsPanel = new JPanel(new GridLayout(2, 4, 4, 2));
        flagsPanel.setBorder(BorderFactory.createTitledBorder("Flags"));
        for (int i = 0; i < 8; i++) {
            flagCheckboxes[i] = new JCheckBox(FLAG_NAMES[i]);
            final int idx = i;
            flagCheckboxes[i].addActionListener(e -> {
                if (!updatingUI) {
                    WBMoveData m = GetCurrentMove();
                    if (m != null) m.SetFlag(idx, flagCheckboxes[idx].isSelected());
                }
            });
            flagsPanel.add(flagCheckboxes[i]);
        }

        // Description
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBorder(BorderFactory.createTitledBorder("Description"));
        descArea = new JTextArea(3, 40);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        // Status Duration row added to a separate misc panel
        JPanel miscPanel = new JPanel(new GridLayout(1, 2, 4, 2));
        miscPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        miscPanel.add(new JLabel("Status Duration"));
        miscPanel.add(statusDurationSpinner);

        // Layout columns
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.add(basicPanel);
        leftCol.add(flagsPanel);
        leftCol.add(descPanel);

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.add(advPanel);
        rightCol.add(miscPanel);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        mainPanel.add(leftCol, gbc);
        gbc.gridx = 1;
        mainPanel.add(rightCol, gbc);

        return mainPanel;
    }

    // ======================== DATA ACCESS ========================

    NTRGameFS FS() {
        return Instance.getMissionControl(VLaunchpad.class).fs;
    }

    TextFile LoadSystemTextArchive(int index) {
        return new TextFile(FS().NARCGet(NARCRef.MSGDATA_SYSTEM, index), GenVMessageHandler.INSTANCE);
    }

    private WBMoveData GetCurrentMove() {
        return currentMoveIndex >= 0 && currentMoveIndex < Moves.size()
            ? Moves.get(currentMoveIndex) : null;
    }

    // ======================== DATA LOADING ========================

    private void LoadAllMoves() throws IOException {
        Moves.clear();
        int max = FS().NARCGetDataMax(NARCRef.PML_MOVE);
        for (int i = 0; i < max; i++) {
            FSFile f = FS().NARCGet(NARCRef.PML_MOVE, i);
            Moves.add(new WBMoveData(f.getDataIOStream()));
        }
    }

    // ======================== UI UPDATE ========================

    private void UI_LoadMove() {
        WBMoveData m = GetCurrentMove();
        if (m == null) return;

        updatingUI = true;

        if (m.GetType() < TYPE_NAMES.length) typeCombо.setSelectedIndex(m.GetType());
        if (m.GetCategory() < CATEGORY_NAMES.length) categoryCombо.setSelectedIndex(m.GetCategory());
        powerSpinner.setValue(m.GetPower());
        accuracySpinner.setValue(m.GetAccuracy());
        ppSpinner.setValue(m.GetPP());
        prioritySpinner.setValue(m.GetPriority());
        effectChanceSpinner.setValue(m.GetEffectChance());
        critStageSpinner.setValue(m.GetCritStage());
        hitMinSpinner.setValue(m.GetHitCountMin());
        hitMaxSpinner.setValue(m.GetHitCountMax());
        drainSpinner.setValue(m.GetDrainPercent());
        healSpinner.setValue(m.GetHealPercent());
        statusInflictSpinner.setValue(m.GetStatusInflict());
        statusDurationSpinner.setValue(m.GetStatusDuration());

        for (int i = 0; i < 8; i++) {
            flagCheckboxes[i].setSelected(m.GetFlag(i));
        }

        // Description
        if (MoveDescs != null && currentMoveIndex < MoveDescs.getLineCount()) {
            descArea.setText(MoveDescs.getLine(currentMoveIndex));
        } else {
            descArea.setText("");
        }

        updatingUI = false;
    }

    // ======================== SAVING ========================

    private void SaveCurrentMove() {
        if (currentMoveIndex < 0 || currentMoveIndex >= Moves.size()) return;

        try {
            FSFile f = FS().NARCGet(NARCRef.PML_MOVE, currentMoveIndex);
            DataIOStream out = f.getDataIOStream();
            Moves.get(currentMoveIndex).Serialize(out);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(VMoveEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ======================== INTERFACE ========================

    @Override
    public void onProjectLoaded(CTRMapProject proj) {
        try {
            MoveNames = LoadSystemTextArchive(403);
            MoveDescs = LoadSystemTextArchive(402);
            LoadAllMoves();
        } catch (IOException ex) {
            Logger.getLogger(VMoveEditor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        updatingUI = true;
        moveSelector.removeAllItems();
        int maxItems = Math.min(MoveNames.getLineCount(), Moves.size());
        for (int i = 0; i < maxItems; i++) {
            moveSelector.addItem(String.format("%03d - %s", i, MoveNames.getLine(i)));
        }
        updatingUI = false;

        if (!Moves.isEmpty()) {
            currentMoveIndex = 0;
            moveSelector.setSelectedIndex(0);
            UI_LoadMove();
        }
    }

    @Override
    public String getTabName() {
        return "Move Editor";
    }

    @Override
    public boolean isGameSupported(GameInfo game) {
        return game.isGenV();
    }
}
