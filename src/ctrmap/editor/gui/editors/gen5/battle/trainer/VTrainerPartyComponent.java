package ctrmap.editor.gui.editors.gen5.battle.trainer;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.gen5.battle.SpriteImageLoader;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerPoke;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

public class VTrainerPartyComponent extends JPanel {

    private CTRMap Instance;
    private WBTrainerPoke pkmn;
    private Runnable onRemove;

    // UI fields
    private JLabel previewLabel;
    private JComboBox<String> speciesComboBox;
    private JSpinner levelSpinner, formSpinner, ivSpinner;
    private JComboBox<String> abilityComboBox, genderComboBox, heldItemComboBox;
    private JComboBox<String> Move1ComboBox, Move2ComboBox, Move3ComboBox, Move4ComboBox;

    // Sprite animation state — mirrors VTrainerEditor's setup so the
    // full battle sprite plays its NMAR cycle in the party slot preview.
    private List<ImageIcon> spriteFrames = new ArrayList<>();
    private int spriteAnimIndex = 0;
    private Timer spriteAnimTimer;
    private static final int SPRITE_ANIM_INTERVAL_MS = 16;

    public VTrainerPartyComponent(CTRMap Instance, WBTrainerPoke Pkmn, int slotIndex,
            TextFile PkmnNames, TextFile ItemNames, TextFile MoveNames, Runnable onRemove) {
        this.Instance = Instance;
        this.pkmn = Pkmn;
        this.onRemove = onRemove;
        initLayout(slotIndex);

        // Populate models
        speciesComboBox.setModel(new DefaultComboBoxModel(PkmnNames.getFriendlyLinesArray()));
        heldItemComboBox.setModel(new DefaultComboBoxModel(ItemNames.getFriendlyLinesArray()));
        String[] MoveNamesFriendly = MoveNames.getFriendlyLinesArray();
        Move1ComboBox.setModel(new DefaultComboBoxModel(MoveNamesFriendly));
        Move2ComboBox.setModel(new DefaultComboBoxModel(MoveNamesFriendly));
        Move3ComboBox.setModel(new DefaultComboBoxModel(MoveNamesFriendly));
        Move4ComboBox.setModel(new DefaultComboBoxModel(MoveNamesFriendly));

        // Initialize values (must come after model sets)
        UI_Init(Pkmn);

        // Handlers
        speciesComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null && Pkmn != null) {
                int sel = speciesComboBox.getSelectedIndex();
                if (sel >= 0) {
                    Pkmn.SetSpecies(sel);
                    loadPokemonIcon(sel);
                }
            }
        });
        levelSpinner.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null && Pkmn != null) {
                Pkmn.SetLevel((int) levelSpinner.getValue());
            }
        });
        formSpinner.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null && Pkmn != null) {
                Pkmn.SetForm((int) formSpinner.getValue());
            }
        });
        abilityComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null && Pkmn != null) {
                int sel = abilityComboBox.getSelectedIndex();
                if (sel >= 0) {
                    Pkmn.SetAbility(sel);
                }
            }
        });
        heldItemComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null && Pkmn != null) {
                int sel = heldItemComboBox.getSelectedIndex();
                if (sel >= 0) {
                    Pkmn.SetHeldItem(sel);
                }
            }
        });
        genderComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null && Pkmn != null) {
                int sel = genderComboBox.getSelectedIndex();
                if (sel >= 0) {
                    Pkmn.SetGender(sel);
                }
            }
        });
        ivSpinner.addChangeListener((ChangeEvent ce) -> {
            if (Instance != null && Pkmn != null) {
                Pkmn.SetIV((int) ivSpinner.getValue());
            }
        });
        Move1ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null && Pkmn != null) {
                int sel = Move1ComboBox.getSelectedIndex();
                if (sel >= 0) Pkmn.SetMove(0, sel);
            }
        });
        Move2ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null && Pkmn != null) {
                int sel = Move2ComboBox.getSelectedIndex();
                if (sel >= 0) Pkmn.SetMove(1, sel);
            }
        });
        Move3ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null && Pkmn != null) {
                int sel = Move3ComboBox.getSelectedIndex();
                if (sel >= 0) Pkmn.SetMove(2, sel);
            }
        });
        Move4ComboBox.addActionListener((ActionEvent ae) -> {
            if (Instance != null && Pkmn != null) {
                int sel = Move4ComboBox.getSelectedIndex();
                if (sel >= 0) Pkmn.SetMove(3, sel);
            }
        });
    }

    private void UI_Init(WBTrainerPoke Pkmn) {
        if (Instance != null && Pkmn != null) {
            speciesComboBox.setSelectedIndex(Pkmn.GetSpecies());
            levelSpinner.setValue(Pkmn.GetLevel());
            formSpinner.setValue(Pkmn.GetForm());
            abilityComboBox.setSelectedIndex(Pkmn.GetAbility());
            heldItemComboBox.setSelectedIndex(Pkmn.GetHeldItem());
            genderComboBox.setSelectedIndex(Pkmn.GetGender());
            ivSpinner.setValue(Pkmn.GetIV());
            Move1ComboBox.setSelectedIndex(Pkmn.GetMove(0));
            Move2ComboBox.setSelectedIndex(Pkmn.GetMove(1));
            Move3ComboBox.setSelectedIndex(Pkmn.GetMove(2));
            Move4ComboBox.setSelectedIndex(Pkmn.GetMove(3));
            loadPokemonIcon(Pkmn.GetSpecies());
        }
    }

    private NTRGameFS FS() {
        return Instance.getMissionControl(VLaunchpad.class).fs;
    }

    private void loadPokemonIcon(int speciesIndex) {
        // Stop any running animation and reset state before loading the
        // new species' frames.
        if (spriteAnimTimer != null) {
            spriteAnimTimer.stop();
        }
        spriteFrames.clear();
        spriteAnimIndex = 0;

        try {
            // Loads the full animated battle sprite from NARC 004
            // (PML_G2D_POKE_SPRITE), NOT the small party icon at NARC 007.
            // Every species including 0 has sprite data in 004 — placeholder
            // slots render as "?" which is their authored content.
            List<BufferedImage> frames = SpriteImageLoader
                .loadPokemonBattleSpriteFrames(FS(), speciesIndex);

            if (frames.isEmpty()) {
                previewLabel.setIcon(null);
                previewLabel.setText("No Sprite");
                return;
            }

            for (BufferedImage frame : frames) {
                spriteFrames.add(new ImageIcon(frame));
            }
            previewLabel.setIcon(spriteFrames.get(0));
            previewLabel.setText("");

            if (spriteFrames.size() > 1) {
                if (spriteAnimTimer == null) {
                    spriteAnimTimer = new Timer(SPRITE_ANIM_INTERVAL_MS, e -> advanceSpriteAnim());
                }
                spriteAnimTimer.start();
            }
        } catch (Exception e) {
            previewLabel.setIcon(null);
            previewLabel.setText("?");
        }
    }

    private void advanceSpriteAnim() {
        if (spriteFrames.isEmpty()) return;
        spriteAnimIndex = (spriteAnimIndex + 1) % spriteFrames.size();
        previewLabel.setIcon(spriteFrames.get(spriteAnimIndex));
    }

    /**
     * Must be called when this component is removed from the UI (e.g.
     * when the trainer selector changes and the party list is rebuilt)
     * so the sprite animation Timer stops firing. Swing's AncestorListener
     * is flaky for nested scroll panes — call this explicitly from
     * VTrainerEditor.UI_UpdatePartyList before removing children instead.
     */
    public void dispose() {
        if (spriteAnimTimer != null) {
            spriteAnimTimer.stop();
            spriteAnimTimer = null;
        }
        spriteFrames.clear();
    }

    private void initLayout(int slotIndex) {
        setBorder(BorderFactory.createTitledBorder("Slot " + (slotIndex + 1)));
        setLayout(new BorderLayout(4, 0));

        // Pokemon icon preview on the left
        previewLabel = new JLabel("?", SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(64, 64));
        previewLabel.setMinimumSize(new Dimension(64, 64));
        previewLabel.setMaximumSize(new Dimension(64, 64));
        previewLabel.setBorder(BorderFactory.createEtchedBorder());
        add(previewLabel, BorderLayout.WEST);

        // Fields panel in center
        JPanel fieldsPanel = new JPanel(new GridBagLayout());

        speciesComboBox = new JComboBox<>();
        speciesComboBox.setMaximumRowCount(20);
        levelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        formSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        abilityComboBox = new JComboBox<>(new String[]{"Random", "Primary", "Secondary", "Hidden"});
        heldItemComboBox = new JComboBox<>();
        heldItemComboBox.setMaximumRowCount(20);
        genderComboBox = new JComboBox<>(new String[]{"Random", "Male", "Female"});
        ivSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
        Move1ComboBox = new JComboBox<>();
        Move2ComboBox = new JComboBox<>();
        Move3ComboBox = new JComboBox<>();
        Move4ComboBox = new JComboBox<>();
        Move1ComboBox.setMaximumRowCount(20);
        Move2ComboBox.setMaximumRowCount(20);
        Move3ComboBox.setMaximumRowCount(20);
        Move4ComboBox.setMaximumRowCount(20);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 3, 1, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Species + Level + X button
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Species"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 3;
        fieldsPanel.add(speciesComboBox, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 4; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Lv"), gbc);
        gbc.gridx = 5; gbc.weightx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        levelSpinner.setPreferredSize(new Dimension(52, 22));
        fieldsPanel.add(levelSpinner, gbc);
        gbc.gridx = 6; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JButton removeBtn = new JButton("\u00D7");
        removeBtn.setMargin(new Insets(0, 4, 0, 4));
        removeBtn.setToolTipText("Remove this Pokemon");
        removeBtn.addActionListener((ActionEvent ae) -> {
            int result = JOptionPane.showConfirmDialog(
                VTrainerPartyComponent.this,
                "Remove this Pokemon from the party?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION && onRemove != null) {
                onRemove.run();
            }
        });
        fieldsPanel.add(removeBtn, gbc);

        // Row 1: Form + Ability + Gender
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Form"), gbc);
        gbc.gridx = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formSpinner.setPreferredSize(new Dimension(48, 22));
        fieldsPanel.add(formSpinner, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Ability"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        fieldsPanel.add(abilityComboBox, gbc);
        gbc.gridx = 4; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Gender"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        fieldsPanel.add(genderComboBox, gbc);

        // Row 2: IVs + Held Item
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("IVs"), gbc);
        gbc.gridx = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        ivSpinner.setPreferredSize(new Dimension(48, 22));
        fieldsPanel.add(ivSpinner, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Held Item"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 3;
        fieldsPanel.add(heldItemComboBox, gbc);
        gbc.gridwidth = 1;

        // Row 3: Move 1 + Move 2
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Move 1"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 2;
        fieldsPanel.add(Move1ComboBox, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Move 2"), gbc);
        gbc.gridx = 4; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 2;
        fieldsPanel.add(Move2ComboBox, gbc);
        gbc.gridwidth = 1;

        // Row 4: Move 3 + Move 4
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Move 3"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 2;
        fieldsPanel.add(Move3ComboBox, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        fieldsPanel.add(new JLabel("Move 4"), gbc);
        gbc.gridx = 4; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 2;
        fieldsPanel.add(Move4ComboBox, gbc);
        gbc.gridwidth = 1;

        add(fieldsPanel, BorderLayout.CENTER);

        // Constrain vertical size so cards don't stretch
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }
}
