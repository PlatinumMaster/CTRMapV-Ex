package ctrmap.missioncontrol_ntr.sequence.player;

import ctrmap.formats.ntr.common.gfx.GXColorAlpha;
import ctrmap.formats.ntr.nitroreader.common.NNSG3DResource;
import ctrmap.formats.ntr.nitroreader.nsbca.NSBCA;
import ctrmap.formats.ntr.nitroreader.nsbma.NSBMA;
import ctrmap.formats.ntr.nitroreader.nsbmd.NSBMD;
import ctrmap.formats.ntr.nitroreader.nsbta.NSBTA;
import ctrmap.formats.ntr.nitroreader.nsbtp.NSBTP;
import ctrmap.formats.ntr.nitroreader.nsbva.NSBVA;
import ctrmap.formats.pokemon.gen5.area.AreaLightFile;
import ctrmap.formats.pokemon.gen5.area.VAreaHeader;
import ctrmap.formats.pokemon.gen5.gfbca.GFBCA;
import ctrmap.formats.pokemon.gen5.sequence.SeqParameters;
import ctrmap.formats.pokemon.gen5.sequence.SeqResources;
import ctrmap.formats.pokemon.gen5.sequence.Sequence;
import ctrmap.formats.pokemon.gen5.sequence.commands.CommandScrFade;
import ctrmap.formats.pokemon.gen5.sequence.commands.SeqCommandObject;
import ctrmap.missioncontrol_base.McLogger;
import ctrmap.missioncontrol_base.util.Fade;
import ctrmap.missioncontrol_ntr.VLaunchpad;
import ctrmap.missioncontrol_ntr.VRTC;
import ctrmap.missioncontrol_ntr.field.VFieldController;
import ctrmap.missioncontrol_ntr.field.VFieldLightSystem;
import ctrmap.missioncontrol_ntr.field.VPlayerController;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import ctrmap.missioncontrol_ntr.sequence.VSequenceController;
import ctrmap.missioncontrol_ntr.sequence.commands.ExecScrFade;
import ctrmap.missioncontrol_ntr.sequence.commands.ExecutableCommand;
import ctrmap.missioncontrol_ntr.sequence.commands.ExecutableCommandFactory;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.scenegraph.SceneAnimationCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import xstandard.fs.FSFile;
import xstandard.math.vec.RGBA;

public class SequencePlayer implements SceneAnimationCallback {

	private final VSequenceController parent;
	private final McLogger logger;

	private final Sequence sequence;

	private final int userParam;

	private Camera camera;
	private final Scene scene;
	private final VFieldLightSystem lightSystem;
	private boolean hasCustomLights = false;
	private final Fade fade;

	private float frame = 0f;
	private float frameCount;

	private State state = State.PRELOAD;

	private SortedSet<ScheduledCommand> scheduledCommands = new TreeSet<>();

	public SequencePlayer(VSequenceController controller, Sequence sequence, int userParam, McLogger logger) {
		this.parent = controller;
		this.logger = logger;
		this.sequence = sequence;
		this.userParam = userParam;
		scene = new Scene("Sequence" + sequence.seqId);
		scene.addSceneAnimationCallback(this);
		enqueueCommands(sequence.playbackCommands);
		lightSystem = new VFieldLightSystem(parent.getMC().fs);
		fade = new Fade();
		loadScene();
	}

