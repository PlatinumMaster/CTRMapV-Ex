package ctrmap.editor.gui.editors.gen5.battle.trainer;

import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerMessage;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerMessageTable;
import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerMessageTable.IndexedMessage;
import ctrmap.formats.pokemon.text.TextFile;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Edits a single trainer's dialogue (trainer-message entries in ARC 090 +
 * strings in MSGDATA_SYSTEM file 381).
 *
 * <p>Layout:</p>
 * <ul>
 *   <li>Top-left: list of the current trainer's messages (one row per
 *       {@link WBTrainerMessage#messageType}), showing type + preview.</li>
 *   <li>Right: editable {@link JTextArea} holding the full selected line,
 *       writes back to the parallel {@link TextFile} on every keystroke
 *       via a {@link DocumentListener}.</li>
 *   <li>Bottom: "Add" (new entry + new line) / "Remove" buttons + a
 *       message-type spinner for the new entry.</li>
 * </ul>
 */
public class VTrainerTextPanel extends JPanel {

    private final DefaultListModel<IndexedMessage> listModel = new DefaultListModel<>();
    private final JList<IndexedMessage> messageList = new JList<>(listModel);
    private final JTextArea textArea = new JTextArea();
    private final JSpinner newTypeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
    private final JButton addButton = new JButton("Add Message");
    private final JButton removeButton = new JButton("Remove");
    private final JLabel unsupportedLabel = new JLabel(
        "<html><center>Trainer messages are only supported in BW2.<br>"
        + "No trainer-message NARCs in this project.</center></html>",
        JLabel.CENTER);

    private int currentTrainerId = -1;
    private WBTrainerMessageTable table;
    private TextFile textFile;
    /** Guard to suppress DocumentListener during programmatic setText. */
    private boolean suppressTextUpdate = false;

    public VTrainerTextPanel() {
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        buildUi();
        wireListeners();
        showUnsupported(false);
    }

    private void buildUi() {
        // Left: message list
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String typeName = messageTypeLabel(value.message.messageType);
            String preview = textFile != null ? snippet(textFile.getLine(value.textIndex)) : "";
            JLabel lbl = new JLabel("[" + value.message.messageType + ":" + typeName
                + "]  " + preview);
            lbl.setOpaque(true);
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } else {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            return lbl;
        });
        JScrollPane listScroll = new JScrollPane(messageList);
        listScroll.setPreferredSize(new Dimension(240, 240));
        add(listScroll, BorderLayout.WEST);

        // Center: editable text area
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(BorderFactory.createTitledBorder("Message Text"));
        add(textScroll, BorderLayout.CENTER);

        // Bottom: add/remove controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        controls.add(new JLabel("New message type:"));
        controls.add(newTypeSpinner);
        controls.add(addButton);
        controls.add(removeButton);
        add(controls, BorderLayout.SOUTH);
    }

    private void wireListeners() {
        messageList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            IndexedMessage sel = messageList.getSelectedValue();
            if (sel == null || textFile == null) {
                suppressTextUpdate = true;
                textArea.setText("");
                suppressTextUpdate = false;
                removeButton.setEnabled(false);
                return;
            }
            suppressTextUpdate = true;
            textArea.setText(textFile.getLine(sel.textIndex));
            textArea.setCaretPosition(0);
            suppressTextUpdate = false;
            removeButton.setEnabled(true);
        });

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { push(); }
            @Override public void removeUpdate(DocumentEvent e) { push(); }
            @Override public void changedUpdate(DocumentEvent e) { push(); }
            private void push() {
                if (suppressTextUpdate) return;
                IndexedMessage sel = messageList.getSelectedValue();
                if (sel == null || textFile == null) return;
                textFile.setLine(sel.textIndex, textArea.getText());
                // Repaint list so the preview updates live
                messageList.repaint();
            }
        });

        addButton.addActionListener(e -> {
            if (table == null || textFile == null || currentTrainerId < 0) return;
            int msgType = (Integer) newTypeSpinner.getValue();
            int newIdx = table.addEntry(currentTrainerId, msgType);
            // Keep the parallel TextFile in sync: append a blank line at
            // the end so its index matches the new record's position.
            textFile.appendLine("");
            refreshForCurrentTrainer();
            // Select the newly-added row
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.get(i).textIndex == newIdx) {
                    messageList.setSelectedIndex(i);
                    break;
                }
            }
        });

        removeButton.addActionListener(e -> {
            if (table == null || textFile == null) return;
            IndexedMessage sel = messageList.getSelectedValue();
            if (sel == null) return;
            table.removeEntry(sel.textIndex);
            textFile.removeLine(sel.textIndex);
            refreshForCurrentTrainer();
        });
    }

    /**
     * Binds this panel to a trainer. Called by VTrainerEditor when the
     * trainer selector changes. {@code messageTable} may have
     * {@code isLoaded()==false} (BW1) — in that case the panel renders a
     * "not supported" notice.
     */
    public void setTrainer(int trainerId, WBTrainerMessageTable messageTable, TextFile trDialogue) {
        this.currentTrainerId = trainerId;
        this.table = messageTable;
        this.textFile = trDialogue;
        showUnsupported(messageTable == null || !messageTable.isLoaded() || trDialogue == null);
        refreshForCurrentTrainer();
    }

    private void refreshForCurrentTrainer() {
        listModel.clear();
        if (table == null || !table.isLoaded() || textFile == null || currentTrainerId < 0) {
            suppressTextUpdate = true;
            textArea.setText("");
            suppressTextUpdate = false;
            removeButton.setEnabled(false);
            addButton.setEnabled(false);
            return;
        }
        addButton.setEnabled(true);
        List<IndexedMessage> msgs = table.getByTrainer(currentTrainerId);
        for (IndexedMessage m : msgs) {
            listModel.addElement(m);
        }
        if (!listModel.isEmpty()) {
            messageList.setSelectedIndex(0);
        } else {
            suppressTextUpdate = true;
            textArea.setText("");
            suppressTextUpdate = false;
            removeButton.setEnabled(false);
        }
    }

    /**
     * Swaps between the normal editor UI and the "not supported in BW1"
     * notice. The label replaces the CENTER region.
     */
    private void showUnsupported(boolean show) {
        // The unsupported label replaces the CENTER text scroll. Simplest
        // approach: toggle the scroll pane's visibility by hiding its
        // parent region components.
        boolean enabled = !show;
        messageList.setEnabled(enabled);
        textArea.setEnabled(enabled);
        addButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        newTypeSpinner.setEnabled(enabled);
        textArea.setText(show ? "Trainer messages are only supported in BW2." : "");
    }

    /** Human-readable label for observed message-type IDs. */
    private static String messageTypeLabel(int type) {
        switch (type) {
            case 0: return "pre-battle";
            case 1: return "defeat";
            case 2: return "post-battle";
            case 3: return "event_3";
            case 5: return "event_5";
            case 6: return "event_6";
            case 7: return "event_7";
            case 8: return "event_8";
            case 9: return "event_9";
            case 10: return "event_10";
            case 13: return "event_13";
            case 14: return "event_14";
            case 15: return "rematch_pre";
            case 16: return "rematch_defeat";
            case 17: return "scripted_17";
            case 19: return "scripted_19";
            case 20: return "scripted_20";
            case 24: return "scripted_24";
            default: return "type_" + type;
        }
    }

    private static String snippet(String s) {
        if (s == null) return "";
        String oneLine = s.replace('\n', ' ').replace('\r', ' ').trim();
        return oneLine.length() > 60 ? oneLine.substring(0, 57) + "..." : oneLine;
    }
}
