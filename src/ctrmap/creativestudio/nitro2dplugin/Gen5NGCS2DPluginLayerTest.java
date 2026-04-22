package ctrmap.creativestudio.nitro2dplugin;

import ctrmap.creativestudio.ngcs2d.res.Sprite2DCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DMultiCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DOAM;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DResource;
import xstandard.fs.accessors.MemoryFile;

/**
 * Verifies that editor-only layer metadata (visible / opacity / layerName
 * / locked) added in Part B phase 1 round-trips in memory and that the
 * NITRO exporters correctly drop hidden layers.
 *
 * <p>No ROM required — synthesises a tiny {@link Sprite2DResource} with a
 * cell containing 3 OAMs (one hidden) and a multi-cell with 2 entries
 * (one hidden), writes via the Gen5 plugin's CER + MCR exporters, reads
 * back via the same plugin's importers, and asserts the visible subset
 * survived.</p>
 *
 * <p>Run: {@code java ctrmap.creativestudio.nitro2dplugin.Gen5NGCS2DPluginLayerTest}.
 * Prints "ALL LAYER TESTS PASSED" on success; AssertionError on failure.</p>
 */
public class Gen5NGCS2DPluginLayerTest {

    public static void main(String[] args) {
        testOamCopyPreservesLayerFields();
        testMultiCellEntryCopyPreservesLayerFields();
        testGetVisibleOAMsFiltersHidden();
        testGetVisibleEntriesFiltersHidden();
        testNcerExportDropsHiddenOAMs();
        testNmcrExportDropsHiddenEntries();
        System.out.println("ALL LAYER TESTS PASSED");
    }

    /** Sanity: Sprite2DOAM copy constructor brings the new fields along. */
    static void testOamCopyPreservesLayerFields() {
        Sprite2DOAM src = new Sprite2DOAM();
        src.layerName = "Head";
        src.visible = false;
        src.opacity = 0.5f;
        src.locked = true;
        Sprite2DOAM cp = new Sprite2DOAM(src);
        assertEq("layerName copied", "Head", cp.layerName);
        assertFalse("visible copied", cp.visible);
        assertEq("opacity copied", 0.5f, cp.opacity);
        assertTrue("locked copied", cp.locked);
    }

    /** Same for MultiCellEntry. */
    static void testMultiCellEntryCopyPreservesLayerFields() {
        Sprite2DMultiCell.MultiCellEntry src = new Sprite2DMultiCell.MultiCellEntry(3, (short) 10, (short) 20);
        src.layerName = "Torso";
        src.visible = false;
        src.opacity = 0.25f;
        src.locked = true;
        Sprite2DMultiCell.MultiCellEntry cp = new Sprite2DMultiCell.MultiCellEntry(src);
        assertEq("entry.animIndex copied", 3, cp.animIndex);
        assertEq("entry.x copied", 10, cp.x);
        assertEq("entry.y copied", 20, cp.y);
        assertEq("entry.layerName copied", "Torso", cp.layerName);
        assertFalse("entry.visible copied", cp.visible);
        assertEq("entry.opacity copied", 0.25f, cp.opacity);
        assertTrue("entry.locked copied", cp.locked);
    }

    /** {@link Sprite2DCell#getVisibleOAMs()} drops hidden OAMs in order. */
    static void testGetVisibleOAMsFiltersHidden() {
        Sprite2DCell cell = new Sprite2DCell("Test");
        Sprite2DOAM a = newOam("A", true);
        Sprite2DOAM b = newOam("B", false);
        Sprite2DOAM c = newOam("C", true);
        cell.oams.add(a);
        cell.oams.add(b);
        cell.oams.add(c);
        java.util.List<Sprite2DOAM> visible = cell.getVisibleOAMs();
        assertEq("visible count", 2, visible.size());
        assertSame("visible[0] == A", a, visible.get(0));
        assertSame("visible[1] == C", c, visible.get(1));
        // Mutating the returned list must not affect the full OAM list.
        visible.clear();
        assertEq("cell.oams untouched after visible list clear", 3, cell.oams.size());
    }

    /** {@link Sprite2DMultiCell#getVisibleEntries()} parallel check. */
    static void testGetVisibleEntriesFiltersHidden() {
        Sprite2DMultiCell mc = new Sprite2DMultiCell("Test");
        Sprite2DMultiCell.MultiCellEntry e1 = new Sprite2DMultiCell.MultiCellEntry(0, (short) 0, (short) 0);
        Sprite2DMultiCell.MultiCellEntry e2 = new Sprite2DMultiCell.MultiCellEntry(1, (short) 10, (short) 10);
        e2.visible = false;
        Sprite2DMultiCell.MultiCellEntry e3 = new Sprite2DMultiCell.MultiCellEntry(2, (short) 20, (short) 20);
        mc.entries.add(e1);
        mc.entries.add(e2);
        mc.entries.add(e3);
        java.util.List<Sprite2DMultiCell.MultiCellEntry> visible = mc.getVisibleEntries();
        assertEq("visible entry count", 2, visible.size());
        assertSame("visible entry[0] == e1", e1, visible.get(0));
        assertSame("visible entry[1] == e3", e3, visible.get(1));
    }

