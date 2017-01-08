package Classes;

public class TargetObject {

	public String targetName;
	public String targetNameCode;
	public String resourcesCode;
	public String sourcesCode;

	public TargetObject(String targetName, String targetNameCode) {
		this(targetName, targetNameCode, null, null);
	}

	public TargetObject(String targetName, String targetNameCode,
			String resourcesCode, String sourcesCode) {
		this.targetName = targetName;
		this.targetNameCode = targetNameCode;
		this.resourcesCode = resourcesCode;
		this.sourcesCode = sourcesCode;
	}

	@Override
	public String toString() {
		return "Target: <" + this.targetName + ">\ntargetCode: <"
				+ this.targetNameCode + "> \nresources Code: <" + this.resourcesCode
				+ ">\nsources Code: <" + this.sourcesCode + ">\n";
	}
}
