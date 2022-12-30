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
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        battleTypeComboBox1 = new javax.swing.JComboBox<>();
        areaLabel2 = new javax.swing.JLabel();
        AIConfigurationInteger1 = new javax.swing.JSpinner();
        areaLabel3 = new javax.swing.JLabel();
        AIConfigurationInteger2 = new javax.swing.JSpinner();
        battleTypeComboBox2 = new javax.swing.JComboBox<>();
        areaLabel4 = new javax.swing.JLabel();
        areaLabel5 = new javax.swing.JLabel();
        battleTypeComboBox3 = new javax.swing.JComboBox<>();
        areaLabel6 = new javax.swing.JLabel();
        AIConfigurationInteger3 = new javax.swing.JSpinner();
        areaLabel7 = new javax.swing.JLabel();
        battleTypeComboBox4 = new javax.swing.JComboBox<>();
        itemsPanel3 = new javax.swing.JPanel();
        Item1ComboBox3 = new javax.swing.JComboBox<>();
        Item2ComboBox9 = new javax.swing.JComboBox<>();
        Item3ComboBox3 = new javax.swing.JComboBox<>();
        Item4ComboBox3 = new javax.swing.JComboBox<>();
        areaLabel9 = new javax.swing.JLabel();

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
                .addContainerGap(40, Short.MAX_VALUE))
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
                .addGroup(aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aiPanelLayout.createSequentialGroup()
                        .addGroup(aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AIConfigurationIntegerLabel)
                            .addComponent(AIConfigurationInteger, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .addGroup(aiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(AIUnused3Checkbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(AIMultiBattleCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                    .addComponent(Item3ComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Item1ComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Item2ComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                            .addComponent(nameEntry))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(canOverrideMoves, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(canOverrideItems, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(4, 4, 4))
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(canHeal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(5, 5, 5))
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(settingsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(88, 88, 88))
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(battleTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(71, 71, 71))
                    .addComponent(battleTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(areaLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(settingsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(generalPanelLayout.createSequentialGroup()
                                .addComponent(canHeal)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(canOverrideItems)
                                .addGap(6, 6, 6)
                                .addComponent(canOverrideMoves)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                                .addComponent(battleTypeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(battleTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(nameEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(propertiesPanelLayout.createSequentialGroup()
                        .addComponent(itemsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rewardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(generalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(aiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(rewardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(aiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        trainerLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        trainerLabel.setText("Trainer");

        trainerSelector.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        trainerSelector.setMaximumRowCount(35);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Party & Trainer Text Table"));

        jTabbedPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab("Trainer Text Table", jPanel5);

        jTabbedPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Pkmn"));

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel4.setBackground(new java.awt.Color(102, 102, 102));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Preview Here");
        jLabel4.setOpaque(true);

        battleTypeComboBox1.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        battleTypeComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        areaLabel2.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        areaLabel2.setText("Level");

        AIConfigurationInteger1.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N

        areaLabel3.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        areaLabel3.setText("Form");

        AIConfigurationInteger2.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N

        battleTypeComboBox2.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        battleTypeComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        areaLabel4.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        areaLabel4.setText("Ability");

        areaLabel5.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        areaLabel5.setText("Gender");

        battleTypeComboBox3.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        battleTypeComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        areaLabel6.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        areaLabel6.setText("IVs");

        AIConfigurationInteger3.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N

        areaLabel7.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        areaLabel7.setText("Held Item");

        battleTypeComboBox4.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        battleTypeComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        itemsPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Items"));

        Item1ComboBox3.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        Item1ComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        Item2ComboBox9.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        Item2ComboBox9.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        Item3ComboBox3.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        Item3ComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        Item4ComboBox3.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        Item4ComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Disabled", "Enabled", "Used" }));

        javax.swing.GroupLayout itemsPanel3Layout = new javax.swing.GroupLayout(itemsPanel3);
        itemsPanel3.setLayout(itemsPanel3Layout);
        itemsPanel3Layout.setHorizontalGroup(
            itemsPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemsPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemsPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Item3ComboBox3, 0, 112, Short.MAX_VALUE)
                    .addComponent(Item1ComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemsPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Item2ComboBox9, 0, 112, Short.MAX_VALUE)
                    .addComponent(Item4ComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        itemsPanel3Layout.setVerticalGroup(
            itemsPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemsPanel3Layout.createSequentialGroup()
                .addGroup(itemsPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(itemsPanel3Layout.createSequentialGroup()
                        .addComponent(Item2ComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(Item4ComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(itemsPanel3Layout.createSequentialGroup()
                        .addComponent(Item1ComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(Item3ComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        areaLabel9.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        areaLabel9.setText("Species");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(areaLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(40, 40, 40))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(areaLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(36, 36, 36))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(areaLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(AIConfigurationInteger1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(areaLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AIConfigurationInteger2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(battleTypeComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(battleTypeComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(areaLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(areaLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(22, 22, 22)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(battleTypeComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(areaLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AIConfigurationInteger3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(battleTypeComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(itemsPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(areaLabel9)
                            .addComponent(battleTypeComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(areaLabel2)
                            .addComponent(areaLabel3)
                            .addComponent(AIConfigurationInteger2, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AIConfigurationInteger1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(areaLabel4)
                            .addComponent(battleTypeComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(areaLabel7)
                            .addComponent(battleTypeComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(battleTypeComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(areaLabel5)
                            .addComponent(areaLabel6)
                            .addComponent(AIConfigurationInteger3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemsPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {AIConfigurationInteger1, AIConfigurationInteger2, battleTypeComboBox1});

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane4)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(152, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("Party", jPanel4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane3))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTabbedPane3)
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
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator1)
                        .addGap(22, 22, 22))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(propertiesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(12, Short.MAX_VALUE))))
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
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(propertiesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12, 12, 12))
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
    private javax.swing.JSpinner AIConfigurationInteger1;
    private javax.swing.JSpinner AIConfigurationInteger2;
    private javax.swing.JSpinner AIConfigurationInteger3;
    private javax.swing.JLabel AIConfigurationIntegerLabel;
    private javax.swing.JCheckBox AIMultiBattleCheckbox;
    private javax.swing.JCheckBox AIRivalCheckbox;
    private javax.swing.JCheckBox AIUnused2Checkbox;
    private javax.swing.JCheckBox AIUnused3Checkbox;
    private javax.swing.JCheckBox AIUnusedCheckbox;
    private javax.swing.JCheckBox AIWillAttackCheckbox;
    private javax.swing.JComboBox<String> Item1ComboBox;
    private javax.swing.JComboBox<String> Item1ComboBox3;
    private javax.swing.JComboBox<String> Item2ComboBox;
    private javax.swing.JComboBox<String> Item2ComboBox9;
    private javax.swing.JComboBox<String> Item3ComboBox;
    private javax.swing.JComboBox<String> Item3ComboBox3;
    private javax.swing.JComboBox<String> Item4ComboBox;
    private javax.swing.JComboBox<String> Item4ComboBox3;
    private javax.swing.JPanel aiPanel;
    private javax.swing.JLabel areaLabel;
    private javax.swing.JLabel areaLabel2;
    private javax.swing.JLabel areaLabel3;
    private javax.swing.JLabel areaLabel4;
    private javax.swing.JLabel areaLabel5;
    private javax.swing.JLabel areaLabel6;
    private javax.swing.JLabel areaLabel7;
    private javax.swing.JLabel areaLabel9;
    private javax.swing.JComboBox<String> battleTypeComboBox;
    private javax.swing.JComboBox<String> battleTypeComboBox1;
    private javax.swing.JComboBox<String> battleTypeComboBox2;
    private javax.swing.JComboBox<String> battleTypeComboBox3;
    private javax.swing.JComboBox<String> battleTypeComboBox4;
    private javax.swing.JLabel battleTypeLabel;
    private javax.swing.JCheckBox canHeal;
    private javax.swing.JCheckBox canOverrideItems;
    private javax.swing.JCheckBox canOverrideMoves;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JPanel itemsPanel;
    private javax.swing.JPanel itemsPanel3;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTextField nameEntry;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JLabel rewardItemLabel;
    private javax.swing.JSpinner rewardItemSpinner;
    private javax.swing.JLabel rewardMoneyLabel;
    private javax.swing.JSpinner rewardMoneySpinner;
    private javax.swing.JPanel rewardsPanel;
    private javax.swing.JLabel settingsLabel;
    private javax.swing.JList<String> trainerClassList;
    private javax.swing.JLabel trainerLabel;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner trainerSelector;
    // End of variables declaration//GEN-END:variables
}
