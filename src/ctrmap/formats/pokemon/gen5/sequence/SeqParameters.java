package ctrmap.formats.pokemon.gen5.sequence;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.GXColorAlpha;
import xstandard.formats.yaml.YamlNode;
import xstandard.formats.yaml.YamlNodeName;
import xstandard.formats.yaml.YamlReflectUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SeqParameters {

	@YamlNodeName("Camera")
	public CameraConfig camera = new CameraConfig();

	@YamlNodeName("RenderState")
	public SeqRenderState renderState = new SeqRenderState();
	@YamlNodeName("Fog")
	public FogConfig fog = new FogConfig();
	@YamlNodeName("EdgeTable")
	public EdgeTableConfig edgeTable = new EdgeTableConfig();
	@YamlNodeName("ClearColor")
	public ClearColorConfig clearColor = new ClearColorConfig();

	@YamlNodeName("P1")
	public int p1;
	@YamlNodeName("FieldZoneID")
	public int fieldZoneID;
	
	@YamlNodeName("StartFade")
	public FadeConfig startFade;
	@YamlNodeName("EndFade")
	public FadeConfig endFade;
	
	public SeqParameters(DataInput in) throws IOException {
		camera.animeID = in.readUnsignedShort();
		p1 = in.readUnsignedShort();
		fieldZoneID = in.readUnsignedShort();

		int flags = in.readUnsignedShort();
		renderState.isVBlank60FPS = (flags & 1) != 0;
		renderState.isDual3DRender = (flags & 2) != 0;
		renderState.alphaBlend = (flags & 4) != 0;
		renderState.antiAliasing = (flags & 8) != 0;
		fog.enableFog = (flags & 16) != 0;
		edgeTable.enableEdgeColorTable = (flags & 32) != 0;
		renderState.depthSort.manual = (flags & 64) != 0;
		renderState.depthSort.wDepth = (flags & 128) != 0;
		renderState.alphaTest.enabled = (flags & 256) != 0;
		renderState.alphaTest.reference = (flags >> 9) & 31;

		camera.fovX = FX.unfx32(in.readInt());
		camera.fovY = FX.unfx32(in.readInt());

		camera.aspectRatio = FX.unfx32(in.readInt());
		camera.zNear = FX.unfx32(in.readInt());
		camera.zFar = FX.unfx32(in.readInt());
		camera.ndcRange = FX.unfx32(in.readInt());

		camera.topScreenOffset = FX.unfx16(in.readShort());
		camera.bottomScreenOffset = FX.unfx16(in.readShort());

		renderState.disp1DotDepth = FX.unfx32(in.readInt());

		clearColor.color = new GXColorAlpha(in.readUnsignedShort(), in.readUnsignedShort());
		clearColor.polygonID = in.readUnsignedShort();
		clearColor.depth = in.readUnsignedShort();

		fog.color = new GXColorAlpha(in.readUnsignedShort(), in.readUnsignedByte());
		fog.slope = in.readUnsignedByte();
		int blendAndOffset = in.readUnsignedShort();
		fog.blend = blendAndOffset >> 15;
		fog.offset = blendAndOffset & 0x7FFF;

		for (int i = 0; i < 8; i++) {
			fog.fogTable[i] = in.readUnsignedByte();
		}

		for (int i = 0; i < 8; i++) {
			edgeTable.edgeColorTable[i] = new GXColor(in.readUnsignedShort());
		}

		startFade = readFadeConfig(in);
		endFade = readFadeConfig(in);
		in.readShort(); //2-byte padding so that struct has proper outer alignment
	}
	
	public SeqParameters(){
		
	}
	
	public void write(DataOutput out) throws IOException {
		out.writeShort(camera.animeID);
		out.writeShort(p1);
		out.writeShort(fieldZoneID);
		
		int flags = 0;
		flags |= renderState.isVBlank60FPS ? 1 : 0;
		flags |= renderState.isDual3DRender ? 2 : 0;
		flags |= renderState.alphaBlend ? 4 : 0;
		flags |= renderState.antiAliasing ? 8 : 0;
		flags |= fog.enableFog ? 16 : 0;
		flags |= edgeTable.enableEdgeColorTable ? 32 : 0;
		flags |= renderState.depthSort.manual ? 64 : 0;
		flags |= renderState.depthSort.wDepth ? 128 : 0;
		flags |= renderState.alphaTest.enabled ? 256 : 0;
		flags |= renderState.alphaTest.reference << 9;
		out.writeShort(flags);
		
		out.writeInt(FX.fx32(camera.fovX));
		out.writeInt(FX.fx32(camera.fovY));
		
		out.writeInt(FX.fx32(camera.aspectRatio));
		
		out.writeInt(FX.fx32(camera.zNear));
		out.writeInt(FX.fx32(camera.zFar));
		out.writeInt(FX.fx32(camera.ndcRange));

		out.writeShort(FX.fx16(camera.topScreenOffset));
		out.writeShort(FX.fx16(camera.bottomScreenOffset));
		
		out.writeInt(FX.fx32(renderState.disp1DotDepth));
		
		clearColor.color.write(out);
		clearColor.color.writeAlpha(out, true);
		out.writeShort(clearColor.polygonID);
		out.writeShort(clearColor.depth);
		
		fog.color.write(out);
		fog.color.writeAlpha(out, false);
		out.write(fog.slope);
		out.writeShort((fog.blend & 0x7FFF) | ((fog.offset & 1) << 15));
		
		for (int i = 0; i < 8; i++){
			out.write(fog.fogTable[i]);
		}
		
		for (int i = 0; i < 8; i++){
			edgeTable.edgeColorTable[i].write(out);
		}
		
		writeFadeConfig(out, startFade);
		writeFadeConfig(out, endFade);
		out.writeShort(0);
	}
	
	private FadeConfig readFadeConfig(DataInput in) throws IOException {
		FadeConfig fade = new FadeConfig();
		fade.isWhite = in.readBoolean();
		fade.duration = in.readUnsignedByte();
		return fade;
	}
	
	private void writeFadeConfig(DataOutput out, FadeConfig fade) throws IOException {
		out.writeBoolean(fade.isWhite);
		out.write(fade.duration);
	}

	public YamlNode getYMLNode() {
		YamlNode n = YamlReflectUtil.serialize("Parameters", this);
		return n;
	}

	public static class CameraConfig {

		@YamlNodeName("AnimeID")
		public int animeID;
		
		@YamlNodeName("FOVX")
		public float fovX;
		@YamlNodeName("FOVY")
		public float fovY;
		
		@YamlNodeName("AspectRatio")
		public float aspectRatio;
		
		@YamlNodeName("ZFar")
		public float zFar;
		@YamlNodeName("ZNear")
		public float zNear;
		@YamlNodeName("NDCRange")
		public float ndcRange;

		@YamlNodeName("TopScreenOffset")
		public float topScreenOffset;
		@YamlNodeName("BottomScreenOffset")
		public float bottomScreenOffset;
	}

	public static class SeqRenderState {

		@YamlNodeName("IsVBlank60FPS")
		public boolean isVBlank60FPS;
		@YamlNodeName("IsDual3DRender")
		public boolean isDual3DRender;
		@YamlNodeName("AntiAliasing")
		public boolean antiAliasing;
		@YamlNodeName("AlphaBlend")
		public boolean alphaBlend;

		@YamlNodeName("AlphaTest")
		public AlphaTest alphaTest = new AlphaTest();
		@YamlNodeName("DepthSort")
		public DepthSort depthSort = new DepthSort();

		@YamlNodeName("Disp1DotDepth")
		public float disp1DotDepth;

		public static class AlphaTest {

			@YamlNodeName("Enabled")
			public boolean enabled;
			@YamlNodeName("Reference")
			public int reference;
		}

		public static class DepthSort {

			@YamlNodeName("Manual")
			public boolean manual;
			@YamlNodeName("WDepth")
			public boolean wDepth;
		}
	}

	public static class FogConfig {

		@YamlNodeName("EnableFog")
		public boolean enableFog;
		@YamlNodeName("Color")
		public GXColorAlpha color;
		@YamlNodeName("Slope")
		public int slope;
		@YamlNodeName("Blend")
		public int blend;
		@YamlNodeName("Offset")
		public int offset;
		@YamlNodeName("FogTable")
		public int[] fogTable = new int[8];
	}

	public static class EdgeTableConfig {

		@YamlNodeName("EnableEdgeColorTable")
		public boolean enableEdgeColorTable;
		@YamlNodeName("EdgeColorTable")
		public GXColor[] edgeColorTable = new GXColor[8];
	}

	public static class ClearColorConfig {

		@YamlNodeName("Color")
		public GXColorAlpha color;
		@YamlNodeName("PolygonID")
		public int polygonID;
		@YamlNodeName("Depth")
		public int depth;
	}
	
	public static class FadeConfig {
		
		@YamlNodeName("IsWhite")
		public boolean isWhite;
		@YamlNodeName("Duration")
		public int duration;
	}
}
