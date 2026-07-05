package ctrmap.editor.gui.editors.gen5.level.entities;

import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import ctrmap.editor.gui.editors.gen5.level.tools.VInitScriptsTool;
import ctrmap.formats.pokemon.gen5.zone.entities.VZoneInitScriptDispatcher;
import ctrmap.formats.pokemon.gen5.zone.entities.VZoneInitScriptDispatcher.DynamicInitScript;
import ctrmap.formats.pokemon.gen5.zone.entities.VZoneInitScriptDispatcher.StaticInitScript;
import ctrmap.formats.pokemon.gen5.zone.entities.VZoneInitScriptDispatcher.WkCmpEntry;
import ctrmap.missioncontrol_ntr.field.debug.VZoneDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import xstandard.util.ArraysEx;

public class VInitScriptEditor extends JPanel implements AbstractToolbarEditor, VZoneDebugger {

    private VInitScriptsTool tool;
    private VZoneInitScriptDispatcher dispatcher;
    private boolean updating = false;

    // Static tab
    private DefaultTableModel staticTableModel;
    private JTable staticTable;
    private JSpinner staticTagSpinner, staticScrIDSpinner;
    private JButton staticAddBtn, staticRemoveBtn;

    // Dynamic tab
    private DefaultTableModel dynTableModel;
    private JTable dynTable;
    private JSpinner dynWkIDSpinner, dynRefValSpinner, dynScrIDSpinner;
    private JButton dynAddBtn, dynRemoveBtn;

    public VInitScriptEditor() {
        tool = new VInitScriptsTool(this);
        initLayout();
        initListeners();
    }

