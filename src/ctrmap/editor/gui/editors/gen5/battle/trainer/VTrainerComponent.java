package ctrmap.editor.gui.editors.gen5.battle.trainer;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.util.SystemTextLUT;
import ctrmap.editor.gui.editors.util.TextSingleton;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerData;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerPoke;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;

public class VTrainerComponent extends javax.swing.JPanel {
    private WBTrainerData trainer;
    private CTRMap Instance;
    
    private TextFile TrClasses, TrNames, TrDialogue, BattleTypes, ItemNames, ItemDescs, MoveNames, MoveDescs, AbilNames, PkmnNames;
    
    public VTrainerComponent() {
        initComponents();
    }
    
    NTRGameFS FS() {
        return Instance.getMissionControl(ctrmap.missioncontrol_ntr.VLaunchpad.class).fs;
    }
        
    TextFile LoadTextFile(String File) {
        return TextSingleton.Load(FS(), NARCRef.MSGDATA_SYSTEM, SystemTextLUT.get(File));
    }
        
    public VTrainerComponent(CTRMap Instance, WBTrainerData trainer) {
        initComponents();
        this.Instance = Instance;
        this.trainer = trainer;
        
        // Trainer settings.        
        trainerClassList.addListSelectionListener((ListSelectionEvent lse) -> {
            this.trainer.SetAssignedClass(trainerClassList.getSelectedIndex());         
        });
        canHeal.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetCanHeal(canHeal.isSelected());
            }
        });
        canOverrideMoves.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetCanOverrideMoves(canOverrideMoves.isSelected());
                UI_UpdateMovesEnabled();
            }
        });
        canOverrideItems.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetCanOverrideHeldItem(canOverrideItems.isSelected());
                UI_UpdateHeldItemEnabled();
            }
        });
        battleTypeComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                int SelIndex = battleTypeComboBox.getSelectedIndex();
                if (SelIndex >= 0) {
                    this.trainer.SetBattleType(SelIndex);
                }            
            }
        });
        
        // Trainer items.
        Item1ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                int SelIndex = Item1ComboBox.getSelectedIndex();
                if (SelIndex >= 0) {
                    this.trainer.SetItem(0, SelIndex);
                }            
            }
        });
        
        Item2ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                int SelIndex = Item2ComboBox.getSelectedIndex();
                if (SelIndex >= 0) {
                    this.trainer.SetItem(1, SelIndex);
                }            
            }
        });
        
        Item3ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                int SelIndex = Item3ComboBox.getSelectedIndex();
                if (SelIndex >= 0) {
                    this.trainer.SetItem(2, SelIndex);
                }            
            }
        });
        
        Item4ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null) {
                int SelIndex = Item4ComboBox.getSelectedIndex();
                if (SelIndex >= 0) {
                    this.trainer.SetItem(3, SelIndex);
                }            
            }
        });
        
        // Trainer rewards.
        rewardItemSpinner.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                int SelIndex = (int) rewardItemSpinner.getValue();
                if (SelIndex >= 0) {
                    this.trainer.SetPrizeIndex(SelIndex);
                }             
            }
        });
        
        rewardMoneySpinner.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                int SelIndex = (int) rewardMoneySpinner.getValue();
                if (SelIndex >= 0) {
                    this.trainer.SetMoneyIndex(SelIndex);
                }             
            }
        });
        
        // AI
        AIBasicCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetAIFlag(0, AIBasicCheckbox.isSelected());
                UI_UpdateAIInteger();
            }
        });
        
        AIWillAttackCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetAIFlag(1, AIWillAttackCheckbox.isSelected());
                UI_UpdateAIInteger();        
            }
        });
        
        AIAdvancedCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetAIFlag(2, AIAdvancedCheckbox.isSelected());
                UI_UpdateAIInteger();
            }
        });
        
        AIUnusedCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetAIFlag(3, AIUnusedCheckbox.isSelected());
                UI_UpdateAIInteger();
            }
        });
        AIRivalCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetAIFlag(4, AIRivalCheckbox.isSelected());
                UI_UpdateAIInteger();          
            }
        });
        AIUnused2Checkbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetAIFlag(5, AIUnused2Checkbox.isSelected());
                UI_UpdateAIInteger();            
            }
        });
        AIUnused3Checkbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetAIFlag(6, AIUnused3Checkbox.isSelected());
                UI_UpdateAIInteger();          
            }
        });
        AIMultiBattleCheckbox.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                this.trainer.SetAIFlag(7, AIMultiBattleCheckbox.isSelected());
                UI_UpdateAIInteger();            
            }
        });
        AIConfigurationInteger.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null) {
                int AIVal = (int) AIConfigurationInteger.getValue();
                if (AIVal >= 0) {
                    this.trainer.SetAIValue(AIVal);
                    UI_UpdateAIFlags();
                }             
            }
        });
        
        this.TrClasses = LoadTextFile("TrainerClasses");
        this.TrNames = LoadTextFile("Trainers");
        this.TrDialogue = LoadTextFile("TrainerDialogue");
        this.ItemNames = LoadTextFile("Items");
        this.ItemDescs = LoadTextFile("ItemDescriptions");
        this.BattleTypes = LoadTextFile("BattleTypes");
        this.PkmnNames = LoadTextFile("Pokemon");
        this.MoveNames = LoadTextFile("Moves");
        
        trainerClassList.setListData(this.TrClasses.getFriendlyLinesArray());
        
        // TODO: Why...
        DefaultComboBoxModel items_cbm = new DefaultComboBoxModel();
        for (String line : this.ItemNames.getFriendlyLinesArray()) {
            items_cbm.addElement(line);
        }
        Item1ComboBox.setModel(items_cbm);
        Item2ComboBox.setModel(items_cbm);
        Item3ComboBox.setModel(items_cbm);
        Item4ComboBox.setModel(items_cbm);
        
        UI_UpdateNameEntry();
        UI_UpdateMiscSettings();
        UI_UpdateAIInteger();
        UI_UpdateTrainerClassList();
        UI_UpdateTrainerPokeTabs();
        UI_UpdateAIFlags();
        for (int Index = 0; Index < WBTrainerData.ITEMS_COUNT_MAX; ++Index) {
            UI_UpdateItem(Index);
        }
        UI_UpdateMovesEnabled();
        UI_UpdateHeldItemEnabled();
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
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
        battleTypeLabel = new javax.swing.JLabel();
        battleTypeComboBox = new javax.swing.JComboBox<>();
        rewardsPanel = new javax.swing.JPanel();
        rewardItemLabel = new javax.swing.JLabel();
        rewardItemSpinner = new javax.swing.JSpinner();
        rewardMoneySpinner = new javax.swing.JSpinner();
        rewardMoneyLabel = new javax.swing.JLabel();
        btnOpenScrInIDE23 = new javax.swing.JButton();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jTrainerPartyEntries = new javax.swing.JPanel();

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
                .addContainerGap(13, Short.MAX_VALUE))
        );
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
                        .addContainerGap(12, Short.MAX_VALUE))
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
                    .addComponent(Item3ComboBox, 0, 138, Short.MAX_VALUE)
                    .addComponent(Item1ComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Item2ComboBox, 0, 139, Short.MAX_VALUE)
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
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                            .addComponent(nameEntry))))
                .addGap(18, 18, 18)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(canOverrideMoves, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, generalPanelLayout.createSequentialGroup()
                        .addComponent(canOverrideItems, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(4, 4, 4))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, generalPanelLayout.createSequentialGroup()
                        .addComponent(canHeal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(5, 5, 5))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, generalPanelLayout.createSequentialGroup()
                        .addComponent(settingsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(88, 88, 88))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, generalPanelLayout.createSequentialGroup()
                        .addComponent(battleTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(71, 71, 71))
                    .addComponent(battleTypeComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addComponent(areaLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        rewardsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Rewards"));

        rewardItemLabel.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        rewardItemLabel.setText("Reward Item");

        rewardItemSpinner.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        rewardItemSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

        rewardMoneySpinner.setFont(new java.awt.Font("Droid Sans", 0, 12)); // NOI18N
        rewardMoneySpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));

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
                .addGap(0, 0, Short.MAX_VALUE))
        );

        btnOpenScrInIDE23.setText("Save Trainer");
        btnOpenScrInIDE23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenScrInIDE23ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout propertiesPanelLayout = new javax.swing.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(propertiesPanelLayout.createSequentialGroup()
                        .addComponent(itemsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rewardsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnOpenScrInIDE23)
                        .addComponent(aiPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propertiesPanelLayout.createSequentialGroup()
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(propertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(rewardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(aiPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnOpenScrInIDE23)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(propertiesPanel);

        jTabbedPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jTrainerPartyEntries.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jTrainerPartyEntries.setLayout(new java.awt.GridLayout(3, 2));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTrainerPartyEntries, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTrainerPartyEntries, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane3.addTab("Party", jPanel4);

        jSplitPane1.setRightComponent(jTabbedPane3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1529, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // UI Update Methods.
    // Trainer Properties
    private void UI_UpdateTrainerPokeTabs() {
        WBTrainerData CurrentTrainer = this.trainer;
        jPanel4.removeAll();
        for (int Index = 0; Index < CurrentTrainer.GetPkmnSize(); ++Index) {
            WBTrainerPoke CurrentPokemon = CurrentTrainer.GetPkmn(Index);
            jTrainerPartyEntries.add(new VTrainerPartyComponent(this.Instance, CurrentPokemon, this.PkmnNames, this.ItemNames, this.MoveNames));
        }
    }
    
    private void UI_UpdateNameEntry() {
//        nameEntry.setText(TrNames.getLine(this.Trainers.indexOf(this.trainer)));
    }
    
    private void UI_UpdateMiscSettings() { 
        // Set settings options.
        canHeal.setSelected(this.trainer.GetCanHeal());
        canOverrideMoves.setSelected(this.trainer.CanOverrideMoves());
        canOverrideItems.setSelected(this.trainer.CanOverrideHeldItem());
        battleTypeComboBox.setSelectedIndex(this.trainer.GetBattleType());
    }
    
    private void UI_UpdateItem(int Index) {
        // Items from each item slot.
        switch (Index) {
            case 0:
                Item1ComboBox.setSelectedIndex(this.trainer.GetItem(Index));
                break;
            case 1:
                Item2ComboBox.setSelectedIndex(this.trainer.GetItem(Index));
                break;
            case 2:
                Item3ComboBox.setSelectedIndex(this.trainer.GetItem(Index));
                break;
            case 3:
                Item4ComboBox.setSelectedIndex(this.trainer.GetItem(Index));
                break;
        }
    } 
    
    private void UI_UpdateAIInteger() {   
        AIConfigurationInteger.setValue(this.trainer.GetAIValue());
    }
    
    private void UI_UpdateAIFlag(int Index) {
        switch (Index) {
            case 0:
                AIBasicCheckbox.setSelected(this.trainer.GetAIFlag(0));
                break;
            case 1:
                AIWillAttackCheckbox.setSelected(this.trainer.GetAIFlag(1));
                break;
            case 2:  
                AIAdvancedCheckbox.setSelected(this.trainer.GetAIFlag(2));
                break;
            case 3: 
                AIUnusedCheckbox.setSelected(this.trainer.GetAIFlag(3));
                break;
            case 4:
                AIRivalCheckbox.setSelected(this.trainer.GetAIFlag(4));
                break;
            case 5:
                AIUnused2Checkbox.setSelected(this.trainer.GetAIFlag(5));
                break;
            case 6:
                AIUnused3Checkbox.setSelected(this.trainer.GetAIFlag(6));
                break;
            case 7:
                AIMultiBattleCheckbox.setSelected(this.trainer.GetAIFlag(7));
                break;
        }
    }  
    
    private void UI_UpdateAIFlags() {
        for (int Index = 0; Index < Byte.SIZE; ++Index) {
            UI_UpdateAIFlag(Index);
        }
    }
    private void UI_UpdateTrainerClassList() {
        trainerClassList.setSelectedIndex(this.trainer.GetAssignedClass());
    }
    private void UI_UpdateMovesEnabled() {
        WBTrainerData CurrentTrainer = this.trainer;
        if (CurrentTrainer != null) {
//            Move1ComboBox.setEnabled(CurrentTrainer.CanOverrideMoves());
//            Move2ComboBox.setEnabled(CurrentTrainer.CanOverrideMoves());
//            Move3ComboBox.setEnabled(CurrentTrainer.CanOverrideMoves());
//            Move4ComboBox.setEnabled(CurrentTrainer.CanOverrideMoves());
        }
    }    
    private void UI_UpdateHeldItemEnabled() {
        WBTrainerData CurrentTrainer = this.trainer;
        if (CurrentTrainer != null) {
//            heldItemComboBox.setEnabled(CurrentTrainer.CanOverrideHeldItem());
        }
    }
    
    private void btnOpenScrInIDE23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenScrInIDE23ActionPerformed

    }//GEN-LAST:event_btnOpenScrInIDE23ActionPerformed


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
    private javax.swing.JButton btnOpenScrInIDE23;
    private javax.swing.JCheckBox canHeal;
    private javax.swing.JCheckBox canOverrideItems;
    private javax.swing.JCheckBox canOverrideMoves;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JPanel itemsPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JPanel jTrainerPartyEntries;
    private javax.swing.JTextField nameEntry;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JLabel rewardItemLabel;
    private javax.swing.JSpinner rewardItemSpinner;
    private javax.swing.JLabel rewardMoneyLabel;
    private javax.swing.JSpinner rewardMoneySpinner;
    private javax.swing.JPanel rewardsPanel;
    private javax.swing.JLabel settingsLabel;
    private javax.swing.JList<String> trainerClassList;
    // End of variables declaration//GEN-END:variables
}
