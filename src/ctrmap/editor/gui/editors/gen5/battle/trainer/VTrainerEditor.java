package ctrmap.editor.gui.editors.gen5.battle.trainer;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
<<<<<<< HEAD
import ctrmap.editor.gui.editors.text.loaders.ITextArcType;
import ctrmap.editor.gui.editors.util.SystemTextLUT;
import ctrmap.editor.gui.editors.util.TextSingleton;
import ctrmap.editor.gui.editors.util.UserEnumHandler;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.UserData;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerData;
=======
import ctrmap.editor.gui.editors.gen5.battle.SpriteImageLoader;
import ctrmap.editor.gui.editors.text.loaders.ITextArcType;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerData;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerMessageTable;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerPoke;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.MessageHandler;
import ctrmap.formats.pokemon.text.MsgStr;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.VRTC;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
<<<<<<< HEAD
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
=======
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
<<<<<<< HEAD
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlNode;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;

public class VTrainerEditor extends javax.swing.JPanel implements AbstractTabbedEditor {
    private CTRMap Instance;
    private TextFile TrClasses, TrNames, TrDialogue, BattleTypes, ItemNames, ItemDescs, MoveNames, MoveDescs, AbilNames, PkmnNames;
    
    private List<WBTrainerData> Trainers;
    
    public VTrainerEditor(CTRMap Instance) {
        initComponents();
        this.Trainers = new ArrayList<>();
        this.Instance = Instance;
        
        LoadAllTextArchives();
        
//        ArrayList<String> trainerNames = new ArrayList<String>();
//        DefaultComboBoxModel cbm = new DefaultComboBoxModel();
//        for (int Index = 0; Index < this.TrNames.getLineCount(); ++Index) {
//            cbm.addElement(String.format("%s %s", this.TrClasses.getLine(Index), this.TrNames.getLine(Index)));
//        }
//        this.trainerSelector.getCB().setModel(cbm);
    }
    
    TextFile LoadSystemTextArchive(int Index) {
	return new TextFile(FS().NARCGet(NARCRef.MSGDATA_SYSTEM, Index), GenVMessageHandler.INSTANCE);
    }
    
    NTRGameFS FS() {
        return Instance.getMissionControl(ctrmap.missioncontrol_ntr.VLaunchpad.class).fs;
    }
    
    TextFile LoadTextFile(String File) {
        return TextSingleton.Load(FS(), NARCRef.MSGDATA_SYSTEM, SystemTextLUT.get(File));
    }
    
    private void LoadAllTextArchives() {
        this.TrClasses = LoadTextFile("TrainerClasses");
        this.TrNames = LoadTextFile("Trainers");
        this.TrDialogue = LoadTextFile("TrainerDialogue");
        this.ItemNames = LoadTextFile("Items");
        this.ItemDescs = LoadTextFile("ItemDescriptions");
        this.BattleTypes = LoadTextFile("BattleTypes");
        this.PkmnNames = LoadTextFile("Pokemon");
        this.MoveNames = LoadTextFile("Moves");
    }
    
