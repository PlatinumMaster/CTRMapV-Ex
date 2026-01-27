package ctrmap.editor.gui.editors.gen5.battle.pokemon;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.util.SystemTextLUT;
import ctrmap.editor.gui.editors.util.TextSingleton;
import ctrmap.formats.pokemon.gen5.pml.WBPMLLearnsets;
import ctrmap.formats.pokemon.gen5.pml.WBPMLLevelUpMove;
import ctrmap.formats.pokemon.gen5.pml.WBPMLLevelUpMoveNames;
import ctrmap.formats.pokemon.gen5.pml.WBPMLPersonal;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlReflectUtil;

/**
 *
 * @author L33TG
 */
public class VPokemonEditorComponent extends javax.swing.JPanel {
    private CTRMap Instance;
    
    private TextFile PkmnNames, MoveNames, AbilNames, ItemNames, Types, DexCategories, DexDescription;
    private WBPMLLearnsets learnset;
    private ArrayList<WBPMLLevelUpMove> listLearnset;
    private DefaultListModel<String> jList1Model = new DefaultListModel<>();
    
    /**
     * Creates new form VPokemonEditorComponent
     */
    public VPokemonEditorComponent() {
        initComponents();
    }
    
    NTRGameFS FS() {
        return Instance.getMissionControl(ctrmap.missioncontrol_ntr.VLaunchpad.class).fs;
    }
    
    TextFile LoadTextFile(String File) {
        return TextSingleton.Load(FS(), NARCRef.MSGDATA_SYSTEM, SystemTextLUT.get(File));
    }
    
    private void LoadAllTextArchives() {
        this.PkmnNames = LoadTextFile("Pokemon");
        this.AbilNames = LoadTextFile("Abilities");
        this.MoveNames = LoadTextFile("Moves");
        this.ItemNames = LoadTextFile("Items");
        this.Types = LoadTextFile("Types");
        this.DexCategories = LoadTextFile("DexCategories");
        this.DexDescription = LoadTextFile("DexDescriptions");
    }
    
