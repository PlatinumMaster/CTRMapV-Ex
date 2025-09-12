package ctrmap.missioncontrol_ntr.sequence;

import ctrmap.missioncontrol_base.debug.IMCDebuggable;
import ctrmap.renderer.scene.Camera;

public class VSequenceCameraController implements IMCDebuggable<VSequenceCameraDebugger> {

	private VSequenceCameraDebugger freeCamSrc;

	private final VSequenceController controller;

	public VSequenceCameraController(VSequenceController cnt) {
		controller = cnt;
		attachFreeCamDebugger();
	}

	public void attachFreeCamDebugger() {
		controller.getMC().getDebuggerManager().registDebuggable(this);
	}

	public void update() {
		boolean fc = isUsingDebugCamera();
		if (!fc && freeCamSrc != null) {
			freeCamSrc.deactivate();
		}
		if (controller.getCurrentPlayer() != null) {
			controller.getCurrentPlayer().setCameraEnabled(!fc);
			if (!fc) {
				Camera seqCamera = controller.getCurrentPlayer().getCamera();
				controller.getMC().getDebuggerManager().callDebuggers(this, (debugger) -> {
					debugger.adjustToSeqCamera(seqCamera);
				});
			}
		}
		if (fc && freeCamSrc != null) {
			freeCamSrc.activate();
		}
	}

	public boolean isUsingDebugCamera() {
		return freeCamSrc != null && freeCamSrc.isDebugCameraEnabled();
	}

	@Override
	public Class<VSequenceCameraDebugger> getDebuggerClass() {
		return VSequenceCameraDebugger.class;
	}

	@Override
	public void attach(VSequenceCameraDebugger dcc) {
		if (freeCamSrc == dcc) {
			return;
		}
		if (freeCamSrc != null) {
			freeCamSrc.deactivate();
		}
		if (dcc != null) {
			freeCamSrc = dcc;
		} else {
			freeCamSrc = null;
		}
		if (freeCamSrc != null) {
			freeCamSrc.addSceneTarget(controller.getScene());
			freeCamSrc.onAttach(controller);
		}
	}

	@Override
	public void detach(VSequenceCameraDebugger debugger) {
		debugger.deactivate();
	}

	@Override
	public void destroy(VSequenceCameraDebugger debugger) {
		debugger.deactivate();
	}
}
