
package ctrmap.missioncontrol_ntr.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.commands.CommandLightVectorChange;
import ctrmap.missioncontrol_ntr.field.VFieldLightSystem;
import ctrmap.missioncontrol_ntr.sequence.player.SequencePlayer;

public class ExecLightVectorChange extends ExecutableCommand<CommandLightVectorChange> {

	public ExecLightVectorChange(CommandLightVectorChange param) {
		super(param);
	}

	@Override
	public void execute(SequencePlayer player, int eventCode) {
		player.overrideLight(param.lightId, (t) -> {
			t.direction = param.getVector();
			VFieldLightSystem.corruptFixedPointLightVector(t.direction);
		});
	}
}
