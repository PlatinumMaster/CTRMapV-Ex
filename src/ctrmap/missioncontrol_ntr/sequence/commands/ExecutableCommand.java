
package ctrmap.missioncontrol_ntr.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.commands.SeqCommandObject;
import ctrmap.missioncontrol_ntr.sequence.player.SequencePlayer;

public abstract class ExecutableCommand<T extends SeqCommandObject> {

	protected final T param;
	
	public ExecutableCommand(T param) {
		this.param = param;
	}
	
	public abstract void execute(SequencePlayer player, int eventCode);
}
