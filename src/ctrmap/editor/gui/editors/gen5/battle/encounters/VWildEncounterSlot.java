package ctrmap.editor.gui.editors.gen5.battle.encounters;

import ctrmap.formats.pokemon.gen5.battle.encounters.WBEncEntry;
import ctrmap.formats.pokemon.gen5.battle.encounters.WBEnc;
import ctrmap.formats.pokemon.text.TextFile;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;

public class VWildEncounterSlot extends JPanel {

    static final int[] GRASS_RATES = {20, 20, 10, 10, 10, 10, 5, 5, 4, 4, 1, 1};
    static final int[] SURF_RATES  = {60, 30, 5, 4, 1};
    static final int[] FISH_RATES  = {60, 30, 5, 4, 1};

    private WBEncEntry entry;
    private JComboBox<String> speciesCombo;
    private JSpinner formSpinner, minLevelSpinner, maxLevelSpinner;
    private JLabel rateLabel;

    public VWildEncounterSlot(WBEncEntry entry, TextFile pkmnNames, int slotIndex, WBEnc.WBEncType encType) {
        this.entry = entry;
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Determine rate
        int rate = 0;
        int[] rates = null;
        switch (encType) {
            case GRASS:
            case GRASS_DOUBLE:
            case GRASS_RARE:
                rates = GRASS_RATES;
                break;
            case SURF:
            case SURF_RARE:
                rates = SURF_RATES;
                break;
            case FISH:
            case FISH_RARE:
                rates = FISH_RATES;
                break;
        }
        if (rates != null && slotIndex >= 0 && slotIndex < rates.length) {
            rate = rates[slotIndex];
        }

        // Rate label with color coding
        rateLabel = new JLabel(rate + "%", SwingConstants.RIGHT);
        rateLabel.setPreferredSize(new Dimension(40, 22));
        rateLabel.setFont(rateLabel.getFont().deriveFont(Font.BOLD));
        rateLabel.setOpaque(true);
        if (rate >= 10) {
            rateLabel.setBackground(new Color(144, 238, 144)); // green
        } else if (rate >= 4) {
            rateLabel.setBackground(new Color(255, 255, 150)); // yellow
        } else {
            rateLabel.setBackground(new Color(255, 180, 120)); // orange/red
        }
        add(rateLabel);

        // Species combo box
        speciesCombo = new JComboBox<>(pkmnNames.getFriendlyLinesArray());
        speciesCombo.setPreferredSize(new Dimension(180, 22));
        if (entry != null) {
            int dexNum = entry.GetDexNum();
            if (dexNum >= 0 && dexNum < speciesCombo.getItemCount()) {
                speciesCombo.setSelectedIndex(dexNum);
            }
        }
        speciesCombo.addActionListener((ActionEvent ae) -> {
            short selIndex = (short) speciesCombo.getSelectedIndex();
            if (this.entry != null && selIndex >= 0) {
                this.entry.SetDexNum(selIndex);
            }
        });
        add(speciesCombo);

        // Form label + spinner
        add(new JLabel("Form"));
        formSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        formSpinner.setPreferredSize(new Dimension(50, 22));
        if (entry != null) {
            formSpinner.setValue((int) entry.GetFormNum());
        }
        formSpinner.addChangeListener((ChangeEvent c) -> {
            short selVal = ((Number) formSpinner.getValue()).shortValue();
            if (this.entry != null && selVal >= 0) {
                this.entry.SetFormNum(selVal);
            }
        });
        add(formSpinner);

        // Level labels + spinners
        add(new JLabel("Lv"));
        minLevelSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        minLevelSpinner.setPreferredSize(new Dimension(45, 22));
        if (entry != null) {
            minLevelSpinner.setValue((int) entry.GetMinLevel());
        }
        minLevelSpinner.addChangeListener((ChangeEvent c) -> {
            byte selVal = ((Number) minLevelSpinner.getValue()).byteValue();
            if (this.entry != null && selVal >= 0) {
                this.entry.SetMinLevel(selVal);
            }
        });
        add(minLevelSpinner);

        add(new JLabel("-"));

        maxLevelSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        maxLevelSpinner.setPreferredSize(new Dimension(45, 22));
        if (entry != null) {
            maxLevelSpinner.setValue((int) entry.GetMaxLevel());
        }
        maxLevelSpinner.addChangeListener((ChangeEvent c) -> {
            byte selVal = ((Number) maxLevelSpinner.getValue()).byteValue();
            if (this.entry != null && selVal >= 0) {
                this.entry.SetMaxLevel(selVal);
            }
        });
        add(maxLevelSpinner);
    }
}
