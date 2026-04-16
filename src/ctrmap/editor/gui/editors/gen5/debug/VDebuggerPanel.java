package ctrmap.editor.gui.editors.gen5.debug;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.util.debug.gdb.GDBClient;
import ctrmap.util.debug.gdb.GDBEventListener;
import ctrmap.util.debug.gdb.GDBRegisters;
import xstandard.arm.ARMDisassembler;
import xstandard.arm.ThumbDisassembler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Debugger tab for CTRMap. Connects to a GDB stub (e.g. melonDS), displays
 * ARM registers, disassembles instructions around the Program Counter, and
 * provides execution control (continue, step, halt, breakpoints).
 */
public class VDebuggerPanel extends javax.swing.JPanel implements AbstractTabbedEditor, GDBEventListener {

	private final CTRMap cm;
	private final GDBClient gdbClient;

	// Connection panel
	private JTextField hostField;
	private JTextField portField;
	private JComboBox<String> cpuSelector;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JLabel statusLabel;

	// Control panel
	private JButton btnContinue;
	private JButton btnStep;
	private JButton btnHalt;

	// Registers
	private JTable registerTable;
	private RegisterTableModel registerModel;

	// Disassembly
	private JTable disasmTable;
	private DisassemblyTableModel disasmModel;

	// Breakpoints
	private JTextField bpAddressField;
	private JButton btnAddBP;
	private JButton btnRemoveBP;
	private DefaultListModel<String> bpListModel;
	private JList<String> bpList;

	// Track breakpoint info for removal
	private final List<BreakpointInfo> breakpoints = new ArrayList<>();

	private static final Color PC_HIGHLIGHT = new Color(255, 255, 180);
	private static final Color REG_CHANGED = new Color(255, 200, 200);
	private static final int DISASM_CONTEXT_BYTES = 128; // bytes before and after PC

	public VDebuggerPanel(CTRMap cm) {
		this.cm = cm;
		this.gdbClient = new GDBClient();
		gdbClient.addListener(this);
		initComponents();
		setControlsEnabled(false);
	}

	@Override
	public String getTabName() {
		return "Debugger";
	}

	@Override
	public boolean isGameSupported(GameInfo game) {
		return game.isGenV();
	}

	@Override
	public void onProjectUnloaded(CTRMapProject proj) {
		if (gdbClient.isConnected()) {
			gdbClient.disconnect();
		}
	}

	// --- GDBEventListener ---

	@Override
	public void onStopped(int signal) {
		updateStatusLabel();
		setStoppedControls();
		// Refresh registers and disassembly on a background thread
		new Thread(() -> {
			try {
				refreshState();
			} catch (Exception e) {
				System.err.println("Error refreshing debugger state: " + e.getMessage());
			}
		}, "GDB-Refresh").start();
	}

	@Override
	public void onDisconnected() {
		updateStatusLabel();
		setControlsEnabled(false);
		registerModel.updateRegisters(null);
		disasmModel.clear();
	}

	@Override
	public void onResumed() {
		updateStatusLabel();
		setRunningControls();
	}

	// --- State refresh ---

	private void refreshState() {
		try {
			int[] regs = gdbClient.readRegisters();
			if (regs == null) {
				return;
			}

			int pc = regs[GDBRegisters.PC];
			int cpsr = regs[GDBRegisters.CPSR];
			boolean thumb = GDBRegisters.isThumbMode(cpsr);

			// Read memory around PC
			int instrSize = thumb ? 2 : 4;
			int startAddr = pc - DISASM_CONTEXT_BYTES;
			// Align to instruction boundary
			startAddr = startAddr & ~(instrSize - 1);
			if (startAddr < 0) {
				startAddr = 0;
			}
			int totalBytes = DISASM_CONTEXT_BYTES * 2 + instrSize;
			byte[] memory = gdbClient.readMemory(startAddr, totalBytes);

			if (memory == null) {
				// Fall back to just reading around PC
				startAddr = pc & ~(instrSize - 1);
				totalBytes = 64;
				memory = gdbClient.readMemory(startAddr, totalBytes);
			}

			// Disassemble
			List<DisassemblyTableModel.DisasmRow> rows = new ArrayList<>();
			int pcRow = -1;

			if (memory != null) {
				if (thumb) {
					pcRow = disassembleThumb(memory, startAddr, pc, rows);
				} else {
					pcRow = disassembleARM(memory, startAddr, pc, rows);
				}
			}

			// Update UI on EDT
			final int[] finalRegs = regs;
			final List<DisassemblyTableModel.DisasmRow> finalRows = rows;
			final int finalPcRow = pcRow;
			SwingUtilities.invokeLater(() -> {
				registerModel.updateRegisters(finalRegs);
				disasmModel.updateDisassembly(finalRows, finalPcRow);

				// Scroll to PC row
				if (finalPcRow >= 0) {
					disasmTable.scrollRectToVisible(disasmTable.getCellRect(finalPcRow, 0, true));
					disasmTable.setRowSelectionInterval(finalPcRow, finalPcRow);
				}
			});
		} catch (Exception e) {
			System.err.println("Error reading debugger state: " + e.getMessage());
		}
	}