    /**
     * End-to-end: synthesise a resource with one hidden OAM, export via
     * the CER plugin, re-import, confirm the hidden OAM is NOT present.
     */
    static void testNcerExportDropsHiddenOAMs() {
        Sprite2DResource res = new Sprite2DResource();
        Sprite2DCell cell = new Sprite2DCell("Cell_0");
        cell.addOAM(oamAt(0, 0, 16, 16, true));   // visible
        cell.addOAM(oamAt(16, 0, 16, 16, false)); // HIDDEN
        cell.addOAM(oamAt(0, 16, 16, 16, true));  // visible
        res.cells.add(cell);
        res.mappingMode = Sprite2DResource.MAPPING_MODE_1D_32K;

        MemoryFile out = new MemoryFile("test.ncer", new byte[0]);
        Gen5NGCS2DPlugin.CSNNS_CER.exportResource(res, out, null);

        Sprite2DResource rt = Gen5NGCS2DPlugin.CSNNS_CER.importFile(
            new MemoryFile("rt.ncer", out.getBytes()), null);
        assertEq("cell count preserved", 1, rt.cells.size());
        assertEq("hidden OAM dropped (expected 2 visible)",
            2, rt.cells.get(0).oams.size());
        // Confirm the SURVIVING OAMs are the two that were visible
        // — we used distinct x/y so positions identify them.
        Sprite2DOAM first = rt.cells.get(0).oams.get(0);
        Sprite2DOAM second = rt.cells.get(0).oams.get(1);
        boolean order1 = first.x == 0 && first.y == 0 && second.x == 0 && second.y == 16;
        boolean order2 = first.x == 0 && first.y == 16 && second.x == 0 && second.y == 0;
        if (!order1 && !order2) {
            throw new AssertionError("visible OAMs lost their original positions: "
                + "[" + first.x + "," + first.y + "] and ["
                + second.x + "," + second.y + "]");
        }
    }

    /**
     * Parallel end-to-end for multi-cells: export, re-import, confirm
     * hidden entries are absent.
     */
    static void testNmcrExportDropsHiddenEntries() {
        Sprite2DResource res = new Sprite2DResource();
        Sprite2DMultiCell mc = new Sprite2DMultiCell("MC_0");
        mc.entries.add(new Sprite2DMultiCell.MultiCellEntry(0, (short) 0, (short) 0));
        Sprite2DMultiCell.MultiCellEntry hidden = new Sprite2DMultiCell.MultiCellEntry(1, (short) 5, (short) 5);
        hidden.visible = false;
        mc.entries.add(hidden);
        mc.entries.add(new Sprite2DMultiCell.MultiCellEntry(2, (short) 10, (short) 10));
        res.multiCells.add(mc);

        MemoryFile out = new MemoryFile("test.nmcr", new byte[0]);
        Gen5NGCS2DPlugin.CSNNS_MCR.exportResource(res, out, null);

        Sprite2DResource rt = Gen5NGCS2DPlugin.CSNNS_MCR.importFile(
            new MemoryFile("rt.nmcr", out.getBytes()), null);
        assertEq("multiCell count preserved", 1, rt.multiCells.size());
        assertEq("hidden entry dropped (expected 2 visible)",
            2, rt.multiCells.get(0).entries.size());
        // Check that animIndex=1 (the hidden one) is absent
        for (Sprite2DMultiCell.MultiCellEntry e : rt.multiCells.get(0).entries) {
            if (e.animIndex == 1) {
                throw new AssertionError("hidden entry (animIndex=1) should not have survived export");
            }
        }
    }

    // --- Helpers ---

    private static Sprite2DOAM oamAt(int x, int y, int w, int h, boolean visible) {
        Sprite2DOAM o = new Sprite2DOAM();
        o.x = x;
        o.y = y;
        o.width = w;
        o.height = h;
        o.visible = visible;
        return o;
    }

    private static Sprite2DOAM newOam(String name, boolean visible) {
        Sprite2DOAM o = new Sprite2DOAM();
        o.layerName = name;
        o.visible = visible;
        return o;
    }

    private static void assertEq(String what, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertEq(String what, float expected, float actual) {
        if (Math.abs(expected - actual) > 1e-6f) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertEq(String what, String expected, String actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(String what, boolean actual) {
        if (!actual) throw new AssertionError(what + ": expected true but got false");
    }

    private static void assertFalse(String what, boolean actual) {
        if (actual) throw new AssertionError(what + ": expected false but got true");
    }

    private static void assertSame(String what, Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError(what + ": identity mismatch");
        }
    }
}
