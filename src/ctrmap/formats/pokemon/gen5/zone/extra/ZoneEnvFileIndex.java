package ctrmap.formats.pokemon.gen5.zone.extra;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZoneEnvFileIndex {

	private FSFile source;

	private Map<Integer, Integer> entryMap = new HashMap<>();

	public ZoneEnvFileIndex(FSFile fsf) {
		try (DataInStream in = fsf.getDataInputStream()) {
			int len = in.getLength() >> 2;
			for (int i = 0; i < len; i++) {
				entryMap.put(in.readUnsignedShort(), in.readUnsignedShort());
			}

			source = fsf;
		} catch (IOException ex) {
			Logger.getLogger(ZoneEnvFileIndex.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void write() {
		if (source != null) {
			try {
				DataOutStream out = source.getDataOutputStream();

				List<Map.Entry<Integer, Integer>> list = new ArrayList<>(entryMap.entrySet());
				list.sort(Map.Entry.comparingByKey());

				for (Map.Entry<Integer, Integer> e : list) {
					out.writeShort(e.getKey());
					out.writeShort(e.getValue());
				}

				out.close();
			} catch (IOException ex) {
				Logger.getLogger(ZoneEnvFileIndex.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public boolean setFileIDForZone(int zone, int datId) {
		int now = getFileIDForZone(zone);
		if (datId != now) {
			if (datId == -1) {
				entryMap.remove(zone);
			} else {
				entryMap.put(zone, datId);
			}
			return true;
		}
		return false;
	}

	public int getFileIDForZone(int zone) {
		return entryMap.getOrDefault(zone, -1);
	}
}
