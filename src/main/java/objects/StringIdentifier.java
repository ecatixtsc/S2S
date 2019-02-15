package objects;

public class StringIdentifier extends UniqueIdentifier {

	/** Serialisation id. */
	private static final long serialVersionUID = -8610663318923490700L;

	/** String uniquely identifying key. */
	private String stringKey;

	/**
	 * Default constructor.
	 */
	public StringIdentifier() {
	}

	/**
	 * 
	 * Default constructor for a simple string identifier.
	 * 
	 * @param uniqueIdentifier the uniquely identifying string
	 */
	public StringIdentifier(final String uniqueIdentifier) {
		stringKey = uniqueIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.catapult.ts.lassie.model.identifier.UniqueIdentifier#equals(java.
	 * lang.Object)
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (obj instanceof StringIdentifier) {
			return stringKey.equals(((StringIdentifier) obj).stringKey);
		}
		return stringKey.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.catapult.ts.lassie.model.identifier.UniqueIdentifier#hashCode()
	 */
	@Override
	public final int hashCode() {
		return stringKey.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.catapult.ts.lassie.model.identifier.UniqueIdentifier#toString()
	 */
	@Override
	public final String toString() {
		return stringKey;
	}

}
