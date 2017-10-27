package gr.iti.kristina.model.fusion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Observation implements Comparable<Observation> {

	private String id;
	private String type;
	private String bodyPart;
	private String[] keyEntities;
	private List<Observation> context;
	private double value;
	private String state;
	private Date start, end;
	private Long duration;
	private String CLASS;

	public Observation() {
		context = new ArrayList<>();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBodyPart() {
		return bodyPart;
	}

	public void setBodyPart(String bodyPart) {
		this.bodyPart = bodyPart;
	}

	public String[] getKeyEntities() {
		return keyEntities;
	}

	public void setKeyEntities(String[] keyEntities) {
		this.keyEntities = keyEntities;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
		duration = end.getTime() - start.getTime();
		System.out.println(duration);
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Observation other = (Observation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(Observation o) {
		return this.getStart().compareTo(o.getStart());
	}

	public List<Observation> getContext() {
		return context;
	}

	public void setContext(List<Observation> context) {
		this.context = context;
	}

	public String getCLASS() {
		return CLASS;
	}

	public void setCLASS(String cLASS) {
		CLASS = cLASS;
	}

}
