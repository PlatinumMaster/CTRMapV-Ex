package ctrmap.editor.gui.editors.gen5.level.maps;

import ctrmap.formats.pokemon.gen5.mapmatrix.VMapMatrix;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

public class MatrixGridCanvas extends JPanel {

	public interface MatrixGridCanvasListener {
		void onCellSelected(int gridX, int gridY, int chunkId, int zoneId);
		void onCellMoved(int fromX, int fromY, int toX, int toY);
		void onCellRightClicked(int gridX, int gridY, int screenX, int screenY);
	}

	private static final int CELL_SIZE = 64;
	private static final int CELL_GAP = 2;
	private static final double ZOOM_MIN = 0.25;
	private static final double ZOOM_MAX = 8.0;
	private static final double ZOOM_STEP_IN = 1.25;
	private static final double ZOOM_STEP_OUT = 0.8;

	private static final Color BG_COLOR = new Color(48, 48, 48);
	private static final Color EMPTY_CELL_COLOR = new Color(80, 80, 80);
	private static final Color NO_ZONE_CELL_COLOR = new Color(90, 130, 170);
	private static final Color SELECTION_COLOR = new Color(255, 200, 50);
	private static final Color DROP_TARGET_COLOR = new Color(100, 255, 100, 120);
	private static final Color DRAG_GHOST_COLOR = new Color(255, 255, 255, 80);
	private static final Color GRID_LINE_COLOR = new Color(30, 30, 30);
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final Color TEXT_SHADOW_COLOR = new Color(0, 0, 0, 160);

	private static final Color[] ZONE_COLORS = {
		new Color(70, 130, 180),   // steel blue
		new Color(178, 102, 102),  // muted red
		new Color(102, 178, 102),  // muted green
		new Color(178, 140, 80),   // muted gold
		new Color(140, 100, 178),  // muted purple
		new Color(80, 170, 170),   // teal
		new Color(178, 120, 150),  // muted pink
		new Color(120, 150, 80),   // olive
		new Color(100, 130, 160),  // slate
		new Color(170, 130, 100),  // tan
		new Color(130, 100, 160),  // lavender
		new Color(80, 160, 130),   // sea green
		new Color(160, 140, 90),   // khaki
		new Color(150, 100, 100),  // dusty rose
		new Color(100, 140, 120),  // sage
		new Color(140, 120, 160),  // wisteria
	};

	private VMapMatrix matrix;
	private MatrixGridCanvasListener listener;

	// View state
	private double zoom = 1.0;
	private int panX = 0, panY = 0;
	private boolean isPanning = false;
	private int lastPanX, lastPanY;

	// Selection state
	private int selectedX = -1, selectedY = -1;

	// Drag state
	private boolean isDragging = false;
	private int dragSourceX = -1, dragSourceY = -1;
	private int dragScreenX, dragScreenY;
	private int dropTargetX = -1, dropTargetY = -1;

	private Font chunkIdFont = new Font("SansSerif", Font.BOLD, 14);
	private Font zoneIdFont = new Font("SansSerif", Font.PLAIN, 10);

