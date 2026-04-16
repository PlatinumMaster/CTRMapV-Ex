package ctrmap.editor.gui.editors.gen5.debug;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for the disassembly view.
 * Shows Address, Hex Bytes, and Instruction columns.
 * Tracks which row corresponds to the current Program Counter.
 */
public class DisassemblyTableModel extends AbstractTableModel {

	private static final String[] COLUMNS = {"Address", "Bytes", "Instruction"};

	private final List<DisasmRow> rows = new ArrayList<>();
	private int pcRowIndex = -1;

	/**
	 * A single row in the disassembly view.
	 */
	public static class DisasmRow {
		public final int address;
		public final String hexBytes;
		public final String mnemonic;

		public DisasmRow(int address, String hexBytes, String mnemonic) {
			this.address = address;
			this.hexBytes = hexBytes;
			this.mnemonic = mnemonic;
		}
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNS[column];
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (row < 0 || row >= rows.size()) {
			return "";
		}
		DisasmRow r = rows.get(row);
		switch (column) {
			case 0:
				return String.format("0x%08X", r.address);
			case 1:
				return r.hexBytes;
			case 2:
				return r.mnemonic;
			default:
				return "";
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Replace the disassembly content with new rows.
	 * @param newRows The new disassembly rows
	 * @param pcRow   The row index that corresponds to the current PC (-1 if unknown)
	 */
	public void updateDisassembly(List<DisasmRow> newRows, int pcRow) {
		rows.clear();
		rows.addAll(newRows);
		this.pcRowIndex = pcRow;
		fireTableDataChanged();
	}

	/**
	 * Get the row index corresponding to the current PC.
	 */
	public int getPCRowIndex() {
		return pcRowIndex;
	}

	/**
	 * Clear all disassembly data.
	 */
	public void clear() {
		rows.clear();
		pcRowIndex = -1;
		fireTableDataChanged();
	}
}