	private int disassembleARM(byte[] memory, int startAddr, int pc, List<DisassemblyTableModel.DisasmRow> rows) {
		int pcRow = -1;
		for (int offset = 0; offset + 3 < memory.length; offset += 4) {
			int addr = startAddr + offset;
			int insn = (memory[offset] & 0xFF)
				| ((memory[offset + 1] & 0xFF) << 8)
				| ((memory[offset + 2] & 0xFF) << 16)
				| ((memory[offset + 3] & 0xFF) << 24);

			String hex = String.format("%08X", insn);
			String mnemonic = ARMDisassembler.disassemble(insn, addr);
			rows.add(new DisassemblyTableModel.DisasmRow(addr, hex, mnemonic));

			if (addr == pc) {
				pcRow = rows.size() - 1;
			}
		}
		return pcRow;
	}

	private int disassembleThumb(byte[] memory, int startAddr, int pc, List<DisassemblyTableModel.DisasmRow> rows) {
		int pcRow = -1;
		int offset = 0;
		while (offset + 1 < memory.length) {
			int addr = startAddr + offset;
			int hw = (memory[offset] & 0xFF) | ((memory[offset + 1] & 0xFF) << 8);

			if (ThumbDisassembler.isLongBranchPrefix(hw) && offset + 3 < memory.length) {
				// 32-bit Thumb instruction (BL/BLX)
				int hw2 = (memory[offset + 2] & 0xFF) | ((memory[offset + 3] & 0xFF) << 8);
				String hex = String.format("%04X %04X", hw, hw2);
				String mnemonic = ThumbDisassembler.disassembleLong(hw, hw2, addr);
				rows.add(new DisassemblyTableModel.DisasmRow(addr, hex, mnemonic));

				if (addr == pc) {
					pcRow = rows.size() - 1;
				}
				offset += 4;
			} else {
				String hex = String.format("%04X", hw);
				String mnemonic = ThumbDisassembler.disassemble(hw, addr);
				rows.add(new DisassemblyTableModel.DisasmRow(addr, hex, mnemonic));

				if (addr == pc) {
					pcRow = rows.size() - 1;
				}
				offset += 2;
			}
		}
		return pcRow;
	}

	// --- Action handlers ---

