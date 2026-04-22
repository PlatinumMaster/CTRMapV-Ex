package ctrmap.creativestudio.nitro2dplugin;

import ctrmap.creativestudio.ngcs2d.project.Cs2dProject;
import ctrmap.creativestudio.ngcs2d.project.Cs2dProjectIO;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DAnimFrame;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DCellAnimation;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DMultiCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DMultiCellAnimation;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DOAM;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DPalette;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DResource;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DTileSheet;
import java.io.File;
import java.nio.file.Files;
import xstandard.fs.accessors.DiskFile;

/**
 * Round-trip test for {@link Cs2dProjectIO}: synthesise a small
 * {@link Sprite2DResource}, save it as a CS2D project (YAML + indexed
 * PNG) into a temp directory, load it back, and verify every field
 * survived intact.
 *
 * <p>Run: {@code java ctrmap.creativestudio.nitro2dplugin.Cs2dProjectRoundTripTest}.
 * Prints "ALL CS2D PROJECT TESTS PASSED" on success; AssertionError on failure.</p>
 */
public class Cs2dProjectRoundTripTest {

	public static void main(String[] args) throws Exception {
		testFullRoundTrip();
		System.out.println("ALL CS2D PROJECT TESTS PASSED");
	}

	static void testFullRoundTrip() throws Exception {
		Sprite2DResource src = buildSyntheticResource();

		File tempDirJ = Files.createTempDirectory("cs2dproj-rt-").toFile();
		try {
			DiskFile tempDir = new DiskFile(tempDirJ);
			Cs2dProjectIO.save(new Cs2dProject(src, tempDir), tempDir);

			Cs2dProject loaded = Cs2dProjectIO.load(tempDir);
			Sprite2DResource dst = loaded.resource;

			assertEq("formatVersion", Cs2dProject.FORMAT_VERSION, loaded.formatVersion);
			assertEq("mappingMode", src.mappingMode, dst.mappingMode);

			assertEq("palette count", src.palettes.size(), dst.palettes.size());
			for (int i = 0; i < src.palettes.size(); i++) {
				Sprite2DPalette a = src.palettes.get(i);
				Sprite2DPalette b = dst.palettes.get(i);
				assertEq("palette[" + i + "].name", a.name, b.name);
				assertEq("palette[" + i + "].format", a.format, b.format);
				assertEq("palette[" + i + "].colors.length", a.colors.length, b.colors.length);
				for (int c = 0; c < a.colors.length; c++) {
					assertEq("palette[" + i + "].colors[" + c + "]", a.colors[c], b.colors[c]);
				}
			}

			assertEq("tileSheet count", src.tileSheets.size(), dst.tileSheets.size());
			for (int i = 0; i < src.tileSheets.size(); i++) {
				Sprite2DTileSheet a = src.tileSheets.get(i);
				Sprite2DTileSheet b = dst.tileSheets.get(i);
				assertEq("sheet[" + i + "].name", a.name, b.name);
				assertEq("sheet[" + i + "].format", a.format, b.format);
				assertEq("sheet[" + i + "].tileWidth", a.tileWidth, b.tileWidth);
				assertEq("sheet[" + i + "].tileHeight", a.tileHeight, b.tileHeight);
				assertEq("sheet[" + i + "].isLinearMapped", a.isLinearMapped, b.isLinearMapped);
				assertEq("sheet[" + i + "].objTilesWide", a.objTilesWide, b.objTilesWide);
				assertEq("sheet[" + i + "].objTilesHigh", a.objTilesHigh, b.objTilesHigh);
				assertEq("sheet[" + i + "].rasterLayout", a.rasterLayout, b.rasterLayout);
				// Pixel data: the PNG holds at least the first src.getTileCount()
				// tiles' worth of indices in the same order; compare those.
				int srcTiles = a.getTileCount();
				assertTrue("sheet[" + i + "] decoded tiles >= original",
					b.getTileCount() >= srcTiles);
				for (int t = 0; t < srcTiles; t++) {
					for (int py = 0; py < 8; py++) {
						for (int px = 0; px < 8; px++) {
							int va = a.getPixel(t, px, py);
							int vb = b.getPixel(t, px, py);
							if (va != vb) {
								throw new AssertionError("sheet[" + i + "] pixel mismatch "
									+ "tile=" + t + " (" + px + "," + py + "): "
									+ va + " vs " + vb);
							}
						}
					}
				}
			}

			assertEq("cell count", src.cells.size(), dst.cells.size());
			for (int i = 0; i < src.cells.size(); i++) {
				Sprite2DCell a = src.cells.get(i);
				Sprite2DCell b = dst.cells.get(i);
				assertEq("cell[" + i + "].name", a.name, b.name);
				assertEq("cell[" + i + "].hasVramTransfer", a.hasVramTransfer, b.hasVramTransfer);
				assertEq("cell[" + i + "].vramTransferSrcAddr", a.vramTransferSrcAddr, b.vramTransferSrcAddr);
				assertEq("cell[" + i + "].vramTransferSize", a.vramTransferSize, b.vramTransferSize);
				assertEq("cell[" + i + "].oams.size", a.oams.size(), b.oams.size());
				for (int o = 0; o < a.oams.size(); o++) {
					Sprite2DOAM oa = a.oams.get(o);
					Sprite2DOAM ob = b.oams.get(o);
					assertEq("oam[" + o + "].x", oa.x, ob.x);
					assertEq("oam[" + o + "].y", oa.y, ob.y);
					assertEq("oam[" + o + "].width", oa.width, ob.width);
					assertEq("oam[" + o + "].height", oa.height, ob.height);
					assertEq("oam[" + o + "].tileIndex", oa.tileIndex, ob.tileIndex);
					assertEq("oam[" + o + "].paletteIndex", oa.paletteIndex, ob.paletteIndex);
					assertEq("oam[" + o + "].flipH", oa.flipH, ob.flipH);
					assertEq("oam[" + o + "].flipV", oa.flipV, ob.flipV);
					assertEq("oam[" + o + "].priority", oa.priority, ob.priority);
					assertEq("oam[" + o + "].rotationScaling", oa.rotationScaling, ob.rotationScaling);
					assertEq("oam[" + o + "].doubleSize", oa.doubleSize, ob.doubleSize);
					assertEq("oam[" + o + "].rsParamIndex", oa.rsParamIndex, ob.rsParamIndex);
					assertEq("oam[" + o + "].layerName", oa.layerName, ob.layerName);
					assertEq("oam[" + o + "].visible", oa.visible, ob.visible);
					assertEq("oam[" + o + "].opacity", oa.opacity, ob.opacity);
					assertEq("oam[" + o + "].locked", oa.locked, ob.locked);
				}
			}

			assertEq("multiCell count", src.multiCells.size(), dst.multiCells.size());
			for (int i = 0; i < src.multiCells.size(); i++) {
				Sprite2DMultiCell a = src.multiCells.get(i);
				Sprite2DMultiCell b = dst.multiCells.get(i);
				assertEq("mc[" + i + "].name", a.name, b.name);
				assertEq("mc[" + i + "].entries.size", a.entries.size(), b.entries.size());
				for (int e = 0; e < a.entries.size(); e++) {
					Sprite2DMultiCell.MultiCellEntry ea = a.entries.get(e);
					Sprite2DMultiCell.MultiCellEntry eb = b.entries.get(e);
					assertEq("mcEntry[" + e + "].animIndex", ea.animIndex, eb.animIndex);
					assertEq("mcEntry[" + e + "].x", ea.x, eb.x);
					assertEq("mcEntry[" + e + "].y", ea.y, eb.y);
					assertEq("mcEntry[" + e + "].layerName", ea.layerName, eb.layerName);
					assertEq("mcEntry[" + e + "].visible", ea.visible, eb.visible);
					assertEq("mcEntry[" + e + "].opacity", ea.opacity, eb.opacity);
				}
			}

			assertEq("cellAnim count", src.cellAnimations.size(), dst.cellAnimations.size());
			for (int i = 0; i < src.cellAnimations.size(); i++) {
				Sprite2DCellAnimation a = src.cellAnimations.get(i);
				Sprite2DCellAnimation b = dst.cellAnimations.get(i);
				assertEq("anim[" + i + "].name", a.name, b.name);
				assertEq("anim[" + i + "].playMode", a.playMode, b.playMode);
				assertEq("anim[" + i + "].frames.size", a.frames.size(), b.frames.size());
				for (int f = 0; f < a.frames.size(); f++) {
					Sprite2DAnimFrame fa = a.frames.get(f);
					Sprite2DAnimFrame fb = b.frames.get(f);
					assertEq("frame[" + f + "].cellIndex", fa.cellIndex, fb.cellIndex);
					assertEq("frame[" + f + "].duration", fa.duration, fb.duration);
					assertEq("frame[" + f + "].translateX", fa.translateX, fb.translateX);
					assertEq("frame[" + f + "].rotation", fa.rotation, fb.rotation);
					assertEq("frame[" + f + "].scaleX", fa.scaleX, fb.scaleX);
				}
			}

			assertEq("multiCellAnim count", src.multiCellAnimations.size(), dst.multiCellAnimations.size());
			for (int i = 0; i < src.multiCellAnimations.size(); i++) {
				Sprite2DMultiCellAnimation a = src.multiCellAnimations.get(i);
				Sprite2DMultiCellAnimation b = dst.multiCellAnimations.get(i);
				assertEq("mcAnim[" + i + "].name", a.name, b.name);
				assertEq("mcAnim[" + i + "].playMode", a.playMode, b.playMode);
				assertEq("mcAnim[" + i + "].frames.size", a.frames.size(), b.frames.size());
				for (int f = 0; f < a.frames.size(); f++) {
					Sprite2DMultiCellAnimation.MultiCellAnimFrame fa = a.frames.get(f);
					Sprite2DMultiCellAnimation.MultiCellAnimFrame fb = b.frames.get(f);
					assertEq("mcFrame[" + f + "].multiCellIndex", fa.multiCellIndex, fb.multiCellIndex);
					assertEq("mcFrame[" + f + "].duration", fa.duration, fb.duration);
				}
			}
		} finally {
			deleteRecursive(tempDirJ);
		}
	}

