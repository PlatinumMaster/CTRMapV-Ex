
package ctrmap.missioncontrol_ntr.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.commands.CommandLightColorChange;
import ctrmap.missioncontrol_ntr.sequence.player.SequencePlayer;

public class ExecLightColorChange extends ExecutableCommand<CommandLightColorChange> {

	public ExecLightColorChange(CommandLightColorChange param) {
		super(param);
	}

	@Override
	public void execute(SequencePlayer player, int eventCode) {
		player.overrideLight(param.lightId, (t) -> {
			t.ambientColor = param.getColor();
			t.diffuseColor.set(t.ambientColor);
		});
	}
}
