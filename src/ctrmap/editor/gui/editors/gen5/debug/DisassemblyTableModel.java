package ctrmap.editor.gui.editors.gen5.debug;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for the disassembly view.
 * Shows Address, Bytes, and Instruction columns.
 * Supports both instruction rows and label/symbol rows.
 * Tracks which row corresponds to the current Program Counter.
 */
public class DisassemblyTableModel extends AbstractTableModel {

	private static final String[] COLUMNS = {"Address", "Bytes", "Instruction"};

	private final List<DisasmRow> rows = new ArrayList<>();
	private int pcRowIndex = -1;

	/**
	 * A single row in the disassembly view.
	 * Can be either an instruction or a symbol label.
	 */
	public static class DisasmRow {
		public final int address;
		public final String hexBytes;
		public final String mnemonic;
		public final boolean isLabel;

		private DisasmRow(int address, String hexBytes, String mnemonic, boolean isLabel) {
			this.address = address;
			this.hexBytes = hexBytes;
			this.mnemonic = mnemonic;
			this.isLabel = isLabel;
		}

		/** Create an instruction row. */
		public static DisasmRow instruction(int address, String hexBytes, String mnemonic) {
			return new DisasmRow(address, hexBytes, mnemonic, false);
		}

		/** Create a symbol label row. */
		public static DisasmRow label(int address, String name) {
			return new DisasmRow(address, "", name + ":", true);
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
				return r.isLabel ? "" : String.format("0x%08X", r.address);
			case 1:
				return r.hexBytes;
			case 2:
				return r.isLabel ? r.mnemonic : "  " + r.mnemonic;
			default:
				return "";
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Check if a row is a label (symbol) row rather than an instruction.
	 */
	public boolean isLabelRow(int row) {
		return row >= 0 && row < rows.size() && rows.get(row).isLabel;
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
