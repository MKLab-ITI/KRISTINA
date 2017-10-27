package gr.iti.kristina.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gr.iti.kristina.utils.Utils;

public class Triple implements Serializable {

	private static final long serialVersionUID = 1L;
	public String s, p, o;
	public boolean S_isUri, O_isLiteral;
	public List<Triple> connections;

	public Triple(String s, String p, String o) {
		this.s = s;
		this.p = p;
		this.o = o;

		connections = new ArrayList<>();
	}

	 @Override
	 public String toString() {
	 return "Triple{" + "s=" + s + ", p=" + p + ", o=" + o + '}';
	 }

//	@Override
//	public String toString() {
//		return "Triple{" + "s=" + Utils.substringAfter(s, "#") + ", p=" + Utils.substringAfter(p, "#") + ", o="
//				+ Utils.substringAfter(o, "#") + '}';
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (O_isLiteral ? 1231 : 1237);
		result = prime * result + (S_isUri ? 1231 : 1237);
		result = prime * result + ((connections == null) ? 0 : connections.hashCode());
		result = prime * result + ((o == null) ? 0 : o.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((s == null) ? 0 : s.hashCode());
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
		Triple other = (Triple) obj;
		if (O_isLiteral != other.O_isLiteral)
			return false;
		if (S_isUri != other.S_isUri)
			return false;
		if (connections == null) {
			if (other.connections != null)
				return false;
		} else if (!connections.equals(other.connections))
			return false;
		if (o == null) {
			if (other.o != null)
				return false;
		} else if (!o.equals(other.o))
			return false;
		if (p == null) {
			if (other.p != null)
				return false;
		} else if (!p.equals(other.p))
			return false;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}

	public void add(Triple t) {
		this.connections.add(t);
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public String getP() {
		return p;
	}

	public void setP(String p) {
		this.p = p;
	}

	public String getO() {
		return o;
	}

	public void setO(String o) {
		this.o = o;
	}

	public List<Triple> getConnections() {
		return connections;
	}

	public void setConnections(List<Triple> connections) {
		this.connections = connections;
	}

}
