
package ctrmap.editor.gui.editors.gen5.sequence;

import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.editors.common.tools.BaseTool;
import ctrmap.renderer.scene.Scene;
import javax.swing.JComponent;

public class VSequenceTool extends BaseTool {
	
	private VSequencePlayerEditor editor;
	
	public VSequenceTool(VSequencePlayerEditor ctrl) {
		this.editor = ctrl;
	}

	@Override
	public AbstractToolbarEditor getEditor() {
		return editor;
	}

	@Override
	public JComponent getGUI() {
		return editor;
	}

	@Override
	public String getFriendlyName() {
		return "Sequence player";
	}

	@Override
	public Scene getG3DEx() {
		return null;
	}

	@Override
	public String getResGroup() {
		return "seq";
	}
}
