
package ctrmap.editor.gui.editors.gen5.sequence;

import ctrmap.editor.CTRMap;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.editor.gui.editors.common.input.DCCManager;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.common.collision.ICollisionProvider;
import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.sequence.VSequenceController;
import ctrmap.renderer.scene.Scene;
import java.util.List;
import xstandard.util.ArraysEx;

public class VSequenceEditor extends AbstractPerspective {
	
	private final VSequenceDebugCamera debugCamera;

	public VSequenceEditor(CTRMap cm) {
		super(cm);
		debugCamera = new VSequenceDebugCamera(this);
	}
	
	public VLaunchpad getMC() {
		return ctrmap.getMissionControl(VLaunchpad.class);
	}
	
	public VSequenceController getSequence() {
		return getMC().sequence;
	}

	@Override
	public boolean isGameSupported(GameInfo game) {
		return game.isGenV();
	}

	@Override
	public void onDCCCameraChanged() {
		if (getMC().sequence != null) {
			getMC().sequence.getCameraController().attach(debugCamera);
		}
	}
	
	@Override
	public List<? extends IMCDebugger> getExtraDebuggers() {
		return ArraysEx.asList(debugCamera);
	}

	@Override
	public String getName() {
		return "Sequence editor";
	}

	@Override
	public Scene getInjectionScene() {
		return getSequence().getScene();
	}

	@Override
	public ICollisionProvider getWorldCollisionProvider() {
		return null;
	}
	
	@Override
	public void onEditorActivated() {
		super.onEditorActivated();
		getSequence().open();
		dcc.setDebugCamera(DCCManager.CAMERA_CONTROLLER_ID_3D);
	}

	@Override
	public void onEditorDeactivated() {
		getSequence().close();
		super.onEditorDeactivated();
	}
}
