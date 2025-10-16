/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ctrmap.editor.gui.editors.gen5.battle.pokemon;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.util.SystemTextLUT;
import ctrmap.editor.gui.editors.util.TextSingleton;
import ctrmap.formats.pokemon.gen5.pml.WBPMLPersonal;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author L33TG
 */
public class VPokemonEditorComponent extends javax.swing.JPanel {
    private CTRMap Instance;
    
    private int baseHP;
    
    private TextFile PkmnNames, AbilNames, MoveNames, ItemNames, Types, DexCategories, DexDescription;
    
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
    
    public VPokemonEditorComponent(CTRMap Instance, WBPMLPersonal species, int speciesIndex, String name) {
        initComponents();
        this.Instance = Instance;
        
        LoadAllTextArchives();
        
        ArrayList<String> speciesNames = new ArrayList<String>();
        DefaultComboBoxModel cbmSpecies = new DefaultComboBoxModel();
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
        
        jSpinnerHeightMetre.setValue(species.GetHeightCm() / 100);
        jSpinnerHeightCentiMetre.setValue(species.GetHeightCm() / 10);
        
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
        jScrollPane2 = new javax.swing.JScrollPane();
        jListLearnset = new javax.swing.JList<>();
        jPanel15 = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jCBLearnsetMoves = new javax.swing.JComboBox<>();
        jLabel48 = new javax.swing.JLabel();
        jSpinnerLearnsetLevel6 = new javax.swing.JSpinner();
        jBtnLearnsetAdd = new javax.swing.JButton();
        jBtnLearnsetRemove = new javax.swing.JButton();
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
        jBtnApplyChanges = new javax.swing.JButton();

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
                    .addComponent(jScrollPane1)
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
                                .addComponent(jLabel30)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jScrollPane2.setViewportView(jListLearnset);

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
                    .addComponent(jSpinnerLearnsetLevel6)
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
                .addComponent(jSpinnerLearnsetLevel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnLearnsetAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnLearnsetRemove)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 96, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jCBWildItem5, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 96, Short.MAX_VALUE)
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
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEVYield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEVYield1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanelDex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanelAbilities1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(129, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelTypes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanelEVYield1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelAbilities1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelEVYield, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(jPanelAbilities, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelDex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );

        jPanelAbilities1.getAccessibleContext().setAccessibleName("panelPreview");
        jPanelAbilities1.getAccessibleContext().setAccessibleDescription("");

        jBtnApplyChanges.setText("Apply Changes");
        jBtnApplyChanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnApplyChangesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jBtnApplyChanges, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 1365, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 592, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnApplyChanges)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnApplyChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnApplyChangesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBtnApplyChangesActionPerformed

    private void jBtnLearnsetAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLearnsetAddActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jBtnLearnsetAddActionPerformed

    private void jBtnLearnsetRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLearnsetRemoveActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBtnLearnsetRemoveActionPerformed

    private void jBtnStaticSpriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnStaticSpriteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBtnStaticSpriteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnApplyChanges;
    private javax.swing.JButton jBtnIconSprite;
    private javax.swing.JButton jBtnLearnsetAdd;
    private javax.swing.JButton jBtnLearnsetRemove;
    private javax.swing.JButton jBtnStaticSprite;
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
    private javax.swing.JList<String> jListLearnset;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel15;
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
    private javax.swing.JSpinner jSpinnerLearnsetLevel6;
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