    private WBTrainerData GetCurrentTrainer() {
        // Load current trainer.
        int Index = trainerSelector.getValueSpinner();
=======
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import xstandard.fs.FSFile;

/**
 *
 * @author platinum
 */
public class VTrainerEditor extends javax.swing.JPanel implements AbstractTabbedEditor {
    private CTRMap Instance;
    private TextFile TrClasses, TrNames, TrDialogue, BattleTypes,
            ItemNames, ItemDescs, MoveNames, MoveDescs, AbilNames, PkmnNames;
    private List<WBTrainerData> Trainers;
    private static final Map<String, Integer> SystemTextLUT = Stream.of(
            new AbstractMap.SimpleEntry<>("ItemDescriptions", 63),
            new AbstractMap.SimpleEntry<>("Items", 64),
            new AbstractMap.SimpleEntry<>("Pokemon", 90),
            new AbstractMap.SimpleEntry<>("BattleTypes", 357),
            new AbstractMap.SimpleEntry<>("SpecialTrainers", 368),
            new AbstractMap.SimpleEntry<>("Abilities", 374),
            new AbstractMap.SimpleEntry<>("TrainerDialogue", 381),
            new AbstractMap.SimpleEntry<>("Trainers", 382),
            new AbstractMap.SimpleEntry<>("TrainerClasses", 383),
            new AbstractMap.SimpleEntry<>("MovesDescriptions", 402),
            new AbstractMap.SimpleEntry<>("Moves", 403)
        )
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
    );

    // UI components
    private JComboBox<String> trainerSelector;
    private JSpinner trainerSpinner;
    private JList<String> trainerClassList;
    private JTextField nameEntry;
    private JCheckBox canOverrideMoves, canOverrideItems, canHeal;
    private JComboBox<String> battleTypeComboBox;
    private JComboBox<String> Item1ComboBox, Item2ComboBox, Item3ComboBox, Item4ComboBox;
    private JCheckBox AIBasicCheckbox, AIWillAttackCheckbox, AIAdvancedCheckbox, AIUnusedCheckbox;
    private JCheckBox AIRivalCheckbox, AIUnused2Checkbox, AIUnused3Checkbox, AIMultiBattleCheckbox;
    private JSpinner AIConfigurationInteger;
    private JSpinner rewardItemSpinner, rewardMoneySpinner;
    private JPanel partyListPanel;
    private JButton addPokeBtn;
    private JLabel partyHeaderLabel;
    private JLabel trainerSpriteLabel;
    private JButton openInCS2DButton;

    // Trainer Text tab
    private VTrainerTextPanel trainerTextPanel;
    private WBTrainerMessageTable trainerMessageTable = new WBTrainerMessageTable();

    // Sprite animation state
    private List<ImageIcon> spriteFrames = new ArrayList<>();
    private int spriteAnimIndex = 0;
    /**
     * Interval between animation frames in ms. Each frame represents one
     * NDS tick (~16.67ms at 60 Hz). SpriteImageLoader pre-renders one
     * frame per NMAR tick, so advancing once per 16 ms here plays the
     * animation at native in-game speed.
     */
    private static final int SPRITE_ANIM_INTERVAL_MS = 16;
    private Timer spriteAnimTimer;

    public VTrainerEditor(CTRMap Instance) {
        this.Trainers = new ArrayList<>();
        this.Instance = Instance;
        initLayout();
        initListeners();
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        // --- Left panel ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(380, 600));
        leftPanel.setMinimumSize(new Dimension(340, 400));

        // Trainer selector row
        JPanel selectorPanel = new JPanel(new GridBagLayout());
        selectorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        selectorPanel.add(new JLabel("Trainer:"), gbc);

        trainerSelector = new JComboBox<>();
        trainerSelector.setMaximumRowCount(35);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        selectorPanel.add(trainerSelector, gbc);

        trainerSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
        trainerSpinner.setPreferredSize(new Dimension(60, 24));
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        selectorPanel.add(trainerSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        selectorPanel.add(new JLabel("Name:"), gbc);

        nameEntry = new JTextField();
        nameEntry.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        selectorPanel.add(nameEntry, gbc);
        gbc.gridwidth = 1;

        selectorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, selectorPanel.getPreferredSize().height));
        leftPanel.add(selectorPanel);

        // Trainer Class panel (with sprite preview)
        JPanel classPanel = new JPanel(new BorderLayout(4, 4));
        classPanel.setBorder(BorderFactory.createTitledBorder("Trainer Class"));
        trainerClassList = new JList<>();
        trainerClassList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trainerClassList.setVisibleRowCount(6);
        JScrollPane classScroll = new JScrollPane(trainerClassList);
        classScroll.setPreferredSize(new Dimension(260, 100));
        classPanel.add(classScroll, BorderLayout.CENTER);

        // Sprite preview + "Open in CS 2D" button on the right side of
        // class panel, stacked vertically.
        JPanel spritePanel = new JPanel();
        spritePanel.setLayout(new BoxLayout(spritePanel, BoxLayout.Y_AXIS));
        trainerSpriteLabel = new JLabel();
        trainerSpriteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        trainerSpriteLabel.setVerticalAlignment(SwingConstants.CENTER);
        trainerSpriteLabel.setAlignmentX(CENTER_ALIGNMENT);
        trainerSpriteLabel.setPreferredSize(new Dimension(96, 96));
        trainerSpriteLabel.setMinimumSize(new Dimension(96, 96));
        trainerSpriteLabel.setMaximumSize(new Dimension(96, 96));
        trainerSpriteLabel.setBorder(BorderFactory.createEtchedBorder());
        spritePanel.add(trainerSpriteLabel);
        openInCS2DButton = new JButton("Open in CS 2D");
        openInCS2DButton.setAlignmentX(CENTER_ALIGNMENT);
        openInCS2DButton.setToolTipText(
            "Open this trainer's sprite files in the CreativeStudio 2D editor");
        openInCS2DButton.addActionListener(e -> openSpriteInCS2D());
        openInCS2DButton.setEnabled(false);
        spritePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        spritePanel.add(openInCS2DButton);
        classPanel.add(spritePanel, BorderLayout.EAST);
        leftPanel.add(classPanel);

        // Items panel (2x2 grid)
        JPanel itemsPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items"));
        Item1ComboBox = new JComboBox<>();
        Item2ComboBox = new JComboBox<>();
        Item3ComboBox = new JComboBox<>();
        Item4ComboBox = new JComboBox<>();
        itemsPanel.add(Item1ComboBox);
        itemsPanel.add(Item2ComboBox);
        itemsPanel.add(Item3ComboBox);
        itemsPanel.add(Item4ComboBox);
        leftPanel.add(itemsPanel);

        // AI Configuration panel
        JPanel aiPanel = new JPanel(new GridBagLayout());
        aiPanel.setBorder(BorderFactory.createTitledBorder("AI Configuration"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 4, 1, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        AIBasicCheckbox = new JCheckBox("Basic AI");
        AIWillAttackCheckbox = new JCheckBox("Will Attack");
        AIAdvancedCheckbox = new JCheckBox("Advanced");
        AIUnusedCheckbox = new JCheckBox("Unused");
        AIRivalCheckbox = new JCheckBox("Rival");
        AIUnused2Checkbox = new JCheckBox("Unused 2");
        AIUnused3Checkbox = new JCheckBox("Unused 3");
        AIMultiBattleCheckbox = new JCheckBox("Multi-Battle");

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        aiPanel.add(AIBasicCheckbox, gbc);
        gbc.gridx = 1;
        aiPanel.add(AIWillAttackCheckbox, gbc);
        gbc.gridx = 2;
        aiPanel.add(AIAdvancedCheckbox, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        aiPanel.add(AIUnusedCheckbox, gbc);
        gbc.gridx = 1;
        aiPanel.add(AIRivalCheckbox, gbc);
        gbc.gridx = 2;
        aiPanel.add(AIUnused2Checkbox, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        aiPanel.add(AIUnused3Checkbox, gbc);
        gbc.gridx = 1;
        aiPanel.add(AIMultiBattleCheckbox, gbc);

        JPanel aiIntPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        aiIntPanel.add(new JLabel("Configuration Integer:"));
        AIConfigurationInteger = new JSpinner();
        AIConfigurationInteger.setPreferredSize(new Dimension(60, 24));
        aiIntPanel.add(AIConfigurationInteger);
        gbc.gridx = 2; gbc.gridy = 2;
        aiPanel.add(aiIntPanel, gbc);

        leftPanel.add(aiPanel);

        // Settings checkboxes row
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 4, 1, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        canOverrideMoves = new JCheckBox("Override Moves");
        canOverrideItems = new JCheckBox("Override Held Item");
        canHeal = new JCheckBox("Can Heal");

        gbc.gridx = 0; gbc.gridy = 0;
        settingsPanel.add(canOverrideMoves, gbc);
        gbc.gridx = 1;
        settingsPanel.add(canOverrideItems, gbc);
        gbc.gridx = 2;
        settingsPanel.add(canHeal, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        settingsPanel.add(new JLabel("Battle Type:"), gbc);
        battleTypeComboBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        settingsPanel.add(battleTypeComboBox, gbc);
        gbc.gridwidth = 1;

        leftPanel.add(settingsPanel);

        // Rewards panel
        JPanel rewardsPanel = new JPanel(new GridBagLayout());
        rewardsPanel.setBorder(BorderFactory.createTitledBorder("Rewards"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        rewardsPanel.add(new JLabel("Reward Item:"), gbc);
        rewardItemSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        rewardItemSpinner.setPreferredSize(new Dimension(70, 24));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        rewardsPanel.add(rewardItemSpinner, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        rewardsPanel.add(new JLabel("Reward Money:"), gbc);
        rewardMoneySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        rewardMoneySpinner.setPreferredSize(new Dimension(70, 24));
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.5;
        rewardsPanel.add(rewardMoneySpinner, gbc);

        leftPanel.add(rewardsPanel);

        // Save button
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((ActionEvent evt) -> {
            saveCurrentTrainer();
        });
        savePanel.add(saveButton);
        savePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, savePanel.getPreferredSize().height));
        leftPanel.add(savePanel);

        // Add vertical glue so components stay at top
        leftPanel.add(Box.createVerticalGlue());

        // --- Right panel: tabbed (Party / Trainer Text) ---
        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.addTab("Party", createPartyTab());
        trainerTextPanel = new VTrainerTextPanel();
        rightTabs.addTab("Trainer Text", trainerTextPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(rightTabs, BorderLayout.CENTER);

        // --- Split pane ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(leftPanel), rightPanel);
        splitPane.setDividerLocation(390);
        splitPane.setResizeWeight(0.0);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Builds the Party tab content (scrollable list of 6 party slot cards +
     * header row). Extracted from the pre-tab layout so the right side can
     * sit inside a JTabbedPane alongside the Trainer Text tab.
     */
    private JPanel createPartyTab() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Party"));

        JPanel partyHeaderPanel = new JPanel(new BorderLayout(4, 0));
        partyHeaderPanel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        partyHeaderLabel = new JLabel("Party (0/6)");
        partyHeaderPanel.add(partyHeaderLabel, BorderLayout.WEST);

        JPanel partyBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        addPokeBtn = new JButton("Add Pokemon");
        partyBtnPanel.add(addPokeBtn);
        partyHeaderPanel.add(partyBtnPanel, BorderLayout.EAST);
        panel.add(partyHeaderPanel, BorderLayout.NORTH);

        partyListPanel = new JPanel();
        partyListPanel.setLayout(new BoxLayout(partyListPanel, BoxLayout.Y_AXIS));
        JScrollPane partyScroll = new JScrollPane(partyListPanel);
        partyScroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(partyScroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Opens the currently selected trainer's sprite files in the
     * CreativeStudio 2D editor. Uses the existing load pipeline from
     * {@link SpriteImageLoader} to build a {@code Sprite2DResource} that
     * NGCS2D's embedded constructor accepts directly, and wires a save
     * callback that writes edits back into the trainer NARC slot.
     */
    private void openSpriteInCS2D() {
        WBTrainerData trainer = GetCurrentTrainer();
        if (trainer == null) return;
        int trainerClass = trainer.GetAssignedClass();
        VLaunchpad mc = Instance.getMissionControl(VLaunchpad.class);
        ctrmap.creativestudio.ngcs2d.res.Sprite2DResource res =
            SpriteImageLoader.loadTrainerSpriteResource(mc.fs, mc.game, trainerClass);
        if (res == null) {
            Logger.getLogger(VTrainerEditor.class.getName())
                .warning("Cannot open CS 2D: sprite resource unavailable for class " + trainerClass);
            return;
        }
        new ctrmap.creativestudio.ngcs2d.NGCS2D(res, edited -> {
            SpriteImageLoader.saveTrainerSpriteResource(mc.fs, trainerClass, edited);
            UI_UpdateTrainerSprite();
            return true;
        }).setVisible(true);
    }

    private void initListeners() {
        // Trainer selector combo
        trainerSelector.addActionListener((ActionEvent e) -> {
            int idx = trainerSelector.getSelectedIndex();
            if (idx >= 0) {
                trainerSpinner.setValue(idx);
            }
            if (Instance != null && trainerSelector.getSelectedIndex() >= 0) {
                UI_UpdateNameEntry();
                UI_UpdateMiscSettings();
                UI_UpdateAIInteger();
                UI_UpdateTrainerClassList();
                UI_UpdatePartyList();
                UI_UpdateAIFlags();
                for (int Index = 0; Index < WBTrainerData.ITEMS_COUNT_MAX; ++Index) {
                    UI_UpdateItem(Index);
                }
                UI_UpdateMovesEnabled();
                UI_UpdateHeldItemEnabled();
                UI_UpdateTrainerText();
                openInCS2DButton.setEnabled(GetCurrentTrainer() != null);
            }
        });

        // Trainer spinner syncs to combo
        trainerSpinner.addChangeListener((ChangeEvent e) -> {
            int val = (int) trainerSpinner.getValue();
            if (val >= 0 && val < trainerSelector.getItemCount()) {
                trainerSelector.setSelectedIndex(val);
            }
        });

        // Trainer class list
        trainerClassList.addListSelectionListener((ListSelectionEvent lse) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    int SelIndex = trainerClassList.getSelectedIndex(), TrIndex = Trainers.indexOf(CurrentTrainer);
                    if (SelIndex < 0 || TrIndex < 0 || TrIndex > FS().NARCGetDataMax(NARCRef.TRAINER_DATA)) {
                        return;
                    }
                    CurrentTrainer.SetAssignedClass(SelIndex);
                }
            }
        });
        canHeal.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetCanHeal(canHeal.isSelected());
                }
            }
        });
        canOverrideMoves.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetCanOverrideMoves(canOverrideMoves.isSelected());
                    UI_UpdateMovesEnabled();
                }
            }
        });
        canOverrideItems.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetCanOverrideHeldItem(canOverrideItems.isSelected());
                    UI_UpdateHeldItemEnabled();
                }
            }
        });
        battleTypeComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                int SelIndex = battleTypeComboBox.getSelectedIndex();
                if (CurrentTrainer != null && SelIndex >= 0) {
                    CurrentTrainer.SetBattleType(SelIndex);
                }
            }
        });

        // Trainer items.
        Item1ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                int SelIndex = Item1ComboBox.getSelectedIndex();
                if (CurrentTrainer != null && SelIndex >= 0) {
                    CurrentTrainer.SetItem(0, SelIndex);
                }
            }
        });
        Item2ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                int SelIndex = Item2ComboBox.getSelectedIndex();
                if (CurrentTrainer != null && SelIndex >= 0) {
                    CurrentTrainer.SetItem(1, SelIndex);
                }
            }
        });
        Item3ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                int SelIndex = Item3ComboBox.getSelectedIndex();
                if (CurrentTrainer != null && SelIndex >= 0) {
                    CurrentTrainer.SetItem(2, SelIndex);
                }
            }
        });
        Item4ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                int SelIndex = Item4ComboBox.getSelectedIndex();
                if (CurrentTrainer != null && SelIndex >= 0) {
                    CurrentTrainer.SetItem(3, SelIndex);
                }
            }
        });

        // Trainer rewards.
        rewardItemSpinner.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                int SelIndex = (int) rewardItemSpinner.getValue();
                if (CurrentTrainer != null && SelIndex >= 0) {
                    CurrentTrainer.SetPrizeIndex(SelIndex);
                }
            }
        });
        rewardMoneySpinner.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                int SelIndex = (int) rewardMoneySpinner.getValue();
                if (CurrentTrainer != null && SelIndex >= 0) {
                    CurrentTrainer.SetMoneyIndex(SelIndex);
                }
            }
        });

        // AI
        AIBasicCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetAIFlag(0, AIBasicCheckbox.isSelected());
                    UI_UpdateAIInteger();
                }
            }
        });
        AIWillAttackCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetAIFlag(1, AIWillAttackCheckbox.isSelected());
                    UI_UpdateAIInteger();
                }
            }
        });
        AIAdvancedCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetAIFlag(2, AIAdvancedCheckbox.isSelected());
                    UI_UpdateAIInteger();
                }
            }
        });
        AIUnusedCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetAIFlag(3, AIUnusedCheckbox.isSelected());
                    UI_UpdateAIInteger();
                }
            }
        });
        AIRivalCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetAIFlag(4, AIRivalCheckbox.isSelected());
                    UI_UpdateAIInteger();
                }
            }
        });
        AIUnused2Checkbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetAIFlag(5, AIUnused2Checkbox.isSelected());
                    UI_UpdateAIInteger();
                }
            }
        });
        AIUnused3Checkbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetAIFlag(6, AIUnused3Checkbox.isSelected());
                    UI_UpdateAIInteger();
                }
            }
        });
        AIMultiBattleCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null) {
                    CurrentTrainer.SetAIFlag(7, AIMultiBattleCheckbox.isSelected());
                    UI_UpdateAIInteger();
                }
            }
        });
        AIConfigurationInteger.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                int AIVal = (int) AIConfigurationInteger.getValue();
                if (CurrentTrainer != null && AIVal >= 0) {
                    CurrentTrainer.SetAIValue(AIVal);
                    UI_UpdateAIFlags();
                }
            }
        });

        // Party add
        addPokeBtn.addActionListener((ActionEvent e) -> {
            if (Instance != null) {
                WBTrainerData CurrentTrainer = GetCurrentTrainer();
                if (CurrentTrainer != null && CurrentTrainer.GetPkmnSize() < 6) {
                    CurrentTrainer.AddPkmn(new WBTrainerPoke());
                    UI_UpdatePartyList();
                }
            }
        });
    }

    private void saveCurrentTrainer() {
        WBTrainerData CurrentTrainer = GetCurrentTrainer();
        if (CurrentTrainer != null) {
            int Index = trainerSelector.getSelectedIndex();
            CurrentTrainer.Serialize(FS().NARCGet(NARCRef.TRAINER_DATA, Index),
                    FS().NARCGet(NARCRef.TRAINER_POKEMON, Index));
        }
        // Persist trainer dialogue edits + the trainer-message index table.
        // Both are scoped per-project (not per-trainer), so they're flushed
        // once on each save regardless of which trainer was edited. BW1
        // leaves trainerMessageTable.isLoaded() == false and save() becomes
        // a no-op.
        try {
            if (TrDialogue != null) {
                TrDialogue.store();
            }
        } catch (Exception ex) {
            Logger.getLogger(VTrainerEditor.class.getName())
                .log(Level.SEVERE, "Failed to store trainer dialogue", ex);
        }
        if (trainerMessageTable != null) {
            trainerMessageTable.save(FS());
        }
    }

    /** Binds the Trainer Text tab to the currently-selected trainer. */
    private void UI_UpdateTrainerText() {
        if (trainerTextPanel == null) return;
        // Use the combo's selectedIndex directly. Going through
        // Trainers.indexOf(currentTrainer) was fragile: if Trainers gets
        // mutated anywhere (or if a trainer reference is swapped), indexOf
        // can fall back to -1 or drift off the NARC slot — which flips
        // the message-table filter to "no records" for every trainer
        // except whichever one we last saw. The combo index IS the NARC
        // slot by construction (LoadAllTrainers + the populate loop both
        // iterate 0..max), so the direct lookup is both simpler and
        // tolerant of any list mutation.
        int trainerId = trainerSelector == null ? -1 : trainerSelector.getSelectedIndex();
        trainerTextPanel.setTrainer(trainerId, trainerMessageTable, TrDialogue);
    }

    TextFile LoadSystemTextArchive(int Index) {
	return new TextFile(FS().NARCGet(NARCRef.MSGDATA_SYSTEM, Index), GenVMessageHandler.INSTANCE);
    }

    NTRGameFS FS() {
        return Instance.getMissionControl(ctrmap.missioncontrol_ntr.VLaunchpad.class).fs;
    }

    private void LoadAllTextArchives() {
        this.TrClasses = LoadSystemTextArchive(SystemTextLUT.get("TrainerClasses"));
        this.TrNames = LoadSystemTextArchive(SystemTextLUT.get("Trainers"));
        this.TrDialogue = LoadSystemTextArchive(SystemTextLUT.get("TrainerDialogue"));
        this.ItemNames = LoadSystemTextArchive(SystemTextLUT.get("Items"));
        this.ItemDescs = LoadSystemTextArchive(SystemTextLUT.get("ItemDescriptions"));
        this.BattleTypes = LoadSystemTextArchive(SystemTextLUT.get("BattleTypes"));
        this.PkmnNames = LoadSystemTextArchive(SystemTextLUT.get("Pokemon"));
        this.MoveNames = LoadSystemTextArchive(SystemTextLUT.get("Moves"));
    }

    private void LoadAllTrainers() throws IOException {
        int TrainerMax = FS().NARCGetDataMax(NARCRef.TRAINER_DATA);
        for (int Index = 0; Index < TrainerMax; ++Index) {
            FSFile TrDat = FS().NARCGet(NARCRef.TRAINER_DATA, Index);
            FSFile TrPoke = FS().NARCGet(NARCRef.TRAINER_POKEMON, Index);
            this.Trainers.add(new WBTrainerData(TrDat.getDataIOStream(), TrPoke.getDataIOStream()));
        }
    }

    private WBTrainerData GetCurrentTrainer() {
        // Load current trainer.
        int Index = trainerSelector.getSelectedIndex();
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
        if (Index < 0 || Index > FS().NARCGetDataMax(NARCRef.TRAINER_DATA)) {
            return null;
        }
        return Trainers.get(Index);
    }
