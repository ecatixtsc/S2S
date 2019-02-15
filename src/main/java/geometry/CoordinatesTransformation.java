package geometry;

import java.util.Set;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.factory.epsg.CartesianAuthorityFactory;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class CoordinatesTransformation {

	public static void transform(String sourceCode, String targetCode)
			throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {

		CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:25829");
		CoordinateReferenceSystem targetCrs = CRS.decode(DefaultEngineeringCRS.GENERIC_2D.toString());

		double x = (double) 636497.59434;
		double y = (double) 4778964.017375;

		boolean lenient = true;
		MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);

		DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, x, y);
		DirectPosition2D destDirectPosition2D = new DirectPosition2D();
		mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);

		double transX = destDirectPosition2D.x;
		double transY = destDirectPosition2D.y;
		System.gc();
	}

	public static void transform1(String sourceCode, String targetCode)
			throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {

		CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:32630");

		String CODE = CartesianAuthorityFactory.GENERIC_2D_CODE;
		CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:" + CODE);

		double x = (double) 651943;
		double y = (double) 5767080;

		boolean lenient = true;
		MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, lenient);

		DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, x, y);
		DirectPosition2D destDirectPosition2D = new DirectPosition2D();
		mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);

		double transX = destDirectPosition2D.x;
		double transY = destDirectPosition2D.y;

		System.gc();
	}
}
