package ctrmap.formats.ntr.nitroreader.common;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitroreader.nsbca.NSBCA;
import ctrmap.formats.ntr.nitroreader.nsbma.NSBMA;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMD;
import ctrmap.formats.ntr.nitroreader.nsbta.NSBTA;
import ctrmap.formats.ntr.nitroreader.nsbtp.NSBTP;
import ctrmap.formats.ntr.nitroreader.nsbtx.NSBTX;
import ctrmap.formats.ntr.nitroreader.nsbva.NSBVA;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;

public class NNSG3DResource {

	protected NNSG3DResourceHeader header;

	protected final void readBase(NTRDataIOStream io) throws IOException {
		header = new NNSG3DResourceHeader(io);
	}

	protected final boolean seekBlock(NTRDataIOStream io, int index) throws IOException {
		if (index < header.offsets.length) {
			io.seek(header.offsets[index]);
			return true;
		}
		return false;
	}
	
	public static NNSG3DResource read(FSFile file) {
		try (NTRDataIOStream io = new NTRDataIOStream(file)) {
			String magic = io.readPaddedString(4);
			io.seek(0);
			
			switch (magic) {
				case NSBMD.MAGIC:
					return new NSBMD(io);
				case NSBTX.MAGIC:
					return new NSBTX(io);
				case NSBCA.MAGIC:
					return new NSBCA(io);
				case NSBTA.MAGIC:
					return new NSBTA(io);
				case NSBTP.MAGIC:
					return new NSBTP(io);
				case NSBMA.MAGIC:
					return new NSBMA(io);
				case NSBVA.MAGIC:
					return new NSBVA(io);
				default:
					System.out.println("Warn: unknown NNS resource magic " + magic);
			}
			
			return null;
		} catch (IOException ex) {
			Logger.getLogger(NNSG3DResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
}