<<<<<<< HEAD
    
    @Override
    public void onProjectLoaded(CTRMapProject proj) {
        // TODO: Move somewhere else
       for (FSFile f : Instance.getProject().userData.getUserDataDir(UserData.UsrDirectory.ENUMS).listFiles()) {
           UserEnumHandler.Load(f);
       }
    }
    
=======

    @Override
    public void onProjectLoaded(CTRMapProject proj) {
        try {
            LoadAllTrainers();
            LoadAllTextArchives();
        } catch (IOException ex) {
            Logger.getLogger(VTrainerEditor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        // Populate the trainer-message record table (ARC 090). On BW1
        // the NARC is absent and load() leaves isLoaded()==false — the
        // Trainer Text tab renders a "not supported" notice in that case.
        trainerMessageTable.load(FS());

        // Setup trainer class list model.
        DefaultListModel TrClassModel = new DefaultListModel();
        for (MsgStr e : this.TrClasses.lines) {
            TrClassModel.addElement(e);
        }
        trainerClassList.setModel(TrClassModel);

        // Setup item boxes and battle type BEFORE populating trainer selector,
        // since adding items to the selector triggers the action listener which
        // calls UI_UpdateMiscSettings/UI_UpdateItem, and those need populated models.
        Item1ComboBox.setModel(new DefaultComboBoxModel(this.ItemNames.getFriendlyLinesArray()));
        Item2ComboBox.setModel(new DefaultComboBoxModel(this.ItemNames.getFriendlyLinesArray()));
        Item3ComboBox.setModel(new DefaultComboBoxModel(this.ItemNames.getFriendlyLinesArray()));
        Item4ComboBox.setModel(new DefaultComboBoxModel(this.ItemNames.getFriendlyLinesArray()));

        // Setup battle type box.
        battleTypeComboBox.setModel(new DefaultComboBoxModel(this.BattleTypes.getFriendlyLinesArray()));

        // Setup top bar.
        for (int Index = 0; Index < TrNames.getLineCount(); ++Index) {
            trainerSelector.addItem(String.format("%s %s", TrClasses.getLine(Trainers.get(Index).GetAssignedClass()), TrNames.getLine(Index)));
        }

        // Update spinner max to match trainer count.
        int max = trainerSelector.getItemCount() - 1;
        if (max < 0) max = 0;
        trainerSpinner.setModel(new SpinnerNumberModel(0, 0, max, 1));
    }

>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
    @Override
    public String getTabName() {
        return "Trainer Editor";
    }

    @Override
    public boolean isGameSupported(GameInfo game) {
        return game.isGenV();
    }
<<<<<<< HEAD
    
    public WBTrainerData LoadEntryViaYml(Yaml yml) throws Exception {
        // Deserialize the YML into some bindings.
        VTrainerDataBinding trDataYml = YamlReflectUtil.deserialize(yml.root, VTrainerDataBinding.class);
        
        // Build a WBTrainer object.
        WBTrainerData trData = new WBTrainerData();
        
        trData.SetAssignedClass(trDataYml.trainerClass);
        trData.SetBattleType(trDataYml.battleType);
        
        // TODO: Workaround for type erasure.
        for (int Index = 0; Index < Math.min(trDataYml.items.length, 4); ++Index) {
            trData.SetItem(Index, UserEnumHandler.Parse(trDataYml.items[Index]));
        }
        
        trData.SetAIValue(trDataYml.AI);
        trData.SetCanHeal(trDataYml.canHeal == 0);
        trData.SetMoneyIndex(trDataYml.rewardMoney);
        trData.SetPrizeIndex(UserEnumHandler.Parse(trDataYml.rewardItem));
        
        for (int Index = 0; Index < Math.min(trDataYml.party.length, 6); ++Index) {
            WBTrainerPoke poke = new WBTrainerPoke();
            poke.SetAbility(trDataYml.party[Index].ability);
            poke.SetLevel(trDataYml.party[Index].level);
            poke.SetForm(trDataYml.party[Index].form);
            poke.SetGender(trDataYml.party[Index].gender);
            poke.SetSpecies(UserEnumHandler.Parse(trDataYml.party[Index].species));
            poke.SetIV(trDataYml.party[Index].difficultyValue);
            trData.AddPkmn(poke);
        }
        
        return trData;
    }
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        fileLoadPanel = new javax.swing.JPanel();
        btnOpenScrInIDE22 = new javax.swing.JButton();
        btnAddZoneData = new javax.swing.JButton();
        trainerSelector = new xstandard.gui.components.combobox.ComboBoxAndSpinner();
        jTrainerMetadata = new javax.swing.JTabbedPane();

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        fileLoadPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Trainer"));

        btnOpenScrInIDE22.setText("Open Trainer");
        btnOpenScrInIDE22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenScrInIDE22ActionPerformed(evt);
            }
        });

        btnAddZoneData.setText("+");
        btnAddZoneData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddZoneDataActionPerformed(evt);
            }
        });

        trainerSelector.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        trainerSelector.setMaximumRowCount(35);

        javax.swing.GroupLayout fileLoadPanelLayout = new javax.swing.GroupLayout(fileLoadPanel);
        fileLoadPanel.setLayout(fileLoadPanelLayout);
        fileLoadPanelLayout.setHorizontalGroup(
            fileLoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileLoadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(trainerSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAddZoneData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOpenScrInIDE22, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(923, Short.MAX_VALUE))
        );
        fileLoadPanelLayout.setVerticalGroup(
            fileLoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileLoadPanelLayout.createSequentialGroup()
                .addGroup(fileLoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(fileLoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnOpenScrInIDE22)
                        .addComponent(btnAddZoneData))
                    .addComponent(trainerSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTrainerMetadata)
                    .addComponent(fileLoadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fileLoadPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTrainerMetadata, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenScrInIDE22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenScrInIDE22ActionPerformed
        // Check if the user directory exists.
        FSFile trainer_user_directory = Instance.getProject().userData.getUserDataDir(UserData.UsrDirectory.TRAINER_DATA);
        int TrainerIndex = trainerSelector.getValueSpinner();
        String yaml_name = String.format("%d.yml", TrainerIndex);
        if (trainer_user_directory.exists() && trainer_user_directory.getChildCount() > 0) {
            FSFile trainer_yaml = trainer_user_directory.getChild(yaml_name);
            if (trainer_yaml.exists()) {
                WBTrainerData trdata;
                try {
                    trdata = this.LoadEntryViaYml(new Yaml(trainer_yaml));
                    this.jTrainerMetadata.add(String.format("%d - %s %s", TrainerIndex, this.TrClasses.getLine(trdata.GetAssignedClass()), this.TrNames.getLine(TrainerIndex)), 
                            new VTrainerComponent(this.Instance, trdata));
                } catch (Exception ex) {
                    Logger.getLogger(VTrainerEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("Did not find YAML");
            } 
        }
    }//GEN-LAST:event_btnOpenScrInIDE22ActionPerformed

    private void btnAddZoneDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddZoneDataActionPerformed

    }//GEN-LAST:event_btnAddZoneDataActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddZoneData;
    private javax.swing.JButton btnOpenScrInIDE22;
    private javax.swing.JPanel fileLoadPanel;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JTabbedPane jTrainerMetadata;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner trainerSelector;
    // End of variables declaration//GEN-END:variables
=======

    // UI Update Methods.
    // Trainer Properties
    private void UI_UpdatePartyList() {
        WBTrainerData CurrentTrainer = GetCurrentTrainer();
        // Dispose the outgoing party components before detaching them —
        // each one owns a 60Hz Timer that keeps firing otherwise, piling
        // up one extra active Timer per Pokemon per trainer switch. The
        // orphaned Timers were the main culprit behind the "editor slows
        // down over time" complaint.
        for (java.awt.Component c : partyListPanel.getComponents()) {
            if (c instanceof VTrainerPartyComponent) {
                ((VTrainerPartyComponent) c).dispose();
            }
        }
        partyListPanel.removeAll();
        if (CurrentTrainer != null) {
            for (int Index = 0; Index < CurrentTrainer.GetPkmnSize(); ++Index) {
                WBTrainerPoke CurrentPokemon = CurrentTrainer.GetPkmn(Index);
                final int slotIndex = Index;
                partyListPanel.add(new VTrainerPartyComponent(
                    this.Instance, CurrentPokemon, Index,
                    this.PkmnNames, this.ItemNames, this.MoveNames,
                    () -> {
                        CurrentTrainer.RemovePkmn(slotIndex);
                        UI_UpdatePartyList();
                    }));
            }
            // Push cards to the top
            partyListPanel.add(Box.createVerticalGlue());
            partyHeaderLabel.setText(String.format("Party (%d/6)", CurrentTrainer.GetPkmnSize()));
            addPokeBtn.setEnabled(CurrentTrainer.GetPkmnSize() < 6);
        }
        partyListPanel.revalidate();
        partyListPanel.repaint();
    }
    private void UI_UpdateNameEntry() {
        nameEntry.setText(TrNames.getLine(this.Trainers.indexOf(GetCurrentTrainer())));
    }
    private void UI_UpdateMiscSettings() {
        // Set settings options.
        canHeal.setSelected(GetCurrentTrainer().GetCanHeal());
        canOverrideMoves.setSelected(GetCurrentTrainer().CanOverrideMoves());
        canOverrideItems.setSelected(GetCurrentTrainer().CanOverrideHeldItem());
        battleTypeComboBox.setSelectedIndex(GetCurrentTrainer().GetBattleType());
    }
    private void UI_UpdateItem(int Index) {
        // Items from each item slot.
        switch (Index) {
            case 0:
                Item1ComboBox.setSelectedIndex(GetCurrentTrainer().GetItem(Index));
                break;
            case 1:
                Item2ComboBox.setSelectedIndex(GetCurrentTrainer().GetItem(Index));
                break;
            case 2:
                Item3ComboBox.setSelectedIndex(GetCurrentTrainer().GetItem(Index));
                break;
            case 3:
                Item4ComboBox.setSelectedIndex(GetCurrentTrainer().GetItem(Index));
                break;
        }
    }
    private void UI_UpdateAIInteger() {
        AIConfigurationInteger.setValue(GetCurrentTrainer().GetAIValue());
    }
    private void UI_UpdateAIFlag(int Index) {
        switch (Index) {
            case 0:
                AIBasicCheckbox.setSelected(GetCurrentTrainer().GetAIFlag(0));
                break;
            case 1:
                AIWillAttackCheckbox.setSelected(GetCurrentTrainer().GetAIFlag(1));
                break;
            case 2:
                AIAdvancedCheckbox.setSelected(GetCurrentTrainer().GetAIFlag(2));
                break;
            case 3:
                AIUnusedCheckbox.setSelected(GetCurrentTrainer().GetAIFlag(3));
                break;
            case 4:
                AIRivalCheckbox.setSelected(GetCurrentTrainer().GetAIFlag(4));
                break;
            case 5:
                AIUnused2Checkbox.setSelected(GetCurrentTrainer().GetAIFlag(5));
                break;
            case 6:
                AIUnused3Checkbox.setSelected(GetCurrentTrainer().GetAIFlag(6));
                break;
            case 7:
                AIMultiBattleCheckbox.setSelected(GetCurrentTrainer().GetAIFlag(7));
                break;
        }
    }
    private void UI_UpdateAIFlags() {
        for (int Index = 0; Index < Byte.SIZE; ++Index) {
            UI_UpdateAIFlag(Index);
        }
    }
    private void UI_UpdateTrainerClassList() {
        trainerClassList.setSelectedIndex(GetCurrentTrainer().GetAssignedClass());
        UI_UpdateTrainerSprite();
    }
    private void UI_UpdateTrainerSprite() {
        if (spriteAnimTimer != null) {
            spriteAnimTimer.stop();
        }
        spriteFrames.clear();
        spriteAnimIndex = 0;

        WBTrainerData trainer = GetCurrentTrainer();
        if (trainer == null) {
            trainerSpriteLabel.setIcon(null);
            trainerSpriteLabel.setText("?");
            return;
        }

        int trainerClass = trainer.GetAssignedClass();
        VLaunchpad mc = Instance.getMissionControl(VLaunchpad.class);
        
        List<BufferedImage> frames = SpriteImageLoader.loadTrainerSpriteFrames(
            mc.fs, mc.game, trainerClass);

        if (frames.isEmpty()) {
            trainerSpriteLabel.setIcon(null);
            trainerSpriteLabel.setText("No sprite");
            return;
        }

        // Convert frames to ImageIcons (already 96x96 from the loader)
        for (BufferedImage frame : frames) {
            spriteFrames.add(new ImageIcon(frame));
        }

        // Show frame 0 immediately
        trainerSpriteLabel.setIcon(spriteFrames.get(0));
        trainerSpriteLabel.setText(null);

        // Start ping-pong animation if there are multiple frames
        if (spriteFrames.size() > 1) {
            if (spriteAnimTimer == null) {
                spriteAnimTimer = new Timer(SPRITE_ANIM_INTERVAL_MS, e -> advanceSpriteAnim());
            }
            spriteAnimTimer.start();
        }
    }

    private void advanceSpriteAnim() {
        if (spriteFrames.isEmpty()) return;
        // Sequential loop: the frames form one full breathing cycle
        spriteAnimIndex = (spriteAnimIndex + 1) % spriteFrames.size();
        trainerSpriteLabel.setIcon(spriteFrames.get(spriteAnimIndex));
    }
    private void UI_UpdateMovesEnabled() {
        WBTrainerData CurrentTrainer = GetCurrentTrainer();
        if (CurrentTrainer != null) {
//            Move1ComboBox.setEnabled(CurrentTrainer.CanOverrideMoves());
//            Move2ComboBox.setEnabled(CurrentTrainer.CanOverrideMoves());
//            Move3ComboBox.setEnabled(CurrentTrainer.CanOverrideMoves());
//            Move4ComboBox.setEnabled(CurrentTrainer.CanOverrideMoves());
        }
    }
    private void UI_UpdateHeldItemEnabled() {
        WBTrainerData CurrentTrainer = GetCurrentTrainer();
        if (CurrentTrainer != null) {
//            heldItemComboBox.setEnabled(CurrentTrainer.CanOverrideHeldItem());
        }
    }
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
}
