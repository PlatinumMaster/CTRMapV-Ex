package ctrmap.missioncontrol_ntr.sequence;

import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;

public interface VSequenceCameraDebugger extends IMCDebugger {
	public void addSceneTarget(Scene scn);
	public void activate();
	public void deactivate();
	public boolean isDebugCameraEnabled();
	public Camera getDebuggerCamera();
	public void adjustToSeqCamera(Camera seqCamera);
	public void onAttach(VSequenceController ctrl);
}
