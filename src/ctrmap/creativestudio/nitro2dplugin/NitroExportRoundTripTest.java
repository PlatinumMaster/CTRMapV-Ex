package ctrmap.creativestudio.nitro2dplugin;

import ctrmap.creativestudio.ngcs2d.io.S2DIOProvider;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DAnimFrame;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DCellAnimation;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DMultiCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DMultiCellAnimation;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DOAM;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DPalette;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DResource;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DTileSheet;
import java.awt.Frame;
import xstandard.fs.accessors.MemoryFile;

/**
 * End-to-end round-trip verifier for the {@link Gen5NGCS2DPlugin} format
 * handlers (CSNNS_CLR / CGR / CER / ANR / MCR / MAR). For each format:
 * synthesise a small resource, export through the plugin handler into an
 * in-memory file, re-import via the same plugin, and assert the data
 * survived intact.
 *
 * <p>Run: {@code java ctrmap.creativestudio.nitro2dplugin.NitroExportRoundTripTest}.
 * Prints "ALL NITRO EXPORT ROUND-TRIP TESTS PASSED" on success.</p>
 */
public class NitroExportRoundTripTest {

	public static void main(String[] args) throws Exception {
		testNclrRoundTrip();
		testNcgrRoundTrip();
		testNcerRoundTrip();
		testNanrRoundTrip();
		testNmcrRoundTrip();
		testNmarRoundTrip();
		System.out.println("ALL NITRO EXPORT ROUND-TRIP TESTS PASSED");
	}

	// ------------------------------------------------------------------
	// NCLR
	// ------------------------------------------------------------------

	static void testNclrRoundTrip() throws Exception {
		Sprite2DResource src = new Sprite2DResource();
		Sprite2DPalette p = new Sprite2DPalette("Test", 16);
		// NDS palettes are BGR555 — 8bpp source colors get quantized on
		// export and re-expanded on import via bit5to8 (x<<3 | x>>>2),
		// which is non-lossless except on values where the top-3 bits of
		// the 5-bit slice equal the bottom-3 bits of the 8-bit value.
		// Pre-quantize the test palette so the round-trip is exact:
		// every test color is the result of bit5to8(bit8to5(x)) applied
		// per channel, mirroring what NITRO storage does anyway.
		for (int i = 0; i < 16; i++) {
			int r = bgrSafe(i * 16 + 8);
			int g = bgrSafe(i * 8 + 32);
			int b = bgrSafe(255 - i * 16);
			p.colors[i] = (i == 0) ? 0x00000000 : (0xFF000000 | (r << 16) | (g << 8) | b);
		}
		src.palettes.add(p);

		MemoryFile mem = new MemoryFile("test.nclr", new byte[0]);
		Gen5NGCS2DPlugin.CSNNS_CLR.exportResource(src, mem, providerFor(src));
		Sprite2DResource imported = Gen5NGCS2DPlugin.CSNNS_CLR.importFile(
			new MemoryFile("rt.nclr", mem.getBytes()), providerFor(null));

		assertEq("NCLR palette count", 1, imported.palettes.size());
		Sprite2DPalette rt = imported.palettes.get(0);
		assertEq("NCLR format", p.format, rt.format);
		assertEq("NCLR colors length", p.colors.length, rt.colors.length);
		for (int i = 0; i < p.colors.length; i++) {
			assertEq("NCLR color[" + i + "]", p.colors[i] & 0x00FFFFFF, rt.colors[i] & 0x00FFFFFF);
		}
	}

	/** Quantise an 8-bit channel value through BGR555 once so the
	 *  resulting value round-trips exactly through NCLR write → read. */
	private static int bgrSafe(int v) {
		int v8 = Math.max(0, Math.min(255, v));
		int v5 = (v8 >> 3) & 31;
		return (v5 << 3) | (v5 >>> 2);
	}

	// ------------------------------------------------------------------
	// NCGR
	// ------------------------------------------------------------------

