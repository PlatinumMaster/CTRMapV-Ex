package ctrmap.missioncontrol_ntr.sequence.commands;

import ctrmap.formats.pokemon.gen5.sequence.commands.*;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutableCommandFactory {

	private static final Map<Class<? extends SeqCommandObject>, Class<? extends ExecutableCommand<?>>> classMap = new HashMap<>();

	static {
		classMap.put(CommandScrFade.class, ExecScrFade.class);
		classMap.put(CommandScrFadeEx.class, ExecScrFadeEx.class);
		classMap.put(CommandCameraSetProjection.class, ExecCameraSetProjection.class);
		classMap.put(CommandLightColorChange.class, ExecLightColorChange.class);
		classMap.put(CommandLightVectorChange.class, ExecLightVectorChange.class);
	}

	public static <T extends SeqCommandObject> ExecutableCommand<T> create(T command) {
		Class<? extends ExecutableCommand<T>> clazz = (Class<? extends ExecutableCommand<T>>) classMap.get(command.getClass());
		if (clazz == null) {
			return null;
		}
		try {
			Constructor<? extends ExecutableCommand<T>> ctor = clazz.getConstructor(command.getClass());
			return ctor.newInstance(command);
		} catch (ReflectiveOperationException ex) {
			Logger.getLogger(ExecutableCommandFactory.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
}
