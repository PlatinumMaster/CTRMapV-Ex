package ctrmap.formats.pokemon.gen5.zone.extra;

import xstandard.fs.FSFile;

public class ZoneStaticLightIndex extends ZoneEnvFileIndex {

	public ZoneStaticLightIndex(FSFile fsf) {
		super(fsf);
	}

	public boolean setLightsForZone(int zone, int fog) {
		return super.setFileIDForZone(zone, fog);
	}

	public int getLightsForZone(int zone) {
		return super.getFileIDForZone(zone);
	}
}
