package ctrmap.missioncontrol_ntr.sequence;

import ctrmap.formats.pokemon.gen5.sequence.Sequence;
import ctrmap.missioncontrol_base.McLogger;
import ctrmap.missioncontrol_base.debug.IMCDebuggable;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.VMCModuleLogSource;
import ctrmap.missioncontrol_ntr.sequence.player.SequencePlayer;
import ctrmap.renderer.scene.Scene;

public class VSequenceController implements IMCDebuggable<VSequencePlaybackDebugger> {

	private final VLaunchpad mc;

	private Scene rootScene = new Scene("SequenceController");

	private SequencePlayer currentPlayer;

	private VSequenceCameraController cameraController;

	private McLogger log;

	public VSequenceController(VLaunchpad mc) {
		this.mc = mc;
		log = mc.log.createSubLogger(VMCModuleLogSource.SEQUENCE);
		cameraController = new VSequenceCameraController(this);
		rootScene.addSceneAnimationCallback((frameAdvance) -> {
			cameraController.update();
		});
		mc.getDebuggerManager().registDebuggable(this);
	}

	public Scene getScene() {
		return rootScene;
	}

	public VSequenceCameraController getCameraController() {
		return cameraController;
	}

	public VLaunchpad getMC() {
		return mc;
	}

	public void stopSequence() {
		if (currentPlayer != null) {
			currentPlayer.removeFromScene(rootScene);
			currentPlayer = null;
		}
	}

	public void playSequence(Sequence sequence, int userParam) {
		stopSequence();
		currentPlayer = new SequencePlayer(this, sequence, userParam, log);
		currentPlayer.addToScene(rootScene);
		mc.getDebuggerManager().callDebuggers(this, (debugger) -> {
			debugger.onSeqPlaybackStarted(currentPlayer);
		});
	}

	public void callOnPlaybackAdvance(SequencePlayer player) {
		mc.getDebuggerManager().callDebuggers(this, (debugger) -> {
			debugger.onPlaybackAdvance(player);
		});
	}
	
	public SequencePlayer getCurrentPlayer() {
		return currentPlayer;
	}

	public void open() {
		mc.mcScene.addChild(rootScene);
	}

	public void close() {
		stopSequence();
		mc.mcScene.removeChild(rootScene);
	}

	@Override
	public Class<VSequencePlaybackDebugger> getDebuggerClass() {
		return VSequencePlaybackDebugger.class;
	}

	@Override
	public void attach(VSequencePlaybackDebugger debugger) {
		debugger.attach(this);
	}

	@Override
	public void detach(VSequencePlaybackDebugger debugger) {
		debugger.attach(null);
	}

	@Override
	public void destroy(VSequencePlaybackDebugger debugger) {
		debugger.attach(null);
	}
}