	public Sequence getSequence() {
		return sequence;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCameraEnabled(boolean cameraEnabled) {
		if (cameraEnabled) {
			scene.instantiateCamera(camera);
		} else {
			scene.deinstantiateCamera(camera);
		}
	}

	public Fade getFade() {
		return fade;
	}

	public boolean isPaused() {
		return state == State.PAUSED;
	}

	public boolean isEnded() {
		return state == State.ENDED;
	}

	public float getFrame() {
		return frame;
	}

	public float getFrameCount() {
		return frameCount;
	}

	private void useCustomLights() {
		if (hasCustomLights) {
			return;
		}

		hasCustomLights = true;
		lightSystem.updateScene(scene);
	}

	public void overrideLight(int lightId, Consumer<Light> apply) {
		useCustomLights();
		if (lightId < scene.lights.size()) {
			apply.accept(scene.lights.get(lightId));
		}
	}

	public void scheduleCommand(SeqCommandObject command, int eventCode) {
		ExecutableCommand<?> exec = ExecutableCommandFactory.create(command);
		if (exec != null) {
			scheduleCommand(exec, command.startFrame, eventCode);
		}
	}
	
	public void scheduleCommand(ExecutableCommand<?> command, int atFrame, int eventCode) {
		scheduledCommands.add(new ScheduledCommand(command, atFrame, eventCode));
	}

	public void rescheduleWithEventCode(ExecutableCommand<?> command, int afterFrames, int eventCode) {
		scheduleCommand(command, (int) frame + afterFrames, eventCode);
	}

	private void update(float delta) {
		if (!hasCustomLights) {
			lightSystem.updateScene(scene);
		}
		switch (state) {
			case PRELOAD:
				logger.out("Starting playback of sequence " + sequence.seqId);
				fade.setVisible(false);
				scene.playAnimations(scene.getAllCameraAnimations());
				for (G3DResourceInstance model : scene.getChildren()) {
					model.playAnimations(model.getAllAnimations());
				}
				for (AbstractAnimationController anm : scene.getThisAndChildrenAnimationControllers()) {
					if (!anm.animeList.isEmpty() && anm.animeList.peek().frameCount >= frameCount) {
						anm.loop = false;
					}
				}
				applyPausedState(true);
				CommandScrFade startFade = createExtFadeCommand(sequence.params.startFade, false);
				if (startFade != null) {
					runCommand(new ExecScrFade(startFade), 0);
				}
				updateCommands(0);
				CommandScrFade endFade = createExtFadeCommand(sequence.params.endFade, true);
				if (endFade != null) {
					scheduleCommand(new ExecScrFade(endFade), endFade.startFrame, 0);
				}
				state = State.START;
				break;
			case START:
				applyPausedState(false);
				updateCommands(delta);
				state = State.RUN;
				break;
			case PAUSED:
				break;
			case RUN:
				if (!updateCommands(delta)) {
					state = State.END;
				}
				parent.callOnPlaybackAdvance(this);
				break;
			case END:
				logger.out("Playback finished.");
				runEndCommands();
				state = State.ENDED;
				break;
		}
	}

	private void applyPausedState(boolean paused) {
		for (AbstractAnimationController anim : scene.getThisAndChildrenAnimationControllers()) {
			if (paused) {
				anim.pauseAnimation();
			} else {
				anim.resumeAnimation();
			}
		}
	}

	private CommandScrFade createExtFadeCommand(SeqParameters.FadeConfig fadeConfig, boolean isOut) {
		if (fadeConfig.duration == 0) {
			return null;
		}
		CommandScrFade cmd = new CommandScrFade();
		cmd.startFrame = isOut ? (int) frameCount - fadeConfig.duration : 0;
		cmd.mode = CommandScrFade.ScreenFadeMode.BOTH;
		cmd.duration = fadeConfig.duration;
		int color = fadeConfig.isWhite ? 16 : -16;
		if (isOut) {
			cmd.srcColor = 0;
			cmd.fadeColor = color;
		} else {
			cmd.srcColor = color;
			cmd.fadeColor = 0;
		}
		return cmd;
	}

	public void setPaused(boolean paused) {
		if (state == State.RUN && paused) {
			state = State.PAUSED;
			applyPausedState(paused);
		} else if (state == State.PAUSED && !paused) {
			state = State.RUN;
			applyPausedState(paused);
		}
	}

	private void enqueueCommands(List<SeqCommandObject> commands) {
		for (SeqCommandObject command : commands) {
			scheduleCommand(command, userParam);
		}
	}

	private boolean updateCommands(float delta) {
		frame += delta;

		while (!scheduledCommands.isEmpty()) {
			ScheduledCommand command = scheduledCommands.first();
			if (frame < command.frame) {
				break;
			}
			runCommand(command.command, command.eventCode);
			scheduledCommands.remove(command);
		}

		return frame < frameCount;
	}

	private void runEndCommands() {
		for (SeqCommandObject command : sequence.endCommands) {
			ExecutableCommand<?> executable = ExecutableCommandFactory.create(command);
			if (executable != null) {
				runCommand(executable, 0);
			}
		}
	}

	private void runCommand(ExecutableCommand<?> command, int eventCode) {
		command.execute(this, eventCode);
	}

	private void loadScene() {
		scene.addChild(fade);
		createSceneCamera();
		loadCameraCurve();

		for (SeqResources.ResourceConfig resource : sequence.resources.resources) {
			if (resource.resourceBundles.isEmpty()) {
				continue;
			}

			int bundleIndex = decideResourceBundle(resource.bundleSelectionType);
			if (bundleIndex >= resource.resourceBundles.size()) {
				bundleIndex = 0;
			}

			List<SeqResources.ResourceAnimationGroup> resAnimGroups = resource.resourceBundles.get(bundleIndex).resAnimGroups;
			if (resAnimGroups.isEmpty()) {
				continue;
			}

			int dayPartIndex = VRTC.DayPart.getRTC().ordinal();
			if (dayPartIndex >= resAnimGroups.size()) {
				dayPartIndex = 0;
			}

			addSceneResource(loadResourceGroup(resAnimGroups.get(dayPartIndex)));
		}

		initializeLightSystem();
	}

	public float adjustFovY(float fov) {
		if (sequence.params.renderState.isDual3DRender) {
			//2f - cover both screens
			//1.2f - compensate for the gap between NDS screens
			return fov * 2f * 1.2f;
		} else {
			return fov;
		}
	}

	private void createSceneCamera() {
		SeqParameters.CameraConfig camConfig = sequence.params.camera;

		camera = new Camera();
		camera.FOV = adjustFovY(camConfig.fovY);
		camera.zNear = camConfig.zNear / camConfig.ndcRange;
		camera.zFar = camConfig.zFar / camConfig.ndcRange;
		camera.aspect = camConfig.aspectRatio;

		setCameraEnabled(true);
	}

	private void loadCameraCurve() {
		int resid = sequence.params.camera.animeID;

		FSFile file = parent.getMC().fs.NARCGet(NARCRef.DEMO3D_RESOURCE, resid);
		if (!file.exists()) {
			System.out.println("Warn: camera resource invalid");
			return;
		}

		GFBCA curve = new GFBCA(file);
		G3DResource animation = curve.toGenericCameraAnimation();

		scene.merge(animation);
		frameCount = curve.frameCount;
	}

	private void initializeLightSystem() {
		VLaunchpad mc = parent.getMC();
		VFieldController field = mc.field;

		int zoneId = sequence.params.fieldZoneID;
		NARCRef lightsNarc = NARCRef.COMMON_LIGHTS;
		int lightsId = field.staticLightIndex.getFileIDForZone(zoneId);

		if (lightsId == -1) {
			int areaId = field.zoneTable.getHeader(zoneId).areaID;

			VAreaHeader areaHeader = field.areaTable.getHeader(areaId);
			if (areaHeader == null) {
				return;
			}

			lightsId = areaHeader.lightIndex;
			lightsNarc = NARCRef.FIELD_ENV_LIGHTS;
		}

		AreaLightFile lightData = new AreaLightFile(mc.fs.NARCGet(lightsNarc, lightsId));
		injectClearColors(lightData);

		lightSystem.setSeason(mc.season);
		lightSystem.loadLights(lightData);
	}

	private void injectClearColors(AreaLightFile lights) {
		GXColorAlpha color = sequence.params.clearColor.color;
		for (AreaLightFile.AreaLightEntry entry : lights.entries) {
			entry.clearColor = color;
		}
	}

	private void addSceneResource(G3DResource res) {
		scene.addChild(res.createInstance());
	}

	private G3DResource loadResourceGroup(SeqResources.ResourceAnimationGroup group) {
		NTRGameFS fs = parent.getMC().fs;

		List<NNSG3DResource> rawResources = new ArrayList<>();

		for (int datId : group.dataIDs) {
			FSFile resFile = fs.NARCGet(NARCRef.DEMO3D_RESOURCE, datId);
			if (resFile.exists()) {
				NNSG3DResource rawRes = NNSG3DResource.read(resFile);
				if (rawRes != null) {
					rawResources.add(rawRes);
				}
			}
		}

		return convertResourceGroup(rawResources);
	}

	private NSBMD findNSBMD(List<NNSG3DResource> rawResources) {
		for (NNSG3DResource res : rawResources) {
			if (res instanceof NSBMD) {
				return (NSBMD) res;
			}
		}
		return null;
	}

	private G3DResource convertResourceGroup(List<NNSG3DResource> rawResources) {
		NSBMD nsbmd = findNSBMD(rawResources);
		if (nsbmd == null) {
			return new G3DResource();
		}

		G3DResource mainRes = nsbmd.toGeneric();

		List<Material> materials = new ArrayList<>();
		for (Model model : mainRes.models) {
			materials.addAll(model.materials);
		}

		for (NNSG3DResource res : rawResources) {
			if (res == nsbmd) {
				continue;
			}

			if (res instanceof NSBCA) {
				NSBCA bca = (NSBCA) res;
				for (Model mdl : mainRes.models) {
					if (bca.acceptsModel(mdl)) {
						mainRes.mergeFull(bca.toGeneric(mdl.skeleton));

						break;
					}
				}
			} else if (res instanceof NSBVA) {
				NSBVA bva = (NSBVA) res;
				for (Model mdl : mainRes.models) {
					if (bva.acceptsModel(mdl)) {
						mainRes.mergeFull(bva.toGeneric(mdl.skeleton));

						break;
					}
				}
			} else if (res instanceof NSBTA) {
				mainRes.mergeFull(((NSBTA) res).toGeneric());
			} else if (res instanceof NSBTP) {
				mainRes.mergeFull(((NSBTP) res).toGeneric());
			} else if (res instanceof NSBMA) {
				NSBMA bma = (NSBMA) res;
				bma.forceAlphaBlendToMaterialsIfNeeded(materials);
				mainRes.mergeFull(bma.toGeneric());
			}
		}

		return mainRes;
	}

	public int decideResourceBundle(SeqResources.ResourceConfig.ResourceBundleDecisionType type) {
		VLaunchpad mc = parent.getMC();
		switch (type) {
			case NONE:
				return 0;
			case PLAYER_GENDER:
				return mc.config.playerGender == VPlayerController.PlayerGender.FEMALE ? 1 : 0;
			case SEASON:
				return mc.season.ordinal();
			case USER_PARAM:
				return userParam;
		}
		return 0;
	}

	@Override
	public void run(float frameAdvance) {
		update(frameAdvance);
	}

	public void addToScene(Scene rootScene) {
		rootScene.addChild(scene);
	}

	public void removeFromScene(Scene rootScene) {
		rootScene.removeChild(scene);
	}

	private static enum State {
		PRELOAD,
		START,
		RUN,
		PAUSED,
		END,
		ENDED
	}

	private static class ScheduledCommand implements Comparable<ScheduledCommand> {

		public final ExecutableCommand<?> command;
		public final int frame;
		public final int eventCode;

		public ScheduledCommand(ExecutableCommand<?> command, int frame, int eventCode) {
			this.command = command;
			this.frame = frame;
			this.eventCode = eventCode;
		}

		@Override
		public int compareTo(ScheduledCommand o) {
			if (o.command.getClass() == command.getClass()) {
				if (o.frame == frame) {
					return eventCode - o.eventCode;
				}
			}
			return frame - o.frame;
		}
	}
}