	private void onConnect() {
		String host = hostField.getText().trim();
		if (host.isEmpty()) {
			host = "localhost";
		}

		int port;
		try {
			port = Integer.parseInt(portField.getText().trim());
		} catch (NumberFormatException e) {
			port = 3333;
		}

		final String fHost = host;
		final int fPort = port;

		btnConnect.setEnabled(false);
		statusLabel.setText("Connecting...");

		new Thread(() -> {
			try {
				gdbClient.connect(fHost, fPort);
			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> {
					statusLabel.setText("Error: " + e.getMessage());
					btnConnect.setEnabled(true);
				});
			}
		}, "GDB-Connect").start();
	}

	private void onDisconnect() {
		gdbClient.disconnect();
	}

	private void onContinue() {
		new Thread(() -> {
			try {
				gdbClient.continueExecution();
			} catch (Exception e) {
				System.err.println("Continue error: " + e.getMessage());
			}
		}, "GDB-Continue").start();
	}

	private void onStep() {
		new Thread(() -> {
			try {
				gdbClient.step();
			} catch (Exception e) {
				System.err.println("Step error: " + e.getMessage());
			}
		}, "GDB-Step").start();
	}

	private void onHalt() {
		new Thread(() -> {
			try {
				gdbClient.halt();
			} catch (Exception e) {
				System.err.println("Halt error: " + e.getMessage());
			}
		}, "GDB-Halt").start();
	}

	private void onAddBreakpoint() {
		String addrText = bpAddressField.getText().trim();
		if (addrText.startsWith("0x") || addrText.startsWith("0X")) {
			addrText = addrText.substring(2);
		}

		try {
			final int addr = (int) Long.parseLong(addrText, 16);
			// Determine kind from CPSR T-bit
			int cpsr = registerModel.getValue(GDBRegisters.CPSR);
			final int kind = GDBRegisters.isThumbMode(cpsr) ? 2 : 4;
			String mode = kind == 2 ? "Thumb" : "ARM";

			new Thread(() -> {
				try {
					gdbClient.setBreakpoint(addr, kind);
					SwingUtilities.invokeLater(() -> {
						String entry = String.format("0x%08X (%s)", addr, mode);
						bpListModel.addElement(entry);
						breakpoints.add(new BreakpointInfo(addr, kind));
						bpAddressField.setText("");
					});
				} catch (Exception e) {
					System.err.println("Set breakpoint error: " + e.getMessage());
				}
			}, "GDB-SetBP").start();
		} catch (NumberFormatException e) {
			statusLabel.setText("Invalid address");
		}
	}

	private void onRemoveBreakpoint() {
		int selected = bpList.getSelectedIndex();
		if (selected < 0 || selected >= breakpoints.size()) {
			return;
		}

		final BreakpointInfo bp = breakpoints.get(selected);
		new Thread(() -> {
			try {
				gdbClient.removeBreakpoint(bp.address, bp.kind);
				SwingUtilities.invokeLater(() -> {
					int idx = breakpoints.indexOf(bp);
					if (idx >= 0) {
						breakpoints.remove(idx);
						bpListModel.remove(idx);
					}
				});
			} catch (Exception e) {
				System.err.println("Remove breakpoint error: " + e.getMessage());
			}
		}, "GDB-RemoveBP").start();
	}

	// --- UI state management ---

	private void setControlsEnabled(boolean connected) {
		btnConnect.setEnabled(!connected);
		btnDisconnect.setEnabled(connected);
		hostField.setEnabled(!connected);
		portField.setEnabled(!connected);
		cpuSelector.setEnabled(!connected);

		boolean stopped = connected && gdbClient.getState() == GDBClient.State.STOPPED;
		btnContinue.setEnabled(stopped);
		btnStep.setEnabled(stopped);
		btnHalt.setEnabled(connected && !stopped);
		btnAddBP.setEnabled(connected);
		btnRemoveBP.setEnabled(connected);
		bpAddressField.setEnabled(connected);
	}

	private void setStoppedControls() {
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);
		btnContinue.setEnabled(true);
		btnStep.setEnabled(true);
		btnHalt.setEnabled(false);
	}

	private void setRunningControls() {
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);
		btnContinue.setEnabled(false);
		btnStep.setEnabled(false);
		btnHalt.setEnabled(true);
	}

	private void updateStatusLabel() {
		GDBClient.State s = gdbClient.getState();
		switch (s) {
			case DISCONNECTED:
				statusLabel.setText("Disconnected");
				statusLabel.setForeground(Color.GRAY);
				break;
			case CONNECTING:
				statusLabel.setText("Connecting...");
				statusLabel.setForeground(Color.ORANGE);
				break;
			case STOPPED:
				statusLabel.setText("Stopped");
				statusLabel.setForeground(new Color(0, 128, 0));
				break;
			case RUNNING:
				statusLabel.setText("Running");
				statusLabel.setForeground(Color.BLUE);
				break;
		}
	}

	// --- Layout construction ---

	private void initComponents() {
		setLayout(new BorderLayout(4, 4));

		// === Top: Connection panel ===
		JPanel connPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
		connPanel.setBorder(BorderFactory.createTitledBorder("Connection"));

		connPanel.add(new JLabel("Host:"));
		hostField = new JTextField("localhost", 10);
		connPanel.add(hostField);

		connPanel.add(new JLabel("Port:"));
		portField = new JTextField("3333", 5);
		connPanel.add(portField);

		connPanel.add(new JLabel("CPU:"));
		cpuSelector = new JComboBox<>(new String[]{"ARM9 (3333)", "ARM7 (3334)"});
		cpuSelector.addActionListener(e -> {
			if (cpuSelector.getSelectedIndex() == 0) {
				portField.setText("3333");
			} else {
				portField.setText("3334");
			}
		});
		connPanel.add(cpuSelector);

		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(e -> onConnect());
		connPanel.add(btnConnect);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(e -> onDisconnect());
		btnDisconnect.setEnabled(false);
		connPanel.add(btnDisconnect);

		statusLabel = new JLabel("Disconnected");
		statusLabel.setForeground(Color.GRAY);
		statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
		connPanel.add(Box.createHorizontalStrut(10));
		connPanel.add(statusLabel);

		add(connPanel, BorderLayout.NORTH);

		// === Center: Registers (left) + Disassembly (center) ===
		JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		centerSplit.setResizeWeight(0.25);

		// --- Register table ---
		JPanel regPanel = new JPanel(new BorderLayout());
		regPanel.setBorder(BorderFactory.createTitledBorder("Registers"));

		registerModel = new RegisterTableModel();
		registerTable = new JTable(registerModel);
		registerTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		registerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		registerTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		registerTable.getColumnModel().getColumn(1).setPreferredWidth(100);

		// Highlight changed registers
		registerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (!isSelected && registerModel.hasChanged(row)) {
					c.setBackground(REG_CHANGED);
				} else if (!isSelected) {
					c.setBackground(Color.WHITE);
				}
				return c;
			}
		});

		regPanel.add(new JScrollPane(registerTable), BorderLayout.CENTER);
		centerSplit.setLeftComponent(regPanel);

		// --- Disassembly + controls ---
		JPanel disasmPanel = new JPanel(new BorderLayout(4, 4));
		disasmPanel.setBorder(BorderFactory.createTitledBorder("Disassembly"));

		disasmModel = new DisassemblyTableModel();
		disasmTable = new JTable(disasmModel);
		disasmTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		disasmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		disasmTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		disasmTable.getColumnModel().getColumn(1).setPreferredWidth(90);
		disasmTable.getColumnModel().getColumn(2).setPreferredWidth(300);

		// Highlight current PC row
		disasmTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (!isSelected && row == disasmModel.getPCRowIndex()) {
					c.setBackground(PC_HIGHLIGHT);
					c.setFont(c.getFont().deriveFont(Font.BOLD));
				} else if (!isSelected) {
					c.setBackground(Color.WHITE);
					c.setFont(c.getFont().deriveFont(Font.PLAIN));
				}
				return c;
			}
		});

		disasmPanel.add(new JScrollPane(disasmTable), BorderLayout.CENTER);

		// Control buttons
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
		btnContinue = new JButton("Continue");
		btnContinue.addActionListener(e -> onContinue());
		controlPanel.add(btnContinue);

		btnStep = new JButton("Step");
		btnStep.addActionListener(e -> onStep());
		controlPanel.add(btnStep);

		btnHalt = new JButton("Halt");
		btnHalt.addActionListener(e -> onHalt());
		controlPanel.add(btnHalt);

		disasmPanel.add(controlPanel, BorderLayout.SOUTH);

		centerSplit.setRightComponent(disasmPanel);
		add(centerSplit, BorderLayout.CENTER);

		// === Bottom: Breakpoints ===
		JPanel bpPanel = new JPanel(new BorderLayout(4, 2));
		bpPanel.setBorder(BorderFactory.createTitledBorder("Breakpoints"));
		bpPanel.setPreferredSize(new Dimension(0, 120));

		JPanel bpTopRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		bpTopRow.add(new JLabel("Address:"));
		bpAddressField = new JTextField(12);
		bpAddressField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		bpTopRow.add(bpAddressField);

		btnAddBP = new JButton("Add");
		btnAddBP.addActionListener(e -> onAddBreakpoint());
		bpTopRow.add(btnAddBP);

		btnRemoveBP = new JButton("Remove");
		btnRemoveBP.addActionListener(e -> onRemoveBreakpoint());
		bpTopRow.add(btnRemoveBP);

		bpPanel.add(bpTopRow, BorderLayout.NORTH);

		bpListModel = new DefaultListModel<>();
		bpList = new JList<>(bpListModel);
		bpList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		bpPanel.add(new JScrollPane(bpList), BorderLayout.CENTER);

		add(bpPanel, BorderLayout.SOUTH);
	}

	// --- Helper classes ---

	private static class BreakpointInfo {
		final int address;
		final int kind;

		BreakpointInfo(int address, int kind) {
			this.address = address;
			this.kind = kind;
		}
	}
}
