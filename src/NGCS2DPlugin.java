
import ctrmap.creativestudio.ngcs2d.rtldr.NGCS2DJulietIface;
import ctrmap.creativestudio.nitro2dplugin.Gen5NGCS2DPlugin;
import rtldr.JRTLDRCore;
import ctrmap.creativestudio.ngcs2d.rtldr.INGCS2DPlugin;

public class NGCS2DPlugin implements INGCS2DPlugin {

	@Override
	public void attach(NGCS2DJulietIface j) {
		loadSubPlugin(Gen5NGCS2DPlugin.class);
	}

	private static void loadSubPlugin(Class<? extends INGCS2DPlugin> cls) {
		try {
			INGCS2DPlugin p = cls.newInstance();
			if (p != null) {
				JRTLDRCore.loadExtension(NGCS2DJulietIface.getInstance(), p);
			}
		} catch (Throwable t) {

		}
	}
}