    public VPokemonEditorComponent(CTRMap Instance, WBPMLPersonal species, WBPMLLearnsets learnset, int speciesIndex, String name) throws IOException {
        initComponents();
        this.Instance = Instance;
        this.learnset = learnset;
        
        LoadAllTextArchives();
        
        ArrayList<String> speciesNames = new ArrayList<String>();
        
        DefaultComboBoxModel cbmSpecies = new DefaultComboBoxModel();
        
        int height = species.GetHeightCm();
        
        
        for (int Index = 0; Index < this.PkmnNames.getLineCount(); ++Index) {
            cbmSpecies.addElement(this.PkmnNames.getLine(Index));
        }
        
        jTFSpeciesName.setText(name);
        
        for (int Index = 1; Index < this.MoveNames.getLineCount(); ++Index) {
            this.jCBLearnsetMoves.addItem(this.MoveNames.getLine(Index));
        }
        
        for (int Index = 0; Index < this.Types.getLineCount(); ++Index) {
            this.jCBType1.addItem(this.Types.getLine(Index));
            this.jCBType2.addItem(this.Types.getLine(Index));
            
            if (species.GetPrimaryType() == Index) {
                this.jCBType1.setSelectedIndex(Index);             
            }
            if (species.GetSecondaryType() == Index) {
                this.jCBType2.setSelectedIndex(Index);
            }
        }
        
        for (int Index = 0; Index < this.AbilNames.getLineCount(); ++Index) {
            this.jCBAbilityPrimary.addItem(this.AbilNames.getLine(Index));
            this.jCBAbilitySecondary.addItem(this.AbilNames.getLine(Index));
            this.jCBAbilityHidden.addItem(this.AbilNames.getLine(Index));
            
            if (species.GetPrimaryAbility() == Index) {
                this.jCBAbilityPrimary.setSelectedIndex(Index);
            }
            
            if (species.GetSecondaryAbility() == Index) {
                this.jCBAbilitySecondary.setSelectedIndex(Index);
            }
            
            if (species.GetHiddenAbility() == Index) {
                this.jCBAbilityHidden.setSelectedIndex(Index);
            }
        }
        
        for (int Index = 0; Index < this.ItemNames.getLineCount(); ++Index) {
            this.jCBWildItem50.addItem(this.ItemNames.getLine(Index));
            this.jCBWildItem5.addItem(this.ItemNames.getLine(Index));
            this.jCBWildItem1.addItem(this.ItemNames.getLine(Index));
            
            if (species.GetWildItem50() == Index) {
                this.jCBWildItem50.setSelectedIndex(Index);
            }
            
            if (species.GetWildItem5() == Index) {
                this.jCBWildItem5.setSelectedIndex(Index);
            }
            
            if (species.GetWildItem1() == Index) {
                this.jCBWildItem1.setSelectedIndex(Index);
            }
        }
        
        
        if (height >= 100) {
            jSpinnerHeightMetre.setValue(height / 100);
            height = height % 100;
        }
        
        jSpinnerHeightCentiMetre.setValue(height / 10);
        
        jSpinnerWeightKilogram.setValue(species.GetWeightCg() / 10);
        jSpinnerWeightGram.setValue(species.GetWeightCg() % 10);
        
        jSpinnerHP.setValue(species.GetBaseHP());
        jSpinnerAtk.setValue(species.GetBaseAttack());
        jSpinnerDef.setValue(species.GetBaseDefense());
        jSpinnerSpAtk.setValue(species.GetBaseSpAttack());
        jSpinnerSpDef.setValue(species.GetBaseSpDefense());
        jSpinnerSpd.setValue(species.GetBaseSpeed());
        
        EVYield yield = decodeEV(species.GetEvYield());
        
        jSpinnerHPYield.setValue(yield.hp);
        jSpinnerAtkYield.setValue(yield.atk);
        jSpinnerDefYield.setValue(yield.def);
        jSpinnerSpAtkYield.setValue(yield.spa);
        jSpinnerSpDefYield.setValue(yield.spd);
        jSpinnerSpdYield.setValue(yield.spe);
        
        jTFDexCategory.setText(DexCategories.getLine(speciesIndex));
        jTXDexDesc.setText(DexDescription.getLine(speciesIndex));
        
        if (learnset != null) {
            listLearnset = new ArrayList<WBPMLLevelUpMove>();
            
            for (int Index = 0; Index < learnset.GetLearnsetSize(); ++Index) {
                
                WBPMLLevelUpMove move = new WBPMLLevelUpMove(
                        learnset.GetLevelUpMove(Index).GetMoveName(),
                        learnset.GetLevelUpMove(Index).GetLearnLevel()
                );
                
                listLearnset.add(move);
                
                System.out.println(listLearnset.get(Index).GetMoveName());
            }

            jList1.setModel(jList1Model);
            
            for (int Index = 0; Index < listLearnset.size(); ++Index) {
                System.out.println(listLearnset.get(Index).GetMoveName() + ", Lv" + listLearnset.get(Index).GetLearnLevel());
                if (listLearnset.get(Index).GetLearnLevel() != 65535) {
                    String moveName = MoveFriendlyName(listLearnset.get(Index).GetMoveName());
                    jList1Model.addElement(moveName + ", Lv" + listLearnset.get(Index).GetLearnLevel());
                }
            }
        } else {
            System.out.println("The learnset is empty!");
        }
        
        //Hide new editor panel
        jPanel2.setVisible(false);
    }
    
    /*
    * Class for getting values from EVYield
    */
    public static class EVYield {
        int hp;
        int atk;
        int def;
        int spe;
        int spa;
        int spd;
        boolean grounded;
    }
    
    /*
    * Method for decoding values from EVYield
    */
    public static EVYield decodeEV(int decEVYield) {
        EVYield evYield = new EVYield();
        
        evYield.hp = (decEVYield >> 0) & 0b11;
        evYield.atk = (decEVYield >> 2) & 0b11;
        evYield.def = (decEVYield >> 4) & 0b11;
        evYield.spe = (decEVYield >> 6) & 0b11;
        evYield.spa = (decEVYield >> 8) & 0b11;
        evYield.spd = (decEVYield >> 10) & 0b11;
        evYield.grounded = ((decEVYield >> 15) & 0b1) == 1;
        
        return evYield;
    }
    
    public String MoveFriendlyName(String moveName) {
        String moveFriendlyName = "error";

        for (int Index = 1; Index < this.MoveNames.getLineCount(); Index++) {
            for (WBPMLLevelUpMoveNames moveNameEnum : WBPMLLevelUpMoveNames.values()) {
                if (moveName.equalsIgnoreCase(moveNameEnum.name())) {
                    moveFriendlyName = moveNameEnum.getMoveNameFriendly();
                }
            }
        }
        
        return moveFriendlyName;
    }
    
