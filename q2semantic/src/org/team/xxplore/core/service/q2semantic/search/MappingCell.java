package org.team.xxplore.core.service.q2semantic.search;

/**
 * This is the element of a mapping pair. Uri and Datasource
 * @author jqchen
 *
 */
public class MappingCell {
	public String uri;
	public String datasource;
	public MappingCell(String uri, String datasource) {
		super();
		this.uri = uri;
		this.datasource = datasource;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((datasource == null) ? 0 : datasource.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingCell other = (MappingCell) obj;
		if (datasource == null) {
			if (other.datasource != null)
				return false;
		} else if (!datasource.equals(other.datasource))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
}