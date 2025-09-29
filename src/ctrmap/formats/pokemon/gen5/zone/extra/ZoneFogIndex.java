package ctrmap.formats.pokemon.gen5.zone.extra;

import xstandard.fs.FSFile;

public class ZoneFogIndex extends ZoneEnvFileIndex {

	public ZoneFogIndex(FSFile fsf) {
		super(fsf);
	}

	public boolean setFogForZone(int zone, int fog) {
		return super.setFileIDForZone(zone, fog);
	}

	public int getFogForZone(int zone) {
		return super.getFileIDForZone(zone);
	}
}