	public MatrixGridCanvas() {
		setBackground(BG_COLOR);
		setPreferredSize(new Dimension(800, 600));
		setFocusable(true);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocusInWindow();
				if (matrix == null) return;

				if (e.getButton() == MouseEvent.BUTTON2) {
					isPanning = true;
					lastPanX = e.getX();
					lastPanY = e.getY();
				} else if (e.getButton() == MouseEvent.BUTTON1) {
					Point grid = screenToGrid(e.getPoint());
					if (isValidCell(grid.x, grid.y)) {
						selectedX = grid.x;
						selectedY = grid.y;
						int chunkId = matrix.chunkIds.get(grid.x, grid.y);
						int zoneId = matrix.hasZones && matrix.zoneIds != null ? matrix.zoneIds.get(grid.x, grid.y) : -1;
						if (listener != null) {
							listener.onCellSelected(grid.x, grid.y, chunkId, zoneId);
						}
						if (chunkId != -1) {
							isDragging = true;
							dragSourceX = grid.x;
							dragSourceY = grid.y;
							dragScreenX = e.getX();
							dragScreenY = e.getY();
						}
					} else {
						selectedX = -1;
						selectedY = -1;
						if (listener != null) {
							listener.onCellSelected(-1, -1, -1, -1);
						}
					}
					repaint();
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					Point grid = screenToGrid(e.getPoint());
					if (isValidCell(grid.x, grid.y)) {
						selectedX = grid.x;
						selectedY = grid.y;
						int chunkId = matrix.chunkIds.get(grid.x, grid.y);
						int zoneId = matrix.hasZones && matrix.zoneIds != null ? matrix.zoneIds.get(grid.x, grid.y) : -1;
						if (listener != null) {
							listener.onCellSelected(grid.x, grid.y, chunkId, zoneId);
							listener.onCellRightClicked(grid.x, grid.y, e.getXOnScreen(), e.getYOnScreen());
						}
					}
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON2) {
					isPanning = false;
				} else if (e.getButton() == MouseEvent.BUTTON1 && isDragging) {
					Point grid = screenToGrid(e.getPoint());
					if (isValidCell(grid.x, grid.y) && (grid.x != dragSourceX || grid.y != dragSourceY)) {
						if (listener != null) {
							listener.onCellMoved(dragSourceX, dragSourceY, grid.x, grid.y);
						}
					}
					isDragging = false;
					dragSourceX = -1;
					dragSourceY = -1;
					dropTargetX = -1;
					dropTargetY = -1;
					repaint();
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (matrix == null) return;

				if (isPanning) {
					panX += e.getX() - lastPanX;
					panY += e.getY() - lastPanY;
					lastPanX = e.getX();
					lastPanY = e.getY();
					repaint();
				} else if (isDragging) {
					dragScreenX = e.getX();
					dragScreenY = e.getY();
					Point grid = screenToGrid(e.getPoint());
					if (isValidCell(grid.x, grid.y)) {
						dropTargetX = grid.x;
						dropTargetY = grid.y;
					} else {
						dropTargetX = -1;
						dropTargetY = -1;
					}
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				// Could add hover effects here in the future
			}
		});

		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() < 0) {
					zoom = Math.min(ZOOM_MAX, zoom * ZOOM_STEP_IN);
				} else {
					zoom = Math.max(ZOOM_MIN, zoom * ZOOM_STEP_OUT);
				}
				repaint();
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && isDragging) {
					isDragging = false;
					dragSourceX = -1;
					dragSourceY = -1;
					dropTargetX = -1;
					dropTargetY = -1;
					repaint();
				}
			}
		});
	}

	public void setMatrix(VMapMatrix matrix) {
		this.matrix = matrix;
		clearSelection();
		repaint();
	}

	public VMapMatrix getMatrix() {
		return matrix;
	}

	public void setListener(MatrixGridCanvasListener listener) {
		this.listener = listener;
	}

	public void clearSelection() {
		selectedX = -1;
		selectedY = -1;
		isDragging = false;
		dragSourceX = -1;
		dragSourceY = -1;
		dropTargetX = -1;
		dropTargetY = -1;
	}

	public Point getSelectedCell() {
		return new Point(selectedX, selectedY);
	}

	public void resetView() {
		zoom = 1.0;
		panX = 0;
		panY = 0;
		repaint();
	}

	private boolean isValidCell(int gx, int gy) {
		return matrix != null && gx >= 0 && gy >= 0 && gx < matrix.getWidth() && gy < matrix.getHeight();
	}

	/**
	 * Converts a screen-space mouse point to grid cell coordinates.
	 */
	public Point screenToGrid(Point mousePoint) {
		if (matrix == null) return new Point(-1, -1);
		int totalCellSize = CELL_SIZE + CELL_GAP;
		int gridPixelW = matrix.getWidth() * totalCellSize;
		int gridPixelH = matrix.getHeight() * totalCellSize;
		int ox = (getWidth() - (int) (gridPixelW * zoom)) / 2 + panX;
		int oy = (getHeight() - (int) (gridPixelH * zoom)) / 2 + panY;
		int gx = (int) Math.floor((mousePoint.x - ox) / (zoom * totalCellSize));
		int gy = (int) Math.floor((mousePoint.y - oy) / (zoom * totalCellSize));
		return new Point(gx, gy);
	}

	private Color getZoneColor(int zoneId) {
		if (zoneId < 0) return NO_ZONE_CELL_COLOR;
		return ZONE_COLORS[zoneId % ZONE_COLORS.length];
	}

	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		Graphics2D g = (Graphics2D) gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Fill background
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (matrix == null) {
			g.setColor(Color.GRAY);
			g.setFont(chunkIdFont);
			FontMetrics fm = g.getFontMetrics();
			String msg = "No matrix loaded";
			int tx = (getWidth() - fm.stringWidth(msg)) / 2;
			int ty = getHeight() / 2;
			g.drawString(msg, tx, ty);
			return;
		}

		int totalCellSize = CELL_SIZE + CELL_GAP;
		int gridPixelW = matrix.getWidth() * totalCellSize;
		int gridPixelH = matrix.getHeight() * totalCellSize;

		// Save transform and apply zoom/pan
		AffineTransform saved = g.getTransform();
		int ox = (getWidth() - (int) (gridPixelW * zoom)) / 2 + panX;
		int oy = (getHeight() - (int) (gridPixelH * zoom)) / 2 + panY;
		g.translate(ox, oy);
		g.scale(zoom, zoom);

		int width = matrix.getWidth();
		int height = matrix.getHeight();

		// Draw cells
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int px = x * totalCellSize;
				int py = y * totalCellSize;
				int chunkId = matrix.chunkIds.get(x, y);

				// Cell background color
				Color cellColor;
				if (chunkId == -1) {
					cellColor = EMPTY_CELL_COLOR;
				} else if (matrix.hasZones && matrix.zoneIds != null) {
					int zoneId = matrix.zoneIds.get(x, y);
					cellColor = getZoneColor(zoneId);
				} else {
					cellColor = NO_ZONE_CELL_COLOR;
				}

				g.setColor(cellColor);
				g.fillRect(px, py, CELL_SIZE, CELL_SIZE);

				// Cell border
				g.setColor(GRID_LINE_COLOR);
				g.drawRect(px, py, CELL_SIZE, CELL_SIZE);

				// Chunk ID text
				if (chunkId != -1) {
					String chunkText = String.valueOf(chunkId);
					g.setFont(chunkIdFont);
					FontMetrics fm = g.getFontMetrics();
					int tx = px + (CELL_SIZE - fm.stringWidth(chunkText)) / 2;
					int ty = py + (CELL_SIZE + fm.getAscent()) / 2 - 2;

					// Text shadow for readability
					g.setColor(TEXT_SHADOW_COLOR);
					g.drawString(chunkText, tx + 1, ty + 1);
					g.setColor(TEXT_COLOR);
					g.drawString(chunkText, tx, ty);

					// Zone ID in bottom-right
					if (matrix.hasZones && matrix.zoneIds != null) {
						int zoneId = matrix.zoneIds.get(x, y);
						String zoneText = "Z:" + zoneId;
						g.setFont(zoneIdFont);
						FontMetrics fmz = g.getFontMetrics();
						int zx = px + CELL_SIZE - fmz.stringWidth(zoneText) - 3;
						int zy = py + CELL_SIZE - 4;
						g.setColor(TEXT_SHADOW_COLOR);
						g.drawString(zoneText, zx + 1, zy + 1);
						g.setColor(TEXT_COLOR);
						g.drawString(zoneText, zx, zy);
					}
				} else {
					// Draw a small dash for empty cells
					g.setColor(new Color(120, 120, 120));
					g.setFont(zoneIdFont);
					FontMetrics fm = g.getFontMetrics();
					String emptyText = "--";
					int tx = px + (CELL_SIZE - fm.stringWidth(emptyText)) / 2;
					int ty = py + (CELL_SIZE + fm.getAscent()) / 2 - 2;
					g.drawString(emptyText, tx, ty);
				}
			}
		}

		// Draw column/row indices along the edges
		g.setFont(zoneIdFont);
		g.setColor(new Color(200, 200, 200));
		FontMetrics fmIdx = g.getFontMetrics();
		for (int x = 0; x < width; x++) {
			String colLabel = String.valueOf(x);
			int lx = x * totalCellSize + (CELL_SIZE - fmIdx.stringWidth(colLabel)) / 2;
			g.drawString(colLabel, lx, -4);
		}
		for (int y = 0; y < height; y++) {
			String rowLabel = String.valueOf(y);
			g.drawString(rowLabel, -fmIdx.stringWidth(rowLabel) - 4, y * totalCellSize + (CELL_SIZE + fmIdx.getAscent()) / 2 - 2);
		}

		// Draw selection highlight
		if (selectedX >= 0 && selectedY >= 0 && selectedX < width && selectedY < height) {
			int px = selectedX * totalCellSize;
			int py = selectedY * totalCellSize;
			g.setColor(SELECTION_COLOR);
			g.setStroke(new BasicStroke(3.0f));
			g.drawRect(px - 1, py - 1, CELL_SIZE + 2, CELL_SIZE + 2);
			g.setStroke(new BasicStroke(1.0f));
		}

		// Draw drop target highlight
		if (isDragging && dropTargetX >= 0 && dropTargetY >= 0
				&& dropTargetX < width && dropTargetY < height
				&& (dropTargetX != dragSourceX || dropTargetY != dragSourceY)) {
			int px = dropTargetX * totalCellSize;
			int py = dropTargetY * totalCellSize;
			g.setColor(DROP_TARGET_COLOR);
			g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
			g.setColor(DROP_TARGET_COLOR.darker());
			g.setStroke(new BasicStroke(2.0f));
			g.drawRect(px, py, CELL_SIZE, CELL_SIZE);
			g.setStroke(new BasicStroke(1.0f));
		}

		// Restore transform for drag ghost overlay (draw in screen space)
		g.setTransform(saved);

		// Draw drag ghost
		if (isDragging && dragSourceX >= 0 && dragSourceY >= 0) {
			int ghostSize = (int) (CELL_SIZE * zoom);
			int ghostX = dragScreenX - ghostSize / 2;
			int ghostY = dragScreenY - ghostSize / 2;

			int srcChunkId = matrix.chunkIds.get(dragSourceX, dragSourceY);
			Color ghostColor;
			if (matrix.hasZones && matrix.zoneIds != null) {
				ghostColor = getZoneColor(matrix.zoneIds.get(dragSourceX, dragSourceY));
			} else {
				ghostColor = NO_ZONE_CELL_COLOR;
			}

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
			g.setColor(ghostColor);
			g.fillRect(ghostX, ghostY, ghostSize, ghostSize);
			g.setColor(SELECTION_COLOR);
			g.setStroke(new BasicStroke(2.0f));
			g.drawRect(ghostX, ghostY, ghostSize, ghostSize);
			g.setStroke(new BasicStroke(1.0f));

			// Draw chunk ID on ghost
			String ghostText = String.valueOf(srcChunkId);
			g.setFont(chunkIdFont.deriveFont((float) (14 * zoom)));
			FontMetrics fmg = g.getFontMetrics();
			int tx = ghostX + (ghostSize - fmg.stringWidth(ghostText)) / 2;
			int ty = ghostY + (ghostSize + fmg.getAscent()) / 2 - 2;
			g.setColor(TEXT_COLOR);
			g.drawString(ghostText, tx, ty);

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
	}
}
