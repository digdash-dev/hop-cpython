package org.phalanxdev.hop.pipeline.transforms.cpython;

import org.apache.hop.metadata.api.HopMetadataProperty;

public class CPythonInputFrame {

	@HopMetadataProperty(key = "transform_name")
	private String transformName;

	@HopMetadataProperty(key = "frame_name")
	private String frameName;

	public CPythonInputFrame() {
	}

	public CPythonInputFrame(String transformName, String frameName) {
		this.transformName = transformName;
		this.frameName = frameName;
	}

	public String getTransformName() {
		return transformName;
	}

	public void setTransformName(String transformName) {
		this.transformName = transformName;
	}

	public String getFrameName() {
		return frameName;
	}

	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}
}