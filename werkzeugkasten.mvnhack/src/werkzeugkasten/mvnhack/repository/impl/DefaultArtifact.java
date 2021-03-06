package werkzeugkasten.mvnhack.repository.impl;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import werkzeugkasten.common.util.StringUtil;
import werkzeugkasten.mvnhack.repository.Artifact;

public class DefaultArtifact implements Artifact {

	protected String groupId = "";

	protected String artifactId = "";

	protected String version = "";

	protected String type = "jar";

	protected boolean optional = false;

	protected Set<Artifact> dependencies = new LinkedHashSet<Artifact>();

	protected Map<String, String> managedDependencies;

	public DefaultArtifact() {
	}

	public DefaultArtifact(Map<String, String> managedDependencies) {
		this.managedDependencies = managedDependencies;
	}

	@Override
	public String getGroupId() {
		return this.groupId;
	}

	protected void setGroupId(String groupId) {
		this.groupId = StringUtil.toString(groupId).trim();
	}

	@Override
	public String getArtifactId() {
		return this.artifactId;
	}

	protected void setArtifactId(String artifactId) {
		this.artifactId = StringUtil.toString(artifactId).trim();
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	protected void setVersion(String version) {
		this.version = StringUtil.toString(version).trim();
	}

	@Override
	public String getType() {
		return this.type;
	}

	protected void setType(String type) {
		this.type = StringUtil.toString(type, "jar").trim();
	}

	@Override
	public boolean isOptional() {
		return this.optional;
	}

	protected void setOptional(boolean optional) {
		this.optional = optional;
	}

	@Override
	public Set<Artifact> getDependencies() {
		return this.dependencies;
	}

	protected void add(Artifact dependency) {
		this.dependencies.add(dependency);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Artifact) {
			Artifact a = (Artifact) obj;
			return equals(a);
		}
		return super.equals(obj);
	}

	public boolean equals(Artifact a) {
		return getGroupId().equals(a.getGroupId())
				&& getArtifactId().equals(a.getArtifactId())
				&& getVersion().equals(a.getVersion());
	}

	@Override
	public int hashCode() {
		StringBuilder stb = new StringBuilder();
		stb.append(getGroupId());
		stb.append(getArtifactId());
		stb.append(getVersion());
		return stb.toString().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append("groupId :");
		stb.append(getGroupId());
		stb.append(" ");
		stb.append("artifactId :");
		stb.append(getArtifactId());
		stb.append(" ");
		stb.append("version :");
		stb.append(getVersion());
		stb.append(" ");
		stb.append("optional :");
		stb.append(isOptional());
		return stb.toString();
	}
}
