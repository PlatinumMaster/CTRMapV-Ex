package ctrmap.missioncontrol_ntr.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.commands.CommandScrFadeEx;
import ctrmap.missioncontrol_base.util.Fade;
import ctrmap.missioncontrol_ntr.sequence.player.SequencePlayer;

public class ExecScrFadeEx extends ExecutableCommand<CommandScrFadeEx> {

	public ExecScrFadeEx(CommandScrFadeEx param) {
		super(param);
	}

	@Override
	public void execute(SequencePlayer player, int eventCode) {
		Fade fade = player.getFade();

		if (eventCode == 0) {
			fade.set(ExecScrFade.convertFadeColor(param.srcColor), ExecScrFade.convertFadeColor(param.fadeColor), param.fadeOutDuration);
			fade.setVisible(true);
			player.rescheduleWithEventCode(this, param.fadeOutDuration, 1);
		} else {
			fade.set(ExecScrFade.convertFadeColor(param.fadeColor), ExecScrFade.convertFadeColor(param.dstColor), param.fadeInDuration);
		}
	}
}
