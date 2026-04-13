package ctrmap.editor.gui.editors.gen5.level.maps;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMD;
import ctrmap.formats.pokemon.containers.DefaultGamefreakContainer;
import ctrmap.formats.pokemon.containers.GFContainer;
import ctrmap.formats.pokemon.containers.util.ContainerFile;
import ctrmap.formats.pokemon.gen5.mapmatrix.VMapMatrix;
import ctrmap.formats.pokemon.gen5.zone.VZoneHeader;
import ctrmap.formats.pokemon.gen5.zone.VZoneTable;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.field.debug.VZoneDebugger;
import ctrmap.missioncontrol_ntr.field.structs.VMap;
import ctrmap.missioncontrol_ntr.field.structs.VZone;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import ctrmap.renderer.scene.model.ModelInstance;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VZoneMatrixEditor extends javax.swing.JPanel implements VZoneDebugger, AbstractTabbedEditor {

	private CTRMap Instance;
	private VMapMatrix matrix;
	private VZoneTable zoneTable;
	private int currentMatrixIndex = -1;

	// Toolbar
	private JSpinner matrixIdSpinner;
	private JButton btnLoad, btnSave;
	private JCheckBox chkUseZoneHeaders;
	private JSpinner widthSpinner, heightSpinner;
	private JButton btnUpdateSize;
	private JButton btnAddRow, btnAddCol, btnRemoveRow, btnRemoveCol;

	// Canvas
	private MatrixGridCanvas gridCanvas;

	// Side panel
	private JPanel cellPropertiesPanel;
	private JLabel lblSelectedCell;
	private JSpinner chunkIdEditor;
	private JSpinner zoneIdEditor;
	private JLabel lblZoneIdLabel;
	private JTextArea zoneInfoArea;

	// State
	private boolean updatingUI = false;

	public VZoneMatrixEditor(CTRMap Instance) {
		this.Instance = Instance;
		initZoneTable();
		buildUI();
		wireListeners();
	}

	@Override
	public String getTabName() {
		return "Zone Matrix Editor";
	}

	@Override
	public boolean isGameSupported(GameInfo game) {
		return game.isGenV();
	}

	NTRGameFS FS() {
		return Instance.getMissionControl(ctrmap.missioncontrol_ntr.VLaunchpad.class).fs;
	}

	private void initZoneTable() {
		try {
			zoneTable = new VZoneTable(FS().NARCGet(NARCRef.FIELD_ZONE_DATA, 0));
		} catch (Exception ex) {
			Logger.getLogger(VZoneMatrixEditor.class.getName()).log(Level.WARNING, "Could not load zone table", ex);
		}
	}

	// ==================== UI Construction ====================

	private void buildUI() {
		setLayout(new BorderLayout(4, 4));

		// Toolbar
		add(buildToolbar(), BorderLayout.NORTH);

		// Main content: canvas + side panel
		gridCanvas = new MatrixGridCanvas();

		cellPropertiesPanel = buildCellPropertiesPanel();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gridCanvas, cellPropertiesPanel);
		splitPane.setResizeWeight(1.0);
		splitPane.setDividerLocation(-1);
		add(splitPane, BorderLayout.CENTER);
	}

	private JPanel buildToolbar() {
		JPanel toolbar = new JPanel();
		toolbar.setBorder(BorderFactory.createTitledBorder("Matrix"));
		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));

		// Matrix ID selection
		matrixIdSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
		matrixIdSpinner.setPreferredSize(new Dimension(80, 25));

		btnLoad = new JButton("Load");
		btnSave = new JButton("Save");

		toolbar.add(new JLabel("Matrix:"));
		toolbar.add(matrixIdSpinner);
		toolbar.add(btnLoad);
		toolbar.add(btnSave);

		toolbar.add(createSeparator());

		// Zone headers toggle
		chkUseZoneHeaders = new JCheckBox("Use Zone Headers");
		toolbar.add(chkUseZoneHeaders);

		toolbar.add(createSeparator());

		// Dimensions
		widthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));
		widthSpinner.setPreferredSize(new Dimension(60, 25));
		heightSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 256, 1));
		heightSpinner.setPreferredSize(new Dimension(60, 25));
		btnUpdateSize = new JButton("Resize");

		toolbar.add(new JLabel("W:"));
		toolbar.add(widthSpinner);
		toolbar.add(new JLabel("H:"));
		toolbar.add(heightSpinner);
		toolbar.add(btnUpdateSize);

		toolbar.add(createSeparator());

		// Add/Remove buttons
		btnAddRow = new JButton("+Row");
		btnAddCol = new JButton("+Col");
		btnRemoveRow = new JButton("-Row");
		btnRemoveCol = new JButton("-Col");

		toolbar.add(btnAddRow);
		toolbar.add(btnAddCol);
		toolbar.add(btnRemoveRow);
		toolbar.add(btnRemoveCol);

		return toolbar;
	}

	private JSeparator createSeparator() {
		JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
		sep.setPreferredSize(new Dimension(2, 25));
		return sep;
	}

	private JPanel buildCellPropertiesPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Cell Properties"));
		panel.setPreferredSize(new Dimension(220, 0));
		panel.setMinimumSize(new Dimension(180, 0));

		// Selected cell label
		lblSelectedCell = new JLabel("No cell selected");
		lblSelectedCell.setAlignmentX(0.0f);
		panel.add(lblSelectedCell);
		panel.add(Box.createVerticalStrut(8));

		// Chunk ID editor
		JLabel lblChunkId = new JLabel("Chunk ID:");
		lblChunkId.setAlignmentX(0.0f);
		panel.add(lblChunkId);
		chunkIdEditor = new JSpinner(new SpinnerNumberModel(-1, -1, 99999, 1));
		chunkIdEditor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		chunkIdEditor.setAlignmentX(0.0f);
		chunkIdEditor.setEnabled(false);
		panel.add(chunkIdEditor);
		panel.add(Box.createVerticalStrut(8));

		// Zone ID editor
		lblZoneIdLabel = new JLabel("Zone ID:");
		lblZoneIdLabel.setAlignmentX(0.0f);
		panel.add(lblZoneIdLabel);
		zoneIdEditor = new JSpinner(new SpinnerNumberModel(-1, -1, 99999, 1));
		zoneIdEditor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		zoneIdEditor.setAlignmentX(0.0f);
		zoneIdEditor.setEnabled(false);
		panel.add(zoneIdEditor);
		panel.add(Box.createVerticalStrut(12));

		// Zone header info
		JLabel lblZoneInfo = new JLabel("Zone Header Info:");
		lblZoneInfo.setAlignmentX(0.0f);
		panel.add(lblZoneInfo);
		zoneInfoArea = new JTextArea(10, 18);
		zoneInfoArea.setEditable(false);
		zoneInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
		zoneInfoArea.setAlignmentX(0.0f);
		zoneInfoArea.setLineWrap(true);
		panel.add(zoneInfoArea);

		panel.add(Box.createVerticalGlue());

		return panel;
	}

	// ==================== Listener Wiring ====================

	private void wireListeners() {
		// Load / Save
		btnLoad.addActionListener(e -> {
			try {
				loadMatrix((Integer) matrixIdSpinner.getValue());
			} catch (Exception ex) {
				Logger.getLogger(VZoneMatrixEditor.class.getName()).log(Level.SEVERE, null, ex);
			}
		});

		btnSave.addActionListener(e -> {
			saveMatrix((Integer) matrixIdSpinner.getValue());
		});

		// Zone headers toggle
		chkUseZoneHeaders.addActionListener(e -> {
			if (matrix != null) {
				matrix.hasZones = chkUseZoneHeaders.isSelected();
				if (matrix.hasZones && matrix.zoneIds == null) {
					matrix.zoneIds = new xstandard.util.ResizeableMatrix<>(matrix.getWidth(), matrix.getHeight(), -1);
				}
				updateZoneUIVisibility();
				gridCanvas.repaint();
			}
		});

		// Resize
		btnUpdateSize.addActionListener(e -> {
			if (matrix != null) {
				int newW = (Integer) widthSpinner.getValue();
				int newH = (Integer) heightSpinner.getValue();
				matrix.chunkIds.resizeMatrix(newW, newH);
				if (matrix.hasZones && matrix.zoneIds != null) {
					matrix.zoneIds.resizeMatrix(newW, newH);
				}
				gridCanvas.clearSelection();
				gridCanvas.repaint();
				updateCellProperties(-1, -1, -1, -1);
			}
		});

		// Add/Remove row/column
		btnAddRow.addActionListener(e -> {
			if (matrix != null) {
				matrix.chunkIds.addRow();
				if (matrix.hasZones && matrix.zoneIds != null) {
					matrix.zoneIds.addRow();
				}
				refreshDimensionSpinners();
				gridCanvas.repaint();
			}
		});

		btnAddCol.addActionListener(e -> {
			if (matrix != null) {
				matrix.chunkIds.addColumn();
				if (matrix.hasZones && matrix.zoneIds != null) {
					matrix.zoneIds.addColumn();
				}
				refreshDimensionSpinners();
				gridCanvas.repaint();
			}
		});

		btnRemoveRow.addActionListener(e -> {
			if (matrix != null && matrix.getHeight() > 1) {
				if (confirmRemove("row")) {
					matrix.chunkIds.removeRow();
					if (matrix.hasZones && matrix.zoneIds != null) {
						matrix.zoneIds.removeRow();
					}
					clampSelection();
					refreshDimensionSpinners();
					gridCanvas.repaint();
				}
			}
		});

		btnRemoveCol.addActionListener(e -> {
			if (matrix != null && matrix.getWidth() > 1) {
				if (confirmRemove("column")) {
					matrix.chunkIds.removeColumn();
					if (matrix.hasZones && matrix.zoneIds != null) {
						matrix.zoneIds.removeColumn();
					}
					clampSelection();
					refreshDimensionSpinners();
					gridCanvas.repaint();
				}
			}
		});

		// Canvas listener
		gridCanvas.setListener(new MatrixGridCanvas.MatrixGridCanvasListener() {
			@Override
			public void onCellSelected(int gridX, int gridY, int chunkId, int zoneId) {
				updateCellProperties(gridX, gridY, chunkId, zoneId);
			}

			@Override
			public void onCellMoved(int fromX, int fromY, int toX, int toY) {
				swapCells(fromX, fromY, toX, toY);
				gridCanvas.repaint();
				// Update side panel with new position
				java.awt.Point sel = gridCanvas.getSelectedCell();
				if (sel.x >= 0 && sel.y >= 0) {
					int cId = matrix.chunkIds.get(toX, toY);
					int zId = matrix.hasZones && matrix.zoneIds != null ? matrix.zoneIds.get(toX, toY) : -1;
					updateCellProperties(toX, toY, cId, zId);
				}
			}

			@Override
			public void onCellRightClicked(int gridX, int gridY, int screenX, int screenY) {
				showContextMenu(gridX, gridY, screenX, screenY);
			}
		});

		// Chunk ID spinner change
		chunkIdEditor.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (updatingUI || matrix == null) return;
				java.awt.Point sel = gridCanvas.getSelectedCell();
				if (sel.x >= 0 && sel.y >= 0) {
					int newChunkId = (Integer) chunkIdEditor.getValue();
					matrix.chunkIds.set(sel.x, sel.y, newChunkId);
					gridCanvas.repaint();
					updateLivePreview(sel.x, sel.y, newChunkId);
				}
			}
		});

		// Zone ID spinner change
		zoneIdEditor.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (updatingUI || matrix == null || !matrix.hasZones || matrix.zoneIds == null) return;
				java.awt.Point sel = gridCanvas.getSelectedCell();
				if (sel.x >= 0 && sel.y >= 0) {
					int newZoneId = (Integer) zoneIdEditor.getValue();
					matrix.zoneIds.set(sel.x, sel.y, newZoneId);
					updateZoneInfo(newZoneId);
					gridCanvas.repaint();
				}
			}
		});
	}

	// ==================== Matrix Load/Save ====================

	@Override
	public void loadZone(VZone z) {
		if (z != null) {
			try {
				matrixIdSpinner.setValue(z.header.matrixID);
				loadMatrix(z.header.matrixID);
			} catch (Exception ex) {
				Logger.getLogger(VZoneMatrixEditor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void loadMatrix(int index) {
		int max = FS().NARCGetDataMax(NARCRef.FIELD_MAP_MATRIX);
		if (index >= 0 && index < max) {
			currentMatrixIndex = index;
			matrix = new VMapMatrix(FS().NARCGet(NARCRef.FIELD_MAP_MATRIX, index));

			updatingUI = true;
			widthSpinner.setValue(matrix.getWidth());
			heightSpinner.setValue(matrix.getHeight());
			chkUseZoneHeaders.setSelected(matrix.hasZones);
			updatingUI = false;

			updateZoneUIVisibility();
			gridCanvas.setMatrix(matrix);
			gridCanvas.resetView();
			updateCellProperties(-1, -1, -1, -1);
		}
	}

	private void saveMatrix(int index) {
		if (matrix != null) {
			int max = FS().NARCGetDataMax(NARCRef.FIELD_MAP_MATRIX);
			if (index >= 0 && index < max) {
				matrix.write(FS().NARCGet(NARCRef.FIELD_MAP_MATRIX, index));
			}
		}
	}

	// ==================== Cell Operations ====================

	private void swapCells(int fromX, int fromY, int toX, int toY) {
		int srcChunk = matrix.chunkIds.get(fromX, fromY);
		int dstChunk = matrix.chunkIds.get(toX, toY);
		matrix.chunkIds.set(fromX, fromY, dstChunk);
		matrix.chunkIds.set(toX, toY, srcChunk);

		if (matrix.hasZones && matrix.zoneIds != null) {
			int srcZone = matrix.zoneIds.get(fromX, fromY);
			int dstZone = matrix.zoneIds.get(toX, toY);
			matrix.zoneIds.set(fromX, fromY, dstZone);
			matrix.zoneIds.set(toX, toY, srcZone);
		}

		updateLivePreview(fromX, fromY, dstChunk);
		updateLivePreview(toX, toY, srcChunk);
	}

	private void insertRowAt(int y) {
		if (matrix == null) return;
		int width = matrix.getWidth();
		// Insert into chunkIds: for each column, insert -1 at row y
		for (int x = 0; x < width; x++) {
			matrix.chunkIds.list.get(x).add(y, -1);
		}
		if (matrix.hasZones && matrix.zoneIds != null) {
			for (int x = 0; x < width; x++) {
				matrix.zoneIds.list.get(x).add(y, -1);
			}
		}
		refreshDimensionSpinners();
		gridCanvas.repaint();
	}

	private void insertColumnAt(int x) {
		if (matrix == null) return;
		int height = matrix.getHeight();
		// Insert a new column at position x
		ArrayList<Integer> newChunkCol = new ArrayList<>();
		for (int y = 0; y < height; y++) {
			newChunkCol.add(-1);
		}
		matrix.chunkIds.list.add(x, newChunkCol);

		if (matrix.hasZones && matrix.zoneIds != null) {
			ArrayList<Integer> newZoneCol = new ArrayList<>();
			for (int y = 0; y < height; y++) {
				newZoneCol.add(-1);
			}
			matrix.zoneIds.list.add(x, newZoneCol);
		}
		refreshDimensionSpinners();
		gridCanvas.repaint();
	}

	private void deleteRowAt(int y) {
		if (matrix == null || matrix.getHeight() <= 1) return;
		int width = matrix.getWidth();
		for (int x = 0; x < width; x++) {
			matrix.chunkIds.list.get(x).remove(y);
		}
		if (matrix.hasZones && matrix.zoneIds != null) {
			for (int x = 0; x < width; x++) {
				matrix.zoneIds.list.get(x).remove(y);
			}
		}
		clampSelection();
		refreshDimensionSpinners();
		gridCanvas.repaint();
	}

	private void deleteColumnAt(int x) {
		if (matrix == null || matrix.getWidth() <= 1) return;
		matrix.chunkIds.list.remove(x);
		if (matrix.hasZones && matrix.zoneIds != null) {
			matrix.zoneIds.list.remove(x);
		}
		clampSelection();
		refreshDimensionSpinners();
		gridCanvas.repaint();
	}

	// ==================== Context Menu ====================

	private void showContextMenu(int gridX, int gridY, int screenX, int screenY) {
		if (matrix == null) return;

		JPopupMenu menu = new JPopupMenu();
		int chunkId = matrix.chunkIds.get(gridX, gridY);

		// Cell value editing
		JMenuItem miSetChunk = new JMenuItem("Set Chunk ID...");
		miSetChunk.addActionListener(e -> {
			String input = JOptionPane.showInputDialog(this, "Enter Chunk ID:", chunkId);
			if (input != null) {
				try {
					int newId = Integer.parseInt(input.trim());
					matrix.chunkIds.set(gridX, gridY, newId);
					updateCellProperties(gridX, gridY, newId,
							matrix.hasZones && matrix.zoneIds != null ? matrix.zoneIds.get(gridX, gridY) : -1);
					gridCanvas.repaint();
					updateLivePreview(gridX, gridY, newId);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		menu.add(miSetChunk);

		if (matrix.hasZones && matrix.zoneIds != null) {
			int zoneId = matrix.zoneIds.get(gridX, gridY);
			JMenuItem miSetZone = new JMenuItem("Set Zone ID...");
			miSetZone.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(this, "Enter Zone ID:", zoneId);
				if (input != null) {
					try {
						int newZone = Integer.parseInt(input.trim());
						matrix.zoneIds.set(gridX, gridY, newZone);
						updateCellProperties(gridX, gridY, matrix.chunkIds.get(gridX, gridY), newZone);
						gridCanvas.repaint();
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(this, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			menu.add(miSetZone);
		}

		JMenuItem miClear = new JMenuItem("Clear Cell");
		miClear.addActionListener(e -> {
			matrix.chunkIds.set(gridX, gridY, -1);
			if (matrix.hasZones && matrix.zoneIds != null) {
				matrix.zoneIds.set(gridX, gridY, -1);
			}
			updateCellProperties(gridX, gridY, -1, -1);
			gridCanvas.repaint();
			updateLivePreview(gridX, gridY, -1);
		});
		menu.add(miClear);

		menu.addSeparator();

		// Row/Column insertion
		JMenuItem miInsertRowAbove = new JMenuItem("Insert Row Above");
		miInsertRowAbove.addActionListener(e -> insertRowAt(gridY));
		menu.add(miInsertRowAbove);

		JMenuItem miInsertRowBelow = new JMenuItem("Insert Row Below");
		miInsertRowBelow.addActionListener(e -> insertRowAt(gridY + 1));
		menu.add(miInsertRowBelow);

		JMenuItem miDeleteRow = new JMenuItem("Delete Row " + gridY);
		miDeleteRow.setEnabled(matrix.getHeight() > 1);
		miDeleteRow.addActionListener(e -> {
			if (confirmDeleteAt("row", gridY)) {
				deleteRowAt(gridY);
			}
		});
		menu.add(miDeleteRow);

		menu.addSeparator();

		JMenuItem miInsertColLeft = new JMenuItem("Insert Column Left");
		miInsertColLeft.addActionListener(e -> insertColumnAt(gridX));
		menu.add(miInsertColLeft);

		JMenuItem miInsertColRight = new JMenuItem("Insert Column Right");
		miInsertColRight.addActionListener(e -> insertColumnAt(gridX + 1));
		menu.add(miInsertColRight);

		JMenuItem miDeleteCol = new JMenuItem("Delete Column " + gridX);
		miDeleteCol.setEnabled(matrix.getWidth() > 1);
		miDeleteCol.addActionListener(e -> {
			if (confirmDeleteAt("column", gridX)) {
				deleteColumnAt(gridX);
			}
		});
		menu.add(miDeleteCol);

		// Import/Export (only for non-empty cells)
		if (chunkId >= 0) {
			menu.addSeparator();
			addImportExportMenuItems(menu, chunkId);
		}

		menu.show(gridCanvas, screenX - gridCanvas.getLocationOnScreen().x, screenY - gridCanvas.getLocationOnScreen().y);
	}

	private void addImportExportMenuItems(JPopupMenu menu, int chunkId) {
		int maxChunks = FS().NARCGetDataMax(NARCRef.FIELD_MAP_CHUNKS);
		if (chunkId < 0 || chunkId >= maxChunks) return;

		GFContainer container;
		try {
			container = new DefaultGamefreakContainer(FS().NARCGet(NARCRef.FIELD_MAP_CHUNKS, chunkId));
		} catch (Exception ex) {
			return;
		}

		String sig = container.getSignature();
		// Determine file indices based on container signature
		int modelIdx = 0;
		int permIdx = -1, perm2Idx = -1, buildIdx = -1;

		switch (sig) {
			case "NG":
				buildIdx = 1;
				break;
			case "WB":
			case "RD":
				permIdx = 1;
				buildIdx = 2;
				break;
			case "GC":
				permIdx = 1;
				perm2Idx = 2;
				buildIdx = 3;
				break;
		}

		final int fModelIdx = modelIdx;
		final int fPermIdx = permIdx;
		final int fPerm2Idx = perm2Idx;
		final int fBuildIdx = buildIdx;
		final GFContainer fContainer = container;

		JMenuItem miImportModel = new JMenuItem("Import Model...");
		miImportModel.addActionListener(e -> importContainerFile(fContainer, fModelIdx));
		menu.add(miImportModel);

		JMenuItem miExportModel = new JMenuItem("Export Model...");
		miExportModel.addActionListener(e -> exportContainerFile(fContainer, fModelIdx));
		menu.add(miExportModel);

		if (permIdx >= 0) {
			JMenuItem miImportPerm = new JMenuItem("Import Permissions...");
			miImportPerm.addActionListener(e -> importContainerFile(fContainer, fPermIdx));
			menu.add(miImportPerm);

			JMenuItem miExportPerm = new JMenuItem("Export Permissions...");
			miExportPerm.addActionListener(e -> exportContainerFile(fContainer, fPermIdx));
			menu.add(miExportPerm);
		}

		if (perm2Idx >= 0) {
			JMenuItem miImportPerm2 = new JMenuItem("Import Permissions 2...");
			miImportPerm2.addActionListener(e -> importContainerFile(fContainer, fPerm2Idx));
			menu.add(miImportPerm2);

			JMenuItem miExportPerm2 = new JMenuItem("Export Permissions 2...");
			miExportPerm2.addActionListener(e -> exportContainerFile(fContainer, fPerm2Idx));
			menu.add(miExportPerm2);
		}

		if (buildIdx >= 0) {
			JMenuItem miImportBuild = new JMenuItem("Import Buildings...");
			miImportBuild.addActionListener(e -> importContainerFile(fContainer, fBuildIdx));
			menu.add(miImportBuild);

			JMenuItem miExportBuild = new JMenuItem("Export Buildings...");
			miExportBuild.addActionListener(e -> exportContainerFile(fContainer, fBuildIdx));
			menu.add(miExportBuild);
		}
	}

	private void importContainerFile(GFContainer container, int fileIndex) {
		if (container == null || fileIndex < 0) return;
		JFileChooser fchooser = new JFileChooser();
		if (fchooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fchooser.getSelectedFile();
			if (file != null) {
				try {
					byte[] data = Files.readAllBytes(file.toPath());
					container.storeFile(fileIndex, data);
				} catch (IOException ex) {
					Logger.getLogger(VZoneMatrixEditor.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private void exportContainerFile(GFContainer container, int fileIndex) {
		if (container == null || fileIndex < 0) return;
		JFileChooser fchooser = new JFileChooser();
		if (fchooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fchooser.getSelectedFile();
			if (file != null) {
				try {
					byte[] data = container.getFile(fileIndex);
					if (data != null) {
						Files.write(file.toPath(), data);
					}
				} catch (IOException ex) {
					Logger.getLogger(VZoneMatrixEditor.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	// ==================== UI Updates ====================

	private void updateCellProperties(int gridX, int gridY, int chunkId, int zoneId) {
		updatingUI = true;
		if (gridX < 0 || gridY < 0) {
			lblSelectedCell.setText("No cell selected");
			chunkIdEditor.setValue(-1);
			chunkIdEditor.setEnabled(false);
			zoneIdEditor.setValue(-1);
			zoneIdEditor.setEnabled(false);
			zoneInfoArea.setText("");
		} else {
			lblSelectedCell.setText("Cell (" + gridX + ", " + gridY + ")");
			chunkIdEditor.setValue(chunkId);
			chunkIdEditor.setEnabled(true);

			if (matrix != null && matrix.hasZones && matrix.zoneIds != null) {
				zoneIdEditor.setValue(zoneId);
				zoneIdEditor.setEnabled(true);
				updateZoneInfo(zoneId);
			} else {
				zoneIdEditor.setValue(-1);
				zoneIdEditor.setEnabled(false);
				zoneInfoArea.setText("");
			}
		}
		updatingUI = false;
	}

	private void updateZoneInfo(int zoneId) {
		if (zoneTable == null || zoneId < 0 || zoneId >= zoneTable.getZoneCount()) {
			zoneInfoArea.setText(zoneId >= 0 ? "Zone " + zoneId + " (invalid)" : "");
			return;
		}
		VZoneHeader hdr = zoneTable.getHeader(zoneId);
		StringBuilder sb = new StringBuilder();
		sb.append("Zone ").append(zoneId).append("\n");
		sb.append("Matrix ID: ").append(hdr.matrixID).append("\n");
		sb.append("Area ID:   ").append(hdr.areaID).append("\n");
		sb.append("Scripts:   ").append(hdr.scriptsID).append("\n");
		sb.append("Text:      ").append(hdr.textFileID).append("\n");
		sb.append("Entities:  ").append(hdr.entitiesID).append("\n");
		sb.append("Enc ID:    ").append(hdr.encID).append("\n");
		sb.append("Weather:   ").append(hdr.weather).append("\n");
		sb.append("BGM Spr:   ").append(hdr.BGMSpring).append("\n");
		zoneInfoArea.setText(sb.toString());
	}

	private void updateZoneUIVisibility() {
		boolean showZones = matrix != null && matrix.hasZones;
		lblZoneIdLabel.setVisible(showZones);
		zoneIdEditor.setVisible(showZones);
	}

	private void refreshDimensionSpinners() {
		if (matrix != null) {
			updatingUI = true;
			widthSpinner.setValue(matrix.getWidth());
			heightSpinner.setValue(matrix.getHeight());
			updatingUI = false;
		}
	}

	private void clampSelection() {
		java.awt.Point sel = gridCanvas.getSelectedCell();
		if (matrix != null && sel.x >= 0 && sel.y >= 0) {
			if (sel.x >= matrix.getWidth() || sel.y >= matrix.getHeight()) {
				gridCanvas.clearSelection();
				updateCellProperties(-1, -1, -1, -1);
			}
		}
	}

	// ==================== Live Preview ====================

	private void updateLivePreview(int gridX, int gridY, int newChunkId) {
		try {
			VLaunchpad vmc = Instance.getMissionControl(VLaunchpad.class);
			if (vmc == null || vmc.field == null || vmc.field.map == null) return;
			VMap map = vmc.field.map;

			// Only update if editing the currently loaded matrix
			if (map.matrixId != currentMatrixIndex) return;
			if (map.matrix == null) return;
			if (!map.models.containsPoint(gridX, gridY)) return;

			// Sync the chunk ID into VMap's own matrix
			map.matrix.chunkIds.set(gridX, gridY, newChunkId);

			// Remove old model from the terrain scene
			ModelInstance oldModel = map.models.get(gridX, gridY);
			if (oldModel != null && map.terrainScene != null) {
				map.terrainScene.removeChild(oldModel);
			}

			if (newChunkId >= 0 && newChunkId < FS().NARCGetDataMax(NARCRef.FIELD_MAP_CHUNKS)) {
				int resolvedId = vmc.field.resolveChunkID(map.matrixId, newChunkId);
				GFContainer source = new DefaultGamefreakContainer(FS().NARCGet(NARCRef.FIELD_MAP_CHUNKS, resolvedId));
				map.chunks.set(gridX, gridY, source);

				ModelInstance newModel = new NSBMD(source.getFile(VMap.MAPPACK_TERRAINMDL_IDX)).toGeneric().createInstance();
				newModel.p.x = gridX * map.chunkSpan + map.chunkSpan / 2f;
				newModel.p.z = gridY * map.chunkSpan + map.chunkSpan / 2f;

				map.models.set(gridX, gridY, newModel);
				if (map.terrainScene != null) {
					map.terrainScene.addModel(newModel);
				}
			} else {
				// Cleared cell
				map.chunks.set(gridX, gridY, null);
				map.models.set(gridX, gridY, null);
			}
		} catch (Exception ex) {
			Logger.getLogger(VZoneMatrixEditor.class.getName()).log(Level.WARNING, "Live preview update failed", ex);
		}
	}

	// ==================== Helpers ====================

	private boolean confirmRemove(String type) {
		return JOptionPane.showConfirmDialog(this,
				"Remove the last " + type + "?",
				"Confirm Remove",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	private boolean confirmDeleteAt(String type, int index) {
		return JOptionPane.showConfirmDialog(this,
				"Delete " + type + " " + index + "? This cannot be undone.",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
}
