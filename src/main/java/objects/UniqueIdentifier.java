package objects;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = StringIdentifier.class)
public abstract class UniqueIdentifier implements Serializable {

	/** Serialisation id. */
	private static final long serialVersionUID = -1223200703636452984L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public abstract boolean equals(Object obj);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public abstract int hashCode();

	/**
	 * @return the unique identifier as a string.
	 */
	public abstract String toString();
}
