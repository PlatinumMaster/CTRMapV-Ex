/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package ctrmap.editor.gui.editors.gen5.level.tools;

import ctrmap.editor.gui.editors.common.tools.BaseTool;
import ctrmap.renderer.scene.Scene;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.editors.gen5.level.entities.VInitScriptEditor;

public class VInitScriptsTool extends BaseTool {
        private VInitScriptEditor ast;
	
	public VInitScriptsTool(VInitScriptEditor ast){
		this.ast = ast;
	}
	
	@Override
	public AbstractToolbarEditor getEditor() {
		return ast;
	}

	@Override
	public JComponent getGUI() {
		return ast;
	}

	@Override
	public String getFriendlyName() {
		return "Init Scripts";
	}

	@Override
	public String getResGroup() {
		return "ScrA";
	}

	@Override
	public Scene getG3DEx() {
		return null;
	}

	@Override
	public void onViewportSwitch(boolean isOrtho) {
	}
}