    public String MoveYAMLName (String moveFriendlyName) {
        String moveYAMLName = "MOVE_ERROR";
        
        for (int Index = 1; Index < this.MoveNames.getLineCount(); Index++) {
            for (WBPMLLevelUpMoveNames moveNameEnum : WBPMLLevelUpMoveNames.values()) {
                if (moveFriendlyName.equalsIgnoreCase(moveNameEnum.getMoveNameFriendly())) {
                    moveYAMLName = moveNameEnum.name();
                }
            }
        }
        
        return moveYAMLName;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanelEVYield = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jSpinnerHPYield = new javax.swing.JSpinner();
        jSpinnerAtkYield = new javax.swing.JSpinner();
        jSpinnerDefYield = new javax.swing.JSpinner();
        jLabel27 = new javax.swing.JLabel();
        jSpinnerSpAtkYield = new javax.swing.JSpinner();
        jSpinnerSpDefYield = new javax.swing.JSpinner();
        jLabel22 = new javax.swing.JLabel();
        jSpinnerSpdYield = new javax.swing.JSpinner();
        jLabel23 = new javax.swing.JLabel();
        jPanelTypes = new javax.swing.JPanel();
        jCBType1 = new javax.swing.JComboBox<>();
        jCBType2 = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanelAbilities = new javax.swing.JPanel();
        jCBAbilityPrimary = new javax.swing.JComboBox<>();
        jCBAbilitySecondary = new javax.swing.JComboBox<>();
        jCBAbilityHidden = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanelDex = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTFDexCategory = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTXDexDesc = new javax.swing.JTextArea();
        jLabel28 = new javax.swing.JLabel();
        jSpinnerHeightMetre = new javax.swing.JSpinner();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jSpinnerHeightCentiMetre = new javax.swing.JSpinner();
        jLabel32 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jSpinnerWeightKilogram = new javax.swing.JSpinner();
        jSpinnerWeightGram = new javax.swing.JSpinner();
        jPanelAbilities1 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabelPreview = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabelPreview1 = new javax.swing.JLabel();
        jBtnStaticSprite = new javax.swing.JButton();
        jBtnIconSprite = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jTFSpeciesName = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jCBLearnsetMoves = new javax.swing.JComboBox<>();
        jLabel48 = new javax.swing.JLabel();
        jSpinnerLearnsetLevel = new javax.swing.JSpinner();
        jBtnLearnsetAdd = new javax.swing.JButton();
        jBtnLearnsetRemove = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jPanelEVYield1 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jSpinnerSpd = new javax.swing.JSpinner();
        jSpinnerSpDef = new javax.swing.JSpinner();
        jSpinnerSpAtk = new javax.swing.JSpinner();
        jSpinnerDef = new javax.swing.JSpinner();
        jSpinnerAtk = new javax.swing.JSpinner();
        jSpinnerHP = new javax.swing.JSpinner();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jCBWildItem50 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jCBWildItem5 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jCBWildItem1 = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jBtnApplyChanges1 = new javax.swing.JButton();

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Stats"));
        jPanel3.setPreferredSize(new java.awt.Dimension(1050, 448));

        jPanelEVYield.setBorder(javax.swing.BorderFactory.createTitledBorder("EV Yield"));
        jPanelEVYield.setPreferredSize(new java.awt.Dimension(206, 220));

        jLabel24.setText("HP");

        jLabel25.setText("Attack");

        jLabel26.setText("Defense");

        jLabel27.setText("Special Attack");

        jLabel22.setText("Special Defense");

        jLabel23.setText("Speed");

        javax.swing.GroupLayout jPanelEVYieldLayout = new javax.swing.GroupLayout(jPanelEVYield);
        jPanelEVYield.setLayout(jPanelEVYieldLayout);
        jPanelEVYieldLayout.setHorizontalGroup(
            jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelEVYieldLayout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 22, Short.MAX_VALUE)
                .addGroup(jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSpinnerSpdYield, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                    .addComponent(jSpinnerSpDefYield)
                    .addComponent(jSpinnerSpAtkYield)
                    .addComponent(jSpinnerDefYield)
                    .addComponent(jSpinnerAtkYield)
                    .addComponent(jSpinnerHPYield))
                .addContainerGap())
        );
        jPanelEVYieldLayout.setVerticalGroup(
            jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEVYieldLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerHPYield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerAtkYield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerDefYield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerSpAtkYield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerSpDefYield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYieldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerSpdYield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanelTypes.setBorder(javax.swing.BorderFactory.createTitledBorder("Types"));

        jLabel8.setText("Primary");

        jLabel9.setText("Secondary");

        javax.swing.GroupLayout jPanelTypesLayout = new javax.swing.GroupLayout(jPanelTypes);
        jPanelTypes.setLayout(jPanelTypesLayout);
        jPanelTypesLayout.setHorizontalGroup(
            jPanelTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTypesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCBType1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCBType2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelTypesLayout.createSequentialGroup()
                        .addGroup(jPanelTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelTypesLayout.setVerticalGroup(
            jPanelTypesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTypesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBType1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBType2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelAbilities.setBorder(javax.swing.BorderFactory.createTitledBorder("Abilities"));

        jLabel15.setText("Primary");

        jLabel16.setText("Secondary");

        jLabel17.setText("Hidden");

        javax.swing.GroupLayout jPanelAbilitiesLayout = new javax.swing.GroupLayout(jPanelAbilities);
        jPanelAbilities.setLayout(jPanelAbilitiesLayout);
        jPanelAbilitiesLayout.setHorizontalGroup(
            jPanelAbilitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAbilitiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAbilitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCBAbilityPrimary, 0, 184, Short.MAX_VALUE)
                    .addComponent(jCBAbilitySecondary, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCBAbilityHidden, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelAbilitiesLayout.createSequentialGroup()
                        .addGroup(jPanelAbilitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelAbilitiesLayout.setVerticalGroup(
            jPanelAbilitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAbilitiesLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBAbilityPrimary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBAbilitySecondary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBAbilityHidden, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelDex.setBorder(javax.swing.BorderFactory.createTitledBorder("Pokédex Data"));

        jLabel10.setText("Category");

        jLabel11.setText("Description");

        jTXDexDesc.setColumns(20);
        jTXDexDesc.setRows(5);
        jScrollPane1.setViewportView(jTXDexDesc);

        jLabel28.setText("Height");

        jSpinnerHeightMetre.setMinimumSize(new java.awt.Dimension(70, 26));
        jSpinnerHeightMetre.setPreferredSize(new java.awt.Dimension(70, 26));

        jLabel29.setText("Weight");

        jLabel30.setText("m");

        jLabel31.setText("kg");

        jSpinnerHeightCentiMetre.setMinimumSize(new java.awt.Dimension(70, 26));
        jSpinnerHeightCentiMetre.setPreferredSize(new java.awt.Dimension(70, 26));

        jLabel32.setText(",");

        jLabel39.setText(",");

        jSpinnerWeightKilogram.setPreferredSize(new java.awt.Dimension(70, 26));

        jSpinnerWeightGram.setPreferredSize(new java.awt.Dimension(70, 26));

        javax.swing.GroupLayout jPanelDexLayout = new javax.swing.GroupLayout(jPanelDex);
        jPanelDex.setLayout(jPanelDexLayout);
        jPanelDexLayout.setHorizontalGroup(
            jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDexLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel29)
                    .addComponent(jLabel28))
                .addGap(18, 18, 18)
                .addGroup(jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addComponent(jTFDexCategory)
                    .addGroup(jPanelDexLayout.createSequentialGroup()
                        .addGroup(jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinnerWeightKilogram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerHeightMetre, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelDexLayout.createSequentialGroup()
                                .addComponent(jLabel39)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinnerWeightGram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel31))
                            .addGroup(jPanelDexLayout.createSequentialGroup()
                                .addComponent(jLabel32)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSpinnerHeightCentiMetre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel30)))))
                .addContainerGap())
        );
        jPanelDexLayout.setVerticalGroup(
            jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDexLayout.createSequentialGroup()
                .addGroup(jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTFDexCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDexLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerHeightMetre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(jLabel30)
                    .addComponent(jLabel32)
                    .addComponent(jSpinnerHeightCentiMetre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelDexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(jLabel31)
                    .addComponent(jLabel39)
                    .addComponent(jSpinnerWeightKilogram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerWeightGram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanelAbilities1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelAbilities1.setName(""); // NOI18N

        jLabel13.setText("Static Sprite Preview");

        jLabelPreview.setBackground(new java.awt.Color(102, 102, 102));
        jLabelPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelPreview.setText("Preview Here");
        jLabelPreview.setOpaque(true);

        jLabel14.setText("Icon Preview");

        jLabelPreview1.setBackground(new java.awt.Color(102, 102, 102));
        jLabelPreview1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelPreview1.setText("Preview Here");
        jLabelPreview1.setOpaque(true);

        jBtnStaticSprite.setText("Change Static Sprite");
        jBtnStaticSprite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnStaticSpriteActionPerformed(evt);
            }
        });

        jBtnIconSprite.setText("Change Icon Sprite");

        javax.swing.GroupLayout jPanelAbilities1Layout = new javax.swing.GroupLayout(jPanelAbilities1);
        jPanelAbilities1.setLayout(jPanelAbilities1Layout);
        jPanelAbilities1Layout.setHorizontalGroup(
            jPanelAbilities1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAbilities1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAbilities1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel13)
                    .addComponent(jLabelPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnStaticSprite, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanelAbilities1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel14)
                    .addComponent(jLabelPreview1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnIconSprite, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelAbilities1Layout.setVerticalGroup(
            jPanelAbilities1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAbilities1Layout.createSequentialGroup()
                .addGroup(jPanelAbilities1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAbilities1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelPreview, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                    .addComponent(jLabelPreview1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAbilities1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnStaticSprite)
                    .addComponent(jBtnIconSprite))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Species Name"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTFSpeciesName)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTFSpeciesName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Learnset"));

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Entry"));

        jLabel47.setText("Move");

        jLabel48.setText("Level");

        jBtnLearnsetAdd.setText("Add");
        jBtnLearnsetAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnLearnsetAddActionPerformed(evt);
            }
        });

        jBtnLearnsetRemove.setText("Remove");
        jBtnLearnsetRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnLearnsetRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnLearnsetAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnLearnsetRemove, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                    .addComponent(jCBLearnsetMoves, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSpinnerLearnsetLevel)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel47)
                            .addComponent(jLabel48))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(jLabel47)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCBLearnsetMoves, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel48)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinnerLearnsetLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnLearnsetAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnLearnsetRemove)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jList1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelEVYield1.setBorder(javax.swing.BorderFactory.createTitledBorder("Base Stats"));
        jPanelEVYield1.setPreferredSize(new java.awt.Dimension(206, 220));

        jLabel33.setText("HP");

        jLabel34.setText("Attack");

        jLabel35.setText("Defense");

        jLabel36.setText("Special Attack");

        jLabel37.setText("Special Defense");

        jLabel38.setText("Speed");

        javax.swing.GroupLayout jPanelEVYield1Layout = new javax.swing.GroupLayout(jPanelEVYield1);
        jPanelEVYield1.setLayout(jPanelEVYield1Layout);
        jPanelEVYield1Layout.setHorizontalGroup(
            jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelEVYield1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSpinnerSpd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addComponent(jSpinnerSpDef, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSpinnerSpAtk, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSpinnerDef, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSpinnerAtk, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSpinnerHP))
                .addContainerGap())
        );
        jPanelEVYield1Layout.setVerticalGroup(
            jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelEVYield1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(jSpinnerHP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(jSpinnerAtk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(jSpinnerDef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(jSpinnerSpAtk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(jSpinnerSpDef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelEVYield1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(jSpinnerSpd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Item"));

        jLabel1.setText("Wild Item (50% Chance)");

        jLabel2.setText("Wild Item (5% Chance)");

        jLabel3.setText("Wild Item (1% Chance)");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jCBWildItem50, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jCBWildItem5, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCBWildItem1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCBWildItem50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCBWildItem5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCBWildItem1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Misc."));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ctrmap/resources/personaleditor/icon_evolution.png"))); // NOI18N
        jButton1.setText("Evolutions");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ctrmap/resources/personaleditor/icon_tm.png"))); // NOI18N
        jButton2.setText("TM/HM");
        jButton2.setIconTextGap(8);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ctrmap/resources/personaleditor/icon_learnset.png"))); // NOI18N
        jButton3.setText("Learnset");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ctrmap/resources/personaleditor/icon_grass.png"))); // NOI18N
        jButton4.setText("Wild Held Items");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanelAbilities, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEVYield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEVYield1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelDex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelAbilities1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanelEVYield1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(jPanelDex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanelAbilities1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanelEVYield, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                            .addComponent(jPanelAbilities, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanelAbilities1.getAccessibleContext().setAccessibleName("panelPreview");
        jPanelAbilities1.getAccessibleContext().setAccessibleDescription("");
        jPanel2.getAccessibleContext().setAccessibleName("Misc");

        jBtnApplyChanges1.setText("Apply Changes");
        jBtnApplyChanges1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyChanges1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 1288, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBtnApplyChanges1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 592, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnApplyChanges1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnLearnsetAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLearnsetAddActionPerformed
        String selectedMove = (String) jCBLearnsetMoves.getModel().getSelectedItem();
        JOptionPane messageDialog = new JOptionPane();
        List<String> jListOld = new ArrayList<String>();
        List<String> jListSorted = new ArrayList<String>();
        int moveLevel = 0;
        
        if ((int) jSpinnerLearnsetLevel.getModel().getValue() <= 0) {
            JOptionPane.showMessageDialog(jPanelTypes, (String) "The level has to be a number between 1-100!");
            jSpinnerLearnsetLevel.getModel().setValue(1);
        } else if ((int) jSpinnerLearnsetLevel.getModel().getValue() > 100) {
            JOptionPane.showMessageDialog(jPanelTypes, (String) "The level has to be a number between 1-100!");
            jSpinnerLearnsetLevel.getModel().setValue(100);
        }
        
        int selectedMoveLevel = (int) jSpinnerLearnsetLevel.getModel().getValue();
        
        jList1Model.addElement(selectedMove + ", Lv" + selectedMoveLevel);
        
        for (int Index = 0; Index < jList1Model.size(); Index++) {
            jListOld.add(jList1Model.elementAt(Index));
        }
        
        for (String s : jListOld) {
            if (s.contains("Conversion 2")) {
                s.replace("Conversion 2", "");
            } else {
               moveLevel = Integer.parseInt(s.replaceAll("[\\D]", "")); 
            }
            
            System.out.println(moveLevel);
        }
    }//GEN-LAST:event_jBtnLearnsetAddActionPerformed

    private void jBtnLearnsetRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLearnsetRemoveActionPerformed
        String selectedMoveFriendly = jList1.getSelectedValue();
        String selectedMoveYAML = MoveYAMLName(selectedMoveFriendly);
        
        System.out.println(selectedMoveYAML);
    }//GEN-LAST:event_jBtnLearnsetRemoveActionPerformed

    private void jBtnStaticSpriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnStaticSpriteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBtnStaticSpriteActionPerformed

    private void jBtnApplyChanges1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyChanges1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBtnApplyChanges1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
//        try {
//        new VLearnsetEditor(learnset);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnApplyChanges1;
    private javax.swing.JButton jBtnIconSprite;
    private javax.swing.JButton jBtnLearnsetAdd;
    private javax.swing.JButton jBtnLearnsetRemove;
    private javax.swing.JButton jBtnStaticSprite;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jCBAbilityHidden;
    private javax.swing.JComboBox<String> jCBAbilityPrimary;
    private javax.swing.JComboBox<String> jCBAbilitySecondary;
    private javax.swing.JComboBox<String> jCBLearnsetMoves;
    private javax.swing.JComboBox<String> jCBType1;
    private javax.swing.JComboBox<String> jCBType2;
    private javax.swing.JComboBox<String> jCBWildItem1;
    private javax.swing.JComboBox<String> jCBWildItem5;
    private javax.swing.JComboBox<String> jCBWildItem50;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelPreview;
    private javax.swing.JLabel jLabelPreview1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelAbilities;
    private javax.swing.JPanel jPanelAbilities1;
    private javax.swing.JPanel jPanelDex;
    private javax.swing.JPanel jPanelEVYield;
    private javax.swing.JPanel jPanelEVYield1;
    private javax.swing.JPanel jPanelTypes;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinnerAtk;
    private javax.swing.JSpinner jSpinnerAtkYield;
    private javax.swing.JSpinner jSpinnerDef;
    private javax.swing.JSpinner jSpinnerDefYield;
    private javax.swing.JSpinner jSpinnerHP;
    private javax.swing.JSpinner jSpinnerHPYield;
    private javax.swing.JSpinner jSpinnerHeightCentiMetre;
    private javax.swing.JSpinner jSpinnerHeightMetre;
    private javax.swing.JSpinner jSpinnerLearnsetLevel;
    private javax.swing.JSpinner jSpinnerSpAtk;
    private javax.swing.JSpinner jSpinnerSpAtkYield;
    private javax.swing.JSpinner jSpinnerSpDef;
    private javax.swing.JSpinner jSpinnerSpDefYield;
    private javax.swing.JSpinner jSpinnerSpd;
    private javax.swing.JSpinner jSpinnerSpdYield;
    private javax.swing.JSpinner jSpinnerWeightGram;
    private javax.swing.JSpinner jSpinnerWeightKilogram;
    private javax.swing.JTextField jTFDexCategory;
    private javax.swing.JTextField jTFSpeciesName;
    private javax.swing.JTextArea jTXDexDesc;
    // End of variables declaration//GEN-END:variables
}