    private void initLayout() {
        setLayout(new BorderLayout(4, 4));

        JTabbedPane tabs = new JTabbedPane();

        // ===== Static (Condition-Based) Tab =====
        JPanel staticPanel = new JPanel(new BorderLayout(4, 4));
        staticPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        staticTableModel = new DefaultTableModel(new String[]{"Tag (Condition)", "Script ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        staticTable = new JTable(staticTableModel);
        staticTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        staticPanel.add(new JScrollPane(staticTable), BorderLayout.CENTER);

        // Entry editor
        JPanel staticEntryPanel = new JPanel(new GridBagLayout());
        staticEntryPanel.setBorder(BorderFactory.createTitledBorder("Entry"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        staticEntryPanel.add(new JLabel("Tag (Condition)"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        staticTagSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 65535, 1));
        staticEntryPanel.add(staticTagSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        staticEntryPanel.add(new JLabel("Script ID"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        staticScrIDSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        staticEntryPanel.add(staticScrIDSpinner, gbc);

        JPanel staticBtnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        staticAddBtn = new JButton("Add");
        staticRemoveBtn = new JButton("Remove");
        staticBtnPanel.add(staticAddBtn);
        staticBtnPanel.add(staticRemoveBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        staticEntryPanel.add(staticBtnPanel, gbc);

        staticPanel.add(staticEntryPanel, BorderLayout.SOUTH);
        tabs.addTab("Static (Condition-Based)", staticPanel);

        // ===== Dynamic (Variable-Based) Tab =====
        JPanel dynPanel = new JPanel(new BorderLayout(4, 4));
        dynPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        dynTableModel = new DefaultTableModel(new String[]{"Work ID", "Expected Value", "Script ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        dynTable = new JTable(dynTableModel);
        dynTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dynPanel.add(new JScrollPane(dynTable), BorderLayout.CENTER);

        // Entry editor
        JPanel dynEntryPanel = new JPanel(new GridBagLayout());
        dynEntryPanel.setBorder(BorderFactory.createTitledBorder("Entry"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(2, 4, 2, 4);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.weightx = 0;
        dynEntryPanel.add(new JLabel("Work ID"), gbc2);
        gbc2.gridx = 1; gbc2.weightx = 1.0;
        dynWkIDSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 65535, 1));
        dynEntryPanel.add(dynWkIDSpinner, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 1; gbc2.weightx = 0;
        dynEntryPanel.add(new JLabel("Expected Value"), gbc2);
        gbc2.gridx = 1; gbc2.weightx = 1.0;
        dynRefValSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        dynEntryPanel.add(dynRefValSpinner, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 2; gbc2.weightx = 0;
        dynEntryPanel.add(new JLabel("Script ID"), gbc2);
        gbc2.gridx = 1; gbc2.weightx = 1.0;
        dynScrIDSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        dynEntryPanel.add(dynScrIDSpinner, gbc2);

        JPanel dynBtnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        dynAddBtn = new JButton("Add");
        dynRemoveBtn = new JButton("Remove");
        dynBtnPanel.add(dynAddBtn);
        dynBtnPanel.add(dynRemoveBtn);

        gbc2.gridx = 0; gbc2.gridy = 3; gbc2.gridwidth = 2;
        dynEntryPanel.add(dynBtnPanel, gbc2);

        dynPanel.add(dynEntryPanel, BorderLayout.SOUTH);
        tabs.addTab("Dynamic (Variable-Based)", dynPanel);

        add(tabs, BorderLayout.CENTER);
    }

    private void initListeners() {
        // Static table selection → populate entry fields
        staticTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || updating || dispatcher == null) return;
            int sel = staticTable.getSelectedRow();
            if (sel >= 0 && sel < dispatcher.staticScripts.size()) {
                updating = true;
                StaticInitScript s = dispatcher.staticScripts.get(sel);
                staticTagSpinner.setValue(s.tag);
                staticScrIDSpinner.setValue(s.SCRID);
                updating = false;
            }
        });

        // Static spinner changes update selected row live
        staticTagSpinner.addChangeListener(e -> {
            if (updating || dispatcher == null) return;
            int sel = staticTable.getSelectedRow();
            if (sel >= 0 && sel < dispatcher.staticScripts.size()) {
                updating = true;
                dispatcher.staticScripts.get(sel).tag = (int) staticTagSpinner.getValue();
                refreshStaticTable();
                staticTable.setRowSelectionInterval(sel, sel);
                updating = false;
            }
        });
        staticScrIDSpinner.addChangeListener(e -> {
            if (updating || dispatcher == null) return;
            int sel = staticTable.getSelectedRow();
            if (sel >= 0 && sel < dispatcher.staticScripts.size()) {
                updating = true;
                dispatcher.staticScripts.get(sel).SCRID = (int) staticScrIDSpinner.getValue();
                refreshStaticTable();
                staticTable.setRowSelectionInterval(sel, sel);
                updating = false;
            }
        });

        // Static Add — new entry with defaults, then select it for editing
        staticAddBtn.addActionListener(e -> {
            if (dispatcher != null) {
                updating = true;
                StaticInitScript s = new StaticInitScript();
                s.tag = 2;
                s.SCRID = 0;
                dispatcher.staticScripts.add(s);
                refreshStaticTable();
                int idx = dispatcher.staticScripts.size() - 1;
                staticTable.setRowSelectionInterval(idx, idx);
                staticTagSpinner.setValue(s.tag);
                staticScrIDSpinner.setValue(s.SCRID);
                updating = false;
            }
        });

        // Static Remove
        staticRemoveBtn.addActionListener(e -> {
            if (dispatcher != null) {
                int sel = staticTable.getSelectedRow();
                if (sel >= 0 && sel < dispatcher.staticScripts.size()) {
                    updating = true;
                    dispatcher.staticScripts.remove(sel);
                    refreshStaticTable();
                    updating = false;
                }
            }
        });

        // Dynamic table selection → populate entry fields
        dynTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || updating || dispatcher == null || dispatcher.dynScr == null) return;
            int sel = dynTable.getSelectedRow();
            if (sel >= 0 && sel < dispatcher.dynScr.branches.size()) {
                updating = true;
                WkCmpEntry entry = dispatcher.dynScr.branches.get(sel);
                dynWkIDSpinner.setValue(entry.wkId);
                dynRefValSpinner.setValue(entry.refVal);
                dynScrIDSpinner.setValue(entry.SCRID);
                updating = false;
            }
        });

        // Dynamic spinner changes update selected row live
        dynWkIDSpinner.addChangeListener(e -> {
            if (updating || dispatcher == null || dispatcher.dynScr == null) return;
            int sel = dynTable.getSelectedRow();
            if (sel >= 0 && sel < dispatcher.dynScr.branches.size()) {
                updating = true;
                dispatcher.dynScr.branches.get(sel).wkId = (int) dynWkIDSpinner.getValue();
                refreshDynTable();
                dynTable.setRowSelectionInterval(sel, sel);
                updating = false;
            }
        });
        dynRefValSpinner.addChangeListener(e -> {
            if (updating || dispatcher == null || dispatcher.dynScr == null) return;
            int sel = dynTable.getSelectedRow();
            if (sel >= 0 && sel < dispatcher.dynScr.branches.size()) {
                updating = true;
                dispatcher.dynScr.branches.get(sel).refVal = (int) dynRefValSpinner.getValue();
                refreshDynTable();
                dynTable.setRowSelectionInterval(sel, sel);
                updating = false;
            }
        });
        dynScrIDSpinner.addChangeListener(e -> {
            if (updating || dispatcher == null || dispatcher.dynScr == null) return;
            int sel = dynTable.getSelectedRow();
            if (sel >= 0 && sel < dispatcher.dynScr.branches.size()) {
                updating = true;
                dispatcher.dynScr.branches.get(sel).SCRID = (int) dynScrIDSpinner.getValue();
                refreshDynTable();
                dynTable.setRowSelectionInterval(sel, sel);
                updating = false;
            }
        });

        // Dynamic Add — new entry with defaults, then select it for editing
        dynAddBtn.addActionListener(e -> {
            if (dispatcher != null) {
                updating = true;
                if (dispatcher.dynScr == null) {
                    dispatcher.dynScr = new DynamicInitScript();
                }
                WkCmpEntry entry = new WkCmpEntry();
                entry.wkId = 1;
                entry.refVal = 0;
                entry.SCRID = 0;
                dispatcher.dynScr.branches.add(entry);
                refreshDynTable();
                int idx = dispatcher.dynScr.branches.size() - 1;
                dynTable.setRowSelectionInterval(idx, idx);
                dynWkIDSpinner.setValue(entry.wkId);
                dynRefValSpinner.setValue(entry.refVal);
                dynScrIDSpinner.setValue(entry.SCRID);
                updating = false;
            }
        });

        // Dynamic Remove
        dynRemoveBtn.addActionListener(e -> {
            if (dispatcher != null && dispatcher.dynScr != null) {
                int sel = dynTable.getSelectedRow();
                if (sel >= 0 && sel < dispatcher.dynScr.branches.size()) {
                    updating = true;
                    dispatcher.dynScr.branches.remove(sel);
                    refreshDynTable();
                    updating = false;
                }
            }
        });
    }

    // ===== Data Binding =====

    @Override
    public void loadZone(VZone z) {
        if (z != null && z.initScrUnused != null) {
            this.dispatcher = z.initScrUnused;
        } else {
            this.dispatcher = null;
        }
        refreshStaticTable();
        refreshDynTable();
    }

    private void refreshStaticTable() {
        staticTableModel.setRowCount(0);
        if (dispatcher != null) {
            for (StaticInitScript s : dispatcher.staticScripts) {
                staticTableModel.addRow(new Object[]{s.tag, s.SCRID});
            }
        }
    }

    private void refreshDynTable() {
        dynTableModel.setRowCount(0);
        if (dispatcher != null && dispatcher.dynScr != null) {
            for (WkCmpEntry e : dispatcher.dynScr.branches) {
                dynTableModel.addRow(new Object[]{e.wkId, e.refVal, e.SCRID});
            }
        }
    }

    // ===== AbstractToolbarEditor =====

    @Override
    public List<AbstractTool> getTools() {
        return ArraysEx.asList(tool);
    }
}