	static void testNcgrRoundTrip() throws Exception {
		Sprite2DResource src = new Sprite2DResource();
		Sprite2DTileSheet ts = new Sprite2DTileSheet("Test", 3, 8); // IDX4, 8 tiles
		for (int t = 0; t < 8; t++) {
			for (int py = 0; py < 8; py++) {
				for (int px = 0; px < 8; px++) {
					ts.setPixel(t, px, py, (t + px + py) & 0x0F);
				}
			}
		}
		src.tileSheets.add(ts);

		MemoryFile mem = new MemoryFile("test.ncgr", new byte[0]);
		Gen5NGCS2DPlugin.CSNNS_CGR.exportResource(src, mem, providerFor(src));
		Sprite2DResource imported = Gen5NGCS2DPlugin.CSNNS_CGR.importFile(
			new MemoryFile("rt.ncgr", mem.getBytes()), providerFor(null));

		assertEq("NCGR tile sheet count", 1, imported.tileSheets.size());
		Sprite2DTileSheet rt = imported.tileSheets.get(0);
		assertEq("NCGR format", ts.format, rt.format);
		assertEq("NCGR tile count", ts.getTileCount(), rt.getTileCount());
		for (int t = 0; t < ts.getTileCount(); t++) {
			for (int py = 0; py < 8; py++) {
				for (int px = 0; px < 8; px++) {
					int a = ts.getPixel(t, px, py);
					int b = rt.getPixel(t, px, py);
					if (a != b) {
						throw new AssertionError("NCGR pixel tile=" + t
							+ " (" + px + "," + py + "): " + a + " vs " + b);
					}
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// NCER
	// ------------------------------------------------------------------

	static void testNcerRoundTrip() throws Exception {
		Sprite2DResource src = new Sprite2DResource();
		Sprite2DCell cell = new Sprite2DCell("CellA");
		Sprite2DOAM o0 = new Sprite2DOAM();
		o0.x = -16; o0.y = -16; o0.width = 16; o0.height = 16;
		o0.tileIndex = 0; o0.paletteIndex = 0; o0.priority = 1;
		o0.flipH = true; o0.flipV = false;
		cell.oams.add(o0);
		Sprite2DOAM o1 = new Sprite2DOAM();
		o1.x = 0; o1.y = -16; o1.width = 16; o1.height = 16;
		o1.tileIndex = 4; o1.paletteIndex = 1; o1.priority = 0;
		o1.rotationScaling = true; o1.doubleSize = true; o1.rsParamIndex = 3;
		cell.oams.add(o1);
		src.cells.add(cell);

		Sprite2DCell cellB = new Sprite2DCell("CellB");
		Sprite2DOAM o2 = new Sprite2DOAM();
		o2.x = 8; o2.y = 8; o2.width = 8; o2.height = 8;
		o2.tileIndex = 16; o2.paletteIndex = 2;
		cellB.oams.add(o2);
		src.cells.add(cellB);

		MemoryFile mem = new MemoryFile("test.ncer", new byte[0]);
		Gen5NGCS2DPlugin.CSNNS_CER.exportResource(src, mem, providerFor(src));
		Sprite2DResource imported = Gen5NGCS2DPlugin.CSNNS_CER.importFile(
			new MemoryFile("rt.ncer", mem.getBytes()), providerFor(null));

		assertEq("NCER cell count", 2, imported.cells.size());
		assertCellEqualOams(cell, imported.cells.get(0), "Cell0");
		assertCellEqualOams(cellB, imported.cells.get(1), "Cell1");
	}

	private static void assertCellEqualOams(Sprite2DCell expected, Sprite2DCell actual, String label) {
		assertEq(label + ".oamCount", expected.oams.size(), actual.oams.size());
		for (int i = 0; i < expected.oams.size(); i++) {
			Sprite2DOAM a = expected.oams.get(i);
			Sprite2DOAM b = actual.oams.get(i);
			assertEq(label + "[" + i + "].x", a.x, b.x);
			assertEq(label + "[" + i + "].y", a.y, b.y);
			assertEq(label + "[" + i + "].width", a.width, b.width);
			assertEq(label + "[" + i + "].height", a.height, b.height);
			assertEq(label + "[" + i + "].tileIndex", a.tileIndex, b.tileIndex);
			assertEq(label + "[" + i + "].paletteIndex", a.paletteIndex, b.paletteIndex);
			assertEq(label + "[" + i + "].priority", a.priority, b.priority);
			assertEq(label + "[" + i + "].flipH", a.flipH, b.flipH);
			assertEq(label + "[" + i + "].flipV", a.flipV, b.flipV);
			assertEq(label + "[" + i + "].rotationScaling", a.rotationScaling, b.rotationScaling);
			assertEq(label + "[" + i + "].doubleSize", a.doubleSize, b.doubleSize);
			if (a.rotationScaling) {
				assertEq(label + "[" + i + "].rsParamIndex", a.rsParamIndex, b.rsParamIndex);
			}
		}
	}

	// ------------------------------------------------------------------
	// NANR
	// ------------------------------------------------------------------

	static void testNanrRoundTrip() throws Exception {
		Sprite2DResource src = new Sprite2DResource();
		Sprite2DCellAnimation anim = new Sprite2DCellAnimation("AnimA");
		anim.playMode = 2;
		Sprite2DAnimFrame f0 = new Sprite2DAnimFrame(0, 8);
		anim.frames.add(f0);
		Sprite2DAnimFrame f1 = new Sprite2DAnimFrame(1, 4);
		anim.frames.add(f1);
		Sprite2DAnimFrame f2 = new Sprite2DAnimFrame(0, 6);
		anim.frames.add(f2);
		src.cellAnimations.add(anim);

		MemoryFile mem = new MemoryFile("test.nanr", new byte[0]);
		Gen5NGCS2DPlugin.CSNNS_ANR.exportResource(src, mem, providerFor(src));
		Sprite2DResource imported = Gen5NGCS2DPlugin.CSNNS_ANR.importFile(
			new MemoryFile("rt.nanr", mem.getBytes()), providerFor(null));

		assertEq("NANR anim count", 1, imported.cellAnimations.size());
		Sprite2DCellAnimation rt = imported.cellAnimations.get(0);
		assertEq("NANR playMode", anim.playMode, rt.playMode);
		assertEq("NANR frame count", anim.frames.size(), rt.frames.size());
		for (int i = 0; i < anim.frames.size(); i++) {
			assertEq("NANR frame[" + i + "].cellIndex",
				anim.frames.get(i).cellIndex, rt.frames.get(i).cellIndex);
			assertEq("NANR frame[" + i + "].duration",
				anim.frames.get(i).duration, rt.frames.get(i).duration);
		}
	}

	// ------------------------------------------------------------------
	// NMCR
	// ------------------------------------------------------------------

	static void testNmcrRoundTrip() throws Exception {
		Sprite2DResource src = new Sprite2DResource();
		Sprite2DMultiCell mc = new Sprite2DMultiCell("McA");
		mc.entries.add(new Sprite2DMultiCell.MultiCellEntry(0, (short) -8, (short) 4));
		mc.entries.add(new Sprite2DMultiCell.MultiCellEntry(1, (short) 12, (short) -8));
		src.multiCells.add(mc);

		MemoryFile mem = new MemoryFile("test.nmcr", new byte[0]);
		Gen5NGCS2DPlugin.CSNNS_MCR.exportResource(src, mem, providerFor(src));
		Sprite2DResource imported = Gen5NGCS2DPlugin.CSNNS_MCR.importFile(
			new MemoryFile("rt.nmcr", mem.getBytes()), providerFor(null));

		assertEq("NMCR mc count", 1, imported.multiCells.size());
		Sprite2DMultiCell rt = imported.multiCells.get(0);
		assertEq("NMCR entry count", mc.entries.size(), rt.entries.size());
		for (int i = 0; i < mc.entries.size(); i++) {
			Sprite2DMultiCell.MultiCellEntry a = mc.entries.get(i);
			Sprite2DMultiCell.MultiCellEntry b = rt.entries.get(i);
			assertEq("NMCR entry[" + i + "].animIndex", a.animIndex, b.animIndex);
			assertEq("NMCR entry[" + i + "].x", a.x, b.x);
			assertEq("NMCR entry[" + i + "].y", a.y, b.y);
		}
	}

	// ------------------------------------------------------------------
	// NMAR
	// ------------------------------------------------------------------

	static void testNmarRoundTrip() throws Exception {
		Sprite2DResource src = new Sprite2DResource();
		Sprite2DMultiCellAnimation mca = new Sprite2DMultiCellAnimation("McAnim");
		mca.playMode = 1;
		mca.frames.add(new Sprite2DMultiCellAnimation.MultiCellAnimFrame(0, 12));
		mca.frames.add(new Sprite2DMultiCellAnimation.MultiCellAnimFrame(2, 6));
		src.multiCellAnimations.add(mca);

		MemoryFile mem = new MemoryFile("test.nmar", new byte[0]);
		Gen5NGCS2DPlugin.CSNNS_MAR.exportResource(src, mem, providerFor(src));
		Sprite2DResource imported = Gen5NGCS2DPlugin.CSNNS_MAR.importFile(
			new MemoryFile("rt.nmar", mem.getBytes()), providerFor(null));

		assertEq("NMAR anim count", 1, imported.multiCellAnimations.size());
		Sprite2DMultiCellAnimation rt = imported.multiCellAnimations.get(0);
		assertEq("NMAR playMode", mca.playMode, rt.playMode);
		assertEq("NMAR frame count", mca.frames.size(), rt.frames.size());
		for (int i = 0; i < mca.frames.size(); i++) {
			assertEq("NMAR frame[" + i + "].multiCellIndex",
				mca.frames.get(i).multiCellIndex, rt.frames.get(i).multiCellIndex);
			assertEq("NMAR frame[" + i + "].duration",
				mca.frames.get(i).duration, rt.frames.get(i).duration);
		}
	}

	// ------------------------------------------------------------------
	// helpers
	// ------------------------------------------------------------------

	private static S2DIOProvider providerFor(Sprite2DResource res) {
		return new S2DIOProvider() {
			@Override public Sprite2DPalette getPalette() {
				return (res != null && !res.palettes.isEmpty()) ? res.palettes.get(0) : null;
			}
			@Override public Sprite2DTileSheet getTileSheet() {
				return (res != null && !res.tileSheets.isEmpty()) ? res.tileSheets.get(0) : null;
			}
			@Override public Sprite2DResource getAll() { return res; }
			@Override public Frame getGUIParent() { return null; }
		};
	}

	static void assertEq(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + ", got " + actual);
		}
	}

	static void assertEq(String label, boolean expected, boolean actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + ", got " + actual);
		}
	}
}
