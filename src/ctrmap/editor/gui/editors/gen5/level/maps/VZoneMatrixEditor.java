/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ctrmap.editor.gui.editors.gen5.level.maps;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.gui.editors.gen5.battle.encounters.VWildEditor;
import ctrmap.formats.pokemon.gen5.mapmatrix.VMapMatrix;
import ctrmap.formats.pokemon.gen5.zone.VZoneTable;
import ctrmap.missioncontrol_ntr.field.debug.VZoneDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class VZoneMatrixEditor extends javax.swing.JPanel implements VZoneDebugger, AbstractTabbedEditor {
    private CTRMap Instance;
    private VMapMatrix matrix;

    /**
     * Creates new form VZoneMatrixEditor_
     */
    
    public VZoneMatrixEditor(CTRMap Instance) {
        initComponents();
        this.Instance = Instance;
        this.jMatrixViewMaps.setCellSelectionEnabled(true);
        this.jMatrixViewMaps.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jMatrixViewMaps.rowAtPoint(evt.getPoint());
                int col = jMatrixViewMaps.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0 && matrix.hasZones) {
                }
            }
        });
        
        
        VZoneTable zoneTable = new VZoneTable(FS().NARCGet(NARCRef.FIELD_ZONE_DATA, 0));
        
        for (int i = 0; i < zoneTable.getZoneCount(); ++i) {
        }
        
        this.jUseZoneHeaders.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                matrix.hasZones = e.getStateChange() == 1;
            }
         });
    }

    public String getTabName() {
        return "Zone Matrix Editor";
    }
    
    NTRGameFS FS() {
        return Instance.getMissionControl(ctrmap.missioncontrol_ntr.VLaunchpad.class).fs;
    }
        
    @Override
    public void loadZone(VZone z) {
        if (z != null) {
            try {
                LoadMatrix(z.header.matrixID);
            } catch (IOException ex) {
                Logger.getLogger(VWildEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    void LoadMatrix(int Index) throws IOException {
        int Max = FS().NARCGetDataMax(NARCRef.FIELD_MAP_MATRIX);
        
        if (0 <= Index && Index < Max) {
            DefaultTableModel matrixMapModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            DefaultTableModel zoneHeadersModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            matrix = new VMapMatrix(FS().NARCGet(NARCRef.FIELD_MAP_MATRIX, Index));
            if (matrix != null) {
                int rowCount = matrix.getHeight();
                int colCount = matrix.getWidth();
                
                // Set spinners to row and col counts
                jWidthSpinner.setValue(colCount);
                jHeightSpinner.setValue(rowCount);
                
                // If there are row headers, then set the checkbox and enable the tab.
                jUseZoneHeaders.setSelected(matrix.hasZones);
                jScrollPane2.setEnabled(matrix.hasZones);
                
                // Create the row of numbers (TODO: I feel like there's a one liner for this here)
                String[] rowNames = new String[rowCount];
                for (int y = 0; y < rowCount; ++y) {
                    rowNames[y] = "" + y;
                }
                
                // Populate matrices.
                matrixMapModel.addColumn("Row/Col", rowNames);
                zoneHeadersModel.addColumn("Row/Col", rowNames);
                for (int x = 0; x < colCount; ++x) {
                    matrixMapModel.addColumn(x, matrix.chunkIds.list.get(x).toArray());
                    if (matrix.hasZones) { 
                        zoneHeadersModel.addColumn(x, matrix.zoneIds.list.get(x).toArray());
                    }
                }
            }
            
            // Attach listener for matrix
            jMatrixViewMaps.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        int row = jMatrixViewMaps.rowAtPoint(e.getPoint());
                        int col = jMatrixViewMaps.columnAtPoint(e.getPoint());

                        if (row >= 0 && col >= 0) {
                            Object value = jMatrixViewMaps.getValueAt(row, col);
                            System.out.println("Clicked cell at row " + row + ", col " + col + " → " + value);
                        }
                    }
                }
            );
            
            jMatrixViewMaps.setModel(matrixMapModel);
            jMatrixViewMaps.getColumnModel().getColumn(0).setCellRenderer(
                    (tbl, value, isSelected, hasFocus, row, col) -> jMatrixViewMaps.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(tbl, String.valueOf(row), isSelected, hasFocus, row, col)
            );
            jMatrixViewMaps.revalidate();
            jMatrixViewMaps.repaint();
            
            // Attach listener for zone header
            jMatrixViewZoneHeaders.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        int row = jMatrixViewZoneHeaders.rowAtPoint(e.getPoint());
                        int col = jMatrixViewZoneHeaders.columnAtPoint(e.getPoint());

                        if (row >= 0 && col >= 0) {
                            Object value = jMatrixViewZoneHeaders.getValueAt(row, col);
                            System.out.println("Clicked cell at row " + row + ", col " + col + " → " + value);
                        }
                    }
                }
            );
            
            jMatrixViewZoneHeaders.setModel(zoneHeadersModel);   
            jMatrixViewZoneHeaders.getColumnModel().getColumn(0).setCellRenderer(
                    (tbl, value, isSelected, hasFocus, row, col) -> jMatrixViewZoneHeaders.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(tbl, String.valueOf(row), isSelected, hasFocus, row, col)
            );           
            jMatrixViewZoneHeaders.revalidate();
            jMatrixViewZoneHeaders.repaint();
        }
    }
    
    void SaveMatrix(int Index) throws IOException {
        int Max = FS().NARCGetDataMax(NARCRef.FIELD_MAP_MATRIX);
        
        if (0 <= Index && Index < Max) {

        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scriptPnl14 = new javax.swing.JPanel();
        btnOpenScrInIDE22 = new javax.swing.JButton();
        btnOpenScrInIDE23 = new javax.swing.JButton();
        trainerSpinner = new javax.swing.JSpinner();
        jUseZoneHeaders = new javax.swing.JCheckBox();
        jWidthLabel = new javax.swing.JLabel();
        jWidthSpinner = new javax.swing.JSpinner();
        jHeightSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMatrixViewMaps = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jMatrixViewZoneHeaders = new javax.swing.JTable();

        scriptPnl14.setBorder(javax.swing.BorderFactory.createTitledBorder("Matrix"));

        btnOpenScrInIDE22.setText("Open Matrix");
        btnOpenScrInIDE22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenScrInIDE22ActionPerformed(evt);
            }
        });

        btnOpenScrInIDE23.setText("Save Matrix");
        btnOpenScrInIDE23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenScrInIDE23ActionPerformed(evt);
            }
        });

        jUseZoneHeaders.setText("Use Zone Headers");

        jWidthLabel.setText("Width");

        jLabel3.setText("Height");

        jButton3.setText("Update");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout scriptPnl14Layout = new javax.swing.GroupLayout(scriptPnl14);
        scriptPnl14.setLayout(scriptPnl14Layout);
        scriptPnl14Layout.setHorizontalGroup(
            scriptPnl14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptPnl14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(trainerSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOpenScrInIDE22, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOpenScrInIDE23, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 233, Short.MAX_VALUE)
                .addComponent(jUseZoneHeaders)
                .addGap(18, 18, 18)
                .addComponent(jWidthLabel)
                .addGap(22, 22, 22)
                .addComponent(jWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jHeightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        scriptPnl14Layout.setVerticalGroup(
            scriptPnl14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptPnl14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnOpenScrInIDE22)
                .addComponent(btnOpenScrInIDE23)
                .addComponent(trainerSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jUseZoneHeaders)
                .addComponent(jWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jWidthLabel)
                .addComponent(jHeightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel3)
                .addComponent(jButton3))
        );

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jMatrixViewMaps.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jMatrixViewMaps.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jMatrixViewMaps.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jMatrixViewMaps);

        jTabbedPane1.addTab("Maps", jScrollPane1);

        jMatrixViewZoneHeaders.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jMatrixViewZoneHeaders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jMatrixViewZoneHeaders.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jMatrixViewZoneHeaders);

        jTabbedPane1.addTab("Zone Headers", jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scriptPnl14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scriptPnl14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                .addGap(35, 35, 35))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenScrInIDE22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenScrInIDE22ActionPerformed
        try {
            LoadMatrix((Integer) trainerSpinner.getValue());
        } catch (IOException ex) {
            Logger.getLogger(VZoneMatrixEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnOpenScrInIDE22ActionPerformed

    private void btnOpenScrInIDE23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenScrInIDE23ActionPerformed
        try {
            SaveMatrix((Integer) trainerSpinner.getValue());
        } catch (IOException ex) {
            Logger.getLogger(VZoneMatrixEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnOpenScrInIDE23ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOpenScrInIDE22;
    private javax.swing.JButton btnOpenScrInIDE23;
    private javax.swing.JButton jButton3;
    private javax.swing.JSpinner jHeightSpinner;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTable jMatrixViewMaps;
    private javax.swing.JTable jMatrixViewZoneHeaders;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JCheckBox jUseZoneHeaders;
    private javax.swing.JLabel jWidthLabel;
    private javax.swing.JSpinner jWidthSpinner;
    private javax.swing.JPanel scriptPnl14;
    private javax.swing.JSpinner trainerSpinner;
    // End of variables declaration//GEN-END:variables
}
