package ctrmap.editor.gui.editors.gen5.sequence;

import ctrmap.missioncontrol_base.DebugCameraController;
import ctrmap.missioncontrol_ntr.sequence.VSequenceCameraDebugger;
import ctrmap.missioncontrol_ntr.sequence.VSequenceController;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import ctrmap.util.gui.cameras.OrthoCameraInputManager;
import xstandard.math.MathEx;
import xstandard.math.vec.Vec3f;

public class VSequenceDebugCamera implements VSequenceCameraDebugger {

	public VSequenceEditor edt;

	private VSequenceController ctrl;

	public VSequenceDebugCamera(VSequenceEditor e) {
		edt = e;
	}

	@Override
	public Camera getDebuggerCamera() {
		return edt.dcc.getDebugCamera();
	}

	@Override
	public void adjustToSeqCamera(Camera seqCam) {
		Camera cam3d = edt.m3DInput.fpsCamera.cam;

		cam3d.translation.set(seqCam.translation);
		Vec3f rotation = cam3d.rotation.clone();
		if (seqCam.viewMode == Camera.ViewMode.LOOK_AT) {
			seqCam.getTransformMatrix(false).getRotationTo(rotation);
			rotation.mul(MathEx.RADIANS_TO_DEGREES);
		} else if (seqCam.viewMode == Camera.ViewMode.ROTATE) {
			rotation.set(seqCam.rotation);
		}
		cam3d.rotation.x = MathEx.clamp(-90f, 90f, rotation.x % 360f);
		cam3d.rotation.y = rotation.y;

		OrthoCameraInputManager ortho = edt.tilemapInput.orthoCam;
		boolean am = ortho.getAllowMotion();
		ortho.setAllowMotion(true);
		ortho.setCenter(seqCam.translation.x, seqCam.translation.z);
		ortho.setZoom(seqCam.translation.y);
		ortho.setAllowMotion(am);

		for (DebugCameraController c : edt.dcc.camControllers()) {
			Camera debugCam = c.getDebugCamera();
			debugCam.zNear = seqCam.zNear;
			debugCam.zFar = seqCam.zFar;
			debugCam.FOV = seqCam.FOV;
		}
	}

	@Override
	public void onAttach(VSequenceController ctrl) {
		edt.dcc.onControllerActivated();

		this.ctrl = ctrl;
	}

	@Override
	public void addSceneTarget(Scene scn) {
		edt.dcc.addSceneTarget(scn);
	}

	@Override
	public void activate() {
		edt.dcc.activateCamera();
	}

	@Override
	public void deactivate() {
		edt.dcc.deactivateCamera();
	}

	@Override
	public boolean isDebugCameraEnabled() {
		return edt.dcc.getDebugCameraEnabled();
	}
}