	private static Sprite2DResource buildSyntheticResource() {
		Sprite2DResource res = new Sprite2DResource();
		res.mappingMode = Sprite2DResource.MAPPING_MODE_2D;

		// Two palettes — one 16-color, one 256-color.
		Sprite2DPalette p0 = new Sprite2DPalette("Body", 16);
		for (int i = 0; i < 16; i++) {
			p0.colors[i] = (i == 0) ? 0x00000000 : (0xFF000000 | (i * 0x111111));
		}
		res.palettes.add(p0);

		Sprite2DPalette p1 = new Sprite2DPalette("Effect", 16);
		for (int i = 0; i < 16; i++) {
			p1.colors[i] = (i == 0) ? 0x00000000 : (0xFFFF0000 | (i * 0x000F00));
		}
		res.palettes.add(p1);

		// One tile sheet, IDX4 (4bpp), 4 tiles, lineal-mapped to exercise
		// every metadata field.
		Sprite2DTileSheet ts = new Sprite2DTileSheet("MainSheet", 3, 4);
		ts.tileWidth = 2;
		ts.tileHeight = 2;
		ts.isLinearMapped = true;
		ts.objTilesWide = 2;
		ts.objTilesHigh = 2;
		ts.rasterLayout = false;
		// Distinct pattern per tile so pixel-level equality is meaningful.
		for (int t = 0; t < 4; t++) {
			for (int py = 0; py < 8; py++) {
				for (int px = 0; px < 8; px++) {
					ts.setPixel(t, px, py, (t + px + py) & 0x0F);
				}
			}
		}
		res.tileSheets.add(ts);

		// One cell with two OAMs incl. layer metadata.
		Sprite2DCell cell = new Sprite2DCell("Pose0");
		cell.hasVramTransfer = true;
		cell.vramTransferSrcAddr = 0x800;
		cell.vramTransferSize = 0x400;
		Sprite2DOAM o0 = new Sprite2DOAM();
		o0.x = -16;
		o0.y = -16;
		o0.width = 16;
		o0.height = 16;
		o0.tileIndex = 0;
		o0.paletteIndex = 0;
		o0.flipH = false;
		o0.flipV = true;
		o0.priority = 1;
		o0.layerName = "Body";
		o0.visible = true;
		o0.opacity = 0.75f;
		cell.oams.add(o0);
		Sprite2DOAM o1 = new Sprite2DOAM();
		o1.x = 0;
		o1.y = -16;
		o1.width = 16;
		o1.height = 16;
		o1.tileIndex = 4;
		o1.paletteIndex = 1;
		o1.rotationScaling = true;
		o1.doubleSize = true;
		o1.rsParamIndex = 5;
		o1.layerName = "Head";
		o1.visible = false;
		o1.locked = true;
		cell.oams.add(o1);
		res.cells.add(cell);

		// Cell animation with affine + identity frames.
		Sprite2DCellAnimation anim = new Sprite2DCellAnimation("Idle");
		anim.playMode = 2;
		Sprite2DAnimFrame f0 = new Sprite2DAnimFrame(0, 8);
		anim.frames.add(f0);
		Sprite2DAnimFrame f1 = new Sprite2DAnimFrame(0, 6);
		f1.translateX = 4f;
		f1.rotation = 30f;
		f1.scaleX = 1.5f;
		anim.frames.add(f1);
		res.cellAnimations.add(anim);

		// Multi-cell with two entries (one hidden) referencing the anim.
		Sprite2DMultiCell mc = new Sprite2DMultiCell("Full");
		mc.entries.add(new Sprite2DMultiCell.MultiCellEntry(0, (short) -8, (short) 0));
		Sprite2DMultiCell.MultiCellEntry me = new Sprite2DMultiCell.MultiCellEntry(0, (short) 8, (short) 0);
		me.layerName = "Mirror";
		me.visible = false;
		me.opacity = 0.5f;
		mc.entries.add(me);
		res.multiCells.add(mc);

		// Multi-cell anim with a single frame.
		Sprite2DMultiCellAnimation mca = new Sprite2DMultiCellAnimation("Dance");
		mca.playMode = 1;
		mca.frames.add(new Sprite2DMultiCellAnimation.MultiCellAnimFrame(0, 12));
		res.multiCellAnimations.add(mca);

		return res;
	}

	private static void deleteRecursive(File f) {
		if (f == null) return;
		if (f.isDirectory()) {
			File[] kids = f.listFiles();
			if (kids != null) {
				for (File k : kids) {
					deleteRecursive(k);
				}
			}
		}
		f.delete();
	}

	// ----- assertions -----

	static void assertEq(String label, Object expected, Object actual) {
		if (expected == null ? actual != null : !expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + ", got " + actual);
		}
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

	static void assertEq(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001f) {
			throw new AssertionError(label + ": expected " + expected + ", got " + actual);
		}
	}

	static void assertTrue(String label, boolean cond) {
		if (!cond) throw new AssertionError(label);
	}
}
