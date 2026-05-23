package ctrmap.util.tools.medals;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.SpinnerNumberModel;

import java.awt.Image;
import java.awt.image.BufferedImage;

import ctrmap.creativestudio.ngcs2d.canvas.SpriteRenderer;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DPalette;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DResource;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DTileSheet;
import ctrmap.creativestudio.nitro2dplugin.Gen5NGCS2DPlugin;
import ctrmap.editor.CTRMap;
import ctrmap.util.tools.medals.MedalEditor.MedalFile;
import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import ctrmap.util.gui.CMGUI;
import xstandard.fs.FSFile;
import xstandard.util.ReflectionHash;

/**
 *
 * @author mi460
 */
public class VMedals extends java.awt.Dialog {
    CTRMap parent;
    NTRGameFS fs;

    private Sprite2DResource medalResource = null;
    private ArrayList<MedalFile> medalFiles = new ArrayList<MedalFile>();
    private TextFile medalNames, medalDescriptions, hintDescriptions;
    private static final Map<String, Integer> SystemTextLUT = Stream.of(
            new AbstractMap.SimpleEntry<>("MedalNames", 83),
            new AbstractMap.SimpleEntry<>("MedalDescriptions", 80),
            new AbstractMap.SimpleEntry<>("HintDescriptions", 81)
        )
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
    );
    private int prevMedal = -1;

    private static BufferedImage renderCellFromBank(Sprite2DResource res, int bankIndex) {
        Sprite2DCell cell = res.cells.get(bankIndex);

        Sprite2DTileSheet ts = res.tileSheets.get(0);
        Sprite2DPalette pal = res.palettes.get(0);

        return SpriteRenderer.renderCell(cell, ts, pal, res.mappingMode);
    }

    private TextFile LoadSystemTextArchive(int Index) {
        return new TextFile(fs.NARCGet(NARCRef.MSGDATA_SYSTEM, Index), GenVMessageHandler.INSTANCE);
    }

    private String getText(TextFile file, int line) {
        return file.getLine(line).replace("\n", "\\n");
    }

    private MedalFile buildMedalFile() {
        // Fetch medal data.
        int medalProgress = (Integer) progressSpinner.getValue();
        int recordID = (Integer) IDSpinner.getValue();
        int recordType = recordCombo.getSelectedIndex();
        int type = (Integer) typeSpinner.getValue();
        int hintSel = hintSelCombo.getSelectedIndex();
        int hintStage = (Integer) hintStageSpinner.getValue();

        return new MedalFile(medalProgress, recordID, recordType, type, hintSel, hintStage);
    }

    private void loadResource() {
        try {
            // Gather relevant medal icon files
            FSFile nclrFile = fs.NARCGet(NARCRef.MEDAL_ICON, 0);
            FSFile ncgrFile = fs.NARCGet(NARCRef.MEDAL_ICON, 1);
            FSFile ncerFile = fs.NARCGet(NARCRef.MEDAL_ICON, 2);

            // Build a Sprite2DResource
            medalResource = new Sprite2DResource();
            medalResource.merge(Gen5NGCS2DPlugin.CSNNS_CGR.importFile(ncgrFile, null));
            medalResource.merge(Gen5NGCS2DPlugin.CSNNS_CLR.importFile(nclrFile, null));
            medalResource.merge(Gen5NGCS2DPlugin.CSNNS_CER.importFile(ncerFile, null));
            medalResource.linkTileSheetsToCells();

            // Reset icon
            updateMedalIcon(0);

            // Set the hint medal icon.
            BufferedImage raw = renderCellFromBank(medalResource, 27);
            hintImgLabel.setIcon(new ImageIcon(raw.getScaledInstance(64, 64, Image.SCALE_REPLICATE)));

            // Collect relevant text files
            this.medalNames = LoadSystemTextArchive(SystemTextLUT.get("MedalNames"));
            this.medalDescriptions = LoadSystemTextArchive(SystemTextLUT.get("MedalDescriptions"));
            this.hintDescriptions = LoadSystemTextArchive(SystemTextLUT.get("HintDescriptions"));
        } catch (Exception e) {
            System.err.println("Failed to load resources; " + e);
        }
    }

    private void loadFiles() {
        for (int i = 0; i < fs.NARCGetDataMax(NARCRef.MEDAL_INFO); i++) {
            addMedal(MedalEditor.readMedal(fs.NARCGet(NARCRef.MEDAL_INFO, i)), false);
        }
    }

    private void updateMedalIcon(int type) {
        // Render the sprite cell from the bank.
        BufferedImage raw = renderCellFromBank(this.medalResource, type);

        // Set label with medal image.
        typeImgLabel.setIcon(new ImageIcon(raw.getScaledInstance(64, 64, Image.SCALE_REPLICATE)));
    }

    private boolean matchEditorToFile(int medalID) {
        MedalFile prev = medalFiles.get(medalID);
        MedalFile cur = buildMedalFile();

        String pName = getText(medalNames, medalID);
        String pDesc = getText(medalDescriptions, medalID);
        String pHint = getText(hintDescriptions, medalID);

        String cName = nameField.getText();
        String cDesc = descriptionField.getText();
        String cHint = hintDescriptionField.getText();

        return prev.equals(cur) && pName.equals(cName) && pDesc.equals(cDesc) && pHint.equals(cHint);
    }

    private void writeMedal(int medalID) {
        MedalFile file = buildMedalFile();

        // Write to file.
        MedalEditor.writeMedal(fs.NARCGet(NARCRef.MEDAL_INFO, medalID), file);

        // Update ArrayList
        medalFiles.set(medalID, file);

        // Fetch new text data.
        String name = nameField.getText().replace("\\n", "\n");
        String description = descriptionField.getText().replace("\\n", "\n");
        String hintDescription = hintDescriptionField.getText().replace("\\n", "\n");

        // Make changes.
        medalNames.setLine(medalID, name);
        medalDescriptions.setLine(medalID, description);
        hintDescriptions.setLine(medalID, hintDescription);

        // Write changes.
        medalNames.store();
        medalDescriptions.store();
        hintDescriptions.store();
    }

    private void loadMedalEditor(int medalID) {
        prevMedal = medalID;
        MedalFile file = medalFiles.get(medalID);

        nameField.setText(getText(medalNames, medalID));
        nameField.setCaretPosition(0);
        descriptionField.setText(getText(medalDescriptions, medalID));
        descriptionField.setCaretPosition(0);
        hintDescriptionField.setText(getText(hintDescriptions, medalID));
        hintDescriptionField.setCaretPosition(0);

        progressSpinner.setValue(file.medalProgress);
        IDSpinner.setValue(file.recordID);
        recordCombo.setSelectedIndex(file.recordType);
        typeSpinner.setValue(file.type);
        hintSelCombo.setSelectedIndex(file.hintSel);
        hintStageSpinner.setValue(file.hintStage);

        updateMedalIcon(file.type);

        nameField.setEnabled(true);
        descriptionField.setEnabled(true);

        saveButton.setEnabled(true);

        typeImgLabel.setEnabled(true);

        progressSpinner.setEnabled(true);
        IDSpinner.setEnabled(true);
        recordCombo.setEnabled(true);
        typeSpinner.setEnabled(true);
        hintSelCombo.setEnabled(true);
    }

    private void addMedal(MedalFile medalFile, boolean manualAdd) {
        medalFiles.add(medalFile);

        DefaultListModel<String> model = (DefaultListModel<String>) medalList.getModel();
        int medalID = 0;
        if (model.getSize() > 0) {
            medalID = Integer.parseInt(model.getElementAt(model.getSize() - 1)) + 1;
        }
        model.addElement(String.valueOf(medalID));

        if (manualAdd) {
            // Placeholder text
            medalNames.insertLine(medalID, "Medal " + medalID);
            medalDescriptions.appendLine("A Medal given to recognize\nthe hard work of\nhackers like you!");
            hintDescriptions.appendLine("None");

            // Write changes
            medalNames.store();
            medalDescriptions.store();
            hintDescriptions.store();
            MedalEditor.writeMedal(fs.NARCGet(NARCRef.MEDAL_INFO, medalID), medalFile);

            // Reload the editor
            medalList.setSelectedIndex(medalID);
            loadMedalEditor(medalID);
        }
    }

    private void removeMedal() {
        DefaultListModel<String> model = (DefaultListModel<String>) medalList.getModel();
        //int medalID = medalList.getSelectedIndex(); // Causes some issues when medal is not at end of JList
        int medalID = model.getSize() - 1;

        if (medalID != -1) {
            model.removeElementAt(medalID);
            medalFiles.remove(medalID);
            medalNames.removeLine(medalID);
            medalDescriptions.removeLine(medalID);
            hintDescriptions.removeLine(medalID);

            // Write changes
            medalNames.store();
            medalDescriptions.store();
            hintDescriptions.store();
            MedalEditor.deleteMedal(fs.NARCGet(NARCRef.MEDAL_INFO, medalID));

            int newIndex = model.getSize() - 1;
            prevMedal = newIndex;
            medalList.setSelectedIndex(newIndex);
            loadMedalEditor(newIndex);
        }
    }

    /**
     * Creates new form VMedals
     */
    public VMedals(CTRMap parent, boolean modal) {
        super(parent, modal);
        this.parent = parent;
        this.fs = parent.getMissionControl(VLaunchpad.class).fs;

        this.setTitle("Mi460's Medal Editor");
        initComponents();

        loadResource();
        loadFiles();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        windowPanel = new javax.swing.JPanel();
        medalScrollPane = new javax.swing.JScrollPane();
        medalList = new javax.swing.JList<>(new DefaultListModel<String>());
        medalPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        hintHeaderLabel = new javax.swing.JLabel();
        hintSelCombo = new javax.swing.JComboBox<>();
        hintSelLabel = new javax.swing.JLabel();
        hintStageLabel = new javax.swing.JLabel();
        hintStageSpinner = new javax.swing.JSpinner();
        hintDescriptionLabel = new javax.swing.JLabel();
        hintDescriptionField = new javax.swing.JTextField();
        descLabel = new javax.swing.JLabel();
        descriptionField = new javax.swing.JTextField();
        typeImgLabel = new javax.swing.JLabel();
        typeSpinner = new javax.swing.JSpinner();
        typeLabel = new javax.swing.JLabel();
        hintImgLabel = new javax.swing.JLabel();
        recordCombo = new javax.swing.JComboBox<>();
        recordLabel = new javax.swing.JLabel();
        recordIDLabel = new javax.swing.JLabel();
        IDSpinner = new javax.swing.JSpinner();
        progressLabel = new javax.swing.JLabel();
        progressSpinner = new javax.swing.JSpinner();
        saveButton = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        medalList.setName("medal_list"); // NOI18N
        medalList.addListSelectionListener(this::medalListValueChanged);
        medalScrollPane.setViewportView(medalList);

        medalPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        nameLabel.setText("Name:");

        nameField.setEnabled(false);
        nameField.setName("medal_name"); // NOI18N

        hintHeaderLabel.setText("Hint medal:");
        hintHeaderLabel.setToolTipText("");

        hintSelCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Initial", "Stage", "Function" }));
        hintSelCombo.setEnabled(false);
        hintSelCombo.addActionListener(this::hintSelComboActionPerformed);

        hintSelLabel.setText("Type:");

        hintStageLabel.setText("Stage:");

        hintStageSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 5, 1));
        hintStageSpinner.setEnabled(false);

        hintDescriptionLabel.setText("Description:");

        hintDescriptionField.setEnabled(false);

        descLabel.setText("Description:");

        descriptionField.setEnabled(false);

        typeImgLabel.setEnabled(false);
        typeImgLabel.setName("icon_display"); // NOI18N

        typeSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 26, 1));
        typeSpinner.setEnabled(false);
        typeSpinner.addChangeListener(this::typeSpinnerStateChanged);

        typeLabel.setText("Type:");

        hintImgLabel.setEnabled(false);

        recordCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Save Record", "Medal Record" }));
        recordCombo.setEnabled(false);
        recordCombo.addActionListener(this::recordComboActionPerformed);

        recordLabel.setText("Record Type:");

        recordIDLabel.setText("Record ID:");

        IDSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 2147483647, 1));
        IDSpinner.setEnabled(false);

        progressLabel.setText("Progress:");

        progressSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 2147483647, 1));
        progressSpinner.setEnabled(false);

        javax.swing.GroupLayout medalPanelLayout = new javax.swing.GroupLayout(medalPanel);
        medalPanel.setLayout(medalPanelLayout);
        medalPanelLayout.setHorizontalGroup(
            medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(medalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(medalPanelLayout.createSequentialGroup()
                        .addComponent(hintSelLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hintSelCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(medalPanelLayout.createSequentialGroup()
                        .addComponent(hintStageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hintStageSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(medalPanelLayout.createSequentialGroup()
                        .addComponent(hintDescriptionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hintDescriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(hintHeaderLabel)
                    .addGroup(medalPanelLayout.createSequentialGroup()
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(descLabel)
                            .addComponent(recordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(recordIDLabel)
                            .addComponent(nameLabel)
                            .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(descriptionField)
                            .addGroup(medalPanelLayout.createSequentialGroup()
                                .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(recordCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(progressSpinner)
                                    .addComponent(IDSpinner))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(nameField))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, medalPanelLayout.createSequentialGroup()
                        .addComponent(hintImgLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, medalPanelLayout.createSequentialGroup()
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, medalPanelLayout.createSequentialGroup()
                                .addComponent(typeImgLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(22, 22, 22))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, medalPanelLayout.createSequentialGroup()
                                .addComponent(typeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(typeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(30, 30, 30))))
        );
        medalPanelLayout.setVerticalGroup(
            medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(medalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(medalPanelLayout.createSequentialGroup()
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameLabel)
                            .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(descLabel)
                            .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(recordCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(recordLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(recordIDLabel)
                            .addComponent(IDSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(medalPanelLayout.createSequentialGroup()
                        .addComponent(typeImgLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(typeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(typeLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(progressLabel)
                    .addComponent(progressSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, medalPanelLayout.createSequentialGroup()
                        .addComponent(hintHeaderLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hintSelLabel)
                            .addComponent(hintSelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(hintImgLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hintStageLabel)
                    .addComponent(hintStageSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(medalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hintDescriptionLabel)
                    .addComponent(hintDescriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        saveButton.setText("Save changes");
        saveButton.setEnabled(false);
        saveButton.addActionListener(this::saveButtonActionPerformed);

        javax.swing.GroupLayout windowPanelLayout = new javax.swing.GroupLayout(windowPanel);
        windowPanel.setLayout(windowPanelLayout);
        windowPanelLayout.setHorizontalGroup(
            windowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(windowPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(medalScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(windowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(medalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(windowPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveButton)))
                .addContainerGap())
        );
        windowPanelLayout.setVerticalGroup(
            windowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(windowPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(windowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(medalScrollPane)
                    .addComponent(medalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveButton)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        add(windowPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        ReflectionHash hash = new ReflectionHash(new Object());
        if (prevMedal != -1 && !matchEditorToFile(prevMedal)) {
            hash.forceSetChangedFlag();
        }

        if (CMGUI.commonSaveDataSequence(this.parent, hash, true, "Medal " + prevMedal, false, (() -> {
            writeMedal(prevMedal);
        }))) {
            setVisible(false);
            dispose();
        }
    }//GEN-LAST:event_closeDialog

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        int medalID = medalList.getSelectedIndex();
        if (medalID > -1) {
            writeMedal(medalID);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void recordComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordComboActionPerformed
        SpinnerNumberModel model = (SpinnerNumberModel) IDSpinner.getModel();
        int val = ((Integer)model.getValue()).intValue();
        if (recordCombo.getSelectedIndex() == 0) {
            model.setMinimum(0);
            /*
            * Some medals do not correctly follow
            * this pattern. In the code itself,
            * all medals with a medal ID greater
            * than or equal to 0x2710 are considered
            * medal records either way. This is a
            * waste of a byte.

            model.setMaximum(0x270F);
            if (!(0 <= val && val <= 0x270F)) {
                model.setValue(Integer.valueOf(0));
            }
            */
            model.setMaximum(Integer.MAX_VALUE);
        } else {
            model.setMinimum(0x2710);
            model.setMaximum(0x2787);
            if (!(0x2710 <= val && val <= 0x2787)) {
                model.setValue(Integer.valueOf(0x2710));
            }
        }
    }//GEN-LAST:event_recordComboActionPerformed

    private void typeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_typeSpinnerStateChanged
        updateMedalIcon(((SpinnerNumberModel)typeSpinner.getModel()).getNumber().intValue());
    }//GEN-LAST:event_typeSpinnerStateChanged

    private void hintSelComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hintSelComboActionPerformed
        int hintSel = hintSelCombo.getSelectedIndex();
        if (hintSel != 0) {
            hintDescriptionField.setEnabled(true);
            hintImgLabel.setEnabled(true);
        } else {
            hintDescriptionField.setEnabled(false);
            hintImgLabel.setEnabled(false);
        }
        if (hintSel == 2) {
            hintStageSpinner.setEnabled(true);
        } else {
            hintStageSpinner.setEnabled(false);
            SpinnerNumberModel model = (SpinnerNumberModel) hintStageSpinner.getModel();
            model.setValue(0);
        }
    }//GEN-LAST:event_hintSelComboActionPerformed

    private void medalListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_medalListValueChanged
        if (!evt.getValueIsAdjusting()) {
            int medalID = medalList.getSelectedIndex();

            if (medalID != -1 && prevMedal != medalID) {
                ReflectionHash hash = new ReflectionHash(new Object());
                if (prevMedal != -1 && !matchEditorToFile(prevMedal)) {
                    hash.forceSetChangedFlag();
                }

                if (CMGUI.commonSaveDataSequence(this.parent, hash, true, "Medal " + prevMedal, false, (() -> {
                    writeMedal(prevMedal);
                }))) {
                    loadMedalEditor(medalID);
                } else {
                    medalList.setSelectedIndex(prevMedal);
                }
            }
        }
    }//GEN-LAST:event_medalListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner IDSpinner;
    private javax.swing.JLabel descLabel;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JTextField hintDescriptionField;
    private javax.swing.JLabel hintDescriptionLabel;
    private javax.swing.JLabel hintHeaderLabel;
    private javax.swing.JLabel hintImgLabel;
    private javax.swing.JComboBox<String> hintSelCombo;
    private javax.swing.JLabel hintSelLabel;
    private javax.swing.JLabel hintStageLabel;
    private javax.swing.JSpinner hintStageSpinner;
    private javax.swing.JScrollPane medalScrollPane;
    private javax.swing.JList<String> medalList;
    private javax.swing.JPanel medalPanel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JSpinner progressSpinner;
    private javax.swing.JComboBox<String> recordCombo;
    private javax.swing.JLabel recordIDLabel;
    private javax.swing.JLabel recordLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel typeImgLabel;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JSpinner typeSpinner;
    private javax.swing.JPanel windowPanel;
    // End of variables declaration//GEN-END:variables
}
