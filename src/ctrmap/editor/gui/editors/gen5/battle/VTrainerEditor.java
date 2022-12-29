package ctrmap.editor.gui.editors.gen5.battle;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.gui.editors.text.loaders.ITextArcType;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.pokemon.gen5.battle.WBTrainerData;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.MessageHandler;
import ctrmap.formats.pokemon.text.MsgStr;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.VRTC;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    
    public VTrainerEditor(CTRMap Instance) {
        initComponents();
        this.Trainers = new ArrayList<>();
        this.Instance = Instance;
        trainerSelector.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (Instance != null) {
                    UpdateNameEntry();
                    UpdateMiscSettings();
                    UpdateAIInteger();
                    UpdateTrainerClassList();
                    for (int Index = 0; Index < Byte.SIZE; ++Index) {
                        UpdateAIFlag(Index);
                    }
                    for (int Index = 0; Index < WBTrainerData.ITEMS_COUNT_MAX; ++Index) {
                        UpdateItem(Index);
                    }
                }
            }
        });
        trainerClassList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (Instance != null) {
                    int SelIndex = trainerClassList.getSelectedIndex(), TrIndex = Trainers.indexOf(GetCurrentTrainer());
                    if (SelIndex < 0 || TrIndex < 0 || TrIndex > FS().NARCGetDataMax(NARCRef.TRAINER_DATA)) {
                        return;
                    }
                    GetCurrentTrainer().SetAssignedClass(SelIndex);
//                    trainerSelector.setEditable(true);
//                    trainerSelector.getCB().getEditor().setItem(String.format("%s %s", TrClasses.getLine(GetCurrentTrainer().GetAssignedClass()), 
//                    TrNames.getLine(TrIndex)));
//                    trainerSelector.setEditable(false);
                }         
            }
        });
    }
    
    private void LoadAllTextArchives() {
        this.TrClasses = LoadSystemTextArchive(SystemTextLUT.get("TrainerClasses"));
        this.TrNames = LoadSystemTextArchive(SystemTextLUT.get("Trainers"));
        this.TrDialogue = LoadSystemTextArchive(SystemTextLUT.get("TrainerDialogue"));
        this.AbilNames = LoadSystemTextArchive(SystemTextLUT.get("Abilities"));
        this.ItemNames = LoadSystemTextArchive(SystemTextLUT.get("Items"));
        this.ItemDescs = LoadSystemTextArchive(SystemTextLUT.get("ItemDescriptions"));
        this.BattleTypes = LoadSystemTextArchive(SystemTextLUT.get("BattleTypes"));
    }
    
    private void LoadAllTrainers() throws IOException {
        for (int Index = 0; Index < FS().NARCGetDataMax(NARCRef.TRAINER_DATA); ++Index) {
            FSFile TrDat = FS().NARCGet(NARCRef.TRAINER_DATA, Index), 
                    TrPoke = FS().NARCGet(NARCRef.TRAINER_POKEMON, Index);
            this.Trainers.add(new WBTrainerData(TrDat.getDataIOStream(), TrPoke.getDataIOStream()));
        }
    }
    
    private void UpdateNameEntry() {
        nameEntry.setText(TrNames.getLine(this.Trainers.indexOf(GetCurrentTrainer())));
    }
    
    private WBTrainerData GetCurrentTrainer() {
        // Load current trainer.
        int Index = trainerSelector.getValueSpinner();
        if (Index < 0 || Index > FS().NARCGetDataMax(NARCRef.TRAINER_DATA)) {
            return null;
        }
        return Trainers.get(Index);
    }
    
    private void UpdateMiscSettings() { 
        // Set settings options.
        canHeal.setSelected(GetCurrentTrainer().GetCanHeal());
        canOverrideMoves.setSelected(GetCurrentTrainer().CanOverrideMoves());
        canOverrideItems.setSelected(GetCurrentTrainer().CanOverrideHeldItem());
        battleTypeComboBox.setSelectedIndex(GetCurrentTrainer().GetBattleType());
    }
    
    private void UpdateItem(int Index) {
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
    
    private void UpdateAIInteger() {   
        AIConfigurationInteger.setValue(GetCurrentTrainer().GetAIValue());
    }
    
    private void UpdateAIFlag(int Index) {
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
    
    private void UpdateTrainerClassList() {
        trainerClassList.setSelectedIndex(GetCurrentTrainer().GetAssignedClass());
    }
    
    @Override
    public void onProjectLoaded(CTRMapProject proj) {
        try {
            LoadAllTrainers();
            LoadAllTextArchives();
        } catch (IOException ex) {
            Logger.getLogger(VTrainerEditor.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        // Setup trainer class list model.
        DefaultListModel TrClassModel = new DefaultListModel();
        for (MsgStr e : this.TrClasses.lines) {
            TrClassModel.addElement(e);
        }
        trainerClassList.setModel(TrClassModel);
        
        // Setup top bar.
        for (int Index = 0; Index < TrNames.getLineCount(); ++Index) {
            trainerSelector.addItem(String.format("%s %s", TrClasses.getLine(
                    Trainers.get(Index).GetAssignedClass()), TrNames.getLine(Index)));
        }
        
        // Setup item boxes.
        // TODO: Fix! There has to be a better way to do this...
        DefaultComboBoxModel ItemModel = new DefaultComboBoxModel(),
                Item2Model = new DefaultComboBoxModel(),
                Item3Model = new DefaultComboBoxModel(),
                Item4Model = new DefaultComboBoxModel();
        for (MsgStr e : this.ItemNames.lines) {
            ItemModel.addElement(e);
            Item2Model.addElement(e);
            Item3Model.addElement(e);
            Item4Model.addElement(e);
        }
        Item1ComboBox.setModel(ItemModel);
        Item2ComboBox.setModel(Item2Model);
        Item3ComboBox.setModel(Item3Model);
        Item4ComboBox.setModel(Item4Model);
        
        // Setup battle type box.
        DefaultComboBoxModel BattleTypeModel = new DefaultComboBoxModel();
        for (MsgStr e : this.BattleTypes.lines) {
            BattleTypeModel.addElement(e);
        }
        battleTypeComboBox.setModel(BattleTypeModel);
    }
            
    
    TextFile LoadSystemTextArchive(int Index) {
	return new TextFile(FS().NARCGet(NARCRef.MSGDATA_SYSTEM, Index), GenVMessageHandler.INSTANCE);
    }
    
    NTRGameFS FS() {
        return Instance.getMissionControl(VLaunchpad.class).fs;
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
        propertiesPanel = new javax.swing.JPanel();
        aiPanel = new javax.swing.JPanel();
        AIBasicCheckbox = new javax.swing.JCheckBox();
        AIWillAttackCheckbox = new javax.swing.JCheckBox();
        AIAdvancedCheckbox = new javax.swing.JCheckBox();
        AIUnusedCheckbox = new javax.swing.JCheckBox();
        AIRivalCheckbox = new javax.swing.JCheckBox();
        AIUnused2Checkbox = new javax.swing.JCheckBox();
        AIUnused3Checkbox = new javax.swing.JCheckBox();
        AIMultiBattleCheckbox = new javax.swing.JCheckBox();
        AIConfigurationInteger = new javax.swing.JSpinner();
        AIConfigurationIntegerLabel = new javax.swing.JLabel();
        itemsPanel = new javax.swing.JPanel();
        Item1ComboBox = new javax.swing.JComboBox<>();
        Item2ComboBox = new javax.swing.JComboBox<>();
        Item3ComboBox = new javax.swing.JComboBox<>();
        Item4ComboBox = new javax.swing.JComboBox<>();
        generalPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        trainerClassList = new javax.swing.JList<>();
        areaLabel = new javax.swing.JLabel();
        canOverrideMoves = new javax.swing.JCheckBox();
        canOverrideItems = new javax.swing.JCheckBox();
        canHeal = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        settingsLabel = new javax.swing.JLabel();
        nameEntry = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        battleTypeLabel = new javax.swing.JLabel();
        battleTypeComboBox = new javax.swing.JComboBox<>();
        rewardsPanel = new javax.swing.JPanel();
        rewardItemLabel = new javax.swing.JLabel();
        rewardItemSpinner = new javax.swing.JSpinner();
        rewardMoneySpinner = new javax.swing.JSpinner();
        rewardMoneyLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        trainerLabel = new javax.swing.JLabel();
        trainerSelector = new xstandard.gui.components.combobox.ComboBoxAndSpinner();
        partyAndTextPanel = new javax.swing.JPanel();
        partyAndTextTabbedPane = new javax.swing.JTabbedPane();
        partyTab = new javax.swing.JPanel();
        textTab = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Trainer Properties"));

        aiPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("AI Configuration"));

        AIBasicCheckbox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIBasicCheckbox.setText("Basic AI");

        AIWillAttackCheckbox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIWillAttackCheckbox.setText("Will Attack");

        AIAdvancedCheckbox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIAdvancedCheckbox.setText("Advanced");

        AIUnusedCheckbox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIUnusedCheckbox.setText("Unused");

        AIRivalCheckbox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIRivalCheckbox.setText("Rival");

        AIUnused2Checkbox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIUnused2Checkbox.setText("Unused 2");

        AIUnused3Checkbox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIUnused3Checkbox.setText("Unused 3");

        AIMultiBattleCheckbox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIMultiBattleCheckbox.setText("Multi-Battle");

        AIConfigurationInteger.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N

        AIConfigurationIntegerLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        AIConfigurationIntegerLabel.setText("Configuration Integer");

        javax.swing.GroupLayout aiPanelLayout = new javax.swing.GroupLayout(aiPanel);
        aiPanel.setLayout(aiPanelLayout);
        aiPanelLayout.setHorizontalGroup(
            aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aiPanelLayout.createSequentialGroup()
                        .addComponent(AIUnused3Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AIMultiBattleCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AIConfigurationIntegerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AIConfigurationInteger, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(aiPanelLayout.createSequentialGroup()
                        .addComponent(AIBasicCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AIWillAttackCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AIAdvancedCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(aiPanelLayout.createSequentialGroup()
                        .addComponent(AIUnusedCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AIRivalCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AIUnused2Checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        aiPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {AIBasicCheckbox, AIWillAttackCheckbox});

        aiPanelLayout.setVerticalGroup(
            aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aiPanelLayout.createSequentialGroup()
                .addGroup(aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AIBasicCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AIWillAttackCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AIAdvancedCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AIUnusedCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AIRivalCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AIUnused2Checkbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AIUnused3Checkbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AIMultiBattleCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(aiPanelLayout.createSequentialGroup()
                        .addGroup(aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AIConfigurationIntegerLabel)
                            .addComponent(AIConfigurationInteger, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(12, Short.MAX_VALUE))))
        );

        itemsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Items"));

        Item1ComboBox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        Item1ComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        Item2ComboBox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        Item2ComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        Item3ComboBox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        Item3ComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        Item4ComboBox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        Item4ComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        javax.swing.GroupLayout itemsPanelLayout = new javax.swing.GroupLayout(itemsPanel);
        itemsPanel.setLayout(itemsPanelLayout);
        itemsPanelLayout.setHorizontalGroup(
            itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Item3ComboBox, 0, 121, Short.MAX_VALUE)
                    .addComponent(Item1ComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Item2ComboBox, 0, 121, Short.MAX_VALUE)
                    .addComponent(Item4ComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        itemsPanelLayout.setVerticalGroup(
            itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemsPanelLayout.createSequentialGroup()
                .addGroup(itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Item1ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Item2ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Item3ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Item4ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

        trainerClassList.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        trainerClassList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        trainerClassList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        trainerClassList.setAutoscrolls(false);
        jScrollPane1.setViewportView(trainerClassList);

        areaLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        areaLabel.setText("Trainer Class");

        canOverrideMoves.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        canOverrideMoves.setText("Can Override Moves");

        canOverrideItems.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        canOverrideItems.setText("Can Override Items");

        canHeal.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        canHeal.setText("Can Heal Pokemon");

        jLabel1.setBackground(new java.awt.Color(102, 102, 102));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Preview Here");
        jLabel1.setOpaque(true);

        settingsLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        settingsLabel.setText("Settings");

        nameEntry.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        nameEntry.setText("Name");

        jButton1.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        jButton1.setText("Edit Trainer Classes");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        battleTypeLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        battleTypeLabel.setText("Battle Type");

        battleTypeComboBox.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        battleTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(areaLabel)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(canOverrideMoves)
                    .addComponent(canOverrideItems)
                    .addComponent(canHeal)
                    .addComponent(settingsLabel)
                    .addComponent(battleTypeLabel)
                    .addComponent(battleTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(areaLabel)
                            .addComponent(settingsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(canHeal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(canOverrideItems)
                        .addGap(6, 6, 6)
                        .addComponent(canOverrideMoves)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addComponent(battleTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(battleTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(6, 6, 6))
        );

        rewardsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Rewards"));

        rewardItemLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        rewardItemLabel.setText("Reward Item");

        rewardItemSpinner.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N

        rewardMoneySpinner.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N

        rewardMoneyLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        rewardMoneyLabel.setText("Reward Money");

        javax.swing.GroupLayout rewardsPanelLayout = new javax.swing.GroupLayout(rewardsPanel);
        rewardsPanel.setLayout(rewardsPanelLayout);
        rewardsPanelLayout.setHorizontalGroup(
            rewardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rewardsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rewardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rewardMoneyLabel)
                    .addComponent(rewardItemLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(rewardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(rewardItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(rewardMoneySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        rewardsPanelLayout.setVerticalGroup(
            rewardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rewardsPanelLayout.createSequentialGroup()
                .addGroup(rewardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rewardItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rewardItemLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rewardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rewardMoneyLabel)
                    .addComponent(rewardMoneySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout propertiesPanelLayout = new javax.swing.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(aiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(propertiesPanelLayout.createSequentialGroup()
                            .addComponent(itemsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(rewardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(generalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(itemsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rewardsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aiPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        trainerLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        trainerLabel.setText("Trainer");

        trainerSelector.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        trainerSelector.setMaximumRowCount(35);

        partyAndTextPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Other"));

        partyAndTextTabbedPane.setToolTipText("");
        partyAndTextTabbedPane.setFont(new java.awt.Font("Droid Sans", 0, 14)); // NOI18N

        javax.swing.GroupLayout partyTabLayout = new javax.swing.GroupLayout(partyTab);
        partyTab.setLayout(partyTabLayout);
        partyTabLayout.setHorizontalGroup(
            partyTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 548, Short.MAX_VALUE)
        );
        partyTabLayout.setVerticalGroup(
            partyTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );

        partyAndTextTabbedPane.addTab("Party", partyTab);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Message Type", "Index"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable1);

        javax.swing.GroupLayout textTabLayout = new javax.swing.GroupLayout(textTab);
        textTab.setLayout(textTabLayout);
        textTabLayout.setHorizontalGroup(
            textTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, textTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(textTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE))
                .addContainerGap())
        );
        textTabLayout.setVerticalGroup(
            textTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, textTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                .addGap(40, 40, 40))
        );

        partyAndTextTabbedPane.addTab("Text Table", textTab);

        javax.swing.GroupLayout partyAndTextPanelLayout = new javax.swing.GroupLayout(partyAndTextPanel);
        partyAndTextPanel.setLayout(partyAndTextPanelLayout);
        partyAndTextPanelLayout.setHorizontalGroup(
            partyAndTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(partyAndTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(partyAndTextTabbedPane)
                .addContainerGap())
        );
        partyAndTextPanelLayout.setVerticalGroup(
            partyAndTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(partyAndTextPanelLayout.createSequentialGroup()
                .addComponent(partyAndTextTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(trainerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trainerSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(propertiesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(partyAndTextPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1)))
                .addGap(22, 22, 22))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(trainerSelector, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(trainerLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propertiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(partyAndTextPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        propertiesPanel.getAccessibleContext().setAccessibleName("0");
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    @Override
    public String getTabName() {
        return "Trainer Editor";
    }

    @Override
    public boolean isGameSupported(GameInfo game) {
        return game.isGenV();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox AIAdvancedCheckbox;
    private javax.swing.JCheckBox AIBasicCheckbox;
    private javax.swing.JSpinner AIConfigurationInteger;
    private javax.swing.JLabel AIConfigurationIntegerLabel;
    private javax.swing.JCheckBox AIMultiBattleCheckbox;
    private javax.swing.JCheckBox AIRivalCheckbox;
    private javax.swing.JCheckBox AIUnused2Checkbox;
    private javax.swing.JCheckBox AIUnused3Checkbox;
    private javax.swing.JCheckBox AIUnusedCheckbox;
    private javax.swing.JCheckBox AIWillAttackCheckbox;
    private javax.swing.JComboBox<String> Item1ComboBox;
    private javax.swing.JComboBox<String> Item2ComboBox;
    private javax.swing.JComboBox<String> Item3ComboBox;
    private javax.swing.JComboBox<String> Item4ComboBox;
    private javax.swing.JPanel aiPanel;
    private javax.swing.JLabel areaLabel;
    private javax.swing.JComboBox<String> battleTypeComboBox;
    private javax.swing.JLabel battleTypeLabel;
    private javax.swing.JCheckBox canHeal;
    private javax.swing.JCheckBox canOverrideItems;
    private javax.swing.JCheckBox canOverrideMoves;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JPanel itemsPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField nameEntry;
    private javax.swing.JPanel partyAndTextPanel;
    private javax.swing.JTabbedPane partyAndTextTabbedPane;
    private javax.swing.JPanel partyTab;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JLabel rewardItemLabel;
    private javax.swing.JSpinner rewardItemSpinner;
    private javax.swing.JLabel rewardMoneyLabel;
    private javax.swing.JSpinner rewardMoneySpinner;
    private javax.swing.JPanel rewardsPanel;
    private javax.swing.JLabel settingsLabel;
    private javax.swing.JPanel textTab;
    private javax.swing.JList<String> trainerClassList;
    private javax.swing.JLabel trainerLabel;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner trainerSelector;
    // End of variables declaration//GEN-END:variables
}
