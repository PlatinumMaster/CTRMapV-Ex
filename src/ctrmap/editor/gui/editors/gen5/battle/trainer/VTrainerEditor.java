package ctrmap.editor.gui.editors.gen5.battle.trainer;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.gui.editors.text.loaders.ITextArcType;
import ctrmap.editor.gui.editors.util.SystemTextLUT;
import ctrmap.editor.gui.editors.util.TextSingleton;
import ctrmap.editor.gui.editors.util.UserEnumHandler;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.UserData;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerData;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerPoke;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.MessageHandler;
import ctrmap.formats.pokemon.text.MsgStr;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.VRTC;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        if (Index < 0 || Index > FS().NARCGetDataMax(NARCRef.TRAINER_DATA)) {
            return null;
        }
        return Trainers.get(Index);
    }
    
    @Override
    public void onProjectLoaded(CTRMapProject proj) {
        // TODO: Move somewhere else
       for (FSFile f : Instance.getProject().userData.getUserDataDir(UserData.UsrDirectory.ENUMS).listFiles()) {
           UserEnumHandler.Load(f);
       }
    }
    
    @Override
    public String getTabName() {
        return "Trainer Editor";
    }

    @Override
    public boolean isGameSupported(GameInfo game) {
        return game.isGenV();
    }
    
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
}
