
package ctrmap.missioncontrol_ntr.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.commands.CommandCameraSetProjection;
import ctrmap.missioncontrol_ntr.sequence.player.SequencePlayer;
import ctrmap.renderer.scene.Camera;

public class ExecCameraSetProjection extends ExecutableCommand<CommandCameraSetProjection> {

	public ExecCameraSetProjection(CommandCameraSetProjection param) {
		super(param);
	}

	@Override
	public void execute(SequencePlayer player, int eventCode) {
		Camera cam = player.getCamera();
		cam.FOV = player.adjustFovY(param.fovY);
	}
}
