package ctrmap.editor.gui.editors.gen5.debug;

import ctrmap.util.debug.gdb.GDBRegisters;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for the ARM register display.
 * Shows 17 rows (R0-R15 + CPSR) with Name and Value columns.
 * Tracks previous values to highlight registers that changed since the last stop.
 */
public class RegisterTableModel extends AbstractTableModel {

	private static final String[] COLUMNS = {"Register", "Value"};

	private int[] values = new int[GDBRegisters.NUM_REGISTERS];
	private int[] previousValues = new int[GDBRegisters.NUM_REGISTERS];

	@Override
	public int getRowCount() {
		return GDBRegisters.NUM_REGISTERS;
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
		if (column == 0) {
			return GDBRegisters.getName(row);
		} else {
			return String.format("0x%08X", values[row]);
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	/**
	 * Update all register values. Stores the old values for change detection.
	 */
	public void updateRegisters(int[] newValues) {
		System.arraycopy(values, 0, previousValues, 0, values.length);
		if (newValues != null) {
			int len = Math.min(newValues.length, values.length);
			System.arraycopy(newValues, 0, values, 0, len);
		}
		fireTableDataChanged();
	}

	/**
	 * Check if a register value changed since the last update.
	 */
	public boolean hasChanged(int row) {
		return row >= 0 && row < values.length && values[row] != previousValues[row];
	}

	/**
	 * Get the raw value of a register.
	 */
	public int getValue(int row) {
		return (row >= 0 && row < values.length) ? values[row] : 0;
	}
}
