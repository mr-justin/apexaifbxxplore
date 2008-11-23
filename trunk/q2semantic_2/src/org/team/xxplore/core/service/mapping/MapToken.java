package org.team.xxplore.core.service.mapping;

import org.team.xxplore.core.service.mapping.Node.Type;

public class MapToken {
	public String token1;
	public String token2;
	public Type type;
	
	public MapToken(String token1, String token2, Type type) {
		super();
		this.token1 = token1;
		this.token2 = token2;
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token1 == null) ? 0 : token1.hashCode());
		result = prime * result + ((token2 == null) ? 0 : token2.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		final MapToken other = (MapToken) obj;
		if (token1 == null) {
			if (other.token1 != null)
				return false;
		} else if (!token1.equals(other.token1))
			return false;
		if (token2 == null) {
			if (other.token2 != null)
				return false;
		} else if (!token2.equals(other.token2))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
