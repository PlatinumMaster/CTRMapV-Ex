package ctrmap.creativestudio.nitro2dplugin;

import ctrmap.creativestudio.ngcs2d.io.CS2DIOContentType;
import ctrmap.creativestudio.ngcs2d.io.Default2DFormatHandler;
import ctrmap.creativestudio.ngcs2d.io.I2DFormatHandler;
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
import ctrmap.creativestudio.ngcs2d.rtldr.INGCS2DPlugin;
import ctrmap.creativestudio.ngcs2d.rtldr.NGCS2DJulietIface;
import ctrmap.creativestudio.ngcs.io.FormatDetectorInput;
import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.nitro2d.nanr.NANR;
import ctrmap.formats.ntr.nitro2d.nanr.NANRAnimation;
import ctrmap.formats.ntr.nitro2d.nanr.NANRFrame;
import ctrmap.formats.ntr.nitro2d.ncer.NCER;
import ctrmap.formats.ntr.nitro2d.ncer.NCERCell;
import ctrmap.formats.ntr.nitro2d.ncer.NCERCellBank;
import ctrmap.formats.ntr.nitro2d.ncer.NCERCellOAM;
import ctrmap.formats.ntr.nitro2d.ncgr.NCGR;
import ctrmap.formats.ntr.nitro2d.nclr.NCLR;
import ctrmap.formats.ntr.nitro2d.nclr.NCLRPaletteBlock;
import ctrmap.formats.ntr.nitro2d.nmar.NMAR;
import ctrmap.formats.ntr.nitro2d.nmar.NMARAnimation;
import ctrmap.formats.ntr.nitro2d.nmar.NMARFrame;
import ctrmap.formats.ntr.nitro2d.nmcr.NMCR;
import ctrmap.formats.ntr.nitro2d.nmcr.NMCRMultiCell;
import ctrmap.formats.ntr.nitro2d.nmcr.NMCRMultiCellProperty;
import xstandard.fs.FSFile;
import java.awt.Frame;
import java.util.List;

/**
 * NITRO 2D format plugin for NGCS2D.
 *
 * <p>Registers format handlers for NCLR, NCGR, NCER, NANR, NMCR, and NMAR
 * files. Follows the same pattern as Gen5NGCSPlugin for 3D formats.</p>
 */
public class Gen5NGCS2DPlugin implements INGCS2DPlugin {

	public static final I2DFormatHandler CSNNS_CLR = new Default2DFormatHandler(NCLR.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NCLR.MAGIC);
		}

		@Override
		public Sprite2DResource importFile(FSFile fsf, S2DIOProvider exData) {
			NCLR nclr = new NCLR(fsf);
			Sprite2DResource res = new Sprite2DResource();
			int palCount = nclr.getPaletteCount();
			int cpp = nclr.getColorsPerPalette();
			// Flatten every sub-palette into one Sprite2DPalette so that
			// cell rendering can resolve `paletteIndex * 16 + palIdx`
			// against a single colour table. `format` is preserved as the
			// per-sub-palette colour count (16 for 4bpp, 256 for 8bpp).
			Sprite2DPalette pal = new Sprite2DPalette(fsf.getName(), cpp);
			pal.colors = new int[palCount * cpp];
			for (int p = 0; p < palCount; p++) {
				for (int c = 0; c < cpp; c++) {
					GXColor gxc = nclr.getColor(p, c);
					int argb;
					if (c == 0) {
						// Index 0 of every sub-palette is the transparent slot.
						argb = 0x00000000;
					} else {
						argb = (255 << 24) | (gxc.red << 16) | (gxc.green << 8) | gxc.blue;
					}
					pal.colors[p * cpp + c] = argb;
				}
			}
			res.palettes.add(pal);
			return res;
		}

		@Override
		public void exportResource(Sprite2DResource res, FSFile target, S2DIOProvider exData) {
			if (res.palettes.isEmpty()) {
				return;
			}
			NCLR nclr = new NCLR();
			Sprite2DPalette first = res.palettes.get(0);
			boolean is4bpp = first.format <= 16;
			int cpp = is4bpp ? 16 : 256;
			// Number of sub-palettes is determined by the flattened length
			// of the first Sprite2DPalette divided by the per-sub-palette
			// colour count.
			int subCount = Math.max(1, first.colors.length / cpp);
			nclr.paletteBlock.bitDepth = is4bpp ? 4 : 8;
			if (subCount == 1) {
				nclr.paletteBlock.dimensions = is4bpp
					? NCLRPaletteBlock.DIM_1PAL_16COL
					: NCLRPaletteBlock.DIM_1PAL_256COL;
			} else {
				nclr.paletteBlock.dimensions = is4bpp
					? NCLRPaletteBlock.DIM_NPAL_16COL
					: NCLRPaletteBlock.DIM_NPAL_256COL;
			}
			int totalColors = subCount * cpp;
			nclr.paletteBlock.colors = new short[totalColors];
			for (int i = 0; i < totalColors; i++) {
				int argb = i < first.colors.length ? first.colors[i] : 0;
				int r = (argb >> 16) & 0xFF;
				int g = (argb >> 8) & 0xFF;
				int b = argb & 0xFF;
				int bgr555 = GXColor.bit8to5(b) << 10 | GXColor.bit8to5(g) << 5 | GXColor.bit8to5(r);
				nclr.paletteBlock.colors[i] = (short) bgr555;
			}
			nclr.write(target);
		}
	};

	public static final I2DFormatHandler CSNNS_CGR = new Default2DFormatHandler(NCGR.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NCGR.MAGIC);
		}

		@Override
		public Sprite2DResource importFile(FSFile fsf, S2DIOProvider exData) {
			NCGR ncgr = new NCGR(fsf);
			Sprite2DResource res = new Sprite2DResource();
			Sprite2DTileSheet ts = new Sprite2DTileSheet();
			ts.name = fsf.getName();
			ts.format = ncgr.characterBlock.format;
			ts.tileData = ncgr.characterBlock.tileData.clone();
			// Preserve the on-disk dimensions so the canvas can render the
			// tile sheet at its natural width (e.g. Tinke "Lineal" 32-wide).
			ts.tileWidth = ncgr.characterBlock.tileWidth;
			ts.tileHeight = ncgr.characterBlock.tileHeight;
			// Detect 1D / lineal OBJ mapping. The renderer reorders tiles by
			// OBJ-size sub-blocks (set later by linkTileSheetsToCells) so
			// each sprite appears coherent in the tile sheet view.
			ts.isLinearMapped = ncgr.characterBlock.isLinearMapped();
			// Bitmap-mode NCGRs store their pixels as a linear raster; the
			// parser already unswizzled it to tile-major for us, but the
			// resulting tile grid is laid out by NCGR width even if the NCER
			// advertises 1D OBJ mapping. Flag it so the renderer walks the
			// OAM with the NCGR row stride instead of the OAM's own width,
			// otherwise bitmap + 1D sprites fragment into wrong pieces
			// (Pokemon B/W battle sprites, e.g. Cyndaquil Cell_0).
			ts.rasterLayout = ncgr.characterBlock.bitmap;
			res.tileSheets.add(ts);
			return res;
		}

		@Override
		public void exportResource(Sprite2DResource res, FSFile target, S2DIOProvider exData) {
			if (res.tileSheets.isEmpty()) {
				return;
			}
			NCGR ncgr = new NCGR();
			Sprite2DTileSheet ts = res.tileSheets.get(0);
			ncgr.characterBlock.format = ts.format;
			ncgr.characterBlock.tileData = ts.tileData.clone();
			ncgr.write(target);
		}
	};

	public static final I2DFormatHandler CSNNS_CER = new Default2DFormatHandler(NCER.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NCER.MAGIC);
		}

		@Override
		public Sprite2DResource importFile(FSFile fsf, S2DIOProvider exData) {
			NCER ncer = new NCER(fsf);
			Sprite2DResource res = new Sprite2DResource();
			res.mappingMode = ncer.cellBank.mappingMode;
			// Per NitroPaint NCGR_CHNAME(x, m, b):
			//   tile_idx = NCGR_BYTE_BOUNDARY(mode) * charName / (bitDepth << 3)
			// where NCGR_BYTE_BOUNDARY = 32 << modeShift and modeShift is
			// 0..3 for 1D modes (32K..256K) and 0 for 2D. Expanded:
			//   4bpp: tile_idx = (32 << shift) * x / 32 = x << shift
			//   8bpp: tile_idx = (32 << shift) * x / 64 = x << (shift-1)
			// For 8bpp with shift=0 (2D or 1D_32K) the result is x>>1 —
			// the hardware addresses 8bpp tiles at half-char granularity.
			int mode = ncer.cellBank.mappingMode;
			boolean is2D = (mode == NCERCellBank.MAPPING_MODE_2D);
			int modeShift = (mode >= 0 && mode <= 3) ? mode : 0;
			// mappingMode and cell count preserved for later merge
			for (int i = 0; i < ncer.getCellCount(); i++) {
				NCERCell ncerCell = ncer.getCell(i);
				Sprite2DCell cell = new Sprite2DCell("Cell_" + i);
				cell.vramTransferSrcAddr = ncerCell.vramTransferSrcAddr;
				cell.vramTransferSize = ncerCell.vramTransferSize;
				cell.hasVramTransfer = ncerCell.hasVramTransfer;
				for (NCERCellOAM ncerOam : ncerCell.oams) {
					Sprite2DOAM oam = new Sprite2DOAM();
					oam.x = ncerOam.x;
					oam.y = ncerOam.y;
					oam.width = ncerOam.getWidth();
					oam.height = ncerOam.getHeight();
					// Unified NCGR_CHNAME: for the 2D "mapping" case NitroPaint
					// still runs the same macro (NCGR_BYTE_BOUNDARY(2D)=32), so
					// the answer is literally charName << 0 for 4bpp and
					// charName >> 1 for 8bpp. We treat 2D as modeShift=0 for
					// the formula below.
					int effectiveShift = is2D ? 0 : modeShift;
					if (ncerOam.colorMode256) {
						// 8bpp: tile_idx = charName << (shift-1); for shift=0
						// that's a right shift by 1.
						if (effectiveShift >= 1) {
							oam.tileIndex = ncerOam.tileIndex << (effectiveShift - 1);
						} else {
							oam.tileIndex = ncerOam.tileIndex >> 1;
						}
					} else {
						// 4bpp: tile_idx = charName << shift.
						oam.tileIndex = ncerOam.tileIndex << effectiveShift;
					}
					oam.paletteIndex = ncerOam.paletteIndex;
					oam.flipH = ncerOam.flipH;
					oam.flipV = ncerOam.flipV;
					oam.priority = ncerOam.priority;
					// Affine / double-size: NCER OAMs with rotationScaling
					// may have doubleSize set, which shifts the on-screen
					// content origin by (w/2, h/2). Carry these through so
					// the renderer can position the content correctly.
					oam.rotationScaling = ncerOam.rotationScaling;
					oam.doubleSize = ncerOam.rotationScaling && ncerOam.doubleSize;
					oam.rsParamIndex = ncerOam.rsParamIndex;
					cell.addOAM(oam);
				}
				res.cells.add(cell);
			}
			return res;
		}

		@Override
		public void exportResource(Sprite2DResource res, FSFile target, S2DIOProvider exData) {
			if (res.cells.isEmpty()) {
				return;
			}
			NCER ncer = new NCER();
			ncer.cellBank.mappingMode = res.mappingMode;
			int mode = ncer.cellBank.mappingMode;
			boolean is2D = (mode == NCERCellBank.MAPPING_MODE_2D);
			int modeShift = (mode >= 0 && mode <= 3) ? mode : 0;
			for (Sprite2DCell cell : res.cells) {
				NCERCell ncerCell = new NCERCell();
				// getVisibleOAMs() drops OAMs the user hid via the layer
				// panel — those are editor-only and shouldn't appear in
				// the exported NCER. Opacity < 1.0 is NOT representable
				// in NITRO and is silently treated as fully opaque.
				for (Sprite2DOAM oam : cell.getVisibleOAMs()) {
					NCERCellOAM ncerOam = new NCERCellOAM();
					ncerOam.x = oam.x;
					ncerOam.y = oam.y;
					// Encode width/height into Attr0 shape + Attr1 size via
					// the GBATek lookup. Without this every exported OAM
					// reads back as 8x8 — discovered by NitroExportRoundTripTest.
					ctrmap.formats.ntr.nitro2d.common.GXObjShape gxs =
						ctrmap.formats.ntr.nitro2d.common.GXObjShape.fromDimensions(oam.width, oam.height);
					ncerOam.shape = gxs.shape;
					ncerOam.size = gxs.size;
					// Affine state — must be set before the tileIndex math
					// below since colorMode256 is read there.
					ncerOam.rotationScaling = oam.rotationScaling;
					ncerOam.doubleSize = oam.doubleSize;
					ncerOam.rsParamIndex = oam.rsParamIndex;
					// Inverse of the import formula:
					//   4bpp: charName = tileIdx >> shift
					//   8bpp: charName = tileIdx << 1      (for shift=0)
					//   8bpp: charName = tileIdx >> (shift-1)  (for shift>=1)
					int effectiveShift = is2D ? 0 : modeShift;
					if (ncerOam.colorMode256) {
						if (effectiveShift >= 1) {
							ncerOam.tileIndex = oam.tileIndex >> (effectiveShift - 1);
						} else {
							ncerOam.tileIndex = oam.tileIndex << 1;
						}
					} else {
						ncerOam.tileIndex = oam.tileIndex >> effectiveShift;
					}
					ncerOam.paletteIndex = oam.paletteIndex;
					ncerOam.flipH = oam.flipH;
					ncerOam.flipV = oam.flipV;
					ncerOam.priority = oam.priority;
					ncerCell.addOAM(ncerOam);
				}
				ncer.cellBank.cells.add(ncerCell);
			}
			ncer.write(target);
		}
	};

	public static final I2DFormatHandler CSNNS_ANR = new Default2DFormatHandler(NANR.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NANR.MAGIC);
		}

		@Override
		public Sprite2DResource importFile(FSFile fsf, S2DIOProvider exData) {
			NANR nanr = new NANR(fsf);
			Sprite2DResource res = new Sprite2DResource();
			for (int i = 0; i < nanr.getAnimationCount(); i++) {
				NANRAnimation na = nanr.getAnimation(i);
				Sprite2DCellAnimation anim = new Sprite2DCellAnimation("CellAnim_" + i);
				anim.playMode = mapLoopModeToPlayMode(na.loopMode);
				for (NANRFrame nf : na.frames) {
					Sprite2DAnimFrame frame = new Sprite2DAnimFrame(nf.cellIndex, nf.duration);
					frame.translateX = nf.translateX;
					frame.translateY = nf.translateY;
					if (na.animationType == NANRAnimation.ANIM_TYPE_AFFINE) {
						frame.rotation = nf.rotation * 360.0f / 65536.0f;
						frame.scaleX = nf.scaleW / 4096.0f;
						frame.scaleY = nf.scaleH / 4096.0f;
					}
					anim.frames.add(frame);
				}
				res.cellAnimations.add(anim);
			}
			return res;
		}

		@Override
		public void exportResource(Sprite2DResource res, FSFile target, S2DIOProvider exData) {
			if (res.cellAnimations.isEmpty()) {
				return;
			}
			NANR nanr = new NANR();
			for (Sprite2DCellAnimation anim : res.cellAnimations) {
				NANRAnimation na = new NANRAnimation();
				na.loopMode = mapPlayModeToLoopMode(anim.playMode);
				na.cellType = NANRAnimation.CELL_TYPE_SINGLE;
				boolean hasSRT = false;
				boolean hasTrans = false;
				for (Sprite2DAnimFrame f : anim.frames) {
					if (f.scaleX != 1.0f || f.scaleY != 1.0f || f.rotation != 0) {
						hasSRT = true;
					}
					if (f.translateX != 0 || f.translateY != 0) {
						hasTrans = true;
					}
				}
				if (hasSRT) {
					na.animationType = NANRAnimation.ANIM_TYPE_AFFINE;
				} else if (hasTrans) {
					na.animationType = NANRAnimation.ANIM_TYPE_TRANSLATE;
				} else {
					na.animationType = NANRAnimation.ANIM_TYPE_CELL;
				}
				for (Sprite2DAnimFrame f : anim.frames) {
					NANRFrame nf = new NANRFrame();
					nf.cellIndex = f.cellIndex;
					nf.duration = f.duration;
					nf.translateX = (short) f.translateX;
					nf.translateY = (short) f.translateY;
					if (na.animationType == NANRAnimation.ANIM_TYPE_AFFINE) {
						nf.rotation = (int) (f.rotation / 360.0f * 65536.0f);
						nf.scaleW = (int) (f.scaleX * 4096.0f);
						nf.scaleH = (int) (f.scaleY * 4096.0f);
					}
					na.frames.add(nf);
				}
				nanr.animBank.animations.add(na);
			}
			nanr.write(target);
		}
	};

	public static final I2DFormatHandler CSNNS_MCR = new Default2DFormatHandler(NMCR.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NMCR.MAGIC);
		}

		@Override
		public Sprite2DResource importFile(FSFile fsf, S2DIOProvider exData) {
			NMCR nmcr = new NMCR(fsf);
			Sprite2DResource res = new Sprite2DResource();
			for (int i = 0; i < nmcr.getMultiCellCount(); i++) {
				NMCRMultiCell mc = nmcr.getMultiCell(i);
				Sprite2DMultiCell smc = new Sprite2DMultiCell("MultiCell_" + i);
				for (NMCRMultiCellProperty p : mc.properties) {
					Sprite2DMultiCell.MultiCellEntry entry = new Sprite2DMultiCell.MultiCellEntry(
						p.indexSequence, p.translateX, p.translateY
					);
					smc.entries.add(entry);
				}
				res.multiCells.add(smc);
			}
			return res;
		}

		@Override
		public void exportResource(Sprite2DResource res, FSFile target, S2DIOProvider exData) {
			if (res.multiCells.isEmpty()) {
				return;
			}
			NMCR nmcr = new NMCR();
			int uniqueCounter = 0;
			for (Sprite2DMultiCell smc : res.multiCells) {
				NMCRMultiCell mc = new NMCRMultiCell();
				// getVisibleEntries() mirrors Sprite2DCell.getVisibleOAMs()
				// — drops entries the user hid via the layer panel.
				List<Sprite2DMultiCell.MultiCellEntry> visible = smc.getVisibleEntries();
				int cnt = visible.size();
				mc.numberDisplayedCells = cnt;
				mc.numberLoadedCells = cnt;
				for (Sprite2DMultiCell.MultiCellEntry entry : visible) {
					NMCRMultiCellProperty p = new NMCRMultiCellProperty();
					p.indexSequence = entry.animIndex;
					p.translateX = (short) entry.x;
					p.translateY = (short) entry.y;
					p.frameMode = NMCRMultiCellProperty.FRAME_MODE_RESET;
					p.uniqueID = (uniqueCounter++) & 0xFF;
					mc.properties.add(p);
				}
				nmcr.multiCellBank.multiCells.add(mc);
			}
			nmcr.write(target);
		}
	};

	public static final I2DFormatHandler CSNNS_MAR = new Default2DFormatHandler(NMAR.EXTENSION_FILTER) {
		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(NMAR.MAGIC);
		}

		@Override
		public Sprite2DResource importFile(FSFile fsf, S2DIOProvider exData) {
			NMAR nmar = new NMAR(fsf);
			Sprite2DResource res = new Sprite2DResource();
			for (int i = 0; i < nmar.getAnimationCount(); i++) {
				NMARAnimation na = nmar.getAnimation(i);
				Sprite2DMultiCellAnimation anim = new Sprite2DMultiCellAnimation("MultiCellAnim_" + i);
				anim.playMode = mapLoopModeToPlayMode(na.loopMode);
				for (NMARFrame nf : na.frames) {
					Sprite2DMultiCellAnimation.MultiCellAnimFrame frame =
						new Sprite2DMultiCellAnimation.MultiCellAnimFrame(nf.multiCellIndex, nf.duration);
					anim.frames.add(frame);
				}
				res.multiCellAnimations.add(anim);
			}
			return res;
		}

		@Override
		public void exportResource(Sprite2DResource res, FSFile target, S2DIOProvider exData) {
			if (res.multiCellAnimations.isEmpty()) {
				return;
			}
			NMAR nmar = new NMAR();
			for (Sprite2DMultiCellAnimation anim : res.multiCellAnimations) {
				NMARAnimation na = new NMARAnimation();
				na.loopMode = mapPlayModeToLoopMode(anim.playMode);
				na.cellType = NMARAnimation.CELL_TYPE_MULTI;
				na.animationType = NMARAnimation.ANIM_TYPE_CELL;
				for (Sprite2DMultiCellAnimation.MultiCellAnimFrame f : anim.frames) {
					NMARFrame nf = new NMARFrame();
					nf.multiCellIndex = f.multiCellIndex;
					nf.duration = f.duration;
					na.frames.add(nf);
				}
				nmar.animBank.animations.add(na);
			}
			nmar.write(target);
		}
	};

	@Override
	public void registerFormats(NGCS2DJulietIface j) {
		j.registFormatSupport(CS2DIOContentType.PALETTE, CSNNS_CLR);
		j.registFormatSupport(CS2DIOContentType.TILE_SHEET, CSNNS_CGR);
		j.registFormatSupport(CS2DIOContentType.CELL, CSNNS_CER);
		j.registFormatSupport(CS2DIOContentType.CELL_ANIMATION, CSNNS_ANR);
		j.registFormatSupport(CS2DIOContentType.MULTI_CELL, CSNNS_MCR);
		j.registFormatSupport(CS2DIOContentType.MULTI_CELL_ANIMATION, CSNNS_MAR);
	}

	@Override
	public void registerUI(NGCS2DJulietIface j, Frame uiParent, ctrmap.creativestudio.ngcs2d.rtldr.NGCS2DContentAccessor contentAccessor) {
		// Future: Add NNS-specific export menu items
	}

	/**
	 * Maps a NITRO ABNK loopMode (1=stop, 2=repeat, 3=backward, 4=ping-pong)
	 * into the Sprite2D playMode convention (1=forward, 2=forward-loop,
	 * 3=reverse, 4=reverse-loop).
	 */
	private static int mapLoopModeToPlayMode(int loopMode) {
		switch (loopMode) {
			case NANRAnimation.LOOP_STOP:     return 1;
			case NANRAnimation.LOOP_REPEAT:   return 2;
			case NANRAnimation.LOOP_BACKWARD: return 3;
			case NANRAnimation.LOOP_PINGPONG: return 4;
			default:                          return 2;
		}
	}

	/**
	 * Inverse of {@link #mapLoopModeToPlayMode}.
	 */
	private static int mapPlayModeToLoopMode(int playMode) {
		switch (playMode) {
			case 1: return NANRAnimation.LOOP_STOP;
			case 2: return NANRAnimation.LOOP_REPEAT;
			case 3: return NANRAnimation.LOOP_BACKWARD;
			case 4: return NANRAnimation.LOOP_PINGPONG;
			default: return NANRAnimation.LOOP_REPEAT;
		}
	}
}
